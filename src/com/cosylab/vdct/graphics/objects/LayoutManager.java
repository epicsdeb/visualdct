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

package com.cosylab.vdct.graphics.objects;

/**A simple manager that has info on empty spaces in the 2d plane. Uses n^2 space on a grid of variable density,
 * and supports filling in O(1), but no emptying.   
 * 
 * @author ssah
 */
public class LayoutManager {

	private int width = 0;
	private int height = 0;
	private int positionWidth = 0;
	private int positionHeight = 0;
	private int offsetX = 0;
	private int offsetY = 0;
	
	private LayoutPosition[][] positions = null;
	private LayoutPosition firstVacant = null;
	private LayoutPosition defaultPosition = null;

	/**
	 * @param width
	 * @param height
	 * @param positionWidth
	 * @param positionHeight
	 * @param offsetX
	 * @param offsetY
	 */
	public LayoutManager(int width, int height, int positionWidth,
			int positionHeight, int offsetX, int offsetY) {
		super();
		
		this.width = width;
		this.height = height;
		this.positionWidth = positionWidth;
		this.positionHeight = positionHeight;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		
		positions = new LayoutPosition[height][width];
		LayoutPosition previous = null;
		LayoutPosition current = null;
		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				current = new LayoutPosition(w * positionWidth + offsetX, h * positionHeight + offsetY);
				positions[h][w] = current;
				if (previous != null) {
					previous.setNext(current);
					current.setPrevious(previous);
				}
				previous = current;
			}
		}
		firstVacant = positions[0][0];
		defaultPosition = new LayoutPosition(offsetX, offsetY);
	}
	
	public LayoutPosition getVacantPosition() {
		return (firstVacant != null) ? firstVacant : defaultPosition;
	}
	
	public void fillPosition(int posX, int posY) {
		
		posX -= offsetX;
		posY -= offsetY;
		// Improvements: use width/height of the object to occupy more cells than one.
		int lowX = (int)Math.rint((double)posX / positionWidth);  
		int highX = (int)Math.rint((double)posX / positionWidth);  
		int lowY = (int)Math.rint((double)posY / positionHeight);  
		int highY = (int)Math.rint((double)posY / positionHeight);
		
		lowX = Math.max(lowX, 0);
		highX = Math.min(highX, width - 1);
		lowY = Math.max(lowY, 0);
		highY = Math.min(highY, height - 1);
		
		for (int h = lowY; h < highY + 1; h++) {
			for (int w = lowX; w < highX + 1; w++) {
				removeVacantPosition(w, h);
			}
		}
	}
	
	private void removeVacantPosition(int tableX, int tableY) {
		
		LayoutPosition position = positions[tableY][tableX];
		if (position != null) {
			LayoutPosition previous = position.getPrevious();
			LayoutPosition next = position.getNext();
			if (previous != null) {
				previous.setNext(next);
			}
			if (next != null) {
				next.setPrevious(previous);
			}
			positions[tableY][tableX] = null;
			
			if (firstVacant == position) {
				firstVacant = next;
			}
		}
	}
}
