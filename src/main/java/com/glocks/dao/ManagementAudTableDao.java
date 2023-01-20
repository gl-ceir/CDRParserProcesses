package com.glocks.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.Logger;

import com.glocks.pojo.ManagementDb;
import com.glocks.util.Util;

public class ManagementAudTableDao {
//	static Logger logger = LogManager.getLogger(ManagementAudTableDao.class);
	
//	public void insertManagementDbAud(Connection conn, List<ManagementDb> managementDbs, String tableName, String sequenceName) {
//		boolean isOracle = conn.toString().contains("oracle");
//		String dateFunction = Util.defaultDate(isOracle);
//
//		String query = "insert into " + tableName + " (id, rev, revtype, created_on, device_id_type, "
//				+ "device_launch_date, device_status, device_type, imei_esn_meid, modified_on, multiple_sim_status," 
//				+ "sn_of_device, txn_id, user_id, feature_name) values(";
//
//		if (isOracle) {
//			query = query + sequenceName +".nextVal,";
//		}else {
//			query = query + (getMaxIdDeviceImporterAud(conn, tableName) + 1) +",";
//		}
//
//		query = query + "?,?," + dateFunction + ",?,?,?,?,?,?," + dateFunction + ",?,?,?,?)";
//
//		PreparedStatement preparedStatement = null;
//
//		 // System.out.println("Add " + tableName + " [" + query + " ]");
//		logger.info("Add " + tableName + " ["+query+"]");
//
//		try {
//			preparedStatement = conn.prepareStatement(query);
//
//			for (ManagementDb managementDb : managementDbs) {
//				preparedStatement.setLong(1, managementDb.getRev());
//				preparedStatement.setInt(2, 2); 
//				preparedStatement.setString(3, managementDb.getDeviceIdType());
//				preparedStatement.setString(4, managementDb.getDeviceLaunchDate());
//				preparedStatement.setString(5, managementDb.getDeviceStatus());
//				preparedStatement.setString(6, managementDb.getDeviceType());
//				preparedStatement.setString(7, managementDb.getImeiEsnMeid()); 
//				preparedStatement.setString(8, managementDb.getMultipleSimStatus());
//				preparedStatement.setString(9, managementDb.getSnOfDevice());
//				preparedStatement.setString(10, managementDb.getTxnId());
//				preparedStatement.setLong(11, managementDb.getUserId());
//				preparedStatement.setString(12, managementDb.getFeatureName());
//				
//				 // System.out.println("Query " + preparedStatement);
//				preparedStatement.addBatch();
//			}
//
//			preparedStatement.executeBatch();
//
//			 // System.out.println("Inserted in " + tableName + " succesfully.");
//			logger.info("Inserted in " + tableName + " succesfully.");
//			
//			conn.commit();
//
//		} catch (SQLException e) {
//			logger.error(e.getMessage(), e);
//			e.printStackTrace();
//		}
//		finally{
//			try {
//				if(Objects.nonNull(preparedStatement))
//					preparedStatement.close();
//			} catch (SQLException e) {
//				logger.error(e.getMessage(), e);
//				e.printStackTrace();
//			}
//		}
//	}
//
//	public Long getMaxIdDeviceImporterAud(Connection conn, String tableName) {
//		Statement stmt = null;
//		ResultSet rs = null;
//		String query = null;
//		Long max = null;
//
//		try{
//			query = "select max(id) as max from " + tableName;
//
//			logger.info("Query ["+query+"]");
//			 // System.out.println("Query ["+query+"]");
//			stmt  = conn.createStatement();
//			rs = stmt.executeQuery(query);
//
//			if(rs.next()){
//				max = rs.getLong("max");
//			}else {
//				max = 0L;
//			}
//			
//			logger.info("Next Id in device_importer_db_aud[" + max + "]");
//			return max;
// 		}
//		catch(Exception e){
//			logger.info("Exception in getFeatureMapping"+e);
//			e.printStackTrace();
//			return 0L;
//		}
//		finally{
//			try {
//				if(rs!=null)
//					rs.close();
//				if(stmt!=null)
//					stmt.close();
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}			
//		}
//	}
	
}


