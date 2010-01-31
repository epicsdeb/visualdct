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

package com.cosylab.vdct.plugin.config;

import java.util.Iterator;
import java.util.LinkedList;

import com.cosylab.vdct.Console;
import com.cosylab.vdct.plugin.PluginListener;
import com.cosylab.vdct.plugin.PluginManager;
import com.cosylab.vdct.plugin.PluginObject;

/**
 * @author ssah
 *
 */
public class PluginNameConfigManager implements PluginListener {

	private static PluginNameConfigManager instance = null;

    private LinkedList list = null;

	protected PluginNameConfigManager()
	{
		list = new LinkedList();
		PluginManager.getInstance().addPluginListener(this);
	}

	public static PluginNameConfigManager getInstance() {
		if (instance==null) instance = new PluginNameConfigManager();
		return instance;
	}
	
	/**
	 * If name is valid, the function returns null, otherwise it returns a
	 * String with the description of the error. 
	 */
	public String checkValidity(String name) {
		// Use only the most recently loaded running plugin.
		NameConfigPlugin lastPlugin = getMostRecentRunning();
		return (lastPlugin != null) ? lastPlugin.checkValidity(name) : null;
	}
	
	/**
	 * Returns the description of the valid names.    
	 */
	public String getNameDescription() {
		// Use only the most recently loaded plugin.
		NameConfigPlugin lastPlugin = getMostRecentRunning();
		return (lastPlugin != null) ? lastPlugin.getNameDescription() : null;
	}

	public String getDefaultName() {
		// Use only the most recently loaded plugin.
		NameConfigPlugin lastPlugin = getMostRecentRunning();
		return (lastPlugin != null) ? lastPlugin.getDefaultName() : null;
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.plugin.PluginListener#pluginAdded(com.cosylab.vdct.plugin.PluginObject)
	 */
	public void pluginAdded(PluginObject plugin) {
		if (plugin.getPlugin() instanceof NameConfigPlugin) {
			if (!list.contains(plugin)) {
				list.addFirst(plugin);
				Console.getInstance().println(plugin.getName()+" is registered as name type config plugin.");
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.plugin.PluginListener#pluginRemoved(com.cosylab.vdct.plugin.PluginObject)
	 */
	public void pluginRemoved(PluginObject plugin) {
		if (plugin.getPlugin() instanceof NameConfigPlugin) {
			list.remove(plugin);
		}
	}
	
	private NameConfigPlugin getMostRecentRunning() {
		Iterator iterator  = list.iterator();
		NameConfigPlugin mostRecentRunning = null;
		while (iterator.hasNext()) {
			mostRecentRunning = (NameConfigPlugin)((PluginObject)iterator.next()).getPlugin();
			if (mostRecentRunning.isRunning()) {
				return mostRecentRunning;
			}
		}
		return null;
	}
}
