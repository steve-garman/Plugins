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
 * @fileoverview Represents the GoPro Camera status.
 * @author Chris Hopkin
 */

package com.droidscript.plugins;

import com.droidscript.plugins.GoProDefinitions.AutoPowerOff;
import com.droidscript.plugins.GoProDefinitions.BurstRate;
import com.droidscript.plugins.GoProDefinitions.CameraMode;
import com.droidscript.plugins.GoProDefinitions.FieldOfView;
import com.droidscript.plugins.GoProDefinitions.LEDs;
import com.droidscript.plugins.GoProDefinitions.Orientation;
import com.droidscript.plugins.GoProDefinitions.PhotoMode;
import com.droidscript.plugins.GoProDefinitions.VideoMode;
import com.droidscript.plugins.GoProDefinitions.VideoStandard;

public class GoProCameraStatus {
	
	private static class StatusBytes {
		public static final int CAMERA_MODE = 1;
		public static final int STARTUP_MODE = 3;
		public static final int SPOT_METER = 4;
		public static final int TIMELAPSE_INTERVAL = 5;
		public static final int AUTO_POWEROFF = 6;
		public static final int FIELD_OF_VIEW = 7;
		public static final int PHOTO_MODE = 8;
		//public static final int VIDEO_MODE = 9;
		public static final int RECORDING_MINUTES = 13;
		public static final int RECORDING_SECONDS = 14;
		public static final int BEEP_VOLUME = 16;
		public static final int LEDS = 17;
		public static final int STATUS_BITS_1 = 18;
		public static final int BATTERY_LEVEL = 19;
		public static final int PHOTOS_AVAILABLE_HI = 21;
		public static final int PHOTOS_AVAILABLE_LO = 22;
		public static final int PHOTOS_COUNT_HI = 23;
		public static final int PHOTOS_COUNT_LO = 24;
		public static final int VIDEO_REMAINING_MINS_HI = 25;
		public static final int VIDEO_REMAINING_MINS_LO = 26;
		public static final int VIDEO_COUNT_MINS_HI = 27;
		public static final int VIDEO_COUNT_MINS_LO = 28;
		public static final int RECORDING = 29;
		public static final int STATUS_BITS_2 = 30;
	}

	// Video Properties
	public VideoStandard videoStandard = VideoStandard.PAL;
	public VideoMode videoMode = VideoMode.VideoMode_Unknown;
	public double videoFrameRate = 0;
	public FieldOfView videoFOV = FieldOfView.Wide;
	public int videoRecordingTime = 0; // seconds
	public int videoAvailableTime = 0; // seconds
	public int videoCount = 0;
	public boolean videoRecording = false;
	
	// Photo Properties
	public PhotoMode photoMode = PhotoMode.PhotoMode_Unknown;
	public int photoCount = 0;
	public int photosAvailable = 0;
	
	// Burst Properties
	public BurstRate burstRate = BurstRate.BurstRate_Unknown;
	public boolean burstRecording = false;
	
	// Timelapse Properties
	public double timelapseInterval = 0;
	
	// General Properties
	public CameraMode cameraMode = CameraMode.Unknown;
	public CameraMode defaultCameraMode = CameraMode.Unknown;
	public boolean previewOn = false;
	public Orientation orientation = Orientation.Up;
	public boolean oneButtonOn = false;
	public boolean osdOn = false;	
	public boolean locate = false;	
	public boolean protuneOn = false;
	public int batteryLevel = 0;
	public boolean sdCard = true;
	public AutoPowerOff autoPowerOff = AutoPowerOff.AutoPowerOff_Unknown;
	public int beepVolume = 0;
	public LEDs leds = LEDs.LEDs_Unknown;
	public boolean spotMeter = false;

	public GoProCameraStatus(int[] statusBytes, int[] videoModeBytes, int[] videoFrameRateBytes, int[] burstRateBytes)
	{
		initialiseFromBytes(statusBytes, videoModeBytes, videoFrameRateBytes, burstRateBytes);
	}

	public void initialiseFromBytes(int[] statusBytes, int[] videoModeBytes, int[] videoFrameRateBytes, int[] burstRateBytes) 
	{
		int statusBits1 = statusBytes[StatusBytes.STATUS_BITS_1];
		{
			// Bit1 1=on 0=off
			previewOn = (statusBits1 & 0x01) != 0;

			// Bit3 0=UP -1=DOWN
			if ((statusBits1 & 0x04) == 0) 
			{
				orientation = Orientation.Up;
			} 
			else 
			{
				orientation = Orientation.Down;
			}

			// Bit4 1=on 0=off
			oneButtonOn = (statusBits1 & 0x08) != 0;

			// Bit5 1=on 0=off
			osdOn = (statusBits1 & 0x10) != 0;

			// Bit6 0=NTSC 1=PAL
			if ((statusBits1 & 0x20) == 0) 
			{
				videoStandard = VideoStandard.NTSC;
			} 
			else 
			{
				videoStandard = VideoStandard.PAL;
			}

			// Bit7 1=locate
			locate = (statusBits1 & 0x40) != 0;
		}

		int statusBits2 = statusBytes[StatusBytes.STATUS_BITS_2];
		{
			// Bit1 1=on 0=off
			burstRecording = (statusBits2 & 0x01) != 0;

			// Bit2 1=on 0=off
			protuneOn = (statusBits2 & 0x02) != 0;
		}

		// Camera Mode
		switch (statusBytes[StatusBytes.CAMERA_MODE]) 
		{
		case 0:
			cameraMode = CameraMode.Video;
			break;
		case 1:
			cameraMode = CameraMode.Photo;
			break;
		case 2:
			cameraMode = CameraMode.Burst;
			break;
		case 3:
			cameraMode = CameraMode.Timelapse;
			break;
		case 4:
			cameraMode = CameraMode.Timer; // Hero2
			break;
		case 5:
			cameraMode = CameraMode.Playback;
			break;
		case 7:
			cameraMode = CameraMode.Settings;
		}

		// Field of View Mode for video
		switch (statusBytes[StatusBytes.FIELD_OF_VIEW]) 
		{
		case 0:
			videoFOV = FieldOfView.Wide;
			break;
		case 1:
			videoFOV = FieldOfView.Medium;
			break;
		case 2:
			videoFOV = FieldOfView.Narrow;
		}

		// Photo Mode & FOV
		switch (statusBytes[StatusBytes.PHOTO_MODE]) 
		{
		case 0:
			photoMode = PhotoMode.PhotoMode_11mpWide;
			break;
		case 1:
			photoMode = PhotoMode.PhotoMode_8mpMedium;
			break;
		case 2:
			photoMode = PhotoMode.PhotoMode_5mpWide;
			break;
		case 3:
			photoMode = PhotoMode.PhotoMode_5mpMedium;
			break;
		case 4:
			photoMode = PhotoMode.PhotoMode_7mpWide;
			break;
		case 5:
			photoMode = PhotoMode.PhotoMode_12mpWide;
			break;
		case 6:
			photoMode = PhotoMode.PhotoMode_7mpMedium;
		}

		// Timelapse Interval
		switch (statusBytes[StatusBytes.TIMELAPSE_INTERVAL]) 
		{
		case 0:
			timelapseInterval = 0.5f;
			break;
		case 1:
			timelapseInterval = 1;
			break;
		case 2:
			timelapseInterval = 2;
			break;
		case 5:
			timelapseInterval = 5;
			break;
		case 10:
			timelapseInterval = 10;
			break;
		case 30:
			timelapseInterval = 30;
			break;
		case 60:
			timelapseInterval = 60;
		}

		// Photo Count
		int photoCountHiByte = statusBytes[StatusBytes.PHOTOS_COUNT_HI];
		int photoCountLoByte = statusBytes[StatusBytes.PHOTOS_COUNT_LO];
		photoCount = (photoCountLoByte | (photoCountHiByte << 8));

		// Photos Available
		int photosAvailableHiByte = statusBytes[StatusBytes.PHOTOS_AVAILABLE_HI];
		int photosAvailableLoByte = statusBytes[StatusBytes.PHOTOS_AVAILABLE_LO];
		photosAvailable = (photosAvailableLoByte | (photosAvailableHiByte << 8));

		// No SD Card, photoCountAvailableHiByte == 255
		sdCard = (photosAvailableHiByte != 255);

		// Video Count
		int videoCountHiByte = statusBytes[StatusBytes.VIDEO_COUNT_MINS_HI];
		int videoCountLoByte = statusBytes[StatusBytes.VIDEO_COUNT_MINS_LO];
		videoCount = (videoCountLoByte | (videoCountHiByte << 8));

		// Video Minutes Available
		int videoMinutesAvailableHiByte = statusBytes[StatusBytes.VIDEO_REMAINING_MINS_HI];
		int videoMinutesAvailableLoByte = statusBytes[StatusBytes.VIDEO_REMAINING_MINS_LO];
		videoAvailableTime = (videoMinutesAvailableLoByte | (videoMinutesAvailableHiByte << 8)) * 60;
		
		// Video Recording Time
		int videoMinutesRecording = statusBytes[StatusBytes.RECORDING_MINUTES];
		int videoSecondsRecording = statusBytes[StatusBytes.RECORDING_SECONDS];
		videoRecordingTime = (videoMinutesRecording * 60) + videoSecondsRecording;

		// Battery %
		batteryLevel = statusBytes[StatusBytes.BATTERY_LEVEL];

		// Video Recording
		videoRecording = (statusBytes[StatusBytes.RECORDING] == 1);
		
		// Video Mode - expecting 2 bytes, second byte is the video mode
		if(videoModeBytes.length == 2)
        {
            switch(videoModeBytes[1])
            {
                case 0:                                
                	videoMode = VideoMode.VideoMode_WVGA;
                    break;
                case 1:                                
                	videoMode = VideoMode.VideoMode_720;
                    break;                                
                case 2:                   
                	videoMode = VideoMode.VideoMode_960;
                    break;                                
                case 3:
                	videoMode = VideoMode.VideoMode_1080;
                    break;
                case 4:
                	videoMode = VideoMode.VideoMode_1440;
                    break;
                case 5:
                	videoMode = VideoMode.VideoMode_2p7K;
                    break;
                case 6:
                	videoMode = VideoMode.VideoMode_4K;
                    break;
                case 7:
                	videoMode = VideoMode.VideoMode_2p7KCinema;
                    break;
                case 8:
                	videoMode = VideoMode.VideoMode_4KCinema;
                    break;
                case 9:
                	videoMode = VideoMode.VideoMode_1080SuperView;
                    break;
                case 10:
                	videoMode = VideoMode.VideoMode_720SuperView; 
            }
        }
		
		// Video Frame Rate - expecting 2 Bytes, second byte is the frame rate
        if(videoFrameRateBytes.length == 2)
        {
            switch(videoFrameRateBytes[1])
            {
                case 0:
                	videoFrameRate = 12;
                    break;
                case 1:
                	videoFrameRate = 15;
                    break;
                case 2:
                	videoFrameRate = 24;
                    break;                                
                case 3:
                	videoFrameRate = 25;
                    break;                                
                case 4:
                	videoFrameRate = 30;
                    break;                            
                case 5:
                	videoFrameRate = 48;
                    break;                                
                case 6:
                	videoFrameRate = 50;
                    break;                                
                case 7:
                	videoFrameRate = 60;
                    break;                                
                case 8:
                	videoFrameRate = 100;
                    break;                                
                case 9:
                	videoFrameRate = 120;
                    break;                                
                case 10:
                	videoFrameRate = 240;
                    break;                                
                case 11:
                	videoFrameRate = 12.5f;
            }
        }
        
        // Burst Rate - expecting 2 Bytes, second byte is the burst rate
        if(burstRateBytes.length == 2)
        {
            switch(burstRateBytes[1])
            {
            case 0:
                burstRate = BurstRate.BurstRate_30_1s;
                break;               
            case 1:
            	burstRate = BurstRate.BurstRate_5_1s;
                break;
            case 2:
            	burstRate = BurstRate.BurstRate_10_1s;
                break;            
            case 3:
            	burstRate = BurstRate.BurstRate_10_2s;
                break;            
            case 4: 
            	burstRate = BurstRate.BurstRate_30_1s;
                break;           
            case 5: 
            	burstRate = BurstRate.BurstRate_30_2s;
                break;            
            case 6: 
            	burstRate = BurstRate.BurstRate_30_3s;
            }
        }
        
        // Auto Power Off
       	switch(statusBytes[StatusBytes.AUTO_POWEROFF])
       	{
       	case 0:
       		autoPowerOff = AutoPowerOff.AutoPowerOff_Never;
       		break;
       	case 1:
       		autoPowerOff = AutoPowerOff.AutoPowerOff_60;
       		break;
       	case 2:
       		autoPowerOff = AutoPowerOff.AutoPowerOff_120;
       		break;
       	case 3:
       		autoPowerOff = AutoPowerOff.AutoPowerOff_300;
       		break;
       	default:
       		autoPowerOff = AutoPowerOff.AutoPowerOff_Unknown;
       	}
       	
       	// Startup Mode
       	switch(statusBytes[StatusBytes.STARTUP_MODE])
       	{
       	case 0:
       		defaultCameraMode = CameraMode.Video;
       		break;
       	case 1:
       		defaultCameraMode = CameraMode.Photo;
       		break;
       	case 2:
       		defaultCameraMode = CameraMode.Burst;
       		break;
       	case 3:
       		defaultCameraMode = CameraMode.Timelapse;
       		break;
       	default:
       		defaultCameraMode = CameraMode.Unknown;
       	}
       	
       	// Startup Mode
      	switch(statusBytes[StatusBytes.BEEP_VOLUME])
      	{
      	case 0:
      		beepVolume = 0;
      		break;
      	case 1: // 70%
      		beepVolume = 70;
      		break;
      	case 2: // 100%
      		beepVolume = 100;
      		break;
      	default:
      		beepVolume = 0;
      	}

      	// LEDs
		switch(statusBytes[StatusBytes.LEDS])
       	{
       	case 0:
       		leds = LEDs.LEDs_Off;
       		break;
       	case 1:
       		leds = LEDs.LEDs_2;
       		break;
       	case 2:
       		leds = LEDs.LEDs_4;
       		break;
       	default:
       		leds = LEDs.LEDs_Unknown;
       	}
		
		// Spot Metering
		spotMeter = (statusBytes[StatusBytes.SPOT_METER] == 1);
	}
}
