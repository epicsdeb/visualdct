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

package com.cosylab.vdct.inspector;

import java.awt.Color;

import javax.swing.JTable;

/**
 * @author ssah
 *
 */
public class InspectorTable extends JTable {

	private PropertyTableModel tableModel = null;
	private HelpDisplayer helpDisplayer = null;
	
	/**
	 * @param tableModel
	 * @param helpDisplayer
	 */
	public InspectorTable(Object dsId, PropertyTableModel tableModel,
			HelpDisplayer helpDisplayer) {
		super();
		this.tableModel = tableModel;
		this.helpDisplayer = helpDisplayer;
		
		
		setName("ScrollPaneTable");
		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		setBackground(new Color(204,204,204));
		setShowVerticalLines(true);
		setGridColor(Color.black);
		setBounds(0, 0, 200, 200);
		setRowHeight(17);
		
		// enable clipboard actions
		new InspectorTableClipboardAdapter(dsId, this);
		setRowSelectionAllowed(true);
		// note: selection is possible only on name column
		setColumnSelectionAllowed(false);
	}

	// Set help messages at row selection. 
	public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
		super.changeSelection(rowIndex, columnIndex, toggle, extend);
		
		if (getEditingColumn() == -1) {
			String helpString = "";
			if (getSelectedRowCount() == 1) {
				int row = getSelectedRow();
				InspectableProperty property = tableModel.getPropertyAt(row, 0);
				helpString = property.getHelp();
			}
			helpDisplayer.setHelpText(helpString);
		}
	}
}
