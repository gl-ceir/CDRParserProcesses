package com.glocks.parser.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.glocks.dao.DeviceCustomDbDao;
import com.glocks.dao.DeviceImporterDbDao;
import com.glocks.parser.Rule;
import com.glocks.pojo.DeviceImporterDb;

public class ApproveConsignment {
	static Logger logger = LogManager.getLogger(ApproveConsignment.class);

	public void process(Connection conn, String operator, String sub_feature, ArrayList<Rule> rulelist, 
			String txnId, String operator_tag, String usertypeName ){

		DeviceCustomDbDao deviceCustomDbDao = new DeviceCustomDbDao();
		DeviceImporterDbDao deviceImporterDbDao = new DeviceImporterDbDao();
	
		try{
			List<DeviceImporterDb> deviceImporterDbs = deviceImporterDbDao.getDeviceImporterDbByTxnId(conn, txnId);
                    logger.info(
                            203);

			deviceCustomDbDao.insertDeviceCustomDbWithImporterObject(conn, deviceImporterDbs);
//			deviceCustomDbDao.insertDeviceCustomDbAudWithImporterObject(conn, deviceImporterDbs, 0);
			
         //
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
	}
}

