package com.glocks.parser;

import com.glocks.files.FileList;
import java.io.File;
import java.sql.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ParserMain {

     static Logger logger = LogManager.getLogger(ParserMain.class);

     public static void main(String args[]) {
          Connection conn = null;
          if (args.length < 0) {
               printHelp();
          }
          if (args.length > 0) {
               logger.info("ParserMain.class " + args[0]);
               HexFileReader hfr = new HexFileReader();
               String basePath = "";
               String intermPath = "";
               String filePath = "";
               String fileName = null;
               String source = null;
               String tableName = args[0];
               logger.info("...." + tableName);
//               int raw_upload_set_no = 1;
               try {
                    conn = new com.glocks.db.MySQLConnection().getConnection();
                    CEIRParserMain ceir_parser_main = new CEIRParserMain();
//                    ResultSet my_result_set = ceir_parser_main.operatorDetails(conn, tableName);
//                    if (my_result_set.next()) {
//                         raw_upload_set_no = my_result_set.getInt("raw_upload_set_no");
//                    }
                    basePath = hfr.getFilePath(conn, "smart_file_path");
                    if (!basePath.endsWith("/")) {
                         basePath += "/";
                    }
                    intermPath = basePath + "/" + args[0].toLowerCase() + "/";
                    logger.info("intermPath :" + intermPath);
                    source = getFolderNameByOpertor(conn, intermPath, args[0]);   // opt
                    logger.info("source :" + source);
                    filePath = intermPath + source + "output/";
                    fileName = new FileList().readOldestOneFile(filePath);
                    logger.info("FilePath :" + filePath + ";fileName:" + fileName + " ;basePath :" + basePath + ";source : " + source);
                    hfr.readConvertedCSVFile(conn, fileName, args[0], filePath, source);
//                conn.commit();
               } catch (Exception e) {
                    logger.error(" :" + e);
               } finally {
                    try {
                         logger.info(" ..................................................................... " + args[0]);
                         CEIRParserMain.CDRPARSERmain(conn, args[0]);
                    } catch (Exception ex) {
                         logger.error(" :" + ex);
                    }
                    System.exit(0);
               }
          }
     }

     public static void printHelp() {
          System.out.println("java " + ParserMain.class.getSimpleName() + " \"<Operator Name>\" ");
          System.exit(0);
     }

     private static String getFolderNameByOpertor(Connection conn, String intermPath, String opertor) {
          String query = null;
          String mainFolder = null;
          String folderList = null;
          Statement stmt = null;
          ResultSet rs = null;
          File fldr = null;
          try {
               query = "select value from system_configuration_db where tag= '" + opertor + "_folder_list'  ";
               logger.debug("query: " + query);
               stmt = conn.createStatement();
               rs = stmt.executeQuery(query);
               while (rs.next()) {
                    folderList = rs.getString("value");
               }
               stmt.close();
               logger.debug("folderList: " + folderList);
               String folderArr[] = folderList.split(",");
               for (String val : folderArr) {
                    fldr = new File(intermPath + val.trim() + "/output/");
                    logger.debug("fldr : " + fldr);
                    logger.debug("fldr.listFiles().length : " + fldr.listFiles().length);
                    if (fldr.listFiles().length > 0) {
                         mainFolder = val;
                         break;
                    }
               }

          } catch (Exception e) {
               logger.error("Error : " + e);

               e.printStackTrace();
          }
          return mainFolder + "/";
     }

}

//FileList fl = new FileList();
//fileName  = new FileList().getFileName(tableName);
//calendar  = Calendar.getInstance();
//startTime = sdf.format( Calendar.getInstance().getTime() );
//startRow = new Query().getStartIndexFromTable( tableName );
//serial_no = data[data.length-1].substring(0,data[data.length-1].length()-4);
//serial_no = data[data.length-1];
//previousSequence = hfr.getPreviousFileCount( args[1], conn );
//if(Integer.parseInt(serial_no)== Integer.parseInt(previousSequence)+1){
//fileList               = new FileList().fileList( args[0] );
//for( int i = 0; i < fileList.size(); i++ ){
//	startTime = sdf.format( Calendar.getInstance().getTime() );
//    // // System.out.println( "file name is ["+fileList.get(i)[1]+"]+");
//    if( fileList.get(i)[1] != null ){
//   //     hfr.readBinaryFileUsingDIS( fileList.get(i)[0], fileList.get(i)[1], tableName );
//    }
//}
//hfr.sortAllFile( args[1], filePath );
//endTime       = sdf.format( Calendar.getInstance().getTime() );
//fileList      = new FileList().fileList( args[0] );
//if( fileList.size() > 0 ){
//	for( int i = 0; i < fileList.size(); i++ ){
//    	startTime = sdf.format( Calendar.getInstance().getTime() );
//        if( fileList.get(i)[1] != null ){
//        	rawDataResult = hfr.readConvertedCSVFile( conn, fileList.get(i)[0], fileList.get(i)[1], args[0],basePath,raw_upload_set_no);
//        	endTime = sdf.format( Calendar.getInstance().getTime() );
//            if( rawDataResult != null ){
//            	if( rawDataResult[1] != null && rawDataResult[2] != null ){
//            		result  = logWriter.writeLog( fileList.get(i)[0], fileList.get(i)[2], rawDataResult[0], startTime, endTime, rawDataResult[1], rawDataResult[2], rawDataResult[5], rawDataResult[6],rawDataResult[3], rawDataResult[4]);
//            	}else{
//            		break;
//            	}
//            }else{
//            	result = logWriter.writeLog( fileList.get(i)[0], fileList.get(i)[2],"0", startTime, endTime, "null", "null", "0", "0","0",rawDataResult[4]);
//            	break;
//            }
//        }
//	}
//}else{
//	 // System.out.println("No sorted file found");
//	System.exit(0);
//}
//if( requestType.equalsIgnoreCase("test")){
//try{
//	new HexFileReader().getFields( );
//}catch( Exception e ){
//	e.printStackTrace();
//}	
//}
//else if(requestType.equalsIgnoreCase("update")){}else{
// // System.out.println("No request supported");
//}
