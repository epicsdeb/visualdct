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

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JFrame;

import com.cosylab.vdct.Constants;
import com.cosylab.vdct.DataSynchronizer;
import com.cosylab.vdct.events.CommandManager;
import com.cosylab.vdct.events.commands.GetDsManager;
import com.cosylab.vdct.events.commands.GetGUIInterface;
import com.cosylab.vdct.events.commands.GetPrintableInterface;
import com.cosylab.vdct.events.commands.GetVDBManager;
import com.cosylab.vdct.events.commands.LinkCommand;
import com.cosylab.vdct.events.commands.RepaintCommand;
import com.cosylab.vdct.graphics.objects.Box;
import com.cosylab.vdct.graphics.objects.Group;
import com.cosylab.vdct.graphics.objects.Line;
import com.cosylab.vdct.graphics.objects.LinkSource;
import com.cosylab.vdct.graphics.objects.TextBox;
import com.cosylab.vdct.graphics.objects.VisibleObject;

/**
 * @author ssah
 *
 */
public class DsManager
implements DsManagerInterface, GUIMenuInterface, VDBInterface,
LinkCommandInterface, RepaintInterface, Pageable {
	
	protected static DsManager instance = null;

	protected DSGUIInterface dsInterface = null;  

	protected static HashMap drawingSurfaces = new HashMap();
	
	protected DesktopInterface desktopInterface = null;
	protected CopyContext copyContext = null;
	
	protected Vector dsEventListeners = null;
	
	protected int dsCount = 0;

	public DsManager(DesktopInterface desktopInterface) {
		instance = this;
		
		this.desktopInterface = desktopInterface;
		dsEventListeners = new Vector();
		copyContext = new CopyContext();
		
		CommandManager commandManager = CommandManager.getInstance();
		commandManager.addCommand("GetDsManager", new GetDsManager(this));
		commandManager.addCommand("GetVDBManager", new GetVDBManager(this));
		commandManager.addCommand("GetGUIMenuInterface", new GetGUIInterface(this));
		commandManager.addCommand("LinkCommand", new LinkCommand(this));
		commandManager.addCommand("GetPrintableInterface", new GetPrintableInterface(this));
		commandManager.addCommand("RepaintWorkspace", new RepaintCommand(this));
		commandManager.addCommand("RepaintAllFrames", new RepaintCommand(this, true, false));
		commandManager.addCommand("RepaintHighlighted", new RepaintCommand(this, false, true));
	
		DsListenerInitializer.registerDsManagerListeners();
		createDummyDrawingSurface();			
	}
	
	public static DrawingSurface getDrawingSurface() {
		if (instance.dsInterface == null) {
			return null;
		}
		return instance.dsInterface.getDrawingSurface();
	}

	public static DrawingSurface getDrawingSurface(Object id) {
		return (DrawingSurface)drawingSurfaces.get(id);
	}

	public static Vector getAllDrawingSurfaces() {
		Vector vector = new Vector();
		Iterator iterator = drawingSurfaces.values().iterator();
		DrawingSurface surface = null; 
		while (iterator.hasNext()) {
			surface = ((DrawingSurface)iterator.next());
			if (!surface.isDisposed()) {
				vector.add(surface);
			}
		}
		return vector;
	}
	
	public void createDummyDrawingSurface() {
		DrawingSurface drawingSurface = new DrawingSurface(Constants.DEFAULT_NAME, 0, null, copyContext);
		drawingSurface.setDisposed(true);
		drawingSurfaces.put(Constants.DEFAULT_NAME, drawingSurface);
		drawingSurface.initializeWorkspace();
	}
	
	public VisualComponent addDrawingSurface(Object id, InternalFrameInterface displayer) {

		DrawingSurface drawingSurface = new DrawingSurface(id, dsCount, displayer, copyContext);
		dsCount++;

		drawingSurfaces.put(id, drawingSurface);
		
		Iterator iterator = dsEventListeners.iterator();
		while (iterator.hasNext()) {
			((DsEventListener)iterator.next()).onDsAdded(id);
		}
		if (dsInterface == null) {
			setFocusedDrawingSurface(id);
		}

		// This is called after notification of listeners as it uses them.
		drawingSurface.initializeWorkspace();
		return drawingSurface;
	}

	public DrawingSurfaceInterface getDrawingSurfaceById(Object id) {
		return getDrawingSurface(id);
	}

	public void removeDrawingSurface(Object id) {
		DrawingSurface drawingSurface = getDrawingSurface(id);
		if (drawingSurface != null) {
			drawingSurface.setDisposed(true);

			Iterator iterator = dsEventListeners.iterator();
			while (iterator.hasNext()) {
				((DsEventListener)iterator.next()).onDsRemoved(id);
			}
			
			if (dsInterface == drawingSurface.getGuimenu()) {
				dsInterface = null;
			}
		}
	}

	public DrawingSurfaceInterface getFocusedDrawingSurface() {
		if (dsInterface != null) {
			return dsInterface.getDrawingSurface(); 
		}
		return null;
	}
	
	public void setFocusedDrawingSurface(Object id) {
		if (id != null) {
			DrawingSurface drawingSurface = getDrawingSurface(id);
			if (drawingSurface != null && !drawingSurface.isDisposed()) {
				dsInterface = drawingSurface.getGuimenu();
				
				Iterator iterator = dsEventListeners.iterator();
				while (iterator.hasNext()) {
					((DsEventListener)iterator.next()).onDsFocused(id);
				}
				
				if (desktopInterface != null) {
				    desktopInterface.setFocused(drawingSurface.getDisplayer());
				}
				
				drawingSurface.updateWorkspaceScale();
			} else {
				dsInterface = null;
			}
			
		    // check the filesystem changes when a frame gets focus 
			DataSynchronizer.getInstance().checkFilesystemChanges(null);
		} else {
			dsInterface = null;
		}
	}

	public DrawingSurfaceInterface[] getDrawingSurfaces() {
		Vector vector = getAllDrawingSurfaces();
		DrawingSurfaceInterface[] array = new DrawingSurfaceInterface[vector.size()];
		vector.copyInto(array);
		return array;
	}

	public void closeDrawingSurface(Object id) {
		DrawingSurfaceInterface surface = getDrawingSurfaceById(id);
		if (surface != null) {
			if (DataSynchronizer.getInstance().confirmFileClose(surface.getDsId(), false)) {
				surface.close();
			}
		}
	}
	
	public void addDsEventListener(DsEventListener listener) {
		dsEventListeners.add(listener);
	}
	
	public void removeDsEventListener(DsEventListener listener) {
		dsEventListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.LinkCommandInterface#linkCommand(com.cosylab.vdct.graphics.objects.VisibleObject, com.cosylab.vdct.graphics.objects.LinkSource)
	 */
	public void linkCommand(VisibleObject linkObject, LinkSource linkData) {
		if (dsInterface != null) {
			dsInterface.getDrawingSurface().linkCommand(linkObject, linkData);
		}			
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.VDBInterface#checkGroupName(java.lang.String, boolean)
	 */
	public String checkGroupName(String name, boolean relative) {
		if (dsInterface != null) {
			return dsInterface.checkGroupName(name, relative);
		}
		return "Error: No drawing surface selected.";
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.VDBInterface#checkRecordName(java.lang.String, java.lang.String, boolean)
	 */
	public String checkRecordName(String name, String oldName, boolean relative) {
		if (dsInterface != null) {
			return dsInterface.checkRecordName(name, oldName, relative);
		}
		return "Error: No drawing surface selected.";
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.VDBInterface#createBox()
	 */
	public Box createBox() {
		if (dsInterface != null) {
			return dsInterface.createBox();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.VDBInterface#createLine()
	 */
	public Line createLine() {
		if (dsInterface != null) {
			return dsInterface.createLine();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.VDBInterface#createRecord(java.lang.String, java.lang.String, boolean)
	 */
	public void createRecord(String name, String type, boolean relative) {
		if (dsInterface != null) {
			dsInterface.createRecord(name, type, relative);
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.VDBInterface#createTextBox()
	 */
	public TextBox createTextBox() {
		if (dsInterface != null) {
			return dsInterface.createTextBox();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.VDBInterface#isErrorMessage(java.lang.String)
	 */
	public boolean isErrorMessage(String message) {
		if (dsInterface != null) {
			return dsInterface.isErrorMessage(message);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#baseView()
	 */
	public void baseView() {
		if (dsInterface != null) {
			dsInterface.baseView();
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#copy()
	 */
	public void copy() {
		if (dsInterface != null) {
			dsInterface.copy();
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#cut()
	 */
	public void cut() {
		if (dsInterface != null) {
			dsInterface.cut();
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#delete()
	 */
	public void delete() {
		if (dsInterface != null) {
			dsInterface.delete();
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#export(java.io.File)
	 */
	public void export(File file) throws IOException {
		if (dsInterface != null) {
			dsInterface.export(file);
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#exportAsGroup(java.io.File)
	 */
	public void exportAsGroup(File file) throws IOException {
		if (dsInterface != null) {
			dsInterface.exportAsGroup(file);
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#group(java.lang.String)
	 */
	public void group(String groupName) {
		if (dsInterface != null) {
			dsInterface.group(groupName);
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#importBorder(java.io.File)
	 */
	public void importBorder(File file) throws IOException {
		if (dsInterface != null) {
			dsInterface.importBorder(file);
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#importDB(java.io.File)
	 */
	public void importDB(File file) throws IOException {
		if (dsInterface != null) {
			dsInterface.importDB(file);
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#importDBD(java.io.File)
	 */
	public void importDBD(File file) throws IOException {
		if (dsInterface != null) {
			dsInterface.importDBD(file);
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#importFields(java.io.File)
	 */
	public void importFields(File file) throws IOException {
		if (dsInterface != null) {
			dsInterface.importFields(file);
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#importTemplateDB(java.io.File)
	 */
	public void importTemplateDB(File file) throws IOException {
		if (dsInterface != null) {
			dsInterface.importTemplateDB(file);
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#isModified()
	 */
	public boolean isModified() {
		if (dsInterface != null) {
			return dsInterface.isModified();
		}
		return false;
	}

	public boolean isMacroPortsIDChanged() {
		
		Iterator iterator = getAllDrawingSurfaces().iterator();
		while (iterator.hasNext()) {
			if (Group.hasMacroPortsIDChanged(((DrawingSurface)iterator.next()).getDsId())) {
				return true;
			}
		}
	    return false;	
	}
	
	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#levelUp()
	 */
	public void levelUp() {
		if (dsInterface != null) {
			dsInterface.levelUp();
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#loadRdbGroup(javax.swing.JFrame)
	 */
	public void loadRdbGroup(JFrame guiContext) {
		if (desktopInterface != null) {
			desktopInterface.createNewInternalFrame();
			if (dsInterface != null) {
				dsInterface.loadRdbGroup(guiContext);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#morph()
	 */
	public void morph() {
		if (dsInterface != null) {
			dsInterface.morph();
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#morph(java.lang.String, java.lang.String)
	 */
	public void morph(String name, String newType) {
		if (dsInterface != null) {
			dsInterface.morph(name, newType);
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#moveOrigin(int)
	 */
	public void moveOrigin(int direction) {
		if (dsInterface != null) {
			dsInterface.moveOrigin(direction);
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#newCmd()
	 */
	public void newCmd() {
		if (desktopInterface != null) {
		    desktopInterface.createNewInternalFrame();
		}
		if (dsInterface != null) {
			dsInterface.newCmd();
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#openDB(java.io.File)
	 */
	public void openDB(File file) throws IOException {
		// Recycle exiting drawing surface if it is empty and unmodified.
		if (desktopInterface != null && (dsInterface == null || dsInterface.isModified() || !dsInterface.isEmpty())) {
			desktopInterface.createNewInternalFrame();
		}
		if (dsInterface != null) {
			dsInterface.openDB(file);
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#openDBD(java.io.File)
	 */
	public void openDBD(File file) throws IOException {
		if (dsInterface != null) {
			dsInterface.openDBD(file);
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#paste()
	 */
	public void paste() {
		if (dsInterface != null) {
			dsInterface.paste();
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#pasteAtPosition(int, int)
	 */
	public void pasteAtPosition(int pX, int pY) {
		if (dsInterface != null) {
			dsInterface.pasteAtPosition(pX, pY);
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#print()
	 */
	public void print() {
		if (dsInterface != null) {
			dsInterface.print();
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#redo()
	 */
	public void redo() {
		if (dsInterface != null) {
			dsInterface.redo();
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#rename()
	 */
	public void rename() {
		if (dsInterface != null) {
			dsInterface.rename();
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#rename(java.lang.String, java.lang.String)
	 */
	public void rename(String oldName, String newName) {
		if (dsInterface != null) {
			dsInterface.rename(oldName, newName);
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#save(java.io.File)
	 */
	public void save(File file) throws IOException {
		if (dsInterface != null) {
			dsInterface.save(file);
			DataSynchronizer.getInstance().checkFilesystemChanges(dsInterface.getDsId());
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#saveAsGroup(java.io.File)
	 */
	public void saveAsGroup(File file) throws IOException {
		if (dsInterface != null) {
			dsInterface.saveAsGroup(file);
			DataSynchronizer.getInstance().checkFilesystemChanges(dsInterface.getDsId());
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#saveAsRdbGroup(javax.swing.JFrame)
	 */
	public void saveRdbGroup(JFrame guiContext, boolean dialog) {
		if (dsInterface != null) {
			dsInterface.saveRdbGroup(guiContext, dialog);
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#saveAsTemplate(java.io.File)
	 */
	public void saveAsTemplate(File file) throws IOException {
		if (dsInterface != null) {
			dsInterface.saveAsTemplate(file);
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#selectAll()
	 */
	public void selectAll() {
		if (dsInterface != null) {
			dsInterface.selectAll();
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#setFlatView(boolean)
	 */
	public void setFlatView(boolean state) {
		if (dsInterface != null) {
			dsInterface.setFlatView(state);
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#setScale(double)
	 */
	public void setScale(double scale) {
		if (dsInterface != null) {
			dsInterface.setScale(scale);
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#showGrid(boolean)
	 */
	public void showGrid(boolean state) {
		Iterator iterator = getAllDrawingSurfaces().iterator();
		while (iterator.hasNext()) {
			((DrawingSurface)iterator.next()).repaint();
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#showNavigator(boolean)
	 */
	public void showNavigator(boolean state) {
		if (dsInterface != null) {
			dsInterface.showNavigator(state);
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#smartZoom()
	 */
	public void smartZoom() {
		if (dsInterface != null) {
			dsInterface.smartZoom();
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#snapToGrid(boolean)
	 */
	public void snapToGrid(boolean state) {
		if (dsInterface != null) {
			dsInterface.snapToGrid(state);
		}
	}

	/**
	 * 
	 * @see com.cosylab.vdct.graphics.DSGUIInterface#systemCopy()
	 */
	public void systemCopy() {
		if (dsInterface != null) {
			dsInterface.systemCopy();
		}
	}

	/**
	 * 
	 * @see com.cosylab.vdct.graphics.DSGUIInterface#systemPaste()
	 */
	public void systemPaste() {
		if (dsInterface != null) {
			dsInterface.systemPaste();
		}
	}

	/**
	 * 
	 * @see com.cosylab.vdct.graphics.DSGUIInterface#undo()
	 */
	public void undo() {
		if (dsInterface != null) {
			dsInterface.undo();
		}
	}

	/**
	 * 
	 * @see com.cosylab.vdct.graphics.DSGUIInterface#ungroup()
	 */
	public void ungroup() {
		if (dsInterface != null) {
			dsInterface.ungroup();
		}
	}

	/**
	 * 
	 * @see com.cosylab.vdct.graphics.DSGUIInterface#updateGroupLabel()
	 */
	public void updateGroupLabel() {
		if (dsInterface != null) {
			dsInterface.updateGroupLabel();
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.RepaintInterface#repaint()
	 */
	public void repaint(boolean highlighted) {
		if (dsInterface != null) {
			dsInterface.getDrawingSurface().repaint(highlighted);
		}
	}

	public void repaintAll(boolean highlighted) {
		Iterator iterator = getAllDrawingSurfaces().iterator();
		while (iterator.hasNext()) {
			((DrawingSurface)iterator.next()).repaint(highlighted);
		}
	}
	
	public void reset() {
		Iterator iterator = getAllDrawingSurfaces().iterator();
		while (iterator.hasNext()) {
			((DrawingSurface)iterator.next()).reset();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.print.Pageable#getNumberOfPages()
	 */
	public int getNumberOfPages() {
		if (dsInterface != null) {
			return dsInterface.getDrawingSurface().getNumberOfPages();
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see java.awt.print.Pageable#getPageFormat(int)
	 */
	public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
		if (dsInterface != null) {
			return dsInterface.getDrawingSurface().getPageFormat(pageIndex);
		}
		throw new IndexOutOfBoundsException();
	}

	/* (non-Javadoc)
	 * @see java.awt.print.Pageable#getPrintable(int)
	 */
	public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
		if (dsInterface != null) {
			return dsInterface.getDrawingSurface().getPrintable(pageIndex);
		}
		throw new IndexOutOfBoundsException();
	}
	
	public void close() {
		if (dsInterface != null) {
			if (DataSynchronizer.getInstance().confirmFileClose(dsInterface.getDsId(), false)) {
    			dsInterface.getDrawingSurface().close();
			}
		}
	}
	public void closeAll() {
		if (DataSynchronizer.getInstance().confirmFileClose(null, false)) {
			Iterator iterator = getAllDrawingSurfaces().iterator();
			while (iterator.hasNext()) {
				((DrawingSurface)iterator.next()).close();
			}
		}
	}
}
