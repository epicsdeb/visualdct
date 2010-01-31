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

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.cosylab.vdct.Constants;
import com.cosylab.vdct.db.DBData;
import com.cosylab.vdct.db.DBFieldData;
import com.cosylab.vdct.db.DBRecordData;
import com.cosylab.vdct.db.DBResolver;
import com.cosylab.vdct.db.DBTemplate;
import com.cosylab.vdct.graphics.objects.Group;
import com.cosylab.vdct.graphics.objects.Record;
import com.cosylab.vdct.graphics.objects.Template;
import com.cosylab.vdct.util.StringUtils;
import com.cosylab.vdct.vdb.VDBFieldData;
import com.cosylab.vdct.vdb.VDBRecordData;

/**
 * @author ssah
 *
 */
public class RdbDbContext {

	private Object dsId = null;
	private RdbDataId dataId = null;
	private RdbConnection connection = null;
	
	// TASK:RDBDBD
	//private Integer pDbdId = null;
	private Integer pDbId = null; 
	
	private Map recordTypes = new HashMap();

	private static final String emptyString = "";
	// TASK:RDBDBD
	/*
	private Integer pDbdId = null;
	private static final String recordDefDescription = "Saved by VDCT";
	private static final String dtypString = "DTYP";
	*/
	
	public RdbDbContext(Object dsId, RdbDataId dataId, RdbConnection connection)
	throws SQLException, IllegalArgumentException {
		
		super();
		this.dsId = dsId;
		this.dataId = dataId;
		this.connection = connection;
		
		// TASK:RDBDBD
		//prepareMinDefinitions();
		
		loadDbId(dataId);
		loadDefinitions();
	}
	
	public DBData load() throws SQLException {

		String fileName = dataId.getFileName();
		File file = new File(fileName);
		DBData data = new DBData(file.getName(), fileName);
		DBTemplate template = data.getTemplateData();
		template.setModificationTime(file.lastModified());
		template.setVersion(dataId.getVersion()); 
		template.setIoc(dataId.getIoc()); 
		template.setDescription(dataId.getDescription()); 

		loadRecords(data);
		loadTemplates(data);
		loadVdctData(data);
		
		return data;
	}
	
	public void save() throws SQLException {
		
		deleteDb();
				
		// TASK:RDBDBD
		//saveDataDef();
		saveRecords();
		saveVdctData();
				
		connection.commit();
	}
	
	private void loadRecords(DBData data) throws SQLException {

		// TASK:RECCODES
		/*
		Object[] columns = {"sgnl_id", "sgnl_rec.rec_type_id"};  
		Object[][] conditions = {{"epics_grp_id", "sgnl_rec.rec_type_id", "rec_type_code"},
                {group,            "sgnl_rec_type.rec_type_id", "E"}};
		ResultSet set = loadRows("sgnl_rec, sgnl_rec_type", columns, conditions);
        */

		Object[] columns = {"p_rec_id", "p_rec_nm", "p_rec_type_id_FK"};  
		Object[][] conditions = {{"p_db_id_FK"}, {pDbId}};
		
		ResultSet set = connection.loadRows("p_rec", columns, conditions);
        
		Integer id = null;
		String name = null;
		String type = null;
        while (set.next()) {
    		DBRecordData record = new DBRecordData();
    		id = new Integer(set.getInt(1));
    		name = set.getString(2);
    		type = set.getString(3);
    		record.setName(name);
    		record.setRecord_type(type);
    		loadFields(record, id);
    		data.addRecord(record);
    		
    		// TASK:RDBDBD
    		//loadRecordDef(type);
        }
	}

	private void loadTemplates(DBData data) throws SQLException {
//		 TASK:RDBTEMPL: Currently disabled.
		/*
		Object[] columns = {"expand_id", "template_id"};  
		ResultSet set = loadRows("expand", columns, "");
        while (set.next()) {
    		DBTemplateInstance templateInstance =
    			new DBTemplateInstance(set.getString(2), set.getString(1));
    		data.addTemplateInstance(templateInstance);
        }
        */
	}
	
	private DBRecordData loadFields(DBRecordData record, Integer recordId) throws SQLException {
        
		Object[] columns = {"p_fld_type", "p_fld_val"};  
		Object[][] conditions = {{"p_rec_id_FK"}, {recordId}};
		
		ResultSet set = connection.loadRows("p_fld, p_fld_type", columns, conditions, "p_fld_type_id_FK=p_fld_type_id");
        while (set.next()) {
        	// value in DBFieldData can be null, in this case it will be set to default.
        	record.addField(new DBFieldData(set.getString(1), set.getString(2)));
        }
		return record;
	}
	
	// TASK:RDBDBD
	/*
	private void loadRecordDef(String name) throws SQLException {

		DBDData definitions = DataProvider.getInstance().getDbdDB();
		if (definitions.getDBDRecordData(name) == null) {
			DBDRecordData recordDef = new DBDRecordData();
			recordDef.setName(name);
			
			Object[] columns = {"fld_id", "fld_prmt_grp", "fld_type_id", "fld_init", "fld_desc", "fld_base", "fld_size", "sgnl_fld_menu_id"};
			Object[][] conditions = {{"rec_type_id"}, {name}};
			
			
			ResultSet set = connection.loadRows("sgnl_fld_def", columns, conditions, null, "prmpt_ord");
	        while (set.next()) {
	        	DBDFieldData fieldDef = new DBDFieldData();

	        	fieldDef.setName(set.getString(1));
	        	fieldDef.setGUI_type(DBDResolver.getGUIType(set.getString(2)));
	        	fieldDef.setField_type(DBDResolver.getFieldType(set.getString(3)));
	        	
	        	String initValue = set.getString(4);
	        	if (initValue != null) {
	        	    fieldDef.setInit_value(initValue);
	        	}
	        	String promptValue = set.getString(5);
	        	if (promptValue != null) {
	        	    fieldDef.setPrompt_value(promptValue);
	        	}
	        	fieldDef.setBase_type(DBDResolver.getGUIType(set.getString(6)));
	        	fieldDef.setSize_value(set.getInt(7));
	        	String menuName = set.getString(8);
	        	if (menuName != null) {
	        	    fieldDef.setMenu_name(menuName);
	        	    loadMenuOrDeviceDef(menuName);
	        	}
	        	recordDef.addField(fieldDef);
	        }
			
			definitions.addRecord(recordDef);
		}
	}

	private void loadMenuOrDeviceDef(String name) throws SQLException {
		if (name.endsWith(dtypString)) {
			loadDeviceDef(name);
		} else {
			loadMenuDef(name);
		}
	}

	private void loadMenuDef(String name) throws SQLException {
		DBDData definitions = DataProvider.getInstance().getDbdDB();
		
		if (definitions.getDBDMenuData(name) == null) {
			DBDMenuData menuDef = new DBDMenuData();
			menuDef.setName(name);

			Object[] columns = {"fld_menu_val"};
			Object[][] conditions = {{"sgnl_fld_menu_id"}, {name}};
			
			ResultSet set = connection.loadRows("sgnl_fld_menu", columns, conditions);
	        while (set.next()) {
	        	String value = set.getString(1);
	        	menuDef.addMenuChoice(value, value);
	        }
			definitions.addMenu(menuDef);
		}
	}

	private void loadDeviceDef(String name) throws SQLException {
		String recordName = name.substring(0, name.lastIndexOf(dtypString));
		
		DBDData definitions = DataProvider.getInstance().getDbdDB();
		if (definitions.getDBDDeviceData(recordName) == null) {
			
			Object[] columns = {"fld_menu_val"};  
			Object[][] conditions = {{"sgnl_fld_menu_id"}, {name}};
			
			ResultSet set = connection.loadRows("sgnl_fld_menu", columns, conditions);
	        while (set.next()) {
	        	DBDDeviceData deviceDef = new DBDDeviceData();
				deviceDef.setRecord_type(recordName);
				deviceDef.setLink_type("CONSTANT");
				deviceDef.setChoice_string(set.getString(1));
				definitions.addDevice(deviceDef);
	        }
		}
	}
	
	private void saveDataDef() throws SQLException {
		saveMenusDef();
		saveDevicesDef();
		saveRecordsDef();
	}

	private void saveMenusDef() throws SQLException{
		DBDData definitions = DataProvider.getInstance().getDbdDB();
		Hashtable menus = definitions.getMenus();
		Enumeration menusKeys = menus.keys();
		DBDMenuData menuData = null;
		String menuName = null;
		Iterator menuChoices = null;
		String menuChoice = null;
		
		while (menusKeys.hasMoreElements()) {
			menuName = (String)menusKeys.nextElement();
			menuData = (DBDMenuData)menus.get(menuName);
			
			menuChoices = menuData.getChoices().values().iterator();
			while (menuChoices.hasNext()) {
				menuChoice = (String)menuChoices.next();

				Object[][] keyPairs = {{"sgnl_fld_menu_id", "fld_menu_val"},
					      {menuName,           menuChoice}};
				Object[][] valuePairs = {{}, {}};
				connection.saveRow("sgnl_fld_menu", keyPairs, valuePairs);
			}
		}
	}

	private void saveDevicesDef() throws SQLException{

		DBDData definitions = DataProvider.getInstance().getDbdDB();
		Iterator devices = definitions.getDevices().values().iterator();
		DBDDeviceData device = null;
		String recordType = null;
		String menuName = null;
		String choice = null;
		
		while (devices.hasNext()) {
			device = (DBDDeviceData)devices.next();
			recordType = device.getRecord_type();
			menuName = recordType + dtypString;
			choice = device.getChoice_string();
			
			Object[][] menuKeys = {{"sgnl_fld_menu_id", "fld_menu_val"},
					{menuName,           choice}};
			Object[][] menuValues = {{}, {}};
			connection.saveRow("sgnl_fld_menu", menuKeys, menuValues);
		}
	}
	
	private void saveRecordsDef() throws SQLException {
		DBDData definitions = DataProvider.getInstance().getDbdDB();
		Enumeration recordNames = definitions.getRecordNames();
		DBDRecordData record = null;
		String recordName = null;
		
		while (recordNames.hasMoreElements()) {
			recordName = (String)recordNames.nextElement();

			Object[][] keyPairs = {{"rec_type_id"},
		             {recordName}};
			Object[][] valuePairs = {{"rec_type_code", "type_desc"},
		             {"E", recordDefDescription}};
			connection.saveRow("sgnl_rec_type", keyPairs, valuePairs);

   			record = definitions.getDBDRecordData(recordName);
   			System.out.println("Saving record: " + recordName);
   			saveFieldsDef(record);
   			System.out.println("Ending save record: " + recordName);
		}
	}

	private void saveFieldsDef(DBDRecordData record) throws SQLException {

		Iterator fields = record.getFieldsV().iterator();
		DBDFieldData field = null;
		String recordName = record.getName();
		String fieldName = null;
		int fieldType = DBDConstants.NOT_DEFINED;
		String menuName = null;
		
		while (fields.hasNext()) {
			field = (DBDFieldData)fields.next();
			fieldName = field.getName();
			
			fieldType = field.getField_type();
			menuName = fieldType == DBDConstants.DBF_DEVICE
				? recordName + dtypString : field.getMenu_name();
			
			Object[][] keyPairs = {{"rec_type_id",   "fld_id"},
					{recordName, fieldName}};
			Object[][] valuePairs = {{"fld_prmt_grp", "fld_type_id", "fld_init", "fld_desc", "fld_base", "fld_size", "sgnl_fld_menu_id"},
					{
				DBDResolver.getGUIString(field.getGUI_type()),
				DBDResolver.getFieldType(fieldType),
				field.getInit_value(),
				field.getPrompt_value(),
				DBDResolver.getBaseType(field.getBase_type()),
				String.valueOf(field.getSize_value()),
			    menuName}
			};
			connection.saveRow("sgnl_fld_def", keyPairs, valuePairs);
		}
	}
	
	private void saveMinRecordDef(String type) throws SQLException {
		
		Object[][] keyPairs = {{"p_rec_type_id", "p_dbd_id_FK"}, {type, pDbdId}};
		Object[][] valuePairs = {{"p_rec_type_code", "p_type_desc"}, {"E", recordDefDescription}};
		connection.appendRow("p_rec_type", keyPairs, valuePairs);
	}
	
	private int saveMinFieldDef(String fieldName, String recordType) throws SQLException {
		
		Object[][] keyPairs = {{"p_rec_type_id_FK", "p_dbd_id_FK", "p_fld_type"}, {recordType, pDbdId, fieldName}};
		Object[][] valuePairs = {{}, {}};
		connection.appendRow("p_fld_type", keyPairs, valuePairs);

		Object[] columns = {"p_fld_type_id"};
		ResultSet set = connection.loadRows("p_fld_type", columns, keyPairs);
		return set.next() ? set.getInt(1) : 0;
	}
	*/
	
	private void saveRecords() throws SQLException {

		// rec type code usage: remove when Group and rec_type_code usage is defined  
		/*
		PreparedStatement statement = connection.prepareStatement(
        		"SELECT sgnl_id FROM sgnl_rec, sgnl_rec_type"
        		+ " WHERE epics_grp_id=? AND sgnl_rec.rec_type_id = sgnl_rec_type.rec_type_id"
        		+ " AND rec_type_code='E'");
        */
		
        Iterator iterator = Group.getRoot(dsId).getStructure().iterator();

		VDBRecordData recordData = null;
		String name = null;
        
        while (iterator.hasNext()) {
        	Object object = iterator.next();
        	
        	if (object instanceof Record) {
        		recordData = ((Record)object).getRecordData();
        		name = recordData.getName();
        		
        		if (!name.startsWith(Constants.CLIPBOARD_NAME)) {
        			saveRecord(recordData);
        		}
        		
        	} else if (object instanceof Template) {
        		// TASK:RDBTEMPL: Not completed yet.
        		/*
        		VDBTemplateInstance template = ((Template)object).getTemplateData();

        		name = template.getName();

        		if (!name.startsWith(Constants.CLIPBOARD_NAME)) {
        			Object[][] keyPairs = {{"export_id"}, {name}};
        			Object[][] valuePairs = {{"export_id", "template_id"},
        					                 {name,        template.getTemplate().getId()}};
        			saveRow("export", keyPairs, valuePairs);
        		}
        		*/
        	}
        }
	}
	
	private void saveRecord(VDBRecordData recordData) throws SQLException  {
		String name = recordData.getName();
		String type = recordData.getType();
		// TASK:RDBDBD
		//saveMinRecordDef(type);
		
		Object[][] keyPairs = {{"p_rec_nm", "p_db_id_FK", "p_rec_type_id_FK"}, {name, pDbId, type}};
		Object[][] valuePairs = {{}, {}};
		connection.insertRow("p_rec", keyPairs, valuePairs);
		
		Object[] columns = {"p_rec_id"};
		ResultSet set = connection.loadRows("p_rec", columns, keyPairs);
		
		Integer recId = new Integer(set.next() ? set.getInt(1) : 0);
		
		Iterator iterator = recordData.getFieldsV().iterator();
		while (iterator.hasNext()) {
			saveField((VDBFieldData)iterator.next(), recId, type);
		}
	}
	
	private void saveField(VDBFieldData fieldData, Integer recId, String recordType) throws SQLException {

		String name = fieldData.getName();
    	String value = StringUtils.removeQuotes(fieldData.getValue());
    	String table = "p_fld";
		
		// TASK:RDBDBD
    	//Integer fldTypeId = new Integer(saveMinFieldDef(name, recordType));
    	Map map = (Map)recordTypes.get(recordType);
    	if (map == null) {
    		throw new SQLException("Record definition '" + recordType + "' does not exists in"
    				+ " the database.");
    	}
    	Integer fldTypeId = (Integer)map.get(name);
    	if (fldTypeId == null) {
    		throw new SQLException("Field definition '" + name + "' for record '"
    				+ recordType + "' does not exists in the database.");
    	}
    	
    	Object[][] keyPairs = {{"p_rec_id_FK", "p_fld_type_id_FK"}, {recId, fldTypeId}};
		Object[][] valuePairs = {{"p_fld_val"}, {value}};
		
    	if (!fieldData.hasDefaultValue() && !value.equals(emptyString)) {
    		connection.insertRow(table, keyPairs, valuePairs);
        }
	}
	
	private void loadVdctData(DBData data) throws SQLException {

		Object[] columns = {"p_db_vdct"};
    	Object[][] keyPairs = {{"p_db_id"}, {pDbId}};
		
   		ResultSet set = connection.loadRows("p_db", columns, keyPairs);
   		if (set.next()) {
   			String string = set.getString(1);
   			if (string != null) {
    			DBResolver.readVdctData(dsId, data, string, data.getTemplateData().getId());
   			}
   		}
	}
	
	private void saveVdctData() throws SQLException {
		String string = Group.getVDCTData(dsId);
    	Object[][] keyPairs = {{"p_db_id"}, {pDbId}};
		Object[][] valuePairs = {{"p_db_vdct"}, {string}};
   		connection.updateRow("p_db", keyPairs, valuePairs);
	}
	
	private void deleteDb() throws SQLException {
		Object[] columns = {"p_rec_id"};
    	Object[][] recordMatch = {{"p_db_id_FK"}, {pDbId}};
		
   		ResultSet set = connection.loadRows("p_rec", columns, recordMatch);
   		while (set.next()) {
   			Object[][] fieldMatch = {{"p_rec_id_FK"}, {set.getString(1)}};
   			connection.deleteRows("p_fld", fieldMatch);
   		}
   		connection.deleteRows("p_rec", recordMatch);
	}	

	// TASK:RDBDBD
	/*
	private void prepareMinDefinitions() throws SQLException, IllegalArgumentException  {
		Object[][] keyPairs = {{}, {}};
		Object[][] valuePairs = {{}, {}};
		connection.appendRow("p_dbd", keyPairs, valuePairs);
		
		Object[] columns = {"p_dbd_id"};
		ResultSet set = connection.loadRows("p_dbd", columns, keyPairs);
		if (!set.next()) {
			throw new IllegalArgumentException();
		}
		pDbdId = new Integer(set.getInt(1));
	}
    */
	private void loadDbId(RdbDataId dataId) throws SQLException, IllegalArgumentException  {
		String fileName = dataId.getFileName();
		String version = dataId.getVersion();
		String ioc = dataId.getIoc();

		Object[] columns = {"p_db_id"};  
		Object[][] conditions = {{"p_db_file_name", "p_db_version", "ioc_id_FK"},
				{fileName, version, ioc}};
		ResultSet set = connection.loadRows("p_db", columns, conditions);

		if (!set.next()) {
			throw new IllegalArgumentException();
		}
		pDbId = new Integer(set.getInt(1));
	}
	
	private void loadDefinitions() throws SQLException {
		Object[] recTypeColumns = {"p_rec_type_id"};
		Object[][] recPairs = {{}, {}};
		ResultSet set = connection.loadRows("p_rec_type", recTypeColumns, recPairs);
		
		String recType = null;
		ResultSet fieldSet = null;
		Map map = null;
		while (set.next()) {
			
			recType = set.getString(1);
			Object[] fieldTypeColumns = {"p_fld_type_id", "p_fld_type"};
			Object[][] fieldPairs = {{"p_rec_type_id_FK"}, {recType}};
			fieldSet = connection.loadRows("p_fld_type", fieldTypeColumns, fieldPairs);
			
			map = new HashMap();
			while (fieldSet.next()) {
				map.put(fieldSet.getString(2), new Integer(fieldSet.getInt(1)));
			}
			recordTypes.put(recType, map);
		}
	}
}
