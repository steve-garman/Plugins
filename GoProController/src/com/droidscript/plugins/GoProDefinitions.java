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
 * @fileoverview GoPro Camera settings definitions.
 * @author Chris Hopkin
 */

package com.droidscript.plugins;

public class GoProDefinitions 
{
	enum AutoPowerOff
	{
		AutoPowerOff_Unknown,
		AutoPowerOff_Never,
		AutoPowerOff_60,
		AutoPowerOff_120,
		AutoPowerOff_300
	}
	
	enum LEDs
	{
		LEDs_Unknown,
		LEDs_Off,
		LEDs_2,
		LEDs_4
	}
	
	enum VideoStandard
	{
		Unknown,
		PAL,
		NTSC
	}
	
	enum Orientation
	{
		Unknown,
		Up,
		Down
	}
	
	enum CameraMode
	{
		Unknown,
		Video,
		Photo,
		Burst,
		Timelapse,
		Timer, // HERO2
		Playback,
		Settings
	}
	
	enum FieldOfView
	{
		Unknown,
		Wide,
		Medium,
		Narrow
	}
	
	enum VideoMode
	{
		VideoMode_Unknown,
		VideoMode_WVGA,
		VideoMode_720,
		VideoMode_960,
		VideoMode_1080,
		VideoMode_1440,
		VideoMode_2p7K,
		VideoMode_4K,
		VideoMode_2p7KCinema,
		VideoMode_4KCinema,
		VideoMode_1080SuperView,
		VideoMode_720SuperView 
	}
	
	enum PhotoMode
	{
		PhotoMode_Unknown,
        PhotoMode_5mpWide,
        PhotoMode_5mpMedium,
        PhotoMode_7mpWide,
        PhotoMode_7mpMedium,
        PhotoMode_8mpMedium,
        PhotoMode_11mpWide,
        PhotoMode_12mpWide       
	}
	
	enum BurstRate
	{
		BurstRate_Unknown,
		BurstRate_3_1s, // 3/1s
		BurstRate_5_1s,
		BurstRate_10_1s,
		BurstRate_10_2s,
		BurstRate_30_1s,
		BurstRate_30_2s,
		BurstRate_30_3s
	}
	
	enum WhiteBalance
	{
		WhiteBalance_Unknown,
		WhiteBalance_3000k,
		WhiteBalance_5500k,
		WhiteBalance_6500k,
		WhiteBalance_CAMRAW,
	}
	
	enum LoopVideo
	{
		LoopVideo_Unknown,
		LoopVideo_Off,
		LoopVideo_5min,
		LoopVideo_20min,
		LoopVideo_60min,
		LoopVideo_Max
	}
}
