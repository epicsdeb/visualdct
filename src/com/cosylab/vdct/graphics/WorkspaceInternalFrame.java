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

package com.cosylab.vdct.graphics;

import java.beans.PropertyVetoException;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import com.cosylab.vdct.events.CommandManager;
import com.cosylab.vdct.events.MouseEventManager;
import com.cosylab.vdct.events.commands.SetWorkspaceFile;

/**
 * @author ssah
 *
 */
public class WorkspaceInternalFrame extends JInternalFrame
implements InternalFrameInterface, InternalFrameListener {

	protected Object dsId = null; 
	protected PanelDecorator contentPanel = null;
	protected DsManagerInterface drawingSurfaceManager = null;
	protected DesktopInterface desktop = null;
	protected File file = null;

	protected static final String defaultName = "Name";

	public WorkspaceInternalFrame(Object dsId, DesktopInterface desktop, DsManagerInterface drawingSurfaceManager) {
		super(defaultName, true, true, true, true);
		
		this.dsId = dsId;
		this.desktop = desktop;
		this.drawingSurfaceManager = drawingSurfaceManager;
		contentPanel = new PanelDecorator();
		/* First register the component, then create the drawing surface which adds listeners to
		 * it.
		 */
		MouseEventManager.getInstance().registerSubscriber(
				"WorkspaceInternalFrame:" + dsId.toString(), contentPanel);
		contentPanel.setComponent(drawingSurfaceManager.addDrawingSurface(dsId, this));
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		addInternalFrameListener(this);

		setContentPane(contentPanel);
	}

	public JComponent getDisplayingComponent() {
		return contentPanel;
	}

	public void setFile(File file, String title) {
		this.file = file;
		setTitle(title);
		if (isSelected()) {
			sendActiveGroupNotification();
		}
	}

	public boolean onClose() {
		dispose();
		return true;
	}

	public void setFocused() {
		if (!isSelected()) {
			try {
				setSelected(true);
			} catch (PropertyVetoException e) {
			}
		}
	}

	public void internalFrameActivated(InternalFrameEvent e) {
		drawingSurfaceManager.setFocusedDrawingSurface(dsId);
		sendActiveGroupNotification();
		CommandManager.getInstance().execute("SetDefaultFocus");
	}

	public void internalFrameDeactivated(InternalFrameEvent e) {
		drawingSurfaceManager.setFocusedDrawingSurface(null);
	}

	public void internalFrameClosed(InternalFrameEvent e) {
		drawingSurfaceManager.removeDrawingSurface(dsId);
		desktop.onInternalFrameClosed();
	}

	public void internalFrameClosing(InternalFrameEvent e) {
		drawingSurfaceManager.closeDrawingSurface(dsId);
	}
	public void internalFrameDeiconified(InternalFrameEvent e) {
	}
	public void internalFrameIconified(InternalFrameEvent e) {
	}
	public void internalFrameOpened(InternalFrameEvent e) {
	}

	private void sendActiveGroupNotification() {
		SetWorkspaceFile command =
			(SetWorkspaceFile)CommandManager.getInstance().getCommand("SetFile");
		if (command != null) {
			command.setFile(file);
			command.setFilename(title);
			command.execute();
		}
	}
}
