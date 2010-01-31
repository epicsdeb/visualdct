/**
 * Copyright (c) 2007, Cosylab, Ltd., Control System Laboratory, www.cosylab.com
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
package com.cosylab.vdct.vdb;

import com.cosylab.vdct.events.CommandManager;
import com.cosylab.vdct.events.commands.GetGUIInterface;
import com.cosylab.vdct.events.commands.GetVDBManager;
import com.cosylab.vdct.graphics.VDBInterface;
import com.cosylab.vdct.graphics.objects.Flexible;
import com.cosylab.vdct.inspector.Inspectable;
import com.cosylab.vdct.plugin.config.PluginNameConfigManager;

/**
 * An InspectableProperties interface for editing names.
 * @author ssah 
 *
 */
public class NameProperty extends NameValueInfoProperty {

	private Flexible namedObject = null;
	private boolean flexible = false;
	/**
	 * @param name
	 * @param value
	 */
	public NameProperty(String name, Inspectable namedObject) {
		super(name, namedObject.getName());
		flexible = namedObject instanceof Flexible;
		
		if (flexible) {
			this.namedObject = (Flexible)namedObject;
			value = this.namedObject.getFlexibleName();
		}
	}

	public String getHelp() {
		return "Name";
	}
	
	/* (non-Javadoc)
	 * @see com.cosylab.vdct.vdb.NameValueInfoProperty#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		
		String oldName = namedObject.getFlexibleName();
		
		// Do nothing if the name is the same.
		if (value.equals(oldName)) {
			return;
		}
		
		VDBInterface vDBInterface =
			((GetVDBManager)CommandManager.getInstance().getCommand("GetVDBManager")).getManager();
		
		if (vDBInterface.isErrorMessage(checkNewName(value))) {
        	return;
        }

		GetGUIInterface interf = (GetGUIInterface)CommandManager.getInstance().getCommand("GetGUIMenuInterface");
		interf.getGUIMenuInterface().rename(oldName, value);
		this.value = namedObject.getFlexibleName();
	}
	
	/* (non-Javadoc)
	 * @see com.cosylab.vdct.vdb.NameValueInfoProperty#isValid()
	 */
	public boolean isValid() {
		return checkNewName(value) == null;
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.vdb.NameValueInfoProperty#isEditable()
	 */
	public boolean isEditable() {
		return flexible;
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.InspectableProperty#hasValidity()
	 */
	public boolean hasValidity() {
		return true;
	}
	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.InspectableProperty#checkValueValidity(java.lang.String)
	 */
	public String checkValueValidity(String value) {
		return checkNewName(value);
	}
	
	/* (non-Javadoc)
	 * @see com.cosylab.vdct.vdb.NameValueInfoProperty#getToolTipText()
	 */
	public String getToolTipText() {
		return PluginNameConfigManager.getInstance().getNameDescription();
	}

	private String checkNewName(String name) {

		if (!flexible) {
			return null;
		}
		String oldName = namedObject.getFlexibleName();
		
		VDBInterface vDBInterface =
			((GetVDBManager)CommandManager.getInstance().getCommand("GetVDBManager")).getManager();
		return vDBInterface.checkRecordName(name, oldName, true);
	}
}
