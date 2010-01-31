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

package com.cosylab.vdct.rdb;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.cosylab.vdct.db.DBData;

/**
 * @author ssah
 *
 */
public class Rdb implements RdbInterface {

	private RdbDataMapper mapper = null;
	private ConnectionDialog connectionDialog = null;
	private RdbDataChooserDialog groupDialog = null;
	private JFrame guiContext = null;

	public Rdb(JFrame guiContext) {
		super();
		this.guiContext = guiContext; 
		try {
		    mapper = new RdbDataMapper();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		connectionDialog = new ConnectionDialog(guiContext, mapper);
		groupDialog = new RdbDataChooserDialog(mapper, guiContext);
	}

	public void connect() {
		connectionDialog.setVisible(true);
	}
	
	public DBData loadRdbData(Object dsId) {
		if (!mapper.isConnection()) {
			connectionDialog.setVisible(true);
		}
		if (mapper.isConnection()) {
			groupDialog.setLoadMode(true);		
			groupDialog.setDsId(dsId);		
			groupDialog.setRdbDataId(new RdbDataId());		
			groupDialog.setVisible(true);
			return groupDialog.getData();
		}
		return null;
	}
	
	public boolean saveRdbData(Object dsId, RdbDataId rdbId, boolean dialog) {
		boolean success = false;
		if (!mapper.isConnection()) {
			connectionDialog.setVisible(true);
		}
		if (mapper.isConnection()) {
			
			// If no dialog requested try a direct save. 
			if (!dialog) {
				try {
                   	success = mapper.saveRdbData(dsId, rdbId);
				} catch (Exception exception) {
					JOptionPane.showMessageDialog(guiContext, exception.getMessage(),
							"Database error", JOptionPane.ERROR_MESSAGE);
				}
			}
			// If not saved yet bring up saveas dialog.
			if (!success) {
				groupDialog.setLoadMode(false);
				groupDialog.setDsId(dsId);		
				groupDialog.setRdbDataId(rdbId);		
				groupDialog.setVisible(true);
				success = groupDialog.isSuccess();
			}
		}
		return success;
	}
}
