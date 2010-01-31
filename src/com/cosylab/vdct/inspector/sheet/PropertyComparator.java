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

import java.util.Arrays;
import java.util.Comparator;

import com.cosylab.vdct.db.DBSheetSplitCol;
import com.cosylab.vdct.inspector.InspectableProperty;

/**
 * @author ssah
 *
 */
public class PropertyComparator implements Comparator {

    InspectableProperty[][] properties = null;
    private boolean rows = true;
    private int sortingIndex = 0;
    private int sign = 1;
    
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object arg0, Object arg1) {
		int firstIndex = ((Integer)arg0).intValue();
		int secondIndex = ((Integer)arg1).intValue();
		
		InspectableProperty first = null;  
		InspectableProperty second = null;
		if (rows) {
			first = properties[firstIndex][sortingIndex];  
			second = properties[secondIndex][sortingIndex];  
		} else {
			first = properties[sortingIndex][firstIndex];  
			second = properties[sortingIndex][secondIndex];  
		}
		
	    String firstString = first.getValue();
	    String secondString = second.getValue();

	    // TASK:EMPTYNULL
	    if (firstString == null) {
	    	firstString = "";
	    }
	    if (secondString == null) {
	    	secondString = "";
	    }
	    
	    String firstName = DBSheetSplitCol.removeValueAtEnd(firstString);
	    String secondName = DBSheetSplitCol.removeValueAtEnd(secondString);
	    
	    int firstNumber = DBSheetSplitCol.extractValueAtEnd(firstString);
	    int secondNumber = DBSheetSplitCol.extractValueAtEnd(secondString);

		int nameComp = firstName.compareTo(secondName); 
        if (nameComp != 0) {
        	return sign * nameComp;

        }
        
        // names with no number are displayed before any with numbers
		return sign * (firstNumber - secondNumber); 
	}
	
	public static int[] getOrder(InspectableProperty[][] properties, boolean rows,
			int sortingIndex, boolean ascending) {
		
		int orderLen = rows ? properties.length : properties[0].length; 
		Integer[] order = new Integer[orderLen];
	    for (int i = 0; i < order.length; i++) {
	    	order[i] = new Integer(i); 
		}
		
	    PropertyComparator comparator = new PropertyComparator(properties, rows, sortingIndex, ascending);
	    Arrays.sort(order, comparator);

	    int[] intOrder = new int[orderLen];
	    for (int i = 0; i < intOrder.length; i++) {
	    	intOrder[i] = order[i].intValue(); 
		}
	    return intOrder;
	}

	/**
	 * @param properties
	 * @param rows
	 * @param sortingIndex
	 */
	private PropertyComparator(InspectableProperty[][] properties, boolean rows,
			int sortingIndex, boolean ascending) {
		super();
		this.properties = properties;
		this.rows = rows;
		this.sortingIndex = sortingIndex;
		sign = ascending ? 1 : -1;
	}
}
