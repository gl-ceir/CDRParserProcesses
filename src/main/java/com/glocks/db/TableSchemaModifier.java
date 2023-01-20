package com.glocks.db;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

public class TableSchemaModifier {
	public boolean copySchemaTable( Connection conn, String tableName, String newTableName ){
		boolean result = false;
		String query   = null;
		Statement stmt = null;
		try{
			query  = "create table "+newTableName+" like "+tableName;
			stmt   = conn.createStatement();
			result = stmt.execute(query);
		}catch( Exception ex ){
			result = false;
			ex.printStackTrace();
		}finally{
			try{
				if( conn != null && !conn.isClosed() ){
					if( stmt != null && !((Connection) stmt).isClosed() )
						stmt.close();
				}
			}catch( Exception ex ){}
		}
		return result;
	}
	
	public boolean isReportHaveDayWiseTable( Connection conn, String tableName ){
		boolean result = false;
		String query   = null;
		Statement stmt = null;
		ResultSet rs   = null;
		try{
			query  = "select have_day_wise_table from "+tableName+" where rep_name='"+tableName+"'";
			stmt   = conn.createStatement();
			rs     = stmt.executeQuery(query);
			while( rs.next() ){
				if( rs.getString("have_day_wise_table").equalsIgnoreCase("yes") ){
					result = true;
				}else{
					result = false;
				}
			}
		}catch( Exception ex ){
			result = false;
			ex.printStackTrace();
		}finally{
			try{
				if( conn != null && !conn.isClosed() ){
					if( rs != null && !((Connection) rs).isClosed() )
						rs.close();
					if( stmt != null && !((Connection) stmt).isClosed() )
						stmt.close();
				}
			}catch( Exception ex ){}
		}
		return result;
	}
	
}
