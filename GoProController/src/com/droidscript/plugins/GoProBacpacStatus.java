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
 * @fileoverview Represents the GoPro Bacpac status.
 * @author Chris Hopkin
 */

package com.droidscript.plugins;

public class GoProBacpacStatus
{
	class StatusBytes
	{
		public static final int SIGNAL_STRENGTH = 4;
		public static final int POWER = 9;
		public static final int READY = 11;
		public static final int MODEL = 12;
	}
	
	private boolean m_ready = false;
	private boolean m_powerOn = false;
	
	public GoProBacpacStatus(int[] statusBytes)
	{
		initialiseFromBytes(statusBytes);
	}
	
	public void initialiseFromBytes(int[] statusBytes)
	{
		m_powerOn = (statusBytes[StatusBytes.POWER] == 1);
		m_ready = (statusBytes[StatusBytes.READY] == 1);
	}
	
	public boolean isReady()
	{
		return m_ready;
	}
	
	public boolean isPowerOn()
	{
		return m_powerOn;
	}
}
