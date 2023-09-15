package com.glocks.parser;

import com.glocks.util.Util;
import com.glocks.zte.ZTEFields;

import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.text.SimpleDateFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;

import java.io.DataInputStream;
import java.io.IOException;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class HexFileReader {

     // static Logger logger = LogManager.getLogger(HexFileReader.class);
     // logger = LogManager.getLogger(HexFileReader.class);
     static StackTraceElement l = new Exception().getStackTrace()[0];
//   logger.error("" + l.getClassName()+"/"+l.getMethodName()+":"+l.getLineNumber()  + e);
     Logger logger = LogManager.getLogger(HexFileReader.class);
     private static final boolean String = false;
     ZTEFields zte = new ZTEFields();
     String[] fields = zte.zteCDRFields;

     public String getFields() {
          String retVal = "{\"ColumnNames\":[";
          String columnNames = "";
          try {
               for (String column : fields) {
                    columnNames = columnNames + "{\"colName\":\"" + column + "\"},";
               }
               retVal = retVal + columnNames.substring(0, columnNames.length() - 1) + "]}";
          } catch (Exception e) {
               retVal = "{\"ColumnNames\":[{\"colName\":\"400\"},{\"colName\":\"No file found.\"}]}";
               e.printStackTrace();
          }
          logger.info(retVal);
          return retVal;
     }

     public boolean insertDataIntoRaw(String repName, HashMap<String, ArrayList<String>> fileData) throws SQLException {
          int limit = 10000;
          boolean result = false;
          String query = null;
          String values = "values(";
          Connection conn = null;
          PreparedStatement ps = null;
          ResultSet rs = null;
          try {
               query = "insert into " + repName + "(";
               for (String field : fields) {
                    query = query + field + ",";
                    values = values + "?,";
               }
               query = query.substring(0, query.length() - 1) + ") " + values.substring(0, values.length() - 1) + ")";
               conn = new com.glocks.db.MySQLConnection().getConnection();
               ps = conn.prepareStatement(query);
               for (int j = 0; j < fileData.get(fields[0]).size(); j++) {
                    for (int i = 0; i < fields.length; i++) {
                         ps.setString(i + 1, fileData.get(fields[i]).get(j));
                    }
                    ps.addBatch();
                    if ((j % limit) == 0 && j != 0) {
                         ps.executeBatch();
                         logger.info("Data insert is [" + j + "]");
                    } else if (j == (fileData.get(fields[0]).size() - 1)) {
                         ps.executeBatch();
                         logger.info("Remaining data inserted is [" + j + "]");
                    }
               }
               conn.commit();
               result = true;
          } catch (Exception e) {
               logger.error("Failed to insert data.");
               conn.rollback();
               logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
               e.printStackTrace();
          } finally {
               try {
                    if (conn != null) {
                         if (rs != null) {
                              rs.close();
                         }
                         if (ps != null) {
                              ps.close();
                         }
//                    c onn.close();
                    }
               } catch (Exception e) {
                    logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
               }
          }
          return result;
     }

     public void readConvertedCSVFile(Connection conn, String fileName, String repName /*oprtator NAme */, String fileFolderPath, String source) {
          int total_error_record_count = 0;
          int k = 0;
          int rowInserted = 0;
          String query = null;
          String failquery = null;
          String values = "values(";
          String failvalues = "values(";
          PreparedStatement ps = null;
          PreparedStatement failed_ps = null;
          PreparedStatement temPS = null;
          ResultSet rs = null;

          String line = null;
          String[] data = null;
          BufferedReader br = null;
          File file = null;
          FileReader fr = null;
          SimpleDateFormat actF = null;
          SimpleDateFormat sdf = null;
          HashMap<String, int[]> hm = new HashMap<String, int[]>();
          int failed_flag = 1;
          String updatetime = null;
          try {
               DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
               Date p1StartTime = new Date();
               String usertype_name = "";
               ArrayList<CDRColumn> myfilelist = getCDRFields(conn, "CDR", usertype_name);
               logger.info("file list size is " + myfilelist.size());
               actF = new SimpleDateFormat("yyyyMMddHHmmss"); // actF = new SimpleDateFormat("yyyyMMddHHmmss");
               sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            String[] fileArray11 = fileName.split("_");
//            String str1 = fileName.substring(0, fileName.length() - 10);
//            String str2 = str1.substring(str1.length() - 14, str1.length());
//            LocalDateTime now = LocalDateTime.now();  
//            updatetime = sdf.format("20200531123456");
               updatetime = "2020-05-30 12:34:56";
               file = new File(fileFolderPath + fileName);
               fr = new FileReader(file);
               br = new BufferedReader(fr);

               query = "insert into " + repName + "_raw" + "(";
               failquery = "insert into " + repName + "_error" + "(";
               hm = zte.getfieldSet();

               int fail_my_batch = 0;
               int pass_my_batch = 0;
               int my_batch_count = 1;
               String toDate = " ? ";
               if (conn.toString().contains("oracle")) {
                    toDate = " TO_DATE(?,'yyyy/mm/dd hh24:mi:ss') ";
               }
               boolean isOracle = conn.toString().contains("oracle");
               String dateFunction = Util.defaultDate(isOracle);
               while ((line = br.readLine()) != null) {
                    data = line.split(",", -1);
                    if (k == 0) {
                         logger.debug(" data length in file is " + data.length + ".. field List(DB) size is " + myfilelist.size());
                         if (data.length == myfilelist.size()) {
                              logger.debug("Configured Column name and File Headers are matched");
                              int my_column_count = 0;
                              for (CDRColumn cdrColumn : myfilelist) {
                                   if ((cdrColumn.columName).trim().equals(data[my_column_count].trim())) {
                                        logger.debug("Column name matched");
                                        my_column_count++;
                                        query = query + cdrColumn.columName + ",";
                                        values = values + "?,";
                                        failquery = failquery + cdrColumn.columName + ",";   //
                                        failvalues = failvalues + " ?, ";                           //
                                   }

                              }
                              if (my_column_count == myfilelist.size()) {
                                   query = query + "operator" + "," + "file_name" + "," + "record_time" + "," + "status" + ", created_on,modified_on   ";
                                   values = values + "?,?, " + toDate + " ,'Init'," + dateFunction + ", " + dateFunction + "  ";
                                   query = query.substring(0, query.length() - 1) + ") "
                                           + values.substring(0, values.length() - 1) + ")";
                                   ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                                   failquery = failquery + "operator" + "," + "file_name" + "," + "record_time" + "," + "status" + ", created_on,modified_on   ";     // why not comma 
                                   failvalues = failvalues + "?,?, " + toDate + ",'Error'," + dateFunction + ", " + dateFunction + "   ";   // 
                                   failquery = failquery.substring(0, failquery.length() - 1) + ") "
                                           + failvalues.substring(0, failvalues.length() - 1) + ")";
                                   failed_ps = conn.prepareStatement(failquery, Statement.RETURN_GENERATED_KEYS);
                                   logger.debug("complete query is [" + query + "]");
                                   logger.debug("complete error " + failquery);
                              } else {
                                   // logger.info("getting error in file so moving the file ");
                                   fr.close();
                                   new com.glocks.files.FileList().moveCDRFile(conn, fileName, repName, fileFolderPath, source);
                                   logger.warn("Total column are not matched" + my_column_count);
                                   break;
                              }
                         } else {
                              logger.warn("getting error in file so moving the file ");
                              fr.close();
                              logger.warn("Configured Comumn nad File headers are not matched");
                              new com.glocks.files.FileList().moveCDRFile(conn, fileName, repName, fileFolderPath, source);
                         }
                    } else {    // k = 0 End
                         int j = 1;               ///  
                         failed_flag = 1;
                         for (CDRColumn cdrColumn : myfilelist) {
                              logger.debug(j + "   <-- FIELD -> " + data[j - 1]);
                              if (cdrColumn.graceType.equalsIgnoreCase("Mandatory")) {
                                   logger.debug(j + "   <--Mandatory field -> " + data[j - 1]);
                                   if (data[j - 1].equals("") || " ".equals(data[j - 1]) || data[j - 1].equals(" ") || data[j - 1] == null) {
                                        logger.debug(" No data " + data[j - 1]);
                                        failed_flag = 0;
                                   }
//                             j++;
                              }
                              j++;
                         }
                         j = 1;

                         if (failed_flag == 1) {
                              for (; j <= data.length; j++) {
                                   ps.setString(j, data[j - 1].trim());
                              }
                              ps.setString(j, repName);
                              ps.setString(j + 1, fileName);
                              ps.setString(j + 2, updatetime);
//                        logger.info(" query Pass+ ");
                              ps.addBatch();
                              pass_my_batch++;
                         }
                         if (failed_flag == 0) {
                              total_error_record_count++;
//                        logger.info("Fail before  For  " + data.length);
                              for (; j <= data.length; j++) {
//                            logger.info("inside For:::    " + j + " :: " + data[j - 1].trim());
                                   failed_ps.setString(j, data[j - 1].trim());
                              }
                              failed_ps.setString(j, repName);
                              failed_ps.setString(j + 1, fileName);
                              failed_ps.setString(j + 2, updatetime);
                              failed_ps.addBatch();
                              fail_my_batch++;
                         }
                    }
                    k++;
                    if (pass_my_batch == my_batch_count) {
                         logger.debug("Executing Pass batch..... " + k);
                         ps.executeBatch();
                         pass_my_batch = 0;
                         logger.debug("..... " + k);
                         conn.commit();
                    }
                    if (fail_my_batch == my_batch_count) {
                         logger.debug("Executing Fail batch.." + k);
                         failed_ps.executeBatch();
                         fail_my_batch = 0;
                         conn.commit();
                    }
                    logger.info(k);
               }    // while End

               logger.info("File Finished ");
               ps.executeBatch();
               failed_ps.executeBatch();
               conn.commit();
               Date p1EndTime = new Date();
               source = source.replace("_output", "");

               cdrFileDetailsInsert(conn, dateFunction, fileName, repName, k - 1, total_error_record_count, p1StartTime, p1EndTime, source);  //total_records_count

               //            conn.commit();
               if (fr != null) {
                    fr.close();
               }

               rowInserted = ps.getUpdateCount();
               rs = ps.getGeneratedKeys();
               logger.debug("sys insert close ");
               new com.glocks.files.FileList().moveCDRFile(conn, fileName, repName, fileFolderPath, source);

          } catch (Exception e) {
               logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
               e.printStackTrace();
               try {
                    if (conn != null) {
                         conn.rollback();
                    }
               } catch (Exception ex) {
                    logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
               }

          } finally {
               try {
                    if (conn != null) {
                         if (rs != null) {
                              rs.close();
                         }
                         if (ps != null) {
                              ps.clearParameters();
                              ps.close();
                         }
                    }

               } catch (Exception e) {
                    logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
               }
//            
          }
//        return result;
     }

     public String[] readConvertedFeatureFile(Connection conn, String fileName, String filePath, String main_type, String basePath, String txn_id, String subfeature, String management_table, String usertype_name) throws IOException, SQLException {
          int i = 0;
          int k = 0;
          String query = null;
          String values = "values(";
          boolean isOracle = conn.toString().contains("oracle");
          String dateFunction = Util.defaultDate(isOracle);
          PreparedStatement ps = null;
          ResultSet rs = null;
          File file = null;
          String line = null;
          String[] data = null;
          BufferedReader br = null;
          FileWriter fw = null;
          FileReader fr = null;
          String[] result = null;
          SimpleDateFormat actF = null;
          SimpleDateFormat sdf = null;
          DataInputStream dis = null;
          FileInputStream fis = null;
          int conVal = 0;
          int totalBlnkLine = 0;
          int ooo9Break = 0;
          int failed_flag = 1;
          String my_column_name = "";
          ErrorFileGenrator errFile = new ErrorFileGenrator();
          Set<String> set = new HashSet<String>();
          Set<String> hash_Set = new HashSet<String>();
          HashMap<String, String> msgConfig = new HashMap<String, String>();
          ArrayList<String> fileLines = new ArrayList<String>();
          try {
               String SNofDeviceValue = null;
               CEIRFeatureFileFunctions ceirfunction = new CEIRFeatureFileFunctions();
               List<String> sourceTacList = new ArrayList<String>();
               ArrayList<CDRColumn> myfilelist = getCDRFields(conn, main_type, usertype_name);
               logger.debug("file list size is " + myfilelist.size());
               logger.info("File Name is " + fileName);
               Date date = new Date();
               SimpleDateFormat DateFor = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
               String stringDate = DateFor.format(date);
               String P1date = stringDate;
               System.out.println(stringDate);
               actF = new SimpleDateFormat("yyyyMddHHmmssSS"); // actF = new SimpleDateFormat("yyyyMMddHHmmss");
               sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//             String[] fileArray = fileName.split("_");
               Statement st5 = conn.createStatement();
               String qry = " select quantity, device_quantity from  " + main_type.trim().toLowerCase() + "_mgmt where txn_id  = '" + txn_id + "'";
               if (main_type.equalsIgnoreCase("Stolen") || main_type.equalsIgnoreCase("Recovery")
                       || main_type.equalsIgnoreCase("Block") || main_type.equalsIgnoreCase("Unblock")) {
                    qry = "  select  quantity, device_quantity from stolenand_recovery_mgmt  where txn_id  = '" + txn_id + "'  ";
               }
               new FeatureForSingleStolenBlock().deleteFromRawTable(conn, txn_id, main_type);
               ResultSet resul5 = st5.executeQuery(qry);
               logger.info("query for quantity .." + qry);
               long fileCnt = 0;
               int deviceQuantity = 0;
               try {
                    while (resul5.next()) {
                         fileCnt = resul5.getInt("quantity");
                         deviceQuantity = resul5.getInt("device_quantity");
                    }
               } catch (Exception e) {
                    logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
                    logger.info("Error at file Count Quality  " + e);
               }
               st5.close();
               file = new File(filePath);
               fr = new FileReader(file);
               br = new BufferedReader(fr);
               query = "insert into " + main_type + "_raw" + "( ";
               int pass_my_batch = 0;
               int cnt = 0;
               int my_batch_count = 10;   //  raw_upload_set_no1 but tbl removed
               List aList = new ArrayList();
               List aList1 = new ArrayList();
               List alst = new ArrayList();
               String errorString = " , ";
               Statement stmt2 = conn.createStatement();
               ResultSet rsult = stmt2.executeQuery("select tag ,value from message_configuration_db");
               try {
                    while (rsult.next()) {
                         msgConfig.put(rsult.getString("tag"), rsult.getString("value"));
                    }
               } catch (Exception e) {
                    logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
               }
               rsult.close();
               String interpQury = " select interp from system_config_list_db where tag =  ";

               ResultSet result1 = stmt2.executeQuery(interpQury + " 'DEVICE_TYPE'");
               Set<String> deviceType = new HashSet<String>();
               try {
                    while (result1.next()) {
                         deviceType.add(result1.getString("interp").toLowerCase());
                    }
               } catch (Exception e) {
                    logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
               }
               stmt2.close();
               Statement stmt3 = conn.createStatement();
               ResultSet result3 = stmt3.executeQuery(interpQury + " 'DEVICE_ID_TYPE' ");
               Set<String> deviceType3 = new HashSet<String>();
               try {
                    while (result3.next()) {
                         deviceType3.add(result3.getString("interp").toLowerCase());
                    }
               } catch (Exception e) {
                    logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
                    logger.info("Error at DEVICE_ID_TYPE " + e);
               }
               stmt3.close();
               Statement stmt4 = conn.createStatement();
               ResultSet result4 = stmt4.executeQuery(interpQury + " 'MULTI_SIM_STATUS' ");
               Set<String> deviceType4 = new HashSet<String>();
               try {
                    while (result4.next()) {
                         deviceType4.add(result4.getString("interp").toLowerCase());
                    }
               } catch (Exception e) {
                    logger.info("Error at MULTI_SIM_STATUS " + e);
               }
               stmt4.close();
               Statement stmt5 = conn.createStatement();
               ResultSet result5 = stmt5.executeQuery(interpQury + " 'DEVICE_STATUS'");
               Set<String> deviceType5 = new HashSet<String>();
               try {
                    while (result5.next()) {
                         deviceType5.add(result5.getString(1).toLowerCase());
                    }
               } catch (Exception e) {
                    logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
               }
               stmt5.close();
               String errorFilePath = CEIRFeatureFileParser.getErrorFilePath(conn);
               String error_file_path = errorFilePath + txn_id + "/" + txn_id + "_error.csv";
               logger.info("error_file_path " + error_file_path);
               while ((line = br.readLine()) != null) {
                    data = line.split(",", -1);
                    logger.info("line is " + line + "  line length " + line.trim().length());

                    if (line.replace(",", " ").trim().length() > 0) {
                         errorString = "";
                         if (k == 0) {    // check header
                              if (data.length == 1) {
                                   logger.info(" File is corrupted " + data.toString() + "  ... " + line);
                                   errFile.gotoErrorFile(conn, txn_id, "   Error Code :CON_FILE_0010, Error Message: The file is corrupt.    ");
                                   failed_flag = 0;
                                   fr.close();/////////
                                   break;
                              }

                              logger.info(" Check for File Column Empty ");
                              for (int fldCount = 0; fldCount < data.length; fldCount++) {
                                   if (data[fldCount].equals("") || data[fldCount] == "") {
                                        logger.info(" File Column is Empty ");
                                        errFile.gotoErrorFile(conn, txn_id, "    Error Code :CON_FILE_0009, Error Message: The header is not found in the file.    "); /////////
                                        ooo9Break = 1;
                                        failed_flag = 0;
                                        fr.close();
                                        break;
                                   } else {
                                        alst.add(data[fldCount]);
                                   }
                              }
                              if (ooo9Break == 1) {
                                   failed_flag = 0;
                                   break;
                              }
                              logger.info(" Check for Configured Column name match with File Headers ");

                              logger.info("data.length " + data.length);
                              logger.info(" myfilelist.size()" + myfilelist.size());

                              if (data.length == myfilelist.size()) {

                                   logger.info("Configured Column name and File Headers are matched");
                                   int my_column_count = 0;

                                   for (CDRColumn cdrColumn : myfilelist) {
                                        my_column_name = data[my_column_count].trim();
                                        my_column_name = my_column_name.replaceAll(" ", "");
                                        my_column_name = my_column_name.replaceAll("_", "");
                                        my_column_name = my_column_name.replaceAll(" ", "");
                                        my_column_name = my_column_name.replaceAll("/", "");
                                        logger.info(cdrColumn.columName + " file column " + data[my_column_count].trim());

                                        if (cdrColumn.columName.equalsIgnoreCase("SNofDevice")) {
                                             SNofDeviceValue = cdrColumn.graceType;
                                             logger.info(cdrColumn.columName + " //SNofDeviceValue...." + SNofDeviceValue);

                                        }

                                        if ((cdrColumn.columName).trim().equalsIgnoreCase(my_column_name)) {
                                             logger.info("column name matchedd...." + my_column_name);
                                             my_column_count++;
                                        } else {
                                             logger.info("column name  NOTT matchedd ");
                                             logger.info(alst.toArray());

//                                             if (!alst.contains(my_column_name)) {      //  aug5
                                             failed_flag = 0;
                                             logger.info("Column name not matched");
                                             errFile.gotoErrorFile(conn, txn_id, "  Error Code :CON_FILE_0001, Error Message: The header name in the file is not correct   "); /////////
                                             conVal = 1;
                                             break;
//                                             }
                                        }
                                   }
                                   logger.info("Total column mtch check ");
                                   if (my_column_count == myfilelist.size()) {
                                   } else {
                                        failed_flag = 0;
                                        fr.close();
                                        if (conVal == 0) {
                                             errFile.gotoErrorFile(conn, txn_id, "    Error Code :CON_FILE_0002, Error Message: The header in the file is not in correct order.     "); /////////
                                             logger.info("Total column are not matched" + my_column_count);
                                        }
                                        break;
                                   }
                              } else {
                                   errFile.gotoErrorFile(conn, txn_id, "  Error Code :CON_FILE_0011, Error Message: The Rows contain more Column than allowed in the header.    "); /////////
                                   failed_flag = 0;
                                   fr.close();
                                   break;
                              }
                              fileLines.add(line.toString() + ",Error Code , Error Message");
                         } else {
                              logger.info("FILE AFTER HEADER");
                              int j = 1;           //
                              logger.info(" DATA " + data[j - 1]);
                              String[] arrOfFile = line.trim().split(",", 8);
                              String imeiV = arrOfFile[4];
                              hash_Set.add(arrOfFile[3].trim());
                              sourceTacList.add(arrOfFile[3].trim());

                              if (arrOfFile.length != 7) {
                                   logger.info("errfor   Wrok set.." + imeiV);
                                   errorString += "   Error Code :CON_FILE_0011, Error Message: The Rows contain more Column than allowed in the header.";
                                   failed_flag = 0; /// added after
                              }
//                              logger.debug("***");
                              for (int v = 0; v < data.length; v++) {
//                                   if (data[v].length() > 25) {
                                   if (data[v].length() > 50) {
                                        errorString += " Error Code :CON_FILE_0004, Error Message:   File Contain a Long Field  Record , ";
                                        failed_flag = 0;
                                   }
                              }
                              logger.debug("!!!");
                              //       if (set.add(imeiV) == false) {  // nov12

                              logger.debug("@@@");
                              if (!(deviceType.contains(data[0].trim().toLowerCase()))) {
                                   errorString += "  Error Code :CON_FILE_0006, Error Message:  The field value(Device Type) is not as per the specifications,";
                                   failed_flag = 0;
                              }

                              if (!(deviceType3.contains(data[1].trim().toLowerCase()))) {
                                   errorString += "  Error Code :CON_FILE_0006, Error Message:  The field value(Device ID Type) is not as per the specifications,";
                                   failed_flag = 0;
                              }

                              int minDvcTypeIdLength = 14;
                                   if (data[1].trim().equalsIgnoreCase("esn")) {
                                        minDvcTypeIdLength = 8;
                                   }
                                 if (imeiV.length() < minDvcTypeIdLength) {
                                        logger.info(" Lenth is Less.." + imeiV);
                                        errorString += "   Error Code :CON_FILE_0019, Error Message:   The imei_esn_meid is not as per specifications,";
                                        failed_flag = 0; /// added after
                                   }
                              
                              try {
                                   logger.debug("###");
                                   if (set.add(imeiV.substring(0, minDvcTypeIdLength)) == false) {
                                        logger.info("errfor First Wrok set.." + imeiV);
                                        errorString += "   Error Code :CON_FILE_0008, Error Message:   The record is duplicate in the file,";
                                        failed_flag = 0; /// added after
                                   }
                              } catch (Exception e) {
//                                   logger.debug("Excpt e minDvcTypeIdLength  " + e);
                              }

                              logger.debug("###");
                              if (!(deviceType4.contains(data[2].trim().toLowerCase()))) {
                                   errorString += "  Error Code :CON_FILE_0006, Error Message:  The field value(Multiple Sim Status) is not as per the specifications,";
                                   failed_flag = 0;
                              }
                              logger.debug("$$$");

                              if (!(deviceType5.contains(data[6].trim().toLowerCase()))) {
                                   errorString += "  Error Code :CON_FILE_0006, Error Message:  The field value(Device Status) is not as per the specifications,";
                                   failed_flag = 0;
                              }
                              logger.debug("^^^");

                              boolean val = validateJavaDate(data[5]);
                              if (!val) {
                                   errorString += "  Error Code :CON_FILE_0006, Error Message:  The field value(Device Launch Date) is not as per the specifications,";
                                   failed_flag = 0;
                              }
                              logger.debug("&&&");

                              for (CDRColumn cdrColumn : myfilelist) {
                                   if (cdrColumn.graceType.equalsIgnoreCase("Mandatory")) {
                                        logger.debug("DATA in field ... " + data[j - 1]);
                                        if (data[j - 1].equals("") || data[j - 1] == " " || data[j - 1].equals(" ") || data[j - 1] == null) {
                                             logger.debug("2 mandorty are their. .remove one ");
                                             errorString += "   Error Code :CON_FILE_0007, Error Message:   The mandatory parameter does not contain the value. ,";
                                             failed_flag = 0;
                                        }
                                        j++;
                                   }
                                   j++;
                              }
                              logger.info("CON_FILE_0007 All done");
                              fileLines.add(line.toString() + errorString);
                         }
                         k++;
                    } else {
                         totalBlnkLine++;
                    }
                    logger.info("Line Number ..    " + (k + totalBlnkLine));
               }
               int lngth = hash_Set.size();
               logger.info(" Total size of Serial Number ..    " + lngth);
               logger.info(" Total lines with data    " + k);
               logger.info("Total Blank Lines in File " + totalBlnkLine);
               if ((k - 1) != fileCnt) {
                    logger.info("  Quantity  provided doesnot matched with Lines in File  but Error will not see  till now if earlier Error happened ");
                    if (failed_flag != 0) {
                         logger.info("  Quantity  provided doesnot matched with Data  in File  ");
                         //   errFile.gotoErrorFile(txn_id, "  Error Code :CON_FILE_0010, Error Message:  IMEI/ESN/MEID Quantity  does not match with the  count of data records in the uploaded file   ");
                         fileLines.add(" Error Code :CON_FILE_0012, Error Message:  IMEI/ESN/MEID Quantity  does not match with the  count of data records in the uploaded file   ");
                         failed_flag = 0;
                    }
               }
               if (lngth != deviceQuantity) {
                    logger.debug("   Devce Qntity   method started ");
                    if (failed_flag != 0) {
                         logger.info(" Device Quantity   doesnot matched with unique serial number  in File  ");
                         //  errFile.gotoErrorFile(txn_id, "Error Code :CON_FILE_0011, Error Message: Device Quantity does not match with the  count of unique serial number in the uploaded file  ");
                         fileLines.add("Error Code :CON_FILE_0013, Error Message: Device Quantity does not match with the  count of unique serial number in the uploaded file  ");
                         failed_flag = 0;
                    }
               }

               try {
                    if (SNofDeviceValue.equalsIgnoreCase("Mandatory")) {

                         Map<String, Integer> hm = new HashMap<String, Integer>();
                         for (String ii : sourceTacList) {
                              Integer j = hm.get(ii);
                              hm.put(ii, (j == null) ? 1 : j + 1);
                         }
                         for (Map.Entry<String, Integer> val : hm.entrySet()) {
                              // System.out.println("Element " + val.getKey() + " " + "occurs" + ": " + val.getValue() + " times");
                              if (val.getValue() > 4) {
                                   logger.info("Error Code :CON_FILE_0014, Error Message: Serial number " + val.getKey() + "  is assigned to " + val.getValue() + "  imeis. Max(4 imeis to be linked)  ");
                                   fileLines.add("Error Code :CON_FILE_0014, Error Message: Serial number " + val.getKey() + "  is assigned to " + val.getValue() + "  imeis. Max(4 imeis to be linked)  ");
                                   failed_flag = 0;
                              }
                         }

                    }

               } catch (Exception e) {
                    logger.info("Mandatory  " + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() );
               }

               br.close();
               k = 0;
               if (failed_flag == 1) {
                    File errorfile = new File(error_file_path);
                    if (errorfile.exists()) {     // in case of no error   ,,  file is deleted
                         logger.info("Error file already esists ");
                         if (errorfile.delete()) {
                              logger.info("File deleted successfully");
                         } else {
                              logger.debug("Failed to delete the file");
                         }
                    }
                    logger.info("Uploading file"); // uploading data to db ..
                    file = new File(filePath);
                    fr = new FileReader(file);
                    br = new BufferedReader(fr);
                    while ((line = br.readLine()) != null) {
                         data = line.split(",", -1);
                         if (line.replace(",", " ").trim().length() > 0) {
                              if (k == 0) {
                                   if (data.length == myfilelist.size()) {
                                        logger.info("Configured Column name and File Headers are matched");
                                        int my_column_count = 0;
                                        for (CDRColumn cdrColumn : myfilelist) {
                                            if(cdrColumn.columName.equals("IMEI")){cdrColumn.columName = "IMEIESNMEID";}
                                             query = query + cdrColumn.columName + ",";
                                             values = values + "?,";
                                             my_column_count++;
                                        }
                                        logger.info( " Inner Query : " + query);
                                                
                                        if (my_column_count == myfilelist.size()) {
                                             query = query + "txn_id" + "," + "file_name" + "," + "feature_type" + ",created_on ,modified_on    ";   // removin comma may14
                                             values = values + "?,?,?,?,?  ";
                                             query = query.substring(0, query.length() - 1) + ") "
                                                     + values.substring(0, values.length() - 1) + ")";
                                             ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                                             logger.info("complete query is [" + query + "]");
                                        } else {
                                             fr.close();
                                             logger.info("Total column are not matched" + my_column_count);
                                             break;
                                        }
                                   }

                              } else {
                                   int j = 1;
                                   if (failed_flag == 1) {
                                        if (data[0].equals("") && data[1].equals("") && data[2].equals("") && data[3].equals("")
                                                && data[4].equals("") && data[5].equals("") && data[6].equals("")) {
                                             logger.info("err..   BLNK FILE   ,, just skip it");
                                             continue;
                                        }
                                        String imeiV = data[4].trim();
                                        logger.info("imeiV;;" + imeiV);
                                        if (aList.contains(imeiV)) {
                                             logger.debug("err. for list,,." + imeiV);
                                        } else {
                                             aList.add(imeiV);
                                             for (; j <= data.length; j++) {
                                                  logger.debug("DATA at setString " + data[j - 1].trim());
                                                  ps.setString(j, data[j - 1].trim());
                                             }
                                             ps.setString(j, txn_id);
                                             ps.setString(j + 1, fileName);
                                             ps.setString(j + 2, main_type);
                                             ps.setString(j + 3, stringDate);
                                             ps.setString(j + 4, stringDate);
                                             logger.debug(fileName + main_type + stringDate + txn_id);
                                             ps.addBatch();
                                             pass_my_batch++;

                                        }
                                   }
                              }
                              k++;
                              logger.info("counting :  " + k);
                              if (pass_my_batch == my_batch_count) {
                                   logger.debug("Executing Pass Batch File");
                                   ps.executeBatch();
//                            conn.commit();
                                   pass_my_batch = 0;
                              }
                         }
                    }      // While close 

                    br.close();
                    logger.debug("Batch started");
                    if (ps != null) {
                         ps.executeBatch();
                    }
                    if (fr != null) {
                         fr.close();
                    }
                    logger.debug("Batch ended");
                    rs = ps.getGeneratedKeys();
                    if (rs != null) {
                         rs.close();
                    }
                    logger.debug("Batch endsed");
                    if (ps != null) {
                         ps.clearParameters();
                         ps.close();
                    }
//                    if (main_type.equalsIgnoreCase("Consignment")) {
//                         ceirfunction.UpdateStatus ViaApi(conn, txn_id, 0, main_type);
//                    } else {
//                         ceirfunction.updateFeatureManagementStatus(conn, txn_id, 1, management_table, main_type);      // 1 for processing
//                         logger.info("mgmt Db  1  done");
//                    }

                    ceirfunction.updateFeatureFileStatus(conn, txn_id, 2, main_type, subfeature); // update web_action_db 
                    logger.debug("web_action_db 2 done");
//                    conn.commit();
               } else {
                    errFile.gotoErrorFilewithList(errorFilePath, txn_id, fileLines);
//                    if (main_type.equalsIgnoreCase("Consignment")) {
                    ceirfunction.UpdateStatusViaApi(conn, txn_id, 1, main_type);
//                    } else {
                    ceirfunction.updateFeatureManagementStatus(conn, txn_id, 2, management_table, main_type);
//                    }
                    ceirfunction.updateFeatureFileStatus(conn, txn_id, 4, main_type, subfeature); // update web_action_db  
               }
          } catch (Exception e) {
               logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
               e.printStackTrace();
               try {
                    if (conn != null) {
                         conn.rollback();
                    }
                    new ErrorFileGenrator().gotoErrorFile(conn, txn_id, "Something went Wrong. Please Contact to Ceir Admin.  ");
                    new CEIRFeatureFileFunctions().UpdateStatusViaApi(conn, txn_id, 1, main_type);       //1 for reject
                    new CEIRFeatureFileFunctions().updateFeatureFileStatus(conn, txn_id, 5, main_type, subfeature); // update web_action_db    
                    conn.commit();
               } catch (Exception ex) {
               }
               result = null;
          } finally {
               try {
                    logger.debug("in Finally Block");
                    if (Objects.nonNull(conn)) {
                         if (Objects.nonNull(rs)) {
                              rs.close();
                         }
//                         logger.debug("1212");
//                         if (Objects.nonNull(ps)) {
//                              ps.clearParameters();
//                              ps.close();
//                         }
                         logger.debug("Committed Conntion ");
                         conn.commit();

                    }
                    if (fis != null) {
                         fis.close();
                    }
                    if (dis != null) {
                         dis.close();
                    }
                    if (fw != null) {
                         fw.close();
                    }
               } catch (Exception e) {
                    logger.error("._." + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
               }
               query = null;
               values = null;
               data = null;
               file = null;

          }
          return result;
     }

     public String[] readConvertedCSVFileBack(Connection conn, String fileName, String filePath, String repName) {
          String errorFilePath = filePath + fileName + ".error";
          Boolean inTrkStatus = false;
          String cdrCount = null;
          String answerTime = null;
          String endTime = null;
          String inTrkName = null;
          String outTrkName = null;
          String inTrkNo = null;
          String outTrkNo = null;
          String partId = null;
          String billId = null;
          int sCount = 0;
          int fCount = 0;
          int startId = 0;
          int endId = 0;
          int endRow = 0;
          int i = 0;
          int limit = 10000;
          int startRow = 0;
          int rowInserted = 0;
          String query = null;
          String values = "values(";
          // Connection conn = null;
          PreparedStatement ps = null;
          PreparedStatement temPS = null;
          ResultSet rs = null;
          String cdrStartTime = null;
          String cdrEndTime = null;
          String cdrTime = null;
          String changeCDRTime = null;
          String fieldName = null;
          File file = null;
          String line = null;
          String str = null;
          String answerId = null;
          String startTime = null;
          String preCDRTime = null;
          String[] data = null;
          BufferedReader br = null;
          FileWriter fw = null;
          FileReader fr = null;
          String[] result = null;
          Date date = null;
          SimpleDateFormat actF = null;
          SimpleDateFormat sdf = null;
          DataInputStream dis = null;
          FileInputStream fis = null;
          ArrayList<String> billIds = null;
          List<String> fieldList = null;
          HashMap<String, int[]> hm = new HashMap<String, int[]>();
          int fieldValue = 0;
          String recordtype = null;
          String imei = null;
          String imsi = null;
          String msisdn = null;
          String systemtype = null;
          logger.info("in file reader");
          try {
               fieldList = new ArrayList<String>();
               logger.info("NOTEEEEEEEEE  (readConvertedCSVFileBack)>>>>>>>>>>  main_type, txn_id  is hard coded   but usertype_name is not used ****************************");
               String main_type = "";
               String txn_id = "";
               String usertype_name = "";

               // String usertype_name = getUserTypeName(conn, main_type, txn_id); // get
               // usertypr .. importor/ ditributer /
               ArrayList filelist = getCDRFields(conn, "CDR", usertype_name);
               logger.info("array list " + filelist.toString());
               fieldList = Arrays.asList(fields);

               date = new Date();
               actF = new SimpleDateFormat("yyyyMMddHHmmssSS"); // actF = new SimpleDateFormat("yyyyMMddHHmmss");
               sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
               // fw = new FileWriter(errorFilePath);
               file = new File(filePath);
               fr = new FileReader(file);
               br = new BufferedReader(fr);
               // startTime = sdf.format(Calendar.getInstance().getTime());
               // startRow = new com.functionapps.db.Query().getStartIndexFromTable(conn,
               // repName);
               /**
                * **************Insert Query and prepared statement start**************
                */
               query = "insert into " + repName + "(";
               for (String field : fields) {
                    query = query + field + ",";
                    values = values + "?,";
               }
               query = query.substring(0, query.length() - 1) + ") " + values.substring(0, values.length() - 1) + ")";
               logger.info("query is " + query);

               // conn = new com.functionapps.db.MySQLConnection().getConnection();
               ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
               // temPS = conn.prepareStatement( "insert into
               // zte_billid_temp(partId,billId,answerTime) values(?,?,?)",
               // Statement.RETURN_GENERATED_KEYS );
               /**
                * **************Insert Query and prepared statement ends**************
                */
               hm = zte.getfieldSet();
               billIds = new ArrayList<String>();

               // preCDRTime = this.getPreviousTimeFromRaw(repName, conn);
               while ((line = br.readLine()) != null) {
                    data = line.split(",", -1);
                    recordtype = data[0];
                    imei = data[1];
                    imsi = data[2];
                    msisdn = data[3];
                    systemtype = data[4];

                    if (i == 0) {
                         // query = "insert into "+repName+"(";
                         // for( String field : fields ){
                         // query = query + field + ",";
                         // values = values + "?,";
                         //
                         // }
                         // query = query.substring( 0, query.length() - 1 )+") "+values.substring( 0,
                         // values.length() - 1 )+")";
                    } else {
                         ps.setString(1, recordtype);
                         ps.setString(2, imei);
                         ps.setString(3, imsi);
                         ps.setString(4, msisdn);
                         ps.setString(5, systemtype);
                         ps.addBatch();
                    }
                    i++;
               }
               logger.info("cdrStartTIme [" + cdrStartTime + "], cdrEndTime [" + cdrEndTime + "]");
               ps.executeBatch();
               if (fr != null) {
                    fr.close();
               }
               rowInserted = ps.getUpdateCount();
               rs = ps.getGeneratedKeys();
               while (rs.next()) {
                    endRow = (int) rs.getLong(1);
                    rowInserted = endRow - startRow;
               }

               if (rs != null) {
                    rs.close();
               }
               // logger.info("cdr start row is ["+startRow+"] and end row is ["+endRow+"]");
               // this.updateCDRWithPartIdThree( conn, billIds, repName, cdrStartTime,
               // startRow, endRow );
               // startId = new Query().getStartIndexFromTable(conn,"zte_billid_temp");
               // temPS.executeBatch();
               // rs = temPS.getGeneratedKeys();
               // while( rs.next() ){
               // endId = (int)rs.getLong(1);
               // }
               // if( endId != 0 ){
               // this.insertSlotStartAndEndSnoId( conn, cdrStartTime, startId, endId);
               // }
               // if( temPS != null ){
               // temPS.clearParameters();
               // }

               new com.glocks.files.FileList().moveFile(fileName, repName, "", "");

               // cdrCount = String.valueOf(i+1);
               if (cdrCount != null && cdrStartTime != null && cdrEndTime != null) {
                    // logger.info("CDR start time ["+cdrStartTime+"] and CDR end time
                    // ["+cdrEndTime+"]");
                    // result = new String[]{ cdrCount, cdrStartTime, cdrEndTime,
                    // String.valueOf(rowInserted), this.getTableSize( conn,
                    // repName),String.valueOf(sCount),String.valueOf(fCount) };
               } else {
                    result = new String[]{"0", "null", "null", "0", this.getTableSize(conn, repName), "0", "0"};
               }
               conn.commit();
          } catch (Exception e) {
               e.printStackTrace();
               logger.error("e " + e);
               try {
                    if (conn != null) {
                         conn.rollback();
                    }
               } catch (Exception ex) {
                    logger.error("e " + ex);
               }
               result = null;
               // result = new String[]{ "0", "null", "null", "0", this.getTableSize( conn,
               // repName),"0","0" };
          } finally {
               try {
                    if (conn != null) {
                         if (rs != null) {
                              rs.close();
                         }
                         if (ps != null) {
                              ps.clearParameters();
                              ps.close();
                         }
                         if (temPS != null) {
                              temPS.clearParameters();
                              temPS.close();
                         }
                         // c onn.close();
                    }
                    if (fis != null) {
                         fis.close();
                    }
                    if (dis != null) {
                         dis.close();
                    }
                    if (fw != null) {
                         fw.close();
                    }
               } catch (Exception ex) {
               }
               // errorFilePath = null;
               query = null;
               cdrCount = null;
               answerTime = null;
               endTime = null;
               inTrkName = null;
               outTrkName = null;
               inTrkNo = null;
               outTrkNo = null;
               partId = null;
               billId = null;
               values = null;
               cdrStartTime = null;
               cdrEndTime = null;
               data = null;
               fieldName = null;
               file = null;
               billIds = null;
               hm = null;
          }
          return result;
     }

     ArrayList getCDRFields(Connection conn, String feature, String usertype_name) {
          String query = null;
          int rs0Count = 0;

          ArrayList columnDetails = new ArrayList<CDRColumn>();
          String period = checkGraceStatus(conn); // grace/postgrace
          String qry = ""; //
          try {
               Statement stmto = conn.createStatement();

               ResultSet rs0 = stmto.executeQuery("select count(*)  from static_rule_engine_mapping where feature='"
                       + feature + "' and   user_type =  '" + usertype_name + "'  order by id asc ");
               while (rs0.next()) {
                    rs0Count = rs0.getInt(1);
               }
          } catch (Exception e) {
               logger.error("e " + e);
          }
          logger.info("  ...... .." + rs0Count);

          if (rs0Count == 0) {
               qry = "  and user_type =  'default'  ";
          } else {

               qry = "  and user_type =  '" + usertype_name + "'  ";
          }

          Statement stmt = null;
          ResultSet rs = null;
          try {
               query = "select * from static_rule_engine_mapping where feature='" + feature + "'   " + qry + "   order by id asc    "; /// usertype

               stmt = conn.createStatement();
               rs = stmt.executeQuery(query);
               logger.info("Query is for static rule_engine_mapping (get CDRFields) ..." + query);
               while (rs.next()) {
                    CDRColumn cdrColumn = new CDRColumn(rs.getString("name"), rs.getString(period + "_type"), rs.getString("post_grace_type"));
                    columnDetails.add(cdrColumn);
               }
          } catch (Exception ex) {
               ex.printStackTrace();
               logger.error("e " + ex);
          } finally {
               if (conn != null) {
                    try {
                         if (rs != null) {
                              rs.close();
                         }
                         if (stmt != null) {
                              stmt.close();
                         }
                    } catch (Exception e) {
                         logger.error("e " + e);
                    }
               }
          }

          return columnDetails;
     }

     public String checkGraceStatus(Connection conn) {

          // static Logger logger = LogManager.getLogger(HexFileReader.class);
          // logger = LogManager.getLogger(HexFileReader.class);
          String period = "";
          String query = null;
          ResultSet rs1 = null;
          Statement stmt = null;
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
          Date currentDate = new Date();
          Date graceDate = null;
          try {
               query = "select value from system_configuration_db where tag='GRACE_PERIOD_END_DATE'";
               logger.info("Query is " + query);
               stmt = conn.createStatement();
               rs1 = stmt.executeQuery(query);
               while (rs1.next()) {
                    graceDate = sdf.parse(rs1.getString("value"));
                    if (currentDate.compareTo(graceDate) > 0) {
                         period = "post_grace";
                    } else {
                         period = "grace";
                    }
               }
               logger.info(" Current Period is " + period);
          } catch (Exception ex) {
               ex.printStackTrace();
          } finally {
               try {
                    stmt.close();
                    rs1.close();
               } catch (SQLException e) {

               }

          }
          return period;
     }

     public String getPreviousTimeFromRaw(String repName, Connection conn) {
          String lastDate = null;
          String query = null;
          ResultSet rs = null;
          Statement stmt = null;
          try {
               query = "select Changed_date from " + repName + " where sno=(select MAX(sno) from " + repName + ")";
               stmt = conn.createStatement();
               rs = stmt.executeQuery(query);
               while (rs.next()) {
                    lastDate = rs.getString("Changed_date");
               }
          } catch (Exception ex) {
               lastDate = null;
               ex.printStackTrace();
          } finally {
               if (conn != null) {
                    try {
                         if (rs != null) {
                              rs.close();
                         }
                         if (stmt != null) {
                              stmt.close();
                         }
                    } catch (Exception e) {
                    }
               }
          }
          return lastDate;
     }

//     public String getPreviousFileCounts(String repName, Connection conn) {
//          String seq_no = null;
//          String query = null;
//          ResultSet rs = null;
//          Statement stmt = null;
//          try {
//               query = "select seq_no from re p_schedule where rep_name = '" + repName + "'";
//               logger.info("Query is " + query);
//               stmt = conn.createStatement();
//               rs = stmt.executeQuery(query);
//               while (rs.next()) {
//                    seq_no = rs.getString("seq_no");
//               }
//          } catch (Exception ex) {
//               seq_no = null;
//               ex.printStackTrace();
//          } finally {
//               if (conn != null) {
//                    try {
//                         if (rs != null) {
//                              rs.close();
//                         }
//                         if (stmt != null) {
//                              stmt.close();
//                         }
//                    } catch (Exception e) {
//                    }
//               }
//          }
//          return seq_no;
//     }
     public String getFilePath(Connection conn, String tag_type) {
          String file_path = "";
          String query = null;
          ResultSet rs = null;
          Statement stmt = null;
          try {
               query = "select value from sys_param where tag='" + tag_type + "'";
               stmt = conn.createStatement();
               rs = stmt.executeQuery(query);
               logger.info("to get configuration" + query);
               while (rs.next()) {
                    file_path = rs.getString("value");
                    logger.info("in function file path " + file_path);
               }
          } catch (Exception ex) {
               ex.printStackTrace();
               logger.error("e " + ex);
          } finally {
               try {
                    rs.close();
                    stmt.close();
               } catch (SQLException ex) {
                    logger.error("e " + ex);
               }

          }
          return file_path;

     }

//     public String updateNextCounts(String repName, Connection conn, int seq_no) {
//          String seq_no1 = null;
//          String query = null;
//          ResultSet rs = null;
//          Statement stmt = null;
//          try {
//               query = "update rep _schedule set seq_no='" + seq_no + "' where rep_name = '" + repName + "';";
//               logger.info("Query is " + query);
//               // PreparedStatement pstmt = conn.prepareStatement(query);
//               // pstmt.executeUpdate();
//
//               stmt = conn.createStatement();
//               logger.info("update count are " + stmt.executeUpdate(query));
//               conn.commit();
//
//               // rs = stmt.executeQuery(query);
//               // while( rs.next() ){
//               // seq_no = rs.getString("seq_no");
//               // }
//          } catch (Exception ex) {
//               seq_no1 = null;
//               ex.printStackTrace();
//          } finally {
//               if (conn != null) {
//                    try {
//                         if (rs != null) {
//                              rs.close();
//                         }
//                         if (stmt != null) {
//                              stmt.close();
//                         }
//                         // c onn.close();
//                    } catch (Exception e) {
//                    }
//               }
//          }
//          return seq_no1;
//     }
     public String getTableSize(Connection conn, String tableName) {
          String tableSize = null;
          String query = null;
          // Connection conn = null;
          Statement stmt = null;
          ResultSet rs = null;
          try {
               query = "SELECT round(sum((data_length + index_length) / 1024 / 1024 ), 4) `Size`  FROM information_schema.TABLES where table_name = '"
                       + tableName + "'";
               // conn = conn.getConnection();
               stmt = conn.createStatement();
               rs = stmt.executeQuery(query);

               while (rs.next()) {
                    tableSize = rs.getString("Size");
               }
          } catch (Exception e) {
               e.printStackTrace();
               tableSize = "0";
          } finally {
               if (conn != null) {
                    try {
                         if (rs != null) {
                              rs.close();
                         }
                         if (stmt != null) {
                              stmt.close();
                         }
                         // c onn.close();
                    } catch (Exception e) {
                    }
               }
          }
          return tableSize;
     }

     public boolean updateCDRWithPartIdThree(Connection conn, ArrayList<String> billIds, String repName,
             String startTime, int startSno, int endSno) {
          int i = 0;
          boolean result = false;
          int eStartId = 0;
          int eEndId = 0;
          String query = null;
          String updateQuery = null;
          String tempBillID = null;
          Statement stmt = null;
          ResultSet rs = null;
          PreparedStatement ps = null;
          ArrayList<Integer[]> idDetailsOfCdr = null;
          // HashMap< String, String > tempBillIds = null;
          try {
               idDetailsOfCdr = new ArrayList<Integer[]>();
               idDetailsOfCdr = this.getBillIdDetailByCDRStartTime(conn, startTime);
               if (idDetailsOfCdr != null) {
                    for (Object[] idDetail : idDetailsOfCdr) {
                         if (i == 0) {
                              eStartId = (int) idDetail[0];
                              eEndId = (int) idDetail[1];
                         } else {
                              eEndId = (int) idDetail[1];
                         }
                         i++;
                    }
                    query = "select billID,answerTime from zte_billid_temp where sno >=" + eStartId + " and sno <="
                            + eEndId;
                    logger.info("Query for getting all is :[" + query + "]");
                    stmt = conn.createStatement();
                    rs = stmt.executeQuery(query);
                    updateQuery = "update " + repName
                            + " set AnswerTime=?,cdr_date=? where PartRecID='3' and BillID=? and sno >=" + startSno
                            + " and sno <=" + endSno;
                    // updateQuery = "update "+repName+" set AnswerTime=IF((select answerTime from
                    // zte_billid_temp where partID='1' and billID=?)IS NULL, AnswerTime,
                    // answerTime) where PartRecID='3' and BillID=?";
                    ps = conn.prepareStatement(updateQuery);
                    if (rs != null) {
                         // tempBillIds = new HashMap< String, String >();
                         while (rs.next()) {
                              tempBillID = rs.getString("billID");// rs.getString("answerTime")
                              if (billIds.contains(tempBillID)) {
                                   ps.setString(1, rs.getString("answerTime"));
                                   ps.setString(2, rs.getString("answerTime"));
                                   ps.setString(3, tempBillID);
                                   // logger.info("Update query for BillId ["+tempBillID+"] and AnswerTime
                                   // ["+rs.getString("answerTime")+"] is :["+updateQuery.toString()+"]");
                                   ps.addBatch();
                              }
                         }
                    }
                    ps.executeBatch();
               }
               if (conn != null) {
                    conn.commit();
               }
               // stmt = conn.createStatement();
               // stmt.executeQuery("delete from zte_billid_temp where
               // answerTime<=DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 HOUR)")

          } catch (Exception e) {
               try {
                    if (conn != null) {
                         conn.rollback();
                    }
               } catch (Exception ex) {
               }
               result = false;
               e.printStackTrace();
          } finally {
               try {
                    if (ps != null) {
                         ps.clearParameters();
                         ps.close();
                    }
                    if (rs != null) {
                         rs.close();
                    }
                    if (stmt != null) {
                         stmt.close();
                    }
               } catch (Exception ex) {
               }
          }
          return result;
     }

     public boolean insertSlotStartAndEndSnoId(Connection conn, String cdrStartTime, int eStartId, int eEndId) {
          boolean result = false;
          try {
               result = new com.glocks.db.Query().insert(conn,
                       "insert into zte_id_details(cdr_start_time,e_start_id,e_end_id) values('" + cdrStartTime + "',"
                       + eStartId + "," + eEndId + ")");
          } catch (Exception ex) {
               result = false;
               ex.printStackTrace();
          }
          return result;
     }

     public ArrayList<Integer[]> getBillIdDetailByCDRStartTime(Connection conn, String cdrStartTime) {
          ArrayList<Integer[]> result = null;
          String query = null;
          Statement stmt = null;
          ResultSet rs = null;
          try {
               query = "select e_start_id,e_end_id from zte_id_details where DATE_FORMAT(cdr_start_time,'%Y-%m-%d %H:%i:%s') <= DATE_SUB(FROM_UNIXTIME(FLOOR( UNIX_TIMESTAMP('"
                       + cdrStartTime
                       + "')/300 ) * 300),INTERVAL 15 MINUTE) AND DATE_FORMAT(cdr_start_time,'%Y-%m-%d %H:%i:%s') >= DATE_SUB(FROM_UNIXTIME(FLOOR( UNIX_TIMESTAMP('"
                       + cdrStartTime + "')/300 ) * 300),INTERVAL 2 HOUR)";
               logger.info("Query for getting id range is [" + query + "]");
               stmt = conn.createStatement();
               rs = stmt.executeQuery(query);
               if (rs != null) {
                    result = new ArrayList<Integer[]>();
                    while (rs.next()) {
                         result.add(new Integer[]{rs.getInt("e_start_id"), rs.getInt("e_end_id")});
                    }
               }
          } catch (Exception ex) {
               ex.printStackTrace();
          } finally {
               try {
                    if (rs != null) {
                         rs.close();
                    }
                    if (stmt != null) {
                         stmt.close();
                    }
               } catch (Exception ex) {
               }
          }
          return result;
     }

     public String getDatabaseSize(Connection conn, String dbName) {
          String result = null;
          Statement stmt = null;
          ResultSet rs = null;
          try {
               stmt = conn.createStatement();
               rs = stmt.executeQuery(
                       "SELECT  sum(round(((data_length + index_length) / 1024 / 1024 ), 4))  as size FROM information_schema.TABLES  WHERE table_schema ='"
                       + dbName + "'");
               if (rs != null) {
                    while (rs.next()) {
                         result = rs.getString("size");
                    }
               }
          } catch (Exception e) {
               result = "0";
               e.printStackTrace();
          } finally {
               try {
                    if (rs != null) {
                         rs.close();
                    }
                    if (stmt != null) {
                         stmt.close();
                    }
               } catch (Exception ex) {
               }
          }
          return result;
     }

     public int getFieldIndex(String fieldName) {
          int result = 0;
          List<String> fieldList = null;
          try {
               fieldList = new ArrayList<String>();
               fieldList = Arrays.asList(fields);
               if (fieldList.contains(fieldName)) {
                    result = fieldList.indexOf(fieldName);
               }

          } catch (Exception ex) {
               result = 0;
               ex.printStackTrace();
          }
          return result;
     }

     public boolean validateJavaDate(String strDate) {
          logger.info("date,operator,," + strDate);
          strDate = strDate.replaceAll("/", "-");
          logger.info("date....." + strDate);
          if (strDate.trim().equals("")) {
               return true;
          } else {
               SimpleDateFormat sdfrmt = new SimpleDateFormat("dd-MM-yyyy");
               sdfrmt.setLenient(false);

               try {
                    Date javaDate = sdfrmt.parse(strDate);
                    logger.info(strDate + " is valid date format");
               } catch (Exception e) {
                    logger.info(strDate + " is Invalid Date format");
                    return false;
               }
               return true;
          }
     }

     private String getUserTypeName(Connection conn, String main_type, String txn_id) {
          String usrtype = "";
          String query = null;
          ResultSet rs1 = null;
          Statement stmt = null;
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
          Date currentDate = new Date();
          Date graceDate = null;
          try {
               query = "select  usertype_name  from users inner join usertype on users.usertype_id  = usertype.id inner join "
                       + main_type.trim().toLowerCase() + "_mgmt on user_id =  users.id where txn_id  = '" + txn_id + "'";
               logger.info("Query  (getUserTypeName) is " + query);
               stmt = conn.createStatement();
               rs1 = stmt.executeQuery(query);
               while (rs1.next()) {
                    usrtype = rs1.getString("usertype_name");
               }
               logger.info("  usrtype is " + usrtype);
          } catch (Exception ex) {
               ex.printStackTrace();
          } finally {
               try {
                    stmt.close();
                    rs1.close();
               } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
               }

          }
          return usrtype;
     }

     public void insertSourceTac(Connection conn, String tac, String txnFile, Long tacCount, String dbName) {
          Statement stmt = null;
          logger.info("tacCount " + tacCount);
          String raw_query = "insert into " + dbName + "(tac , TXN_ID, RECORD_COUNT   ) "
                  + "  values('" + tac + "', '" + txnFile + "', " + tacCount + " )";
          try {
               logger.info(" " + raw_query);
               stmt = conn.createStatement();
               stmt.executeUpdate(raw_query);
          } catch (Exception e) {
               logger.warn("  " + e);
          } finally {
               try {
                    stmt.close();
               } catch (SQLException ex) {
                   // java.util.logging.LogManager.getLogger(HexFileReader.class.getName()).log(Level.SEVERE, null, ex);
            	   org.apache.logging.log4j.LogManager.getLogger(HexFileReader.class.getName());
               }
          }

     }

     void cdrFileDetailsInsert(Connection conn, String dateFunction, String fileName, String repName, int total_records_count, int total_error_record_count, Date p1starttime, Date p1endTime, String sourc) {

          Statement stmt = null;

          DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          String raw_query = "insert into cdr_file_details_db(created_on, file_name, operator, total_records_count, total_error_record_count,source   ,   P1StartTime ,P1EndTime    ) "
                  + "  values(current_timestamp ,  '" + fileName + "', '" + repName + "', '" + total_records_count + "','" + total_error_record_count + "','" + sourc + "' , p1starttime,p1endTime  )";
          try {
               logger.info("  cdrFileDetailsInsert is " + raw_query);
               stmt = conn.createStatement();
               stmt.executeUpdate(raw_query);

          } catch (Exception e) {
               logger.warn("  " + e);
          } finally {
               try {
                    stmt.close();
               } catch (SQLException ex) {
                    //java.util.logging.LogManager.getLogger(HexFileReader.class.getName()).log(Level.SEVERE, null, ex);
                    org.apache.logging.log4j.LogManager.getLogger(HexFileReader.class.getName());
               }
          }
     }

     private void checkExtraSerialNo(ArrayList<String> fileLines, int failed_flag, String z, Long v) {
          if (v > 4) {
          }

     }

}

//
//    public void readFeatureWithoutFile(Connection conn, String feature, int raw_upload_set_no, String txn_id,
//            String sub_feature, String mgnt_table_db, String user_type) {
//
//        Statement stmt = null; // stolenand_recovery_mgmt
//        Statement stmt1 = null;
//        Map<String, String> map = new HashMap<String, String>();
//        try {
//              CEIRFeatureFileFunctions ceirfunction = new CEIRFeatureFileFunctions();
//              ceirfunction.u pdateFeatureManagementStatus(conn, txn_id, 1, mgnt_table_db, feature);
//              ceirfunction.updateFeatureFileStatus(conn,txn_id, 2, feature, sub_feature); // update web_action_db    
//             
//            map.put("feature", feature);
//            // map.put("raw_upload_set_no", (String)raw_upload_set_no);
//            map.put("sub_feature", sub_feature);
//            map.put("mgnt_table_db", mgnt_table_db);
//            map.put("user_type", user_type);
//
//            map.put("txn_id", txn_id);
//            map = getstolenandRecoveryDetails(conn, map);
//
//            logger.info("  request_type is " + map.get("request_type"));
//            logger.info("  source_type is " + map.get("source_type"));
//
//            if (map.get("request_type").equals("0") || map.get("request_type").equals("2")) {
//                logger.info("welcome to Stolen/Block flow");
//
//                if (map.get("source_type").equals("4") || map.get("source_type").equals("5")) {
//                    logger.info("welcome to Stolen/Block flow Indidual ");
//                    stolenFlowStartSingle(conn, map);
//                } else {
//                    logger.info("Error is only for Single  Stolen/Block,,Something went wrong ");
//                }
//
//            }
//
//            if (map.get("request_type").equals("1") || map.get("request_type").equals("3")) {
//                logger.info("welcome to Recovery / Unblock Flow");
//                if (map.get("source_type").equals("4") || map.get("source_type").equals("5")) {
//                    logger.info("welcome to Recovery/Unblock flow Single ");
//                    recoverFlowStartSingle(conn, map);
//                } else {
//                    logger.info("Error is only for Single Recovery/Unblock ,,Something went wrong ");
//                }
//            }
//        } catch (Exception e) {
//            logger.info("  Error  is  + " + e);
//
//        } finally {
//            try {
//                // conn.commit();
////                c onn.close();
//            } catch (Exception ex) {
//                logger.info("  Error  is  + " + ex);
//            }
//        }
//    }
//
//    private void recoverFlowStartSingle(Connection conn, Map<java.lang.String, java.lang.String> map) {
//        try {
//            ErrorFileGenrator errFile = new ErrorFileGenrator();
//            String id = map.get("id");
//            String txn_id = map.get("txn_id");
//            logger.info("recoverFlowStartSingle with   id  " + id);
//            for (int i = 1; i < 5; i++) {
//                logger.info(" ging to chck exists imei  ");
//                String resllt = chckOtherExistingImei(conn, map, i);
//
//                if (resllt.equals("PRO")) {
//                    logger.info(" imei exist in sinle_stolenImei  ");
//
//                    String sing_imei = "";
//                    String stln_imei = "";
//
//                    if (i == 1) {
//                        sing_imei = "first_imei";
//                        stln_imei = "imei_esn_meid1";
//                    }
//                    if (i == 2) {
//                        sing_imei = "second_imei";
//                        stln_imei = "imei_esn_meid2";
//                    }
//                    if (i == 3) {
//                        sing_imei = "third_imei";
//                        stln_imei = "imei_esn_meid3";
//                    }
//                    if (i == 4) {
//                        sing_imei = "fourth_imei";
//                        stln_imei = "imei_esn_meid4";
//                    }
//
//                    Statement stmt1 = conn.createStatement();
//                    String qury1 = "";
//                    if (map.get("request_type").equals("1")) {
//                        qury1 = " select " + stln_imei + " from stolen_individual_userdb  where stolen_id  =" + id
//                                + " "; // , model_number, device_brand_name, contact_number
//                    } else {
//                        qury1 = "select " + sing_imei + "  from single_imei_details where txn_id ='" + txn_id + "' ";
//                    }
//                    logger.info(" Query i... " + qury1);
//                    ResultSet res = stmt1.executeQuery(qury1);
//                    String res1 = "0";
//                    try {
//                        while (res.next()) {
//                            res1 = res.getString(1);
//                        }
//                    } catch (Exception e) {
//                        logger.info("Error..getImedn.." + e);
//                    }
//
//                    String valz = res1;
//                    logger.info("Imei<,....." + valz);
//                    map.put("imei_esn_meid", valz);
//                    stmt1.close();
//                    try {
//                        if (valz == "0") {
//                            logger.info("imei Null");
//                            logger.info("File error... IMEI which are provided,  mainSingle in present Database.");
//                            errFile.gotoErrorFile(txn_id, "IMEI which are provided,  not in present Database.");
//                            failstatusUpdator(conn, map);
//                        } else {
//                            insertinRawtable(conn, map);
//                        }
//
//                    } catch (Exception e) {
//                        logger.info("File error at catch  " + e);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            logger.error("  Error  is  + " + e);
//        }
//
//    }
//
//    private String chckOtherExistingImei(Connection conn, Map<String, String> map, int i) {
//        String otpt = "";
//        try {
//
//            Statement stmt = conn.createStatement();
//            ResultSet resultmsdn = null;
//            String id = map.get("id");
//            String txn_id = map.get("txn_id");
//
//            String sing_imei = "";
//            String stln_imei = "";
//
//            if (i == 1) {
//                sing_imei = "first_imei";
//                stln_imei = "imei_esn_meid1";
//            }
//            if (i == 2) {
//                sing_imei = "second_imei";
//                stln_imei = "imei_esn_meid2";
//            }
//            if (i == 3) {
//                sing_imei = "third_imei";
//                stln_imei = "imei_esn_meid3";
//            }
//            if (i == 4) {
//                sing_imei = "fourth_imei";
//                stln_imei = "imei_esn_meid4";
//            }
//
//            String qury1 = "";
//            if (map.get("request_type").equals("1")) {
//                qury1 = " select " + stln_imei + " from stolen_individual_userdb where stolen_id  =" + id + " "; // ,
//                // model_number,
//                // device_brand_name,
//                // contact_number
//            } else {
//                qury1 = "select " + sing_imei + "  from single_imei_details where txn_id ='" + txn_id + "' ";
//            }
//            logger.info(" chckOtherExistingImei quury  " + qury1);
//
//            resultmsdn = stmt.executeQuery(qury1);
//            String res = "";
//            try {
//                while (resultmsdn.next()) {
//                    res = resultmsdn.getString(1);
//                }
//            } catch (Exception e) {
//                logger.info(" Resultset error " + e);
//            }
//            if (res == null || res == "" || res.equals("") || res.equals("0")) {
//                otpt = "NA";
//            } else {
//                otpt = "PRO";
//            }
//            stmt.close();
//            logger.info(" chckOtherExistingImei output  " + otpt);
//        } catch (Exception e) {
//            logger.info(" chckOtherExistingImei error " + e);
//        }
//
//        return otpt;
//    }
//
//    public Map getstolenandRecoveryDetails(Connection conn, Map<String, String> map) {
//        Statement stmt = null;
//        try {
//            ResultSet resultmsdn = null;
//            stmt = conn.createStatement();
//
//            String qury = " select  id,  request_type,  source_type ,file_name ,quantity, user_id from stolenand_recovery_mgmt where txn_id  = '"
//                    + map.get("txn_id") + "'  ";
//            logger.info("qury: +" + qury);
//            resultmsdn = stmt.executeQuery(qury);
//            while (resultmsdn.next()) {
//                map.put("id", resultmsdn.getString("id"));
//                map.put("request_type", resultmsdn.getString("request_type"));
//                map.put("source_type", resultmsdn.getString("source_type"));
//                map.put("file_name", resultmsdn.getString("file_name"));
//                map.put("quantity", resultmsdn.getString("quantity"));
//                map.put("user_id", resultmsdn.getString("user_id"));
//            }
//            resultmsdn.close();
//            stmt.close();
//        } catch (Exception e) {
//            logger.info("Excepton: +" + e);
//        }
//        return map;
//    }
//
//    void stolenFlowStartSingle(Connection conn, Map<java.lang.String, java.lang.String> map) {
//        String id = map.get("id");
//        String txn_id = map.get("txn_id");
//        int cnt = 1;
//        String sing_imei = "";
//        String stln_imei = "";
//        String qury = null;
//        String ty = null;
//        Statement stmt = null;
//        try {
//            logger.info("stolenFlowStart with   id  " + id);
//            for (int i = 1; i < 5; i++) {
//
//                if (i == 1) {
//                    sing_imei = "first_imei";
//                    stln_imei = "imei_esn_meid1";
//                }
//                if (i == 2) {
//                    sing_imei = "second_imei";
//                    stln_imei = "imei_esn_meid2";
//                }
//                if (i == 3) {
//                    sing_imei = "third_imei";
//                    stln_imei = "imei_esn_meid3";
//                }
//                if (i == 4) {
//                    sing_imei = "fourth_imei";
//                    stln_imei = "imei_esn_meid4";
//                }
//
//                if (map.get("request_type").equals("2")) {
//                    qury = "select " + sing_imei
//                            + "    as   imei_esn_meid , second_imei  as model_number,third_imei  as device_brand_name,  fourth_imei  as contact_number from single_imei_details where txn_id ='"                            + txn_id + "'   ";
//                    ty = "BLOCK";
//                }
//                if (map.get("request_type").equals("0")) {
//                    qury = " select " + stln_imei
//                            + "    as imei_esn_meid , model_number, device_brand_name, contact_number from stolen_individual_userdb where stolen_id  =" + id + " ";
//                    ty = "STOLEN";
//                }
//                stmt = conn.createStatement();
//                logger.info("  Flow type " + ty);
//                logger.info(" squery query is.... " + qury);
//                ResultSet resultmsdn = null;
//                resultmsdn = stmt.executeQuery(qury);
//
//                try {
//                    while (resultmsdn.next()) {
//                        map.put("imei_esn_meid", resultmsdn.getString("imei_esn_meid"));
//                        map.put("model_number", resultmsdn.getString("model_number"));
//                        map.put("device_brand_name", resultmsdn.getString("device_brand_name"));
//                        map.put("contact_number", resultmsdn.getString("contact_number"));
//                    }
//                } catch (Exception e) {
//                    logger.info("Error..getImedn.." + e);
//                }
//                stmt.close();
//                // conn.commit();
//                if (i == 1) {
//                    logger.info("start..stolenFlowStartSingleExtended...." + i);
//                    stolenFlowStartSingleExtended(conn, map);
//                }
//                if (i != 1 && !(map.get("imei_esn_meid") == null || map.get("imei_esn_meid").trim() == "" || map.get("imei_esn_meid").trim().equals("") || map.get("imei_esn_meid").equals("0"))) {
//                    logger.info("start..stolenFlowStartSingleExtended.  having  imei..i.e.." + map.get("imei_esn_meid"));
//                    stolenFlowStartSingleExtended(conn, map);
//                } else {
//                    // break;
//                }
//            }
//        } catch (Exception e) {
//            logger.info("Excep: +" + e);
//        }
//
//    }
//
//    void stolenFlowStartSingleExtended(Connection conn, Map<String, String> map) {
//        try {
//            for (Map.Entry<String, String> entry : map.entrySet()) {
//                String key = entry.getKey();
//                String val = entry.getValue();
//                logger.info(" KKKKEEYYYY..." + key + " --->   " + val);
//            }
//            if (map.get("imei_esn_meid") == null) {
//                logger.info(" Action for Null IMEI Started.......");
//
//                String imei = getImeiWithMsisdn(conn, map);
//                logger.info("GETTED IMEI is " + imei);
//                map.put("imei_esn_meid", imei);
//                logger.info("Going to  insert into Raw  after getting imei...... ");
//                insertinRawtable(conn, map);
//
//                for (int i = 2; i <= 4; i++) {
//                    String msisdnothr = getOtherContactsImei(conn, i, map);
//                    if (msisdnothr != null || msisdnothr.trim() == "" || msisdnothr.equals("") || msisdnothr.equals("0")) {
//                        logger.info(" new msisdnothr ...." + msisdnothr);
//                        map.put("contact_number", msisdnothr);
//                        imei = getImeiWithMsisdn(conn, map);
//                        map.put("imei_esn_meid", imei);
//                        logger.info("Going to  insert into Raw after getting imei...... ");
//                        insertinRawtable(conn, map);
//                    }
//                }
//
//            } else {
//                logger.info("Going to insert in RAW with imei..... " + map.get("imei_esn_meid"));
//                insertinRawtable(conn, map);
//            }
//
//        } catch (Exception e) {
//            logger.info("Excep  at  stolenFlowStartSingleExtended: +" + e);
//
//        }
//
//    }
//
//    private String getImeiWithMsisdn(Connection conn, Map<String, String> map)
//            throws ClassNotFoundException, SQLException {
//        ErrorFileGenrator errFile = new ErrorFileGenrator();
//        String imei = "";
//        String txn_id = map.get("txn_id");
//        String lawful_stolen_usage_db_num_days_qury = " select value from  system_configuration_db  where tag  = 'lawful_stolen_usage_db_num_days'";
//        logger.info(" getImeiMsisdn ,,,lawful_stolen_usage_db_num_days_qury,,, " + lawful_stolen_usage_db_num_days_qury);
//        Statement stmt8 = conn.createStatement();
//        ResultSet resultDay = stmt8.executeQuery(lawful_stolen_usage_db_num_days_qury);
//        int days = 0;
//        try {
//            while (resultDay.next()) {
//                days = resultDay.getInt(1);
//            }
//        } catch (Exception e) {
//            logger.info("Error..lawful_stolen_usage_db_num_days_qury.." + e);
//        }
//
//        stmt8.close();
//        DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd"); //
//        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.DATE, -days);
//        String date = dateFormat1.format(cal.getTime());
//        logger.info("Date from which Data is calculated" + date);
//
//        List lst = new ArrayList();
//        List lstGsma = new ArrayList();
//        String msisdn = map.get("contact_number");
//        String strTacs = "Result......";
//        String device_usage_db_qury = " select imei from  device_usage_db  where msisdn = '" + msisdn
//                + "'  and created_on > '" + date + "' ";
//        logger.info(" ImeiWithMsisdn ,,,device_usage_db,,, " + device_usage_db_qury);
//        Statement stmt = conn.createStatement();
//        ResultSet resultmsdn = stmt.executeQuery(device_usage_db_qury);
//        try {
//            while (resultmsdn.next()) {
//                lst.add(resultmsdn.getString(1));
//            }
//        } catch (Exception e) {
//            logger.info("Error..getErrorMsisdn.." + e);
//        }
//        logger.info("List size ..." + lst.size());
//        String device_duplicate_db_qury = " select imei from  device_duplicate_db  where msisdn = '" + msisdn
//                + "'  and created_on > '" + date + "' ";
//        logger.info(" getImeiSMsisdn ,,,device_duplicate_db,,, " + device_duplicate_db_qury);
//        ResultSet result2 = stmt.executeQuery(device_duplicate_db_qury);
//        try {
//            while (result2.next()) {
//                lst.add(result2.getString(1));
//            }
//        } catch (Exception e) {
//            logger.info("Error..getImeithMsisdn   ,,,,  device_duplicate_db.." + e);
//        }
//        logger.info("list SIZE" + lst.size());
//        if (lst.isEmpty()) {
//            logger.info("Empty List");
//            String fileString = "No device Found in device_duplicate_db and device_usage_db  with  msisdn = '" + msisdn
//                    + "' and  Use date after " + date + "";
//
//            errFile.gotoErrorFile(txn_id, fileString);
//            failstatusUpdator(conn, map);
//        } else {
//            logger.info("NOT Empty List");
//            if (lst.size() == 1) {
//                imei = (String) lst.get(0);
//            } else {
//                int column = 1;
//                String device_brand_name = map.get("device_brand_name");
//                String model_number = map.get("model_number");
//                logger.info("device_brand_name*************" + device_brand_name);
//                logger.info("model_number*************" + model_number);
//                int coustion = 0;
//                int resultsiz = 0;
//                for (int ind = 0; ind < lst.size(); ind++) {
//                    String gsma_tac_qury = " select    band_name , model_name  from  gsma_tac_db  where device_id = "
//                            + lst.get(ind).toString().substring(0, 8) + "  ";
//                    logger.info(" Query ,,,,,, " + gsma_tac_qury);
//                    ResultSet result3 = stmt.executeQuery(gsma_tac_qury);
//                    try {
//                        while (result3.next()) {
//                            resultsiz++;
//                            logger.info("Result Startes");
//                            {
//                                strTacs += column + "> Brand Name :" + result3.getString("band_name") + ",Model Name : "
//                                        + result3.getString("model_name") + ",";
//                                if (model_number.equalsIgnoreCase(result3.getString("model_name"))
//                                        && device_brand_name.equalsIgnoreCase(result3.getString("band_name"))) {
//                                    lstGsma.add(lst.get(ind).toString());
//                                    coustion++;
//                                }
//                            }
//                        }
//                        column++;
//                    } catch (Exception e) {
//                        logger.info("Error..gmeiWithMsisdn.." + e);
//                    }
//                }
//                stmt.close();
//                logger.info(" resultsiz listZise" + resultsiz);
//                logger.info(" column of  listZise" + column);
//
//                if (coustion > 1) {
//                    logger.info("  in gsma_tac_db ,,  2 or more values  exists  ");
//                }
//                if (lstGsma.size() == 1) {
//                    imei = (String) lstGsma.get(0);
//                } else {
//                    logger.info(" List Size  in Gsma_tac_db is not valid");
//                    String fileString = strTacs   + "...... NO SIMILAR  Model And Brand Name  FOUND IN Gsma_tac_Db SCHEMA ";
//                    errFile.gotoErrorFile(txn_id, fileString);
//                    failstatusUpdator(conn, map);
//
//                }
//            }
//        }
//
//        return imei;
//    }
//
//    public void insertinRawtable(Connection conn, Map<String, String> map) {
//        String feature = map.get("feature");
//        String mgnt_table_db = map.get("mgnt_table_db");
//        String imei_esn_meid = map.get("imei_esn_meid");
//        String user_type = map.get("user_type");
//        String txn_id = map.get("txn_id");
//        String id = map.get("id");
//        String dateNow = "";
//        String DeviceType = "";
//        String DeviceIdType = "";
//        String MultipleSIMStatus = "";
//        String DeviceSerial = "";
//
//        Statement stmt = null;
//        if (conn.toString().contains("oracle")) {
//            dateNow = new SimpleDateFormat("dd-MMM-YY").format(new Date());
//        } else {
//            dateNow = new SimpleDateFormat("YYYY-MM-dd").format(new Date());
//        }
//
//        if (feature.equalsIgnoreCase("Stolen") || feature.equalsIgnoreCase("Recovery")) {
//            DeviceType = "( select interp from system_config_list_db where tag = 'DEVICE_TYPE' and value  =(select device_type from stolen_individual_userdb where stolen_id  =" + id + " ) )";
//            DeviceIdType = "( select interp from system_config_list_db where tag = 'DEVICE_ID_TYPE' and value  =( select device_id_type from stolen_individual_userdb where stolen_id  =" + id + " ))";
//            MultipleSIMStatus = " (select interp from system_config_list_db where tag = 'MULTI_SIM_STATUS' and value  = (select multi_sim_status  from stolen_individual_userdb where stolen_id  =" + id + " ) )";
//            DeviceSerial = " ( select  device_serial_number from stolen_individual_userdb where stolen_id  =" + id + " )";
//
//        } else {
//            DeviceType = "select interp from system_config_list_db where tag = 'DEVICE_TYPE'  and value  =(select device_type  from single_imei_details where txn_id ='" + txn_id + "' )) ";
//            DeviceIdType = "( select interp from system_config_list_db where tag = 'DEVICE_ID_TYPE' and value  =(select device_id_type   from single_imei_details where txn_id ='" + txn_id + "') )";
//            MultipleSIMStatus = "  (select interp from system_config_list_db where tag = 'MULTI_SIM_STATUS' and value  =(select multiple_sim_status   from single_imei_details where txn_id ='" + txn_id + "') )";
//            DeviceSerial = " ( select  device_serial_number  from single_imei_details where txn_id ='" + txn_id + "' )";
//        }
//
//        String query = "insert into  " + feature + "_raw"
//                + "   (   DeviceType,DeviceIdType ,MultipleSIMStatus,SNofDevice,IMEIESNMEID,Devicelaunchdate,DeviceStatus,status,file_name , txn_id , time ,feature_type)   values  "
//                + " (  " + DeviceType + "  , " + DeviceIdType + " ,  " + MultipleSIMStatus + " ,  " + DeviceSerial
//                + " ,  '" + imei_esn_meid + "' , '', '' , 'Init' ,  'NA' , '" + txn_id + "' ,  '" + dateNow + "'  , '"
//                + feature + "' )   ";
//
//        logger.info(" Query  for insertion... " + query);
//
//        try {
//            stmt = conn.createStatement();
//            stmt.executeUpdate(query);
//            stmt.close();
//            // conn.commit();
//
//        } catch (Exception e) {
//            logger.info(" Error  " + e);
//        }
//
//    }
//
//    void failstatusUpdator(Connection conn, Map<String, String> map) {
//        String tblName = (map.get("feature").equalsIgnoreCase("Stolen")                || map.get("feature").equalsIgnoreCase("Recovery") || map.get("feature").equalsIgnoreCase("Block")                || map.get("feature").equalsIgnoreCase("Unblock")) ? "file" : map.get("feature");
//        String subfeature = map.get("sub_feature");
//        String txn_id = map.get("txn_id");
//        String feature = map.get("feature");
//        String fileName = map.get("feature");
//        String management_table = map.get("mgnt_table_db");
//        CEIRFeatureFileFunctions ceirfunction = new CEIRFeatureFileFunctions();
//        logger.info("main_type Is .." + feature);
////        ceirfunction.addFeatureFileConfigDetails(conn, "update", feature, subfeature, txn_id, fileName,   "PARAM_NOT_VALID", "");
//        ceirfunction.updateFeatureFileStatus(conn, txn_id, 4, feature, subfeature); // update web_action_db set
//        ceirfunction.u pdateFeatureManagementStatus(conn, txn_id, 2, management_table, tblName);
//
//    }
//
//    public String getOtherContactsImei(Connection conn, int i, Map<String, String> map) {
//        String cntctNo = null;
//
//        ResultSet resultmsdn = null;
//        String qury = " select contact_number" + i + "  from stolen_individual_userdb where txn_id = '" + map.get("id") + "'    ";
//        logger.info("getOtherContactsImei qury :" + qury);
//        try {
//            Statement stmt = conn.createStatement();
//            resultmsdn = stmt.executeQuery(qury);
//            try {
//                while (resultmsdn.next()) {
//                    cntctNo = resultmsdn.getString("contact_number");
//                }
//                logger.info("Result at getOtherContactsImei  " + cntctNo);
//            } catch (Exception e) {
//                logger.info("Error..getOtherContactsImei.." + e);
//            }
//            stmt.close();
//
//        } catch (Exception e) {
//            logger.info("Error..getOtherContactsImei ..2.." + e);
//        }
//        return cntctNo;
//    }
//
//	

/*
 * public boolean readHexdumpFile( String path, String fileName ){ int i = 0;
 * int lineLimit = 300; char[] buffSize = new char[392]; byte[] buffSize1 = new
 * byte[392]; boolean result = false; //File file = null; BufferedReader br =
 * null; StringBuffer sb = new StringBuffer(); String line = null; Process
 * newProcess = null; DecimalConverter dc = new DecimalConverter(); try{ //br =
 * new BufferedReader( new FileReader(path+fileName)); newProcess =
 * Runtime.getRuntime().exec( "hexdump "+path+fileName ); br = new
 * BufferedReader( new InputStreamReader( newProcess.getInputStream() )); //br =
 * new BufferedReader( new FileInputStream(path+fileName)); br.read(buffSize);
 * sb.append(buffSize,2,0); logger.info(dc.hex2Decimal(sb.toString()));
 * br.close(); }catch( Exception e ){ e.printStackTrace(); result = false; }
 * return result; }
 */
 /*
 * public HashMap< String, ArrayList< String > > readBinaryFileUsingDIS( String
 * fileName, String filePath, String repName ){ int i = 0; int offset = 0; int
 * numRead = 0; //boolean result = false; int limit = 10000; //boolean result =
 * false; String query = null; String values = "values("; Connection conn =
 * null; PreparedStatement ps = null; ResultSet rs = null; String cdrConsider =
 * null; String cdrStartTime = null; String drEndTime = null; String data =
 * null; String fieldName = null; File file = null; FileInputStream fis = null;
 * DataInputStream dis = null; byte[] buffer = null; //String line = null; int[]
 * fieldOffset = null; DecimalConverter dc = new DecimalConverter(); ArrayList<
 * String > temp = null; HashMap< String, int[] > hm = new HashMap< String,
 * int[] >(); //HashMap< String, String > fieldValues = new HashMap< String,
 * String >(); HashMap< String, ArrayList< String > > result = new HashMap<
 * String, ArrayList< String > >(); HashMap< String, ArrayList< String > >
 * fieldValues = new HashMap< String, ArrayList< String > >(); try{ query =
 * "insert into "+repName+"("; for( String field : fields ){ query = query +
 * field + ","; values = values + "?,"; } query = query.substring( 0,
 * query.length() - 1 )+") "+values.substring( 0, values.length() - 1 )+")";
 * conn = new com.functionapps.db.MySQLConnection().getConnection(); ps =
 * conn.prepareStatement( query ); buffer = new byte[392]; file = new
 * File(filePath); fis = new FileInputStream(file); dis = new DataInputStream(
 * fis ); hm = zte.getfieldSet(); //i = dis.read( buffer ); while( ( offset <
 * buffer.length ) && (numRead=dis.read(buffer, offset, buffer.length-offset))
 * >= 0 ){ for( int j = 0; j < fields.length; j++ ){ data = null; temp = new
 * ArrayList< String >(); fieldOffset = new int[3]; fieldName = fields[j]; if(
 * !fieldName.equals("cdr_condsider") ){ fieldOffset = hm.get(fieldName); byte[]
 * byteData = null; if( i != 0 ){ temp = fieldValues.get( fieldName ); } switch(
 * fieldOffset[2] ){ case 0 : byteData = Arrays.copyOfRange( buffer,
 * fieldOffset[0], fieldOffset[0]+fieldOffset[1]); data =
 * dc.hex2Decimal(dc.bytesToHex(byteData)); temp.add( data ); break; case 1 :
 * 
 * byteData = Arrays.copyOfRange( buffer, fieldOffset[0],
 * fieldOffset[0]+fieldOffset[1]);
 * //logger.info("FieldName:["+fieldName+"],Offset["+fieldOffset[0]+
 * "],EndIndex["+fieldOffset[0]+fieldOffset[1]+"] and Hex ["+dc.bytesToHex(
 * byteData)+"]"); data = dc.getNumberFromBCD(dc.bytesToHex(byteData));
 * temp.add( data ); break; case 2 : byteData = Arrays.copyOfRange( buffer,
 * fieldOffset[0], fieldOffset[0]+fieldOffset[1]); data =
 * dc.getNumberFromRightBCD(dc.bytesToHex(byteData)); temp.add( data ); break;
 * case 3: data = dc.getBitFromByte( buffer[fieldOffset[0]], fieldOffset[1]);
 * temp.add( data );
 * //logger.info("Original data:"+fieldOffset[1]+", Type 3 data offset:"+(int)
 * fieldOffset[1]);
 * //logger.info("byte:"+buffer[131]+",bits:"+Integer.toBinaryString( (int)
 * buffer[131])); break; case 4: byteData = Arrays.copyOfRange( buffer,
 * fieldOffset[0], fieldOffset[0]+fieldOffset[1]); data = dc.getStringFromByte(
 * byteData ); temp.add( data );
 * //logger.info("Original data:"+fieldOffset[1]+", Type 3 data offset:"+(int)
 * fieldOffset[1]);
 * //logger.info("byte:"+buffer[131]+",bits:"+Integer.toBinaryString( (int)
 * buffer[131])); break; default : break; } if( fieldName.equals("EndTime") &&
 * data != null || !data.equals("NULL") || !data.equals("0") || !data.equals("")
 * ){ cdrConsider = "Y"; if( i == 0 ){ cdrStartTime = data; } }else{ cdrConsider
 * = "N"; } ps.setString( j+1, data); }else{ ps.setString( j+1, data);
 * temp.add(cdrConsider); } if( i == 0 ){ fieldValues.put( fieldName, temp ); }
 * } i++; } result = fieldValues; new
 * com.functionapps.files.FileList().moveFile(fileName, repName);
 * //logger.info(fieldValues.toString()); }catch( Exception e ){
 * e.printStackTrace(); result = null; }finally{ try{ if(conn != null){ if(rs !=
 * null) rs.close(); if( ps != null ) ps.close(); c onn.close(); } if( fis !=
 * null ) fis.close(); if( dis != null ) dis.close(); }catch( Exception ex ){} }
 * return result; }
 */
//long lineCount = 0;
//try {
//	Path path = Paths.get(filePath);
//	lineCount = Files.lines(path).count();
//
//	if (lineCount == 1) {
//		logger.info(" CON_FILE_0003	The uploaded file is blank.");
//		errFile.gotoErrorFile(txn_id, "    Error Code :CON_FILE_0003, Error Message: The uploaded file is blank.    ");                                   /////////
//	}
//}catch(Exception e) {
//	logger.info("this can be happen asfile cAn be curropt..."+ e);
//	crptBrk = 1;
//}
//if ((lineCount - 1) != fileCnt) {
//	logger.info("  Quantity  provided doesnot matched with Lines in File  ");
//	errFile.gotoErrorFile(txn_id, "    Error Code :CON_FILE_0010, Error Message: Quantity  provided doesnot matched with Lines in File    ");                                   /////////
//	failed_flag = 0;
////	fr.close();
////	break;
//}
//    public String[] readBinaryFileUsingDIS(String fileName, String filePath, String repName) {
//        String csvFilePath = "/home/ildidea/upload/" + repName + "/";
//        int i = 0;
//        int offset = 0;
//        int numRead = 0;
//        int sCalls = 0;
//        int fCalls = 0;
//        String cdrSeqNo = null;
//        String callDuraExt = null;
//        String cdrStartTime = null;
//        String cdrEndTime = null;
//        String endTime = null;
//        String data = null;
//        String fieldName = null;
//        String answerId = null;
//        String callDuration = null;
//        String partId = null;
//        File file = null;
//        FileInputStream fis = null;
//        DataInputStream dis = null;
//        byte[] buffer = null;
//        // String line = null;
//        int[] fieldOffset = null;
//        String[] result = null;
//        StringBuilder sbCsv = null;
//        FileWriter csvFileWriter = null;
//        DecimalConverter dc = new DecimalConverter();
//        HashMap<String, int[]> hm = new HashMap<String, int[]>();
//
//        try {
//             
//            buffer = new byte[392];
//            file = new File(filePath);
//            fis = new FileInputStream(file);
//            dis = new DataInputStream(fis);
//            hm = zte.getfieldSet();
//            sbCsv = new StringBuilder();
//            csvFileWriter = new FileWriter(csvFilePath + fileName + ".csv");
//             while ((offset < buffer.length) && (numRead = dis.read(buffer, offset, buffer.length - offset)) >= 0) {
//                for (int j = 0; j < fields.length; j++) {
//                    data = null;
//                    fieldOffset = new int[3];
//                    fieldName = fields[j];
//                    if (!fieldName.equals("cdr_condsider") && !fieldName.equals("CallDurationInSec")
//                            && !fieldName.equals("Changed_date") && !fieldName.equals("cdr_date")) {
//                        fieldOffset = hm.get(fieldName);
//                        // logger.info("Field name ["+fieldName+"]");
//                        byte[] byteData = null;
//                        switch (fieldOffset[2]) {
//                            case 0:
//                                // byteData = Arrays.copyOfRange( buffer, fieldOffset[0],
//                                // fieldOffset[0]+fieldOffset[1]);
//                                data = dc.hex2Decimal(dc.bytesToHex(byteData));
//                                break;
//                            case 1:
//                                // byteData = Arrays.copyOfRange( buffer, fieldOffset[0],
//                                // fieldOffset[0]+fieldOffset[1]);
//                                if (fieldName.equalsIgnoreCase("Call_Duration")) {
//                                    data = dc.bytesToHex(byteData);
//                                    callDuration = data;
//                                    // logger.info("Call_Duration hex value ["+dc.bytesToHex(byteData)+"]");
//                                } else {
//                                    data = dc.getNumberFromBCD(dc.bytesToHex(byteData));
//                                }
//                                break;
//                            case 2:
//                                // byteData = Arrays.copyOfRange( buffer, fieldOffset[0],
//                                // fieldOffset[0]+fieldOffset[1]);
//                                data = dc.getNumberFromRightBCD(dc.bytesToHex(byteData));
//                                break;
//                            case 3:
//                                data = dc.getBitFromByte(buffer[fieldOffset[0]], fieldOffset[1]);
//                                break;
//                            case 4:
//                                // byteData = Arrays.copyOfRange( buffer, fieldOffset[0],
//                                // fieldOffset[0]+fieldOffset[1]);
//                                data = dc.getStringFromByte(byteData);
//                                break;
//                            default:
//                                break;
//                        }
//                        if (fieldName.equalsIgnoreCase("AnswerID")) {
//                            answerId = data;
//                        } else if (fieldName.equalsIgnoreCase("EndTime")) {
//                            endTime = data;
//                        } else if (fieldName.equalsIgnoreCase("SeqNum")) {
//                            cdrSeqNo = data;
//                        } else if (fieldName.equalsIgnoreCase("Call_Duration_Extended")) {
//                            callDuraExt = data;
//                        } else if (fieldName.equalsIgnoreCase("PartRecID")) {
//                            partId = data;
//                        }
//
//                        // ps.setString( j+1, data);
//                    } else if (fieldName.equals("CallDurationInSec")) {
//                        if (answerId.equals("1")) {
//                            // logger.info("Call duration ["+callDuration+"]");
//                            data = this.getCallDurationInSecond(callDuration, partId, cdrSeqNo, callDuraExt);
//                        } else {
//                            data = "0";
//                        }
//                    } else if (fieldName.equals("Changed_date") || fieldName.equals("cdr_date")) {
//                        data = "0";
//                    } else {
//                        data = "N";
//                    }
//                    sbCsv.append(data).append(",");
//                }
//                sbCsv.setLength(Math.max(sbCsv.length() - 1, 0));
//                csvFileWriter.write(sbCsv.toString());
//                csvFileWriter.write(System.getProperty("line.separator"));
//                sbCsv.setLength(0);
//                i++;
//                // break;
//            }
//            csvFileWriter.flush();
//            new com.functionapps.files.FileList().moveFile(fileName, repName, "", "");
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (fis != null) {
//                    fis.close();
//                }
//                if (dis != null) {
//                    dis.close();
//                }
//            } catch (Exception ex) {
//            }
//            data = null;
//            fieldName = null;
//            file = null;
//            buffer = null;
//            fieldOffset = null;
//            dc = null;
//            hm = null;
//        }
//        return result;
//    }
//    public String getCallDurationInSecond(String callDuration, String partId, String cdrSeqNo, String callDurationExt) {
//        String duration = null;
//        int seqNo = 0;
//        int hour = 0;
//        int minute = 0;
//        int second = 0;
//        int total = 0;
//        try {
//            if (cdrSeqNo != null && !cdrSeqNo.equals("")) {
//                seqNo = Integer.valueOf(cdrSeqNo);
//            }
//            if (callDuration.length() == 8) {
//                // logger.info("Hour part ["+callDuration.substring( 2, 4)+"],minute part
//                // ["+callDuration.substring( 4, 6)+"] and second part["+callDuration.substring(
//                // 6, 8)+"]");
//                hour = Integer.valueOf(callDuration.substring(2, 4)) * 3600;
//                minute = Integer.valueOf(callDuration.substring(4, 6)) * 60;
//                second = Integer.valueOf(callDuration.substring(6, 8));
//                // logger.info("Hour part ["+hour+"],minute part ["+minute+"] and second
//                // part["+second+"]");
//                if (partId.equals("3") && seqNo != 0) {
//                    total = hour + minute + second + ((seqNo - 1) * 1800);
//                } else {
//                    total = hour + minute + second;
//                }
//                if (callDurationExt != null && !callDurationExt.equals("") && !callDurationExt.equals("0")) {
//                    // duration = String.valueOf((total + 1 ));
//                } else {
//                    // duration = String.valueOf(total);
//                }
//            } else {
//                duration = "0";
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return duration;
//    }
//    public boolean sortAllFile(String repName, String path) {
//        boolean result = false;
//        Process newProcess = null;
//        String command = null;
//        try {
//            if (path != null) {
//                command = "sh " + path + "script/sort_file.sh " + path;
//                logger.info("Command to execute script is [" + command + "]");
//                newProcess = Runtime.getRuntime().exec(command);
//                newProcess.waitFor();
//                logger.info("File sorting completed.");
//                result = true;
//            } else {
//                result = false;
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return result;
//    }
