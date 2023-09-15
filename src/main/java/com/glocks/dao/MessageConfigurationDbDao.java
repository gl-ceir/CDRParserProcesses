package com.glocks.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.glocks.pojo.MessageConfigurationDb;

public class MessageConfigurationDbDao {
	static Logger logger = LogManager.getLogger(MessageConfigurationDbDao.class);
	static String GENERIC_DATE_FORMAT = "dd-MM-yyyy";

    public Optional<MessageConfigurationDb> getMessageDbTag(Connection conn, String tag, String appdbName) {
		ResultSet rs = null;
		Statement stmt = null;
		String query = "select id, description, tag, value, channel, active, subject "
				+ "from " + appdbName + ".msg_cfg where tag='" + tag + "'";

		logger.info("Query ["+query+"]");
		 // System.out.println("Query ["+query+"]");
		
		try{
			stmt  = conn.createStatement();
			rs = stmt.executeQuery(query);

			if(rs.next()){
				return Optional.of(new MessageConfigurationDb(rs.getLong("id"), rs.getString("tag"), 
						rs.getString("value"), rs.getString("description"), rs.getInt("channel"), 
						rs.getString("subject")));
			}
		}
		catch(Exception e){
			logger.info(e.getMessage(), e);
			e.printStackTrace();
		}
		finally{
			try {
				if(Objects.nonNull(stmt))
                 rs.close();
					stmt.close();
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}
		
		return Optional.empty();
	}
}
