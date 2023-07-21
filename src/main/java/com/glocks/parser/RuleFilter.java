package com.glocks.parser;

import java.io.FileWriter;
import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gl.Rule_engine_Old.RuleEngineApplication;
import com.glocks.log.LogWriter;
import static com.glocks.parser.CdrParserProcess.appdbName;
import com.glocks.util.Util;

import java.io.BufferedWriter;

public class RuleFilter {

    private static Logger logger = LogManager.getLogger(RuleFilter.class);

    public HashMap getMyRule(Connection conn, HashMap<String, String> device_info, ArrayList<Rule> rulelist) {
        logger.debug("getMyRule started ");
        BufferedWriter bw = null;
        HashMap<String, String> rule_detail = new HashMap<String, String>();    // CDR
        String output = "Yes";
        String action_output = "";
        for (Rule rule : rulelist) {
            logger.debug("  MyRule  " + rule.rule_name);
            device_info.put("rule_name", rule.rule_name);
            device_info.put("output", rule.output);
            device_info.put("ruleid", rule.ruleid);
            device_info.put("period", rule.period);
            device_info.put("action", rule.action);
            device_info.put("failed_rule_aciton", rule.failed_rule_aciton);
            String[] my_arr = {device_info.get("rule_name"), //0
                "1", //1
                "CDR", //  2
                // (device_info.get("rule_name").equals("IMEI_LUHN_CHECK") || device_info.get("rule_name").equals("IMEI_LENGTH")) ? device_info.get("IMEI") : device_info.get("IMEI").substring(0, 1 4), //3
                device_info.get("IMEI").length() > 14 ? device_info.get("IMEI").substring(0, 14) : device_info.get("IMEI"),
                "0", //4
                device_info.get("file_name"), //5
                "0", //6
                device_info.get("record_time"), //7
                device_info.get("operator"), //8
                "", //9
                device_info.get("operator_tag"), // 10     GSM/CDMA
                device_info.get("period"), //11
                device_info.get("MSISDN"), //12
                device_info.get("action") //13
        };
            try {
                logger.debug("Rule Started .." + device_info.get("rule_name"));
                output = RuleEngineApplication.startRuleEngine(my_arr, conn, bw);
                logger.debug("Rule End .." + device_info.get("rule_name") + "; Rule Output:" + output + " ;  Expected O/T : " + device_info.get("output"));
            } catch (Exception e) {
                logger.error("Error1 " + e);
            }
            if (device_info.get("output").equalsIgnoreCase(output)) {
                rule_detail.put("rule_name", null);
            } else {
                String[] my_action_arr = {
                    device_info.get("rule_name"), //0  ruleName
                    "2", //1   executeRuleExecuteAction
                    "CDR", //2   FeatureName
                    device_info.get("IMEI"), //3   imei
                    "0", //4
                    device_info.get("file_name"), //5        fileName
                    "0", //6
                    device_info.get("record_time"), //7      recordTime
                    device_info.get("operator"), //8       operator
                    "error", //9
                    device_info.get("operator_tag"), //10   operatorTag GSMA/CDMA
                    device_info.get("period"), //11        peroid
                    device_info.get("MSISDN"), //12         msisdn
                    device_info.get("action"), //13       action
                    device_info.get("IMSI"), //1 4         imsi
                    device_info.get("record_type"), //15  recordType
                    device_info.get("system_type"), //16  systemType
                    device_info.get("source"), //17       source
                    device_info.get("raw_cdr_file_name"), //18 rawCdrFileName
                    device_info.get("imei_arrival_time"), //19 imeiArrivalTime
                    device_info.get("operator"), //20           operator
                    device_info.get("file_name") //21         fileName
                };
                try {
                    action_output = RuleEngineApplication.startRuleEngine(my_action_arr, conn, bw);
                } catch (Exception e) {
                    logger.error("Error2 " + e);
                }

                if (device_info.get("rule_name").equalsIgnoreCase("TEST_IMEI")) {
                    rule_detail.put("test_imei", "true");
                }

                if (device_info.get("failed_rule_aciton").equalsIgnoreCase("rule")) {
                    if (!device_info.get("rule_name").equalsIgnoreCase("EXISTS_IN_ALL_ACTIVE_DB")) {
                        insertInDeviceInvalidDb(conn, device_info);
                    }

                    rule_detail.put("rule_name", null);
                } else {
                    rule_detail.put("period", device_info.get("period"));
                    rule_detail.put("action", device_info.get("action"));
                    rule_detail.put("output", "Yes");
                    rule_detail.put("rule_name", device_info.get("rule_name"));
                    rule_detail.put("rule_id", device_info.get("ruleid"));
                    if (!device_info.get("rule_name").equalsIgnoreCase("EXISTS_IN_ALL_ACTIVE_DB")) {
                        insertInDeviceInvalidDb(conn, device_info);
                    }
                    break;
                }
            }
        }
        return rule_detail;
    }

    private void insertInDeviceInvalidDb(Connection conn, HashMap<String, String> device_info) {
        boolean isOracle = conn.toString().contains("oracle");
        String dateFunction = Util.defaultDate(isOracle);
        Statement stmt = null;
        try {
            String query = "insert into " + appdbName + ".invalid_imei (CREATED_ON,MODIFIED_ON ,IMEI_ESN_MEID ,RULE_NAME ,OPERATOR_NAME, SN_OF_DEVICE ,OPERATOR_TYPE ,  FILE_NAME ,RECORD_DATE) "
                    + " values ( now() , now() ,'" + device_info.get("IMEI") + "','" + device_info.get("rule_name") + "','" + device_info.get("operator") + "', ' ' ,'" + device_info.get("operator_tag") + "' ,'" + device_info.get("file_name") + "','" + device_info.get("record_time") + "'  ) ";
            logger.debug("Qury " + query);
            stmt = conn.createStatement();
            stmt.executeUpdate(query);
        } catch (Exception e) {
            logger.error(this.getClass().getName() + e);
        } finally {
            try {
                stmt.close();
            } catch (Exception ex) {
                logger.error(this.getClass().getName() + ex);
            }
        }

    }

    public HashMap getMyFeatureRule(Connection conn, HashMap<String, String> device_info, ArrayList<Rule> rulelist, FileWriter myWriter, BufferedWriter bw) {
        int errorFlag = 0;                  // NONCDR

        HashMap<String, String> rule_detail = new HashMap<String, String>();
        String output = "Yes";
        String action_output = "";
        for (Rule rule : rulelist) {
            device_info.put("rule_name", rule.rule_name);
            device_info.put("output", rule.output);
            device_info.put("ruleid", rule.ruleid);
            device_info.put("period", rule.period);
            device_info.put("action", rule.action);
            device_info.put("failed_rule_aciton", rule.failed_rule_aciton);
            String fileArray = device_info.get("DeviceType") + "," + device_info.get("DeviceIdType") + "," + device_info.get("MultipleSIMStatus") + "," + device_info.get("SNofDevice") + "," + device_info.get("IMEIESNMEID") + "," + device_info.get("Devicelaunchdate") + "," + device_info.get("DeviceStatus") + "";
            String[] my_arr = {
                device_info.get("rule_name"), //0
                "1", //1
                device_info.get("feature"), //2   (consign,stock,Non CDr etc
                (((device_info.get("rule_name").equals("IMEI_LUHN_CHECK") || device_info.get("rule_name").equals("IMEI_LENGTH"))) ? device_info.get("IMEIESNMEID") : device_info.get("IMEIESNMEID").substring(0, 14)), //3
                device_info.get("SNofDevice"), //4//
                device_info.get("file_name"), //5     foreignSim Only
                device_info.get("DeviceType"), //6//
                device_info.get("record_time"), //7//
                device_info.get("operator"), //8    foreignSim Only
                device_info.get("DeviceIdType"), //9     imei/esn/meid imeiLength  / luhn
                device_info.get("operator_tag"),//10    ""
                device_info.get("period"), //11
                "", //12
                device_info.get("action"), //13
                device_info.get("txn_id"), //1 4
                fileArray //15
            };
            logger.debug(" Rule Execution Start" + Arrays.toString(my_arr));
            output = RuleEngineApplication.startRuleEngine(my_arr, conn, bw);
            logger.info("Rule End .." + device_info.get("rule_name") + "; Rule Output:" + output + " ;  Expected O/T : " + device_info.get("output"));

            new LogWriter().writeFeatureEvents(myWriter, device_info.get("IMEIESNMEID"),
                    device_info.get("DeviceType"), device_info.get("MultipleSIMStatus"), device_info.get("SNofDevice"),
                    device_info.get("Devicelaunchdate"), device_info.get("DeviceStatus"),
                    device_info.get("txn_id"), device_info.get("operator"), device_info.get("file_name"), "RuleCheckEnd",
                    device_info.get("ruleid"), device_info.get("rule_name"), output, new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));

            if (device_info.get("output").equalsIgnoreCase(output)) {    // go to next rule(  rule_engine _mapping )
                rule_detail.put("rule_name", null);
            } else {																	// kuch to gdbad h.
                new LogWriter().writeFeatureEvents(myWriter, device_info.get("IMEIESNMEID"),
                        device_info.get("DeviceType"), device_info.get("MultipleSIMStatus"), device_info.get("SNofDevice"),
                        device_info.get("Devicelaunchdate"), device_info.get("DeviceStatus"),
                        device_info.get("txn_id"), device_info.get("operator"), device_info.get("file_name"), "RuleActionStart",
                        device_info.get("ruleid"), device_info.get("rule_name"), "", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));

                // Perform Action 
                String[] my_action_arr = {
                    device_info.get("rule_name"),
                    "2",
                    device_info.get("feature"),
                    device_info.get("IMEIESNMEID"),
                    device_info.get("SNofDevice"),
                    device_info.get("file_name"),
                    "0",
                    "",
                    device_info.get("operator"),
                    "error",
                    device_info.get("operator_tag"),
                    device_info.get("period"),
                    "",
                    output.equalsIgnoreCase("NAN") ? "NAN" : device_info.get("action"),
                    device_info.get("txn_id"),
                    fileArray
                };

                action_output = RuleEngineApplication.startRuleEngine(my_action_arr, conn, bw);
//                errorFlag = 1;
                try {
                    bw.flush();
                } catch (Exception e) {
                    logger.error("e " + e);
                }
                logger.info("Rule Filter Action Output is [" + action_output + "]");

                new LogWriter().writeFeatureEvents(myWriter, device_info.get("IMEIESNMEID"),
                        device_info.get("DeviceType"), device_info.get("MultipleSIMStatus"), device_info.get("SNofDevice"),
                        device_info.get("Devicelaunchdate"), device_info.get("DeviceStatus"),
                        device_info.get("txn_id"), device_info.get("operator"), device_info.get("file_name"), "failed_rule_action :" + device_info.get("failed_rule_aciton"),
                        device_info.get("ruleid"), device_info.get("rule_name"), action_output, new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));

                if (device_info.get("failed_rule_aciton").equalsIgnoreCase("rule")) {
                    rule_detail.put("rule_name", null);
                    logger.info("failed_rule_aciton is Rule ..   ");  // if action FAils But next failed_rule_aciton is Rule , ..action _output  will not work ....         
                } else {
                    errorFlag = 1;
                    rule_detail.put("period", device_info.get("period"));
                    rule_detail.put("action", device_info.get("action"));
                    rule_detail.put("output", "Yes");
                    rule_detail.put("rule_name", device_info.get("rule_name"));
                    rule_detail.put("rule_id", device_info.get("ruleid"));
                    rule_detail.put("action_output", action_output);
                    break;
                }
            }
        }
        rule_detail.put("errorFlag", String.valueOf(errorFlag));
        return rule_detail;
    }

//     private HashMap<String, String> checkMyRule(Connection conn, HashMap<String, String> device_info) {
//          ResultSet rs_set = null;
//          Statement stmt_set = null;
//
//          HashMap<String, String> rule_details = new HashMap<String, String>();
//
//          String filter_set_query = "select * from rule_filter_set where rule_id='" + device_info.get("ruleid") + "'";
//          try {
//               logger.info("Check my rule set query [" + filter_set_query + "]");
//               // System.out.println("Rule filter Set Query" + filter_set_query + "");
//               stmt_set = conn.createStatement();
//               rs_set = stmt_set.executeQuery(filter_set_query);
//               while (rs_set.next()) {
//                    if (rs_set.getString("rule_set_type").equalsIgnoreCase("from_db")) {
//                         // System.out.println("checing in DB");
//                         logger.info("Checking in DB Rules [" + filter_set_query + "]");
//                         rule_details = fromDBRule(conn, device_info);
//
//                    } else if (rs_set.getString("rule_set_type").equalsIgnoreCase("norm_check")) {
//                         // System.out.println("checking in Norm Check");
//                         logger.info("Checking in Normal check rule [" + filter_set_query + "]");
//                         rule_details = fromNormCheckRule(conn, device_info);
//
//                    } else if (rs_set.getString("rule_set_type").equalsIgnoreCase("java_api")) {
//                         // System.out.println("checkingin javaapi");
//                         logger.info("Checking in Java API Rules [" + filter_set_query + "]");
//                         rule_details = fromJavaAPI(conn, device_info);
//                    }
//                    if (rule_details.get("output") != null) {
//                         logger.info("Rule Details find after rule filter [" + rule_details + "]");
//                         if (device_info.get("output").equalsIgnoreCase(rule_details.get("output"))) {
//                              rule_details = null;
//                         } else {
//                              if (device_info.get("failed_rule_aciton").equalsIgnoreCase("rule")) {
//                                   rule_details.put("rule_name", null);
//                              } else {
//                                   rule_details.put("period", device_info.get("period"));
//                                   rule_details.put("action", device_info.get("action"));
//                                   ruleFilterAction(conn, rule_details.get("action_type"), device_info);
//                              }
//                         }
//                    }
//               }
//          } catch (Exception e) {
//               e.printStackTrace();
//               logger.info("Exception [" + e + "]");
//
//          } finally {
//               try {
//                    stmt_set.close();
//                    rs_set.close();
//               } catch (SQLException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//               }
//          }
//
//          return rule_details;
//
//     }
    private HashMap<String, String> fromJavaAPI(Connection conn, HashMap<String, String> device_info) {
        HashMap<String, String> rule_details = new HashMap<String, String>();
        ResultSet rs = null;
        Statement stmt = null;
        String query = "select * from rule_filter_java_api_db where rule_id='" + device_info.get("ruleid") + "'";
        // System.out.println("Java api Rule Query" + query);
        logger.info("Query for Java API Rule filter [" + query + "]");
        String output = "";

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                String complete_api = rs.getString("context_path");
                String[] values = rs.getString("param_value").split(",");
                for (int i = 0; i < values.length; i++) {
                    complete_api += " " + device_info.get(values[i].substring(3));
                }
                // System.out.println("Complete command to call the java api Rule" + complete_api);
                logger.info("API to call Java Rule Filter [" + complete_api + "]");
                Process process = Runtime.getRuntime().exec(complete_api);
                InputStream is = process.getInputStream();
                Scanner sc = new Scanner(is);
                if (sc.hasNext()) {
                    output = sc.next();
                }
                logger.info("Output from Java API Rule Filter [" + output + "]");

                // System.out.println("Complete command to call the java api output" + complete_api);
                rule_details.put("output", output);
                rule_details.put("action_type", rs.getString("action_type"));
                rule_details.put("rule_name", device_info.get("rule_name"));
                rule_details.put("rule_id", device_info.get("ruleid"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Exception [" + e + "]");

        } finally {
            try {
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return rule_details;
    }

    private HashMap<String, String> fromDBRule(Connection conn, HashMap<String, String> device_info) {
        ResultSet rs = null;
        Statement stmt = null;
        ResultSet rs1 = null;
        Statement stmt1 = null;
        String my_rule_query = "";
        HashMap<String, String> rule_details = new HashMap<String, String>();

        String query = "select * from rule_filter_condition_db where rule_id='" + device_info.get("ruleid") + "'";
        // System.out.println("Normal Check query" + query);
        logger.info("Query for DB Check Rule filter [" + query + "]");

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {

                my_rule_query = "select * from " + rs.getString("table_name") + " where ";
                String[] keys = rs.getString("param_key").split(",");
                String[] condition = rs.getString("param_condition").split(",");
                String[] values = rs.getString("param_value").split(",");
                String[] operators = rs.getString("param_operator").split(",");
                String operator = "";

                for (int i = 0; i < keys.length; i++) {
                    if (i > 1) {
                        operator = operators[i - 1];
                    }
                    if (values[i].startsWith("hm.")) {
                        my_rule_query += " " + keys[i] + condition[i] + "'" + device_info.get(values[i].substring(3)) + "' " + operator;
                    } else {
                        my_rule_query += " " + keys[i] + condition[i] + "'" + values[i] + "' " + operator;
                    }
                }
                logger.info("Query for DB check query [" + my_rule_query + "]");
                stmt1 = conn.createStatement();
                rs1 = stmt1.executeQuery(my_rule_query);
                if (rs1.next()) {
                    if (parseExpression(Integer.parseInt(rs1.getString(rs.getString("result_key"))), rs.getString("result_condition"), Integer.parseInt(rs.getString("result_value")))) {
                        //						ruleFilterAction(conn,rs.getString("action_type"),device_info);
                        rule_details.put("output", "Yes");
                        rule_details.put("action", rs.getString("action_type"));
                        rule_details.put("rule_name", device_info.get("rule_name"));
                        rule_details.put("rule_id", device_info.get("ruleid"));
                    }
                }
            }
            logger.info("Final Rule Details for DB check Rule filter [" + query + "]");

        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Exception [" + e + "]");

        } finally {
            try {
                rs.close();
                stmt.close();
                rs1.close();
                stmt1.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        return rule_details;
    }

    private HashMap<String, String> fromNormCheckRule(Connection conn, HashMap<String, String> device_info) {
        ResultSet rsnorm = null;
        Statement stmtnorm = null;
        //		ResultSet rs2 = null;
        //		Statement stmt2 = null;
        //		ResultSet rs3=null;
        //		Statement stmt3=null;
        HashMap<String, String> rule_details = new HashMap<String, String>();

        String query = "select * from rule_filter_norm_check_db where rule_id='" + device_info.get("ruleid") + "'";
        // System.out.println("In Norm Check Query" + query);
        logger.info("Query for Normal check query [" + query + "]");
        String my_rule_query = "";
        try {
            stmtnorm = conn.createStatement();
            rsnorm = stmtnorm.executeQuery(query);
            while (rsnorm.next()) {
                String[] keys = rsnorm.getString("param_key").split(",");
                String[] condition = rsnorm.getString("param_condition").split(",");
                String[] values = rsnorm.getString("param_value").split(",");
                String[] operators = rsnorm.getString("param_operator").split(",");
                String operator = "";
                // System.out.println("norm check 1");
                boolean my_flag = false;
                for (int i = 0; i < keys.length; i++) {
                    if (keys[i].startsWith("hm.")) {
                        if (keys[i].endsWith(".len")) {
                            if (i == 0) {
                                my_flag = parseExpression(
                                        parseMethod(
                                                device_info.get(keys[i].substring(3).substring(0, keys[i].substring(3).length() - 4)),
                                                keys[i].substring(keys[i].length() - 3, keys[i].length())),
                                        condition[i], Integer.parseInt(values[i]));
                                // System.out.println("my_flag 1 =" + my_flag);

                            } else {
                                my_flag = parseOperator(my_flag,
                                        operators[i - 1],
                                        parseExpression(
                                                parseMethod(
                                                        device_info.get(keys[i].substring(3).substring(0, keys[i].substring(3).length() - 4)),
                                                        keys[i].substring(keys[i].length() - 3, keys[i].length())),
                                                condition[i], Integer.parseInt(values[i]))
                                );
                            }
                        } else {
                        }
                    }
                }
                // System.out.println("my_flag =" + my_flag);
                if (my_flag) {

                    //					ruleFilterAction(conn,rsnorm.getString("action_type"),device_info);
                    rule_details.put("output", "Yes");
                    rule_details.put("action", rsnorm.getString("action_type"));
                    rule_details.put("rule_name", device_info.get("rule_name"));
                    rule_details.put("rule_id", device_info.get("ruleid"));

                }
            }
            logger.info("Query for Normal check final rule details [" + rule_details + "]");

        } catch (Exception e) {
            logger.info("Exception [" + e + "]");
            e.printStackTrace();
        } finally {
            try {
                //				rs2.close();
                //				stmt2.close();
                //				rs3.close();
                //				stmt3.close();
                rsnorm.close();
                stmtnorm.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return rule_details;

    }

    private void ruleFilterAction(Connection conn, String action_type, HashMap<String, String> device_info) {
        Statement stmt2 = null;
        ResultSet rs2 = null;
        Statement stmt3 = null;
        try {
            if (action_type.equalsIgnoreCase("Insert")) {
                String action_query = "select * from rule_filter_action_db where rule_id='" + device_info.get("ruleid") + "'";
                stmt2 = conn.createStatement();
                rs2 = stmt2.executeQuery(action_query);
                logger.info("Rule filter action query [" + action_query + "]");
                // System.out.println("NOrm check Action DB Query" + action_query);
                while (rs2.next()) {
                    String[] insert_keys = rs2.getString("param_key").split(",");
                    String[] insert_values = rs2.getString("param_value").split(",");
                    String action_insert_query = "insert into " + rs2.getString("context_path") + " (";
                    String values1 = " values(";
                    for (int j = 0; j < insert_keys.length; j++) {
                        // System.out.println(insert_keys[j] + " value " + insert_values[j]);
                        action_insert_query = action_insert_query + insert_keys[j] + ",";
                        if (insert_values[j].startsWith("hm.")) {
                            values1 = values1 + "'" + device_info.get(insert_values[j].substring(3)) + "',";
                        } else {
                            values1 = values1 + "'" + insert_values[j] + "',";
                        }
                    }
                    action_insert_query = action_insert_query.substring(0, action_insert_query.length() - 1) + ") " + values1.substring(0, values1.length() - 1) + ")";
                    logger.info("Query to insert in rule filter action [" + action_insert_query + "]");
                    stmt3 = conn.createStatement();
                    stmt3.executeUpdate(action_insert_query);
                    //  // conn.commit();

                }
            } else if (action_type.equals("java_api")) {
                String action_query = "select * from rule_filter_action_db where rule_id='" + device_info.get("ruleid") + "'";
                // System.out.println("java api calling action " + action_query);
                logger.info("Query to java_api in rule filter action [" + action_query + "]");
                stmt2 = conn.createStatement();
                rs2 = stmt2.executeQuery(action_query);
                String output = "";
                while (rs2.next()) {
                    String complete_api = rs2.getString("context_path");
                    String[] values = rs2.getString("param_value").split(",");
                    for (int i = 0; i < values.length; i++) {
                        complete_api += " " + device_info.get(values[i].substring(3));
                    }
                    // System.out.println("Complete command to call the java api Action" + complete_api);
                    logger.info("Complete command to call the java api Action[" + complete_api + "]");
                    Process process = Runtime.getRuntime().exec(complete_api);
                    InputStream is = process.getInputStream();
                    Scanner sc = new Scanner(is);
                    if (sc.hasNext()) {
                        output = sc.next();
                    }
                    // System.out.println("Java API Output action" + output);
                    logger.info("Java API Rule filter action[" + output + "]");

                }
            }
        } catch (Exception e) {
            logger.info("Exception [" + e + "]");
            e.printStackTrace();
        } finally {
            try {
                stmt2.close();
                rs2.close();
                if (stmt3 != null) {
                    stmt3.close();
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    private static boolean parseExpression(int number1, String operator, int number2) {
        if ("<".equals(operator)) {
            return number1 < number2;
        } else if (">".equals(operator)) {
            return number1 > number2;
        } else if ("<=".equals(operator)) {
            return number1 <= number2;
        } else if (">=".equals(operator)) {
            return number1 >= number2;
        } else if ("==".equals(operator)) {
            // System.out.println(number1 + "checking both number " + number2);
            return number1 == number2;
        } else if ("!=".equals(operator)) {
            // System.out.println(number1 + "checking both number " + number2);
            return number1 != number2;
        } else {
            throw new IllegalArgumentException("Invalid operator");
        }
    }

    private static int parseMethod(String my_string, String operator) {
        if ("len".equals(operator)) {
            // System.out.println("my imei length" + my_string.length() + my_string);
            return my_string.length();
        } else {
            throw new IllegalArgumentException("Invalid operator");
        }
    }

    private static boolean parseOperator(boolean param1, String operator, boolean param2) {
        if ("||".equals(operator)) {
            return param1 || param2;
        } else if ("&&".equals(operator)) {
            return param1 && param2;
        } else {
            throw new IllegalArgumentException("Invalid operator");
        }
    }

    public HashMap getMyFeatureRuleForTypeApprove(Connection conn, HashMap<String, String> device_info, ArrayList<Rule> rulelist, FileWriter myWriter, BufferedWriter bw) {
        int errorFlag = 0;                  // NONCDR

        HashMap<String, String> rule_detail = new HashMap<String, String>();
        String output = "Yes";
        String action_output = "";
        for (Rule rule : rulelist) {
            device_info.put("rule_name", rule.rule_name);
            device_info.put("output", rule.output);
            device_info.put("ruleid", rule.ruleid);
            device_info.put("period", rule.period);
            device_info.put("action", rule.action);
            device_info.put("failed_rule_aciton", rule.failed_rule_aciton);

            String fileArray = "TAC No.: " + device_info.get("IMEIESNMEID") + " :: ";
            String[] my_arr = {
                device_info.get("rule_name"), //0
                "1", //1
                device_info.get("feature"), //2   (consign,stock,CDr etc
                device_info.get("IMEIESNMEID"), //3
                "0", //4//
                device_info.get("file_name"), //5     foreignSim Only
                "0", //6//
                device_info.get("record_time"), //7//
                device_info.get("operator"), //8    foreignSim Only
                device_info.get("DeviceIdType"), //9     imei/esn/meid imeiLength  / luhn
                device_info.get("operator_tag"),//10    ""
                device_info.get("period"), //11
                "", //12
                device_info.get("action"), //13
                device_info.get("txn_id"), //1 4
                fileArray //15
            };
            logger.info(" Rule Execution Start" + Arrays.toString(my_arr));
            output = RuleEngineApplication.startRuleEngine(my_arr, conn, bw);
            logger.info("Rule End .." + device_info.get("rule_name") + "; Rule Output:" + output + " ;  Expected O/T : " + device_info.get("output"));

            if (device_info.get("output").equalsIgnoreCase(output)) {    // go to next rule(  rule_engine _mapping )
                rule_detail.put("rule_name", null);
            } else {																	// kuch to gdbad h.
                String[] my_action_arr = {
                    device_info.get("rule_name"),
                    "2",
                    device_info.get("feature"),
                    device_info.get("IMEIESNMEID"),
                    "0",
                    device_info.get("file_name"),
                    "0",
                    "",
                    device_info.get("operator"),
                    "error",
                    device_info.get("operator_tag"),
                    device_info.get("period"),
                    "",
                    device_info.get("output").equalsIgnoreCase("NAN") ? "NAN" : device_info.get("action"),
                    device_info.get("txn_id"),
                    fileArray
                };

                action_output = RuleEngineApplication.startRuleEngine(my_action_arr, conn, bw);
//                errorFlag = 1;
                logger.info("Rule Filter Action Output is [" + action_output + "]");

                new LogWriter().writeFeatureEvents(myWriter, device_info.get("IMEIESNMEID"),
                        device_info.get("DeviceType"), device_info.get("MultipleSIMStatus"), device_info.get("SNofDevice"),
                        device_info.get("Devicelaunchdate"), device_info.get("DeviceStatus"),
                        device_info.get("txn_id"), device_info.get("operator"), device_info.get("file_name"), "failed_rule_action :" + device_info.get("failed_rule_aciton"),
                        device_info.get("ruleid"), device_info.get("rule_name"), action_output, new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));

                if (device_info.get("failed_rule_aciton").equalsIgnoreCase("rule")) {
                    rule_detail.put("rule_name", null);
                    logger.info("failed_rule_aciton is Rule ..   ");  // if action FAils But next failed_rule_aciton is Rule , ..action _output  will not work ....         
                } else {
                    errorFlag = 1;
                    rule_detail.put("period", device_info.get("period"));
                    rule_detail.put("action", device_info.get("action"));
                    rule_detail.put("output", "Yes");
                    rule_detail.put("rule_name", device_info.get("rule_name"));
                    rule_detail.put("rule_id", device_info.get("ruleid"));
                    rule_detail.put("action_output", action_output);
                    break;
                }
            }
        }
        rule_detail.put("errorFlag", String.valueOf(errorFlag));
        return rule_detail;
    }

}

//package com.functionapps.parser;
//
//import java.io.FileWriter;
//import java.io.InputStream;
//import java.sql.*;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Scanner;
//import org.apache.logging.log4j.Logger;
//
//import com.functionapps.log.LogWriter;
////import com.gl.Rule_engine.RuleEngineApplication;
//import com.gl.Rule_engine.RuleEngineApplication;
//
//public class RuleFilter {
//	private static Logger	logger = LogManager.getLogger(RuleFilter.class);
//	public HashMap getMyRule(Connection conn,HashMap<String, String> device_info, ArrayList<Rule> rulelist ,FileWriter myWriter){
//		HashMap<String, String> rule_detail = new HashMap<String, String>();
//		String output="Yes";
//		String action_output="";
//		for (Rule rule : rulelist) {
//
//			device_info.put("rule_name", rule.rule_name);
//			device_info.put("output", rule.output);
//			device_info.put("ruleid", rule.ruleid);
//			device_info.put("period", rule.period);
//			device_info.put("action", rule.action);
//			device_info.put("failed_rule_aciton", rule.failed_rule_aciton);
//
//
//			new LogWriter().writeEvents(myWriter, device_info.get("servedIMEI"),
//					device_info.get("recordType"), device_info.get("servedIMSI"),device_info.get("servedMSISDN"),
//					device_info.get("systemType"),
//					device_info.get("operator"), device_info.get("file_name"), device_info.get("record_time"), "RuleCheckStart",
//					device_info.get("ruleid"), device_info.get("rule_name"), "",new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));				
//
//
//
//			RuleEngineApplication rea = new RuleEngineApplication();
//
//
//			String [] my_arr = {device_info.get("rule_name"),
//					"1",
//					"CDR",
//					device_info.get("servedIMEI"),
//					"0",
//					device_info.get("file_name"),
//					"0",
//					device_info.get("record_time"),
//					device_info.get("operator"),
//					"error",
//					device_info.get("operator_tag"),
//					device_info.get("period"),
//					device_info.get("servedMSISDN"),
//					device_info.get("action")
//			};
//
//
//
//			output = rea.startRuleEngine(my_arr);
//			logger.info("Rule Execute output is ["+output+"]");
//			new LogWriter().writeEvents(myWriter, device_info.get("servedIMEI"),
//					device_info.get("recordType"), device_info.get("servedIMSI"),device_info.get("servedMSISDN"),
//					device_info.get("systemType"),
//					device_info.get("operator"), device_info.get("file_name"), device_info.get("record_time"), "RuleCheckEnd",
//					device_info.get("ruleid"), device_info.get("rule_name"), output,new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));				
//
//
//			if(device_info.get("output").equalsIgnoreCase(output)){
//				rule_detail.put("rule_name", null);				
//			}
//			else{
//				if(device_info.get("failed_rule_aciton").equalsIgnoreCase("rule")){
//					rule_detail.put("rule_name", null);
//				}
//				else{
//					rule_detail.put("period" , device_info.get("period"));
//					rule_detail.put("action",device_info.get("action"));
//					rule_detail.put("output", "Yes");
//					rule_detail.put("rule_name", device_info.get("rule_name"));
//					rule_detail.put("rule_id", device_info.get("ruleid"));
//					new LogWriter().writeEvents(myWriter, device_info.get("servedIMEI"),
//							device_info.get("recordType"), device_info.get("servedIMSI"),device_info.get("servedMSISDN"),
//							device_info.get("systemType"),
//							device_info.get("operator"), device_info.get("file_name"), device_info.get("record_time"), "RuleActionStart",
//							device_info.get("ruleid"), device_info.get("rule_name"), output,new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));				
//
//					// Perform Action 
//					String [] my_action_arr = {device_info.get("rule_name"),
//							"2",
//							"CDR",
//							device_info.get("servedIMEI"),
//							"0",
//							device_info.get("file_name"),
//							"0",
//							device_info.get("record_time"),
//							device_info.get("operator"),
//							"error",
//							device_info.get("operator_tag"),
//							device_info.get("period"),
//							device_info.get("servedMSISDN"),
//							device_info.get("action")
//					};
//					action_output = rea.startRuleEngine(my_action_arr);					
//					logger.info("Rule Filter Action Output is ["+action_output+"]");
//
//
//					new LogWriter().writeEvents(myWriter, device_info.get("servedIMEI"),
//							device_info.get("recordType"), device_info.get("servedIMSI"),device_info.get("servedMSISDN"),
//							device_info.get("systemType"),
//							device_info.get("operator"), device_info.get("file_name"), device_info.get("record_time"), "RuleActionEnd",
//							device_info.get("ruleid"), device_info.get("rule_name"), action_output,new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));									
//					break;
//				}
//			}
//
//			//
//			//		
//			//		rule_detail = checkMyRule(conn ,device_info);
//			//		if(rule_detail.get("rule_name") != null ){
//			//			logger.info("Rule Detials"+rule_detail+rule_detail.get("rule_name"));
//			//			logger.info("Breaking the Rules");
//			//			break;
//			//		}
//
//		}
//		return rule_detail;
//	}
//	public HashMap getMyFeatureRule(Connection conn,HashMap<String, String> device_info, ArrayList<Rule> rulelist ,FileWriter myWriter){
//		HashMap<String, String> rule_detail = new HashMap<String, String>();
//		String output="Yes";
//		String action_output="";
//		for (Rule rule : rulelist) {
//			device_info.put("rule_name", rule.rule_name);
//			device_info.put("output", rule.output);
//			device_info.put("ruleid", rule.ruleid);
//			device_info.put("period", rule.period);
//			device_info.put("action", rule.action);
//			device_info.put("failed_rule_aciton", rule.failed_rule_aciton);
//
//			logger.info("faild  RULE action,,,,,"+  rule.failed_rule_aciton);
//			try {
//				new LogWriter().writeFeatureEvents(myWriter, device_info.get("IMEIESNMEID"),
//						device_info.get("DeviceType"), device_info.get("MultipleSIMStatus"),device_info.get("SNofDevice"),
//						device_info.get("Devicelaunchdate"),device_info.get("DeviceStatus"),
//						device_info.get("txn_id"), device_info.get("operator"), device_info.get("file_name"), "RuleCheckStart",
//						device_info.get("ruleid"), device_info.get("rule_name"), "",new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));				
//			}catch(Exception e ) {
//				logger.info(" rule filter exception getMyFeatureRule  +"  + e);
//			}
//
//
//			RuleEngineApplication rea = new RuleEngineApplication();
//			String [] my_arr = {device_info.get("rule_name"),
//					"1",
//					device_info.get("feature"),
//					device_info.get("IMEIESNMEID"),
//					"0",
//					device_info.get("file_name"),
//					"0",
//					device_info.get("record_time"),
//					device_info.get("operator"),
//					"error",
//					device_info.get("operator_tag"),
//					device_info.get("period"),
//					"",
//					device_info.get("action"),
//					device_info.get("txn_id")
//			};
//
//			logger.info(" Rule Execution Start.."+Arrays.toString(my_arr));
//			output = rea.startRuleEngine(my_arr);
//			logger.info("Rule Execute output is ["+output+"]");
//
//
//			try {
//				new LogWriter().writeFeatureEvents(myWriter, device_info.get("IMEIESNMEID"),
//						device_info.get("DeviceType"), device_info.get("MultipleSIMStatus"),device_info.get("SNofDevice"),
//						device_info.get("Devicelaunchdate"),device_info.get("DeviceStatus"),
//						device_info.get("txn_id"), device_info.get("operator"), device_info.get("file_name"), "RuleCheckEnd",
//						device_info.get("ruleid"), device_info.get("rule_name"), output,new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));				
//
//			}catch(Exception e ) {
//				logger.info(" rule filter exception getMyFeatureRule 22  +"  + e);
//			}
//			logger.info( "" + device_info.get("output")    + "...  output "+  output);
//			if(device_info.get("output").equalsIgnoreCase(output)){
//				rule_detail.put("rule_name", null);				
//			}else{
//				if(device_info.get("failed_rule_aciton").equalsIgnoreCase("rule")){
//					rule_detail.put("rule_name", null);
//				}else{
//					rule_detail.put("period" , device_info.get("period"));
//					rule_detail.put("action",device_info.get("action"));
//					rule_detail.put("output", "Yes");
//					rule_detail.put("rule_name", device_info.get("rule_name"));
//					rule_detail.put("rule_id", device_info.get("ruleid"));
//
//					try {
//						new LogWriter().writeFeatureEvents(myWriter, device_info.get("IMEIESNMEID"),
//								device_info.get("DeviceType"), device_info.get("MultipleSIMStatus"),device_info.get("SNofDevice"),
//								device_info.get("Devicelaunchdate"),device_info.get("DeviceStatus"),
//								device_info.get("txn_id"), device_info.get("operator"), device_info.get("file_name"), "RuleActionStart",
//								device_info.get("ruleid"), device_info.get("rule_name"), "",new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));				
//
//					}catch(Exception e ) {
//						logger.info(" rule filter exception getMyFeatureRule  +"  + e);
//					}
//
//
//					// Perform Action 
//					String [] my_action_arr = {
//							device_info.get("rule_name"),
//							"2",                          //  executeaction 
//							device_info.get("feature"),
//							device_info.get("IMEIESNMEID"),
//							"0",
//							device_info.get("file_name"),
//							"0",
//							"",
//							device_info.get("operator"),
//							"error",
//							device_info.get("operator_tag"),
//							device_info.get("period"),
//							"",
//							device_info.get("action"),
//							device_info.get("txn_id")
//					};
//					logger.info(" Rule Action  Start.."+Arrays.toString(my_action_arr));
//					
//					action_output = rea.startRuleEngine(my_action_arr);					
//					logger.info("Rule Filter Action Output is ["+action_output+"]");
//
//					try {
//						new LogWriter().writeFeatureEvents(myWriter, device_info.get("IMEIESNMEID"),
//								device_info.get("DeviceType"), device_info.get("MultipleSIMStatus"),device_info.get("SNofDevice"),
//								device_info.get("Devicelaunchdate"),device_info.get("DeviceStatus"),
//								device_info.get("txn_id"), device_info.get("operator"), device_info.get("file_name"), "RuleActionEnd",
//								device_info.get("ruleid"), device_info.get("rule_name"), action_output,new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));				
//					}catch(Exception e ) {
//						logger.info(" rule filter exception getMyFeatureRule 44 +"  + e);
//					}
//
//					break;
//				}
//			}
//
//			//
//			//		
//			//		rule_detail = checkMyRule(conn ,device_info);
//			//		if(rule_detail.get("rule_name") != null ){
//			//			logger.info("Rule Detials"+rule_detail+rule_detail.get("rule_name"));
//			//			logger.info("Breaking the Rules");
//			//			break;
//			//		}
//
//		}
//		return rule_detail;
//	}
//
//	private HashMap<String, String> checkMyRule(Connection  conn,HashMap<String, String> device_info) {
//		ResultSet rs_set = null;
//		Statement stmt_set = null;
//
//
//		HashMap<String, String> rule_details = new HashMap<String, String>();
//
//		String filter_set_query = "select * from rule_filter_set where rule_id='"+device_info.get("ruleid")+"'";
//		try{
//			logger.info("Check my rule set query ["+filter_set_query+"]");
//			logger.info("Rule filter Set Query"+filter_set_query+"");
//			stmt_set = conn.createStatement();
//			rs_set = stmt_set.executeQuery(filter_set_query);
//			while(rs_set.next()){
//				if(rs_set.getString("rule_set_type").equalsIgnoreCase("from_db")){
//					logger.info("checing in DB");
//					logger.info("Checking in DB Rules ["+filter_set_query+"]");
//					rule_details = fromDBRule(conn,device_info);
//
//				}
//				else if(rs_set.getString("rule_set_type").equalsIgnoreCase("norm_check")){
//					logger.info("checking in Norm Check");
//					logger.info("Checking in Normal check rule ["+filter_set_query+"]");
//					rule_details = fromNormCheckRule(conn,device_info);
//
//				}
//				else if(rs_set.getString("rule_set_type").equalsIgnoreCase("java_api")){
//					logger.info("checkingin javaapi");
//					logger.info("Checking in Java API Rules ["+filter_set_query+"]");
//					rule_details = fromJavaAPI(conn,device_info);
//				}
//				if(rule_details.get("output")!=null){
//					logger.info("Rule Details find after rule filter ["+rule_details+"]");
//					if(device_info.get("output").equalsIgnoreCase(rule_details.get("output"))){
//						rule_details=null;
//					}
//					else{
//						if(device_info.get("failed_rule_aciton").equalsIgnoreCase("rule")){
//							rule_details.put("rule_name", null);
//						}
//						else{
//							rule_details.put("period" , device_info.get("period"));
//							rule_details.put("action",device_info.get("action"));
//							ruleFilterAction(conn,rule_details.get("action_type"),device_info);							
//						}
//					}
//				}
//			}
//		}
//		catch(Exception e){
//			e.printStackTrace();
//			logger.info("Exception ["+e+"]");
//
//		}
//		finally{
//			try {
//				stmt_set.close();
//				rs_set.close();
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//
//		return rule_details;
//
//	}
//
//
//	private HashMap<String, String> fromJavaAPI(Connection conn,HashMap<String, String> device_info) {
//		HashMap<String, String> rule_details = new HashMap<String, String>();
//		ResultSet rs = null;
//		Statement stmt= null;
//		String query = "select * from rule_filter_java_api_db where rule_id='"+device_info.get("ruleid")+"'";
//		logger.info("Java api Rule Query"+query);
//		logger.info("Query for Java API Rule filter ["+query+"]");
//		String output ="";
//
//		try{
//			stmt = conn.createStatement();
//			rs = stmt.executeQuery(query);
//			while(rs.next()){
//				String complete_api = rs.getString("context_path");
//				String[] values = rs.getString("param_value").split(",");
//				for(int i=0;i<values.length;i++){
//					complete_api +=" "+device_info.get(values[i].substring(3));
//				}
//				logger.info("Complete command to call the java api Rule"+complete_api);
//				logger.info("API to call Java Rule Filter ["+complete_api+"]");
//				Process process = Runtime.getRuntime().exec(complete_api);
//				InputStream is = process.getInputStream();
//				Scanner sc = new Scanner(is);
//				if(sc.hasNext()){
//					output =  sc.next();
//				}			
//				logger.info("Output from Java API Rule Filter ["+output+"]");
//
//				logger.info("Complete command to call the java api output"+complete_api);
//
//				rule_details.put("output",output);
//				rule_details.put("action_type", rs.getString("action_type"));
//				rule_details.put("rule_name", device_info.get("rule_name"));
//				rule_details.put("rule_id", device_info.get("ruleid"));		}
//
//		}
//
//		catch(Exception e){
//			e.printStackTrace();
//			logger.info("Exception ["+e+"]");
//
//		}
//		finally{
//			try {
//				rs.close();
//				stmt.close();
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}			
//		}
//		return rule_details;
//	}
//
//	private HashMap<String, String> fromDBRule(Connection conn,HashMap<String, String> device_info) {
//		ResultSet rs = null;
//		Statement stmt = null;
//		ResultSet rs1 = null;
//		Statement stmt1 = null;
//		String my_rule_query = "";
//		HashMap<String, String> rule_details = new HashMap<String, String>();
//
//		String query = "select * from rule_filter_condition_db where rule_id='"+device_info.get("ruleid")+"'";
//
//		logger.info("Query for DB Check Rule filter ["+query+"]");
//
//		try{
//			stmt = conn.createStatement();
//			rs=stmt.executeQuery(query);			
//			while(rs.next()){
//
//				my_rule_query = "select * from "+rs.getString("table_name")+" where ";
//				String[] keys = rs.getString("param_key").split(",");
//				String[] condition = rs.getString("param_condition").split(",");
//				String[] values = rs.getString("param_value").split(",");
//				String[] operators = rs.getString("param_operator").split(",");
//				String operator="";
//
//				for(int i=0;i<keys.length;i++){
//					if(i>1){
//						operator = operators[i-1];
//					}
//					if(values[i].startsWith("hm.")){
//						my_rule_query += " "+keys[i]+condition[i]+"'"+device_info.get(values[i].substring(3))+"' "+operator; 						
//					}
//					else{
//						my_rule_query += " "+keys[i]+condition[i]+"'"+values[i]+"' "+operator; 						
//					}
//				}
//				logger.info("Query for DB check query ["+my_rule_query+"]");								
//				stmt1 = conn.createStatement();
//				rs1 = stmt1.executeQuery(my_rule_query);
//				if(rs1.next()){
//					if(parseExpression(Integer.parseInt(rs1.getString(rs.getString("result_key"))),rs.getString("result_condition"),Integer.parseInt(rs.getString("result_value")))){
//						//						ruleFilterAction(conn,rs.getString("action_type"),device_info);
//						rule_details.put("output", "Yes");
//						rule_details.put("action", rs.getString("action_type"));
//						rule_details.put("rule_name", device_info.get("rule_name"));
//						rule_details.put("rule_id", device_info.get("ruleid"));
//					}
//				}				
//			}
//			logger.info("Final Rule Details for DB check Rule filter ["+query+"]");
//
//		}
//		catch(Exception e){
//			e.printStackTrace();
//			logger.info("Exception ["+e+"]");
//
//		}
//		finally{
//			try {
//				rs.close();
//				stmt.close();
//				rs1.close();
//				stmt1.close();
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//
//
//		}
//
//		return rule_details;
//	}
//	private HashMap<String, String> fromNormCheckRule(Connection conn,HashMap<String, String> device_info) {
//		ResultSet rsnorm = null;
//		Statement stmtnorm = null;
//		//		ResultSet rs2 = null;
//		//		Statement stmt2 = null;
//		//		ResultSet rs3=null;
//		//		Statement stmt3=null;
//		HashMap<String, String> rule_details = new HashMap<String, String>();
//
//		String query = "select * from rule_filter_norm_check_db where rule_id='"+device_info.get("ruleid")+"'";
//		logger.info("In Norm Check Query"+query);
//		logger.info("Query for Normal check query ["+query+"]");
//		String my_rule_query = "";
//		try{
//			stmtnorm = conn.createStatement();
//			rsnorm=stmtnorm.executeQuery(query);			
//			while(rsnorm.next()){				
//				String[] keys = rsnorm.getString("param_key").split(",");
//				String[] condition = rsnorm.getString("param_condition").split(",");
//				String[] values = rsnorm.getString("param_value").split(",");
//				String[] operators = rsnorm.getString("param_operator").split(",");
//				String operator="";
//				logger.info("norm check 1");
//				boolean my_flag = false;
//				for(int i=0;i<keys.length;i++){
//					if(keys[i].startsWith("hm.")){
//						if(keys[i].endsWith(".len")){
//							if(i==0){
//								my_flag = parseExpression(
//										parseMethod(
//												device_info.get(keys[i].substring(3).substring(0,keys[i].substring(3).length()-4)),
//												keys[i].substring(keys[i].length()-3 ,keys[i].length()))
//										,condition[i],Integer.parseInt(values[i])); 
//								logger.info("my_flag 1 ="+my_flag);
//
//							}
//							else{
//								my_flag = parseOperator(my_flag,
//										operators[i-1],
//										parseExpression(
//												parseMethod(
//														device_info.get(keys[i].substring(3).substring(0,keys[i].substring(3).length()-4)),
//														keys[i].substring(keys[i].length()-3 ,keys[i].length()))
//												,condition[i],Integer.parseInt(values[i]))
//										);
//							}							
//						}
//						else{
//						}
//					}
//				}
//				logger.info("my_flag ="+my_flag);
//				if(my_flag){
//
//					//					ruleFilterAction(conn,rsnorm.getString("action_type"),device_info);
//					rule_details.put("output", "Yes");
//					rule_details.put("action", rsnorm.getString("action_type"));
//					rule_details.put("rule_name", device_info.get("rule_name"));
//					rule_details.put("rule_id", device_info.get("ruleid"));
//
//				}
//			}
//			logger.info("Query for Normal check final rule details ["+rule_details+"]");
//
//		}
//		catch(Exception e){
//			logger.info("Exception ["+e+"]");
//			e.printStackTrace();
//		}	
//		finally{
//			try {
//				//				rs2.close();
//				//				stmt2.close();
//				//				rs3.close();
//				//				stmt3.close();
//				rsnorm.close();
//				stmtnorm.close();
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		}
//		return rule_details;
//
//	}
//
//	private void ruleFilterAction(Connection conn, String action_type,HashMap<String, String> device_info) {
//		Statement stmt2 = null;
//		ResultSet rs2=null;
//		Statement stmt3=null;
//		try{
//			if(action_type.equalsIgnoreCase("Insert")){
//				String action_query = "select * from rule_filter_action_db where rule_id='"+device_info.get("ruleid")+"'";
//				stmt2 = conn.createStatement();
//				rs2 = stmt2.executeQuery(action_query);
//				logger.info("Rule filter action query ["+action_query+"]");
//				logger.info("NOrm check Action DB Query"+action_query);
//				while(rs2.next()){		
//					String[] insert_keys = rs2.getString("param_key").split(",");
//					String[] insert_values = rs2.getString("param_value").split(",");
//					String action_insert_query = "insert into "+rs2.getString("context_path")+" (";
//					String values1=" values(";
//					for(int j=0;j<insert_keys.length;j++){
//						logger.info(insert_keys[j]+" value "+insert_values[j]);
//						action_insert_query  = action_insert_query + insert_keys[j] + ",";
//						if(insert_values[j].startsWith("hm.")){
//							values1 = values1+"'"+device_info.get(insert_values[j].substring(3))+"',";
//						}
//						else{
//							values1 = values1 + "'"+insert_values[j]+"',";									
//						}
//					}
//					action_insert_query  = action_insert_query.substring( 0, action_insert_query.length() - 1 )+") "+values1.substring( 0, values1.length() - 1 )+")";
//					logger.info("Query to insert in rule filter action ["+action_insert_query+"]");
//					stmt3=conn.createStatement();
//					stmt3.executeUpdate(action_insert_query);
//					 // conn.commit();
//
//				}						
//			}
//			else if(action_type.equals("java_api")){
//				String action_query = "select * from rule_filter_action_db where rule_id='"+device_info.get("ruleid")+"'";
//				logger.info("java api calling action "+action_query);
//				logger.info("Query to java_api in rule filter action ["+action_query+"]");
//				stmt2 = conn.createStatement();
//				rs2 = stmt2.executeQuery(action_query);
//				String output="";				
//				while(rs2.next()){
//					String complete_api = rs2.getString("context_path");
//					String[] values = rs2.getString("param_value").split(",");
//					for(int i=0;i<values.length;i++){
//						complete_api +=" "+device_info.get(values[i].substring(3));
//					}
//					logger.info("Complete command to call the java api Action"+complete_api);
//					logger.info("Complete command to call the java api Action["+complete_api+"]");
//					Process process = Runtime.getRuntime().exec(complete_api);
//					InputStream is = process.getInputStream();
//					Scanner sc = new Scanner(is);
//					if(sc.hasNext()){
//						output =  sc.next();
//					}			
//					logger.info("Java API Output action"+output);
//					logger.info("Java API Rule filter action["+output+"]");
//
//				}
//			}
//		}
//		catch(Exception e){
//			logger.info("Exception ["+e+"]");
//			e.printStackTrace();
//		}
//		finally{
//			try {
//				stmt2.close();
//				rs2.close();
//				if(stmt3!=null)
//					stmt3.close();
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		}
//
//
//	}
//
//	private static boolean parseExpression(int number1, String operator, int number2)
//	{
//		if ("<".equals(operator)) {
//			return number1 < number2;
//		} else if (">".equals(operator)) {
//			return number1 > number2;
//		} else if ("<=".equals(operator)) {
//			return number1 <= number2;
//		} else if (">=".equals(operator)) {
//			return number1 >= number2;
//		} else if ("==".equals(operator)) {
//			logger.info(number1+"checking both number "+number2);
//			return number1 == number2;
//		}else if ("!=".equals(operator)) {
//			logger.info(number1+"checking both number "+number2);
//			return number1 != number2;
//		} else { 
//			throw new IllegalArgumentException("Invalid operator");
//		}
//	}
//
//	private static int parseMethod(String my_string, String operator)
//	{
//		if ("len".equals(operator)) {
//			logger.info("my imei length"+my_string.length()+my_string);
//			return my_string.length();
//		}  else { 
//			throw new IllegalArgumentException("Invalid operator");
//		}
//	}
//
//	private static boolean parseOperator(boolean param1, String operator,boolean param2)
//	{
//		if ("||".equals(operator)) {
//			return param1 || param2;
//		}
//		else if("&&".equals(operator)){
//			return param1 && param2;
//		}
//		else { 
//			throw new IllegalArgumentException("Invalid operator");
//		}
//	}
//
//
//}
//
//
//
//
//
//
////
////device_info.put("rule_name", rule.rule_name);
////device_info.put("output", rule.output);
////device_info.put("ruleid", rule.ruleid);
////device_info.put("period", rule.period);
////device_info.put("action", rule.action);
////device_info.put("failed_rule_aciton", rule.failed_rule_aciton);
////
////
//////output = RuleEngineApplication.main();
////
////
////new LogWriter().writeEvents(myWriter, device_info.get("servedIMEI"),
////		device_info.get("recordType"), device_info.get("servedIMSI"),device_info.get("servedMSISDN"),
////		device_info.get("systemType"),
////		device_info.get("operator"), device_info.get("file_name"), device_info.get("record_time"), "RuleCheckStart",
////		device_info.get("ruleid"), device_info.get("rule_name"), "",new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));				
////
////new LogWriter().writeEvents(myWriter, device_info.get("servedIMEI"),
////		device_info.get("recordType"), device_info.get("servedIMSI"),device_info.get("servedMSISDN"),
////		device_info.get("systemType"),
////		device_info.get("operator"), device_info.get("file_name"), device_info.get("record_time"), "RuleCheckEnd",
////		device_info.get("ruleid"), device_info.get("rule_name"), output,new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));				
////
////
////if(device_info.get("output").equalsIgnoreCase(output)){
////	rule_detail.put("rule_name", null);				
////}
////else{
////	if(device_info.get("failed_rule_aciton").equalsIgnoreCase("rule")){
////		rule_detail.put("rule_name", null);
////	}
////	else{
////		rule_detail.put("period" , device_info.get("period"));
////		rule_detail.put("action",device_info.get("action"));
////		new LogWriter().writeEvents(myWriter, device_info.get("servedIMEI"),
////				device_info.get("recordType"), device_info.get("servedIMSI"),device_info.get("servedMSISDN"),
////				device_info.get("systemType"),
////				device_info.get("operator"), device_info.get("file_name"), device_info.get("record_time"), "RuleActionStart",
////				device_info.get("ruleid"), device_info.get("rule_name"), output,new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));				
////		
////		// Perform Action 
////		
////		
////		new LogWriter().writeEvents(myWriter, device_info.get("servedIMEI"),
////				device_info.get("recordType"), device_info.get("servedIMSI"),device_info.get("servedMSISDN"),
////				device_info.get("systemType"),
////				device_info.get("operator"), device_info.get("file_name"), device_info.get("record_time"), "RuleActionEnd",
////				device_info.get("ruleid"), device_info.get("rule_name"), action_output,new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));				
////
////		
////		break;
////	}
////}
////
//////rule_detail = checkMyRule(conn ,device_info);			
//////if(rule_detail.get("rule_name") != null ){
//////	logger.info("Rule Detials"+rule_detail+rule_detail.get("rule_name"));
//////	logger.info("Breaking the Rules");
//////	break;
//////}
//
//			device_info.put("rule_name", rule.rule_name);
//			device_info.put("output", rule.output);
//			device_info.put("ruleid", rule.ruleid);
//			device_info.put("period", rule.period);
//			device_info.put("action", rule.action);
//			device_info.put("failed_rule_aciton", rule.failed_rule_aciton);
//			
//			
////			output = RuleEngineApplication.main();
//			
//			
//			new LogWriter().writeEvents(myWriter, device_info.get("servedIMEI"),
//					device_info.get("recordType"), device_info.get("servedIMSI"),device_info.get("servedMSISDN"),
//					device_info.get("systemType"),
//					device_info.get("operator"), device_info.get("file_name"), device_info.get("record_time"), "RuleCheckStart",
//					device_info.get("ruleid"), device_info.get("rule_name"), "",new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));				
//			
//			new LogWriter().writeEvents(myWriter, device_info.get("servedIMEI"),
//					device_info.get("recordType"), device_info.get("servedIMSI"),device_info.get("servedMSISDN"),
//					device_info.get("systemType"),
//					device_info.get("operator"), device_info.get("file_name"), device_info.get("record_time"), "RuleCheckEnd",
//					device_info.get("ruleid"), device_info.get("rule_name"), output,new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));				
//
//			
//			if(device_info.get("output").equalsIgnoreCase(output)){
//				rule_detail.put("rule_name", null);				
//			}
//			else{
//				if(device_info.get("failed_rule_aciton").equalsIgnoreCase("rule")){
//					rule_detail.put("rule_name", null);
//				}
//				else{
//					rule_detail.put("period" , device_info.get("period"));
//					rule_detail.put("action",device_info.get("action"));
//					new LogWriter().writeEvents(myWriter, device_info.get("servedIMEI"),
//							device_info.get("recordType"), device_info.get("servedIMSI"),device_info.get("servedMSISDN"),
//							device_info.get("systemType"),
//							device_info.get("operator"), device_info.get("file_name"), device_info.get("record_time"), "RuleActionStart",
//							device_info.get("ruleid"), device_info.get("rule_name"), output,new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));				
//					
//					// Perform Action 
//					
//					
//					new LogWriter().writeEvents(myWriter, device_info.get("servedIMEI"),
//							device_info.get("recordType"), device_info.get("servedIMSI"),device_info.get("servedMSISDN"),
//							device_info.get("systemType"),
//							device_info.get("operator"), device_info.get("file_name"), device_info.get("record_time"), "RuleActionEnd",
//							device_info.get("ruleid"), device_info.get("rule_name"), action_output,new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));				
//
//					
//					break;
//				}
//			}
//			
////			rule_detail = checkMyRule(conn ,device_info);			
////			if(rule_detail.get("rule_name") != null ){
////				 // System.out.println("Rule Detials"+rule_detail+rule_detail.get("rule_name"));
////				 // System.out.println("Breaking the Rules");
////				break;
////			}

