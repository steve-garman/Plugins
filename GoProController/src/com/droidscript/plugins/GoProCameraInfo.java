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
 * @fileoverview Represents the GoPro Camera model information.
 * @author Chris Hopkin
 */

package com.droidscript.plugins;

import android.util.Log;

public class GoProCameraInfo 
{
	private String TAG = "GoProCameraInfo";
	
	public String modelName = "Unknown";
	public String modelId = "Unknown";
	public String firmware = "Unknown";
	
	public GoProCameraInfo(String raw)
	{		
		try
		{
			// Drop the first 4 characters, don't seem to be part of the content
			String trimmed = raw.substring(4);
			
			// trimmed looks something like this HD3.02.03.00HERO3
			
			// The name HERO3 is separated from the other part of the string by \u0005 character
			String[] info = trimmed.split("\u0005");
			
			if(info != null && info.length > 1)
			{
				// The model is the first 2 parts of HD3.02.03.00
				String[] modelAndFirmware = info[0].split("\\.");
				
				if(modelAndFirmware != null && modelAndFirmware.length >= 2)
				{
					modelId = modelAndFirmware[0] + "." + modelAndFirmware[1];
					
					modelName = getModelName(modelId);
				
					// The firmware version is the remaining parts - 03.00
					firmware = "";
					for(int i = 2; i < modelAndFirmware.length; ++i)
					{
						firmware += modelAndFirmware[i];
						firmware += ".";
					}
					
					int dotIndex = firmware.lastIndexOf(".");
				    if(dotIndex != -1)  
				    {
				    	firmware = firmware.substring(0, dotIndex);
				    }
				}
			}
		}
		catch(Exception e)
		{
			Log.e(TAG, "Construct with " + raw, e);
		}	
	}
	
	private static String getModelName(String modelId)
	{		
		if(modelId.compareToIgnoreCase("HD2.08") == 0)
		{
			return "HD HERO2";
		}
		if(modelId.compareToIgnoreCase("HD3.01") == 0)
		{
			return "HERO3 White Edition";
		}
		if(modelId.compareToIgnoreCase("HD3.02") == 0)
		{
			return "HERO3 Silver Edition";
		}
		if(modelId.compareToIgnoreCase("HD3.03") == 0)
		{
			return "HERO3 Black Edition";
		}
		if(modelId.compareToIgnoreCase("HD3.10") == 0)
		{
			return "HERO3+ Silver Edition";
		}
		if(modelId.compareToIgnoreCase("HD3.11") == 0)
		{
			return "HERO3+ Black Edition";
		}
		if(modelId.compareToIgnoreCase("HD4.01") == 0)
		{
			return "HERO4 Silver Edition";
		}
		if(modelId.compareToIgnoreCase("HD4.02") == 0)
		{
			return "HERO4 Black Edition";
		}
		
		// Return the modelId as a last resort
		return modelId;
	}
}
