package com.glocks.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.glocks.pojo.UserWithProfile;

public class UserWithProfileDao {
	static Logger logger = LogManager.getLogger(UserWithProfileDao.class);
	static String GENERIC_DATE_FORMAT = "dd-MM-yyyy";

	public UserWithProfile getUserWithProfileById(Connection conn, Long userId) {
		Statement stmt = null;
		ResultSet rs = null;
		String query = null;

		try{
			query = "select users.id as id, user_profile.first_name as first_name, "
					+ "usertype.usertype_name as usertype_name "
					+ "from users "
					+ "inner join user_profile on users.id=user_profile.userid "
					+ "inner join usertype on users.usertype_id=usertype.id " 
					+ "where users.id=" + userId;

			logger.info("Query ["+query+"]"); 
			stmt  = conn.createStatement();
			rs = stmt.executeQuery(query);

			if(rs.next()){
				return new UserWithProfile(rs.getLong("id"), rs.getString("first_name"), rs.getString("usertype_name"));
			}
		}
		catch(Exception e){
			logger.info("Exception in getFeatureMapping"+e);
			e.printStackTrace();
		}
		finally{
			try {
				if(rs!=null)
					rs.close();
				if(stmt!=null)
					stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}

		return null;
	}
}
