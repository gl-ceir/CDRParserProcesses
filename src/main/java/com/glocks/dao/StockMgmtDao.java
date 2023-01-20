package com.glocks.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.glocks.pojo.StockMgmt;
import com.glocks.util.Util;

public class StockMgmtDao {
	static Logger logger = LogManager.getLogger(StockMgmtDao.class);
	static String GENERIC_DATE_FORMAT = "dd-MM-yyyy";

	public StockMgmt getStockByTxnId(Connection conn, String txnId) {
		Statement stmt = null;
		ResultSet rs = null;
		String query = null;
		StockMgmt stockMgmt = new StockMgmt();
		
		try{
			query = "select user_type, role_type from stock_mgmt where txn_id='" + txnId + "'";

			logger.info("Query ["+query+"]");
			 // System.out.println("Query ["+query+"]");
			
			stmt  = conn.createStatement();
			rs = stmt.executeQuery(query);

			if(rs.next()){
				stockMgmt.setUserType(rs.getString("user_type"));
				stockMgmt.setRoleType(rs.getString("role_type"));
			}
		}
		catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		finally{
			try {
				if(rs!=null)
					rs.close();
				if(stmt!=null)
					stmt.close();
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			}			
		}

		return stockMgmt;
	}
	
	public void updateDeleteFlag(Connection conn, String txnId, int deleteFlag) {
		boolean isOracle = conn.toString().contains("oracle");
		String dateFunction = Util.defaultDate(isOracle);

		String query = "update stock_mgmt set delete_flag=? where txn_id=?";
		logger.info("update delete flag in stock_mgmt [" + query + " ]");

		PreparedStatement preparedStatement = null;

		try {
			preparedStatement = conn.prepareStatement(query);

			preparedStatement.setInt(1, deleteFlag);
			preparedStatement.setString(2, txnId);

			logger.info("Query " + preparedStatement);
			preparedStatement.execute();

			logger.info("Update delete flag in  consignment_mgmt succesfully.");

		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		finally{
			try {
				if(Objects.nonNull(preparedStatement))
					preparedStatement.close();
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}
	}

}