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

package com.cosylab.vdct.rdb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import com.cosylab.vdct.db.DBData;

/**
 * @author ssah
 *
 */
public class RdbDataMapper {

	private RdbConnection helper = null;

	public RdbDataMapper() throws Exception {
		helper = new RdbConnection();
	}
	
	public void setConnectionParameters(String host, String database, String user, String password) {
		helper.setParameters(host, database, user, password);
	}

	public DBData loadRdbData(Object dsId, RdbDataId dataId) throws Exception {

		DBData data = null;
		Exception exception = null;
		try {
			RdbDbContext context = new RdbDbContext(dsId, dataId, helper);
			data = context.load();
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
			exception = new Exception("Error while loading database: " + sqlException.getMessage());
		} catch (IllegalArgumentException illegalArgumentException) {
		    // Nothing
		}
		
		if (exception != null) {
			throw exception;
		}
		return data;
	}
	
	public boolean saveRdbData(Object dsId, RdbDataId dataId) throws Exception {
		
		boolean success = false;
		Exception exception = null;
		try {
			RdbDbContext context = new RdbDbContext(dsId, dataId, helper);
			context.save();
			helper.commit();

			success = true;
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
			exception = new Exception("Error while saving database: " + sqlException.getMessage());
			helper.rollbackConnection();
		} catch (IllegalArgumentException illegalArgumentException) {
		    // Nothing
		}
			
		if (exception != null) {
			throw exception;
		}
		return success;
	}
	
	public Connection createNewConnection() throws SQLException {
		return helper.createConnection();
	}

	public boolean isConnection() {
		return helper.isConnection();
	}
	
	public void closeConnection() throws SQLException {
		helper.closeConnection();
	}
	
	public int createAnIoc() throws SQLException {
		int iocId = saveIoc();
        helper.commit();
		return iocId;
	}
	
	/** Returns Vector of String objects representing IOCs.
	 */ 
	public Vector getIocs() throws SQLException {

		Object[] columns = {"ioc_id"};  
		Object[][] conditions = {{}, {}};
		ResultSet set = helper.loadRows("ioc", columns, conditions, null, "ioc_id");
        
        Vector iocs = new Vector();
		while (set.next()) {
        	iocs.add(set.getString(1));
        }
        return iocs;
	}

	/** Returns Vector of String objects representing db files under the given IOC.
	 */ 
	public Vector getRdbDatas(String iocId) throws SQLException {

		Object[] columns = {"p_db_file_name"};  
		Object[][] conditions = {{"ioc_id_FK"}, {iocId}};
		ResultSet set = helper.loadRows("p_db", columns, conditions, null, "p_db_file_name");
        
        Vector groups = new Vector();
        while (set.next()) {
        	groups.add(set.getString(1));
        }
        return groups;
	}

	/** Returns Vector of String objects representing versions of the given group.
	 */ 
	public Vector getVersions(String group, String iocId) throws SQLException {

		Object[] columns = {"p_db_version"};  
		Object[][] conditions = {{"ioc_id_FK", "p_db_file_name"}, {iocId, group}};
		ResultSet set = helper.loadRows("p_db", columns, conditions, null, "p_db_version");
        
        Vector versions = new Vector();
        while (set.next()) {
        	versions.add(set.getString(1));
        }
        return versions;
	}
	
	public void addRdbDataId(RdbDataId dataId, String desription) throws SQLException {

    	Object[][] keyPairs = {{"p_db_file_name", "ioc_id_FK", "p_db_version"},
    			{dataId.getFileName(), dataId.getIoc(), dataId.getVersion()}};
		Object[][] valuePairs = {{"p_db_desc"}, {desription}};
   		helper.saveRow("p_db", keyPairs, valuePairs);
        helper.commit();
	}
	
	private int saveIoc() throws SQLException {
		Object[][] keyPairs = {{}, {}};
		Object[][] valuePairs = {{}, {}};
		helper.appendRow("ioc", keyPairs, valuePairs);
		
		Object[] columns = {"ioc_id"};
		ResultSet set = helper.loadRows("ioc", columns, keyPairs);
		if (!set.next()) {
			throw new SQLException("Could not create new ioc.");
		}
		return set.getInt(1);
	}
}
