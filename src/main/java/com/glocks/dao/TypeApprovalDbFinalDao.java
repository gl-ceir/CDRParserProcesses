package com.glocks.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TypeApprovalDbFinalDao {
	static Logger logger = LogManager.getLogger(TypeApprovalDbFinalDao.class);
	static String GENERIC_DATE_FORMAT = "dd-MM-yyyy";

	public int deleteByTxnId(Connection conn, String txnId) {
		String query = "";
		Statement stmt = null;
		int executeStatus = 0;

		query = "delete from type_approved_db_final where txn_id='" + txnId + "'";	
		logger.info("Query ["+query+"]");
		 // System.out.println("Query ["+query+"]");

		try {
			stmt = conn.createStatement();
			executeStatus = stmt.executeUpdate(query);
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return executeStatus;
	} 
}