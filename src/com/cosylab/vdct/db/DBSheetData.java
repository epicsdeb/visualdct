/**
 * Copyright (c) 2002, Cosylab, Ltd., Control System Laboratory, www.cosylab.com
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.cosylab.vdct.Constants;
import com.cosylab.vdct.events.CommandManager;
import com.cosylab.vdct.events.commands.GetDsManager;
import com.cosylab.vdct.graphics.DsEventListener;

/**
 * @author ssah
 */
public class DBSheetData implements DsEventListener {

	protected static HashMap instances = new HashMap();
	
	private Map map = null;

	private DBSheetData() {
		super();
		map = new HashMap();
	}
	
	public static DBSheetData getInstance(Object dsId) {

		DBSheetData dbSheetData = (DBSheetData)instances.get(dsId);
		if (dbSheetData == null) {
			System.err.println("Warning: DBSheetData.getInstance: instance with id does not exist,"
					+ " creating new one.");

			dbSheetData = new DBSheetData();
			instances.put(dsId, dbSheetData);
		}
		return dbSheetData;
	}
	
	public void add(DBSheetView record) {
		map.put(record.getKey(), record);
	}

	/** Returns the record with the given key, or null if there is no such record.
	 */
	public DBSheetView get(String key) {
		return (DBSheetView)map.get(key);
	}

	public void remove(String key) {
		map.remove(key);
	}
	
	public Iterator getRecords() {
		return map.values().iterator();
	}

	public static void registerDsListener() {
		
		DBSheetData data = new DBSheetData();
	    instances.put(Constants.DEFAULT_NAME, data);
		
		GetDsManager command = (GetDsManager)CommandManager.getInstance().getCommand("GetDsManager");
		if (command != null) {
			command.getManager().addDsEventListener(data);
		}
	}

	public void onDsAdded(Object id) {
	    instances.put(id, new DBSheetData());
	}
	public void onDsRemoved(Object id) {
	}
	public void onDsFocused(Object id) {
	}
}
