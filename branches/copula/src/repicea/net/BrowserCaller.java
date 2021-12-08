/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge Epicea.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package repicea.net;

import java.lang.reflect.Method;

/**
 * This method opens a web browser given an URL.
 * @author Original code at http://javaxden.blogspot.fr/2007/09/launch-web-browser-through-java.html
 */
public class BrowserCaller {
	
	/**
	* Method to Open the Broser with Given URL
	* @param url
	*/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void openUrl(String url){
		String os = System.getProperty("os.name");
		Runtime runtime = Runtime.getRuntime();
		try {	// Block for Windows Platform
			if (os.startsWith("Windows")){
				String cmd = "rundll32 url.dll, FileProtocolHandler "+ url;
				runtime.exec(cmd);
			} else if(os.startsWith("Mac OS")) {				//Block for Mac OS
//				Class fileMgr = ClassLoader.getSystemClassLoader().loadClass("com.apple.eio.FileManager");
				Class fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] {String.class});
				openURL.invoke(null, new Object[] {url});
			} else { 	//Block for UNIX Platform
				String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
				String browser = null;
				for (int count = 0; count < browsers.length && browser == null; count++) {
					if (runtime.exec(new String[] {"which", browsers[count]}).waitFor() == 0) {
						browser = browsers[count];
					}
				}
				if (browser == null) {
					throw new Exception("Could not find web browser");
				} else {
					runtime.exec(new String[] {browser, url});
				}
			}
		} catch (Exception e){
			System.err.println("Exception occurd while invoking Browser!");
			e.printStackTrace();
		}
	}
	
}

