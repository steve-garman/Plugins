/**
 * @license
 * DroidScript GoPro Controller Plugin
 *
 * Copyright 2015 droidscript.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview The GoPro Camera object.
 * @author Chris Hopkin
 */

package com.droidscript.plugins;

import java.net.HttpURLConnection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.droidscript.plugins.GoProDefinitions.AutoPowerOff;
import com.droidscript.plugins.GoProDefinitions.BurstRate;
import com.droidscript.plugins.GoProDefinitions.CameraMode;
import com.droidscript.plugins.GoProDefinitions.FieldOfView;
import com.droidscript.plugins.GoProDefinitions.LEDs;
import com.droidscript.plugins.GoProDefinitions.Orientation;
import com.droidscript.plugins.GoProDefinitions.PhotoMode;
import com.droidscript.plugins.GoProDefinitions.VideoMode;
import com.droidscript.plugins.GoProDefinitions.VideoStandard;

import android.util.Log;

public class GoProCamera
{
	private static class Commands
	{
		public static String NAME = "/bacpac/cv";
		public static String PASSWORD = "/bacpac/sd";
		public static String BACPAC_STATUS = "/bacpac/se";
		public static String SHUTTER = "/bacpac/SH";
		public static String POWER = "/bacpac/PW";
		
		public static String CAMERA_NAME = "/camera/cn";
		public static String CAMERA_INFO = "/camera/cv";
		public static String CAMERA_STATUS = "/camera/se";
		public static String PREVIEW = "/camera/PV";
		public static String VIDEO_MODE = "/camera/VV";
		public static String VIDEO_FPS = "/camera/FS";
		public static String BURST_RATE = "/camera/BU";
		public static String CAMERA_MODE = "/camera/CM";
		public static String DEFAULT_CAMERA_MODE = "/camera/DM";
		public static String ORIENTATION = "/camera/UP";
		public static String PROTUNE = "/camera/PT";
		public static String ONE_BUTTON = "/camera/OB";
		public static String OSD = "/camera/OS";
		public static String VIDEO_STANDARD = "/camera/VM";
		public static String VIDEO_FOV = "/camera/FV";
		public static String PHOTO_MODE = "/camera/PR";
		public static String TIMELAPSE_INTERVAL = "/camera/TI";
		public static String LOCATE = "/camera/LL";
		public static String BEEP_VOLUME = "/camera/BS";
		public static String SPOT_METER = "/camera/EX";
		public static String LEDS = "/camera/LB";
		public static String AUTO_POWER_OFF = "/camera/AO";
		public static String WHITE_BALANCE = "/camera/WB";
		public static String LOOP_VIDEO = "/camera/LO";
	};	
	
	private static String TAG = "GoProCamera";
	private ExecutorService m_executor;
	private GoProCameraListener m_listener;
	private String m_ipAddress;
	private String m_wifiPassword;
	private String m_wifiName;
	private GoProBacpacStatus m_bacpacStatus;
	private Timer m_bacpacStatusTimer;
	private GoProCameraInfo m_cameraInfo;
	
	private static int BACPAC_STATUS_POLL_INTERVAL = 2000;
		
	public GoProCamera(GoProCameraListener listener)
	{
		m_listener = listener;
		m_executor = Executors.newSingleThreadExecutor();
		m_bacpacStatusTimer = new Timer();
	}
	
	// Try to connect to a GoPro camera at the given ip address
	//  1. Request the name from the camera, then if successful, 
    //  2. Request the password from the camera, then if successful,
    //  3. Request the bacpac status - start polling
	public void connect(String ipAddress)
	{
		final GoProCamera localThis = this;
		
		m_ipAddress = ipAddress;
		
		m_executor.submit(new Runnable() {
			@Override
			public void run() 
			{
				synchronized(localThis) // Synchronized with the GoProCamera instance
				{		
					// Cancel the bacpac status polling timer, in case Connect is called
					// when the camera has already been connected.
					m_bacpacStatusTimer.cancel(); // Timers need to be recreated after cancelling
					m_bacpacStatusTimer = null;
					
					m_wifiName = getWifiNameImmediate();
					
					if(m_wifiName != null)
					{
						m_wifiPassword = getWifiPasswordImmediate();
						
						if(m_wifiPassword != null)
						{
							// Get the initial bacpac status before firing the onConnected callback, 
							// this ensures that isPoweredOn, and isReady can be called from the
							// onConnected handler.
							m_bacpacStatus = getBacpacStatusImmediate();
							
							// Fire the OnConnected event
							Log.d(TAG, "Connected to GoPro " + m_wifiName + " at " + m_ipAddress);
							m_listener.onConnected();
							
							if(m_bacpacStatus.isReady())
							{
								// Get the camera info, name, model, firmware
								m_cameraInfo = getCameraInfoImmediate();
								
								m_listener.onReady();
							}
							
							// Now start polling the bacpac status at regular intervals
							startPollingBacpacStatus();
						}
					}
					
					if(m_wifiName == null || m_wifiPassword == null)
					{
						Log.d(TAG, "Failed to connect to GoPro at " + m_ipAddress);
						m_listener.onError(GoProCameraErrors.NOT_FOUND);
					}	
				}
			}			
		});
	}
	
	public void disconnect()
	{
		if(m_bacpacStatusTimer != null)
		{
			m_bacpacStatusTimer.cancel(); // Timers need to be recreated after cancelling
			m_bacpacStatusTimer = null;
		}
		
		m_bacpacStatus = null;
		m_cameraInfo = null;
	}
	
	private void startPollingBacpacStatus()
	{
		m_bacpacStatusTimer = new Timer();
		
		final GoProCamera localThis = this;
		
		m_bacpacStatusTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() 
			{
				m_executor.submit(new Runnable() {
					@Override
					public void run() 
					{
						GoProBacpacStatus status = getBacpacStatusImmediate();
						
						synchronized(localThis) // Synchronized with GoProCamera instance
						{
							if(status == null)
							{
								// Cancel polling timer
								m_bacpacStatusTimer.cancel();
								
								// Camera disconnected
								m_listener.onError(GoProCameraErrors.DISCONNECTED);
							}
							else
							{
								// Fire the onReady callback if the camera ready state has changed from false to true
								boolean hasBecomeReady = false;
								
								if(m_bacpacStatus != null)
								{
									hasBecomeReady = (!m_bacpacStatus.isReady() && status.isReady());
								}
								else
								{
									hasBecomeReady = status.isReady();
								}
								
								if(hasBecomeReady)
								{	
									// Get the camera info, name, model, firmware
									m_cameraInfo = getCameraInfoImmediate();
									
									m_listener.onReady();
								}
							}
							
							m_bacpacStatus = status;
						}
					}
				});
			}								
		}, BACPAC_STATUS_POLL_INTERVAL, BACPAC_STATUS_POLL_INTERVAL);
	}
	
	public boolean isReady()
	{
		synchronized(this)
		{
			if(m_bacpacStatus != null)
			{
				return m_bacpacStatus.isReady();
			}
		}
		
		return false;
	}
	
	public boolean isPowerOn()
	{
		synchronized(this)
		{
			if(m_bacpacStatus != null)
			{
				return m_bacpacStatus.isPowerOn();
			}
		}
		
		return false;
	}
	
	public String getModelName()
	{
		if(m_cameraInfo != null)
		{
			return m_cameraInfo.modelName;
		}
		
		return "Unknown";
	}
	
	public String getFirmware()
	{	
		if(m_cameraInfo != null)
		{
			return m_cameraInfo.firmware;
		}
		
		return "Unknown";
	}
	
	public String getPreviewUrl()
	{
	    return "http://" + m_ipAddress + ":8080/live/amba.m3u8";
	}
	
	private String makeRequestUrl(String command, String param)
	{
		String url = "http://" + m_ipAddress + command + "?t=" + m_wifiPassword;
		
		if(param.length() > 0)
		{
			url += ("&p=" + param);
		}
		
		return url;
	}
	
	public Future<?> execute(final String url)
	{
		return m_executor.submit(new Runnable() {
			@Override
			public void run() 
			{
				HttpUtils.sendRequest(url);
			}
		});
	}
	
	// Non-Blocking call
	public void startPreview()
	{
		execute(makeRequestUrl(Commands.PREVIEW, "%02"));
	}

	// Non-Blocking call
	public void stopPreview()
	{
		execute(makeRequestUrl(Commands.PREVIEW, "%00"));
	}
	
	// Non-Blocking call
	public void startShutter()
	{
		execute(makeRequestUrl(Commands.SHUTTER, "%01"));
	}

	// Non-Blocking call
	public void stopShutter()
	{
		execute(makeRequestUrl(Commands.SHUTTER, "%00"));
	}
	
	// Non-Blocking call
	public void powerOn()
	{
		execute(makeRequestUrl(Commands.POWER, "%01"));
	}
	
	// Non-Blocking call
	public void powerOff()
	{
		execute(makeRequestUrl(Commands.POWER, "%00"));
	}
	
	// Non-Blocking call
	public void startLocate()
	{	
	    execute(makeRequestUrl(Commands.LOCATE, "%01"));
	}
	
	// Non-Blocking call
	public void stopLocate()
	{	
	    execute(makeRequestUrl(Commands.LOCATE, "%00"));
	}
	
	// Non-Blocking call
	public void setCameraMode(CameraMode mode)
	{	
		String param = "";
	    switch(mode)
	    {
	        case Video:
	        	param = "%00";
	            break;
	        case Photo:
	        	param = "%01";
	            break;
	        case Burst:
	        	param = "%02";
	            break;
	        case Timelapse:
	        	param = "%03";
	            break;
	        case Settings:
	        	param = "%07";
	            break;
	        case Playback:
	        	param = "%05";
	        	break;
	        default:
	        	param = "";
	    }
	    
	    if(param.length() > 0)
	    {
	    	execute(makeRequestUrl(Commands.CAMERA_MODE, param));
	    }
	}
	
	// Non-Blocking call
	public void setDefaultCameraMode(CameraMode mode)
	{	
		String param = "";
	    switch(mode)
	    {
	        case Video:
	        	param = "%00";
	            break;
	        case Photo:
	        	param = "%01";
	            break;
	        case Burst:
	        	param = "%02";
	            break;
	        case Timelapse:
	        	param = "%03";
	            break;
	        default:
	        	param = "";
	    }
	    
	    if(param.length() > 0)
	    {
	    	execute(makeRequestUrl(Commands.DEFAULT_CAMERA_MODE, param));
	    }
	}
	
	// Non-Blocking call
	public void setOrientation(Orientation orientation)
	{	
		String param = "";
	    switch(orientation)
	    {
	        case Up:
	        	param = "%00";
	            break;
	        case Down:
	        	param = "%01";
	            break;
	        default:
	        	param = "";
	    }
	    
	    if(param.length() > 0)
	    {
	    	execute(makeRequestUrl(Commands.ORIENTATION, param));
	    }
	}
	
	// Non-Blocking call
	public void setProtune(boolean on)
	{	
		String param = on ? "%01" : "%00";
	    execute(makeRequestUrl(Commands.PROTUNE, param));
	}
	
	// Non-Blocking call
	public void setOneButton(boolean on)
	{	
		Log.d(TAG, "setOneButton " + on);
		
		String param = on ? "%01" : "%00";
	    execute(makeRequestUrl(Commands.ONE_BUTTON, param));
	}
	
	// Non-Blocking call
	public void setOSD(boolean on)
	{	
		String param = on ? "%01" : "%00";
	    execute(makeRequestUrl(Commands.OSD, param));
	}
	
	// Non-Blocking call
	public void setVideoStandard(VideoStandard standard)
	{	
		String param = "";
	    switch(standard)
	    {
	        case NTSC:
	        	param = "%00";
	            break;
	        case PAL:
	        	param = "%01";
	            break;
	        default:
	        	param = "";
	    }
	    
	    if(param.length() > 0)
	    {
	    	execute(makeRequestUrl(Commands.VIDEO_STANDARD, param));
	    }
	}
	
	// Non-Blocking call
	public void setVideoMode(VideoMode mode)
	{	
		Log.d(TAG, "setVideoMode " + mode.toString());
		String param = "";
	    switch(mode)
	    {
	    case VideoMode_WVGA:
	    	param = "%00";
	    	break;
	    case VideoMode_720:
	    	param = "%01";
	    	break;
	    case VideoMode_960:
	    	param = "%02";
	    	break;
	    case VideoMode_1080:
	    	param = "%03";
	    	break;
	    case VideoMode_1440:
	    	param = "%04";
	    	break;
	    case VideoMode_2p7K:
	    	param = "%05";
	    	break;
	    case VideoMode_4K:
	    	param = "%06";
	    	break;
	    case VideoMode_2p7KCinema:
	    	param = "%07";
	    	break;
	    case VideoMode_4KCinema:
	    	param = "%08";
	    	break;
	    case VideoMode_1080SuperView:
	    	param = "%09";
	    	break;
	    case VideoMode_720SuperView:
	    	param = "%0a";
	    	break;
	    default:
	        param = "";
	    }
	    
	    if(param.length() > 0)
	    {
	    	execute(makeRequestUrl(Commands.VIDEO_MODE, param));
	    }
	}
	
	// Non-Blocking call
	public void setVideoFPS(double fps)
	{	
		Log.d(TAG, "setVideoMode " + fps);
		
		String param = "";
	    if(fps == 12)
	    {
	    	param = "%00";
	    }
	    else if(fps == 15)
	    {
	    	param = "%01";
	    }
	    else if(fps == 12.5)
	    {
	    	param = "%0b";
	    }
	    else if(fps == 24)
	    {
	    	param = "%02";
	    }
	    else if(fps == 25)
	    {
	    	param = "%03";
	    }
	    else if(fps == 30)
	    {
	    	param = "%04";
	    }
	    else if(fps == 48)
	    {
	    	param = "%05";
	    }
	    else if(fps == 50)
	    {
	    	param = "%06";
	    }
	    else if(fps == 60)
	    {
	    	param = "%07";
	    }
	    else if(fps == 100)
	    {
	    	param = "%08";
	    }
	    else if(fps == 120)
	    {
	    	param = "%09";
	    }
	    else if(fps == 240)
	    {
	    	param = "%0a";
	    }
	    
	    if(param.length() > 0)
	    {
	    	execute(makeRequestUrl(Commands.VIDEO_FPS, param));
	    }
	}
	
	// Non-Blocking call
	public void setVideoFOV(FieldOfView fov)
	{	
		String param = "";
	    switch(fov)
	    {
	    case Wide:
	    	param = "%00";
	    	break;
	    case Medium:
	    	param = "%01";
	    	break;
	    case Narrow:
	    	param = "%02";
	    	break;
	    default:
	        param = "";
	    }
	    
	    if(param.length() > 0)
	    {
	    	execute(makeRequestUrl(Commands.VIDEO_FOV, param));
	    }
	}
	
	// Non-Blocking call
	public void setPhotoMode(PhotoMode photoMode)
	{	
		String param = "";
	    switch(photoMode)
	    {
	    case PhotoMode_5mpWide:
	    	param = "%02";
	    	break;
	    case PhotoMode_5mpMedium:
	    	param = "%03";
	    	break;
	    case PhotoMode_7mpWide:
	    	param = "%04";
	    	break;
	    case PhotoMode_7mpMedium:
	    	param = "%06";
	    	break;
	    case PhotoMode_8mpMedium:
	    	param = "%01";
	    	break;
	    case PhotoMode_11mpWide:
	    	param = "%00";
	    	break;
	    case PhotoMode_12mpWide:
	    	param = "%05";
	    	break;
	    default:
	        param = "";
	    }
	    
	    if(param.length() > 0)
	    {
	    	execute(makeRequestUrl(Commands.PHOTO_MODE, param));
	    }
	}
	
	// Non-Blocking call
	public void setBurstRate(BurstRate burstRate)
	{	
		Log.d(TAG, "setBurstRate " + burstRate.toString());
		
		String param = "";
	    switch(burstRate)
	    {
	    case BurstRate_3_1s:
	    	param = "%00";
	    	break;
	    case BurstRate_5_1s:
	    	param = "%01";
	    	break;
	    case BurstRate_10_1s:
	    	param = "%02";
	    	break;
	    case BurstRate_10_2s:
	    	param = "%03";
	    	break;
	    case BurstRate_30_1s:
	    	param = "%04";
	    	break;
	    case BurstRate_30_2s:
	    	param = "%05";
	    	break;
	    case BurstRate_30_3s:
	    	param = "%06";
	    	break;
	    default:
	        param = "";
	    }
	    
	    if(param.length() > 0)
	    {
	    	execute(makeRequestUrl(Commands.BURST_RATE, param));
	    }
	}
	
	// Non-Blocking call
	public void setTimelapseInterval(double interval)
	{	
		String param = "";
	    if(interval == 0.5)
	    {
	    	param = "%00";
	    }
	    else if(interval == 1)
	    {
	    	param = "%01";
	    }
	    else if(interval == 2)
	    {
	    	param = "%02";
	    }
	    else if(interval == 5)
	    {
	    	param = "%05";
	    }
	    else if(interval == 10)
	    {
	    	param = "%0a";
	    }
	    else if(interval == 20)
	    {
	    	param = "%14";
	    }
	    else if(interval == 30)
	    {
	    	param = "%1e";
	    }
	    else if(interval == 60)
	    {
	    	param = "%3c";
	    }
	    
	    if(param.length() > 0)
	    {
	    	execute(makeRequestUrl(Commands.TIMELAPSE_INTERVAL, param));
	    }
	}
	
	// Non-Blocking call
	public void setBeepVolume(int volume)
	{	
		String param = "%00";
		
		if(volume > 0 && volume <= 70)
		{
			param = "%01"; // 70%
		}
		else if(volume > 70)
		{
			param = "%02"; // 100%
		}
		
	    execute(makeRequestUrl(Commands.BEEP_VOLUME, param));
	}
	
	// Non-Blocking call
	public void setSpotMeter(boolean on)
	{	
		String param = on ? "%01" : "%00";
	    execute(makeRequestUrl(Commands.SPOT_METER, param));
	}
	
	// Non-Blocking call
	public void setLEDs(LEDs leds)
	{	
		String param = "";
	    switch(leds)
	    {
	    case LEDs_Off:
	    	param = "%00";
	    	break;
	    case LEDs_2:
	    	param = "%01";
	    	break;
	    case LEDs_4:
	    	param = "%02";
	    	break;
	    default:
	        param = "";
	    }
	    
	    if(param.length() > 0)
	    {
	    	execute(makeRequestUrl(Commands.LEDS, param));
	    }
	}
	
	// Non-Blocking call
	public void setAutoPowerOff(AutoPowerOff autoPowerOff)
	{	
		String param = "";
	    switch(autoPowerOff)
	    {
	    case AutoPowerOff_Never:
	    	param = "%00";
	    	break;
	    case AutoPowerOff_60:
	    	param = "%01";
	    	break;
	    case AutoPowerOff_120:
	    	param = "%02";
	    	break;
	    case AutoPowerOff_300:
	    	param = "%03";
	    	break;
	    default:
	        param = "";
	    }
	    
	    if(param.length() > 0)
	    {
	    	execute(makeRequestUrl(Commands.AUTO_POWER_OFF, param));
	    }
	}
	
	// Non-Blocking fire and forget call
	// Request the camera status, fires the onCameraStatus callback on the GoProCameraListener if successful
	public void getCameraStatus()
	{
		m_executor.submit(new Runnable() {
			@Override
			public void run() 
			{
				GoProCameraStatus status = getCameraStatusImmediate();
				if(status != null)
				{
					m_listener.onCameraStatus(status);
				}
			}
		});
	}
	
	// Blocking call
	// Request and return the camera status
	public GoProCameraStatus getCameraStatusImmediate()
	{
		HttpURLConnection httpConnection = null;
		
		int[] statusBytes = null;

        try
        {
        	httpConnection = HttpUtils.openHttpConnection("http://" + m_ipAddress + Commands.CAMERA_STATUS + "?t=" + m_wifiPassword, 5000);

        	statusBytes = HttpUtils.responseToUIntArray(httpConnection);       	
        }
        catch(Exception e)
        {
        	Log.e(TAG, "getCameraStatusImmediate", e);
        	statusBytes = null;
        }
        finally
        {
        	if(httpConnection != null)
        	{
        		httpConnection.disconnect();
        	}
        }
        
        // If successfully read the status bytes...
        if(statusBytes != null)
        {
        	try
            {
        		int[] videoModeBytes = getVideoModeImmediate();
        		int[] videoFPSBytes = getVideoFrameRateImmediate();
        		int[] burstRateBytes = getBurstRateImmediate();
        		
        		GoProCameraStatus status = new GoProCameraStatus(statusBytes, videoModeBytes, videoFPSBytes, burstRateBytes);
        		
        		return status;
            }
        	catch(Exception e)
        	{
        		Log.e(TAG, "getCameraStatusImmediate", e);
        	}
        }
        
        return null;
	}
	
	// Blocking call
	// Request and return the password
	private String getWifiNameImmediate()
	{    
		String name = null;
		HttpURLConnection httpConnection = null;

        try
        {
        	httpConnection = HttpUtils.openHttpConnection("http://" + m_ipAddress + Commands.NAME, 5000);

        	name = HttpUtils.responseToString(httpConnection);
        }
        catch(Exception e)
        {
        	Log.e(TAG, "getWifiNameImmediate", e);
        }
        finally
        {
        	if(httpConnection != null)
        	{
        		httpConnection.disconnect();
        	}
        }
        
        return name;
	}
	
	// Blocking call
	// Request and return the password
	private String getWifiPasswordImmediate()
	{    
		String password = null;
		HttpURLConnection httpConnection = null;

        try
        {
        	httpConnection = HttpUtils.openHttpConnection("http://" + m_ipAddress + Commands.PASSWORD, 5000);

        	// Strip off the first 2 characters of the response to 
            // get the password - these appear to represent the
            // password length
        	password = HttpUtils.responseToString(httpConnection);       	
        	password = password.substring(2);
        }
        catch(Exception e)
        {
        	Log.e(TAG, "getWifiPasswordImmediate", e);
        }
        finally
        {
        	if(httpConnection != null)
        	{
        		httpConnection.disconnect();
        	}
        }
        
        return password;
	}
	
	// Blocking call
	// Request and return the bacpac status
	private GoProBacpacStatus getBacpacStatusImmediate()
	{
		GoProBacpacStatus status = null;
		HttpURLConnection httpConnection = null;

        try
        {
        	httpConnection = HttpUtils.openHttpConnection("http://" + m_ipAddress + Commands.BACPAC_STATUS + "?t=" + m_wifiPassword, 5000);

        	int[] statusBytes = HttpUtils.responseToUIntArray(httpConnection);
        	status = new GoProBacpacStatus(statusBytes);
        }
        catch(Exception e)
        {
        	Log.e(TAG, "getBacpacStatusImmediate", e);
        	status = null;
        }
        finally
        {
        	if(httpConnection != null)
        	{
        		httpConnection.disconnect();
        	}
        }
        
        return status;
	}
	
	// Blocking call
	// Request and return the Camera Info - name, model, firmware
	private GoProCameraInfo getCameraInfoImmediate()
	{
		GoProCameraInfo info = null;
		HttpURLConnection httpConnection = null;

        try
        {
        	httpConnection = HttpUtils.openHttpConnection("http://" + m_ipAddress + Commands.CAMERA_INFO + "?t=" + m_wifiPassword, 5000);

        	String rawResponse = HttpUtils.responseToString(httpConnection);
        	info = new GoProCameraInfo(rawResponse);
        }
        catch(Exception e)
        {
        	Log.e(TAG, "getCameraInfoImmediate", e);
        	info = null;
        }
        finally
        {
        	if(httpConnection != null)
        	{
        		httpConnection.disconnect();
        	}
        }
        
        return info;
	}
	
	// Blocking call
	private int[] getVideoModeImmediate()
	{
		int[] videoModeBytes = null;		
		HttpURLConnection httpConnection = null;

        try
        {
        	// Make sure the command is lowercase for getting (uppercase for setting)
        	httpConnection = HttpUtils.openHttpConnection("http://" + m_ipAddress + Commands.VIDEO_MODE.toLowerCase() + "?t=" + m_wifiPassword, 5000);

        	videoModeBytes = HttpUtils.responseToUIntArray(httpConnection);
        }
        catch(Exception e)
        {
        	Log.e(TAG, "getVideoModeImmediate", e);
        	videoModeBytes = null;
        }
        finally
        {
        	if(httpConnection != null)
        	{
        		httpConnection.disconnect();
        	}
        }
        
        return videoModeBytes;
	}
	
	// Blocking call
	private int[] getVideoFrameRateImmediate()
	{
		int[] videoFrameRateBytes = null;		
		HttpURLConnection httpConnection = null;

        try
        {
        	// Make sure the command is lowercase for getting (uppercase for setting)
        	httpConnection = HttpUtils.openHttpConnection("http://" + m_ipAddress + Commands.VIDEO_FPS.toLowerCase() + "?t=" + m_wifiPassword, 5000);

        	videoFrameRateBytes = HttpUtils.responseToUIntArray(httpConnection);
        }
        catch(Exception e)
        {
        	Log.e(TAG, "getVideoFrameRateImmediate", e);
        	videoFrameRateBytes = null;
        }
        finally
        {
        	if(httpConnection != null)
        	{
        		httpConnection.disconnect();
        	}
        }
        
        return videoFrameRateBytes;
	}
	
	// Blocking call
	private int[] getBurstRateImmediate()
	{
		int[] burstRateBytes = null;		
		HttpURLConnection httpConnection = null;

        try
        {
        	// Make sure the command is lowercase for getting (uppercase for setting)
        	httpConnection = HttpUtils.openHttpConnection("http://" + m_ipAddress + Commands.BURST_RATE.toLowerCase() + "?t=" + m_wifiPassword, 5000);

        	burstRateBytes = HttpUtils.responseToUIntArray(httpConnection);
        }
        catch(Exception e)
        {
        	Log.e(TAG, "getBurstRateImmediate", e);
        	burstRateBytes = null;
        }
        finally
        {
        	if(httpConnection != null)
        	{
        		httpConnection.disconnect();
        	}
        }
        
        return burstRateBytes;
	}	
};