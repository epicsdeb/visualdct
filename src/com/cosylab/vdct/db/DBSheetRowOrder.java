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

package com.cosylab.vdct.db;

/**
 * @author ssah
 *
 */
public class DBSheetRowOrder {

	private String columnName = null;
	private int columnSplitIndex = 0;
	private boolean ascending = true;
	
    private static final String ascendingString = "Ascending";
    private static final String descendingString = "Descending";

    /**
	 * @param columnName
	 * @param columnSplitIndex
	 * @param ascending
	 */
	public DBSheetRowOrder(String columnName, int columnSplitIndex, String ascending) {
		this(columnName, columnSplitIndex, isAscending(ascending));
	}
    
    /**
	 * @param columnName
	 * @param columnSplitIndex
	 * @param ascending
	 */
	public DBSheetRowOrder(String columnName, int columnSplitIndex, boolean ascending) {
		super();
		this.columnName = columnName;
		this.columnSplitIndex = columnSplitIndex;
		this.ascending = ascending;
	}

	/**
	 * @return the orderedColumnName
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * @return the orderedColumnSplitIndex
	 */
	public int getColumnSplitIndex() {
		return columnSplitIndex;
	}

	/**
	 * @return the orderAscending
	 */
	public boolean isAscending() {
		return ascending;
	}

	/**
	 * @return the orderAscending
	 */
	public String getAscendingString() {
		return ascending ? ascendingString : descendingString;
	}

	/**
	 * @param ascending the orderAscending to set
	 */
	public static boolean isAscending(String orderAscendingString) {
		return orderAscendingString.equals(ascendingString);
	}
}
