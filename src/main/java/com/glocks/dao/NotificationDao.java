package com.glocks.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.glocks.pojo.Notification;
import com.glocks.util.Util;

public class NotificationDao {
	static Logger logger = LogManager.getLogger(NotificationDao.class);
	static String GENERIC_DATE_FORMAT = "dd-MM-yyyy";

	public void insertNotification(Connection conn, Notification notification) {
		boolean isOracle = conn.toString().contains("oracle");
		String dateFunction = Util.defaultDate(isOracle);
		
		String query = "insert into notification ( channel_type, created_on, feature_id, feature_name, "
				+ "message, modified_on, sub_feature, user_id, feature_txn_id, status, "
				+ "retry_count, subject, refer_table, role_type, receiver_user_type" 
				+ ") values (?," + dateFunction + ",?,?,?," + dateFunction + ",?,?,?,0,0,?,'USERS',?,?)";
	
		logger.info("Add notification [ " + query + "]");

		try(PreparedStatement preparedStatement = conn.prepareStatement(query);){
			preparedStatement.setString(1, notification.getChannelType());
			preparedStatement.setLong(2, notification.getFeatureId());
			preparedStatement.setString(3, notification.getFeatureName());	 
			preparedStatement.setString(4, notification.getMessage());
			preparedStatement.setString(5, notification.getSubFeature());
			preparedStatement.setLong(6, notification.getUserId());
			preparedStatement.setString(7, notification.getFeatureTxnId());
			preparedStatement.setString(8, notification.getSubject());
			preparedStatement.setString(9, notification.getRoleType());
			preparedStatement.setString(10, notification.getReceiverUserType());

			 logger.info("Query " + preparedStatement);

			preparedStatement.execute();

			 logger.info("Inserted in notification succesfully.");

		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}
}