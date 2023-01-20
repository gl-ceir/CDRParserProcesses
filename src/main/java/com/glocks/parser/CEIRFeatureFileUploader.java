package com.glocks.parser;

import com.glocks.parser.service.ConsignmentInsertUpdate;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Logger;

import com.glocks.util.Util;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class CEIRFeatureFileUploader {

    //static Logger logger = LogManager.getLogger(CEIRFeatureFileUploader.class);
    private static final Logger logger = LogManager.getLogger(CEIRFeatureFileUploader.class);
    static StackTraceElement l = new Exception().getStackTrace()[0];

    public static void main(String[] args) {
        logger.info(" CEIRFeatureFileUploader.class");
        Connection conn = new com.glocks.db.MySQLConnection().getConnection();
        HexFileReader hfr = new HexFileReader();
        String basePath = "";
        String complete_file_path = "";
        CEIRParserMain ceir_parser_main = new CEIRParserMain();
//          int raw_upload_set_no = 1;
        String[] rawDataResult = null;
        String feature = null;
        if (args.length == 1) {
            feature = args[0];
        }
//          new ErrorFileGenrator().apiConnectionErrorFileReader();     // required later
        HashMap<String, String> feature_file_management = new HashMap<String, String>();
        CEIRFeatureFileFunctions ceirfunction = new CEIRFeatureFileFunctions();
        HashMap<String, String> feature_file_mapping = new HashMap<String, String>();
        ResultSet file_details = ceirfunction.getFileDetails(conn, 0, feature);  //select * from web_action_db limit 1 
        try {
            while (file_details.next()) {
                logger.info("Feature :" + file_details.getString("feature") + "; SubFeature :" + file_details.getString("sub_feature") + ";State : " + file_details.getString("state"));
                if (file_details.getString("state").equalsIgnoreCase("2") || file_details.getString("state").equalsIgnoreCase("3")) {
                    CEIRFeatureFileParser.cEIRFeatureFileParser(conn, feature);
                    return;
                }
                ceirfunction.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 1, file_details.getString("feature"), file_details.getString("sub_feature"));  //update web_action_db set state 1
                if (file_details.getString("feature").equalsIgnoreCase("Register Device")) {
                    if ((file_details.getString("sub_feature").equalsIgnoreCase("Register")) || (file_details.getString("sub_feature").equalsIgnoreCase("Add Device"))) {     //'Add Device'
                        ceirfunction.UpdateStatusViaApi(conn, file_details.getString("txn_id"), 0, file_details.getString("feature"));  // ravi sir api who update status
                        ceirfunction.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 2, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db           
                        break;
                    } else if (file_details.getString("sub_feature").equalsIgnoreCase("Clear")) {
                        new CEIRFeatureFileFunctions().getfromRegulizeEnterInCustom(conn, file_details.getString("txn_id"));
                        ceirfunction.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db           
                        break;
                    } else {
                        ceirfunction.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 2, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db           
                        break;
                    }
                }
                if (file_details.getString("feature").equalsIgnoreCase("Stolen") && (file_details.getString("sub_feature").equalsIgnoreCase("Approve") || file_details.getString("sub_feature").equalsIgnoreCase("Accept"))) {
                    updateDeviceDetailsByTxnId(conn, file_details.getString("txn_id"), "device_lawful_db");
                    new ConsignmentInsertUpdate().rawTableCleanUp(conn, file_details.getString("feature"), file_details.getString("txn_id"));
                    ceirfunction.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db           
                    logger.debug("Web Action 4 done ");
                    break;
                }
                if (file_details.getString("feature").equalsIgnoreCase("Stolen") && file_details.getString("sub_feature").equalsIgnoreCase("Reject")) {
                    removeDeviceDetailsByTxnId(conn, file_details.getString("txn_id"), "device_lawful_db");
                    ceirfunction.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db           
                    logger.debug("Web Action 4 done ");
                    break;
                }
                if (file_details.getString("feature").equalsIgnoreCase("Recovery") && (file_details.getString("sub_feature").equalsIgnoreCase("Approve") || file_details.getString("sub_feature").equalsIgnoreCase("Accept"))) {
                    removeDeviceDetailsByTxnId(conn, file_details.getString("txn_id"), "device_lawful_db");
                    new ConsignmentInsertUpdate().rawTableCleanUp(conn, file_details.getString("feature"), file_details.getString("txn_id"));
                    ceirfunction.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db           
                    logger.debug("Web Action 4 done ");
                    break;

                }
                if (file_details.getString("feature").equalsIgnoreCase("Recovery") && file_details.getString("sub_feature").equalsIgnoreCase("Reject")) {
                    updateDeviceDetailsByTxnId(conn, file_details.getString("txn_id"), "device_lawful_db");
                    ceirfunction.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db           
                    logger.debug("Web Action 4 done ");
                    break;
                }
                if (file_details.getString("feature").equalsIgnoreCase("Block") && (file_details.getString("sub_feature").equalsIgnoreCase("Approve") || file_details.getString("sub_feature").equalsIgnoreCase("Accept"))) {
                    updateDeviceDetailsByTxnId(conn, file_details.getString("txn_id"), "device_operator_db");
                    new ConsignmentInsertUpdate().rawTableCleanUp(conn, file_details.getString("feature"), file_details.getString("txn_id"));
                    ceirfunction.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db           
                    logger.debug("Web Action 4 done ");
                    break;
                }
                if (file_details.getString("feature").equalsIgnoreCase("Block") && file_details.getString("sub_feature").equalsIgnoreCase("Reject")) {
                    removeDeviceDetailsByTxnId(conn, file_details.getString("txn_id"), "device_operator_db");
                    ceirfunction.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db           
                    logger.debug("Web Action 4 done ");
                    break;
                }

                if (file_details.getString("feature").equalsIgnoreCase("Unblock") && (file_details.getString("sub_feature").equalsIgnoreCase("Approve") || file_details.getString("sub_feature").equalsIgnoreCase("Accept"))) {
                    removeDeviceDetailsByTxnId(conn, file_details.getString("txn_id"), "device_operator_db");
                    new ConsignmentInsertUpdate().rawTableCleanUp(conn, file_details.getString("feature"), file_details.getString("txn_id"));
                    ceirfunction.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db           
                    logger.debug("Web Action 4 done ");
                    break;

                }
                if (file_details.getString("feature").equalsIgnoreCase("Unblock") && file_details.getString("sub_feature").equalsIgnoreCase("Reject")) {
                    updateDeviceDetailsByTxnId(conn, file_details.getString("txn_id"), "device_operator_db");
                    ceirfunction.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db           
                    logger.debug("Web Action 4 done ");
                    break;

                }

                if (file_details.getString("feature").equalsIgnoreCase("Update Visa")) {
                    ceirfunction.UpdateStatusViaApi(conn, file_details.getString("txn_id"), 0, file_details.getString("feature"));
                    ceirfunction.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 2, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db           
                    break;
                }

                feature_file_mapping = ceirfunction.getFeatureMapping(conn, file_details.getString("feature"), "NOUSER");  //select * from feature_mapping_db
                feature_file_management = ceirfunction.getFeatureFileManagement(conn, feature_file_mapping.get("mgnt_table_db"), file_details.getString("txn_id"));   //select * from " + management_db + " 

                long diffTime = 0L;
//                      diffTime = Util.timeDiff(feature_file_management.get("created_on"), feature_file_management.get("modified_on"));
                logger.info("Time Difference .." + diffTime);
                if (((file_details.getString("sub_feature").equalsIgnoreCase("Register") || file_details.getString("sub_feature").equalsIgnoreCase("UPLOAD")) && (diffTime != 0))) {
                    logger.debug("  It is Regsiter/Upload and different time" + feature_file_management.get("created_on") + " ||  " + feature_file_management.get("modified_on") + " OR delete Flaag : " + feature_file_management.get("delete_flag"));
                    ceirfunction.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db           
                    break;
                }
                try {     // check it for null
                    if (file_details.getString("feature").equalsIgnoreCase("CONSIGNMENT") || file_details.getString("feature").equalsIgnoreCase("STOCK")) {
                        if (file_details.getString("sub_feature").equalsIgnoreCase("REJECT") || file_details.getString("sub_feature").equalsIgnoreCase("DELETE")) {
                            ceirfunction.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 2, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db           
                            break;
                        }
                    } else {     // optimise
                        if (feature_file_management.get("delete_flag") == null) {
                            logger.info("Delete_flag null");
                        } else {
                            if (feature_file_management.get("delete_flag").equals("0")) {
                                logger.debug("  Other Than Stock/Consignment , delete Flag : " + feature_file_management.get("delete_flag"));
                                ceirfunction.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db           
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
                }

                String errFilEpath = CEIRFeatureFileParser.getErrorFilePath(conn);
                String error_file_path = errFilEpath + file_details.getString("txn_id") + "/" + file_details.getString("txn_id") + "_error.csv";
                File errorfile = new File(error_file_path);
                if (errorfile.exists()) {     // in case of update  ,, earlier file is remove
                    logger.debug("File  moving to old Folder ");
                    errorfile = new File(errFilEpath + file_details.getString("txn_id") + "/old/");
                    if (!errorfile.exists()) {
                        errorfile.mkdir();
                    }
                    logger.debug("MKdir Done ");
                    Path temp = Files.move(Paths.get(error_file_path),
                            Paths.get(errFilEpath + file_details.getString("txn_id") + "/old/" + file_details.getString("txn_id") + "_" + java.time.LocalDateTime.now() + "_P1_error.csv"));
                    if (temp != null) {
                        logger.info("File renamed and moved successfully for P1");
                    } else {
                        logger.warn("Failed to move the file");
                    }
                }

                String user_type = ceirfunction.getUserType(conn, feature_file_management.get("user_id"), file_details.getString("feature").toUpperCase(), file_details.getString("txn_id"));      //   usertype_name from users a, usertype b
                logger.debug("user_types *****" + user_type);
                if (!(feature_file_management.get("file_name") == null || feature_file_management.get("file_name").equals(""))) {
                    logger.info("File Found... " + file_details.getString("feature"));
                    basePath = hfr.getFilePath(conn, "system_upload_filepath");  // filePath
                    if (!basePath.endsWith("/")) {
                        basePath += "/";
                    }
                    logger.info(" file basePath " + basePath);
                    complete_file_path = basePath + file_details.getString("txn_id") + "/" + feature_file_management.get("file_name");
                    logger.info("Complete file name is.... " + complete_file_path);

                    File f = new File(complete_file_path);
                    if (f.exists()) {
                        logger.info(" File Exists ");
                    } else {
                        try {
                            String query = "update web_action_db set state=" + 0 + " ,  retry_count = retry_count +1   where    txn_id='" + file_details.getString("txn_id") + "' and feature='" + file_details.getString("feature")
                                    + "' and sub_feature='" + file_details.getString("sub_feature") + "' ";
                            Statement stmt = conn.createStatement();
                            stmt.executeUpdate(query);
                            logger.info(" [ " + query + "]");
                        } catch (Exception e) {
                            logger.info("errror" + e);
                        }
                        return;
                    }

                    if (file_details.getString("sub_feature").equalsIgnoreCase("delete") || file_details.getString("sub_feature").equalsIgnoreCase("approve") || file_details.getString("sub_feature").equalsIgnoreCase("reject")) {
                        logger.info("sub_feature ...  DELETE   " + file_details.getString("sub_feature"));
                        ceirfunction.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 2, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db               
                        logger.info("WEb action status  2 Done ");
                        break;
                    }
                    if (file_details.getString("sub_feature").equalsIgnoreCase("register") || file_details.getString("sub_feature").equalsIgnoreCase("update") || file_details.getString("sub_feature").equalsIgnoreCase("upload")) {
                        logger.info("sub_feature ...   " + file_details.getString("sub_feature"));
                        if (file_details.getInt("state") == 1) {
                            logger.info(" state == 1  ");
                            getFinalDetailValues(conn, complete_file_path, feature_file_mapping.get("output_device_db"), file_details.getString("txn_id"), file_details.getString("feature"), file_details.getString("sub_feature"));
                            break;
                        } else {
                            ceirfunction.UpdateStatusViaApi(conn, file_details.getString("txn_id"), 0, file_details.getString("feature"));
                            rawDataResult = hfr.readConvertedFeatureFile(conn, feature_file_management.get("file_name"), complete_file_path, file_details.getString("feature"), basePath, file_details.getString("txn_id"), file_details.getString("sub_feature"), feature_file_mapping.get("mgnt_table_db"), user_type);
                        }
                    }
                } else {
                    logger.info("No File Found.. ");
                    if (file_details.getString("feature").equalsIgnoreCase("TYPE_APPROVED")) {
                        logger.info("TYPE_APPROVED  with .. " + file_details.getString("sub_feature"));
                        ceirfunction.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 2, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db    
                    } else {
                        logger.info("STOLEN/BLOCK/RECOVERY/UNBLOCK   ");
                        ceirfunction.UpdateStatusViaApi(conn, file_details.getString("txn_id"), 0, file_details.getString("feature"));
                        FeatureForSingleStolenBlock featureForSingleStolenBlock = new FeatureForSingleStolenBlock();
                        featureForSingleStolenBlock.readFeatureWithoutFile(conn, file_details.getString("feature"), file_details.getString("txn_id"), file_details.getString("sub_feature"), feature_file_mapping.get("mgnt_table_db"), user_type);
                    }
                }
            }
//               raw_upload_set_no = 1;
        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
                new ErrorFileGenrator().gotoErrorFile(conn, file_details.getString("txn_id"), "  Something went Wrong. Please Contact to Ceir Admin.  ");
                new CEIRFeatureFileFunctions().UpdateStatusViaApi(conn, file_details.getString("txn_id"), 1, file_details.getString("feature"));       //1 for reject
                new CEIRFeatureFileFunctions().updateFeatureFileStatus(conn, file_details.getString("txn_id"), 5, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db    
                conn.commit();
            } catch (Exception e1) {
                logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e1);
            }
        } finally {
            try {
                CEIRFeatureFileParser.cEIRFeatureFileParser(conn, feature);
            } catch (Exception e) {
                logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            }
        }
    }

    static void getFinalDetailValues(Connection conn, String complete_file_path, String outputDb, String txn_id, String feature, String sub_feature) {
        ResultSet rs = null;
        Statement stmt = null;
        String query = null;
        CEIRFeatureFileFunctions ceirfunction = new CEIRFeatureFileFunctions();
        try {
            query = "select count(*) as cont from  " + feature + "_raw  where txn_id ='" + txn_id + "'";
            logger.info("Query is " + query);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            int dbCount = 0;
            while (rs.next()) {
                dbCount = rs.getInt("cont");
            }

            Path path = Paths.get(complete_file_path);
            long lineCount = Files.lines(path).count();         //  if space is their , it fails 
            logger.info("lineCount from File  is " + lineCount);
            logger.info("lineCount from outputDB   is " + dbCount);
            if (dbCount == lineCount) {
                ceirfunction.updateFeatureFileStatus(conn, txn_id, 2, feature, sub_feature);
                logger.info("Web action 2   done");
            } else {
                query = "delete from   " + outputDb + " where txn_id ='" + txn_id + "'";
                logger.info(query);
                stmt.executeQuery(query);
                ceirfunction.updateFeatureFileStatus(conn, txn_id, 0, feature, sub_feature);
                logger.info("Web action 0   done");
            }

        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);

        }

    }

    private static void updateDeviceDetailsByTxnId(Connection conn, String txnId, String TableName) {
        insertInGreyListByTxnId(conn, txnId, TableName);
        HashMap<String, String> stolnRcvryDetails = new HashMap<String, String>();
        CEIRFeatureFileParser cEIRFeatureFileParser = new CEIRFeatureFileParser();
        stolnRcvryDetails = cEIRFeatureFileParser.getStolenRecvryDetails(conn, txnId);   //IMEI_ESN_MEID   
//          String dfnc =;   // "+ Util.defaultDateNow(true) +"

        String query = "update " + TableName + " set DEVICE_STATUS  = 'Approved' , modified_on =    " + Util.defaultDateNow(true) + " "
                + " where actual_imei  in   "
                + "(select       IMEIESNMEID   from  " + stolnRcvryDetails.get("reason") + "_raw  where  TXN_ID =  '" + txnId + "'   ) "
                + "   ";
        Statement stmt = null;
        logger.info("update   as  APPROVED  ...[" + query + "]");

        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(query);
        } catch (Exception e) {
            logger.info("Error" + e);
        } finally {
            try {
                stmt.close();
                conn.commit();
                updateModelBrandNameByTxnId(conn, txnId, TableName);
            } catch (Exception e) {
                logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            }
        }
    }

    private static void removeDeviceDetailsByTxnId(Connection conn, String txn_id, String tableName) {
        Statement stmt = null;
        Statement stmt1 = null;
        Statement stmt2 = null;
        Statement stmt3 = null;
        Statement stmt4 = null;
        ResultSet rs = null;
//          String rawTable =  tableName == "device_lawful_db" ? "Recovery" : "Unblock" ;
        String expdate = getExpiryDateValue(conn, txn_id);
        CEIRFeatureFileParser cEIRFeatureFileParser = new CEIRFeatureFileParser();
        HashMap<String, String> stolnRcvryDetails = cEIRFeatureFileParser.getStolenRecvryDetails(conn, txn_id);   //IMEI_ESN_MEID
        boolean isOracle = conn.toString().contains("oracle");
        String dateFunction = Util.defaultDate(isOracle);

//          String query = "select imei_esn_meid , user_id from  " + tableName + "  where SN_OF_DEVICE in ( "
//                  + "select SN_OF_DEVICE from  " + tableName + " where  TXN_ID = '" + txn_id + "'  "
//                  + "and  SN_OF_DEVICE is not null)  union  select   imei_esn_meid   , user_id "
//                  + "from   " + tableName + " where  TXN_ID = '" + txn_id + "'  "
//                  + "and  SN_OF_DEVICE is null  ";
        String query = " select actual_imei,  imei_esn_meid , user_id , DEVICE_ID_type  , DEVICE_TYPE  from   " + tableName + "  where SN_OF_DEVICE in (   select SN_OF_DEVICE from   " + tableName + " where imei_esn_meid in "
                + "(select SUBSTR( IMEIESNMEID, 1,14)    from  " + stolnRcvryDetails.get("reason") + "_raw  where  TXN_ID =  '" + txn_id + "'   ) "
                + "  and  SN_OF_DEVICE is not null and SN_OF_DEVICE != 'null' )      "
                + "  UNION      select actual_imei,  imei_esn_meid   , user_id , DEVICE_ID_type  , DEVICE_TYPE  from    " + tableName + "  "
                + " where imei_esn_meid  in  ( select  SUBSTR( IMEIESNMEID, 1,14)    from  " + stolnRcvryDetails.get("reason") + "_raw where  TXN_ID =  '" + txn_id + "'   )    and   SN_OF_DEVICE =  'null'   ";
//           query = "select imei_esn_meid , user_id  from  " + tableName + "  where txn_id =   ";
        logger.info(" ..:::   " + query);
        try {
            stmt1 = conn.createStatement();
            stmt = conn.createStatement();
            stmt2 = conn.createStatement();
            stmt3 = conn.createStatement();
            stmt4 = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {

                query = "insert into   greylist_db_history ( EXPIRY_DATE, modified_on ,  created_on , imei, user_id , txn_id , mode_type  , request_type, user_type  , complain_type ,operation    , operator_id , operator_name  ,actual_imei , DEVICE_ID_type  , DEVICE_TYPE  )   "
                        + "values(   " + expdate + " , " + dateFunction + ",   " + dateFunction + ",    " + "'" + rs.getString("imei_esn_meid")
                        + "'," + " ( select username from users where users.id=  "
                        + rs.getString("user_id") + "  )  ,  " + " '" + txn_id + "', " + "'"
                        + stolnRcvryDetails.get("source") + "'," + "'" + stolnRcvryDetails.get("reason") + "',"
                        + " ( select USERTYPE_NAME from usertype  where ID = (select  usertype_id from users where id =  " + rs.getString("user_id") + "  ) )   ," + "'"
                        + stolnRcvryDetails.get("complaint_type") + "' ,"
                        + "  1    , (select OPERATOR_TYPE_ID  from user_profile where USERID =   " + rs.getString("user_id") + "  )  , (select OPERATOR_TYPE_NAME  from user_profile where USERID =   " + rs.getString("user_id") + "  ) , '" + rs.getString("actual_imei") + "'  , '" + rs.getString("device_id_type") + "'  , '" + rs.getString("device_type") + "'         )";

                logger.info(" ..:::: " + query);
                stmt1.executeUpdate(query);

                try {

                    query = "delete from " + tableName + " where imei_esn_meid = '" + rs.getString("imei_esn_meid") + "' ";
                    logger.info(" ..:::: " + query);

                    stmt2.executeUpdate(query);

                } catch (Exception e) {
                    logger.error(" ..@ :" + e);
                }

                try {
                    query = "delete from greylist_db where imei  = '" + rs.getString("imei_esn_meid") + "' ";
                    logger.info(" ___ " + query);
                    stmt3.executeUpdate(query);

                } catch (Exception e) {
                    logger.error(" .. $ :" + e);
                }
                try {
                    query = "delete from black_list where imei = '" + rs.getString("imei_esn_meid") + "' ";

                    logger.info(" ___ " + query);
                    stmt3.executeUpdate(query);
                } catch (Exception e) {
                    logger.error(" .. $$ :" + e);
                }

            }
            rs.close();
            stmt.close();
            stmt1.close();
            stmt2.close();
            stmt3.close();
            stmt4.close();
            conn.commit();
        } catch (Exception e) {
            logger.error(" ..1:" + e);
        }

    }

    public static void insertInGreyListByTxnId(Connection conn, String txnId, String tableName) {
        ResultSet rs = null;
        String expdate = getExpiryDateValue(conn, txnId);

        Statement stmt = null;
        Statement stmt1 = null;
        Statement stmt2 = null;
        HashMap<String, String> stolnRcvryDetails = new HashMap<String, String>();
        CEIRFeatureFileParser cEIRFeatureFileParser = new CEIRFeatureFileParser();
        stolnRcvryDetails = cEIRFeatureFileParser.getStolenRecvryDetails(conn, txnId);   //IMEI_ESN_MEID
//                    String query = "select imei_esn_meid , user_id  from  " + tableName + "  where txn_id = '" + txnId + "'   ";
        // mar9
//        String query = " select actual_imei, imei_esn_meid , user_id , DEVICE_ID_TYPE , DEVICE_TYPE  from   " + tableName + "  where SN_OF_DEVICE in (   select SN_OF_DEVICE from   " + tableName + " where actual_imei in "
//                + "(select IMEIESNMEID  from  " + stolnRcvryDetails.get("reason") + "_raw  where  TXN_ID =  '" + txnId + "'   ) "
//                + "  and  SN_OF_DEVICE is not null and SN_OF_DEVICE != 'null' )      "
//                + "  UNION      select actual_imei,  imei_esn_meid   , user_id , DEVICE_ID_TYPE , DEVICE_TYPE from    " + tableName + "  "
//                + " where actual_imei  in  ( select IMEIESNMEID from  " + stolnRcvryDetails.get("reason") + "_raw where  TXN_ID =  '" + txnId + "'   )    and   SN_OF_DEVICE =  'null'   ";

        String query = " select actual_imei, imei_esn_meid , user_id , DEVICE_ID_TYPE , DEVICE_TYPE  from   " + tableName + " "
                + "  where   actual_imei     in (select IMEIESNMEID  from " + stolnRcvryDetails.get("reason") + "_raw  where  TXN_ID =   '" + txnId + "'   )  ";

        logger.info(" ...[" + query + "]");
        String device_greylist_db_qry = null;
        String device_greylist_History_db_qry = null;
        boolean isOracle = conn.toString().contains("oracle");
        String dateFunction = Util.defaultDate(isOracle);
        try {
            stmt = conn.createStatement();
            stmt1 = conn.createStatement();
            stmt2 = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
//                    {
//                         if (stolnRcvryDetails.get("operation").equals("0")) {
                device_greylist_db_qry = "insert into   greylist_db (EXPIRY_DATE,created_on ,modified_on , imei, user_id , txn_id , mode_type  , request_type, user_type  , complain_type , operator_id , operator_name ,actual_imei , DEVICE_ID_type  , DEVICE_TYPE  )   "
                        + "values(  " + expdate + "    ,  " + dateFunction + ",    " + dateFunction + "," + "'" + rs.getString("imei_esn_meid")
                        + "'," + " ( select username from users where users.id=  "
                        + rs.getString("user_id") + "  )  ,  " + " '" + txnId + "', " + "'"
                        + stolnRcvryDetails.get("source") + "'," + "'" + stolnRcvryDetails.get("reason")
                        + "'," + "   ( select USERTYPE_NAME from usertype  where ID = (select  usertype_id from users where id =  " + rs.getString("user_id") + "  ) )     ," + "'"
                        + stolnRcvryDetails.get("complaint_type") + "' , (select OPERATOR_TYPE_ID  from user_profile where USERID =   " + rs.getString("user_id") + "  )  , (select OPERATOR_TYPE_NAME  from user_profile where USERID =   " + rs.getString("user_id") + "  )   , '" + rs.getString("actual_imei") + "' , '" + rs.getString("device_id_type") + "'  , '" + rs.getString("device_type") + "'        )";
//                         }
                device_greylist_History_db_qry = "insert into   greylist_db_history ( EXPIRY_DATE,modified_on ,  created_on , imei, user_id , txn_id , mode_type  , request_type, user_type  , complain_type ,operation  ,   operator_id , operator_name ,actual_imei  , DEVICE_ID_type  , DEVICE_TYPE  )   "
                        + "values(   " + expdate + " ,  " + dateFunction + ",       " + dateFunction + "," + "'" + rs.getString("imei_esn_meid")
                        + "'," + " ( select username from users where users.id=  "
                        + rs.getString("user_id") + "  )  ,  " + " '" + txnId + "', " + "'"
                        + stolnRcvryDetails.get("source") + "'," + "'" + stolnRcvryDetails.get("reason") + "',"
                        + "  ( select USERTYPE_NAME from usertype  where ID = (select  usertype_id from users where id =  " + rs.getString("user_id") + "  ) )     ," + "'"
                        + stolnRcvryDetails.get("complaint_type") + "' , "
                        + " 0   , (select OPERATOR_TYPE_ID  from user_profile where USERID =   " + rs.getString("user_id") + "  )  , (select OPERATOR_TYPE_NAME  from user_profile where USERID =   " + rs.getString("user_id") + "  )  , '" + rs.getString("actual_imei") + "' , '" + rs.getString("device_id_type") + "'  , '" + rs.getString("device_type") + "'   )";
                logger.info(" " + device_greylist_db_qry);
                try {
                    stmt1.executeUpdate(device_greylist_db_qry);
                } catch (Exception e) {
//                         logger.error(" . " + e);
                }
                logger.info("" + device_greylist_History_db_qry);
                try {
                    stmt2.executeUpdate(device_greylist_History_db_qry);
                } catch (Exception e) {
                    logger.error(" .histry  " + e);
                }

//                    }
            }
            rs.close();
        } catch (Exception e) {
            logger.error("Error:: " + e);
        } finally {
            try {
                rs.close();
                stmt.close();
                stmt1.close();
                stmt2.close();
                conn.commit();
            } catch (Exception e) {
                logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            }
        }
    }

    private static String getExpiryDateValue(Connection conn, String txnId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        Statement stmt = null;
        ResultSet rs = null;
        String finalDate = null;
        String BLOCKING_TYPE = null;
        String BLOCKING_TIME_PERIOD = null;
        String defaultDays = null;
        try {
            stmt = conn.createStatement();
            String qry = "select BLOCKING_TYPE , BLOCKING_TIME_PERIOD  from stolenand_recovery_mgmt where  txn_id  = '" + txnId + "' ";
            logger.info("" + qry);
            rs = stmt.executeQuery(qry);
            while (rs.next()) {
                BLOCKING_TYPE = rs.getString("BLOCKING_TYPE");
                BLOCKING_TIME_PERIOD = rs.getString("BLOCKING_TIME_PERIOD");
            }
            logger.info("BLOCKING_TYPE " + BLOCKING_TYPE);
            if (BLOCKING_TYPE.equalsIgnoreCase("default")) {
                rs = stmt.executeQuery("select value from system_configuration_db where tag = 'GREY_TO_BLACK_MOVE_PERIOD_IN_DAY'   ");

                while (rs.next()) {
                    defaultDays = rs.getString("value");
                }
                logger.info(" defaultDays " + defaultDays);
                cal.add(Calendar.DAY_OF_MONTH, Integer.parseInt(defaultDays));
                finalDate = sdf.format(cal.getTime());
            } else if (BLOCKING_TYPE.equalsIgnoreCase("Immediate")) {
                cal.add(Calendar.DAY_OF_MONTH, 0);
                finalDate = sdf.format(cal.getTime());
            } else if (BLOCKING_TYPE.equalsIgnoreCase("tillDate")) {
                finalDate = BLOCKING_TIME_PERIOD;
            }
            logger.info(" finalDate " + finalDate);

        } catch (Exception e) {
            logger.error(e);
        }
        return "TO_DATE('" + finalDate + "','YYYY-MM-DD HH24:MI:SS')";
    }

    private static void updateModelBrandNameByTxnId(Connection conn, String txnId, String TableName) {

        String query = " select tac from  " + TableName + " where  txn_id =   '" + txnId + "'   ";
        Statement stmt = null;
        Statement stmt2 = null;
        logger.info("tac       ...[" + query + "]");
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            stmt2 = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                String updtQry = " update  " + TableName + " set model_name = (select  MODEL_NAME_NEW  from gsma_tac_db where device_id = '" + rs.getString("tac") + "'  ) "
                        + " ,   brand_name = ( select BRAND_NAME_NEW  from gsma_tac_db where device_id = '" + rs.getString("tac") + "' )    where tac =   '" + rs.getString("tac") + "'  ";
                logger.info(updtQry);
                stmt2.executeUpdate(updtQry);
            }
        } catch (Exception e) {
            logger.info("Error" + e);
        } finally {
            try {
                rs.close();
                 stmt.close();
                stmt2.close();
                conn.commit();

            } catch (Exception e) {
                logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            }
        }

    }
}

//                ResultSet my_result_set = ceir_parser_main.operatorDetails(conn, file_details.getString("feature"));     //select * from re p_schedule_config_db
//                if (my_result_set.next()) {
//                    raw_upload_set_no = my_result_set.getInt("raw_upload_set_no");
//                }
//                logger.info("raw_upload_set_no >>>>>>" + raw_upload_set_no);
//                complete_file_path = basePath + file_details.getString("txn_id") + "/" + feature_file_management.get("file_name");
//                logger.info("Complete file name is.... " + complete_file_path);
//                File uplodedfile = new File(complete_file_path);
//                logger.info("File exist Type.... " + uplodedfile.exists());
//                if (uplodedfile.exists()) {
//                    logger.info("File Exists.... ");
//                    rawDataResult = hfr.readConvertedFeatureFile(conn, feature_file_management.get("file_name"), complete_file_path, file_details.getString("feature"), basePath, raw_upload_set_no, file_details.getString("txn_id"), file_details.getString("sub_feature"), feature_file_mapping.get("mgnt_table_db"), user_type);
//                } else {
//                    logger.info("File not exists.... ");
//                    hfr.readFeatureWithoutFile(conn, file_details.getString("feature"), raw_upload_set_no, file_details.getString("txn_id"), file_details.getString("sub_feature"), feature_file_mapping.get("mgnt_table_db"), user_type);
//                }

