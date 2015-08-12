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
 * @fileoverview Represents the GoPro Camera version information.
 * @author Chris Hopkin
 */

package com.droidscript.plugins;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class GoProCameraVersion 
{
//	"info version":"1.1",
//	"firmware version":"HD3.02.03.00",
//	"wifi version":"3.4.2.18",
//	"wifi bootloader version":"0.2.2",
//	"wifi mac":"d896852ef6d4",
//	"camera type":"Hero3-Silver Edition"
	
	private static String TAG = "GoProCameraVersion";
	
	public boolean isValid = false;
	public String infoVersion;
	public String firmwareVersion;
	public String wifiVersion;
	public String wifiBootloaderVersion;
	public String wifiMac;
	public String cameraType;
	
	private static int TIMEOUT = 10000;

	public GoProCameraVersion(String ip, String port)
	{
		JSONObject version = fetchUsingMethod1(ip, port);
		
		if(version == null)
		{
			version = fetchUsingMethod2(ip, port);
		}
		
		if(version != null)
		{
			try 
			{
				infoVersion = version.getString("info version");
				firmwareVersion = version.getString("firmware version");
				wifiVersion = version.getString("wifi version");
				wifiBootloaderVersion = version.getString("wifi bootloader version");
				wifiMac = version.getString("wifi mac");
				cameraType = version.getString("camera type");
				
				isValid = true;
			} 
			catch (JSONException e) 
			{
				Log.e(TAG, "Construct GoProCameraVersion", e);
				
				isValid = false;
			}
		}
	}
	
	// Try "http://10.5.5.9:8080/videos/MISC/version.txt"
	private JSONObject fetchUsingMethod1(String ip, String port)
	{
		JSONObject version = null;
		HttpURLConnection httpConnection = null;
		
		try 
		{
			httpConnection = HttpUtils.openHttpConnection("http://" + ip + ":" + port + "/videos/MISC/version.txt", TIMEOUT);
			
			version = HttpUtils.responseToJSON(httpConnection);
		} 
		catch (IOException e) 
		{
			Log.e(TAG, "fetchUsingMethod1", e);
		}
		finally
        {
        	
        	if(httpConnection != null)
        	{
        		httpConnection.disconnect();
        	}
        }
		
		return version;
	}
	
	// Try "http://10.5.5.9:8080/MISC/version.txt"
	private JSONObject fetchUsingMethod2(String ip, String port)
	{
		JSONObject version = null;
		HttpURLConnection httpConnection = null;
		
		try 
		{
			httpConnection = HttpUtils.openHttpConnection("http://" + ip + ":" + port + "/MISC/version.txt", TIMEOUT);
			
			version = HttpUtils.responseToJSON(httpConnection);
		} 
		catch (IOException e) 
		{
			Log.e(TAG, "fetchUsingMethod2", e);
		}
		finally
        {
        	
        	if(httpConnection != null)
        	{
        		httpConnection.disconnect();
        	}
        }
		
		return version;
	}
}
