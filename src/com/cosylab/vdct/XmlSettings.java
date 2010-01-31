/**
 * Copyright (c) 2008, Cosylab, Ltd., Control System Laboratory, www.cosylab.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution. 
 * Neither the name of the Cosylab, Ltd., Control System Laboratory nor the names
 * of its contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.cosylab.vdct;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.cosylab.vdct.xml.XMLManager;

/**
 * @author ssah
 *
 */
public class XmlSettings {
	
	private static XmlSettings xmlSettings = null;
	
	private String rdbHost = "localhost"; 
	private String rdbDatabase = "epics"; 
	private String rdbUser = "user"; 
	private String rdbPassword = "password";
	
	public static XmlSettings getInstance() {
		if (xmlSettings == null) {
			xmlSettings = new XmlSettings();
			xmlSettings.load();
		}
		return xmlSettings;
	}
	
	public void load() {

		Document doc = null;
	    String fileName = Constants.getConfigFile(Constants.VDCT_SETTINGS_FILE_NAME, Constants.VDCT_SETTINGS_FILE);

	    try {

	    	// If file does not exists, load default file.
	    	if (!(new File(fileName).exists())) {
	    		Console.getInstance().println("o) No settings file found. Using defaults...");

	    		URL url  = getClass().getResource("/" + Constants.CONFIG_DIR + Constants.VDCT_SETTINGS_FILE_NAME);
	    		if (url == null) {
	    			throw new FileNotFoundException();
	    		}
	    		fileName = url.getFile();
	    	}

	    	/* Sometimes fileName is in a format with backslashes and sometimes slashes.
	    	 * TASK:SLASH: replace this hack with a general rule on how the file names should be passed.
	    	 */
	    	if (fileName.indexOf('\\') >= 0) {
	    		try {
	    			fileName = (new File(fileName).toURI()).toURL().getFile();
	    		} catch (MalformedURLException exception) {
	    			// nothing
	    		}
	    	}
	    	doc = XMLManager.readResourceDocument(fileName);
	    } catch (FileNotFoundException e) {
	    	Console.getInstance().println("Settings file '"+fileName+"' not found.");
	    	return;
	    } catch (Exception e) {
			Console.getInstance().println("An error occured while loading settings!");
			Console.getInstance().println(e);
			return;
		}

		if (doc == null) {
			Console.getInstance().println("Failed to read settings file '"+fileName+"'.");
			return;
		}
		
	    loadRdbSettings(doc);
	}
	
	public void save() {
		Document doc = XMLManager.newDocument();
		
		Element root = (Element)doc.createElement("vdct");

		doc.appendChild(root);
		saveRdbSettings(doc, root);
		root.normalize();
		
		String fileName = System.getProperty(Constants.VDCT_SETTINGS_FILE);
		if (fileName == null) {
			fileName = System.getProperty("user.home") + "/" + Constants.VDCT_SETTINGS_FILE_NAME;
		}
		
		try {
			XMLManager.writeDocument(fileName, doc, null, null, null);

		} catch (IOException e) {
			Console.getInstance().println("Error while saving settings file!");
			Console.getInstance().println(e);
		}
	}
	
	private void loadRdbSettings(Document doc) {
		Node node = XMLManager.findNode(doc, "rdb");

		rdbHost = XMLManager.getNodeAttribute(node, "host");
		rdbDatabase = XMLManager.getNodeAttribute(node, "database");
		rdbUser = XMLManager.getNodeAttribute(node, "user");
		rdbPassword = XMLManager.getNodeAttribute(node, "password");
	}
	
	private void saveRdbSettings(Document doc, Element parent) {

		Element node = doc.createElement("rdb");		
		
		node.setAttribute("host", rdbHost);
	    node.setAttribute("database", rdbDatabase);
	    node.setAttribute("user", rdbUser);
	    node.setAttribute("password", rdbPassword);
	    
	    parent.appendChild(node);
	}

	/**
	 * @return the rdbHost
	 */
	public String getRdbHost() {
		return rdbHost;
	}

	/**
	 * @param rdbHost the rdbHost to set
	 */
	public void setRdbHost(String rdbHost) {
		this.rdbHost = rdbHost;
	}

	/**
	 * @return the rdbDatabase
	 */
	public String getRdbDatabase() {
		return rdbDatabase;
	}

	/**
	 * @param rdbDatabase the rdbDatabase to set
	 */
	public void setRdbDatabase(String rdbDatabase) {
		this.rdbDatabase = rdbDatabase;
	}

	/**
	 * @return the rdbUser
	 */
	public String getRdbUser() {
		return rdbUser;
	}

	/**
	 * @param rdbUser the rdbUser to set
	 */
	public void setRdbUser(String rdbUser) {
		this.rdbUser = rdbUser;
	}

	/**
	 * @return the rdbPassword
	 */
	public String getRdbPassword() {
		return rdbPassword;
	}

	/**
	 * @param rdbPassword the rdbPassword to set
	 */
	public void setRdbPassword(String rdbPassword) {
		this.rdbPassword = rdbPassword;
	}
}
