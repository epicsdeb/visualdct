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

import java.awt.Component;
import java.util.regex.Pattern;

import com.cosylab.vdct.inspector.InspectableProperty;

/**
 * @author ssah
 *
 */
public class SplitPropertyPart implements InspectableProperty {

	private int partIndex = 0;
	private SplitPropertyGroup owner = null;
	private String value = null;
	private String initValue = null;
	private String lead = null;
	private boolean editable = true;

	/**
	 * @param partIndex
	 * @param owner
	 * @param value
	 * @param initValue
	 * @param lead
	 * @param editable
	 */
	public SplitPropertyPart(int partIndex, SplitPropertyGroup owner, String value, String initValue, String lead,
			boolean editable) {
		super();
		this.partIndex = partIndex;
		this.owner = owner;
		this.value = value;
		this.initValue = initValue;
		this.lead = lead;
		this.editable = editable;
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.InspectableProperty#allowsOtherValues()
	 */
	public boolean allowsOtherValues() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.InspectableProperty#getEditPattern()
	 */
	public Pattern getEditPattern() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.InspectableProperty#getHelp()
	 */
	public String getHelp() {
		return owner.getOwner().getToolTipText();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.InspectableProperty#getInitValue()
	 */
	public String getInitValue() {
		return initValue;
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.InspectableProperty#getName()
	 */
	public String getName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.InspectableProperty#getSelectableValues()
	 */
	public String[] getSelectableValues() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.InspectableProperty#getToolTipText()
	 */
	public String getToolTipText() {
		return owner.getOwner().getToolTipText();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.InspectableProperty#getValue()
	 */
	public String getValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.InspectableProperty#getVisibility()
	 */
	public int getVisibility() {
		return owner.getOwner().getVisibility();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.InspectableProperty#isEditable()
	 */
	public boolean isEditable() {
		return editable;
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.InspectableProperty#isSeparator()
	 */
	public boolean isSeparator() {
		return owner.getOwner().isSeparator();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.InspectableProperty#isValid()
	 */
	public boolean isValid() {
		return owner.getOwner().isValid();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.InspectableProperty#popupEvent(java.awt.Component, int, int)
	 */
	public void popupEvent(Component component, int x, int y) {
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.InspectableProperty#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		this.value = value;
		owner.setValuePart(this);
	}

	/**
	 * @return the partIndex
	 */
	public int getPartIndex() {
		return partIndex;
	}

	/**
	 * @return the lead
	 */
	public String getLead() {
		return lead;
	}

	/**
	 * @return the owner
	 */
	public SplitPropertyGroup getOwner() {
		return owner;
	}
	
	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.InspectableProperty#hasDefaultValue()
	 */
	public boolean hasDefaultValue() {
		return owner.getOwner().hasDefaultValue();
	}
	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.InspectableProperty#hasValidity()
	 */
	public boolean hasValidity() {
		return owner.getOwner().hasValidity();
	}
	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.InspectableProperty#checkValueValidity(java.lang.String)
	 */
	public String checkValueValidity(String value) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.cosylab.vdct.inspector.InspectableProperty#getGuiGroup()
	 */
	public Integer getGuiGroup() {
		return owner.getOwner().getGuiGroup();
	}
}
