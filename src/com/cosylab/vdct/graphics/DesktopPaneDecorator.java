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

package com.cosylab.vdct.graphics;

import java.awt.Graphics;

import javax.swing.JDesktopPane;

/**
 * @author ssah
 *
 */
public abstract class DesktopPaneDecorator extends JDesktopPane implements VisualComponent {

	private VisualComponent component;

	public DesktopPaneDecorator() {}

	public DesktopPaneDecorator(VisualComponent component) {
		this.component=component;
	}
	
	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.VisualComponent#draw(java.awt.Graphics)
	 */
	public void draw(Graphics g) {
		component.draw(g);
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.VisualComponent#getComponentHeight()
	 */
	public int getComponentHeight() {
		if (component==null) return 0;
		else return component.getComponentHeight();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.VisualComponent#getComponentWidth()
	 */
	public int getComponentWidth() {
		if (component==null) return 0;
		else return component.getComponentWidth();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.vdct.graphics.VisualComponent#resize(int, int, int, int)
	 */
	public void resize(int x0, int y0, int width, int height) {
		if (component != null) { 
			component.resize(x0, y0, width, height);
		}
	}
	
	public VisualComponent getComponent() {
		return component;
	}

	public void setComponent(VisualComponent newComponent) {
		component = newComponent;
	}
	
}
