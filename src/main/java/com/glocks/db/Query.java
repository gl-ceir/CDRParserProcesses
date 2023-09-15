package com.glocks.db;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;
import java.util.LinkedHashMap;
 

public class Query{
	
 
	public String isTableContainsDateField( String[] columnNames ){
		String result   = null;
		String columnId = null;
		try{
			for( int i = 0; i < columnNames.length; i++ ){
				if( columnNames[i].toLowerCase().contains( "date" ) ){
					columnId = columnNames[i];
					break;
				}else{
					columnId = null;
				}
			}
			result = columnId;
		}catch( Exception e ){
			e.printStackTrace();
			result = null;
		}
		 // System.out.println("Date column name is ["+result+"]");
		return result;
	}
	
	public void createDataTable(String query){
		boolean retVal  = false;
		Connection conn = null;
		Statement st    = null;
		int numOfEffRec = 0;
		try{
			 // System.out.println("Query to create table is ["+query+"].");
		//	conn = new MySQLConnection().getConnection();
			st   = conn.createStatement();
			numOfEffRec = st.executeUpdate(query);
			if(numOfEffRec > 0)
				retVal = true;
			else
				retVal = false;
		}catch(Exception e){
			e.printStackTrace();
			retVal = false;
		}
		finally{
			try{
				if(conn != null ){
					if(st != null)
						st.close();
					conn.close();
				}
			}catch(Exception ex){}
			
		}
		 // System.out.println();
	}
	public boolean isTableAllReadyPresent(String tableName){
		boolean retVal  = false;
		Connection conn = null;
		ResultSet rs    = null;
		try{
                    //conn                 = new MySQLConnection().getConnection();
			DatabaseMetaData dmd = conn.getMetaData();
			rs                   = dmd.getTables(null, null, "+tableName+" , null);
			if(!rs.next()){
				 // System.out.println("Create table ["+tableName+"].");
				retVal = false;
			}else{
				 // System.out.println("Table ["+tableName+"] is all ready exist.");
				retVal = true;
			}
			
		}catch(Exception e){
			e.printStackTrace();
			retVal = true;
		}
		finally{
			try{
				if(conn != null){
					if(rs != null)
						rs.close();
					conn.close();
				}
			}catch(Exception ex){}
		}
		return retVal;
	}

	public boolean insert( Connection conn, String query ){
		//Connection conn = null;
		Statement stmt  = null;
		int numOfEffRec = 0;
		boolean retVal  = false;

		try{
			// // System.out.println( "Query to run is ["+query+"]" );
			//conn = new MySQLConnection().getConnection();
			if(conn == null)
				 // System.out.println("Connection is not working.");
			stmt = conn.createStatement();
			numOfEffRec = stmt.executeUpdate( query );
			if( numOfEffRec > 0 ){
				 // conn.commit();
				 // System.out.println( "Data inserted successfully..." );
				retVal = true;
			}else{
				conn.rollback();
				 // System.out.println( "Data insertion failed..." );
				retVal = false;
			}
		}catch(Exception e){
			e.printStackTrace();
			retVal = false;
		}finally{
			if ( conn != null ){
				try{
					if ( stmt != null )
						stmt.close();

					//conn.close();

				}catch(Exception e){
				}
			}
		}
		return retVal;
	}

	public boolean update( Connection conn, String query ){
		return this.insert( conn ,query );
	}
	
	public boolean isTableEmpty(String tableName){
		boolean result  = false;
		Connection conn = null;
		Statement stmt  = null;
		ResultSet rs    = null;
		String query    = null;
		try{
			query = "select count(*) as cnt from `"+tableName+"`";
                    //conn  = new MySQLConnection().getConnection();
			stmt  = conn.createStatement();
			rs    = stmt.executeQuery(query);
			while(rs.next()){
				if(rs.getInt("cnt") > 0){
					result = false;
				}else{
					result = true;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			result = true;
		}finally{
			if ( conn != null ){
				try{
					if ( rs != null )
						rs.close();

					if ( stmt != null )
						stmt.close();

					conn.close();

				}catch(Exception e){
				}
			}
		}
		// // System.out.println("result of isTableEmpty function:"+result);
		return result;
	}
	
	public String getLastDateTimeFromTable(String tableName){
		String result   = null;
		String query    = null;
		Connection conn = null;
		Statement stmt  = null;
		ResultSet rs    = null;
		String date     = null;
		try{
			query = "select `date` from `"+tableName+"` order by `date` desc limit 1";
                         // System.out.println(".. getLastDateTimeFromTable .. qry is ;;"+ query);
		//	conn  = new MySQLConnection().getConnection();
			stmt  = conn.createStatement();
			rs    = stmt.executeQuery(query);
			if(rs != null){
				while(rs.next()){
					date = rs.getString("date");
				}
				result = date;
			}else{
				result = null;
			}
		}catch(Exception e){
			e.printStackTrace();
			result = null;
		}finally{
			if ( conn != null ){
				try{
					if ( rs != null )
						rs.close();

					if ( stmt != null )
						stmt.close();

					conn.close();

				}catch(Exception e){
				}
			}
		}
		 // System.out.println("result of getLastDateTimeFromTable function :"+result);
		return result;
	}
	
	public LinkedHashMap<String, Double> getolumnNameAverage(String repName){
		int i                 = 0;
		int j                 = 0;
		int columnCount       = 0;
		int numOfRec          = 0;
		int thresholdRowLimit = 0;   
		int currentRowNum    = 0;
		String qry           = "select ";
		Connection conn      = null;
		Statement stmt       = null;
		ResultSet rs         = null;
		String[] columnNames = null;
		double[] sumOfColumn = null;
		double columnValue   = 0.0;
		String columnVal     = null;
		
		LinkedHashMap<String, Double> lhm = new LinkedHashMap<String, Double>();
		
		try{
			columnNames   = this.getTableColumnsName(repName);
			for(String str : columnNames){
				qry = qry + str+",";
			}
			qry  = ((qry.substring(0, qry.lastIndexOf(","))).trim() + " from "+repName).trim();
			 // System.out.println("Query for getting column values is ["+qry+"]");
			//conn = new MySQLConnection().getConnection();
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs   = stmt.executeQuery(qry);
			if(rs != null){
				rs.last();
				numOfRec   = rs.getRow();
				rs.beforeFirst();
			}
			columnCount = rs.getMetaData().getColumnCount();
			if(numOfRec != 0 && numOfRec > 30){
				thresholdRowLimit = 30;
			}else{
				thresholdRowLimit = numOfRec;
			}
			sumOfColumn    = new double[columnCount];
			sumOfColumn[0] = 0;
			 // System.out.println("Threshold limit for report is ["+thresholdRowLimit+"]");
			while(rs.next() && currentRowNum < thresholdRowLimit){
				for( int k = 1; k < columnNames.length; k++ ){
					columnVal   = rs.getString(columnNames[k]).trim();
					// // System.out.println("Column value for column ["+columnNames[k]+"] is ["+columnVal+"]");
					if(columnVal.contains("%")){
						columnVal   = columnVal.substring( 0, columnVal.indexOf("%"));
						 // System.out.println("Column value for column ["+columnVal+"] after removing % is ["+columnVal+"]");
						columnValue = Double.parseDouble(columnVal);
					}else if(columnVal.contains(" ")){
						columnVal   = columnVal.substring( 0, columnVal.indexOf(" "));
						 // System.out.println("Column value for column ["+columnNames[k]+"] after removing space is ["+columnVal+"]");
						columnValue = Double.parseDouble(columnVal);
					}else{
						 // System.out.println("Normal column value ["+columnVal+"]");
						columnValue = Double.parseDouble( columnVal );
					}
					sumOfColumn[k] = sumOfColumn[k] + columnValue;
				}
				currentRowNum++;
			}
			for( i = 0; i < sumOfColumn.length; i++){
				 // System.out.println("Row count is ["+thresholdRowLimit+"]");
				 // System.out.println("Average of column ["+columnNames[i]+"] is ["+sumOfColumn[i]/thresholdRowLimit+"]");
				lhm.put(columnNames[i], sumOfColumn[i]/thresholdRowLimit);
			}
		}catch(Exception e){
			e.printStackTrace();
			lhm = null;
		}finally{
			if ( conn != null ){
				try{
					if ( rs != null )
						rs.close();

					if ( stmt != null )
						stmt.close();

					conn.close();

				}catch(Exception e){
				}
			}
		}
		
		return lhm;
	}
	
	public String[] getTableColumnsName(String repName){
		//int i                  = 0;
		String qry             = null;
		Connection conn        = null;
		Statement stmt         = null;
		ResultSet rs           = null;
		ResultSetMetaData rsmd = null;
		String[] columnName    = null;
		String[] columnNames   = null;
		
		try{
			
			qry  = "select * from "+repName;
                    //conn = new MySQLConnection().getConnection();
			stmt = conn.createStatement();
			rs   = stmt.executeQuery(qry);
			rsmd  = rs.getMetaData();
			columnName = new String[rsmd.getColumnCount()-1];
			for(int i = 0; i < columnName.length; i++){
				columnName[i] = rsmd.getColumnName(i+2);
				 // System.out.println("Column Name is ["+columnName[i]+"].");
			}
			columnNames = columnName;
		}catch(Exception e){
			e.printStackTrace();
			columnName = null;
		}finally{
			if ( conn != null ){
				try{
					if ( rs != null )
						rs.close();

					if ( stmt != null )
						stmt.close();

					conn.close();

				}catch(Exception e){
				}
			}
		}
		
		return columnName;
	}
	
	public int getStartIndexFromTable(Connection conn, String repName){
		int result      = 0;
		String query    = null;
		//Connection conn = null;
		Statement stmt  = null;
		ResultSet rs    = null;
		try{
			query = "select MAX(`sno`) as sno from `"+repName+"`";
                    //conn  = new MySQLConnection().getConnection();
			stmt  = conn.createStatement();
			rs    = stmt.executeQuery(query);
			while(rs.next()){
				result = rs.getInt("sno");	
			}
		}catch(Exception e){
			e.printStackTrace();
			result = 0;
		}finally{
			try{
				if(conn != null){
					if(rs != null)
						rs.close();
					//conn.close();
				}
			}catch(Exception ex){}
		}
		return result;
	}
	
	public String getReportNameByReportId( int repId ){
		String result   = null;
		String repName  = null;
		String query    = null;
		Connection conn = null;
		Statement stmt  = null;
		ResultSet rs    = null;
		try{
			query = "select `rep_name` from `rep_info` where rep_id ="+repId;
                    //	conn  = new MySQLConnection().getConnection();
			stmt  = conn.createStatement();
			rs    = stmt.executeQuery(query);
			while(rs.next()){
				repName = rs.getString("rep_name");	
			}
			result = repName;
		}catch( Exception e ){
			result = null;
			e.printStackTrace();
		}finally{
			try{
				if(conn != null){
					if(rs != null)
						rs.close();
					conn.close();
				}
			}catch(Exception ex){}
		}
		return result;
	}
	
	public String getReportIdByReportName( String repName ){
		String result   = null;
		String repId  = null;
		String query    = null;
		Connection conn = null;
		Statement stmt  = null;
		ResultSet rs    = null;
		try{
			query = "select `rep_name` from `rep_info` where rep_name ='"+repName+"'";
                    //conn  = new MySQLConnection().getConnection();
			stmt  = conn.createStatement();
			rs    = stmt.executeQuery(query);
			while(rs.next()){
				repId = rs.getString("rep_id");	
			}
			result = repId;
		}catch( Exception e ){
			result = null;
			e.printStackTrace();
		}finally{
			try{
				if(conn != null){
					if(rs != null)
						rs.close();
					conn.close();
				}
			}catch(Exception ex){}
		}
		return result;
	}

}
