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


/** Interface for accessing properties from a table. 
 * @author ssah
 *
 */
public interface PropertyTableModel {
	
	/**
	 * Defines the empty cell display type.
	 */
	public static final int DISP_NONE = 0;
	/**
	 * Defines the name cell display type.
	 */
	public static final int DISP_NAME = 1;
	/**
	 * Defines the editable value cell display type.
	 */
	public static final int DISP_VALUE = 2;
	/**
	 * Defines the visibility icon cell display type.
	 */
	public static final int DISP_VISIBILITY = 3;
	
	/**
	 * The header display types, text, eye icon, or nothing.
	 */
	public static final int HEADERDISP_TEXT = 0;
	public static final int HEADERDISP_EYE = 1;
	public static final int HEADERDISP_NONE = 2;

	public InspectableProperty getPropertyAt(int row, int column);
	
	/**
	 * Returns the cell display type DISP_* at the given position.
	 */
	public int getPropertyDisplayTypeAt(int row, int column);

	/**
	 * Returns the header display type HEADERDISP_* at the given position.
	 */
	public int getHeaderDisplayType(int column);
}
