package com.glocks.parser.service;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import com.glocks.parser.ErrorFileGenrator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.glocks.log.LogWriter;
import com.glocks.parser.CEIRFeatureFileFunctions;
import com.glocks.parser.CEIRFeatureFileParser;
import com.glocks.parser.HexFileReader;
import com.glocks.parser.Rule;
import com.glocks.parser.RuleFilter;
import com.glocks.util.Util;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsignmentInsertUpdate {

    static Logger logger = LogManager.getLogger(ConsignmentInsertUpdate.class);

    public void process(Connection conn, String operator, String sub_feature, ArrayList<Rule> rulelist, String txn_id, String operator_tag, String usertype_name, int webActState) {
        ErrorFileGenrator errFile = new ErrorFileGenrator();
        String query = null;
        ResultSet rs = null;
        ResultSet rs1 = null;
        Statement stmt = null;  // for rs
        Statement stmt0 = null; // for rs1
        Statement stmt1 = null; // devce_dv batch
        Statement stmt2 = null; // device_imprter_db btch
        Statement raw_stmt = null; // update raw table to cmplete
        Statement stmt3 = null;    // custom db
        Statement stmt4 = null;    // greylist_db
        Statement stmt5 = null;   // greylisthistory_db

        String raw_query = null;
        List<String> sourceTacList = new ArrayList<String>();

        String my_query = null;
        String device_db_query = null;
        String device_custom_db_qry = null;
        String device_greylist_db_qry = null;
        String device_greylist_History_db_qry = null;

        HashMap<String, String> my_rule_detail = new HashMap<String, String>();;
        HashMap<String, String> stolnRcvryDetails = new HashMap<String, String>();
        HashMap<String, String> feature_file_mapping = new HashMap<String, String>();
        HashMap<String, String> feature_file_management = new HashMap<String, String>();
        CEIRFeatureFileFunctions ceirfunction = new CEIRFeatureFileFunctions();
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime = sdf1.format(new Date());

        String period = "";
//          int parser_base_limit = 0;
        int update_sno = 0;
        LogWriter logWriter = new LogWriter();
        String logPath = logWriter.getLogPath();
        String fileName = null;
        File file = null;
        int dvcDbCounter = 0;
//        String log = null;
        int split_upload_batch_no = 1; // it should be dymnamic
        int split_upload_batch_count = 0;
        int rrslt = 0;
        int countError = 0;
        int stolenRecvryBlock = 0;
        int totalCount = 0;
        CEIRFeatureFileParser cEIRFeatureFileParser = new CEIRFeatureFileParser();
        String errorFilePath = cEIRFeatureFileParser.getErrorFilePath(conn);
        BufferedWriter bw = null;
        try {
            File fileEr = new File(errorFilePath + txn_id);       //   errFile.gotoErrorFilewithList(errorFilePath, txn_id, fileErrorLines);
            fileEr.mkdir();
            String fileNameInput = errorFilePath + txn_id + "/" + txn_id + "_error.csv";
            new ErrorFileGenrator().redudencyApiConnect(txn_id + "_error.csv", txn_id, errorFilePath + txn_id + "/");
            File fout = new File(fileNameInput);
            FileOutputStream fos = new FileOutputStream(fout, true);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            feature_file_mapping = ceirfunction.getFeatureMapping(conn, operator, usertype_name); // select * from // feature_mapping_db
            logger.info("ErrorFile Name " + fileNameInput);
            if (webActState == 3) {
                query = "select count(*) as counnt from " + operator + "_raw where  txn_id='" + txn_id + "'  ";
                logger.info("Query(STATE 3 ) " + query);
                stmt = conn.createStatement();
                rs = stmt.executeQuery(query);
                int rawCount = 0;
                int finalCount = 0;
                while (rs.next()) {
                    rawCount = rs.getInt(1);
                }
                query = " select count(*) as counnt  from  " + feature_file_mapping.get("output_device_db") + " where   txn_id='" + txn_id + "'   ";
                rs = stmt.executeQuery(query);
                while (rs.next()) {
                    finalCount = rs.getInt(1);
                }
                logger.info("Query(STATE 3 ). " + query); // 
                logger.info("State Result ..rawCount  " + rawCount + "  finalCount" + finalCount);
                if (rawCount == finalCount) {
                    ceirfunction.UpdateStatusViaApi(conn, txn_id, 1, operator);
                    ceirfunction.updateFeatureFileStatus(conn, txn_id, 4, operator, sub_feature);
                } else {
                    query = " delete from   " + feature_file_mapping.get("output_device_db") + " where   txn_id='" + txn_id + "'   ";
                    rs = stmt.executeQuery(query);
                    logger.info("Query(STATE 3 ).. " + query);
                    query = "update  " + operator + "_raw  set status = 'Init' where  txn_id='" + txn_id + "'  ";
                    rs = stmt.executeQuery(query);
                    logger.info("Query(STATE 3 ). " + query);
                    ceirfunction.updateFeatureFileStatus(conn, txn_id, 2, operator, sub_feature);
                    conn.commit();
                }
                return;
            }   // state 3 End
            stolnRcvryDetails.put("operator", operator);
//               ResultSet my_result_set = cEIRFeatureFileParser.operatorDetails(conn, operator);
//               if (my_result_set.next()) {
//                    parser_base_limit = my_result_set.getInt("split_upload_set_no");
//               }
            query = "select * from " + operator + "_raw where   txn_id='" + txn_id + "' and status='Init' order by sno asc ";
            stmt = conn.createStatement();
            logger.info("Query.. " + query);
            rs = stmt.executeQuery(query);
            HashMap<String, String> device_info = new HashMap<String, String>();
            RuleFilter rule_filter = new RuleFilter();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            fileName = "CEIR_" + operator + "_File_" + sdf.format(Calendar.getInstance().getTime()) + ".csv";
            file = new File(logPath);
            if (!file.exists()) {
                file.mkdir();
                logger.info("File not exists");
            }
            file = new File(logPath + fileName);
            FileWriter myWriter;
            if (file.exists()) {
                myWriter = new FileWriter(file, true);
            } else {
                myWriter = new FileWriter(file);
            }

            period = cEIRFeatureFileParser.checkGraceStatus(conn);  // grace / postgrace
            
            feature_file_management = ceirfunction.getFeatureFileManagement(conn, feature_file_mapping.get("mgnt_table_db"), txn_id); // select * from " + management_db + " where
            if (operator.equalsIgnoreCase("Stolen") || operator.equalsIgnoreCase("Recovery") || operator.equalsIgnoreCase("Block") || operator.equalsIgnoreCase("Unblock")) {
                stolnRcvryDetails = cEIRFeatureFileParser.getStolenRecvryDetails(conn, txn_id);
                stolenRecvryBlock = 1;
            }
            logger.info("OPERATOR/FEATURE--" + operator + "--SUBFEATURE--" + sub_feature + "");
            bw.write("DEVICETYPE,DeviceIdType,MultipleSIMStatus,S/NofDevice,IMEI,Devicelaunchdate,DeviceStatus, Error Code ,Error Message ");
            bw.newLine();
            while (rs.next()) {   // raw db records 
                logger.info("Served IMEI  =" + rs.getString("IMEIESNMEID"));
                device_info.put("DeviceIdType", rs.getString("DeviceIdType"));
                device_info.put("IMEIESNMEID", rs.getString("IMEIESNMEID"));
                device_info.put("DeviceType", rs.getString("DeviceType"));
                device_info.put("MultipleSIMStatus", rs.getString("MultipleSIMStatus"));
                device_info.put("SNofDevice", rs.getString("SNofDevice"));
                device_info.put("Devicelaunchdate", rs.getString("Devicelaunchdate"));
                device_info.put("DeviceStatus", rs.getString("DeviceStatus"));
                device_info.put("operator", operator);
                device_info.put("feature", operator);
                device_info.put("file_name", rs.getString("file_name"));
                device_info.put("operator_tag", operator_tag);
                device_info.put("txn_id", txn_id);

                logger.info("GetMyFeatureRule start   ");
                my_rule_detail = rule_filter.getMyFeatureRule(conn, device_info, rulelist, myWriter, bw);
                logger.info("GetMyFeatureRule Error Flag  --    " + my_rule_detail.get("errorFlag"));
                String fileArray = device_info.get("DeviceType") + "," + device_info.get("DeviceIdType") + "," + device_info.get("MultipleSIMStatus") + "," + device_info.get("SNofDevice") + "," + device_info.get("IMEIESNMEID") + "," + device_info.get("Devicelaunchdate") + "," + device_info.get("DeviceStatus") + "";
                if (my_rule_detail.get("errorFlag").equals("0")) {   // details Pass
                    logger.info("Error Flag 0  ");
                    bw.write(fileArray);
                    bw.newLine();
                } else {   // execute Action False
                    logger.info("Action_output." + my_rule_detail.get("action_output"));
                    if (my_rule_detail.get("action_output").equalsIgnoreCase("Failure")) {//action is failed +  on which rule it is failed 
                        bw.write(fileArray + ", Action is not Completed for  " + my_rule_detail.get("rule_name"));
                        bw.newLine();
                    }
                    countError++;
                }
                update_sno = Integer.parseInt(rs.getString("sno"));
            }                 // END While 
//               if (update_sno != 0) {
//                    cEIRFeatureFileParser.updateRawLastSno(conn, operator, update_sno);
//               }
            logger.info("Count error.. " + countError + " , Total imei in mgmt db .. ");
            String error_file_path = errorFilePath + txn_id + "/" + txn_id + "_error.csv";
            logger.info("CountError(if 0: Process Pass  )  -- " + countError);
            if (countError != 0) {
                ceirfunction.UpdateStatusViaApi(conn, txn_id, 1, operator);    
                ceirfunction.updateFeatureFileStatus(conn, txn_id, 4, operator, sub_feature); // update web_action_db
            } else {
                logger.info("File  moving to old Folder ");
                file = new File(errorFilePath + txn_id + "/old/");
                if (!file.exists()) {
                    file.mkdir();
                }
                Path temp = Files.move(Paths.get(error_file_path),
                        Paths.get(errorFilePath + txn_id + "/old/" + txn_id + "_" + java.time.LocalDateTime.now() + "_P2_error.csv"));
                if (temp != null) {
                    logger.info("File renamed and moved successfully");
                } else {
                    logger.info("Failed to move the file");
                }

                rrslt = cEIRFeatureFileParser.getCustomData(conn, txn_id); //  1  for custom select user_type from stock_mgmt where txn_id
                logger.info(".getCustomData rslt ." + rrslt);
                stmt0 = conn.createStatement();
                stmt1 = conn.createStatement();
                stmt2 = conn.createStatement();
                stmt3 = conn.createStatement();
                stmt4 = conn.createStatement();
                stmt5 = conn.createStatement();
                split_upload_batch_count = 0;
                logger.info("  inserting....");
                rs1 = stmt0.executeQuery(query);
                String dateNow1 = "";
                if (conn.toString().contains("oracle")) {
                    dateNow1 = new SimpleDateFormat("dd-MMM-YY").format(new Date());
                } else {
                    dateNow1 = new SimpleDateFormat("YYYY-MM-dd").format(new Date());
                }

                boolean isOracle = conn.toString().contains("oracle");
                String dateFunction = Util.defaultDate(isOracle);

                logger.info(".output_device_db  ." + feature_file_mapping.get("output_device_db"));
                while (rs1.next()) {      //can b rs (we can run again)
                    split_upload_batch_count++;
                    String dvsStatus = rs1.getString("DeviceStatus");
                    if (stolenRecvryBlock == 1) {
                        dvsStatus = stolnRcvryDetails.get("divceStatus");
                    }
                    dvcDbCounter = getCounterFromDeviceDb(conn, rs1.getString("IMEIESNMEID").substring(0, 14));
                    sourceTacList.add(rs1.getString("IMEIESNMEID").substring(0, 8));
                    my_query = "insert into " + feature_file_mapping.get("output_device_db")
                            + " (device_id_type,created_on,device_launch_date,device_status,"
                            + "device_type,imei_esn_meid,modified_on,multiple_sim_status,period,sn_of_device,txn_id,user_id ,feature_name ,actual_imei ,tac  ) " //,feature_name
                            + "values(" + "'" + rs1.getString("DeviceIdType") + "'," + "" + dateFunction + "," /// "dd-MMM-YY"                              
                            + "'" + rs1.getString("Devicelaunchdate") + "', '" + dvsStatus + "'  ,'" + rs1.getString("DeviceType") + "'," + "'" + rs1.getString("IMEIESNMEID").substring(0, 14)
                            + "'," + "" + dateFunction + "," + "'" + rs1.getString("MultipleSIMStatus") + "'," + "'"
                            + period + "'," + "'" + rs1.getString("SNofDevice") + "'," + "'" + txn_id + "'," + ""
                            + feature_file_management.get("user_id") + ", '" + operator + "' , '" + rs1.getString("IMEIESNMEID") + "'  , '" + rs1.getString("IMEIESNMEID").substring(0, 8) + "'    )";    // "," + operator +  

                    logger.info("Counnter si " + dvcDbCounter);
                    if (dvcDbCounter == 0) {
                        device_db_query = "insert into device_db (counter ,device_id_type,created_on,device_launch_date,device_status,"
                                + "device_type,imei_esn_meid,modified_on,multiple_sim_status,period,sn_of_device, tac  ,txn_id , actual_imei ,   feature_name   ) " // feature_name
                                + "values(1 ,'" + rs1.getString("DeviceIdType") + "'," + "" + dateFunction + "," + "'"
                                + rs1.getString("Devicelaunchdate") + "'," + "'" + rs1.getString("DeviceStatus") + "',"
                                + "'" + rs1.getString("DeviceType") + "'," + "'" + rs1.getString("IMEIESNMEID").substring(0, 14) + "',"
                                + "" + dateFunction + "," + "'" + rs1.getString("MultipleSIMStatus") + "',"
                                + "'" + period + "'," + "'" + rs1.getString("SNofDevice") + "',"
                                + "'" + rs1.getString("IMEIESNMEID").substring(0, 8) + "'  "
                                + ",'" + txn_id + "'  "
                                + ",'" + rs1.getString("IMEIESNMEID") + "'  "
                                + " , '" + operator + "' "
                                + ")";
                    } else {
                        device_db_query = "update  device_db set counter = " + (dvcDbCounter + 1) + " where imei_esn_meid = '" + rs1.getString("IMEIESNMEID").substring(0, 14) + "' ";
                    }

                    device_custom_db_qry = "insert into device_custom_db (device_id_type,created_on,device_launch_date,device_status,"
                            + "device_type,imei_esn_meid,modified_on,multiple_sim_status,period,sn_of_device,txn_id ,feature_name  ,user_id  ,actual_imei , tac  ) " // 
                            + "values('" + rs1.getString("DeviceIdType") + "'," + " " + dateFunction + "," + "'"
                            + rs1.getString("Devicelaunchdate") + "'," + "'" + rs1.getString("DeviceStatus") + "',"
                            + "'" + rs1.getString("DeviceType") + "'," + "'" + rs1.getString("IMEIESNMEID").substring(0, 14) + "',"
                            + "" + dateFunction + "," + "'" + rs1.getString("MultipleSIMStatus") + "',"
                            + "'" + period + "'," + "'" + rs1.getString("SNofDevice") + "',"
                            + "'" + txn_id + "'  "
                            + " , '" + operator + "' ,  '" + feature_file_management.get("user_id") + "'  ,  '" + rs1.getString("IMEIESNMEID") + "' ,  '" + rs1.getString("IMEIESNMEID").substring(0, 8) + "'      "
                            + ")";

                    if (stolenRecvryBlock == 1) {
                        if (stolnRcvryDetails.get("operation").equals("0")) {
                            device_greylist_db_qry = "insert into   greylist_db (created_on , imei, user_id , txn_id , mode_type  , request_type, user_type  , complain_type, "
                                    + " device_id_type , device_status , device_type  , multiple_sim_status ,actual_imei , tac )   "
                                    + "values(           " + "" + dateFunction + "," + "'" + rs1.getString("IMEIESNMEID").substring(0, 14)
                                    + "'," + " ( select username from users where users.id=  "
                                    + feature_file_management.get("user_id") + "  )  ,  " + " '" + txn_id + "', " + "'"
                                    + stolnRcvryDetails.get("source") + "'," + "'" + stolnRcvryDetails.get("reason")
                                    + "'," + "'" + stolnRcvryDetails.get("usertype") + "'," + "'"
                                    + stolnRcvryDetails.get("complaint_type") + "' ,  '" + rs1.getString("DeviceIdType") + "',   '" + rs1.getString("DeviceStatus") + "',     '" + rs1.getString("DeviceType") + "' ,     '" + rs1.getString("MultipleSIMStatus") + "'  ,     '" + rs1.getString("IMEIESNMEID") + "'  ,  '" + rs1.getString("IMEIESNMEID").substring(0, 8) + "'      )";
                        } else {
                            device_greylist_db_qry = "delete from greylist_db where imei  = '"
                                    + rs1.getString("IMEIESNMEID").substring(0, 14) + "' ";
                            my_query = "update  " + feature_file_mapping.get("output_device_db") + "  set device_status = '" + dvsStatus + "'  where imei_esn_meid  = '" + rs1.getString("IMEIESNMEID").substring(0, 14) + "'";
                        }
                        device_greylist_History_db_qry = "insert into   greylist_db_history (created_on , imei, user_id , txn_id , mode_type  , request_type, user_type  , complain_type ,operation    ,  device_id_type , device_status , device_type ,MULTIPLE_SIM_STATUS ,actual_imei , tac  )   "
                                + "values(           " + "" + dateFunction + "," + "'" + rs1.getString("IMEIESNMEID").substring(0, 14)
                                + "'," + " ( select username from users where users.id=  "
                                + feature_file_management.get("user_id") + "  )  ,  " + " '" + txn_id + "', " + "'"
                                + stolnRcvryDetails.get("source") + "'," + "'" + stolnRcvryDetails.get("reason") + "',"
                                + "'" + stolnRcvryDetails.get("usertype") + "'," + "'"
                                + stolnRcvryDetails.get("complaint_type") + "' , " + "'"
                                + stolnRcvryDetails.get("operation") + "'    ,  '" + rs1.getString("DeviceIdType") + "', '" + rs1.getString("DeviceStatus") + "','" + rs1.getString("DeviceType") + "' , ,     '" + rs1.getString("MultipleSIMStatus") + "' ,     '" + rs1.getString("IMEIESNMEID") + "' ,  '" + rs1.getString("IMEIESNMEID").substring(0, 8) + "'       )";
                    }
                    stmt1.addBatch(my_query);
                    logger.info("my_query  : " + my_query);
                    if (rrslt != 0) {
                        stmt3.addBatch(device_custom_db_qry);
                        logger.info("device_custom_db_qry  : " + device_custom_db_qry);
                    }
                    if (stolenRecvryBlock == 1) {
                        insertFromImporterManufactor(conn, rs1, stolnRcvryDetails, feature_file_management, feature_file_mapping, dateFunction, period, txn_id, operator, sub_feature);
                    } else {
//                              try {
                        logger.info("device_db_query:  : " + device_db_query);
                        stmt2.executeUpdate(device_db_query);

//                              } catch (Exception e) {
//                                   logger.error("Error in device_db   " + e);
//                              }
                    }
                    split_upload_batch_count++;
                    if (split_upload_batch_count == split_upload_batch_no) {
                        stmt1.executeBatch();
                        conn.commit();
                        if (rrslt != 0) {
                            stmt3.executeBatch();
                            conn.commit();
                        }
                        if (stolenRecvryBlock == 1) {
                            stmt4.executeBatch();
                            conn.commit();
                            stmt5.executeBatch();
                            conn.commit();
                        }
                        split_upload_batch_count = 0;
                    }
                    totalCount++;
                }               // WHILE CLOSE END
                stmt1.executeBatch();
                conn.commit();
                if (rrslt != 0) {
                    stmt3.executeBatch();
                    conn.commit();
                }
                if (stolenRecvryBlock == 1) {
                    stmt4.executeBatch();
                    conn.commit();
                    stmt5.executeBatch();
                    conn.commit();
                }
                logger.info("Entered outside");
                conn.commit();
                stmt.close();
                stmt0.close();
                stmt1.close();
                stmt2.close();
                stmt3.close();
                stmt4.close();
                stmt5.close();
                logger.info(" sourceTacList SIZE:: " + sourceTacList.size());
                if (feature_file_mapping.get("USERTYPE_NAME").equalsIgnoreCase("Importer") || feature_file_mapping.get("USERTYPE_NAME").equalsIgnoreCase("Manufacturer")) {
                    logger.info(" sourceTacList SIZE:: " + sourceTacList.size());
                    Map<String, Long> map = sourceTacList.stream()
                            .collect(Collectors.groupingBy(c -> c, Collectors.counting()));
                    map.forEach((k, v) -> new HexFileReader().insertSourceTac(conn, k, device_info.get("file_name"), v, "source_tac_inactive_info"));
                }
                ceirfunction.UpdateStatusViaApi(conn, txn_id, 2, operator);
                ceirfunction.updateFeatureFileStatus(conn, txn_id, 4, operator, sub_feature);
            }       // ELSE CLOSE END

            if (operator.equalsIgnoreCase("STOCK") || operator.equalsIgnoreCase("CONSIGNMENT")) {
                rawTableCleanUp(conn, operator, txn_id);
            }

            String EndTime = sdf.format(new Date());
            logger.info(" Insert Details ");
            insertIntoDeviceFileDetailsDb(conn, operator, sub_feature, txn_id, totalCount, feature_file_mapping.get("output_device_db"), startTime, EndTime);

            conn.commit();

        } catch (Exception e) {
            new ErrorFileGenrator().gotoErrorFile(conn, txn_id, "  Something went Wrong. Please Contact to Ceir Admin.  ");
            new CEIRFeatureFileFunctions().UpdateStatusViaApi(conn, txn_id, 1, operator);       //1 for reject
            new CEIRFeatureFileFunctions().updateFeatureFileStatus(conn, txn_id, 5, operator, sub_feature); // update web_action_db    

            logger.error("Error.." + e);
        } finally {
            try {
                bw.close();
            } catch (Exception e) {
                logger.error("Error....." + e);
            }
        }
    }

    private void insertFromImporterManufactor(Connection conn, ResultSet rs1, HashMap<String, String> stolnRcvryDetails, HashMap<String, String> feature_file_management, HashMap<String, String> feature_file_mapping, String dateFunction, String period, String txn_id, String feature_name, String subFeature) {
        logger.info("insertFromImporterManufactor.. ");
        try {
            String qry = " select a.imei_esn_meid  , a.sn_of_device      from device_importer_db  a , device_importer_db  b  where a.sn_of_device = b.sn_of_device "
                    + "and b.imei_esn_meid = '" + rs1.getString("IMEIESNMEID").substring(0, 14) + "' "
                    //                    + " and a.imei_esn_meid  not in(select imei_esn_meid  from device_lawful_db ) "  
                    + " and a.imei_esn_meid != '" + rs1.getString("IMEIESNMEID").substring(0, 14) + "'  union  "
                    + "select a.imei_esn_meid  , a.sn_of_device        from device_manufacturer_db  a , device_manufacturer_db  b  where a.sn_of_device = b.sn_of_device and"
                    + " b.imei_esn_meid = '" + rs1.getString("IMEIESNMEID").substring(0, 14) + "' "
                    //                    + " and a.imei_esn_meid  not in(select imei_esn_meid  from device_lawful_db ) "  
                    + " and a.imei_esn_meid != '" + rs1.getString("IMEIESNMEID").substring(0, 14) + "'  ";

            logger.info("insertFromImporterManufactor.. query is ::" + qry);
            Statement stmt = conn.createStatement();
            Statement stmt1 = conn.createStatement();
            Statement stmt2 = conn.createStatement();
            Statement stmt3 = conn.createStatement();
            ResultSet rs = stmt.executeQuery(qry);
            String my_query = null;
            String device_greylist_db_qry = null;
            String device_greylist_History_db_qry = null;
            while (rs.next()) {
                my_query = "insert into " + feature_file_mapping.get("output_device_db")
                        + " (device_id_type,created_on,device_launch_date,device_status,"
                        + "device_type,imei_esn_meid,modified_on,multiple_sim_status,period,sn_of_device,txn_id,user_id  ,FEATURE_NAME , actual_imei ,tac ) "
                        + "values(" + "'" + rs1.getString("DeviceIdType") + "'," + "" + dateFunction + "," /// "dd-MMM-YY"                              
                        + "'" + rs1.getString("Devicelaunchdate") + "', '" + stolnRcvryDetails.get("divceStatus") + "'  ,'" + rs1.getString("DeviceType") + "',"
                        + "'" + rs.getString("imei_esn_meid").substring(0, 14)
                        + "'," + "" + dateFunction + "," + "'" + rs1.getString("MultipleSIMStatus") + "'," + "'"
                        + period + "'," + "'" + rs.getString("sn_of_device") + "'," + "'" + txn_id + "'," + ""
                        + feature_file_management.get("user_id") + " ,   '" + feature_name + "'  ,   '" + rs.getString("imei_esn_meid") + "' ,  '" + rs.getString("imei_esn_meid").substring(0, 8) + "'     )";

                insertInStolenprocssDb(conn, feature_file_mapping.get("output_device_db"), rs.getString("imei_esn_meid").substring(0, 14), rs.getString("sn_of_device"), txn_id);

//                    if (stolnRcvryDetails.get("operation").equals("0")) {
//                         device_greylist_db_qry = "insert into   greylist_db (created_on , imei, user_id , txn_id , mode_type  , request_type, user_type  , complain_type)   "
//                                 + "values(  " + "" + dateFunction + "," + "'" + rs.getString("imei_esn_meid")
//                                 + "'," + " ( select username from users where users.id=  "
//                                 + feature_file_management.get("user_id") + "  )  ,  " + " '" + txn_id + "', " + "'"
//                                 + stolnRcvryDetails.get("source") + "'," + "'" + stolnRcvryDetails.get("reason")
//                                 + "'," + "'" + stolnRcvryDetails.get("usertype") + "'," + "'"
//                                 + stolnRcvryDetails.get("complaint_type") + "'  " + ")";
//                    } else {
//                         device_greylist_db_qry = "delete from greylist_db where imei  = '" + rs.getString("imei_esn_meid") + "' ";
//   status on based on Block; pOA ;  Unblock :pOD 
//                         my_query = "    update    " + feature_file_mapping.get("output_device_db") + "  set device_status = '"+dvsStatus+"'  where imei_esn_meid  = '" + rs1.getString("IMEIESNMEID") + "'";
//                         my_query = "    delete from    " + feature_file_mapping.get("output_device_db")
//                                 + " where imei_esn_meid  = '" + rs.getString("imei_esn_meid") + "'";
//                    }
//                    device_greylist_History_db_qry = "insert into   greylist_db_history (created_on , imei, user_id , txn_id , mode_type  , request_type, user_type  , complain_type ,operation)   "
//                            + "values(           " + "" + dateFunction + "," + "'" + rs.getString("imei_esn_meid")
//                            + "'," + " ( select username from users where users.id=  "
//                            + feature_file_management.get("user_id") + "  )  ,  " + " '" + txn_id + "', " + "'"
//                            + stolnRcvryDetails.get("source") + "'," + "'" + stolnRcvryDetails.get("reason") + "',"
//                            + "'" + stolnRcvryDetails.get("usertype") + "'," + "'"
//                            + stolnRcvryDetails.get("complaint_type") + "' , " + "'"
//                            + stolnRcvryDetails.get("operation") + "'  " + ")";
                try {
                    stmt1.executeUpdate(my_query);
                } catch (Exception e) {
                    logger.error("Error 1 " + e);
                }
                try {
//                         stmt2.executeUpdate(device_greylist_db_qry);
                } catch (Exception e) {
                    logger.error("Error 2 " + e);
                }
                try {
//                         stmt3.executeUpdate(device_greylist_History_db_qry);
                } catch (Exception e) {
                    logger.error("Error 3 " + e);
                }

                logger.info(".. my_query is ::" + my_query);

//                    logger.info("insertFromImporterManufactor.. device_greylist_db_qry is ::" + device_greylist_db_qry);
//                    logger.info("insertFromImporterManufactor.. device_greylist_History_db_qry is ::" + device_greylist_History_db_qry);
            }

            stmt.close();
            stmt1.close();
            stmt2.close();
            stmt3.close();
            conn.commit();
        } catch (Exception e) {
            new ErrorFileGenrator().gotoErrorFile(conn, txn_id, " Something went Wrong. Please Contact to Ceir Admin.  ");
            new CEIRFeatureFileFunctions().UpdateStatusViaApi(conn, txn_id, 1, feature_name);       //1 for reject
            new CEIRFeatureFileFunctions().updateFeatureFileStatus(conn, txn_id, 5, feature_name, subFeature); // update web_action_db       
            logger.error("Error.." + e);
               System.exit(0);
        }

    }

    private void insertIntoDeviceFileDetailsDb(Connection conn, String operator, String sub_feature, String txn_id, int totalCount, String tableName, String startTime, String EndTime) {

        try {
            Statement stmt = conn.createStatement();

            String qry = " insert into  device_details_report_db ( created_on, modified_on, txn_id ,feature , sub_feature , total_insert_count  ,tableName , startTime , endTime     ) "
                    + "  values ( " + Util.defaultDateNow(true) + " , " + Util.defaultDateNow(true) + " , '" + txn_id + "', '" + operator + "', '" + sub_feature + "', '" + totalCount + "', '" + tableName + "',     TO_DATE('" + startTime + "','YYYY-MM-DD HH24:MI:SS') ,  TO_DATE('" + EndTime + "','YYYY-MM-DD HH24:MI:SS')           ) ";
            logger.info("" + qry);

            stmt.executeUpdate(qry);
            conn.commit();
        } catch (Exception e) {
            logger.error(e);
        }

    }

    public int getCounterFromDeviceDb(Connection conn, String imei) {
        Statement stmt = null;
        ResultSet rs = null;
        int counter = 0;
        try {
            imei = imei.substring(0, 14);
            stmt = conn.createStatement();
            String qry = "select counter from device_db where imei_esn_meid = '" + imei + "' ";
            logger.info("getCounterFromDeviceDb qry " + qry);
            rs = stmt.executeQuery(qry);
            while (rs.next()) {
                counter = rs.getInt(1);
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            try {
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                logger.error("Error.." + e);
            }
        }
        return counter;
    }

    private void insertInStolenprocssDb(Connection conn, String outputDb, String imei, String serailNo, String txn_id) {
        try {
            Statement stmt = conn.createStatement();
            String qry = " insert into  stolen_process_db ( created_on, modified_on, txn_id,      tableName , imei_esn_meid , SERAILNO    ) "
                    + "  values ( " + Util.defaultDateNow(true) + " , " + Util.defaultDateNow(true) + " , '" + txn_id + "', '" + outputDb + "', '" + imei + "', '" + serailNo + "'        ) ";
            logger.info("" + qry);
            stmt.executeUpdate(qry);
            conn.commit();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void rawTableCleanUp(Connection conn, String operator, String txn_id) {

        try {
            Statement raw_stmt = conn.createStatement();
            raw_stmt = conn.createStatement();
            String raw_query = " delete from " + operator + "_raw" + "  where TXN_ID ='" + txn_id + "'";
            logger.info("delete raw table .." + raw_query);
            raw_stmt.executeUpdate(raw_query);
            raw_stmt.close();
        } catch (Exception e) {
            logger.error(e);
        }

    }

}
