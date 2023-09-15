package com.glocks.parser;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import com.glocks.util.Util;
import java.text.DateFormat;

import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;
 

public class CEIRParserMain {

     static Logger logger = LogManager.getLogger(CEIRParserMain.class);

     static StackTraceElement l = new Exception().getStackTrace()[0];
//   logger.error("" + l.getClassName()+"/"+l.getMethodName()+":"+l.getLineNumber()  + e);

     public static void main(String args[]) {
//          Connection conn = null;
//          conn = (Connection) new com.glocks.db.MySQLConnection().getConnection();
//          String operator = args[0];
//          CDRPARSERmain(conn, operator);
     }

     public static void CDRPARSERmain(Connection conn, String operator) {

//        logger = LogManager.getLogger(CEIRParserMain.class)
          logger.info(" CEIRParserMain.class");
          String operator_tag = getOperatorTag(conn, operator);
          logger.debug("Operator tag is [" + operator_tag + "] ");
          ArrayList rulelist = new ArrayList<Rule>();
          String period = checkGraceStatus(conn);
          logger.debug("Period is [" + period + "] ");
          rulelist = getRuleDetails(operator, conn, operator_tag, period);
          addCDRInProfileWithRule(operator, conn, rulelist, operator_tag, period);
          System.exit(0);
     }

     private static String checkGraceStatus(Connection conn) {
          String period = "";
          String query = null;
          ResultSet rs1 = null;
          Statement stmt = null;
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
          Date currentDate = new Date();
          Date graceDate = null;
          try {
               query = "select value from system_configuration_db where tag='GRACE_PERIOD_END_DATE'";
               logger.info("Check Grace End Date [" + query + "]");
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
               logger.info("Period is " + period);
          } catch (Exception e) {
               logger.error("" + e);
               e.printStackTrace();
          } finally {
               try {
                    stmt.close();
                    rs1.close();
               } catch (SQLException e) {
                    logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
               }
          }
          return period;
     }

     private static String getOperatorTag(Connection conn, String operator) {
          String operator_tag = "";
          String query = null;
          ResultSet rs1 = null;
          Statement stmt = null;
          try {
              query = "select * from sys_param_list_value where tag='OPERATORS' and interpretation='" + operator + "'";
               logger.debug("get operator tag [" + query + "]");
               stmt = conn.createStatement();
               rs1 = stmt.executeQuery(query);
               while (rs1.next()) {
                    operator_tag = rs1.getString("tag_id");
               }
          } catch (Exception e) {
               logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
               operator_tag = "GSM";  // if no opertor found
          } finally {
               try {
                    stmt.close();
                    rs1.close();
               } catch (SQLException e) {
                    logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
                    e.printStackTrace();
               }

          }
          return operator_tag;

     }

    private static void addCDRInProfileWithRule(String operator, Connection conn, ArrayList<Rule> rulelist, String operator_tag, String period) {
//          String query = null;
//          ResultSet rs = null;
//          Statement stmt = null;
//          Statement raw_stmt = null;
//          String raw_query = null;
//          int output = 0;
//          String my_query = null;
//          Statement stmt1 = null;
//          HashMap<String, String> my_rule_detail;
//          String failed_rule_name = null;
//          String failed_rule_id = null;
//          String finalAction = "";
////          int old_sno = 0;
//          int update_sno = 0;
//          int usageInsert = 0;
//          int usageUpdate = 0;
//          int duplicateInsert = 0;
//          int duplicateUpdate = 0;
//          int nullInsert = 0;
//          int nullUpdate = 0;
//          DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//          LogWriter logWriter = new LogWriter();
//          String logPath = logWriter.getLogPath();
//          String fileName = null;
//          File file = null;
//          String log = null;
////          int split_upload_batch_no = 0;
//          int split_upload_batch_count = 0;
//          BufferedWriter bw1 = null;
//          try {
//               String foldrName = "/home/ceirapp/Sql_Loader_Files/";
//               SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//               File file1 = new File(foldrName);
//               file1.mkdir();
//               String fileNameInput1 = foldrName + "SqlLoader_" + sdf.format(Calendar.getInstance().getTime()) + ".sql";
//
//               logger.info("SQL_LOADER NAME   " + fileNameInput1);
//               File fout1 = new File(fileNameInput1);
//               FileOutputStream fos1 = new FileOutputStream(fout1, true);
//               bw1 = new BufferedWriter(new OutputStreamWriter(fos1));
//
////               ResultSet my_result_set = operatorDetails(conn, operator);
////               if (my_result_set.next()) {
////                    old_sno = my_result_set.getInt("last_upload_sno");
////                    split_upload_batch_no = my_result_set.getInt("split_upload_batch_no");
////               }
//            
//               Date p2Starttime = new Date();
//               query = "select * from " + operator + "_raw where  status='Init'  and file_name = (select file_name from   " + operator + "_raw where  status='Init' order by sno asc  fetch next 1 rows only)  order by sno    asc ";
//               stmt = conn.createStatement();
//               rs = stmt.executeQuery(query);
//               logger.debug("Getting Data from raw table  [" + query + "]");
//
//               HashMap<String, String> device_info = new HashMap<String, String>();
//
//               List<String> sourceTacList = new ArrayList<String>();
//               RuleFilter rule_filter = new RuleFilter();
//               stmt1 = conn.createStatement();
//               raw_stmt = conn.createStatement();
//               // CDR File Writer
//
//               fileName = "CEIR_CDR_File_" + sdf.format(Calendar.getInstance().getTime()) + ".csv";
//               file = new File(logPath);
//               if (!file.exists()) {
//                    file.mkdir();
//                    logger.debug("File not exist , Now Created ");
//               }
//               file = new File(logPath + fileName);
//               FileWriter myWriter;
//               if (file.exists()) {
//                    myWriter = new FileWriter(file, true);
//               } else {
//                    myWriter = new FileWriter(file);
//               }
//               BufferedWriter bw = null;
//               try {
//                    FileOutputStream fos = new FileOutputStream(file, true);
//                    bw = new BufferedWriter(new OutputStreamWriter(fos));
//               } catch (Exception e) {
//                    logger.error("Error  " + e);
//               }
//               boolean isOracle = conn.toString().contains("oracle");
//               String dateFunction = Util.defaultDateNow(isOracle);
//               while (rs.next()) {
//                     logger.info("Served IMEI =" + rs.getString("IMEI"));
//                    device_info.put("servedIMEI", rs.getString("IMEI"));
//                    device_info.put("recordType", rs.getString("record_type"));
//                    device_info.put("servedIMSI", rs.getString("IMSI"));
//                    device_info.put("servedMSISDN", ((rs.getString("MSISDN").startsWith("19") || rs.getString("MSISDN").startsWith("00") )     ? rs.getString("MSISDN").substring(2) : rs.getString("MSISDN")));
//                    device_info.put("systemType", rs.getString("system_type"));
//                    device_info.put("operator", rs.getString("operator"));
//                    device_info.put("file_name", rs.getString("file_name"));
//                    device_info.put("record_time", rs.getString("record_time"));
//                    device_info.put("operator_tag", operator_tag);
//
//                    // Event writer into file
////                if (file.length() == 0) {
////                    new LogWriter().writeEvents(myWriter, "servedIMEI",
////                            "recordType", "servedIMSI", "servedMSISDN", "systemType",
////                            "operator", "file_name", "record_time", "type", "rule_id", "rule_name", "status", "time");
////                } else {
////                    new LogWriter().writeEvents(myWriter, rs.getString("IMEI"),
////                            rs.getString("record_type"), rs.getString("IMSI"), (rs.getString("MSISDN").startsWith("19") ? rs.getString("MSISDN").substring(2) : rs.getString("MSISDN")),
////                            rs.getString("system_type"),
////                            rs.getString("operator"), rs.getString("file_name"), rs.getString("record_time"), "Init", "", "", "", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
////                }
//                    if (rs.getString("IMEI") == null || rs.getString("IMEI").equals("") || rs.getString("IMEI") == "" || rs.getString("IMEI").length() < 8) {
//                         logger.debug("Imei Null");
////                         if (rs.getString("MSISDN") != null) {
//                         output = checkDeviceNullDB(conn,  (  (rs.getString("MSISDN").startsWith("19") || rs.getString("MSISDN").startsWith("00") )  ? rs.getString("MSISDN").substring(2) : rs.getString("MSISDN")));
//                         logger.debug(" Null Output " + output);
//                         if (output == 0) {
//                              my_query = "insert into device_null_db (msisdn,imsi,create_filename,update_filename,"
//                                      + "updated_on,modified_on , created_on,record_type,system_type , operator ,record_time  ) "
//                                      + "values('" + ((rs.getString("MSISDN").startsWith("19")|| rs.getString("MSISDN").startsWith("00") )  ? rs.getString("MSISDN").substring(2) : rs.getString("MSISDN")) + "',"
//                                      + "'" + rs.getString("IMSI") + "',"
//                                      + "'" + rs.getString("file_name") + "',"
//                                      + "'" + rs.getString("file_name") + "',"
//                                      + "" + dateFunction + ","
//                                      + "" + dateFunction + ","
//                                      + "" + dateFunction + ","
//                                      + "'" + rs.getString("record_type") + "',"
//                                      + "'" + rs.getString("record_type") + "',"
//                                      + "'" + rs.getString("operator") + "',"
//                                      + "'" + rs.getString("record_time") + "'"
//                                      + ")";
//                              nullInsert++;
//                         } else {
//                              //  NOT SUED
//                              my_query = "update device_null_db set "
//                                      + "update_filename = '" + rs.getString("file_name")
//                                      //								+"',updated_on='"+rs.getString("record_time")+
//                                      + "',MODIFIED_ON=" + dateFunction
//                                      + ""
//                                      + " where msisdn='" + (rs.getString("MSISDN").startsWith("19") ? rs.getString("MSISDN").substring(2) : rs.getString("MSISDN")) + "'";
//                              logger.debug("need to update");
//                              nullUpdate++;
////                              }
//                         }
//                    } else {  // IMEI NOT NULL
//                         String failedRuleDate = null;
//                         sourceTacList.add(rs.getString("IMEI").substring(0, 8));
//                         device_info.put("tac", rs.getString("IMEI").substring(0, 8));
////                         my_rule_detail = rule_filter.getMyRule(conn, device_info, rulelist, myWriter, bw);
//                         my_rule_detail = rule_filter.getMyRule(conn, device_info, rulelist);
//                         logger.debug("getMyRule done");
//                         if (my_rule_detail.get("rule_name") != null) {
//                              failed_rule_name = my_rule_detail.get("rule_name");
//                              failed_rule_id = my_rule_detail.get("rule_id");
////                        action = my_rule_detail.get("action");          
//                              period = my_rule_detail.get("period");
//                              failedRuleDate = dateFunction;
//                         }
//                         if (failed_rule_id == null || failed_rule_name.equalsIgnoreCase("EXISTS_IN_ALL_ACTIVE_DB")) {
//                              finalAction = "ALLOWED";
//                         } else {
//                              if (failed_rule_name.equalsIgnoreCase("EXIST_IN_GSMABLACKLIST_DB") || failed_rule_name.equalsIgnoreCase("EXIST_IN_BLACKLIST_DB")) {
//                                   finalAction = "BLOCKED";
//                              } else if (period.equalsIgnoreCase("Grace")) {
//                                   finalAction = "SYS_REG";
//                              } else if (period.equalsIgnoreCase("Post_ Grace")) {
//                                   finalAction = "USER_REG";
//                                   sendMessageToMsisdn(conn, rs.getString("MSISDN"), rs.getString("IMEI"));
//                              }
//                         }
//                         output = checkDeviceUsageDB(conn, rs.getString("IMEI").substring(0, 14) , ((rs.getString("MSISDN").startsWith("19")|| rs.getString("MSISDN").startsWith("00") )  ? rs.getString("MSISDN").substring(2) : rs.getString("MSISDN")));
//                         logger.debug("output  " + output);
//                         if (output == 0) {                                         // imei not found in usagedb
//                              my_query = "insert into device_usage_db (imei,msisdn,imsi,create_filename,update_filename,"
//                                      + "updated_on,created_on,system_type,failed_rule_id,failed_rule_name,tac,period,action "
//                                      + " , mobile_operator , record_type , failed_rule_date,  modified_on ,record_time  , imei_index , raw_cdr_file_name , imei_arrival_time) "
//                                      + "values('" + rs.getString("IMEI") + "',"
//                                      + "'" + ((rs.getString("MSISDN").startsWith("19")|| rs.getString("MSISDN").startsWith("00") )  ? rs.getString("MSISDN").substring(2) : rs.getString("MSISDN")) + "',"
//                                      + "'" + rs.getString("IMSI") + "',"
//                                      + "'" + rs.getString("file_name") + "',"
//                                      + "'" + rs.getString("file_name") + "',"
//                                      + "" + dateFunction + ","
//                                      + "" + dateFunction + ","
//                                      + "'" + rs.getString("system_type") + "',"
//                                      + "'" + failed_rule_id + "',"
//                                      + "'" + failed_rule_name + "',"
//                                      + "'" + rs.getString("IMEI").substring(0, 8) + "',"
//                                      + "'" + period + "',"
//                                      + "'" + finalAction + "' , "
//                                      + "'" + rs.getString("operator") + "' , "
//                                      + "'" + rs.getString("record_type") + "',"
//                                      + "" + failedRuleDate + ","
//                                      + "" + dateFunction + ","
//                                      + "'" + rs.getString("record_time") + "' , "
//                                      + "'" + rs.getString("IMEI").substring(0, 14) + "'  "
//                                      + "'" + rs.getString("IMEI") + "'  "
//                                    
//                                      + ")";
//                              usageInsert++;
//                         }
//                         if (output == 1) {                        // imei found with same msisdn 
//                              my_query = "update device_usage_db set "
//                                      + "update_filename = '" + rs.getString("file_name")
//                                      //								+"', updated_on=TO_DATE('"+rs.getString("record_time")+"','yyyy/mm/dd hh24:mi:ss')"
//                                      + "', updated_on=" + dateFunction + ""
//                                      + ", modified_on=" + dateFunction + ""
//                                      + ", failed_rule_date=" + failedRuleDate + ""
//                                      + ", failed_rule_id='" + failed_rule_id
//                                      + "', failed_rule_name='" + failed_rule_name
//                                      + "',period='" + period
//                                   
//                                      + "',action='" + finalAction
//                                      + "' where imei_index='" + rs.getString("IMEI").substring(0, 14) + "'";
//                              usageUpdate++;
//                         }
//                         if (output == 2) {                                 // imei found with different msisdn
//                              output = checkDeviceDuplicateDB(conn, rs.getString("IMEI").substring(0, 14)  ,(  (rs.getString("MSISDN").startsWith("19") || rs.getString("MSISDN").startsWith("00") ) ? rs.getString("MSISDN").substring(2) : rs.getString("MSISDN")));
//                              if (output == 0) {
//                                   my_query = "insert into device_duplicate_db (imei,msisdn,imsi,create_filename,update_filename,"
//                                           + "updated_on,created_on,system_type,failed_rule_id,failed_rule_name,tac,period,action  "
//                                           + " , mobile_operator , record_type , failed_rule_date,  modified_on  ,record_time , imei_index "
//                                           + "  ) "
//                                           + "values('" + rs.getString("IMEI") + "',"
//                                           + "'" + ((rs.getString("MSISDN").startsWith("19") || rs.getString("MSISDN").startsWith("00") )  ? rs.getString("MSISDN").substring(2) : rs.getString("MSISDN")) + "',"
//                                           + "'" + rs.getString("IMSI") + "',"
//                                           + "'" + rs.getString("file_name") + "',"
//                                           + "'" + rs.getString("file_name") + "',"
//                                           //									+"TO_DATE('"+rs.getString("record_time")+"','yyyy/mm/dd hh24:mi:ss'),"
//                                           //									+"TO_DATE('"+rs.getString("record_time")+"','yyyy/mm/dd hh24:mi:ss'),"
//                                           + "" + dateFunction + ","
//                                           + "" + dateFunction + ","
//                                           + "'" + rs.getString("system_type") + "',"
//                                           + "'" + failed_rule_id + "',"
//                                           + "'" + failed_rule_name + "',"
//                                           + "'" + rs.getString("IMEI").substring(0, 8) + "',"
//                                           + "'" + period + "',"
//                                           + "'" + finalAction + "' , "
//                                           + "'" + rs.getString("operator") + "' , "
//                                           + "'" + rs.getString("record_type") + "' , "
//                                           + "" + failedRuleDate + " , "
//                                           + "" + dateFunction + ",  "
//                                           + "'" + rs.getString("record_time") + "' "
//                                           + "'" + rs.getString("IMEI").substring(0, 14) + "' "
//                                           + ")";
//                                   duplicateInsert++;
//
//                              } else {
//                                   my_query = "update device_duplicate_db set "
//                                           + "update_filename = '" + rs.getString("file_name")
//                                           //									+"', updated_on=TO_DATE('"+rs.getString("record_time")+"','yyyy/mm/dd hh24:mi:ss')"
//                                           + "', updated_on=" + dateFunction + ""
//                                           + ", modified_on=" + dateFunction + ""
//                                           + ", failed_rule_id='" + failed_rule_id
//                                           + "', failed_rule_name='" + failed_rule_name
//                                           + "',period='" + period
//                                           + "',action='" + finalAction
//                                           + "' where msisdn='" + ((rs.getString("MSISDN").startsWith("19")|| rs.getString("MSISDN").startsWith("00") )   ? rs.getString("MSISDN").substring(2) : rs.getString("MSISDN")) + "' and imei_index='" + rs.getString("IMEI").substring(0, 14) + "'";
//                                   duplicateUpdate++;
//                              }
//                         }
//                    }
//                    split_upload_batch_count++;
////                    stmt1.addBatch(my_query);
//                    update_sno = Integer.parseInt(rs.getString("sno"));
//                    logger.info("Count Number " + update_sno + " ::: " + my_query);
//
//                    stmt1.executeUpdate(my_query);
//
//                    bw1.write(my_query);
//                    bw1.newLine();
//
////                    if (split_upload_batch_count == split_upload_batch_no) {
////                         stmt1.executeBatch();
////                         split_upload_batch_count = 0;
////                         raw_query = "update " + operator + "_raw" + " set status='Complete' where sno <='" + update_sno + "'     ";
////                         raw_stmt.executeUpdate(raw_query);
////                         conn.commit();
////                    }
//               }   //While End   
//               conn.commit();
//
//               Date p2Endtime = new Date();
//               cdrFileDetailsUpdate(conn, operator, device_info.get("file_name"), usageInsert, usageUpdate, duplicateInsert, duplicateUpdate, nullInsert, nullUpdate, p2Starttime, p2Endtime);
//               raw_query = "update " + operator + "_raw" + " set status='Complete' where sno <='" + update_sno + "'     ";
//               Map<String, Long> map = sourceTacList.stream()
//                       .collect(Collectors.groupingBy(c -> c, Collectors.counting()));
//               map.forEach((k, v) -> new HexFileReader().insertSourceTac(conn, k, device_info.get("file_name"), v, "source_tac_info"));
//
//               raw_stmt.executeUpdate(raw_query);
////               updateRawLastSno(conn, operator, update_sno);
//               conn.commit();
//          } catch (Exception e) {
//               logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
//
//          } finally {
//               try {
//                    bw1.close();
//                    raw_stmt.close();
////                    stmt1.close();
//                    conn.commit();
//                    stmt.close();
//               } catch (Exception e) {
//                    logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
//               }
//          }
     }

     private static void updateRawData(Connection conn, String operator, String id, String status) {
          String query = null;
          Statement stmt = null;
          query = "update " + operator + "_raw" + " set status='" + status + "' where sno='" + id + "'";
          logger.info(query);
          try {
               stmt = conn.createStatement();
               ;
               conn.commit();
          } catch (SQLException e) {
               logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
          } finally {
               try {
                    stmt.close();
               } catch (SQLException e) {
                    logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
               }
          }

     }

     private static int checkDeviceDuplicateDB(Connection conn, String imei, String msisdn) {
          String query = null;
          ResultSet rs1 = null;
          Statement stmt = null;
          int status = 0;
          try {
               query = "select * from device_duplicate_db where imei_index='" + imei + "' and msisdn = '" + msisdn + "'";
               logger.debug("device_dupliate db" + query);
               stmt = conn.createStatement();
               rs1 = stmt.executeQuery(query);
               while (rs1.next()) {
                    status = 1;
               }
          } catch (Exception e) {
               logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
          } finally {
               try {
                    stmt.close();
                    rs1.close();
               } catch (SQLException e) {
                    logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
               }
          }
          return status;
     }

     private static int checkDeviceUsageDB(Connection conn, String imeiIndex, String msisdn) {
          String query = null;
          ResultSet rs1 = null;
          Statement stmt = null;
          int status = 0;                                                         // imei not found
          try {
               query = "select * from device_usage_db where imei_index='" + imeiIndex + "'";
               logger.debug("device usage db" + query);
               stmt = conn.createStatement();
               rs1 = stmt.executeQuery(query);
               while (rs1.next()) {
                    logger.debug(rs1.getString("msisdn"));
                    if (rs1.getString("msisdn").equalsIgnoreCase(msisdn)) {     // imei found with same msisdn 
                         status = 1;
                    } else {
                         status = 2;                                                 // imei found with different msisdn
                    }
               }
          } catch (Exception e) {
               logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
          } finally {
               try {
                    stmt.close();
                    rs1.close();
               } catch (SQLException e) {
                    logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
               }
          }
          return status;
     }

     public static int checkDeviceNullDB(Connection conn, String msisdn) {
          String query = null;
          ResultSet rs1 = null;
          Statement stmt = null;
          int status = 0;
          try {
               query = "select * from device_null_db where msisdn='" + msisdn + "'";
               logger.debug("device usage db" + query);
               stmt = conn.createStatement();
               rs1 = stmt.executeQuery(query);
               while (rs1.next()) {
                    status = 1;
               }
          } catch (Exception e) {
               logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
          } finally {
               try {
                    stmt.close();
                    rs1.close();
               } catch (SQLException e) {
                    logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
               }
          }
          return status;
     }

     private static ArrayList getRuleDetails(String operator, Connection conn, String operator_tag, String period) {
          ArrayList rule_details = new ArrayList<Rule>();
          String query = null;
          ResultSet rs1 = null;
          Statement stmt = null;
          try {
               query = "select a.id as rule_id,a.name as rule_name,b.output as output,b.grace_action, b.post_grace_action, b.failed_rule_action_grace, b.failed_rule_action_post_grace from rule_engine a, rule_engine_mapping b where  a.name=b.name  and a.state='Enabled' and b.feature='CDR' and   b." + period + "_action !='NA'         order by b.rule_order asc";
               logger.debug("Query is " + query);
               stmt = conn.createStatement();
               rs1 = stmt.executeQuery(query);
               while (rs1.next()) {
                    if (rs1.getString("rule_name").equalsIgnoreCase("IMEI_LENGTH")) {
                         if (operator_tag.equalsIgnoreCase("GSM")) {
//						Rule rule = new Rule(rs1.getString("rule_name"),rs1.getString("output"),rs1.getString("rule_id"),period, rs1.getString(period+"_action"));
                              Rule rule = new Rule(rs1.getString("rule_name"), rs1.getString("output"), rs1.getString("rule_id"), period, rs1.getString(period + "_action"), rs1.getString("failed_rule_action_" + period));
                              rule_details.add(rule);
                         }
                    } else {
//					Rule rule = new Rule(rs1.getString("rule_name"),rs1.getString("output"),rs1.getString("rule_id"),period, rs1.getString(period+"_action"));
                         Rule rule = new Rule(rs1.getString("rule_name"), rs1.getString("output"), rs1.getString("rule_id"), period, rs1.getString(period + "_action"), rs1.getString("failed_rule_action_" + period));
                         rule_details.add(rule);
                    }
               }
          } catch (Exception e) {
               logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
          } finally {
               try {
                    stmt.close();
                    rs1.close();
               } catch (SQLException e) {
                    logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
               }
          }
          return rule_details;
     }

//     static ResultSet operatorDetails(Connection conn, String operator) {
//          Statement stmt = null;
//          ResultSet rs = null;
//          String query = null;
//          try {
//               query = "select * from r ep_schedule_config_db where operator='" + operator + "'";
//               stmt = conn.createStatement();
//               return rs = stmt.executeQuery(query);
//          } catch (Exception e) {
//               logger.error("  Error operatorDetails::" + e);
//          }
//          return rs;
//     }

     private static void updateLastStatuSno(Connection conn, String operator, int id, int limit) {
          String query = null;
          Statement stmt = null;
//		query = "update "+operator+"_raw"+" set status='Start' where sno>'"+id+"' and rownum<= "+limit;
          query = "update " + operator + "_raw" + " set status='Start' where sno>'" + id + "' and sno<=" + (id + limit);
          logger.info("updateLastStatuSno qury.." + query);
          try {
               stmt = conn.createStatement();
               stmt.executeUpdate(query);
               conn.commit();
          } catch (SQLException e) {
               logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
          } finally {
               try {
                    stmt.close();
               } catch (SQLException e) {
                    logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
               }
          }
     }

//     private static void updateRawLastSno(Connection conn, String operator, int sno) {
//          String query = null;
//          Statement stmt = null;
//          query = "update r ep_schedule_config_db set last_upload_sno=" + sno + " where operator='" + operator + "'";
//          logger.info("updateRawLastSno qury is " + query);
//
//          try {
//               stmt = conn.createStatement();
//               stmt.executeUpdate(query);
//               conn.commit();
//          } catch (SQLException e) {
//               logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
//          } finally {
//               try {
//                    stmt.close();
//               } catch (SQLException e) {
//                    logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
//               }
//          }
//     }

     static void cdrFileDetailsUpdate(Connection conn, String operator, String fileName, int usageInsert, int usageUpdate, int duplicateInsert, int duplicateUpdate, int nullInsert, int nullUpdate, Date P2StartTime, Date P2EndTime) {
          String query = null;
          DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          Statement stmt = null;
          query = "update cdr_file_details_db set  MODIFIED_ON =   "+ Util.defaultDateNow(true) + " ,     total_inserts_in_usage_db='" + usageInsert + "' , total_updates_in_usage_db='" + usageUpdate + "'  ,  total_insert_in_dup_db='" + duplicateInsert + "' , total_updates_in_dup_db='" + duplicateUpdate + "' "
                  + " ,total_insert_in_null_db='" + nullInsert + "' ,total_update_in_null_db='" + nullUpdate + "'    , P2StartTime=      TO_DATE('" + df.format(P2StartTime) + "','YYYY-MM-DD HH24:MI:SS') , P2EndTime=          TO_DATE('" + df.format(P2EndTime) + "','YYYY-MM-DD HH24:MI:SS')          where operator='" + operator + "'   and file_name = '" + fileName + "' ";
          logger.info(" qury is " + query);

          try {
               stmt = conn.createStatement();
               stmt.executeUpdate(query);
               conn.commit();
          } catch (SQLException e) {
               logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
          } finally {
               try {
                    stmt.close();
               } catch (SQLException e) {
                    logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
               }
          }

     }

//     private static void sendMessageToMsisdn(Connection conn, String msisdn, String imei) {
//
//          MessageConfigurationDbDao messageConfigurationDbDao = new MessageConfigurationDbDao();
//          PolicyBreachNotificationDao policyBreachNotificationDao = new PolicyBreachNotificationDao();
//          MessageConfigurationDb messageDb = null;
//
//          try {
//               Optional<MessageConfigurationDb> messageDbOptional = messageConfigurationDbDao.getMessageDbTag(conn, "USER_REG_MESSAGE");
//
//               if (messageDbOptional.isPresent()) {
//                    messageDb = messageDbOptional.get();
//                    String message = messageDb.getValue().replace("<imei>", imei);
//
//                    PolicyBreachNotification policyBreachNotification = new PolicyBreachNotification("SMS", message, "", msisdn, imei);
//
//                    policyBreachNotificationDao.insertNotification(conn, policyBreachNotification);
//
//               }
//
//          } catch (Exception e) {
//               logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
//          }
//
//     }

}

