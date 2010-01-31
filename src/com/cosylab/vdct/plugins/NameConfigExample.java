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

package com.cosylab.vdct.plugins;

import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.cosylab.vdct.Console;
import com.cosylab.vdct.Constants;
import com.cosylab.vdct.plugin.PluginContext;
import com.cosylab.vdct.plugin.config.NameConfigPlugin;
import com.cosylab.vdct.xml.XMLManager;

/**
 * This NameConfigPlugin example checks the record names using a pattern loaded from an xml file. 
 * The file name is defined by VDCT_NAME_CONFIG_FILE filename located in user home directory
 * (or in VDCT_CONFIG_DIR dir). Use VDCT_NAME_CONFIG_FILE_ENV environment variable to override
 * this default setting.

 * An example of XML file:
 * <?xml version="1.0" encoding="UTF-8"?>
 * 
 * <name pattern="VDCT_.*" default="VDCT_Name1" description="Must start with VDCT_"/>
 * 
 * Default and description parameters are optional.
 * 
 * @author ssah
 */
public class NameConfigExample implements NameConfigPlugin {

	public static final String VDCT_NAME_CONFIG_FILE = ".vdctname.xml";
	public static final String VDCT_NAME_CONFIG_FILE_ENV = "VDCT_LINK_CONFIG";
	
	private boolean running = false;
	
	private Pattern pattern = null;
	private String defaultName = null;
	private String description = null;
	
	/* (non-Javadoc)
	 * @see com.cosylab.vdct.plugin.config.NameConfigPlugin#checkValidity(java.lang.String)
	 */
	public String checkValidity(String name) {
		if (pattern != null && !pattern.matcher(name).matches()) {
			return "The name does not match the pattern: " + pattern.pattern();
		}
		return null;
	}

	/**
	 * Returns the default name.    
	 */
	public String getDefaultName() {
		return defaultName;
	}
	
	/* (non-Javadoc)
	 * @see com.cosylab.vdct.plugin.config.NameConfigPlugin#getNameDescription()
	 */
	public String getNameDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.plugin.config.NameConfigPlugin#isRunning()
	 */
	public boolean isRunning() {
		return running;
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.plugin.Plugin#destroy()
	 */
	public void destroy() {

	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.plugin.Plugin#getAuthor()
	 */
	public String getAuthor() {
		return "sunil.sah@cosylab.com";
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.plugin.Plugin#getDescription()
	 */
	public String getDescription() {
		return "NameConfigPlugin which loads name validation pattern from an XML file.";
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Name Configuration";
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.plugin.Plugin#getVersion()
	 */
	public String getVersion() {
		return "0.1";
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.plugin.Plugin#init(java.util.Properties, com.cosylab.vdct.plugin.PluginContext)
	 */
	public void init(Properties properties, PluginContext context) {

		String fileName = null;
		Document doc = null;
		try
		{
			fileName = Constants.getConfigFile(VDCT_NAME_CONFIG_FILE, VDCT_NAME_CONFIG_FILE_ENV);

			// is file does not exists, load default file
			if (!(new java.io.File(fileName).exists()))
			{
				Console.getInstance().println("[NameConfigPlugin] No name configuration file found.");
				return;
			}

			doc = XMLManager.readFileDocument(fileName);
		} catch (Exception e) {
			Console.getInstance().println("[NameConfigPlugin] Failed to open name configuration file '"+fileName+"'.");
			Console.getInstance().println(e);
			return;
		}

		if (doc==null) {
			Console.getInstance().println("[NameConfigPlugin] Invalid name configuration file '"+fileName+"'.");
			return;
		}

		NodeList nodes = doc.getElementsByTagName("name");
		for (int i=0; i<nodes.getLength(); i++)
		{
			Element e = (Element)nodes.item(i); 
			String patternString = e.getAttribute("pattern");
			defaultName = e.getAttribute("default");
			description = e.getAttribute("description");
			if (patternString != null)
			{
				try {
					pattern = Pattern.compile(patternString);
				} catch (PatternSyntaxException pse) {
					Console.getInstance().println("[NameConfigPlugin] Invalid pattern '"+pattern+"'. Skipping...");
					Console.getInstance().println(pse);
					Console.getInstance().println();
				}
			} else {
				Console.getInstance().println("[NameConfigPlugin] Invalid name configuration file '"+fileName+"'.");
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.plugin.Plugin#start()
	 */
	public void start() {
		running = true;
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.plugin.Plugin#stop()
	 */
	public void stop() {
		running = false;
	}
}
