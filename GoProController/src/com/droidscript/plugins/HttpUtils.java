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
 * @fileoverview Utility class for making HTTP requests.
 * @author Chris Hopkin
 */

package com.droidscript.plugins;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class HttpUtils 
{
	private static String TAG = "HttpUtils";
	
	public static HttpURLConnection openHttpConnection(String url, int timeout) throws IOException
	{
		URL requestUrl = new URL(url);
	    
		HttpURLConnection httpConnection = (HttpURLConnection) requestUrl.openConnection();
		
		if(timeout > 0)
		{
			httpConnection.setConnectTimeout(timeout);
		}
		
		return httpConnection;
	}
	
	public static void sendRequest(String url)
	{		
		InputStream is = null;
		HttpURLConnection httpConnection = null;
		
		try
        {
			httpConnection = openHttpConnection(url, 0);
        	is = httpConnection.getInputStream();
        }
        catch(Exception e)
        {
        	Log.e(TAG, "sendRequest", e);
        }
        finally
        {
        	if(is != null) 
	        {
	            try 
	            { 
	                is.close(); 
	            } 
	            catch (IOException e) 
	            {
	                Log.e(TAG, "Error closing InputStream", e);
	            }
	        }
        	
        	if(httpConnection != null)
        	{
        		httpConnection.disconnect();
        	}
        }
	}
	
	public static byte[] responseToByteArray(HttpURLConnection connection) 
	{
		byte[] response = null;
		InputStream is = null;
		
		try 
		{
			is = connection.getInputStream();
			
		    // Now that the InputStream is open, get the content length
		    int contentLength = connection.getContentLength();

		    // To avoid having to resize the array over and over and over as
		    // bytes are written to the array, provide an accurate estimate of
		    // the ultimate size of the byte array
		    ByteArrayOutputStream byteArrayOS;
		    if (contentLength != -1) 
		    {
		    	byteArrayOS = new ByteArrayOutputStream(contentLength);
		    } 
		    else 
		    {
		    	byteArrayOS = new ByteArrayOutputStream(1024);
		    }

		    byte[] buffer = new byte[512];
		    while(true) 
		    {
		        int read = is.read(buffer);
		        if(read > 0) 
		        {
		        	byteArrayOS.write(buffer, 0, read);
		        }
		        else
		        {
		        	break;
		        }
		    }

		    response = byteArrayOS.toByteArray();		    
		}
		catch (Exception e) 
	    {
	        Log.e(TAG, "responseToByteArray: " + e.getMessage());
	        response = null;
	    }
		finally 
	    {
	        if (is != null) 
	        {
	            try 
	            { 
	                is.close(); 
	            } 
	            catch (IOException e) 
	            {
	                Log.e(TAG, "Error closing InputStream: " + e.getMessage());
	            }
	        }   
	    }
		
		return response;
	}
	
	public static String responseToString(HttpURLConnection connection) 
	{
		byte[] bytes = responseToByteArray(connection);

		if(bytes != null)
		{
			try 
			{
				return new String(bytes, "UTF-8");
			} 
			catch (UnsupportedEncodingException e) 
			{
				Log.e(TAG, "responseToString: " + e.getMessage());
			}
		}
		
		return null;
	}
	
	public static JSONObject responseToJSON(HttpURLConnection connection) 
	{
	    String response = responseToString(connection);
	    
	    if(response != null)
	    {
	    	try 
	    	{
	    		// Remove all \n characters
	    		response = response.replace("\n", "");
	    		
	    		// Remove any trailing commas that will prevent the JSONObject from parsing properly
	    		response = response.replace(",}", "}");
	    		
				JSONObject jsonResponse = new JSONObject(response);
				return jsonResponse;
			} 
	    	catch (JSONException e) 
	    	{
	    		Log.e(TAG, "responseToJSON", e);
			}
	    }
	    
	    return null;
	}
	
	public static int[] responseToUIntArray(HttpURLConnection connection) 
	{		
		return byteArrayToUintArray(responseToByteArray(connection));
	}
	
	private static int[] byteArrayToUintArray(byte[] bytes)
	{
		if(bytes == null)
		{
			return null;
		}
		
		int[] ints = new int[bytes.length];
		for(int i = 0; i < bytes.length; ++i)
		{
			ints[i] = (int)bytes[i] & 0xFF;
		}
		
		return ints;
	}
}
