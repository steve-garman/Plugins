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
 * @fileoverview The main plugin class for GoPro Controller.
 * @author Chris Hopkin
 */

package com.droidscript.plugins;

import android.os.*;
import android.content.*;
import android.util.Log;
import java.lang.reflect.*;
import org.json.JSONObject;

public class GoProController implements GoProCameraListener
{
	private static final String TAG = "GoProController";	
	private static final float VERSION = 1.0f;
	
	private Method m_callscript;
	private Object m_parent;
	private GoProCamera m_camera;

	// Script callbacks.
	private String m_onConnectCallback;
	private String m_onErrorCallback;
	private String m_onReadyCallback;
	private String m_onStatusCallback;

	// Construct plugin.
	public GoProController()
	{
		Log.d( TAG, "Creating plugin object");
	}

	//Initialise plugin.
	public void Init( Context ctx, Object parent )
	{
		try 
		{
			Log.d( TAG, "Initialising plugin object");

			//Save reference to parent (DroidScript).
			m_parent = parent;

			//Use reflection to get 'CallScript' method
			Log.d( TAG, "Getting CallScript method");
			m_callscript = parent.getClass().getMethod( "CallScript", Bundle.class );
		 } 
		 catch (Exception e) 
		 {
			   Log.e( TAG, "Failed to Initialise plugin!", e );
		 }
	}

	//Call a function in the user's script.
	private void CallScript( Bundle b )
	{
		try 
		{
			m_callscript.invoke( m_parent, b );
		} 
		catch (Exception e)
		{
			Log.e( TAG, "Failed to call script function!", e );
		}
	}

	//Handle commands from DroidScript.
	public String CallPlugin( Bundle b )
	{
		//Extract command.
		String cmd = b.getString("cmd");
	
		//Process commands.
		String ret = null;
		try 
		{
			if( cmd.equals("GetVersion") )
			{
				return GetVersion(b);
			}
			else if( cmd.equals("Connect") )
			{
				Connect(b);
			}
			else if( cmd.equals("Disconnect") )
			{
				if(m_camera != null)
				{
					m_camera.disconnect();
				}
			}
			else if( cmd.equals("SetOnConnect") )
			{
				m_onConnectCallback = b.getString("p1");
			}
			else if( cmd.equals("SetOnError") )
			{
				m_onErrorCallback = b.getString("p1");
			}
			else if ( cmd.equals("SetOnReady") )
			{
				m_onReadyCallback = b.getString("p1");
			}
			else if ( cmd.equals("StartShutter") )
			{
				if(m_camera != null)
				{
					m_camera.startShutter();
				}
			}
			else if ( cmd.equals("StopShutter") )
			{
				if(m_camera != null)
				{
					m_camera.stopShutter();
				}
			}
			else if ( cmd.equals("GetPreviewUrl") )
			{
				if(m_camera != null)
				{
					return m_camera.getPreviewUrl();
				}
				else
				{
					return "";
				}
			}
			else if ( cmd.equals("StartPreview") )
			{
				if(m_camera != null)
				{
					m_camera.startPreview();
				}
			}
			else if ( cmd.equals("StopPreview") )
			{
				if(m_camera != null)
				{
					m_camera.stopPreview();
				}
			}
			else if ( cmd.equals("LoadStatus") )
			{
				LoadStatus(b);
			}
			else if ( cmd.equals("PowerOn") )
			{
				if(m_camera != null)
				{
					m_camera.powerOn();
				}
			}
			else if ( cmd.equals("PowerOff") )
			{
				if(m_camera != null)
				{
					m_camera.powerOff();
				}
			}
			else if ( cmd.equals("SetOptions") )
			{
				SetOptions(b);
			}
			else if ( cmd.equals("GetModel") )
			{
				if(m_camera != null)
				{
					return m_camera.getModelName();
				}
				else
				{
					return "";
				}
			}
			else if ( cmd.equals("StartLocate") )
			{
				if(m_camera != null)
				{
					m_camera.startLocate();
				}
			}
			else if ( cmd.equals("StopLocate") )
			{
				if(m_camera != null)
				{
					m_camera.stopLocate();
				}
			}
			else if ( cmd.equals("IsPowerOn") )
			{
				return (m_camera != null && m_camera.isPowerOn()) ? "true" : "false";
			}
			else if ( cmd.equals("IsReady") )
			{
				return (m_camera != null && m_camera.isReady()) ? "true" : "false";
			}
		} 
		catch (Exception e) 
		{
		   Log.e( TAG, "Plugin command failed!", e);
		}
		return ret;
	}

	//Handle the GetVersion command.
	private String GetVersion( Bundle b )
	{
		Log.d( TAG, "Got GetVersion" );
		return Float.toString( VERSION );
	}
	
	private void Connect(Bundle b)
	{
		m_camera = new GoProCamera(this);
		
		// Extract the ip address
		String ipAddress = b.getString("p1");
		
		Log.d( TAG, "Connect( " + ipAddress +" )");
		
		m_camera.connect(ipAddress);
	}
	
	private void LoadStatus(Bundle b)
	{
		Log.d( TAG, "LoadStatus( )");
		
		m_onStatusCallback = b.getString("p1");
		
		if(m_camera != null)
		{
			m_camera.getCameraStatus();
		}
	}
	
	private void SetOptions(Bundle b)
	{
		if(m_camera == null)
		{
			return;
		}	
				
		String optionsString = b.getString("p1");
		
		Log.d( TAG, "SetOptions( " + optionsString + " )");
	
		try 
		{
			JSONObject options = new JSONObject(optionsString);
			
			GoProJSON.setCameraOptionsFromJSON(m_camera, options);
		} 
		catch (Exception e) 
		{
		   Log.e( TAG, "SetOptions failed", e);
		}
	}

	// GoProCameraListener override
	@Override
	public void onConnected() 
	{
		if(m_onConnectCallback != null)
		{
			Log.d( TAG, "Calling OnConnect Callback");
			
			Bundle b = new Bundle();
			b.putString("cmd", m_onConnectCallback);
			CallScript(b);
		}
	}

	// GoProCameraListener override
	@Override
	public void onError(String error)
	{
		if(m_onErrorCallback != null)
		{
			Log.d( TAG, "Calling OnError Callback");
			
			Bundle b = new Bundle();
			b.putString("cmd", m_onErrorCallback);
			b.putString("p1", error);
			CallScript(b);
		}
	}

	// GoProCameraListener override
	@Override
	public void onReady() 
	{
		if(m_onReadyCallback != null)
		{
			Log.d( TAG, "Calling OnReady Callback");
			
			Bundle b = new Bundle();
			b.putString("cmd", m_onReadyCallback);
			CallScript(b);
		}
	}

	// GoProCameraListener override
	@Override
	public void onCameraStatus(GoProCameraStatus status) 
	{
		if(m_onStatusCallback != null)
		{
			Log.d( TAG, "Calling OnCameraStatus Callback");
			
			Bundle b = new Bundle();
			b.putString("cmd", m_onStatusCallback);
			
			JSONObject jsonStatus = GoProJSON.getJSONCameraStatus(status);

			if(jsonStatus != null)
			{
				String jsonString = jsonStatus.toString();
				
				// JSONObject.toString() will escape '/' to '\/' which messes up the burst rate '10\/2s'
				// Get around this by replacing '\/' with '/' before sending
				jsonString = jsonString.replace("\\/", "/");
				b.putString("p1", "json:"+jsonString);				
				CallScript(b);
			}	
		}
	}
} 


