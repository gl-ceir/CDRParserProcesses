package com.glocks.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.glocks.util.Util;

public class ConsignmentMgmtDao {
	static Logger logger = LogManager.getLogger(ConsignmentMgmtDao.class);
	static String GENERIC_DATE_FORMAT = "dd-MM-yyyy";

	public void updateConsignmentMgmtDeleteFlag(Connection conn, String txnId, int deleteFlag) {
		boolean isOracle = conn.toString().contains("oracle");
		String dateFunction = Util.defaultDate(isOracle);

		String query = "update consignment_mgmt set delete_flag=? where txn_id=?";
		logger.info("update delete flag in  consignment_mgmt [" + query + " ]");

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
		}
		finally{
			try {
				if(Objects.nonNull(preparedStatement))
					preparedStatement.close();
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

}
