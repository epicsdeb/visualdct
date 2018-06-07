package com.cosylab.vdct.graphics;

/**
 * Copyright (c) 2002, Cosylab, Ltd., Control System Laboratory, www.cosylab.com
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

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import com.cosylab.vdct.Constants;
import com.cosylab.vdct.DataProvider;
import com.cosylab.vdct.Settings;
import com.cosylab.vdct.VisualDCT;
import com.cosylab.vdct.events.CommandManager;
import com.cosylab.vdct.events.commands.ShowMorphingDialog;
import com.cosylab.vdct.events.commands.ShowRenameDialog;
import com.cosylab.vdct.graphics.objects.Border;
import com.cosylab.vdct.graphics.objects.Box;
import com.cosylab.vdct.graphics.objects.Connector;
import com.cosylab.vdct.graphics.objects.Flexible;
import com.cosylab.vdct.graphics.objects.Group;
import com.cosylab.vdct.graphics.objects.Line;
import com.cosylab.vdct.graphics.objects.Macro;
import com.cosylab.vdct.graphics.objects.Morphable;
import com.cosylab.vdct.graphics.objects.Movable;
import com.cosylab.vdct.graphics.objects.NamingContext;
import com.cosylab.vdct.graphics.objects.Port;
import com.cosylab.vdct.graphics.objects.Record;
import com.cosylab.vdct.graphics.objects.Template;
import com.cosylab.vdct.graphics.objects.TextBox;
import com.cosylab.vdct.graphics.objects.VisibleObject;
import com.cosylab.vdct.plugin.config.PluginNameConfigManager;
import com.cosylab.vdct.rdb.RdbDataId;
import com.cosylab.vdct.undo.ComposedAction;
import com.cosylab.vdct.undo.CreateAction;
import com.cosylab.vdct.undo.DeleteAction;
import com.cosylab.vdct.undo.MorphAction;
import com.cosylab.vdct.undo.MorphTemplateAction;
import com.cosylab.vdct.undo.MoveToGroupAction;
import com.cosylab.vdct.undo.RenameAction;
import com.cosylab.vdct.undo.UndoManager;
import com.cosylab.vdct.util.StringUtils;
import com.cosylab.vdct.vdb.VDBData;
import com.cosylab.vdct.vdb.VDBRecordData;
import com.cosylab.vdct.vdb.VDBTemplate;
import com.cosylab.vdct.vdb.VDBTemplateInstance;

/**
 * Insert the type's description here.
 * Creation date: (4.2.2001 15:32:01)
 * @author Matej Sekoranja
 */
public class DSGUIInterface implements VDBInterface {

	private DrawingSurface drawingSurface;
	private Object id = null;
	private CopyContext copyContext;

	private double pasteX = 0;
	private double pasteY = 0;

	private static final String emptyString = "";
	private static final String errorString = "Error: ";
	private static final String warningString = "Warning: ";

	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:49)
	 * @param drawingSurface com.cosylab.vdct.graphics.DrawingSurface
	 */
	public DSGUIInterface(DrawingSurface drawingSurface, CopyContext copyContext) {
		this.drawingSurface=drawingSurface;
		this.id = drawingSurface.getDsId();
		this.copyContext = copyContext;

		// Init the name configuration.
		PluginNameConfigManager.getInstance();
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:12:21)
	 */
	public void moveOrigin(int direction)
	{
		int dx = 0; 
		int dy = 0;
		ViewState view = ViewState.getInstance(id);
		int d = (int)(100*view.getScale()); 

		switch (direction)
		{
		case SwingConstants.WEST:
			dx =- d;
			break;
		case SwingConstants.EAST:
			dx =+ d;
			break;
		case SwingConstants.NORTH:
			dy =+ d;
			break;
		case SwingConstants.SOUTH:
			dy =- d;
			break;
		}

		if (view.moveOrigin(dx, dy))
		{
			drawingSurface.setBlockNavigatorRedrawOnce(true);
			drawingSurface.recalculateNavigatorPosition();
			drawingSurface.repaint();
		}
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 */
	public void baseView() {
		drawingSurface.baseView();
	}
	/**
	 * Returns error message or null if OK
	 * Creation date: (3.2.2001 22:11:01)
	 * @return java.lang.String
	 * @param name java.lang.String
	 */
	public java.lang.String checkGroupName(String name, boolean relative) {
		return checkRecordName(name, null, relative);
	}
	/**
	 * Returns error message or null if OK
	 * Creation date: (3.2.2001 22:11:01)
	 * @return java.lang.String
	 * @param name java.lang.String
	 */
	public java.lang.String checkRecordName(String name, String oldName, boolean relative) {

		String[] names = StringUtils.expandMacros(name);

		if (names == null) {
			return errorString + "Won't add more than " + Constants.MAX_NAME_MACRO_EXPANSIONS + " records!";
		}

		String recordName = null;
		String nameString = null;

		for (int n = 0; n < names.length; n++) {
			recordName = names[n];
			nameString = recordName + ": ";

			if (recordName.trim().length() == 0) {
				return errorString + nameString + "Empty name!";
			} else if (recordName.indexOf(' ') != -1) {
				return errorString + nameString + "No spaces allowed!";
			} else if (recordName.indexOf('"') != -1) {
				return errorString + nameString + "No quotes allowed!";
			} else if (!recordName.equals(oldName)
					&& ((!relative && Group.getRoot(id).findObject(recordName, true) != null) ||
							(relative && drawingSurface.getViewGroup().findObject(recordName, true) != null))) { 
				return errorString + nameString + "Name already exists!";
			} else if (recordName.length()>Settings.getInstance().getRecordLength()) {
				return warningString + nameString + "Name length is "+recordName.length()+" characters!";
			}

			String errorStr = PluginNameConfigManager.getInstance().checkValidity(recordName);
			if (errorStr != null) {
				return warningString + nameString + errorStr;
			}
		}
		return null;
	}

	public boolean isErrorMessage(String message) {
		return message != null && !message.startsWith(warningString);
	}

	public void copyToSystemClipboard(Vector objs)
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			NamingContext nc = new NamingContext(null, Group.getEditingTemplateData(id), null, null, false);

			if (Group.getEditingTemplateData(id) != null)
			{
				boolean hasPortOrMacro = false;
				Enumeration en = objs.elements();
				while (en.hasMoreElements())
				{
					Object o = en.nextElement();
					if (o instanceof Port || o instanceof Macro)
					{
						hasPortOrMacro = true;
						break;
					}
				}
				if (hasPortOrMacro)
					Group.writeTemplateData(id, dos, nc, objs);
			}

			Group.writeObjects(id, objs, dos, nc, false);
			Group.writeVDCTObjects(objs, dos, nc, false);

			StringSelection ss = new StringSelection(baos.toString());
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
		}
		catch (Throwable th) 
		{
			th.printStackTrace();
		}
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 */
	public void copy() {

		ViewState view = ViewState.getInstance(id);
		
		Vector objects = view.getSelectedObjects();

		if (objects.size() == 0) {
			return;
		}
		copy(objects);
		copyContext.setPasteCount(0);
		copyContext.setDoOffsetAtPaste(true);
		
		ViewState.getInstance(id).deselectAll();
		drawingSurface.repaint();
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 */
	public void systemCopy() {
		ViewState view = ViewState.getInstance(id);
		copyToSystemClipboard(view.getSelectedObjects());
		view.deselectAll();
		drawingSurface.repaint();
	}

	private void copy(Vector objects) {
		
		Group.getClipboard().destroy();

		ViewState view = ViewState.getInstance(id);

		Object obj;
		Enumeration selected = objects.elements();

		int minx=Integer.MAX_VALUE, miny=Integer.MAX_VALUE;
		while (selected.hasMoreElements()) {
			obj = selected.nextElement();
			if (obj instanceof VisibleObject) {
				minx = Math.min(minx, ((VisibleObject)obj).getX());
				miny = Math.min(miny, ((VisibleObject)obj).getY());
			}
		}

		// remember position for paste
		copyContext.setDsId(id);
		pasteX = minx - view.getRx() / view.getScale();
		pasteY = miny - view.getRy() / view.getScale();

		selected = objects.elements();	
		while (selected.hasMoreElements()) {
			obj = selected.nextElement();
			if (obj instanceof Flexible && obj instanceof VisibleObject) {
				Flexible copy = ((Flexible)obj).copyToGroup(Constants.DEFAULT_NAME, emptyString);
				if (copy != null && copy instanceof Movable) {
					((Movable)copy).move(-minx, -miny);
				}
			}
		}
		// fix links (due to order of copying links might not be validated...)
		Group.getClipboard().manageLinks(true);
	}

	public Box createBox()
	{
		Group parentGroup = drawingSurface.getViewGroup();

		ViewState view = ViewState.getInstance(id);
		double scale = view.getScale();

		int posX = (int)((drawingSurface.getPressedX() + view.getRx()) / scale);
		int posY = (int)((drawingSurface.getPressedY() + view.getRy()) / scale);

		//String parentName = parentGroup.getAbsoluteName();

		Box grBox = new Box(null, parentGroup, posX, posY, posX, posY);
		if (Settings.getInstance().getSnapToGrid())
			grBox.snapToGrid();

		Group.getRoot(id).addSubObject(grBox.getName(), grBox, true);

		drawingSurface.repaint();

		return grBox;
	}

	public Line createLine()
	{
		Group parentGroup = drawingSurface.getViewGroup();

		ViewState view = ViewState.getInstance(id);
		double scale = view.getScale();

		int posX = (int)((drawingSurface.getPressedX() + view.getRx()) / scale);
		int posY = (int)((drawingSurface.getPressedY() + view.getRy()) / scale);

		//String parentName = parentGroup.getAbsoluteName();

		Line grLine = new Line(null, parentGroup, posX, posY, posX, posY);
		if (Settings.getInstance().getSnapToGrid())
			grLine.snapToGrid();

		Group.getRoot(id).addSubObject(grLine.getName(), grLine, true);

		drawingSurface.repaint();

		return grLine;
	}

	public TextBox createTextBox()
	{
		Group parentGroup = drawingSurface.getViewGroup();

		ViewState view = ViewState.getInstance(id);
		double scale = view.getScale();

		int posX = (int)((drawingSurface.getPressedX() + view.getRx()) / scale);
		int posY = (int)((drawingSurface.getPressedY() + view.getRy()) / scale);

		//String parentName = parentGroup.getAbsoluteName();

		TextBox grTextBox = new TextBox(null, parentGroup, posX, posY, posX, posY);
		if (Settings.getInstance().getSnapToGrid())
			grTextBox.snapToGrid();

		Group.getRoot(id).addSubObject(grTextBox.getName(), grTextBox, true);

		grTextBox.setBorder(true);

		drawingSurface.repaint();

		return grTextBox;
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (3.2.2001 23:27:30)
	 * @param name java.lang.String
	 * @param type java.lang.String
	 * @param relative boolean
	 */
	public void createRecord(String name, String type, boolean relative) {

		String[] names = StringUtils.expandMacros(name);

		/* If more than Constants.MAX_NAME_MACRO_EXPANSIONS records would be added.
		 * Gui forbids this so we should never get null.
		 */
		if (names == null) {
			return;
		}

		ViewState view = ViewState.getInstance(id);
		double scale = view.getScale();
		int posX = drawingSurface.getPressedX();
		int posY = drawingSurface.getPressedY();

		UndoManager undoManager = UndoManager.getInstance(id);

		try {
			undoManager.startMacroAction();

			for (int n = 0; n < names.length; n++) {
				String recordName = names[n];

				String errmsg = checkRecordName(recordName, null, true);
				if (errmsg != null && isErrorMessage(errmsg)) {
					continue;
				}

				if (relative) {
					String parentName = drawingSurface.getViewGroup().getAbsoluteName();
					if (parentName.length()>0)
						recordName = parentName + Constants.GROUP_SEPARATOR + recordName;
				}

				VDBRecordData recordData = VDBData.getNewVDBRecordData(id,
						DataProvider.getInstance().getDbdDB(), type, recordName);
				if (recordData==null) {
					com.cosylab.vdct.Console.getInstance().println("o) Interal error: failed to create record "+recordName+" ("+type+")!");
					continue;
				}

				int recPosX = (int)((posX + view.getRx()) / scale);
				int recPosY = (int)((posY + view.getRy()) / scale);

				Record record = new Record(Group.getRoot(id), recordData, recPosX, recPosY);
				recordData.setRecord(record);

				Point move = record.getMoveInsideView();
				record.move(move.x, move.y);

				posX += view.getGridSize() * 2;  
				posY += view.getGridSize() * 2;
				drawingSurface.setPressedMousePos(posX, posY);

				record.setParent(null);
				Group.getRoot(id).addSubObject(recordName, record, true);

				if (Settings.getInstance().getSnapToGrid())
					record.snapToGrid();

				undoManager.addAction(new CreateAction(record));
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally { 
			undoManager.stopMacroAction();

			//drawingSurface.setModified(true);
			drawingSurface.repaint();
		}
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 */
	public void cut() {
		
		ViewState view = ViewState.getInstance(id);
		//copyToSystemClipboard(view.getSelectedObjects());

		Vector objects = view.getSelectedObjects();
		if (objects.size() == 0) {
			return;
		}
		
		copy(objects);

		try	{
			UndoManager.getInstance(id).startMacroAction();

			Object obj = null;
			Enumeration selected = objects.elements();
			while (selected.hasMoreElements()) {
				obj = selected.nextElement();
				if (obj instanceof Flexible && obj instanceof VisibleObject) {
					((VisibleObject)obj).destroy();
					UndoManager.getInstance(id).addAction(new DeleteAction((VisibleObject)obj));
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			UndoManager.getInstance(id).stopMacroAction();
		}

		view.deselectAll();
		view.deblinkAll();
		view.setAsHilited(null);
		copyContext.setDoOffsetAtPaste(false);
		drawingSurface.repaint();
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 */
	public void delete() {
		ViewState view = ViewState.getInstance(id);
		if (view.getSelectedObjects().size()==0) return;

		try	{

			UndoManager.getInstance(id).startMacroAction();

			VisibleObject obj;
			Enumeration selected = view.getSelectedObjects().elements();
			while (selected.hasMoreElements())
			{
				obj = (VisibleObject)selected.nextElement();

				if (obj instanceof Connector)
				{
					((Connector)obj).bypass();
				}
				else
				{
					obj.destroy();
					UndoManager.getInstance(id).addAction(new DeleteAction(obj));
				}

			}

		} catch (Exception e) {
		} finally {
			UndoManager.getInstance(id).stopMacroAction();
		}

		view.deselectAll();
		view.deblinkAll();
		view.setAsHilited(null);
		drawingSurface.repaint();
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 */
	public void group(String groupName) {
		ViewState view = ViewState.getInstance(id);
		if (view.getSelectedObjects().size()==0) return;

		ComposedAction composedAction = new ComposedAction();

		Group g = (Group)Group.getRoot(id).findObject(groupName, true);
		if (g==null)
		{
			g = Group.createGroup(id, groupName);
			if (Settings.getInstance().getSnapToGrid())
				g.snapToGrid();
			composedAction.addAction(new CreateAction(g));
		}

		int n = 0; int avgX = 0; int avgY = 0;
		Object obj; Flexible flex; String oldGroup;
		Enumeration selected = view.getSelectedObjects().elements();
		while (selected.hasMoreElements()) {
			obj = selected.nextElement();
			if (obj instanceof Flexible)
			{
				flex = (Flexible)obj; oldGroup = Group.substractParentName(flex.getFlexibleName()); 
				flex.moveToGroup(id, groupName);

				composedAction.addAction(new MoveToGroupAction(flex, oldGroup, groupName, id));		// if true ?!!!

				if (obj instanceof VisibleObject)
				{
					VisibleObject vo = (VisibleObject)obj;
					avgX += vo.getX();	avgY += vo.getY(); n++;
				}
			}
		}

		UndoManager.getInstance(id).addAction(composedAction);

		//g = (Group)Group.getRoot().findObject(groupName, true);
		if ((g!=null) && (n!=0)) {
			// center of all
			g.setX(avgX/n); g.setY(avgY/n);
		}
		view.deselectAll();
		if (g.getParent()==drawingSurface.getViewGroup())
			view.setAsSelected(g);
		drawingSurface.getViewGroup().manageLinks(true);
		drawingSurface.repaint();
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 * @param file java.io.File
	 */
	public void importDB(java.io.File file) throws IOException {
		drawingSurface.importDB(file);
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 * @param file java.io.File
	 */
	public void importTemplateDB(java.io.File file) throws IOException {
		drawingSurface.open(file, true);
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 * @param file java.io.File
	 */
	public void importFields(java.io.File file) throws IOException {
		int result = JOptionPane.showConfirmDialog(VisualDCT.getInstance(),
				"Do you want to ignore database link fields?",
				"Import fields", 
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);

		// window closed
		if (result == JOptionPane.CLOSED_OPTION)
			return;

		boolean ignoreLinkFields = (result == JOptionPane.OK_OPTION);
		drawingSurface.importFields(file, ignoreLinkFields);
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 * @param file java.io.File
	 */
	public void importBorder(java.io.File file) throws IOException {
		drawingSurface.importBorder(file);
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 * @param file java.io.File
	 */
	public void importDBD(java.io.File file) throws IOException {
		drawingSurface.openDBD(file, true);
	}

	public void loadRdbGroup(JFrame guiContext) {
		drawingSurface.loadRdbDbGroup(guiContext);
	}

	public void saveRdbGroup(JFrame guiContext, boolean dialog) {
		
		RdbDataId rdbId = new RdbDataId();
		VDBTemplate template = Group.getEditingTemplateData(id);
		if (template != null) {
			rdbId.setFileName(template.getFileName());
			rdbId.setVersion(template.getVersion());
			rdbId.setIoc(template.getIoc());
			rdbId.setDescription(template.getDescription());
		}
		
		boolean saved = drawingSurface.saveRdbGroup(guiContext, rdbId, dialog);
		
		if (saved) {
			String path = rdbId.getFileName();
			String fileName = new File(path).getName(); 
			String ioc = rdbId.getIoc(); 
			String version = rdbId.getVersion(); 
			String description = rdbId.getDescription(); 
			updateEditingTemplate(fileName, path, version, ioc, description);

			drawingSurface.setModified(false);
			UndoManager.getInstance(id).prepareAfterSaving();

			CommandManager.getInstance().execute("UpdateLoadLabel");
		}
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (29.4.2001 11:37:22)
	 * @return boolean
	 */
	public boolean isModified() {
		return drawingSurface.isModified();
	}

	public boolean isEmpty() {
		return Group.getRoot(id).getStructure().isEmpty();
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 */
	public void levelUp() {
		drawingSurface.moveLevelUp();
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 */
	public void newCmd() {
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 * @param file java.io.File
	 */
	public void openDB(java.io.File file) throws IOException {
		drawingSurface.open(file);
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 * @param file java.io.File
	 */
	public void openDBD(java.io.File file) throws IOException {
		drawingSurface.openDBD(file);
	}

	public void systemPaste() {
		try
		{
			Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
			boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
			if (hasTransferableText)
			{
				String str = (String)contents.getTransferData(DataFlavor.stringFlavor);
				if (str == null || str.length() == 0)
					return;
				ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes());
				drawingSurface.open(bais, null, true, true);
			}
		}
		catch (Throwable th) 
		{
			th.printStackTrace();
		}
	}

	public void paste() {
		// do some offset (a little trick to have snapping also done) for copy only
		final int OFFSET = Constants.GRID_SIZE;
		double scale = ViewState.getInstance(id).getScale();
		if (copyContext.isDoOffsetAtPaste())
			pasteAtPosition((int)((pasteX+OFFSET)*scale), (int)((pasteY+OFFSET)*scale));
		else
			pasteAtPosition((int)(pasteX*scale), (int)(pasteY*scale));
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 */
	public void pasteAtPosition(int pX, int pY) {
		ViewState view = ViewState.getInstance(id);
		String currentGroupName = drawingSurface.getViewGroup().getAbsoluteName();

		Group group = Group.getClipboard();

		int size = group.getSubObjectsV().size();
		Object sourceDsId = copyContext.getDsId();

		// If source of the data not set, treat as no data.
		if (size == 0 || sourceDsId == null) {
			return;
		}

		double scale = view.getScale();
		int pasteCount = copyContext.getPasteCount();
		int posX = (int)((pX + view.getRx()) / scale) + pasteCount*Constants.MULTIPLE_PASTE_GAP;
		int posY = (int)((pY + view.getRy()) / scale) + pasteCount*Constants.MULTIPLE_PASTE_GAP;
		copyContext.setPasteCount(pasteCount + 1);

		posX = posX <= 0 ? 0 : posX;
		posX = posX >= view.getWidth() - group.getAbsoulteWidth() ? view.getWidth() - group.getAbsoulteWidth() : posX;
		posY = posY <= 0 ? 0 : posY;
		posY = posY >= view.getHeight() - group.getAbsoulteHeight() ? view.getHeight() - group.getAbsoulteHeight() : posY;

		Object objs[] = new Object[size];
		group.getSubObjectsV().copyInto(objs);

		view.deselectAll();
		
		ComposedAction action = new ComposedAction();

		Flexible flexible = null; 
		Flexible copy = null;
		for(int i = 0; i < size; i++) {
			if (objs[i] instanceof Flexible && objs[i] instanceof VisibleObject) {
				flexible = (Flexible)objs[i];
				
				copy = flexible.copyToGroup(id, currentGroupName);
				if (copy != null) {
					action.addAction(new CreateAction((VisibleObject)copy));
					if (copy instanceof Movable) {
						((Movable)copy).move(posX, posY);
					}
					view.setAsSelected((VisibleObject)copy);
				}
			}
		}

		if (!action.isEmpty()) {
			UndoManager.getInstance(id).addAction(action);
		}

		drawingSurface.getViewGroup().manageLinks(true);
		drawingSurface.repaint();
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 */
	public void print() {}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 */
	public void redo() {
		ViewState.getInstance(id).deselectAll();
		UndoManager.getInstance(id).redo();
		drawingSurface.repaint();
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 */
	public void rename() {

		ViewState view = ViewState.getInstance(id);
		int size = view.getSelectedObjects().size();
		if (size==0) return;

		Object objs[] = new Object[size];
		view.getSelectedObjects().copyInto(objs);

		for(int i=0; i<size; i++)
			if (objs[i] instanceof Flexible)
			{
				// call gui
				ShowRenameDialog cmd = (ShowRenameDialog)CommandManager.getInstance().getCommand("ShowRenameDialog");
				cmd.setOldName(((Flexible)objs[i]).getFlexibleName());
				cmd.execute();
			}
		view.deselectAll();
		drawingSurface.getViewGroup().manageLinks(true);

		drawingSurface.repaint();
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (3.5.2001 10:05:02)
	 */
	public void rename(java.lang.String oldName, java.lang.String newName) {
		ViewState view = ViewState.getInstance(id);
		Object obj = Group.getRoot(id).findObject(oldName, true);
		if (obj instanceof Flexible)
		{
			Flexible flex = (Flexible)obj;
			if (flex.rename(id, newName))
			{
				UndoManager.getInstance(id).addAction(new RenameAction(id, flex, oldName, newName));

				view.deselectObject((VisibleObject)obj);
				drawingSurface.getViewGroup().manageLinks(true);
				drawingSurface.repaint();
			}
		}
	}

	public void morph() {
		ViewState view = ViewState.getInstance(id);

		int size = view.getSelectedObjects().size();
		if (size==0) return;

		Object objs[] = new Object[size];
		view.getSelectedObjects().copyInto(objs);

		for(int i=0; i<size; i++)
			if (objs[i] instanceof Morphable)
			{
				// call gui
				ShowMorphingDialog cmd = (ShowMorphingDialog)CommandManager.getInstance().getCommand("ShowMorphingDialog");
				cmd.setName(((Morphable)objs[i]).getName());
				cmd.setOldType(((Morphable)objs[i]).getType());
				cmd.setTargets(((Morphable)objs[i]).getTargets());
				cmd.execute();
			}
		view.deselectAll();

		drawingSurface.repaint();
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (3.5.2001 10:05:02)
	 */
	public void morph(java.lang.String name, String newType) {
		ViewState view = ViewState.getInstance(id);
		Object oldObject = Group.getRoot(id).findObject(name, true);
		if (oldObject instanceof Record)
		{
			try {
				UndoManager.getInstance(id).startMacroAction();

				Record record = (Record)oldObject;

				VDBRecordData oldRecordData = record.getRecordData();

				if (record.morph(newType))
				{	
					UndoManager.getInstance(id).addAction(new MorphAction(record, oldRecordData, record.getRecordData()));

					view.deselectObject((VisibleObject)oldObject);
					drawingSurface.repaint();
				}
			} finally {
				UndoManager.getInstance(id).stopMacroAction();
			}
		}
		else if (oldObject instanceof Template)
		{
			try {
				UndoManager.getInstance(id).startMacroAction();

				Template template = (Template)oldObject;

				VDBTemplateInstance oldTemplateData = template.getTemplateData();

				if (template.morph(newType))
				{	
					UndoManager.getInstance(id).addAction(new MorphTemplateAction(template, oldTemplateData, template.getTemplateData()));

					view.deselectObject((VisibleObject)oldObject);
					drawingSurface.repaint();
				}
			} finally {
				UndoManager.getInstance(id).stopMacroAction();
			}
		}
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:48:27)
	 * @param file java.io.File
	 */
	public void save(File file) throws IOException {
		/*
 if (drawingSurface.isTemplateMode())
 {
 	saveAsTemplate(file);
 	return;
 }
		 */ 
		Group.save(id, Group.getRoot(id), file, false);

		VDBTemplate data = Group.getEditingTemplateData(id);
		
		// save as check
		if (data != null && !file.getAbsolutePath().equals(data.getFileName())) {
			// reload previous ...
			drawingSurface.reloadTemplate(data);
		}
		
		// possibly fix current id (basename) and path
		updateEditingTemplate(file.getName(), file.getAbsolutePath(), null, null, null);

		Group.getEditingTemplateData(id).setModificationTime(file.lastModified());

		// if ok
		drawingSurface.setModified(false);
		UndoManager.getInstance(id).prepareAfterSaving();

		CommandManager.getInstance().execute("UpdateLoadLabel");
		drawingSurface.updateFile(file);
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 * @param file java.io.File
	 */
	public void saveAsGroup(java.io.File file) throws IOException {
		Group.save(id, drawingSurface.getViewGroup(), file, false);

	}
	/**
	 * @see com.cosylab.vdct.graphics.GUIMenuInterface#saveAsTemplate(File)
	 */
	public void saveAsTemplate(File file) throws IOException
	{
		/*	
 VDBTemplate data = null; 
 Stack stack = drawingSurface.getTemplateStack();
 if (stack.isEmpty())
 {

	String id = file.getName();
	// remove spaces and extension
	id = id.replace(' ', '_');
	int pos = id.lastIndexOf('.');
	if (pos>0)
		id = id.substring(0, pos);

	// generate first free
	while (VDBData.getTemplates().containsKey(id))
		id = StringUtils.incrementName(id, null);

 	// create a new
	data = new VDBTemplate(id, file.getAbsolutePath());
	data.setDescription(data.getId());
	data.setInputs(new Hashtable());
	data.setInputComments(new Hashtable());
	data.setOutputs(new Hashtable());
	data.setOutputComments(new Hashtable());
	data.setGroup(Group.getRoot());
 	stack.push(data);

 	Group.setEditingTemplateData(data);
 }

 Group.saveAsTemplate(Group.getRoot(), file);

 drawingSurface.setModified(false);

 // show user template mode 
 drawingSurface.updateWorkspaceGroup();

 // new
 if (data!=null)
 {
 	// add to list of loaded templates
 	VDBData.getTemplates().put(data.getId(), data);

 	VisualDCT.getInstance().updateLoadLabel();
 }

 SetWorkspaceFile cmd = (SetWorkspaceFile)CommandManager.getInstance().getCommand("SetFile");
 cmd.setFile(file.getCanonicalPath());
 cmd.execute();
		 */
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:48:27)
	 * @param file java.io.File
	 */
	public void export(java.io.File file) throws IOException {
		Group.save(id, Group.getRoot(id), file, true); 
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 * @param file java.io.File
	 */
	public void exportAsGroup(java.io.File file) throws IOException {
		Group.save(id, drawingSurface.getViewGroup(), file, true);
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 */
	public void selectAll() {
		if (drawingSurface.getViewGroup().selectAllComponents())
			drawingSurface.repaint();
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 * @param state boolean
	 */
	public void setFlatView(boolean state) {
		drawingSurface.getView().setFlat(state);
		drawingSurface.getViewGroup().unconditionalValidateSubObjects(state);
		drawingSurface.repaint();
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:43:50)
	 * @param scale double
	 */
	public void setScale(double scale) {
		drawingSurface.setScale(scale);
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 * @param state boolean
	 */
	public void showGrid(boolean state) {
		drawingSurface.repaint();
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (27.4.2001 19:54:27)
	 * @param state boolean
	 */
	public void showNavigator(boolean state) {
		drawingSurface.repaint();
	}
	/**
	 * Zoom selection
	 * Creation date: (4.2.2001 15:57:56)
	 */
	public void smartZoom() {
		ViewState view = ViewState.getInstance(id);
		if (view.getSelectedObjects().size()==0) return;

		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;

		VisibleObject vo;
		Enumeration e = view.getSelectedObjects().elements();
		while (e.hasMoreElements())
		{
			vo = (VisibleObject)e.nextElement();
			if (vo instanceof Border)
			{
				Border b = (Border)vo;
				Enumeration e2 = b.getSubObjectsV().elements();
				while (e2.hasMoreElements())
				{
					vo = (VisibleObject)e2.nextElement();
					minX = Math.min(minX, vo.getRx());
					minY = Math.min(minY, vo.getRy());
					maxX = Math.max(maxX, vo.getRx()+vo.getRwidth());
					maxY = Math.max(maxY, vo.getRy()+vo.getRheight());
				}
			}
			else
			{
				minX = Math.min(minX, vo.getRx());
				minY = Math.min(minY, vo.getRy());
				maxX = Math.max(maxX, vo.getRx()+vo.getRwidth());
				maxY = Math.max(maxY, vo.getRy()+vo.getRheight());
			}
		}

		int space = (minX+minY+maxX+maxY)/75;
		drawingSurface.zoomArea(minX-space-view.getRx(), minY-space-view.getRy(),
				maxX+space-view.getRx(), maxY+space-view.getRy());

	}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 * @param state boolean
	 */
	public void snapToGrid(boolean state) {
		drawingSurface.getViewGroup().unconditionalValidateSubObjects(drawingSurface.isFlat());
		drawingSurface.repaint();
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 */
	public void undo() {
		ViewState.getInstance(id).deselectAll();
		UndoManager.getInstance(id).undo();
		drawingSurface.repaint();
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (4.2.2001 15:32:01)
	 */
	public void ungroup() {
		ViewState view = ViewState.getInstance(id);
		int size = view.getSelectedObjects().size();
		if (size==0) return;

		ComposedAction composedAction = new ComposedAction();

		String currentGroupName = drawingSurface.getViewGroup().getAbsoluteName();

		Object objs2[]; int size2;
		Group group;

		Object objs[] = new Object[size]; 
		view.getSelectedObjects().copyInto(objs);
		for (int i=0; i<size; i++) {
			if (objs[i] instanceof Group) {
				group = (Group)objs[i];
				view.deselectObject(group);
				size2 = group.getSubObjectsV().size();
				objs2 = new Object[size2];
				group.getSubObjectsV().copyInto(objs2);
				for (int j=0; j<size2; j++)
				{
					/*!!!can be outside	if (objs2[i] instanceof Movable)
					((Movable)objs2[i]).move(view.getRx()-group.getInternalRx(),
											 view.getRy()-group.getInternalRy());

					 */
					if (objs2[i] instanceof Flexible) {
						Flexible flex = (Flexible)objs2[j];
						flex.moveToGroup(id, currentGroupName);

						composedAction.addAction(new MoveToGroupAction(flex, group.getAbsoluteName(), currentGroupName, id));		// if true ?!!!


						view.setAsSelected((VisibleObject)objs2[j]);
					}
				}

				if (group.getSubObjectsV().size()==0) {
					group.destroy();
					composedAction.addAction(new DeleteAction(group));
				}
			}
		}

		UndoManager.getInstance(id).addAction(composedAction);

		drawingSurface.getViewGroup().manageLinks(true);
		drawingSurface.repaint();
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (22.4.2001 18:12:34)
	 */
	public void updateGroupLabel() {
		drawingSurface.updateWorkspaceGroup();
	}
	/**
	 * @return the drawingSurface
	 */
	public DrawingSurface getDrawingSurface() {
		return drawingSurface;
	}
	
	private void updateEditingTemplate(String fileName, String path, String version,
			String ioc, String description) {
		
		VDBTemplate data = Group.getEditingTemplateData(id);

		if (data == null) {
			data = new VDBTemplate(fileName, path);
			
			if (version != null) {
				data.setVersion(version);
			}

			data.setPorts(new Hashtable());
			data.setPortsV(new Vector());

			data.setMacros(new Hashtable());
			data.setMacrosV(new Vector());

			data.setGroup(Group.getRoot(id));

			Group.setEditingTemplateData(id, data);
			drawingSurface.getTemplateStack().push(data);

			VDBData.getInstance(id).addTemplate(data);
		}
		
		if (!path.equals(data.getFileName())) {
			data.setFileName(path); 	
			data.setId(id, fileName);
		}
		
		if (version != null) {
			data.setVersion(version);
		}
		if (ioc != null) {
			data.setIoc(ioc);
		}
		if (description != null) {
			data.setDescription(description);
		}
	}
	/**
	 * @return the id
	 */
	public Object getDsId() {
		return id;
	}
}
