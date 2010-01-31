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

package com.cosylab.vdct.inspector.sheet;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.cosylab.vdct.db.DBSheetSplitCol;
import com.cosylab.vdct.inspector.InspectableProperty;

/**
 * @author ssah
 *
 */
public class SplitPropertyGroup {

	private InspectableProperty owner = null;
	private SplitPropertyPart[] parts = null;
	private String trail = null;
	
	private String value = null;
	
	private int patternParts = 0;
	private boolean patternMatch = true;
	private boolean nestedGroups = false;
	private boolean errorInPattern = false;
	private String errorDesc = null;

	private static final String defaultDelimiter = " ";
	
	/**
	 * @param owner
	 * @param splitPattern
	 */
	public SplitPropertyGroup(InspectableProperty owner, DBSheetSplitCol splitData) {
		this(owner.getValue(), splitData);
		this.owner = owner;
	}
		
	/**
	 * @param value
	 * @param splitPattern
	 */
	public SplitPropertyGroup(String value, DBSheetSplitCol splitData) {
		super();
		
		boolean delimiterType = splitData.isDelimiterType();
		String pattern = splitData.getPattern();
		int partsCount = splitData.getParts();
		
		if (value == null) {
			value = "";
		}
		this.value = value;
		
		Vector partsVector = new Vector();
		int foundParts = 0;
		
		Pattern compiledPattern = null;
		errorInPattern = false;
		
		try {
			compiledPattern = Pattern.compile(pattern);
		} catch (PatternSyntaxException exception) {
			errorInPattern = true;
			errorDesc = exception.getDescription();
		}
		
		String delimiter = ""; 
		if (!errorInPattern) {

			/* If the type of splitting is by delimiter pattern, split the string by them and store the matching
			 * delimiters.  
			 */
			if (delimiterType) {
				Matcher matcher = compiledPattern.matcher(value);
				int partStart = 0;
				int partEnd = 0;
				String part = "";
				while ((foundParts < partsCount - 1 || partsCount == -1) && matcher.find()) {
					partEnd = matcher.start();
					part = value.substring(partStart, partEnd);
					partStart = matcher.end();
					partsVector.add(new SplitPropertyPart(foundParts, this, part, "", delimiter, true));
					delimiter = matcher.group();
					foundParts++;
				}
				// If there are trailing delimiters, add them under trail.
				if ((foundParts >= partsCount - 1 && partsCount != -1) && matcher.find()) {
					partEnd = matcher.start();
					part = value.substring(partStart, partEnd);
					partStart = matcher.end();
					partsVector.add(new SplitPropertyPart(foundParts, this, part, "", delimiter, true));
					trail = value.substring(partStart);
				} else {
					part = value.substring(partStart);
					partsVector.add(new SplitPropertyPart(foundParts, this, part, "", delimiter, true));
					trail = "";
				}
				foundParts++;
				if (partsCount == -1) {
					partsCount = foundParts;
				}
			} else {
				// If the type of splitting is by pattern, match it by groups and store them and in-between strings.
				Matcher matcher = compiledPattern.matcher(value);
				patternMatch = matcher.matches();

				int leadStart = 0;
				int leadEnd = 0;
				if (patternMatch) {
					int groupCount = matcher.groupCount();
					if (partsCount == -1) {
						partsCount = groupCount; 
					}

					// First group is the whole expression, skip it.
					groupCount++;
					int group = 1;

					while (group < groupCount && foundParts < partsCount) {
						leadEnd = matcher.start(group);
						if (leadStart > leadEnd) {
							nestedGroups = true;
							break;
						}
						String lead = value.substring(leadStart, leadEnd);
						leadStart = matcher.end(group);
						String groupStr = matcher.group(group);
						partsVector.add(new SplitPropertyPart(foundParts, this, groupStr, "", lead, true));
						foundParts++;
						group++;
					}
				}
				if (patternMatch && !nestedGroups) {
					trail = value.substring(leadStart);
				} else {
					partsVector.clear();
					partsVector.add(new SplitPropertyPart(0, this, value, "", "", true));
					foundParts = 1;
					trail = "";
				}	
			}
		}
		
		/* If there are columns remaining, fill them with empty fields with the same delimiter. If delimiter is not
		 * known, use default.
		 */
		if (delimiter.equals("")) {
			delimiter = defaultDelimiter;
		}
		while (foundParts < partsCount) {
			partsVector.add(new SplitPropertyPart(foundParts, this, "", "", delimiter, true));
			foundParts++;
		}
		parts = new SplitPropertyPart[partsVector.size()];
		partsVector.copyInto(parts);

		patternParts = foundParts; 
	}

	public static int getPartsCount(String value, DBSheetSplitCol splitData) {
		
		if (value == null) {
			value = "";
		}
		boolean delimiterType = splitData.isDelimiterType();
		String pattern = splitData.getPattern();
		
		if (delimiterType) {
			return value.split(pattern, -1).length;
		} else {
			int groupCount = 0;
			try {
				Matcher matcher = Pattern.compile(pattern).matcher(value);
				boolean match = matcher.matches();
				groupCount = match ? matcher.groupCount() : 0;
			} catch (PatternSyntaxException exception) {
				// do nothing
			}
			return groupCount;
		}
	}
	
	public void setValuePart(SplitPropertyPart part) {
		String value = "";
		for (int p = 0; p < parts.length; p++) {
			value += parts[p].getLead() + parts[p].getValue();
		}
		this.value = value + trail;
		owner.setValue(this.value);
	}

	/**
	 * @return the owner
	 */
	public InspectableProperty getOwner() {
		return owner;
	}
	
	/**
	 * @param owner the owner to set
	 */
	public void setOwner(InspectableProperty owner) {
		this.owner = owner;
	}

	public SplitPropertyPart getPart(int index) {
		return parts[index];
	}

	/** Returns the number of parts that the property value was split into using split data.  
	 * 
	 * @return the patternParts
	 */
	public int getPatternParts() {
		return patternParts;
	}

	/**
	 * @return the patternMatch
	 */
	public boolean isPatternMatch() {
		return patternMatch;
	}

	/**
	 * @return the nestedGroups
	 */
	public boolean isNestedGroups() {
		return nestedGroups;
	}

	/**
	 * @return the errorInPattern
	 */
	public boolean isErrorInPattern() {
		return errorInPattern;
	}

	/**
	 * @return the errorDesc
	 */
	public String getErrorDesc() {
		return errorDesc;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
}
