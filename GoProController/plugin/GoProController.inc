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
 * @fileoverview GoPro Controller Plugin JavaScript wrapper class.
 * @author Chris Hopkin
 */
 
function GoProController( options )
{
    this.plg = _CreatePlugin( "com.droidscript.plugins.GoProController", options );
    
	this.GetVersion = function( ) 
	{ 
		return parseFloat( this.plg.Send( "GetVersion" ) ); 
	}
	
	this.Connect = function( ipAddress ) 
	{ 
		this.plg.Send( "Connect", ipAddress ); 
	}
	
	this.Disconnect = function( ) 
	{ 
		this.plg.Send( "Disconnect" ); 
	}
	
	this.SetOnConnect = function( cb ) 
	{ 
		this.plg.Send( "SetOnConnect", cb.name ); 
	}
	
	this.SetOnError = function( cb ) 
	{ 
		this.plg.Send( "SetOnError", cb.name ); 
	}
	
	this.SetOnReady = function( cb ) 
	{ 
		this.plg.Send( "SetOnReady", cb.name ); 
	}
	
	this.LoadStatus = function( cb ) 
	{ 
		this.plg.Send( "LoadStatus", cb.name ); 
	}
	
    this.StartShutter = function( ) 
    { 
    	this.plg.Send( "StartShutter" ); 
    }
    
    this.StopShutter = function( ) 
    { 
    	this.plg.Send( "StopShutter" ); 
    }
    
    this.GetPreviewUrl = function( ) 
    { 
    	return this.plg.Send( "GetPreviewUrl" ); 
    }
    
    this.GetModel = function( ) 
    { 
    	return this.plg.Send( "GetModel" ); 
    }
    
    this.IsPowerOn = function( ) 
    { 
    	return this.plg.Send( "IsPowerOn" ).localeCompare("true") == 0; 
    }
    
    this.IsReady = function( ) 
    { 
    	return this.plg.Send( "IsReady" ).localeCompare("true") == 0; 
    }
    
    this.StartPreview = function( ) 
    { 
    	this.plg.Send( "StartPreview" ); 
    }
    
    this.StopPreview = function( ) 
    { 
    	this.plg.Send( "StopPreview" ); 
    }
    
    this.PowerOn = function( ) 
    { 
    	this.plg.Send( "PowerOn" ); 
    }
    
    this.PowerOff = function( ) 
    { 
    	this.plg.Send( "PowerOff" ); 
    }
    
    this.StartLocate = function( ) 
    { 
    	this.plg.Send( "StartLocate" ); 
    }
    
    this.StopLocate = function( ) 
    { 
    	this.plg.Send( "StopLocate" ); 
    }
    
    this.SetOptions = function( options ) 
    {
    	var jsonString = "";
    	try
    	{
    		jsonString = JSON.stringify(options);
    	}
    	catch(e)
    	{
    		// Assume the JSON object has already been stringified
    		jsonString = options;
    	}
    	
    	this.plg.Send( "SetOptions", jsonString ); 
    }
}
