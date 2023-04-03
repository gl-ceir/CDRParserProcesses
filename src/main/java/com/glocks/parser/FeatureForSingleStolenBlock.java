/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.glocks.parser;

import com.glocks.util.Util;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author user
 */
public class FeatureForSingleStolenBlock {

    Logger logger = LogManager.getLogger(FeatureForSingleStolenBlock.class);

    public void readFeatureWithoutFile(Connection conn, String feature, String txn_id, String sub_feature, String mgnt_table_db, String user_type) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            logger.info("  readFeatureWithoutFile ");
            new CEIRFeatureFileFunctions().updateFeatureFileStatus(conn, txn_id, 2, feature, sub_feature); // update web_action_db    

            map.put("feature", feature);
            map.put("sub_feature", sub_feature);
            map.put("mgnt_table_db", mgnt_table_db);
            map.put("user_type", user_type);
            map.put("txn_id", txn_id);
            deleteFromRawTable(conn, txn_id, feature);
            map = getstolenandRecoveryDetails(conn, map);
            logger.info("  request_type is " + map.get("request_type"));
            logger.info("  source_type is " + map.get("source_type"));
            if (map.get("request_type").equals("0") || map.get("request_type").equals("2")) {
                logger.info("welcome to Stolen/Block flow");
                if (map.get("source_type").equals("4") || map.get("source_type").equals("5")) {
                    logger.info("welcome to Stolen/Block flow Indidual ");
                    stolenFlowStartSingle(conn, map);
                } else {
                    logger.info("Error is only for Single  Stolen/Block,,Something went wrong ");
                }
            }
            if (map.get("request_type").equals("1") || map.get("request_type").equals("3")) {
                logger.info("welcome to Recovery / Unblock Flow");
                if (map.get("source_type").equals("4") || map.get("source_type").equals("5")) {
                    logger.info("welcome to Recovery/Unblock flow Single ");
                    recoverFlowStartSingle(conn, map);
                } else {
                    logger.info("Error is only for Single Recovery/Unblock ,,Something went wrong ");
                }
            }
        } catch (Exception e) {
            logger.info("  Error  is  + " + e);
            new ErrorFileGenrator().gotoErrorFile(conn, txn_id, "  Something went Wrong. Please Contact to Ceir Admin.  ");
            new CEIRFeatureFileFunctions().UpdateStatusViaApi(conn, txn_id, 1, feature);       //1 for reject
            new CEIRFeatureFileFunctions().updateFeatureFileStatus(conn, txn_id, 5, feature, sub_feature); // update web_action_db    
        } finally {
            try {
                conn.commit();

            } catch (Exception ex) {
                logger.info("  Error  is  + " + ex);
            }
        }
    }

    private void recoverFlowStartSingle(Connection conn, Map<java.lang.String, java.lang.String> map) {
        try {
            ErrorFileGenrator errFile = new ErrorFileGenrator();
            String id = map.get("id");
            String txn_id = map.get("txn_id");
            logger.info("recoverFlowStartSingle with   id  " + id);
            int noImeiCounter = 0;
            for (int i = 1; i < 5; i++) {
                logger.info(" going to chck exists imei  ");
                String resllt = chckOtherExistingImei(conn, map, i);

                if (resllt.equals("PRO")) {
                    logger.info(" imei exist in sinle_stolenImei  ");

                    String sing_imei = "";
                    String stln_imei = "";

                    if (i == 1) {
                        sing_imei = "first_imei";
                        stln_imei = "imei_esn_meid1";
                    }
                    if (i == 2) {
                        sing_imei = "second_imei";
                        stln_imei = "imei_esn_meid2";
                    }
                    if (i == 3) {
                        sing_imei = "third_imei";
                        stln_imei = "imei_esn_meid3";
                    }
                    if (i == 4) {
                        sing_imei = "fourth_imei";
                        stln_imei = "imei_esn_meid4";
                    }

                    Statement stmt1 = conn.createStatement();
                    String qury1 = "";
                    if (map.get("request_type").equals("1")) {
                        qury1 = " select " + stln_imei + " from stolen_individual_userdb  where stolen_id  =" + id
                                + " "; // , model_number, device_brand_name, contact_number
                    } else {
                        qury1 = "select " + sing_imei + "  from single_imei_details where txn_id ='" + txn_id + "' ";
                    }
                    logger.info(" Query i... " + qury1);
                    ResultSet res = stmt1.executeQuery(qury1);
                    String res1 = "0";
                    try {
                        while (res.next()) {
                            res1 = res.getString(1);
                        }
                    } catch (Exception e) {
                        logger.info("Error..getImedn.." + e);
                    }

                    String valz = res1;
                    logger.info("Imei<,....." + valz);
                    map.put("imei_esn_meid", valz);
                    stmt1.close();
                    try {
                        if (valz == "0") {
                            logger.info("imei Null");
                            logger.info("File error... IMEI which are provided,  mainSingle in present Database.");
                            errFile.gotoErrorFile(conn, txn_id, " IMEI/ESN/MEID is not present in the system  ");
                            failPasstatusUpdator(conn, map, 1);
                        } else {
                            insertinRawtable(conn, map);
                            failPasstatusUpdator(conn, map, 0);
                        }

                    } catch (Exception e) {
                        logger.info("File error at catch  " + e);
                    }
                } else {
                    noImeiCounter++;
                }
            }
            logger.info(" noImeiCounter. " + noImeiCounter);
            if (noImeiCounter == 4) {
                logger.info("File error... No imei is provided for recovery/unblock.");
                errFile.gotoErrorFile(conn, txn_id, " Error Code :CON_RULE_0026, Error Description :   No  IMEI/ESN/MEID is provided for  " + (map.get("request_type").equals("1") ? "Recovery" : "Unblocking") + " ");
                failPasstatusUpdator(conn, map, 1);  // reject
            }

        } catch (Exception e) {
            logger.error("  Error  is  + " + e);
        }

    }

    private String chckOtherExistingImei(Connection conn, Map<String, String> map, int i) {
        String otpt = "";
        try {

            Statement stmt = conn.createStatement();
            ResultSet resultmsdn = null;
            String id = map.get("id");
            String txn_id = map.get("txn_id");

            String sing_imei = "";
            String stln_imei = "";

            if (i == 1) {
                sing_imei = "first_imei";
                stln_imei = "imei_esn_meid1";
            }
            if (i == 2) {
                sing_imei = "second_imei";
                stln_imei = "imei_esn_meid2";
            }
            if (i == 3) {
                sing_imei = "third_imei";
                stln_imei = "imei_esn_meid3";
            }
            if (i == 4) {
                sing_imei = "fourth_imei";
                stln_imei = "imei_esn_meid4";
            }

            String qury1 = "";
            if (map.get("request_type").equals("1")) {
                qury1 = " select " + stln_imei + " from stolen_individual_userdb where stolen_id  =" + id + " "; // ,
                // model_number,
                // device_brand_name,
                // contact_number
            } else {
                qury1 = "select " + sing_imei + "  from single_imei_details where txn_id ='" + txn_id + "' ";
            }
            logger.info(" chckOtherExistingImei quury  " + qury1);

            resultmsdn = stmt.executeQuery(qury1);
            String res = "";
            try {
                while (resultmsdn.next()) {
                    res = resultmsdn.getString(1);
                }
            } catch (Exception e) {
                logger.info(" Resultset error " + e);
            }
            if (res == null || res == "" || res.equals("") || res.equals("0")) {
                otpt = "NA";
            } else {
                otpt = "PRO";
            }
            stmt.close();
            logger.info(" chckOtherExistingImei output  " + otpt);
        } catch (Exception e) {
            logger.info(" chckOtherExistingImei error " + e);
        }

        return otpt;
    }

    public Map getstolenandRecoveryDetails(Connection conn, Map<String, String> map) {
        Statement stmt = null;
        try {
            ResultSet resultmsdn = null;
            stmt = conn.createStatement();

            String qury = " select  id,  request_type,  source_type ,file_name ,quantity, user_id from stolenand_recovery_mgmt where txn_id  = '"
                    + map.get("txn_id") + "'  ";
            logger.info("qury: +" + qury);
            resultmsdn = stmt.executeQuery(qury);
            while (resultmsdn.next()) {
                map.put("id", resultmsdn.getString("id"));
                map.put("request_type", resultmsdn.getString("request_type"));
                map.put("source_type", resultmsdn.getString("source_type"));
                map.put("file_name", resultmsdn.getString("file_name"));
                map.put("quantity", resultmsdn.getString("quantity"));
                map.put("user_id", resultmsdn.getString("user_id"));
            }
            resultmsdn.close();
            stmt.close();
        } catch (Exception e) {
            logger.info("Excepton: +" + e);
        }
        return map;
    }

    void stolenFlowStartSingle(Connection conn, Map<java.lang.String, java.lang.String> map) {
        String id = map.get("id");
        String txn_id = map.get("txn_id");
        int cnt = 1;
        String sing_imei = "";
        String stln_imei = "";
        String qury = null;
        String ty = null;
        Statement stmt = null;
        try {
            logger.info("stolenFlowStart with   id  " + id);
            for (int i = 1; i < 5; i++) {
                if (i == 1) {
                    sing_imei = "first_imei";
                    stln_imei = "imei_esn_meid1";
                }
                if (i == 2) {
                    sing_imei = "second_imei";
                    stln_imei = "imei_esn_meid2";
                }
                if (i == 3) {
                    sing_imei = "third_imei";
                    stln_imei = "imei_esn_meid3";
                }
                if (i == 4) {
                    sing_imei = "fourth_imei";
                    stln_imei = "imei_esn_meid4";
                }

                if (map.get("request_type").equals("2")) {
                    qury = "select " + sing_imei
                            + "    as   imei_esn_meid , second_imei  as model_number,third_imei  as device_brand_name,  fourth_imei  as contact_number from single_imei_details where txn_id ='" + txn_id + "'   ";
                    ty = "BLOCK";
                }
                if (map.get("request_type").equals("0")) {
                    qury = " select " + stln_imei
                            + "    as imei_esn_meid , model_number, device_brand_name, contact_number from stolen_individual_userdb where stolen_id  =" + id + " ";
                    ty = "STOLEN";
                }
                stmt = conn.createStatement();
                logger.info("  Flow type " + ty);
                logger.info(" query is.... " + qury);
                ResultSet resultmsdn = null;
                resultmsdn = stmt.executeQuery(qury);
                try {
                    while (resultmsdn.next()) {
                        map.put("imei_esn_meid", resultmsdn.getString("imei_esn_meid"));
                        map.put("model_number", resultmsdn.getString("model_number"));
                        map.put("device_brand_name", resultmsdn.getString("device_brand_name"));
                        map.put("contact_number", resultmsdn.getString("contact_number"));
                    }
                } catch (Exception e) {
                    logger.info("Error..getImedn.." + e);
                }
                stmt.close();
//                conn.commit();
//                if (map.get("contact_number") == null) {
//                    logger.info(" null");
//                }

                if (i == 1) {
                    if ( (map.get("contact_number") == null  || map.get("contact_number").equals("")   ) &&   (  map.get("imei_esn_meid") == null   || map.get("imei_esn_meid").equals("")    )  ){            //      //msisdn is 
                        logger.info(" neither msisdn nor imei "); 
                        ErrorFileGenrator errFile = new ErrorFileGenrator();
                        errFile.gotoErrorFile(conn, txn_id, "Error Code :CON_RULE_0027, Error Description :  Neither IMEI/ESN/MEID nor Msisdn is provided for  " + (map.get("request_type").equals("1") ? "Recovery" : "Unblocking") + " ");
                        failPasstatusUpdator(conn, map, 1);  // reject
                    } else {
                        logger.info("start..stolenFlowStartSingleExtended...." + i);
                        stolenFlowStartSingleExtended(conn, map, i); //
                    }
                }

                if (i != 1 && !(map.get("imei_esn_meid") == null || map.get("imei_esn_meid").trim() == "" || map.get("imei_esn_meid").trim().equals("") || map.get("imei_esn_meid").equals("0"))) {
                    logger.info("start..stolenFlowStartSingleExtended.  having  imei..i.e.." + map.get("imei_esn_meid"));
                    stolenFlowStartSingleExtended(conn, map, i);
                }
            }  // End For

            if (map.get("failPassUpdator").equalsIgnoreCase("PASS")) {
                failPasstatusUpdator(conn, map, 0);
            } else {
                failPasstatusUpdator(conn, map, 1);
            }

        } catch (Exception e) {
            logger.info("Excep: +" + e);
        }

    }

    void stolenFlowStartSingleExtended(Connection conn, Map<String, String> map, int ii) {
        try {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue();
                logger.info(" KKKKEEYYYY..." + key + " --->   " + val);
            }
            if (map.get("imei_esn_meid") == null) {
                logger.info(" Action for   IMEI Started.......");
                String imei = getImeiWithMsisdn(conn, map);
                logger.info("GETTED IMEI is " + imei);
                map.put("imei_esn_meid", imei);
                logger.info("Going to  insert into Raw  after getting imei...... ");
                if (imei != null) {
                    insertinRawtable(conn, map);
                    map.put("failPassUpdator", "PASS");
//                    f ailPasstatusUpdator(conn, map, 0);
                }
//                else {
//                    return;  //  
//                }

                for (int i = 2; i <= 4; i++) {
                    String msisdnothr = getOtherContactsImei(conn, i, map);

                    if (msisdnothr == null || msisdnothr.trim() == "" || msisdnothr.equals("") || msisdnothr.equals("0")) {
                        logger.info(" new msisdnothr  id NULL...." + msisdnothr);  //optmse
                    } else {
                        logger.info(" new msisdnothr ...." + msisdnothr);
                        map.put("contact_number", msisdnothr);
                        imei = getImeiWithMsisdn(conn, map);
                        map.put("imei_esn_meid", imei);
                        logger.info("Going to  insert into Raw for I " + i + " after getting imei...... ");
                        if (imei != null) {
                            insertinRawtable(conn, map);
                            map.put("failPassUpdator", "PASS");
//                            f ailPasstatusUpdator(conn, map, 0); 
                        }
                    }
                }
            } else {
                logger.info("Going to insert in RAW with imei..... " + map.get("imei_esn_meid"));
                insertinRawtable(conn, map);
                map.put("failPassUpdator", "PASS");
//                f ailPasstatusUpdator(conn, map, 0);
            }

        } catch (Exception e) {
            logger.info("Excep  at  stolenFlowStartSingleExtended: +" + e);

        }

    }

    private String getImeiWithMsisdn(Connection conn, Map<String, String> map) throws ClassNotFoundException, SQLException {
        ErrorFileGenrator errFile = new ErrorFileGenrator();
        String imei = null;
        String txn_id = map.get("txn_id");
        String lawful_stolen_usage_db_num_days_qury = " select value from  system_configuration_db  where tag  = 'lawful_stolen_usage_db_num_days'";
        logger.info(" getImeiMsisdn ,,,lawful_stolen_usage_db_num_days_qury,,, " + lawful_stolen_usage_db_num_days_qury);
        Statement stmt8 = conn.createStatement();
        ResultSet resultDay = stmt8.executeQuery(lawful_stolen_usage_db_num_days_qury);
        int days = 0;
        try {
            while (resultDay.next()) {
                days = resultDay.getInt(1);
            }
        } catch (Exception e) {
            logger.info("Error..lawful_stolen_usage_db_num_days_qury.." + e);
        }

        stmt8.close();
        DateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yy"); //
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -days);
        String date = dateFormat1.format(cal.getTime());
        logger.info("Date from which Data is calculated" + date);

        List lst = new ArrayList();
        List lstGsma = new ArrayList();
        String msisdn = map.get("contact_number");
        String strTacs = "Result......";
        String device_usage_db_qury = " select imei from  device_usage_db  where msisdn = '" + msisdn
                + "'  and modified_on > '" + date + "' ";
        logger.info(" ImeiWithMsisdn ,,,device_usage_db,,, " + device_usage_db_qury);
        Statement stmt = conn.createStatement();
        ResultSet resultmsdn = stmt.executeQuery(device_usage_db_qury);
        try {
            while (resultmsdn.next()) {
                lst.add(resultmsdn.getString(1));
            }
        } catch (Exception e) {
            logger.info("Error..getErrorMsisdn.." + e);
        }
        logger.info("List size ..." + lst.size());
        String device_duplicate_db_qury = " select imei from  device_duplicate_db  where msisdn = '" + msisdn
                + "'  and modified_on > '" + date + "' ";
        logger.info(" getImeiSMsisdn ,,,device_duplicate_db,,, " + device_duplicate_db_qury);
        ResultSet result2 = stmt.executeQuery(device_duplicate_db_qury);
        try {
            while (result2.next()) {
                lst.add(result2.getString(1));
            }
        } catch (Exception e) {
            logger.info("Error..getImeithMsisdn   ,,,,  device_duplicate_db.." + e);
        }
        logger.info("list SIZE" + lst.size());
        if (lst.isEmpty()) {
            logger.info("Empty List");
//            String fileString = "No device Found in device_duplicate_db and device_usage_db  with  msisdn = '" + msisdn
//                    + "' and  Use date after " + date + "";

            String fileString = "Error Code :    , Error Description :  No Device Found associated with  msisdn " + msisdn;

            errFile.gotoErrorFile(conn, txn_id, fileString);
            map.put("failPassUpdator", "FAIL");
//            f ailPasstatusUpdator(conn, map, 1);
        } else {
            logger.info("NOT Empty List");
            if (lst.size() == 1) {
                imei = (String) lst.get(0);
            } else {
                int column = 1;
                String device_brand_name = map.get("device_brand_name");
                String model_number = map.get("model_number");
                logger.info("device_brand_name*************" + device_brand_name);
                logger.info("model_number*************" + model_number);
                int coustion = 0;
                int resultsiz = 0;
                for (int ind = 0; ind < lst.size(); ind++) {
                    String gsma_tac_qury = " select    band_name , model_name  from  gsma_tac_db  where device_id = "
                            + lst.get(ind).toString().substring(0, 8) + "  ";
                    logger.info(" Query ,,,,,, " + gsma_tac_qury);
                    ResultSet result3 = stmt.executeQuery(gsma_tac_qury);
                    try {
                        while (result3.next()) {
                            resultsiz++;
                            logger.info("Result Startes");
                            {
                                strTacs += column + "> Brand Name :" + result3.getString("band_name") + ",Model Name : "
                                        + result3.getString("model_name") + ",";
                                if (model_number.equalsIgnoreCase(result3.getString("model_name"))
                                        && device_brand_name.equalsIgnoreCase(result3.getString("band_name"))) {
                                    lstGsma.add(lst.get(ind).toString());
                                    coustion++;
                                }
                            }
                        }
                        column++;
                    } catch (Exception e) {
                        logger.info("Error..gmeiWithMsisdn.." + e);
                    }
                }
                stmt.close();
                logger.info(" resultsiz listZise" + resultsiz);
                logger.info(" column of  listZise" + column);

                if (coustion > 1) {
                    logger.info("  in gsma_tac_db ,,  2 or more values  exists  ");
                }
                if (lstGsma.size() == 1) {
                    imei = (String) lstGsma.get(0);
                } else {
                    logger.info(" List Size  in Gsma_tac_db is not valid");
//                         String fileString = strTacs + "...... NO SIMILAR  Model And Brand Name  FOUND IN Gsma_tac_Db SCHEMA ";
                    String fileString = " No Model / Brand Name Match with the Imei associated with Msisdn ";
                    errFile.gotoErrorFile(conn, txn_id, fileString);
                    map.put("failPassUpdator", "FAIL");
//                    f ailPasstatusUpdator(conn, map, 1);
                }
            }
        }
        return imei;
    }

    public void insertinRawtable(Connection conn, Map<String, String> map) {
        String feature = map.get("feature");
        String mgnt_table_db = map.get("mgnt_table_db");
        String imei_esn_meid = map.get("imei_esn_meid");
        String user_type = map.get("user_type");
        String txn_id = map.get("txn_id");
        String id = map.get("id");
        String dateNow = "";
        String DeviceType = "";
        String DeviceIdType = "";
        String MultipleSIMStatus = "";
        String DeviceSerial = "";

        Statement stmt = null;
        if (conn.toString().contains("oracle")) {
            dateNow = new SimpleDateFormat("dd-MMM-YY").format(new Date());
        } else {
            dateNow = new SimpleDateFormat("YYYY-MM-dd").format(new Date());
        }

        String dateFunction = Util.defaultNowDate();
        if (feature.equalsIgnoreCase("Stolen") || feature.equalsIgnoreCase("Recovery")) {
            DeviceType = "( select interp from system_config_list_db where tag = 'DEVICE_TYPE' and value  =(select device_type from stolen_individual_userdb where stolen_id  =" + id + " ) )";
            DeviceIdType = "( select interp from system_config_list_db where tag = 'DEVICE_ID_TYPE' and value  =( select device_id_type from stolen_individual_userdb where stolen_id  =" + id + " ))";
            MultipleSIMStatus = " (select interp from system_config_list_db where tag = 'MULTI_SIM_STATUS' and value  = (select multi_sim_status  from stolen_individual_userdb where stolen_id  =" + id + " ) )";
            DeviceSerial = " ( Select  device_serial_number from stolen_individual_userdb where stolen_id  =" + id + " ) ";
        } else {
            DeviceType = "( select interp from system_config_list_db where tag = 'DEVICE_TYPE'  and value  =(select device_type  from single_imei_details where txn_id ='" + txn_id + "' )) ";
            DeviceIdType = "( select interp from system_config_list_db where tag = 'DEVICE_ID_TYPE' and value  =(select device_id_type   from single_imei_details where txn_id ='" + txn_id + "') )";
            MultipleSIMStatus = "  (select interp from system_config_list_db where tag = 'MULTI_SIM_STATUS' and value  =(select multiple_sim_status   from single_imei_details where txn_id ='" + txn_id + "') )";
            DeviceSerial = " ( select  device_serial_number  from single_imei_details where txn_id ='" + txn_id + "' )";
        }

        String query = "insert into  " + feature + "_raw"
                + "   (   DeviceType,DeviceIdType ,MultipleSIMStatus,SNofDevice,IMEIESNMEID,Devicelaunchdate,DeviceStatus,status,file_name , txn_id , time ,feature_type ,CREATED_ON , modified_on    )   values  "
                + " (  " + DeviceType + "  , " + DeviceIdType + " ,  " + MultipleSIMStatus + " ,  " + DeviceSerial
                + " ,  '" + imei_esn_meid + "' , '', '' , 'Init' ,  'NA' , '" + txn_id + "' ,  '" + dateNow + "'  , '"
                + feature + "'  ,  " + dateFunction + ", " + dateFunction + "      )   ";

        logger.info(" Query  for insertion... " + query);

        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
//            conn.commit();

        } catch (Exception e) {
            logger.info(" Error  " + e);
        }
    }

    void failPasstatusUpdator(Connection conn, Map<String, String> map, int stats) {   // 1 reject
        String statusName = (map.get("feature").equalsIgnoreCase("Stolen") || map.get("feature").equalsIgnoreCase("Recovery") || map.get("feature").equalsIgnoreCase("Block") || map.get("feature").equalsIgnoreCase("Unblock")) ? "file" : map.get("feature");
        String subfeature = map.get("sub_feature");
        String txn_id = map.get("txn_id");
        String feature = map.get("feature");
        String management_table = map.get("mgnt_table_db");
        CEIRFeatureFileFunctions ceirfunction = new CEIRFeatureFileFunctions();
        logger.info("main_type Is:" + feature + ",  management_table:" + management_table);

        ceirfunction.updateFeatureFileStatus(conn, txn_id, (stats == 1 ? 4 : 2), feature, subfeature); // update web_action_db 
        if (stats == 1) {
            ceirfunction.UpdateStatusViaApi(conn, txn_id, stats, feature);
        }

//        ceirfunction.updateFeatureManagementStatus(conn, txn_id, (stats == 1 ? 2 : 1), management_table, statusName);
    }

    public String getOtherContactsImei(Connection conn, int i, Map<String, String> map) {
        String cntctNo = null;

        ResultSet resultmsdn = null;
        String qury = " select contact_number" + i + "  as  cantctNo from stolen_individual_userdb where stolen_id  = '" + map.get("id") + "'    ";
        logger.info("getOtherContactsImei qury :" + qury);
        try {
            Statement stmt = conn.createStatement();
            resultmsdn = stmt.executeQuery(qury);
            try {
                while (resultmsdn.next()) {
                    cntctNo = resultmsdn.getString("cantctNo");
                }
                logger.info("Result at getOtherContactsImei  " + cntctNo);
            } catch (Exception e) {
                logger.info("Error..getOtherContactsImei.." + e);
            }
            stmt.close();

        } catch (Exception e) {
            logger.info("Error..getOtherContactsImei ..2.." + e);
        }
        return cntctNo;
    }

    public void deleteFromRawTable(Connection conn, String txn_id, String feature) {
        String query = "delete from " + feature + "_raw where txn_id ='" + txn_id + "'";
        logger.info(query);
        Statement st5 = null;
        try {
            st5 = conn.createStatement();
            st5.executeUpdate(query);
            logger.info("delete from Raw table");
        } catch (Exception e) {
            logger.error("Error at  " + e);
        }
    }

}
