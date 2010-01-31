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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author ssah
 *
 */
public class RdbConnection {

	private String user = null;
	private String password = null;
	private String host = null;
	private String database = null;
	private Connection connection = null;
	
	private int selectCount = 0;
	private int updateCount = 0;
	private int insertCount = 0;
	private int deleteCount = 0;
	
	public RdbConnection() throws Exception {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
	}
	
	public void setParameters(String host, String database, String user, String password) {
		this.host = host;
		this.database = database;
		this.user = user;
		this.password = password;
	}
	
	public boolean isConnection() {
		return connection != null;
	}
	
	public void commit() throws SQLException {
		connection.commit();
	}
	
	public Connection createConnection() throws SQLException {
		connection = null;
		String connectionString = "jdbc:mysql://" + host + "/" + database
		+ "?user=" + user + "&password=" + password;

		connection = DriverManager.getConnection(connectionString);
		connection.setAutoCommit(false);
		return connection;
	}

	public Connection getConnection() {
		return connection;
	}
	
	public void rollbackConnection() throws SQLException {
		connection.rollback();
	}
	
	public void closeConnection() throws SQLException {
		connection.close();
	}
	
	private String getList(Object[] elements) {
		StringBuffer buffer = new StringBuffer();
		int len = elements.length;
		for (int e = 0; e < len; e++) {
			buffer.append(elements[e].toString());
			if (e < len - 1) {
				buffer.append(",");
			}
		}
		return buffer.toString();
	}

	private String getQuestionMarkList(int length) {
		StringBuffer buffer = new StringBuffer();
		for (int e = 0; e < length; e++) {
			buffer.append("?");
			if (e < length - 1) {
				buffer.append(",");
			}
		}
		return buffer.toString();
	}
	
	private String getEqualityList(Object[] elements) {
		return getEqualityExpression(elements, ",");
	}

	private String getEqualityStatement(Object[] elements) {
		return getEqualityExpression(elements, " AND ");
	}

	private String getEqualityExpression(Object[] elements, String delimiter) {
		StringBuffer buffer = new StringBuffer();
		int len = elements.length;
		for (int e = 0; e < len; e++) {
			buffer.append(elements[e].toString());
			buffer.append("=?");
			if (e < len - 1) {
				buffer.append(delimiter);
			}
		}
		return buffer.toString();
	}
	
	private void insertValues(PreparedStatement statement, Object[] elements) throws SQLException {
		insertValues(statement, elements, 0);
	}

	private void insertValues(PreparedStatement statement, Object[] elements1, Object[] elements2) throws SQLException {
		insertValues(statement, elements1, 0);
		insertValues(statement, elements2, elements1.length);
	}
	
	private void insertValues(PreparedStatement statement, Object[] elements, int offset) throws SQLException {
		for (int i = 0; i < elements.length; i++) {
        	statement.setString(i + offset + 1, String.valueOf(elements[i]));
        }
	}

	public ResultSet loadRows(String table, Object[] columns, Object[][] keyPairs) throws SQLException {
		return loadRows(table, columns, keyPairs, null, null);
	}
	
	public ResultSet loadRows(String table, Object[] columns, Object[][] keyPairs, String conditions) throws SQLException {
		return loadRows(table, columns, keyPairs, conditions, null);
	}
	
	public ResultSet loadRows(String table, Object[] columns, Object[][] keyPairs, String conditions, String orderBy) throws SQLException {

        String columnsString = columns != null ? getList(columns) : "*";
        String equalityConditions = getEqualityStatement(keyPairs[0]);
        boolean equalityExists = !equalityConditions.equals(""); 
        
        String sqlString = "SELECT DISTINCT " + columnsString + " FROM " + table;
        if (equalityExists) {
            sqlString += " WHERE " + equalityConditions;
        }
        if (equalityExists && conditions != null) {
            sqlString += " AND ";
        }
        if (conditions != null) {
            sqlString += conditions;
        }
        if (orderBy != null) {
        	sqlString += " ORDER BY " + orderBy;
        }
        PreparedStatement statement = connection.prepareStatement(sqlString);
        insertValues(statement, keyPairs[1]);
        selectCount++;
        return statement.executeQuery();
	}
	
	private boolean isRowPresent(String table, Object[][] keyPairs) throws SQLException {
		ResultSet set = loadRows(table, null, keyPairs);
		return set.next();
	}

	// Performs insert.
	public void insertRow(String table, Object[][] keyPairs, Object[][] valuePairs) throws SQLException {
		int keyLen = keyPairs[1].length;
		int valueLen = valuePairs[1].length;

		String columnString = getList(keyPairs[0]) + (valueLen > 0 ?  "," + getList(valuePairs[0]) : "");
		String valueString = getQuestionMarkList(keyLen + valueLen);
		String sqlString = "INSERT INTO " + table
		+ " (" + columnString + ") VALUES (" + valueString + ")";

		PreparedStatement statement = connection.prepareStatement(sqlString);
		insertValues(statement, keyPairs[1], valuePairs[1]);
		statement.execute();
		insertCount++;
	}

	// Performs update.
	public void updateRow(String table, Object[][] keyPairs, Object[][] valuePairs) throws SQLException {
		if (valuePairs[0].length > 0) {
			String condition = getEqualityStatement(keyPairs[0]);
			String setString = getEqualityList(valuePairs[0]);
			String sqlString = "UPDATE " + table + " SET " + setString + " WHERE " + condition;

			PreparedStatement statement = connection.prepareStatement(sqlString);
			insertValues(statement, valuePairs[1], keyPairs[1]);
			statement.execute();
			updateCount++;
		}
	}

	// Performs delete.
	public void deleteRows(String table, Object[][] keyPairs) throws SQLException {
		String sqlString = "DELETE FROM " + table + " WHERE " + getEqualityStatement(keyPairs[0]);
		PreparedStatement statement = connection.prepareStatement(sqlString);
        insertValues(statement, keyPairs[1]);

        statement.execute();
        deleteCount++;
	}
	
	// Performs insert or update if the row already exist.
	public void saveRow(String table, Object[][] keyPairs, Object[][] valuePairs) throws SQLException {
		if (!isRowPresent(table, keyPairs)) {
			insertRow(table, keyPairs, valuePairs);
		} else {
			updateRow(table, keyPairs, valuePairs);
		}
	}
	
	// Performs insert or does nothing if the row already exists.
	public void appendRow(String table, Object[][] keyPairs, Object[][] valuePairs) throws SQLException {
		if (!isRowPresent(table, keyPairs)) {
			insertRow(table, keyPairs, valuePairs);
		}
	}
	
	public void displayStatistics() {
		System.out.println("select statements " + selectCount);
		System.out.println("update statements " + updateCount);
		System.out.println("insert statements " + insertCount);
		System.out.println("delete statements " + deleteCount);
		System.out.println("total statements "
				+ (selectCount + updateCount + insertCount + deleteCount)); 
		System.out.println(); 
	}
	
	public void resetStatistics() {
		selectCount = 0;
		updateCount = 0;
		insertCount = 0;
		deleteCount = 0;
	}
}
