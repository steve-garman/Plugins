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
 * @fileoverview Converts GoPro Camera settings to and from JSON representations.
 * @author Chris Hopkin
 */

package com.droidscript.plugins;

import org.json.JSONException;
import org.json.JSONObject;

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

public class GoProJSON 
{
	private static String TAG = "GoProJSON";
	
	private static class GoProJSONLabels 
	{
		public static String PREVIEW = "Preview";
		public static String ORIENTATION = "Orientation";
		public static String ONE_BUTTON = "OneButton";
		public static String OSD = "OSD";
		public static String VIDEO_STANDARD = "VideoStandard";
		public static String PROTUNE = "Protune";
		public static String CAMERA_MODE = "CameraMode";
		public static String DEFAULT_CAMERA_MODE = "DefaultCameraMode";
		public static String VIDEO_MODE = "VideoMode";
		public static String VIDEO_FPS = "VideoFPS";
		public static String VIDEO_FOV = "VideoFOV";
		public static String PHOTO_MODE = "PhotoMode";
		public static String BURST_RATE = "BurstRate";
		public static String TIMELAPSE_INTERVAL = "TimelapseInterval";
		public static String PHOTO_COUNT = "PhotoCount";
		public static String PHOTOS_AVAILABLE = "PhotosAvailable";
		public static String VIDEO_AVAILABLE_TIME = "VideoAvailableTime";
		public static String VIDEO_RECORDING_TIME = "VideoRecordingTime";
		public static String VIDEO_COUNT = "VideoCount";
		public static String BATTERY_LEVEL = "BatteryLevel";
		public static String VIDEO_RECORDING = "VideoRecording";
		public static String BURST_RECORDING = "BurstRecording";
		public static String SD_CARD = "SDCard";
		public static String LOCATE = "Locate";
		public static String AUTO_POWER_OFF = "AutoPowerOff";
		public static String BEEP_VOLUME = "BeepVolume";
		public static String SPOT_METER = "SpotMeter";
		public static String LEDS = "LEDs";
	};
	
	public static JSONObject getJSONCameraStatus(GoProCameraStatus status)
	{
		JSONObject jsonStatus = new JSONObject();

		try
		{
			// Video Properties
			jsonStatus.put(GoProJSONLabels.VIDEO_STANDARD, status.videoStandard.toString());
			jsonStatus.put(GoProJSONLabels.VIDEO_MODE, VideoModeToString(status.videoMode));
			jsonStatus.put(GoProJSONLabels.VIDEO_FPS, status.videoFrameRate);
			jsonStatus.put(GoProJSONLabels.VIDEO_FOV, status.videoFOV.toString());
			jsonStatus.put(GoProJSONLabels.VIDEO_COUNT, status.videoCount);
			jsonStatus.put(GoProJSONLabels.VIDEO_AVAILABLE_TIME, status.videoAvailableTime);
			jsonStatus.put(GoProJSONLabels.VIDEO_RECORDING, status.videoRecording ? "On" : "Off");
			jsonStatus.put(GoProJSONLabels.VIDEO_RECORDING_TIME, status.videoRecordingTime);
			
			// Photo Properties
			jsonStatus.put(GoProJSONLabels.PHOTO_MODE, PhotoModeToString(status.photoMode));
			jsonStatus.put(GoProJSONLabels.PHOTO_COUNT, status.photoCount);
			jsonStatus.put(GoProJSONLabels.PHOTOS_AVAILABLE, status.photosAvailable);
			
			// Burst Properties
			jsonStatus.put(GoProJSONLabels.BURST_RATE, BurstRateToString(status.burstRate));
			jsonStatus.put(GoProJSONLabels.BURST_RECORDING, status.burstRecording ? "On" : "Off");
			
			// Timelapse Properties
			jsonStatus.put(GoProJSONLabels.TIMELAPSE_INTERVAL, status.timelapseInterval);
			
			// General Properties
			jsonStatus.put(GoProJSONLabels.CAMERA_MODE, status.cameraMode.toString());
			jsonStatus.put(GoProJSONLabels.DEFAULT_CAMERA_MODE, status.defaultCameraMode.toString());
			jsonStatus.put(GoProJSONLabels.PREVIEW, status.previewOn ? "On" : "Off");
			jsonStatus.put(GoProJSONLabels.ORIENTATION, OrientationToString(status.orientation));
			//jsonStatus.put(GoProJSONLabels.ONE_BUTTON, status.oneButtonOn ? "On" : "Off"); // IGNORE ONE BUTTON (N/A when under remote control)
			jsonStatus.put(GoProJSONLabels.OSD, status.osdOn ? "On" : "Off");
			jsonStatus.put(GoProJSONLabels.LOCATE, status.locate ? "On" : "Off");
			jsonStatus.put(GoProJSONLabels.PROTUNE, status.protuneOn ? "On" : "Off");
			jsonStatus.put(GoProJSONLabels.BATTERY_LEVEL, status.batteryLevel);
			jsonStatus.put(GoProJSONLabels.SD_CARD, status.sdCard ? "Yes" : "No");
			jsonStatus.put(GoProJSONLabels.AUTO_POWER_OFF, AutoPowerOffToString(status.autoPowerOff));
			jsonStatus.put(GoProJSONLabels.BEEP_VOLUME, status.beepVolume);
			jsonStatus.put(GoProJSONLabels.LEDS, LEDsToString(status.leds));
			jsonStatus.put(GoProJSONLabels.SPOT_METER, status.spotMeter ? "On" : "Off");
		} 
		catch (Exception e) 
		{
			Log.e(TAG, "getJSONCameraStatus", e);
			jsonStatus = null;
		}
		
		return jsonStatus;
	}
	
	public static void setCameraOptionsFromJSON(GoProCamera camera, JSONObject options) throws JSONException
	{
		try
		{
	        // These properties should be set first since they will have an effect on the available Video settings
	        if(options.has(GoProJSONLabels.CAMERA_MODE))
	        {
	        	camera.setCameraMode(CameraModeFromString(options.getString(GoProJSONLabels.CAMERA_MODE)));
	        }
	        if(options.has(GoProJSONLabels.PROTUNE))
	        {
	        	camera.setProtune(BoolFromString(options.getString(GoProJSONLabels.PROTUNE)));
	        }
	        
	        // Video Properties
	        if(options.has(GoProJSONLabels.VIDEO_STANDARD))
			{
				camera.setVideoStandard(VideoStandardFromString(options.getString(GoProJSONLabels.VIDEO_STANDARD)));
			}
	        if(options.has(GoProJSONLabels.VIDEO_MODE))
	        {
	        	camera.setVideoMode(VideoModeFromString(options.getString(GoProJSONLabels.VIDEO_MODE)));
	        }
	        if(options.has(GoProJSONLabels.VIDEO_FPS))
	        {
	        	camera.setVideoFPS(options.getDouble(GoProJSONLabels.VIDEO_FPS));
	        }
	        if(options.has(GoProJSONLabels.VIDEO_FOV))
	        {
	        	camera.setVideoFOV(FieldOfViewFromString(options.getString(GoProJSONLabels.VIDEO_FOV)));
	        }
			
			// Photo Properties
	        if(options.has(GoProJSONLabels.PHOTO_MODE))
	        {
	        	camera.setPhotoMode(PhotoModeFromString(options.getString(GoProJSONLabels.PHOTO_MODE)));
	        }
			
			// Burst Properties
	        if(options.has(GoProJSONLabels.BURST_RATE))
	        {
	        	camera.setBurstRate(BurstRateFromString(options.getString(GoProJSONLabels.BURST_RATE)));
	        }
	        
			// Timelapse Properties
	        if(options.has(GoProJSONLabels.TIMELAPSE_INTERVAL))
	        {
	        	camera.setTimelapseInterval(options.getDouble(GoProJSONLabels.TIMELAPSE_INTERVAL));
	        }
			
			// General Properties
	        if(options.has(GoProJSONLabels.ORIENTATION))
			{
				Orientation orientation = OrientationFromString(options.getString(GoProJSONLabels.ORIENTATION));
				camera.setOrientation(orientation);
			}
	        // IGNORE ONE BUTTON (N/A when under remote control)
//	        if(options.has(GoProJSONLabels.ONE_BUTTON))
//			{
//				camera.setOneButton(BoolFromString(options.getString(GoProJSONLabels.ONE_BUTTON)));
//			}
	        if(options.has(GoProJSONLabels.OSD))
			{
				camera.setOSD(BoolFromString(options.getString(GoProJSONLabels.OSD)));
			}
	        if(options.has(GoProJSONLabels.DEFAULT_CAMERA_MODE))
			{
				camera.setDefaultCameraMode(CameraModeFromString(options.getString(GoProJSONLabels.DEFAULT_CAMERA_MODE)));
			}
	        if(options.has(GoProJSONLabels.BEEP_VOLUME))
			{
				camera.setBeepVolume(options.getInt(GoProJSONLabels.BEEP_VOLUME));
			}
	        if(options.has(GoProJSONLabels.SPOT_METER))
			{
				camera.setSpotMeter(BoolFromString(options.getString(GoProJSONLabels.SPOT_METER)));
			}
	        if(options.has(GoProJSONLabels.LEDS))
			{
				camera.setLEDs(LEDsFromString(options.getString(GoProJSONLabels.LEDS)));
			}
	        if(options.has(GoProJSONLabels.AUTO_POWER_OFF))
			{
				camera.setAutoPowerOff(AutoPowerOffFromString(options.getString(GoProJSONLabels.AUTO_POWER_OFF)));
			}
		} 
		catch (Exception e) 
		{
			Log.e(TAG, "setCameraOptionsFromJSON", e);
		}
	}
	
	private static String VideoModeToString(VideoMode videoMode)
	{
		String videoModeString = videoMode.toString();
		videoModeString = videoModeString.replace("VideoMode_", ""); // VideoMode_2p7KCinema -> 2p7KCinema
		videoModeString = videoModeString.replace('p', '.'); // 2p7KCinema -> 2.7KCinema
		
		return videoModeString;
	}
	
	private static VideoMode VideoModeFromString(String videoModeString)
	{
		videoModeString = "VideoMode_" + videoModeString;
		videoModeString = videoModeString.replace('.', 'p');
		
		for(VideoMode mode : VideoMode.values()) 
		{
			if(videoModeString.compareToIgnoreCase(mode.toString()) == 0)
			{
				return mode;
			}
		}
		
		return VideoMode.VideoMode_Unknown;
	}
	
	private static String PhotoModeToString(PhotoMode photoMode)
	{
		String photoModeString = photoMode.toString();
		photoModeString = photoModeString.replace("PhotoMode_", "");
		return photoModeString;
	}
	
	private static PhotoMode PhotoModeFromString(String photoModeString)
	{
		photoModeString = "PhotoMode_" + photoModeString;
		
		for(PhotoMode mode : PhotoMode.values()) 
		{
			if(photoModeString.compareToIgnoreCase(mode.toString()) == 0)
			{
				return mode;
			}
		}
		
		return PhotoMode.PhotoMode_Unknown;
	}
	
	private static String BurstRateToString(BurstRate burstRate)
	{
		String burstRateString = burstRate.toString();
		burstRateString = burstRateString.replace("BurstRate_", "");
		burstRateString = burstRateString.replace("_", "/");
		return burstRateString;
	}
	
	private static BurstRate BurstRateFromString(String burstRateString)
	{
		burstRateString = "BurstRate_" + burstRateString;
		burstRateString = burstRateString.replace("/", "_");
		
		for(BurstRate mode : BurstRate.values()) 
		{
			if(burstRateString.compareToIgnoreCase(mode.toString()) == 0)
			{
				return mode;
			}
		}
		
		return BurstRate.BurstRate_Unknown;
	}
	
	private static CameraMode CameraModeFromString(String modeString)
	{
		for(CameraMode mode : CameraMode.values()) 
		{
			if(modeString.compareToIgnoreCase(mode.toString()) == 0)
			{
				return mode;
			}
		}
		
		return CameraMode.Unknown;
	}
	
	private static String OrientationToString(Orientation orientation)
	{
		return orientation.toString();
	}
	
	private static Orientation OrientationFromString(String orientationString)
	{
		for(Orientation orientation : Orientation.values()) 
		{
			if(orientationString.compareToIgnoreCase(orientation.toString()) == 0)
			{
				return orientation;
			}
		}
		
		return Orientation.Unknown;
	}
	
	private static boolean BoolFromString(String boolString)
	{
		if(boolString.compareToIgnoreCase("On") == 0 ||
		   boolString.compareToIgnoreCase("True") == 0)
		{
			return true;
		}
		else if(boolString.compareToIgnoreCase("Off") == 0 ||
				boolString.compareToIgnoreCase("False") == 0)
		{
			return false;
		}
		
		throw new IllegalArgumentException("BoolFromString(" + boolString + ") - failed to convert");
	}
	
	private static VideoStandard VideoStandardFromString(String videoStandardString)
	{
		for(VideoStandard videoStandard : VideoStandard.values()) 
		{
			if(videoStandardString.compareToIgnoreCase(videoStandard.toString()) == 0)
			{
				return videoStandard;
			}
		}
		
		return VideoStandard.Unknown;
	}
	
	private static String AutoPowerOffToString(AutoPowerOff autoPowerOff)
	{
		String autoPowerOffString = autoPowerOff.toString();
		autoPowerOffString = autoPowerOffString.replace("AutoPowerOff_", "");
		
		return autoPowerOffString;
	}
	
	private static AutoPowerOff AutoPowerOffFromString(String autoPowerOffString)
	{
		autoPowerOffString = "AutoPowerOff_" + autoPowerOffString;
		
		for(AutoPowerOff autoPowerOff : AutoPowerOff.values()) 
		{
			if(autoPowerOffString.compareToIgnoreCase(autoPowerOff.toString()) == 0)
			{
				return autoPowerOff;
			}
		}
		
		return AutoPowerOff.AutoPowerOff_Unknown;
	}
	
	private static String LEDsToString(LEDs leds)
	{
		String ledsString = leds.toString();
		ledsString = ledsString.replace("LEDs_", "");
		
		return ledsString;
	}
	
	private static LEDs LEDsFromString(String ledsString)
	{
		ledsString = "LEDs_" + ledsString;
		
		for(LEDs led : LEDs.values()) 
		{
			if(ledsString.compareToIgnoreCase(led.toString()) == 0)
			{
				return led;
			}
		}
		
		return LEDs.LEDs_Unknown;
	}
	
	private static FieldOfView FieldOfViewFromString(String fieldOfViewString)
	{
		for(FieldOfView fieldOfView : FieldOfView.values()) 
		{
			if(fieldOfViewString.compareToIgnoreCase(fieldOfView.toString()) == 0)
			{
				return fieldOfView;
			}
		}
		
		return FieldOfView.Unknown;
	}
}
