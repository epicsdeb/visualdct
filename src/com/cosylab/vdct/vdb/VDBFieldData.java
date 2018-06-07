package com.cosylab.vdct.vdb;

import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Pattern;

import com.cosylab.vdct.Console;
import com.cosylab.vdct.Constants;
import com.cosylab.vdct.DataProvider;
import com.cosylab.vdct.dbd.DBDConstants;
import com.cosylab.vdct.dbd.DBDData;
import com.cosylab.vdct.dbd.DBDDeviceData;
import com.cosylab.vdct.dbd.DBDFieldData;
import com.cosylab.vdct.dbd.DBDMenuData;
import com.cosylab.vdct.dbd.DBDResolver;
import com.cosylab.vdct.graphics.objects.Debuggable;
import com.cosylab.vdct.graphics.objects.LinkSource;
import com.cosylab.vdct.graphics.objects.Record;
import com.cosylab.vdct.inspector.ChangableVisibility;
import com.cosylab.vdct.inspector.InspectableProperty;
import com.cosylab.vdct.inspector.InspectorManager;
import com.cosylab.vdct.plugin.debug.PluginDebugManager;
import com.cosylab.vdct.undo.FieldValueChangeAction;
import com.cosylab.vdct.undo.UndoManager;
import com.cosylab.vdct.util.StringUtils;

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

/**
 * This type was created in VisualAge.
 */
public class VDBFieldData implements InspectableProperty, Debuggable, ChangableVisibility, LinkSource {
	protected int type;
	protected int GUI_type;
	protected String name;
	protected String value;
	protected String init_value;
	protected String comment;
	protected DBDFieldData dbdData;
	protected VDBRecordData record = null;

	private static final String debugDefault = "###";
	protected String debugValue = debugDefault;
	
	private int positionIndex = -1;
	/**
	 * ISO 8601 date formatter.
	 */
	private static final SimpleDateFormat timeFormatter =
	//	new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		new SimpleDateFormat("HH:mm:ss.SSS");
	
	protected String debugValueTimeStamp = "n/a";
	protected short severity = 0;
	protected boolean connected = false;
	private static final String NAME_VAL = "VAL";
	
	protected int visibility = NON_DEFAULT_VISIBLE;

/**
 * Insert the method's description here.
 * Creation date: (11.1.2001 21:47:04)
 * @return boolean
 */
public boolean allowsOtherValues() {
	return true;
}
/**
 * Insert the method's description here.
 * Creation date: (9.12.2000 18:11:46)
 * @return java.lang.String
 */
public java.lang.String getComment() {
	return comment;
}
/**
 * Insert the method's description here.
 * Creation date: (11.1.2001 22:01:51)
 * @return com.cosylab.vdct.dbd.DBDFieldData
 */
public com.cosylab.vdct.dbd.DBDFieldData getDbdData() {
	return dbdData;
}
/**
 * Insert the method's description here.
 * Creation date: (1.2.2001 12:11:29)
 * @return java.lang.String
 */
public String getFullName() {
	if (getRecord()==null)
		return "(undefined)"+com.cosylab.vdct.Constants.FIELD_SEPARATOR+getName();
	else
		return getRecord().getName()+com.cosylab.vdct.Constants.FIELD_SEPARATOR+getName();
}
/**
 * Insert the method's description here.
 * Creation date: (9.12.2000 18:11:46)
 * @return int
 */
public int getGUI_type() {
	return GUI_type;
}
/**
 * Insert the method's description here.
 * Creation date: (26.1.2001 15:03:07)
 * @return java.lang.String
 */
public java.lang.String getHelp() {
	return dbdData != null ? dbdData.getPrompt_value() : null;
}
/**
 * Insert the method's description here.
 * Creation date: (9.12.2000 18:11:46)
 * @return java.lang.String
 */
public java.lang.String getInit_value() {
	return init_value;
}
/**
 * Insert the method's description here.
 * Creation date: (9.12.2000 18:11:46)
 * @return java.lang.String
 */
public java.lang.String getName() {
	return name;
}
/**
 * Insert the method's description here.
 * Creation date: (9.12.2000 18:11:46)
 * @return java.lang.String
 */
public java.lang.String toString() {
	return name;
}
/**
 * Insert the method's description here.
 * Creation date: (12.1.2001 20:01:39)
 * @return com.cosylab.vdct.vdb.VDBRecordData
 */
public VDBRecordData getRecord() {
	return record;
}
/**
 * Insert the method's description here.
 * Creation date: (11.1.2001 21:47:04)
 * @return java.lang.String[]
 */
public java.lang.String[] getSelectableValues() {
	if (dbdData != null && ((dbdData.getField_type() == DBDConstants.DBF_MENU) ||
		(dbdData.getField_type() == DBDConstants.DBF_DEVICE))) {

			DBDData dbd = DataProvider.getInstance().getDbdDB();
			Vector values = new Vector();

			if (dbdData.getField_type() == DBDConstants.DBF_MENU) {
					DBDMenuData md = dbd.getDBDMenuData(dbdData.getMenu_name());
					if (md!=null) {
						Enumeration e = md.getChoices().elements();

				 	if (dbdData.getInit_value().length()!=0)
						values.addElement(dbdData.getInit_value()+com.cosylab.vdct.Constants.MENU_DEFAULT_VALUE_INDICATOR);
					else
						values.addElement(com.cosylab.vdct.Constants.NONE);
						
						while (e.hasMoreElements())
							values.addElement(e.nextElement().toString());
					}					
					else Console.getInstance().println("Menu '"+dbdData.getMenu_name()+"' not found...");
			}
			else if (dbdData.getField_type() == DBDConstants.DBF_DEVICE) {
				
					Enumeration e = dbd.getDevices().elements();
					DBDDeviceData dev;
					
				 	if (dbdData.getInit_value().length()!=0)
						values.addElement(dbdData.getInit_value()+com.cosylab.vdct.Constants.MENU_DEFAULT_VALUE_INDICATOR);
					else
						values.addElement(com.cosylab.vdct.Constants.NONE);
					
					while (e.hasMoreElements()) {
						dev = (DBDDeviceData)(e.nextElement());
						if (record.getType().equals(dev.getRecord_type()))
							values.addElement(dev.getChoice_string());
					}
			}

			if (values.size()==0) return null;

			String choices[] = new String[values.size()];
			values.copyInto(choices);
			// !!!
			new com.cosylab.vdct.util.StringQuickSort().sort(choices);
			return choices;
			
	}
	else return null;
}
/**
 * Insert the method's description here.
 * Creation date: (9.12.2000 18:11:46)
 * @return int
 */
public int getType() {
	return type;
}
/**
 * Insert the method's description here.
 * Creation date: (9.12.2000 18:11:46)
 * @return java.lang.String
 */
public java.lang.String getValue() {
	if (!PluginDebugManager.isDebugState())
		return value;
	else
	{
		if (visibility == InspectableProperty.ALWAYS_VISIBLE || name.equals(NAME_VAL))
			return debugValue;
		else
			return value;	// if it is not monitored return non-debug value
	}
	
}
/**
 * Insert the method's description here.
 * Creation date: (11.1.2001 21:29:48)
 * @return java.lang.String
 */
public String getInitValue()
{
	if (dbdData != null && ((dbdData.getField_type()==DBDConstants.DBF_INLINK) ||
		(dbdData.getField_type()==DBDConstants.DBF_OUTLINK)))
	{
		// if not software
		String linkType = record.getDTYPLinkType();
		if (linkType!=null)
			return DataProvider.getInstance().getEditInitialValueLinkType(linkType);
		else
			return getInit_value();
	}
	else
		return getInit_value();
}
/**
 * Insert the method's description here.
 * Creation date: (27.1.2001 16:08:45)
 * @return boolean
 */
public boolean hasDefaultValue() {
	if (dbdData != null) {
		if (dbdData.getField_type()==DBDConstants.DBF_MENU ||
				dbdData.getField_type()==DBDConstants.DBF_DEVICE) {
			// if initial value is pecified, than it is explicity written
			if ((value.equals(com.cosylab.vdct.Constants.NONE) && dbdData.getInit_value().length()==0) ||
					(dbdData.getInit_value().length()>0 &&
							value.equals(dbdData.getInit_value()+com.cosylab.vdct.Constants.MENU_DEFAULT_VALUE_INDICATOR))) 
				return true;
			else
				return false;
		}
		else if (!value.equals(dbdData.getInit_value()))
			return false;
		else 
			return true;
	}
	else 
		return false;

}
/**
 * Insert the method's description here.
 * Creation date: (11.1.2001 21:47:04)
 * @return boolean
 */
public boolean isEditable() {
	return true;
}
/**
 * Insert the method's description here.
 * Creation date: (11.1.2001 21:47:04)
 * @return boolean
 */
public boolean isSeparator() {
	return false;
}
/**
 * Insert the method's description here.
 * Creation date: (9.12.2000 18:11:46)
 * @param newComment java.lang.String
 */
public void setComment(java.lang.String newComment) {
	comment = newComment;
}
/**
 * Insert the method's description here.
 * Creation date: (11.1.2001 22:01:51)
 * @param newDbdData com.cosylab.vdct.dbd.DBDFieldData
 */
public void setDbdData(com.cosylab.vdct.dbd.DBDFieldData newDbdData) {
	dbdData = newDbdData;
}

/**
 * @see com.cosylab.vdct.graphics.objects.Debuggable#setDebugValue(java.lang.String, java.util.Date, short)
 */
public void setDebugValue(String newValue, Date timeStamp, short severity)
{
	if (com.cosylab.vdct.plugin.debug.PluginDebugManager.isDebugState())
	{
		debugValueTimeStamp = timeFormatter.format(timeStamp);
		debugValue = newValue; 
		this.severity = severity;
		
		if (record!=null) record.fieldValueChanged(this);
	}
}
/**
 * Insert the method's description here.
 * Creation date: (9.12.2000 18:11:46)
 * @param newGUI_type int
 */
public void setGUI_type(int newGUI_type) {
	GUI_type = newGUI_type;
}
/**
 * Insert the method's description here.
 * Creation date: (9.12.2000 18:11:46)
 * @param newInit_value java.lang.String
 */
public void setInit_value(java.lang.String newInit_value) {
	init_value = newInit_value;
}
/**
 * Insert the method's description here.
 * Creation date: (9.12.2000 18:11:46)
 * @param newName java.lang.String
 */
public void setName(java.lang.String newName) {
	name = newName;
}
/**
 * Insert the method's description here.
 * Creation date: (12.1.2001 20:01:39)
 * @param newRecord com.cosylab.vdct.vdb.VDBRecordData
 */
public void setRecord(VDBRecordData newRecord) {
	record = newRecord;
}
/**
 * Insert the method's description here.
 * Creation date: (9.12.2000 18:11:46)
 * @param newType int
 */
public void setType(int newType) {
	type = newType;
}

/**
 * Insert the method's description here.
 * Creation date: (9.12.2000 18:11:46)
 */
public void updateInspector()
{
	Record visualRecord = (record != null) ? record.getRecord() : null;
	if (visualRecord != null) {
		InspectorManager.getInstance().updateProperty(visualRecord, this);
	}
}

/**
 * Insert the method's description here.
 * Creation date: (9.12.2000 18:11:46)
 * @param newValue java.lang.String
 */
public void setValue(java.lang.String newValue) {

	Object dsId = getDsId();

	if ((value!=null) && !value.equals(newValue) && (dsId != null))
		UndoManager.getInstance(dsId).addAction(
				new FieldValueChangeAction(this, value, newValue)
		);
	value = newValue;
	if (record!=null)
	{
		record.fieldValueChanged(this);

		// if DTYP has changed - notify all INP/OUT links
		if (name.equals("DTYP") && !com.cosylab.vdct.plugin.debug.PluginDebugManager.isDebugState())
		{
			java.util.Enumeration e = record.getFieldsV().elements();
			while (e.hasMoreElements())
			{
				VDBFieldData f = (VDBFieldData)e.nextElement();
				if (f!=this &&
						((f.getDbdData().getField_type()==DBDConstants.DBF_INLINK) ||
								(f.getDbdData().getField_type()==DBDConstants.DBF_OUTLINK)))
					f.updateInspector();
			}
		}
	}
}
/**
 * Insert the method's description here.
 * Creation date: (9.12.2000 18:11:46)
 * @param newValue java.lang.String
 */
public void setValueSilently(java.lang.String newValue) {
	value = newValue;
}
/**
 * Insert the method's description here.
 * Creation date: (24/8/99 15:29:04)
 */
public String getToolTipText()
{
	String type = null;
	if (dbdData != null) {

		type = DBDResolver.getFieldType(dbdData.getField_type());

		if ((dbdData.getField_type()==DBDConstants.DBF_INLINK) ||
				(dbdData.getField_type()==DBDConstants.DBF_OUTLINK))
		{
			// if not software
			String linkType = record.getDTYPLinkType();
			if (linkType!=null)
			{
				Pattern pattern = DataProvider.getInstance().getEditPatternLinkType(linkType);
				if (pattern!=null)
					type = type+" ["+DataProvider.getInstance().getEditDescriptionLinkType(linkType)+"]";
			}
		}
	}
	return type;
}
 
/**
 * Insert the method's description here.
 * Creation date: (24/8/99 15:29:04)
 * @return java.util.regex.Pattern
 */
public Pattern getEditPattern() {
	return null;
}

/**
 * Insert the method's description here.
 * Creation date: (11.1.2001 21:28:51)
 * @return boolean
 */
public boolean isValid() {
	return checkExpandedValues(value) == null;	
}
/**
 * Insert the method's description here.
 * Creation date: (11.1.2001 21:30:04)
 * @return int
 */
public int getVisibility()
{
	return visibility;
}

/**
 * Sets the visibility.
 * @param visibility The visibility to set
 */
public void setVisibility(int visibility)
{
	// debug plugin support
	if (visibility != this.visibility)
		if (PluginDebugManager.isDebugState())
			if (visibility == InspectableProperty.ALWAYS_VISIBLE)
				PluginDebugManager.getDebugPlugin().registerMonitor(this);
			else if (this.visibility == InspectableProperty.ALWAYS_VISIBLE)
				PluginDebugManager.getDebugPlugin().deregisterMonitor(this);


	this.visibility = visibility;
	if (record!=null) record.fieldValueChanged(this);
	//updateInspector();
}
/**
 * Insert the method's description here.
 * Creation date: (11.1.2001 21:30:04)
 * @param java.awt.Component
 * @param x
 * @param y
 */
public void popupEvent(Component component, int x, int y)
{
}

/**
 * Returns stringified timestamp of the last debug value update.
 * @return  stringified timestamp of the last debug value update.
 */
public String getDebugValueTimeStamp() {
	return debugValueTimeStamp;
}


/**
 * Get last debug value severity.
 * @return last debug value severity.
 */
public short getSeverity() {
	return severity;
}

/**
 * @see com.cosylab.vdct.graphics.objects.Debuggable#initialize()
 */
public void initialize() {
	debugValue = debugDefault;
	debugValueTimeStamp = "n/a";
	severity = 0;
}

/**
 * @see com.cosylab.vdct.graphics.objects.Debuggable#setConnected(boolean)
 */
public void setConnected(boolean connected) {
	this.connected = connected;
	
	// update
	if (record!=null) record.fieldValueChanged(this);
}

/**
 * Return debug connection status.
 * @return debug connection status
 */
public boolean isConnected() {
	return connected;
}

public void setPositionIndex(int index) {
    this.positionIndex = index;
}

public int getPositionIndex() {
    return positionIndex;
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
	return checkExpandedValues(value);
}

/* (non-Javadoc)
 * @see com.cosylab.vdct.inspector.InspectableProperty#getGuiGroup()
 */
public Integer getGuiGroup() {
	return new Integer(getGUI_type());
}

private Pattern getPattern()
{
	if ((dbdData != null) && ((dbdData.getField_type()==DBDConstants.DBF_INLINK) ||
		(dbdData.getField_type()==DBDConstants.DBF_OUTLINK)))
	{
		// if not software
		String linkType = record.getDTYPLinkType();
		if (linkType!=null)
			return DataProvider.getInstance().getEditPatternLinkType(linkType);
	}

	return null;
}

private String checkExpandedValues(String value) {
	
	Pattern pattern = getPattern();
	if (pattern == null) {
		return null;
	}

	String[] values = StringUtils.expandMacros(value);
	if (values == null) {
		return getHelp() + ": Error: Won't set more than " + Constants.MAX_NAME_MACRO_EXPANSIONS + " fields!";
	}
	
	for (int n = 0; n < values.length; n++) {
		if (!pattern.matcher(values[n]).matches()) {
			return getHelp() + ": Warning: " + values[n] + " doesn't match the pattern " + pattern.pattern();
		}
	}
	return null;
}

public Object getDsId() {
	if (record != null) {
		return record.getDsId();
	} else {
		// TASK:VISREPACT: uncomment when done
		//System.err.println("Warning: VDBFieldData.getDsId: returning null.");
		return null;
	}
}

}