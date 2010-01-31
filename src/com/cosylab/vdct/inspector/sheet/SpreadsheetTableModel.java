package com.cosylab.vdct.inspector.sheet;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import com.cosylab.vdct.db.DBSheetData;
import com.cosylab.vdct.db.DBSheetView;
import com.cosylab.vdct.graphics.DsManager;
import com.cosylab.vdct.graphics.objects.Record;
import com.cosylab.vdct.graphics.objects.Template;
import com.cosylab.vdct.graphics.objects.VisibleObject;
import com.cosylab.vdct.inspector.Inspectable;
import com.cosylab.vdct.inspector.InspectableProperty;
import com.cosylab.vdct.inspector.PropertyTableModel;
import com.cosylab.vdct.undo.DeleteAction;
import com.cosylab.vdct.undo.UndoManager;
import com.cosylab.vdct.util.StringUtils;
import com.cosylab.vdct.vdb.CommentProperty;
import com.cosylab.vdct.vdb.VisibilityProperty;

/**This table model supports ordering the inspectable properties into columns based on their name.
 * 
 * @author ssah
 */
public class SpreadsheetTableModel extends AbstractTableModel implements PropertyTableModel {

	protected Object dsId = null;

	protected String dataType = null;
	protected String typeSign = null;
	
	private Inspectable[] inspectables = null;
	private Set loadedInspectablesNames = null;
	
	private int columnOrder = 0;
	private int propertiesRowCount = 0;
	private int propertiesColumnCount = 0;
	
	private InspectableProperty[][] properties = null;
	private HashMap nameToPropertiesColumnIndex = null;
	private HashMap nameToPropertiesRowIndex = null;
	private String[] propertiesColumnNames = null;

    // The name of the column that contains the row names.  
    protected static final String propertiesNamesColumn = "Name";
    
    // The name of the column that contains the comments.  
    protected static final String propertiesCommentsColumn = "Comment";
    
	private static ArrayList defaultModes = null; 
	
	private static final String recordType = "R"; 
	private static final String templateType = "T"; 
	private static final String unknownType = "U";
    
	public SpreadsheetTableModel(Object dsId, String dataType, Vector displayData, Vector loadedData)
			throws IllegalArgumentException {
			
  		if (displayData == null || displayData.size() == 0) {
  			throw new IllegalArgumentException("inspectables must not be empty or null");
  		}
		  		
		this.dsId = dsId;
  		this.dataType = dataType;
		inspectables = new Inspectable[displayData.size()];
		displayData.copyInto(inspectables); 
        propertiesRowCount = inspectables.length;

		typeSign = getTypeString(inspectables[0]);
        
    	// Store all names of the loaded objects. 
        loadedInspectablesNames = new HashSet();
        Iterator iterator = loadedData.iterator();
        while (iterator.hasNext()) {
        	loadedInspectablesNames.add(((Inspectable)iterator.next()).getName());
        }
        
        refreshProperties();
	}
	
	public void refresh() {
		storeView();
		refreshAll();
		recallView();
	}
	
	public void recallView() {
		recallTableModelData();
	}

	public void storeView() {
		storeTableModelData();
	}

    public int getColumnCount() {
		return propertiesColumnCount;
	}

	public int getRowCount() {
		return propertiesRowCount;
	}
	
	public Object getValueAt(int rowIndex, int columnIndex) {
	    String value = properties[rowIndex][columnIndex].getValue();
		// TASK:EMPTYNULL
	    return (value != null) ? value : ""; 
	}

	public InspectableProperty getPropertyAt(int row, int column) {
		return properties[row][column];
	}

	public void setValueAt(Object aValue, int row, int column) {
		String[] values = StringUtils.expandMacros(aValue.toString());
		if (values == null) {
			return;
		}
		UndoManager.getInstance(dsId).startMacroAction();
		int curRow = row;
		while (curRow < getRowCount() && curRow - row < values.length) {
			internalSetValueAtAndUpdate(values[curRow - row], curRow, column);
			curRow++;
		}
        UndoManager.getInstance(dsId).stopMacroAction();
	}

	protected void internalSetValueAtAndUpdate(Object aValue, int row, int column) {
		if (internalSetValueAt(aValue, row, column)) {
			// Update the whole row as validity of this inspectable's fields can change on property value change. 
			fireTableRowsUpdated(row, row);
		}
	}
	
	/* Returns true if there was a change in the property.
	 */
	protected boolean internalSetValueAt(Object aValue, int row, int column) {
		
		InspectableProperty property = properties[row][column];

		int propertiesNamesColumnIndex = nameToPropertiesColumnIndex(propertiesNamesColumn);
		// If a name change, the whole table is going to be refreshed.
		if (column == propertiesNamesColumnIndex) {
			properties[row][column].setValue(aValue.toString());
			return false;
			// TASK:NONMODALSPR
			//nameToPropertiesRowIndex.remove(property.getValue());
		}
		/* Check if the property is a creator property that creates a new property on the inspectable object.
		 */
		if (property instanceof CreatorProperty) {
			CreatorProperty creatorProperty = (CreatorProperty)property; 

			creatorProperty.setName(propertiesColumnNames[column]);
			creatorProperty.setValue(aValue.toString());
			
			property = creatorProperty.getCreatedProperty();
			// If a new property has been created, replace it.
			if (property != null) { 
			    properties[row][column] = property;
			}
		} else {
			properties[row][column].setValue(aValue.toString());
		}
		
		// TASK:NONMODALSPR
		/*
		if (column == propertiesNamesColumnIndex) {
   			nameToPropertiesRowIndex.put(properties[row][column].getValue(), new Integer(row));
		}
		*/
		return true;
	}
	
	public int getHeaderDisplayType(int column) {
	    return (column > 0) ? PropertyTableModel.HEADERDISP_TEXT : PropertyTableModel.HEADERDISP_NONE;
	}
	
	public int getPropertyDisplayTypeAt(int row, int column) {
	    return (column > 0) ? PropertyTableModel.DISP_VALUE : PropertyTableModel.DISP_VISIBILITY;
	}
	
	protected String getColumnId(int column) {
		return propertiesColumnNames[column]; 
	}
	
	public void deleteRows(int[] rows) {
		try	{
			UndoManager.getInstance(dsId).startMacroAction();

			Inspectable inspectable = null;
			for (int r = 0; r < rows.length; r++) {
				inspectable = inspectables[rows[r]];
				if (inspectable instanceof VisibleObject) {
					VisibleObject visible = (VisibleObject)inspectable;
					visible.destroy();
					UndoManager.getInstance(dsId).addAction(new DeleteAction(visible));
				}
			}
		} catch (Exception exception) {
			// Nothing.
		} finally {
			UndoManager.getInstance(dsId).stopMacroAction();
		}
		DsManager.getDrawingSurface(dsId).repaint();
	}
	
	/* Returns the multi-line string associated with the cell at the given position, or null it there is
	 * none. 
	 */
	public String getMultilineString(int row, int column) { 
		// Check if the cell is part of the comment, if so return it.
		if (row >= 0 && column >= 0) {
			InspectableProperty property = properties[row][column];
			if (property instanceof CommentProperty) {
				String value = property.getValue();
				return (value == null) ? "" : value;
			}
		}
		return null;
	}
	
	/* Refresh all data on the model. 
	 */
	protected void refreshAll() {
        refreshProperties();
	}
	
	protected int getNamesColumn() {
	    return nameToPropertiesColumnIndex(propertiesNamesColumn);
	}
	
	protected Set getLoadedInspectablesNames() {
	    return loadedInspectablesNames;
	}
	
	/* Returns -1 if none for this modeName.
	 */
	protected int getColumnOrderIndex(String name) {
    	ArrayList list = getColumnOrderNames();
    	for (int m = 0; m < list.size(); m++) {
    		if (name.equals(list.get(m).toString())) {
    			return m;
    		}
    	}
    	return -1;
	}

	protected void setColumnOrderIndex(int columnOrder) {
   		this.columnOrder = columnOrder;
   		refreshProperties();
	}

	public Inspectable getLastInspectable() {
		return inspectables[propertiesRowCount - 1];
	}

	public final InspectableProperty getProperty(int row, int column) {
		return properties[row][column];
	}
	
	public final String getPropertyValue(int row, int column) {
		return properties[row][column].getValue();
	}

	public final int getPropertiesRowCount() {
		return propertiesRowCount;
	}

	public final int getPropertiesColumnCount() {
		return propertiesColumnCount;
	}
	
	public String getPropertiesColumnNames(int column) {
		return propertiesColumnNames[column];
	}
	
	public int getPropertiesColumnIndex(String name) {
		return nameToPropertiesColumnIndex(name);
	}

	protected String getPropertiesRowNames(int row) {
		return properties[row][nameToPropertiesColumnIndex(propertiesNamesColumn)].getValue();
	}
	
	protected int getPropertiesRowIndex(String name) {
		return nameToPropertiesRowIndex(name);
	}
    
	public Class getColumnClass(int column) {
		return String.class;
	}
	
	public void setColumnOrder(String modeName) {
		int newMode = getColumnOrderIndex(modeName);
		if (newMode != -1) {
			setColumnOrderIndex(newMode);
		}
	}

	protected ArrayList getColumnOrderNames() {

		ArrayList list = inspectables[0].getModeNames();
		if (list == null) {
			if (defaultModes == null) {
				defaultModes = new ArrayList();
				defaultModes.add("Default view");
			}
			list = defaultModes;
		}
		return list;
	}
	
	protected final InspectableProperty[][] getProperties() {
		return properties;
	}
	
	private String getTypeString(Inspectable inspectable) {
		if (inspectable instanceof Record) {
			return recordType;
		} else if (inspectable instanceof Template) {
			return templateType;
		}
		return unknownType;
	}
	
    /** Returns the index of the named data row or -1 if there is none.
     */
    private int nameToPropertiesRowIndex(String name) {
    	Integer index = (Integer)nameToPropertiesRowIndex.get(name);
        return (index != null) ? index.intValue() : -1;
    }
    /** Returns the index of the named data column or -1 if there is none.
     */
    private int nameToPropertiesColumnIndex(String name) {
    	Integer index = (Integer)nameToPropertiesColumnIndex.get(name);
        return (index != null) ? index.intValue() : -1;
    }
    
	private void refreshProperties() {

        // Get the properties of all inspectable objects and store the first creator property of each.
		InspectableProperty[][] inspectableProperties = new InspectableProperty[propertiesRowCount][];
        InspectableProperty[] creatorProperties = new InspectableProperty[propertiesRowCount];
        for (int i = 0; i < propertiesRowCount; i++) {
        	inspectableProperties[i] = inspectables[i].getProperties(columnOrder, true);
			for (int j = 0; j < inspectableProperties[i].length; j++) {
				InspectableProperty property = inspectableProperties[i][j];
				if (property instanceof CreatorProperty && creatorProperties[i] == null) {
					creatorProperties[i] = property;
					break;
				}
			}
        }
			
		// Count the number of columns required by matching the properties by their names.
  		nameToPropertiesColumnIndex = new HashMap();
        Vector propStrings = new Vector();
        // Add the starting fixed columns.
        propStrings.add("");
        propStrings.add(propertiesNamesColumn);
		nameToPropertiesColumnIndex.put(propertiesNamesColumn, new Integer(1));
        propStrings.add(propertiesCommentsColumn);
		nameToPropertiesColumnIndex.put(propertiesCommentsColumn, new Integer(2));
        propertiesColumnCount = 3;
        for (int i = 0; i < propertiesRowCount; i++) {
			for (int j = 0; j < inspectableProperties[i].length; j++) {
				InspectableProperty property = inspectableProperties[i][j];
				if (!(property instanceof CreatorProperty)) {
					String name = property.getName();
					if (nameToPropertiesColumnIndex.get(name) == null) {
						nameToPropertiesColumnIndex.put(name, Integer.valueOf(String.valueOf(propertiesColumnCount)));
						propStrings.add(name);
						propertiesColumnCount++;
					}
				}
			}
		}
		propertiesColumnNames = new String[propertiesColumnCount];
		propStrings.copyInto(propertiesColumnNames);

		// Copy the properties into a rectangle shaped table. Empty spaces are filled with creator properties.
		properties = new InspectableProperty[propertiesRowCount][];
		for (int i = 0; i < propertiesRowCount; i++) {
			properties[i] = new InspectableProperty[propertiesColumnCount];

			VisibilityProperty visibilityProperty = new VisibilityProperty(true);
			for (int j = 0; j < propertiesColumnCount; j++) {
				if (j == 0) {
				    properties[i][j] = visibilityProperty;
				} else if (propertiesColumnNames[j].equals(propertiesCommentsColumn)) {
				    properties[i][j] = inspectables[i].getCommentProperty();
				} else {
				    properties[i][j] = creatorProperties[i];
				}
			}
			
			for (int j = 0; j < inspectableProperties[i].length; j++) {
				InspectableProperty property = inspectableProperties[i][j];
				if (!(property instanceof CreatorProperty)) {
					int col = ((Integer)nameToPropertiesColumnIndex.get(property.getName())).intValue();
					properties[i][col] = property; 
				}
			}
		}

  		// Create a name to row translation table.
		nameToPropertiesRowIndex = new HashMap();
		int nameColumn = nameToPropertiesColumnIndex(propertiesNamesColumn);
		if (nameColumn >= 0) {
			for (int i = 0; i < propertiesRowCount; i++) {
				nameToPropertiesRowIndex.put(properties[i][nameColumn].getValue(), new Integer(i));
			}
		}
	}

	protected DBSheetView getViewRecord() {
		String typeSign = getTypeString(inspectables[0]);
		DBSheetView record = DBSheetData.getInstance(dsId).get(typeSign + dataType);
		if (record == null) {
			record = new DBSheetView(typeSign, dataType);
		 	DBSheetData.getInstance(dsId).add(record);
		}
		return record;
	}
	
	private void recallTableModelData() {
		DBSheetView record = getViewRecord();

        int newColumOrder = -1;
        String columnOrderName = record.getModeName();
        if (columnOrderName != null) {
            newColumOrder = getColumnOrderIndex(columnOrderName);
        }        
        if (newColumOrder != -1 && newColumOrder != columnOrder) {
			setColumnOrderIndex(newColumOrder);
		}
	}

	private void storeTableModelData() {
		DBSheetView record = getViewRecord();
		
		boolean defaultMode = columnOrder == 0;
        if (!defaultMode) {
        	record.setModeName(getColumnOrderNames().get(columnOrder).toString());
        } else {
        	record.setModeName(null);
        }
	}
}
