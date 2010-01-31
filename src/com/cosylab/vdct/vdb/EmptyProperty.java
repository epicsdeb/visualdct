/**
 * 
 */
package com.cosylab.vdct.vdb;


/**
 * @author ssah
 *
 */
public class EmptyProperty extends NameValueInfoProperty {

	public EmptyProperty() {
		super("", "");
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.vdb.NameValueInfoProperty#isEditable()
	 */
	public boolean isEditable() {
		return false;
	}

}
