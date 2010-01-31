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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.cosylab.vdct.events.CommandManager;
import com.cosylab.vdct.events.commands.GetVDBManager;
import com.cosylab.vdct.graphics.VDBInterface;

/**
 * @author ssah
 *
 */
public class NameChecker implements DocumentListener {

	protected JTextField nameField = null;
	protected JLabel oldNameLabel = null;
	protected boolean group = false;
	protected JButton confirmButton = null;
	protected JLabel description = null;
	
	/**
	 * If group is true, the name is checked as a group name, otherwise it is
	 * checked as a record name.
	 * 
	 * @param nameField
	 * @param oldNameField
	 * @param group
	 * @param confirmButton
	 * @param description
	 */
	public NameChecker(JTextField nameField, JLabel oldNameLabel,
			boolean group, JButton confirmButton, JLabel description) {
		super();
		this.nameField = nameField;
		this.oldNameLabel = oldNameLabel;
		this.group = group;
		this.confirmButton = confirmButton;
		this.description = description;
		
		nameField.getDocument().addDocumentListener(this);
	}

	public void check() {
		VDBInterface vDBInterface =
			((GetVDBManager)CommandManager.getInstance().getCommand("GetVDBManager")).getManager();
		
		String errmsg = null;
		String name = nameField.getText();
		if (group) {
            errmsg = vDBInterface.checkGroupName(name, true);
		} else {
			String oldName = oldNameLabel != null ? oldNameLabel.getText() : null;
            errmsg = vDBInterface.checkRecordName(name, oldName, true);
		}
       	confirmButton.setEnabled(errmsg == null || !vDBInterface.isErrorMessage(errmsg));
    	description.setText(errmsg != null ? errmsg : " ");
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent arg0) {
        // we won't ever get this with a PlainDocument
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent arg0) {
		check();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent arg0) {
		check();
	}
}
