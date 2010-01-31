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

package com.cosylab.vdct.inspector.sheet;

import java.util.Vector;

import com.cosylab.vdct.db.DBSheetRowOrder;
import com.cosylab.vdct.db.DBSheetSplitCol;
import com.cosylab.vdct.db.DBSheetView;
import com.cosylab.vdct.inspector.InspectableProperty;
import com.cosylab.vdct.plugin.debug.PluginDebugManager;
import com.cosylab.vdct.undo.UndoManager;
import com.cosylab.vdct.util.StringUtils;
import com.cosylab.vdct.vdb.CommentProperty;

/**This table model supports splitting of columns.
 *   
 * @author ssah
 */
public class SpreadsheetSplitViewModel extends SpreadsheetViewModel {

    private int modelColumnCount = 0;
	private InspectableProperty[][] model = null;

    private int propertiesToModelColumnIndex[] = null;
    private int modelToPropertiesColumnIndex[] = null;
    private String[] modelColumnNames = null;

    private int sortedSplitIndex = 0;
    private DBSheetSplitCol[] propertiesColumnSplitData = null;
    private Vector recentSplitData = null;

    // The maximum number of recent entries for splitting columns.
    private static final int recentSplitDataMaxCount = 8;

	/**
	 * @param dataType
	 * @param displayData
	 * @param loadedData
	 * @throws IllegalArgumentException
	 */
	public SpreadsheetSplitViewModel(Object dsId, String dataType, Vector displayData,
			Vector loadedData) throws IllegalArgumentException {
		super(dsId, dataType, displayData, loadedData);
		//comparator = new SpreadsheetRowComparator(this);
        recentSplitData = new Vector();
		refreshSplitData();
		refreshModel();
	}
	
	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetViewModel#recallView()
	 */
	public void recallView() {
		super.recallView();
		refreshModel();
		recallSplitData();
		recallViewData();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetViewModel#storeView()
	 */
	public void storeView() {
		super.storeView();
		storeSplitData();
		storeViewData();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetViewModel#setShowAllRows(boolean)
	 */
	public void setShowAllRows(boolean showAllRows) {
		super.setShowAllRows(showAllRows);
		refreshModel();
	}
	
	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetViewModel#setGroupColumnsByGuiGroup(boolean)
	 */
	public void setGroupColumnsByGuiGroup(boolean groupColumnsByGuiGroup) {
		super.setGroupColumnsByGuiGroup(groupColumnsByGuiGroup);
		refreshModel();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetViewModel#setRowsVisibility(int[], boolean)
	 */
	public void setRowsVisibility(int[] rows, boolean visible) {
		super.setRowsVisibility(rows, visible);
		refreshModel();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetViewModel#setColumnsVisibility(int[], boolean)
	 */
	public void setColumnsVisibility(int[] columns, boolean visible) {

		for (int c = 0; c < columns.length; c++) {
			columns[c] = modelToPropertiesColumnIndex[columns[c]];
		}
		super.setColumnsVisibility(columns, visible);
		refreshModel();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetViewModel#setPropertyColumnVisibility(int, boolean)
	 */
	public void setPropertyColumnsVisibility(int[] columns, boolean visible) {
		super.setPropertyColumnsVisibility(columns, visible);
		refreshModel();
	}
	
	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetViewModel#setDefaultColumnVisibility()
	 */
	public void setDefaultColumnVisibility() {
		super.setDefaultColumnVisibility();
		refreshModel();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetViewModel#moveColumn(int, int)
	 */
	public void repositionColumn(int startIndex, int destIndex) {
		super.repositionColumn(modelToPropertiesColumnIndex[startIndex], modelToPropertiesColumnIndex[destIndex]);
		refreshModel();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetViewModel#sortRowsByColumn(int)
	 */
	public void sortRowsByColumn(int column) {

		int visibleIndex = modelToPropertiesColumnIndex[column];
		int newSortedColumn = visibleToBaseColumn(visibleIndex);

		int newSortedSplitIndex = column - propertiesToModelColumnIndex[visibleIndex];

		boolean sameColumn = (sortedColumn == newSortedColumn && sortedSplitIndex == newSortedSplitIndex); 
		sortedOrderAsc = sameColumn ? !sortedOrderAsc : true;
		sortedColumn = newSortedColumn;
		sortedSplitIndex = newSortedSplitIndex;

		int[] order = PropertyComparator.getOrder(model, true, column, sortedOrderAsc);
        setRowOrder(order);
		refreshModel();
	}
	
	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetViewModel#getColumnCount()
	 */
	public int getColumnCount() {
		return modelColumnCount;
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetViewModel#getColumnClass(int)
	 */
	public Class getColumnClass(int column) {
	   return super.getColumnClass(modelToPropertiesColumnIndex[column]);
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetViewModel#getColumnId(int)
	 */
	protected String getColumnId(int column) {
		return super.getColumnId(modelToPropertiesColumnIndex[column]);
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetViewModel#getNamesColumn()
	 */
	protected int getNamesColumn() {
		return propertiesToModelColumnIndex[super.getNamesColumn()];
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetViewModel#getPropertyDisplayTypeAt(int, int)
	 */
	public int getPropertyDisplayTypeAt(int row, int column) {
		return super.getPropertyDisplayTypeAt(row, modelToPropertiesColumnIndex[column]);
	}

	/* Returns the multi-line string associated with the cell at the given position, or null it there is
	 * none. 
	 */
	public String getMultilineString(int row, int column) { 
		// Check if the cell is part of the comment, if so return it.
		if (row >= 0 && column >= 0) {
			InspectableProperty property = model[row][column];
			InspectableProperty baseProperty = property;
			if (baseProperty instanceof SplitPropertyPart) {
			    baseProperty = ((SplitPropertyPart)baseProperty).getOwner().getOwner();
			}
			
			if (baseProperty instanceof CommentProperty) {
				String value = property.getValue();
				return (value == null) ? "" : value;
			}
		}
		return null;
	}

    /* Returns true if this property should not be hidden. First column and name column must not be hidden. 
     */
	public boolean isSolidColumn(int column) {
		return super.isSolidColumn(modelToPropertiesColumnIndex[column]);
	}
	
	public boolean isSplit(int column) {
		return propertiesColumnSplitData[splitToBaseColumn(column)] != null;		
	}

	public int getSplitParts(int baseColumn) {
		DBSheetSplitCol splitData = propertiesColumnSplitData[baseColumn];
		return splitData != null ? splitData.getParts() : 1;		
	}
	
    public int getPropertyColumn(int column) {
    	return super.getPropertyColumn(modelToPropertiesColumnIndex[column]);
    }
	
	public int getModelToPropertiesColumnIndex(int column) {
		return modelToPropertiesColumnIndex[column];
	}
	
	public Vector getRecentSplitData() {
		return recentSplitData;
	}
	
	public String getModelValue(int row, int column) {
		return model[row][column].getValue();
	}

	public int getRecentSplitDataMaxCount() {
		return recentSplitDataMaxCount;
	}
	
	public void splitColumn(DBSheetSplitCol splitData, int column) {
		int propertiesColumn = splitToBaseColumn(column); 
		if (splitData != null) {
			splitData.setName(getPropertiesColumnNames(propertiesColumn));
		}
		propertiesColumnSplitData[propertiesColumn] = splitData;
		refreshModel();
	}

	public void splitColumnByRecentList(int recentIndex, int column) {
		// Add the used item to the top of the list.
		DBSheetSplitCol data = (DBSheetSplitCol)recentSplitData.remove(recentIndex);
		recentSplitData.add(0, data);

		String name = super.getColumnId(modelToPropertiesColumnIndex[column]);
		DBSheetSplitCol split = new DBSheetSplitCol(name, data.getDelimiterTypeString(), data.getPattern());
		splitColumn(split, column);
	}
	
	public void setColumnOrder(String modeName) {
		storeSplitData();
		storeViewData();
		super.setColumnOrder(modeName);

		refreshSplitData();
		refreshModel();

		recallSplitData();
		recallViewData();
	}
	
	/* Returns -1 if the column at the index should not be dragged. 
	 */
	public int validateDraggedColumnIndex(int columnIndex) {
		// The first empty column must not be dragged. 
		return (columnIndex > 0) ? columnIndex : -1;
	}
	
	public Object getValueAt(int rowIndex, int columnIndex) {
		String value = model[rowIndex][columnIndex].getValue();
		// TASK:EMPTYNULL
	    return (value != null) ? value : ""; 
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetViewModel#internalSetValueAt(java.lang.Object, int, int)
	 */
	protected boolean internalSetValueAt(Object value, int row, int column) {
		
		boolean change = false;
		
		InspectableProperty property = model[row][column];

		int propertiesColumn = modelToPropertiesColumnIndex[column];
		
		if (property instanceof SplitPropertyPart) {
			/* If this split property is part of creator property, set the new created property as the owner
			 * of this group.  
			 */
			SplitPropertyGroup propertyGroup = ((SplitPropertyPart)property).getOwner();
			InspectableProperty baseProperty = propertyGroup.getOwner(); 
			
			if (baseProperty instanceof CreatorProperty) {
				// Just call to create a new property, value doesn't matter as it will be overwritten. 
				change |= super.internalSetValueAt(value, row, propertiesColumn);
                InspectableProperty newProperty = ((CreatorProperty)baseProperty).getCreatedProperty();
    			// If a new property has been created, set it.
				if (newProperty != null) { 
					propertyGroup.setOwner(newProperty);
	                property.setValue(value.toString());
	                change = true;
				}
			} else {
                property.setValue(value.toString());
                change = true;
			}
		} else {
			/* If this isn't a split part, just call the base function and update the model data structure if a new
			 * property has been created by creator property.
			 */
			change |= super.internalSetValueAt(value, row, propertiesColumn);
			if (property instanceof CreatorProperty) {
				
                InspectableProperty newProperty = ((CreatorProperty)property).getCreatedProperty();
    			// If a new property has been created, replace it.
				if (newProperty != null) { 
					model[row][column] = newProperty;
				}
			}
		}
		
		/* TASK:NONMODALSPR
		 * On name change, if the whole table is not refreshed, at least splitting has to be recalculated as the
		 * field values that point to the name change.
		 */
		/*
		if (propertiesColumn == getNamesColumn()) {
   			refreshModel();
		}
		*/
		
		return change;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// no editing in debug
		if (PluginDebugManager.isDebugState()) {
			return false;
		}
		return model[rowIndex][columnIndex].isEditable();
	}

	public InspectableProperty getPropertyAt(int row, int column) {
		return model[row][column];
	}
	
	public int getHeaderDisplayType(int column) {
	    return super.getHeaderDisplayType(splitToBaseColumn(column));
	}

	public void extendCounters(int[] rows, int[] columns) {
		
		UndoManager.getInstance(dsId).startMacroAction();
		for (int c = 0; c < columns.length; c++) {
			String firstEntry = model[rows[0]][columns[c]].getValue();
			String secondEntry = model[rows[1]][columns[c]].getValue();
			String baseString = DBSheetSplitCol.removeValueAtEnd(firstEntry);
			int firstValue = DBSheetSplitCol.extractValueAtEnd(firstEntry);
			int secondValue = DBSheetSplitCol.extractValueAtEnd(secondEntry);

			// Assume base at 0 and step 1 if values are absent.
			if (firstValue == -1) {
				firstValue = 0;
			}
			if (secondValue == -1 || secondValue == firstValue) {
				secondValue = firstValue + 1;
			}
			
			int value = firstValue;
			int step = secondValue - firstValue;

			for (int r = 0; r < rows.length; r++) {
				internalSetValueAt(baseString + value, rows[r], columns[c]);
				value += step;
			}
		}
		// Changes can affect whole rows as certain field validity states depend on other fields of the inspectable.
		for (int r = 0; r < rows.length; r++) {
			fireTableRowsUpdated(rows[r], rows[r]);
		}
        UndoManager.getInstance(dsId).stopMacroAction();
	}
	
	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetViewModel#refreshAll()
	 */
	protected void refreshAll() {
		super.refreshAll();
		refreshSplitData();
		refreshModel();
	}
	
	protected String getColumnHeaderValue(int column) {
		return modelColumnNames[column];
	}

	protected final int splitToBaseColumn(int column) {
		return super.visibleToBaseColumn(modelToPropertiesColumnIndex[column]);
	}

	protected final int baseToSplitColumn(int column) {
		int visibleIndex = super.baseToVisibleColumn(column);
		return visibleIndex >= 0 ? propertiesToModelColumnIndex[visibleIndex] : -1;
	}
	
	private void refreshModel() {

		int viewColumnCount = super.getColumnCount();
		int viewRowCount = super.getRowCount();
		updateSplitData();
        
		model = new InspectableProperty[viewRowCount][];
		for (int i = 0; i < viewRowCount; i++) {
			model[i] = new InspectableProperty[modelColumnCount];
		}
		
		// Set the model column names and model data according to split columns.
		modelColumnNames = new String[modelColumnCount];
		for (int j = 0; j < viewColumnCount; j++) {

		    String name = super.getColumnId(j);
			DBSheetSplitCol split = propertiesColumnSplitData[super.visibleToBaseColumn(j)]; 
			int modelJ = propertiesToModelColumnIndex[j];

			if (split != null) {

	    		String colName = null;
		    	int parts = split.getParts();
				for (int p = 0; p < parts; p++) {
					colName = "";
					if (p == 0) {
						colName += "<";
					}
					colName += name + "[" + (p + 1) + "]";
					if (p == parts - 1) {
						colName += ">";
					}
			    	modelColumnNames[modelJ + p] = colName;
				}
				
				SplitPropertyGroup[] splitGroups = split.getSplitGroups();
				for (int i = 0; i < viewRowCount; i++) {
					//SplitPropertyGroup group = new SplitPropertyGroup(super.getPropertyAt(i, j), split);
					SplitPropertyGroup group = splitGroups[visibleToBaseRow(i)];
					
					for (int p = 0; p < parts; p++) {
						model[i][modelJ + p] = group.getPart(p);
					}
				}
			} else {
		    	modelColumnNames[modelJ] = name;
				for (int i = 0; i < viewRowCount; i++) {
					model[i][modelJ] = super.getPropertyAt(i, j);
				}
            }
		}
	}

	private void updateSplitData() {

		int propertiesRowCount = getPropertiesRowCount();
		int propertiesColumnCount = getPropertiesColumnCount();
		
		for (int j = 0; j < propertiesColumnCount; j++) {
			DBSheetSplitCol split = propertiesColumnSplitData[j];
			if (split != null) {
				
                // Check if current data is out of date.
				boolean refresh = false;
				SplitPropertyGroup[] splitGroups = split.getSplitGroups();
				if (splitGroups != null && splitGroups.length == propertiesRowCount) {
					for (int i = 0; i < propertiesRowCount; i++) {
						if (splitGroups[i] == null || !StringUtils.emptyEquals(splitGroups[i].getValue(), getPropertyValue(i, j))) {
							refresh = true;
							break;
						}
					}
				} else {
					refresh = true;
				}
				
				if (refresh) {
					splitGroups = new SplitPropertyGroup[propertiesRowCount];
					split.setSplitGroups(splitGroups);

					/* Calculate the number of required columns to which the current must be split into.
					 */
					int maxParts = 1;
					int parts = 0;
					for (int i = 0; i < propertiesRowCount; i++) {
						parts = SplitPropertyGroup.getPartsCount(getPropertyValue(i, j), split);
						maxParts = Math.max(maxParts, parts);
					}
					split.setParts(maxParts);
					
					for (int i = 0; i < propertiesRowCount; i++) {
					    splitGroups[i] = new SplitPropertyGroup(getProperty(i, j), split);
					}
				}
			}
		}

		int viewColumnCount = super.getColumnCount();
		
		// Create split translation tables.
		DBSheetSplitCol splitData = null;
		modelColumnCount = 0;
		for (int j = 0; j < viewColumnCount; j++) {
			splitData = propertiesColumnSplitData[super.visibleToBaseColumn(j)];
			modelColumnCount += (splitData != null) ? splitData.getParts() : 1;
		}
		
		propertiesToModelColumnIndex = new int[viewColumnCount];
		modelToPropertiesColumnIndex = new int[modelColumnCount];
		
		int col = 0;
		for (int j = 0; j < viewColumnCount; j++) {
		    propertiesToModelColumnIndex[j] = col;
			splitData = propertiesColumnSplitData[super.visibleToBaseColumn(j)];
		    int parts = (splitData != null) ? splitData.getParts() : 1;
		    for (int p = 0; p < parts; p++) {
		    	modelToPropertiesColumnIndex[col] = j;
		    	col++;
		    }
		}
	}

	private void recallSplitData() {
		DBSheetView record = getViewRecord();

		// Set the split data and refresh the model.
    	for (int j = 0; j < getPropertiesColumnCount(); j++) {
    		propertiesColumnSplitData[j] = null;
    	}
    	DBSheetSplitCol[] splitColumns = record.getSplitColumns();
    	if (splitColumns != null) {
    		for (int j = 0; j < splitColumns.length; j++) {
    			int index = getPropertiesColumnIndex(splitColumns[j].getName());
    			if (index >= 0) {
    				propertiesColumnSplitData[index] = splitColumns[j];
    			}
    		}
    	}
		refreshModel();
    	
    	DBSheetSplitCol[] recentSplits = record.getRecentSplits();
    	if (recentSplits != null) {
    		recentSplitData.clear();
    		for (int j = 0; j < recentSplits.length; j++) {
        		recentSplitData.add(recentSplits[j]);
    		}
    	}
	}
	
	private void recallViewData() {
		DBSheetView record = getViewRecord();

		DBSheetRowOrder rowOrder = record.getRowOrder();
        if (rowOrder != null) {
        	String orderedColumnName = rowOrder.getColumnName();
        	int index = getPropertiesColumnIndex(orderedColumnName);
        	if (index >= 0) {
        		int visibleIndex = baseToVisibleColumn(sortedColumn);
        		if (visibleIndex >= 0) {
        			sortedSplitIndex = rowOrder.getColumnSplitIndex();
        			int modelIndex = propertiesToModelColumnIndex[visibleIndex] + sortedSplitIndex;
        			int[] order = PropertyComparator.getOrder(model, true, modelIndex, sortedOrderAsc);
        			setRowOrder(order);
        			refreshModel();
        		}
        	}
        }
	}

	protected void storeSplitData() {
		DBSheetView record = getViewRecord();
		
		boolean defaultNoColumnSplit = true;
		int propertiesColumnCount = getPropertiesColumnCount();
        for (int j = 0; j < propertiesColumnCount; j++) {
        	if (propertiesColumnSplitData[j] != null) {
        		defaultNoColumnSplit = false;
        		break;
        	}
        }
        
        if (!defaultNoColumnSplit) {
            DBSheetSplitCol[] splitColumns = null;
        	Vector splitColumnVector = new Vector();
        	for (int j = 0; j < propertiesColumnCount; j++) {
        		if (propertiesColumnSplitData[j] != null) {
            		splitColumnVector.add(propertiesColumnSplitData[j]);
        		}
        	}
        	splitColumns = new DBSheetSplitCol[splitColumnVector.size()]; 
            splitColumnVector.copyInto(splitColumns);
    		record.setSplitColumns(splitColumns);
        } else {
    		record.setSplitColumns(null);
        }

		boolean defaultNoRecentSplits = recentSplitData.isEmpty();
        if (!defaultNoRecentSplits) {
        	DBSheetSplitCol[] recentSplits = new DBSheetSplitCol[recentSplitData.size()]; 
        	recentSplitData.copyInto(recentSplits);
    		record.setRecentSplits(recentSplits);
        } else {
    		record.setRecentSplits(null);
        }
	}
	
	protected void storeViewData() {
		DBSheetView record = getViewRecord();

        boolean defaultNoSortOrder = (sortedColumn == -1);
        if (!defaultNoSortOrder) {
        	String name = getPropertiesColumnNames(sortedColumn);
        	record.setRowOrder(new DBSheetRowOrder(name, sortedSplitIndex, sortedOrderAsc));
        } else {
        	record.setRowOrder(null);
        }
	}
	
	private void refreshSplitData() {
		
		int propertiesColumnCount = getPropertiesColumnCount();
		if (propertiesColumnSplitData == null || propertiesColumnSplitData.length != propertiesColumnCount) {
			// Prepare space for properties column data.
			propertiesColumnSplitData = new DBSheetSplitCol[propertiesColumnCount];
			for (int j = 0; j < propertiesColumnCount; j++) {
			    propertiesColumnSplitData[j] = null;
			}
		}
	}
}
