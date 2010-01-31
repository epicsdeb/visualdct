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

import java.awt.Color;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.cosylab.vdct.db.DBSheetColWidth;
import com.cosylab.vdct.db.DBSheetColumn;
import com.cosylab.vdct.db.DBSheetSplitCol;
import com.cosylab.vdct.db.DBSheetView;

/**This table model manages the column model and persistence of column widths and background color.   
 * 
 * @author ssah
 */
public class SpreadsheetColumnViewModel extends SpreadsheetSplitViewModel implements TableColumnModel {
	
	TableColumnModel columnModel = null;
	TableCellRenderer renderer = null;

    private Color defaultBackground = null;
    private Color background = null;
	
	/**
	 * @param dataType
	 * @param displayData
	 * @param loadedData
	 * @throws IllegalArgumentException
	 */
	public SpreadsheetColumnViewModel(Object dsId, String dataType, Vector displayData,
			Vector loadedData) throws IllegalArgumentException {
		super(dsId, dataType, displayData, loadedData);
		columnModel = new DefaultTableColumnModel();
		columnModel.setColumnSelectionAllowed(true);
		refreshColumns();
	}
	
	/**
	 * @param renderer the renderer to set
	 */
	public void setRenderer(TableCellRenderer renderer) {
		this.renderer = renderer;
	}

	public void onColumnWidthChange() {
		storeView();
	}
	
	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetSplitViewModel#recallView()
	 */
	public void recallView() {
		super.recallView();
		refreshColumns();
		recallTableColor();
		recallColumnWidths();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetSplitViewModel#storeView()
	 */
	public void storeView() {
		super.storeView();
		storeTableColor();
		storeColumnWidths();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetSplitViewModel#refreshAll()
	 */
	protected void refreshAll() {
		super.refreshAll();
		refreshColumns();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetSplitViewModel#repositionColumn(int, int)
	 */
	public void repositionColumn(int startIndex, int destIndex) {
		super.repositionColumn(startIndex, destIndex);
		refreshColumns();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetSplitViewModel#setColumnOrder(java.lang.String)
	 */
	public void setColumnOrder(String modeName) {
		super.setColumnOrder(modeName);
		refreshColumns();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetSplitViewModel#setColumnsVisibility(int[], boolean)
	 */
	public void setColumnsVisibility(int[] columns, boolean visible) {
		super.setColumnsVisibility(columns, visible);
		refreshColumns();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetSplitViewModel#setPropertyColumnsVisibility(int[], boolean)
	 */
	public void setPropertyColumnsVisibility(int[] columns, boolean visible) {
		super.setPropertyColumnsVisibility(columns, visible);
		refreshColumns();
	}
	
	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetSplitViewModel#setDefaultColumnVisibility()
	 */
	public void setDefaultColumnVisibility() {
		super.setDefaultColumnVisibility();
		refreshColumns();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetSplitViewModel#setRowsVisibility(int[], boolean)
	 */
	public void setRowsVisibility(int[] rows, boolean visible) {
		super.setRowsVisibility(rows, visible);
		refreshColumns();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetSplitViewModel#setShowAllRows(boolean)
	 */
	public void setShowAllRows(boolean showAllRows) {
		super.setShowAllRows(showAllRows);
		refreshColumns();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetSplitViewModel#setGroupColumnsByGuiGroup(boolean)
	 */
	public void setGroupColumnsByGuiGroup(boolean groupColumnsByGuiGroup) {
		super.setGroupColumnsByGuiGroup(groupColumnsByGuiGroup);
		refreshColumns();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetSplitViewModel#sortRowsByColumn(int)
	 */
	public void sortRowsByColumn(int column) {
		super.sortRowsByColumn(column);
		refreshColumns();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetSplitViewModel#splitColumn(com.cosylab.vdct.inspector.SplitData, int)
	 */
	public void splitColumn(DBSheetSplitCol splitData, int column) {
		super.splitColumn(splitData, column);
		refreshColumns();
	}

	public Color getBackground() {
		return background;
	}
	
	public void setBackground(Color background) {
		this.background = background;
	}
	
	public Color getDefaultBackground() {
		return defaultBackground;
	}

	public void setDefaultBackground(Color defaultBackground) {
		this.defaultBackground = defaultBackground;
	}
	
	private void refreshColumns() {
		
		while (getColumnCount() > 0) {
        	removeColumn(columnModel.getColumn(0));
        }
		for (int j = 0; j < super.getColumnCount(); j++) {
		    columnModel.addColumn(createColumn(j, true));
		}
		recallColumnWidths();
		return;
		/* TASK:COLREUSE: Sample code for reusing of existing columns.
		 * First do TASK:UNEDITCOM.
		 */
        /*
        int pos = 0;
		for (pos = 0; pos < super.getColumnCount(); pos++) {
			if (pos < getColumnCount()) {
				// Recycle existing columns.
				TableColumn column = getColumn(pos);
				if (column.getModelIndex() != pos) {
					column.setModelIndex(pos);
				}
				String headerValue = getColumnHeaderValue(pos);
				if (!column.getHeaderValue().equals(headerValue)) {
					column.setHeaderValue(headerValue);
				}
			} else {
			    addColumn(createColumn(pos, true));
			}
		}
		// Delete the remaining unneeded columns.  
		if (pos < getColumnCount()) {
			TableColumn column = getColumn(pos);
		    removeColumn(column);
		}
		*/
	}
	
	private TableColumn createColumn(int colIndex, boolean defaultWidth) {
		TableColumn column = new SpreadsheetColumn(defaultWidth);
		column.setModelIndex(colIndex);
		column.setHeaderValue(getColumnHeaderValue(colIndex));
		column.setHeaderRenderer(renderer);
		
		// TASK:UNEDITCOM
		String name = getColumnId(colIndex);
		if (name.equals(propertiesCommentsColumn)) {
			column.setCellEditor(new DefaultCellEditor(new JTextField()){
				public boolean isCellEditable(EventObject anEvent) {
					return false;
				}
			});
		}
		
		return column;
	}

	private void recallTableColor() {
		DBSheetView record = getViewRecord();

		Integer recBackgroundColor = record.getBackgroundColor(); 
		if (recBackgroundColor != null) {
			background = new Color(recBackgroundColor.intValue());
		}
	}
	
	private void storeTableColor() {
		DBSheetView record = getViewRecord();

		boolean defaultBackgroundColour = background.equals(defaultBackground);
        if (!defaultBackgroundColour) {
        	record.setBackgroundColor(new Integer(background.getRGB()));
        } else {
        	record.setBackgroundColor(null);
        }
	}
	
	private void recallColumnWidths() {
		DBSheetView record = getViewRecord();
		
		Iterator columns = record.getColumns().values().iterator();
		while (columns.hasNext()) {
			DBSheetColumn columnData = (DBSheetColumn)columns.next();

			int baseIndex = getPropertiesColumnIndex(columnData.getName());
			if (baseIndex == -1) {
				continue;
			}
			int index = baseToSplitColumn(baseIndex);
			if (index == -1) {
				continue;
			}
			boolean split = isSplit(index);
			int splitParts = getSplitParts(baseIndex);
			
			DBSheetColWidth[] data = columnData.getSplitIndices();
			for (int i = 0; i < data.length; i++) {
				int splitIndex = data[i].getSplitIndex();
				if (split == (splitIndex != -1) && splitIndex < splitParts) {
					SpreadsheetColumn column = (SpreadsheetColumn)getColumn(split ? index + splitIndex : index);
					column.setDefaultWidth(false);
					column.setPreferredWidth(data[i].getWidth());
				}
			}
		}
	}
	
	private void storeColumnWidths() {
		DBSheetView record = getViewRecord();
		
		Map columnsMap = record.getColumns();
		Iterator columns = columnsMap.values().iterator();
		while (columns.hasNext()) {
			DBSheetColumn columnData = (DBSheetColumn)columns.next();

			int baseIndex = getPropertiesColumnIndex(columnData.getName());
			if (baseIndex == -1) {
				continue;
			}
			int splitIndex = baseToSplitColumn(baseIndex);
			if (splitIndex == -1 || splitIndex >= getColumnCount()) {
				continue;
			}
			boolean split = isSplit(splitIndex);
			int splitParts = getSplitParts(baseIndex);
			
			Vector widthsVector = new Vector();
			for (int i = 0; i < splitParts; i++) {
				SpreadsheetColumn column = (SpreadsheetColumn)getColumn(splitIndex + i);
				if (!column.isDefaultWidth()) {
					widthsVector.add(new DBSheetColWidth(split ? i : -1, column.getPreferredWidth()));
				}
			}
			DBSheetColWidth[] widths = new DBSheetColWidth[widthsVector.size()];
			widthsVector.copyInto(widths);
			columnData.setSplitIndices(widths);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.SpreadsheetViewModel#isColumsDefault()
	 */
	protected boolean isColumsDefault() {
		// Default when all widths default.
		int columnCount = getColumnCount();
		for (int c = 0; c < columnCount; c++) {
			if (!((SpreadsheetColumn)getColumn(c)).isDefaultWidth()) {
				return false;
			}
		}
		return super.isColumsDefault();
	}

	public void addColumn(TableColumn column) {
		columnModel.addColumn(column);
	}

	public void addColumnModelListener(TableColumnModelListener x) {
		columnModel.addColumnModelListener(x);
	}

	public TableColumn getColumn(int columnIndex) {
		return columnModel.getColumn(columnIndex);
	}

	public int getColumnCount() {
		return columnModel.getColumnCount();
	}

	public int getColumnIndex(Object columnIdentifier) {
		return columnModel.getColumnIndex(columnIdentifier);
	}

	public int getColumnIndexAtX(int position) {
		return columnModel.getColumnIndexAtX(position);
	}

	public int getColumnMargin() {
		return columnModel.getColumnMargin();
	}

	public Enumeration getColumns() {
		return columnModel.getColumns();
	}

	public boolean getColumnSelectionAllowed() {
		return columnModel.getColumnSelectionAllowed();
	}

	public int getSelectedColumnCount() {
		return columnModel.getSelectedColumnCount();
	}

	public int[] getSelectedColumns() {
		return columnModel.getSelectedColumns();
	}

	public ListSelectionModel getSelectionModel() {
		return columnModel.getSelectionModel();
	}

	public int getTotalColumnWidth() {
		return columnModel.getTotalColumnWidth();
	}

	public void moveColumn(int columnIndex, int newIndex) {
		columnModel.moveColumn(columnIndex, newIndex);
	}

	public void removeColumn(TableColumn column) {
		columnModel.removeColumn(column);
	}

	public void removeColumnModelListener(TableColumnModelListener x) {
		columnModel.removeColumnModelListener(x);
	}

	public void setColumnMargin(int newMargin) {
		columnModel.setColumnMargin(newMargin);
	}

	public void setColumnSelectionAllowed(boolean flag) {
		columnModel.setColumnSelectionAllowed(flag);
	}

	public void setSelectionModel(ListSelectionModel newModel) {
		columnModel.setSelectionModel(newModel);
	}
}
