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

import com.cosylab.vdct.inspector.sheet.SplitPropertyGroup;

/**
 * @author ssah
 *
 */
public class DBSheetSplitCol {
	private String name = null;
	private boolean delimiterType = false;
    private String pattern = null;
    private int parts = -1;
    
    private SplitPropertyGroup[] splitGroups = null;
    
    private static final String delimiterString = "Delimiter";
    private static final String patternString = "Pattern";

	/**
	 * @param delimiterType
	 * @param pattern
	 */
    public DBSheetSplitCol(boolean delimiterType, String pattern) {
    	this(delimiterType, pattern, -1);
	}
    
	/**
	 * @param delimiterType
	 * @param pattern
	 */
    public DBSheetSplitCol(String delimiterType, String pattern) {
    	this(isDelimiterType(delimiterType), pattern, -1);
	}
    
	/**
	 * @param delimiterType
	 * @param pattern
	 * @param parts
	 */
    public DBSheetSplitCol(boolean delimiterType, String pattern, int parts) {
		this(null, delimiterType, pattern, parts);
	}

	/**
	 * @param name
	 * @param delimiterType
	 * @param pattern
	 */
    public DBSheetSplitCol(String name, String delimiterType, String pattern) {
    	this(name, isDelimiterType(delimiterType), pattern, -1);
	}
    
	/**
	 * @param name
	 * @param delimiterType
	 * @param pattern
	 * @param parts
	 */
    public DBSheetSplitCol(String name, boolean delimiterType, String pattern, int parts) {
		super();
		this.name = name;
		this.pattern = pattern;
		this.delimiterType = delimiterType;
		this.parts = parts;
	}
    
    public static DBSheetSplitCol getWhitespaceSplitData() {
    	return new DBSheetSplitCol(true, "\\s+");
    }

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the delimiterType
	 */
	public boolean isDelimiterType() {
		return delimiterType;
	}
	
	/**
	 * @return the pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * @return the parts
	 */
	public int getParts() {
		return parts;
	}

	/**
	 * @param parts the parts to set
	 */
	public void setParts(int parts) {
		this.parts = parts;
	}
	
	public String getDelimiterTypeString() {
		return delimiterType ? delimiterString : patternString;
	}
	
	/**
	 * @return the splitGroups
	 */
	public SplitPropertyGroup[] getSplitGroups() {
		return splitGroups;
	}

	/**
	 * @param splitGroups the splitGroups to set
	 */
	public void setSplitGroups(SplitPropertyGroup[] splitGroups) {
		this.splitGroups = splitGroups;
	}

	private static boolean isDelimiterType(String delimiterTypeString) {
		return delimiterTypeString.equals(delimiterString);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getDelimiterTypeString() + " <" + pattern + ">";
	}
	
	
	/** Returns the number at the end of the given string. If there is no such number, it returns -1.
	 */
    public static int extractValueAtEnd(String string) {
		int value = -1;
    	int j = string.length() - 1;
		while (j >= 0 && Character.isDigit(string.charAt(j))) {
			j--;
		}
		if (j < string.length() - 1) {
			try {
	  		    value = Integer.parseInt(string.substring(j + 1, string.length()));
			} catch (NumberFormatException exception) {
				// nothing
			}
		}
		return value;
	}
	/** Returns the string without the trailing number, or the string itself if there is no number at the end.
	 */
    public static String removeValueAtEnd(String string) {
    	int j = string.length() - 1;
		while (j >= 0 && Character.isDigit(string.charAt(j))) {
			j--;
		}
		return string.substring(0, j + 1);
	}
}