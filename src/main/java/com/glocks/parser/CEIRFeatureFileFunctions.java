package com.glocks.parser;

import com.gl.Rule_engine_Old.RuleEngineApplication;
import com.glocks.constants.PropertyReader;
import com.glocks.parser.service.ConsignmentInsertUpdate;
import com.glocks.util.Util;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CEIRFeatureFileFunctions {

    public static PropertyReader propertyReader;

    Logger logger = LogManager.getLogger(CEIRFeatureFileFunctions.class);
    static StackTraceElement l = new Exception().getStackTrace()[0];

    public ResultSet getFileDetails(Connection conn, int state, String feature) {
        String featureStmt = "";
        if (feature != null) {
            featureStmt = " and feature =  '" + feature + "' ";
        }
        Statement stmt = null;
        ResultSet rs = null;
        String query = null;
        String limiter = " limit 1 ";
        if (conn.toString().contains("oracle")) {
            limiter = " fetch next 1 rows only ";
        }
        String stater = "";
        if (state == 0) {
            stater = " ( state  = 0  or  state  = 1  )  ";
        } else {
            stater = "  ( state  = 2   or  state  = 3 ) ";
        }
        try {                               //where state =  " + state + " 
            query = "select * from web_action_db where " + stater + featureStmt + " and retry_count < 20  order by state desc , id asc " + limiter + "  ";
            logger.info("Query to get File Details [" + query + "]");
            stmt = conn.createStatement();
            return rs = stmt.executeQuery(query);
        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            // System.out.println("" + e);
        }
        return rs;
    }

    public HashMap<String, String> getFeatureMapping(Connection conn, String feature, String usertype_name) {
        HashMap<String, String> feature_mapping = new HashMap<String, String>();
        Statement stmt = null;
        ResultSet rs = null;
        String query = null;
        String addQuery = "";
        String limiter = " limit 1 ";
        if (conn.toString().contains("oracle")) {
            limiter = " fetch next 1 rows only ";
        }
        if (!usertype_name.equals("NOUSER")) {
            addQuery = " and usertype_name = '" + usertype_name + "'   ";
        }

        try {
            query = "select * from feature_mapping_db where  feature='" + feature + "'  " + addQuery + "    " + limiter
                    + "   ";
            logger.info("Query to get  (tFILEFUNCTIONgetFeatureMapping) File Details [" + query + "]");

            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                feature_mapping.put("usertype", rs.getString("usertype"));
                feature_mapping.put("feature", feature);
                feature_mapping.put("mgnt_table_db", rs.getString("mgnt_table_db"));
                feature_mapping.put("output_device_db", rs.getString("output_device_db"));
                feature_mapping.put("USERTYPE_NAME", rs.getString("USERTYPE_NAME"));
            }
        } catch (Exception e) {
            logger.info("  getFeatureMapping errror" + e);

        } finally {
            try {
                rs.close();
                stmt.close();
            } catch (Exception e) {
                logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            }
        }

        return feature_mapping;

    }

    public HashMap<String, String> getFeatureFileManagement(Connection conn, String management_db, String txn_id) {
        HashMap<String, String> feature_file_management_details = new HashMap<String, String>();
        Statement stmt = null;
        ResultSet rs = null;
        String query = null;
        try {
            query = "select * from " + management_db + " where  txn_id='" + txn_id + "'";
            logger.info("Query to (getFeatureFileManagement) File Details [" + query + "]");
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                feature_file_management_details.put("user_id", rs.getString("user_id"));
                feature_file_management_details.put("file_name", rs.getString("file_name"));
                feature_file_management_details.put("created_on", rs.getString("created_on"));
                feature_file_management_details.put("modified_on", rs.getString("modified_on"));
                feature_file_management_details.put("delete_flag", rs.getString("delete_flag"));
            }
        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        } finally {
            try {
                rs.close();
                stmt.close();
            } catch (Exception e) {
                logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            }
        }

        return feature_file_management_details;

    }

    public void updateFeatureFileStatus(Connection conn, String txn_id, int status, String feature, String subfeature) {
        Statement stmt = null;
        try {
            String query = "update web_action_db set state=" + status + "  where    txn_id='" + txn_id + "' and feature='" + feature
                    + "' and sub_feature='" + subfeature + "' ";
            stmt = conn.createStatement();
            stmt.executeUpdate(query);
        } catch (Exception e) {
            logger.info("errror" + e);
        } finally {
            try {
                stmt.close();
                conn.commit();
            } catch (Exception e) {
                logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            }
        }

    }

    public void updateFeatureManagementStatus(Connection conn, String txn_id, int status, String table_name, String main_type) {
        String query = "";
        Statement stmt = null;
        if (table_name.equalsIgnoreCase("stolenand_recovery_mgmt")) {
            main_type = "file";
        }
        query = "update " + table_name + " set   " + main_type.trim().toLowerCase() + "_status=" + status
                + " where txn_id='" + txn_id + "'";
        logger.info("update  " + main_type.toLowerCase() + "_status db..[" + query + "]");
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(query);

        } catch (Exception e) {

            logger.info("Error at updateFeatureManagementStatus.." + e);
        } finally {
            try {
                stmt.close();
                conn.commit();
            } catch (Exception e) {
                logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            }
        }

    }

    public String getUserType(Connection conn, String user_id, String main_type, String txn_id) {
        String user_type = null;
        String query = null;
        Statement stmt = null;
        ResultSet rs = null;

        query = "select b.usertype_name as usertype_name from users a, usertype b where a.usertype_id=b.id and a.id='" + user_id + "'";

        if (main_type.toLowerCase().equals("stock")) {
            query = "select  role_type  as usertype_name from stock_mgmt  where txn_id = '" + txn_id + "'"; // hardcodeed
        }
        logger.info(" Query at (getUserType)  for " + main_type + ":::" + query);
        try {

            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                user_type = rs.getString(1);
            }
            logger.info("user_type.. +" + user_type);

        } catch (Exception e) {
            logger.warn("NO users Found");
//               logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        }
        try {
            rs.close();
            stmt.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        }
        return user_type;
    }

    public void UpdateStatusViaApi(Connection conn, String txn_id, int Action, String feature) {
        logger.info("UpdateStatus ViaApi..  : 0 - Processing; 1 -Reject; 2- PEnding by admin  ");
        propertyReader = new PropertyReader();
        ResultSet rs1 = null;
        Statement stmt = null;
        String tag = null;
        String apiURI = null;
        String responseBody = null;
        String featureId = "";
        String requestType = "";
        // String txn_id = map.get("txn_id");;
        String userId = "";

        if (feature.equalsIgnoreCase("Register Device")) {
            apiURI = "RegisterDevice_api_URI";
            responseBody = "{\n"
                    + "\"action\": " + Action + "   ,\n"
                    + "\"txnId\": \"" + txn_id + "\",\n"
                    + "\"userType\": \"CEIRSYSTEM\"\n"
                    + "}";
        }
        if (feature.equalsIgnoreCase("Update Visa")) {
            apiURI = "VisaUpdate_api_URI";
            responseBody = "{\n"
                    + "\"action\": " + Action + "   ,\n"
                    + "\"txnId\": \"" + txn_id + "\",\n"
                    + "\"userType\": \"CEIRSYSTEM\"\n"
                    + "}";
        }
        if (feature.equalsIgnoreCase("stock")) {
            apiURI = "stock_api_URI";
            responseBody = "{  \"action\": " + Action + " ,  \"remarks\":\"\",  \"roleType\": \"CEIRSystem\",  \"txnId\": \"" + txn_id + "\"  ,\"featureId\" : 4 }";
        }
        if (feature.equalsIgnoreCase("consignment")) {
            apiURI = "mail_api_path";
            responseBody = "{  \"action\":    " + Action
                    + "    ,  \"requestType\": 0,  \"roleType\": \"CEIRSYSTEM\",  \"txnId\": \"" + txn_id
                    + "\"  ,\"featureId\" : 3 }";
        }

        if ((feature.equalsIgnoreCase("stolen") || feature.equalsIgnoreCase("recovery")
                || feature.equalsIgnoreCase("block") || feature.equalsIgnoreCase("unblock"))) {
            apiURI = "stolen-recovery_mailURI";
            HashMap<String, String> map = CEIRFeatureFileParser.getStolenRecvryDetails(conn, txn_id);
            featureId = (map.get("request_type").equals("0") || map.get("request_type").equals("1")) ? "5" : "7";
            requestType = map.get("request_type");
            userId = map.get("user_id");

            responseBody = " {\n" + "\"action\":" + Action + ",\n" + "\"featureId\":" + featureId + ",\n"
                    + "\"remarks\":\"\",\n" + "\"requestType\":" + requestType + ",\n"
                    + "\"roleType\":\"CEIRSYSTEM\",\n" + "\"roleTypeUserId\":0,\n" + "\"txnId\":\"" + txn_id + "\",\n"
                    + "\"userId\":" + userId + ",\n" + "\"userType\": \"CEIRSYSTEM\"\n" + "}  ";
        }

        String query = "select value from system_configuration_db where tag='" + apiURI + "'";
        try {
            logger.info("Query is " + query);
            logger.info("............   " + responseBody + " ");
            stmt = conn.createStatement();
            rs1 = stmt.executeQuery(query);
            while (rs1.next()) {
                tag = rs1.getString("value");
            }
            stmt.close();

            logger.info(" Tag before Replace  " + tag);
            String aa = propertyReader.getConfigPropValue("localIp").trim();
            logger.info("  aa    " + aa);
//               tag = tag.replace("$LOCAL_IP1", aa);
            tag = tag.replace("$LOCAL_IP", aa);
            logger.info("tag after Replace  " + tag);
            logger.info("tag after Replace  " + tag);

            HttpApiConnecter(tag, responseBody);
        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        }
    }

    public void HttpApiConnecter(String tag, String responseBody) {
        try {
            URL url = new URL(tag);
            HttpURLConnection hurl = (HttpURLConnection) url.openConnection();
            hurl.setRequestMethod("PUT");
            hurl.setDoOutput(true);
            hurl.setRequestProperty("Content-Type", "application/json");
            hurl.setRequestProperty("Accept", "application/json");
            OutputStreamWriter osw = new OutputStreamWriter(hurl.getOutputStream());
            osw.write(responseBody);
            osw.flush();
            osw.close();
            logger.info("DatA Putted");
            hurl.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(hurl.getInputStream()));
            String temp = null;
            StringBuilder sb = new StringBuilder();
            while ((temp = in.readLine()) != null) {
                sb.append(temp).append(" ");
            }
            String result = sb.toString();
            in.close();
            logger.info("OUTPUT result is .." + result);
        } catch (Exception e) {
            logger.error(responseBody + "  " + e);
            new ErrorFileGenrator().apiConnectionErrorFileWriter(tag, responseBody);
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        }

    }

    public HttpURLConnection getHttpConnection(String url, String type) {
        URL uri = null;
        HttpURLConnection con = null;
        try {
            uri = new URL(url);
            con = (HttpURLConnection) uri.openConnection();
            con.setRequestMethod(type); // type: POST, PUT, DELETE, GET
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setConnectTimeout(60000); // 60 secs...
            con.setReadTimeout(60000); // 60 secs
            con.setRequestProperty("Accept-Encoding", "application/json");
            con.setRequestProperty("Content-Type", "application/json");
        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        }
        return con;
    }

    public void updateFeatureManagementDeleteStatus(Connection conn, String txn_id, int status, String table_name) {
        String query = "";
        Statement stmt = null;
        query = "update " + table_name + " set delete_status =" + status + " where txn_id='" + txn_id + "'";
        logger.info("update delete status [" + query + "]");
        // System.out.println("update delete status [" + query + "]");
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(query);
            conn.commit();
        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            }
        }

    }

    public Map<String, String> getUserRoleType(Connection conn, String txn_id) {
        Statement stmt = null;
        ResultSet rs = null;
        String query = null;
        HashMap<String, String> map = new HashMap<String, String>();

        try {
            query = "select role_type , user_type  from stock_mgmt where  txn_id = '" + txn_id + "'   ";
            logger.info("Query to get getUserRoleType [" + query + "]");
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                map.put("user_type", rs.getString("user_type"));
                map.put("role_type", rs.getString("role_type"));
            }
        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        }
        return map;
    }

    void getfromRegulizeEnterInCustom(Connection conn, String txn_id) {
        Statement stmt = null;
        Statement stmt1 = null;
        ResultSet rs = null;
        String device_db_query = null;
        String query = null;
        String InsrtQry = null;
        boolean isOracle = conn.toString().contains("oracle");
        String dateFunction = Util.defaultDate(isOracle);
        String period = new HexFileReader().checkGraceStatus(conn);

        try {
            String ValImei = "";
            for (int i = 1; i < 5; i++) {
                if (i == 1) {
                    ValImei = "first_imei";
                }
                if (i == 2) {
                    ValImei = "second_imei";
                }
                if (i == 3) {
                    ValImei = "third_imei";
                }
                if (i == 4) {
                    ValImei = "fourth_imei";
                }
                query = "select * from regularize_device_db where  txn_id = '" + txn_id + "' and  " + ValImei + " is not null   ";
                logger.info(" / " + query);
                // 0 tax paid , 1 - not tax Paid, 2 : regulized   , 3  ????
                stmt = conn.createStatement();
                stmt1 = conn.createStatement();
                rs = stmt.executeQuery(query);
                int dvcDbCounter = 0;
                try {
                    while (rs.next()) {
                        InsrtQry = "insert  into device_custom_db( CREATED_ON , modified_on , DEVICE_ID_TYPE, DEVICE_STATUS,DEVICE_TYPE,IMEI_ESN_MEID,MULTIPLE_SIM_STATUS,FEATURE_NAME ,TXN_ID,user_id , period , actual_imei) "
                                + "values (" + dateFunction + " , " + dateFunction + " ,  '" + rs.getString("DEVICE_ID_TYPE") + "' , '" + rs.getString("DEVICE_STATUS") + "', '" + ((rs.getString("DEVICE_TYPE") == null || rs.getString("DEVICE_TYPE").equals("")) ? "NA" : rs.getString("DEVICE_TYPE")) + "'  , '" + rs.getString("" + ValImei + "").substring(0, 14) + "' , '" + rs.getString("MULTI_SIM_STATUS") + "' , 'Register Device' , '" + rs.getString("TXN_ID") + "','" + rs.getString("TAX_COLLECTED_BY") + "' , '" + period + "'   , '" + rs.getString("" + ValImei + "") + "'   )";
                        logger.info(" insert qury  [" + InsrtQry + "]");
                        stmt1.executeUpdate(InsrtQry);

                        dvcDbCounter = new ConsignmentInsertUpdate().getCounterFromDeviceDb(conn, rs.getString("" + ValImei + "").substring(0, 14));
                        if (dvcDbCounter == 0) {
                            device_db_query = "insert  into device_db( counter ,  CREATED_ON , modified_on , DEVICE_ID_TYPE, DEVICE_STATUS,DEVICE_TYPE,IMEI_ESN_MEID,MULTIPLE_SIM_STATUS,FEATURE_NAME ,TXN_ID,period ,actual_imei ) "
                                    + "values (  1 , " + dateFunction + " , " + dateFunction + " ,  '" + rs.getString("DEVICE_ID_TYPE") + "' , '" + rs.getString("DEVICE_STATUS") + "', '" + ((rs.getString("DEVICE_TYPE") == null  || rs.getString("DEVICE_TYPE").equals("")     ) ? "NA" : rs.getString("DEVICE_TYPE")) + "' , '" + rs.getString("" + ValImei + "").substring(0, 14) + "' , '" + rs.getString("MULTI_SIM_STATUS") + "' , 'Register Device' , '" + rs.getString("TXN_ID") + "', '" + period + "'  , '" + rs.getString("" + ValImei + "") + "'     )";
                        } else {
                            device_db_query = "update  device_db set counter = " + (dvcDbCounter + 1) + " where imei_esn_meid =   '" + rs.getString("" + ValImei + "").substring(0, 14) + "'   ";
                        }
                        logger.info(" insert device_db_query  [" + device_db_query + "]");
                        stmt1.executeUpdate(device_db_query);
                        markUserRegtoAllowedActiveDb(conn, rs.getString("" + ValImei + ""));
                        removeImeiFromGreyBlackDb(conn, rs.getString("" + ValImei + ""));
                    }
                } catch (Exception e) {
                    logger.error(".. " + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
                }
            }
        } catch (Exception e) {
            logger.error(".... " + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        } finally {
            try {
                rs.close();
                stmt.close();
                stmt1.close();
//                    stmt2.close();
            } catch (SQLException ex) {
                // java.util.logging.LogManager.getLogger(CEIRFeatureFileFunctions.class.getName()).log(Level.SEVERE, null, ex);
                org.apache.logging.log4j.LogManager.getLogger(CEIRFeatureFileFunctions.class.getName());
            }
        }
    }

    private void markUserRegtoAllowedActiveDb(Connection conn, String ValImei) {
        CEIRFeatureFileParser cEIRFeatureFileParser = null;
        Statement stmt = null;
        String prdType = cEIRFeatureFileParser.checkGraceStatus(conn);
        logger.info("  PEROID [" + prdType + "]");
        try {
            ValImei = ValImei.substring(0, 14);
            stmt = conn.createStatement();
            if (prdType.equalsIgnoreCase("post_grace")) {
                String qury = " update device_usage_db set action = 'ALLOWED' where IMEI =  '" + ValImei + "'  and  action = 'USER_REG' ";
                logger.info("  " + qury);
                stmt.executeUpdate(qury);
            }
        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        } finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error("Error.." + e);
            }
        }

    }

    private void removeImeiFromGreyBlackDb(Connection conn, String ValImei) {
        Statement stmt = null;
        String qury = null;
        try {
            ValImei = ValImei.substring(0, 14);
            stmt = conn.createStatement();
            qury = " delete from greylist_db where imei= '" + ValImei + "' ";
            logger.info("  " + qury);
            stmt.executeUpdate(qury);
            qury = " delete from black_list where imei  = '" + ValImei + "'  ";
            logger.info("  " + qury);
            stmt.executeUpdate(qury);
        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        } finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error("Error.." + e);
            }
        }
    }

    void updateStatusOfRegularisedDvc(Connection conn, String txn_id) {

        Statement stmt = null;
        ResultSet rs = null;
        String query = null;
        try {
            stmt = conn.createStatement();
            String ValImei = "";
            BufferedWriter bw = null;
            int resultValue = 1;
            String action_output = null;
            for (int i = 1; i < 5; i++) {
                if (i == 1) {
                    ValImei = "first_imei";
                }
                if (i == 2) {
                    ValImei = "second_imei";
                }
                if (i == 3) {
                    ValImei = "third_imei";
                }
                if (i == 4) {
                    ValImei = "fourth_imei";
                }
                query = "select * from regularize_device_db  where  txn_id = '" + txn_id + "'  and  " + ValImei + " is not null  and TAX_PAID_STATUS =  2 ";                         /////
                logger.info("" + query);
                rs = stmt.executeQuery(query);
                while (rs.next()) {

                    String[] ruleArr = {"EXIST_IN_END_USER_DEVICE_DB", "1", "IMEI", rs.getString("" + ValImei + "").substring(0, 14)};   // typeApproceTac with status =3 
                    action_output = RuleEngineApplication.startRuleEngine(ruleArr, conn, bw);
                    logger.debug("action_output is " + action_output);
                    if (action_output.equalsIgnoreCase("Yes")) {
                        resultValue = 0;
                        return;
                    } else {
                        insertinEndUserDvcDb(conn, ValImei, rs);
                    }
                    logger.debug(".||.");
                    markUserRegtoAllowedActiveDb(conn, rs.getString("" + ValImei + ""));
                    logger.debug("....");
                    removeImeiFromGreyBlackDb(conn, rs.getString("" + ValImei + ""));
                }
            }
            conn.commit();
        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        } finally {
            try {
                logger.debug("final stat ");
                rs.close();
                stmt.close();
            } catch (SQLException ex) {
                logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + ex);
            }
        }
    }

    private void insertinEndUserDvcDb(Connection conn, String ValImei, ResultSet rs) {
        String dfnc = Util.defaultDateNow(true);   // "+dfnc+"
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            String qury = "insert into end_user_device_db ( imei_esn_meid , actual_imei, created_on , modified_on, CURRENCY,	DEVICE_ID_TYPE,	DEVICE_SERIAL_NUMBER,	DEVICE_STATUS,	DEVICE_TYPE ,	TAX_PAID_STATUS,	TXN_ID	,USER_ID	,CREATOR_USER_ID	,ORIGIN   ) values"
                    + "( '" + rs.getString("" + ValImei + "").substring(0, 14) + "', '" + rs.getString("" + ValImei + "") + "',  " + dfnc + " ," + dfnc + ", '" + rs.getString("CURRENCY") + "', '" + rs.getString("DEVICE_ID_TYPE") + "', '" + rs.getString("DEVICE_SERIAL_NUMBER") + "', '" + rs.getString("DEVICE_STATUS") + "', "
                    + " '" + rs.getString("DEVICE_TYPE") + "','" + rs.getString("TAX_PAID_STATUS") + "', '" + rs.getString("TXN_ID") + "', '" + rs.getString("USER_ID") + "', '" + rs.getString("CREATOR_USER_ID") + "',  '" + rs.getString("ORIGIN") + "'   ) ";

            logger.info(" insertinEndUserDvcDb Query  " + qury);
            stmt.executeUpdate(qury);
        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        } finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error("Error.." + e);
            }

        }

    }

}

//        try {
//          URL  url = new URL(tag);
//            HttpURLConnection hurl = (HttpURLConnection) url.openConnection();
//            hurl.setRequestMethod("PUT");
//            hurl.setDoOutput(true);
//            hurl.setRequestProperty("Content-Type", "application/json");
//            hurl.setRequestProperty("Accept", "application/json");
//
//            String payload = "{  action: " + action + ",  requestType: 0,  roleType: CEIRSYSTEM,  txnId: " + txn_id + ",featureId : 3 }";
//
//            OutputStreamWriter osw = new OutputStreamWriter(hurl.getOutputStream());
//            osw.write(payload);
//            osw.flush();
//            osw.close();
//            logger.info("Consignment status have Update SuccessFully  with status" + action + " for txn_d" + txn_id);
//        } catch (MalformedURLException e) {
//            // TODO Auto-generated catch block
//            logger.info("errror" + e);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            logger.info("errror" + e);
//        }
// con = getHttpConnection(tag, "PUT");
//            //you can add any request body here if you want to post
//            logger.info("conn. Reutrned");
//            con.setDoInput(true);
//            con.setDoOutput(true);
//            DataOutputStream out = new DataOutputStream(con.getOutputStream());
//            out.writeBytes(reqbody);
//            out.flush();
//            out.close();
//	public void pdateFeatureManagementStatus(Connection conn, String txn_id,int status,String table_name) {
//		String query = "";
//		Statement stmt = null;
//		query = "update "+table_name+" set status="+status+" where txn_id='"+txn_id+"'";			
//		logger.info("update management db status ["+query+"]");
//		 // System.out.println("update management db status["+query+"]");
//		try {
//			stmt = conn.createStatement();
//			stmt.executeUpdate(query);
//			conn.commit();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		finally{
//			try {
//				stmt.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		
//
//void deleteFromCustom1(Connection conn, String txn_id, String string0) {
//
//          Statement stmt = null;
//          ResultSet rs = null;
//          Statement stmt1 = null;
//          Statement stmt3 = null;
//          ResultSet rs1 = null;
//          String query = null;
//          String InsrtQry = null;
//          boolean isOracle = conn.toString().contains("oracle");
//          String dateFunction = Util.defaultDate(isOracle);
//
//          try {
//               String ValImei = "";
//               for (int i = 1; i < 5; i++) {
//                    if (i == 1) {
//                         ValImei = "first_imei";
//                    }
//                    if (i == 2) {
//                         ValImei = "second_imei";
//                    }
//                    if (i == 3) {
//                         ValImei = "third_imei";
//                    }
//                    if (i == 4) {
//                         ValImei = "fourth_imei";
//                    }
//                    query = "select * from regularize_device_db where  txn_id = '" + txn_id + "'  where " + ValImei + " is not null  ";
//
////                    stmt = conn.createStatement();
////                    rs = stmt.executeQuery(query);
////                    while (rs.next()) {
////                         InsrtQry = "insert  into device_custom_db_au d(CREATED_ON , DEVICE_ID_TYPE, DEVICE_STATUS,DEVICE_TYPE,IMEI_ESN_MEID,MULTIPLE_SIM_STATUS,FEATURE_NAME ,TXN_ID) "
////                                 + "values (" + dateFunction + " , '" + rs.getString("DEVICE_ID_TYPE") + "' , '" + rs.getString("DEVICE_STATUS") + "', '" + rs.getString("DEVICE_TYPE") + "' , '" + rs.getString("" + ValImei + "") + "' , '" + rs.getString("MULTIPLE_SIM_STATUS") + "' , 'Register Device' , '" + rs.getString("TXN_ID") + "'     )";
////                         logger.info(" insert qury  [" + InsrtQry + "]");
////
////                         stmt1 = conn.createStatement();
////                         stmt1.executeQuery(query);
////
////                    }
//               }
//               stmt3 = conn.createStatement();
//               stmt3.executeQuery("delete from device_custom_db  where  txn_id = '" + txn_id + "' ");
//
//               conn.commit();
//          } catch (Exception e) {
//               logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
//          }
//
//     }

