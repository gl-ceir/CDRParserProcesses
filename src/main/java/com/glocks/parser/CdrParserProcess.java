package com.glocks.parser;

import com.glocks.constants.PropertiesReader;
import com.glocks.dao.MessageConfigurationDbDao;
import com.glocks.dao.PolicyBreachNotificationDao;
import com.glocks.db.ConnectionConfiguration;
import com.glocks.files.FileList;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import com.glocks.pojo.MessageConfigurationDb;
import com.glocks.pojo.PolicyBreachNotification;
import com.glocks.util.Util;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootConfiguration
@EnableAutoConfiguration
@SpringBootApplication(scanBasePackages = {"com.glocks"})
@EnableEncryptableProperties
public class CdrParserProcess {

    static Logger logger = LogManager.getLogger(CdrParserProcess.class);
    static StackTraceElement l = new Exception().getStackTrace()[0];
    static String appdbName = null;
    static String auddbName = null;
    static String serverName = null;

    public static PropertiesReader propertiesReader = null;
    static ConnectionConfiguration connectionConfiguration = null;

    public static void main(String args[]) { // OPERATOR FilePath

        ApplicationContext context = SpringApplication.run(CdrParserProcess.class, args);
        propertiesReader = (PropertiesReader) context.getBean("propertiesReader");
        connectionConfiguration = (ConnectionConfiguration) context.getBean("connectionConfiguration");
        logger.info("connectionConfiguration :" + connectionConfiguration.getConnection().toString());

        //  conn = (Connection) new com.glocks.db.MySQLConnection().getConnection();
        Connection conn = connectionConfiguration.getConnection();
        appdbName = propertiesReader.appdbName;
        auddbName = propertiesReader.auddbName;
        serverName = propertiesReader.serverName;
        logger.info(" appdbName:" + appdbName);

        String filePath = null;
        if (args[0] == null) {
            logger.error("Enter the Correct File Path");
        } else {
            filePath = args[0].trim();
        }
        if (!filePath.endsWith("/")) {
            filePath += "/";
        }
        try {

            CdrParserProces(conn, filePath);
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                logger.error("" + e);
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error(ex);
            }
            System.exit(0);
        }
    }

    public static void CdrParserProces(Connection conn, String filePath) {

        logger.debug(" FilePath :" + filePath);

        String source = null;
        String operator = null;
        if (filePath != null) {
            String[] arrOfStr = filePath.split("/", 0);
            int val = 0;
            for (int i = (arrOfStr.length - 1); i >= 0; i--) {
                if (val == 1) {
                    source = arrOfStr[i];
                }
                if (val == 2) {
                    operator = arrOfStr[i].toUpperCase();
                }
                val++;
            }
        }
        String fileName = new FileList().readOldestOneFile(filePath);
        if (fileName == null) {
            logger.debug(" No File Found");
            return;
        }
        logger.debug(" FilePath :" + filePath + "; FileName:" + fileName + ";source : " + source + " ; Operator : "
                + operator);
        String operator_tag = getOperatorTag(conn, operator);
        logger.debug("Operator tag is [" + operator_tag + "] ");
        ArrayList rulelist = new ArrayList<Rule>();
        String period = checkGraceStatus(conn);
        logger.debug("Period is [" + period + "] ");
        rulelist = getRuleDetails(operator, conn, operator_tag, period);
        logger.debug("rule list to be  " + rulelist);
        addCDRInProfileWithRule(operator, conn, rulelist, operator_tag, period, filePath, source, fileName);
    }

    private static void addCDRInProfileWithRule(String operator, Connection conn, ArrayList<Rule> rulelist,
            String operator_tag, String period, String filePath, String source, String fileName) {
        ExecutorService executorService = Executors.newCachedThreadPool();

        int insertedKey = insertModuleAudit(conn, "P3", operator + "_" + source);
        long executionStartTime = new Date().getTime();
        int output = 0;
        String my_query = "";
        HashMap<String, String> my_rule_detail;
        String failed_rule_name = "";
        int failed_rule_id = 0;
        String finalAction = "";
        int usageInsert = 0;
        int usageUpdate = 0;
        int duplicateInsert = 0;
        int duplicateUpdate = 0;

        int usageInsertForeign = 0;
        int usageUpdateForeign = 0;
        int duplicateInsertForeign = 0;
        int duplicateUpdateForeign = 0;

        int nullInsert = 0;
        int nullUpdate = 0;
        File file = null;
        String line = null;
        String[] data = null;
        BufferedReader br = null;
        FileReader fr = null;
        BufferedWriter bw1 = null;
        int counter = 1;
        int foreignMsisdn = 0;
        int fileParseLimit = 1;

        int errorCount = 0;
        int fileCount = 0;
        try {
            String server_origin = propertiesReader.serverName;
            file = new File(filePath + fileName);
            try (Stream<String> lines = Files.lines(file.toPath())) {
                fileCount = (int) lines.count();
                logger.debug("File Count: " + fileCount);
            } catch (Exception e) {
                logger.warn("" + e);
            }
            String enableForeignSimHandling = getSystemConfigDetailsByTag(conn, "enableForeignSimHandling");
            // if fileName present in SQlFolder , get count of file else make countt 1
            fileParseLimit = getExsistingSqlFileDetails(conn, operator, source, fileName);
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            bw1 = getSqlFileWriter(conn, operator, source, fileName);
            // new com.glocks.files.FileList().moveCDRFile(conn, fileName, operator,            // filePath, source);
            Date p2Starttime = new Date();
            HashMap<String, String> device_info = new HashMap<String, String>();
            RuleFilter rule_filter = new RuleFilter();
            // CDR File Writer
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String sdfTime = sdf.format(date);
            boolean isOracle = conn.toString().contains("oracle");
            String dateFunction = Util.defaultDateNow(isOracle);
            logger.debug("fileParseLimit " + fileParseLimit);
            // for (int i = 0; i <= fileParseLimit; i++) {
            br.readLine();
            // counter++;
            // }
            Map<String, String> operatorSeries = getActualOperator(conn);
            String[] testImies = getTestImeis(conn).split(",");
            HashMap<String, Date> validTacMap = getValidTac(conn);
            String dateType = null;
            while ((line = br.readLine()) != null) {
                data = line.split(",", -1);
                logger.info(" Line Started " + Arrays.toString(data));
                try {
                    device_info.put("file_name", fileName.trim());
                    device_info.put("IMEI", data[0].trim());
                    device_info.put("IMSI", data[1].trim());
                    device_info.put("MSISDN", ((data[2].trim().startsWith("19") || data[2].trim().startsWith("00")) ? data[2].substring(2) : data[2]));
                    device_info.put("record_type", data[3].trim());
                    device_info.put("system_type", data[4].trim());
                    device_info.put("source", data[5].trim());
                    device_info.put("raw_cdr_file_name", data[6].trim());
                    device_info.put("modified_imei", data[0].length() > 14 ? data[0].substring(0, 14) : data[0]);
                    device_info.put("tac", data[0].length() > 8 ? data[0].substring(0, 8) : data[0]);
                    logger.debug("modified_imei is  " + device_info.get("modified_imei"));
                    String imei_arrivalTime = null;
                    dateType = "yyyyMMdd";
                    if (propertiesReader.ddMMyyyySource.contains(data[5].trim())) {
                        dateType = "ddMMyyyy";
                    } else if (propertiesReader.yyMMddSource.contains(data[5].trim())) {
                        dateType = "yyMMdd";
                    } else if (propertiesReader.ddMMyySource.contains(data[5].trim())) {
                        dateType = "ddMMddyy";
                    }
                    imei_arrivalTime = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat(dateType).parse(data[7]));
                    device_info.put("imei_arrival_time", imei_arrivalTime);
                    device_info.put("operator", operator.trim());
                    device_info.put("record_time", sdfTime);
                    device_info.put("operator_tag", operator_tag);
                    device_info.put("actual_operator", operatorSeries.get(device_info.get("MSISDN").substring(0, 5)));
                    boolean anyMatch = Arrays.stream(testImies).anyMatch(s -> device_info.get("modified_imei").startsWith(s));
                    if (anyMatch) {
                        String qury = " insert into test_imei_details  " + "(imei ,IMSI, record_type , system_type , source,raw_cdr_file_name,imei_arrival_time ,operator, file_name ,msisdn, created_on , modified_on    )  values "
                                + "('" + device_info.get("modified_imei") + "' , '" + device_info.get("IMSI") + "', '" + device_info.get("record_type") + "' ,'" + device_info.get("system_type") + "' , '" + device_info.get("source") + "',  '" + device_info.get("raw_cdr_file_name") + "', "
                                + "'" + device_info.get("imei_arrival_time") + "', '" + device_info.get("operator") + "', '" + device_info.get("file_name") + "',   '" + device_info.get("MSISDN") + "', now(),  now()   ) ";
                        logger.debug(".test_imei_details :: ." + qury);
                        executorService.execute(new InsertDbDao(conn, qury));
                    }
                    device_info.put("testImeiFlag", String.valueOf(anyMatch));
                    String failedRuleDate = null;
                    counter++;
                    if (device_info.get("MSISDN").startsWith("855") && device_info.get("IMSI").startsWith("456")) {
                        // logger.debug("Local Sim  " + Arrays.toString(data));
                        device_info.put("msisdn_type", "LocalSim");
                        my_rule_detail = rule_filter.getMyRule(conn, device_info, rulelist);
                        logger.debug("getMyRule done with rule name " + my_rule_detail.get("rule_name") + " rule ID " + my_rule_detail.get("rule_id"));
                        if (my_rule_detail.get("rule_name") != null) {
                            failed_rule_name = my_rule_detail.get("rule_name");
                            failed_rule_id = my_rule_detail.get("rule_id") == null ? 0
                                    : Integer.valueOf(my_rule_detail.get("rule_id"));
                            period = my_rule_detail.get("period");
                            failedRuleDate = dateFunction;
                        }
                        if (failed_rule_name == null || failed_rule_name.equals("")
                                || failed_rule_name.equalsIgnoreCase("EXISTS_IN_ALL_ACTIVE_DB")) {
                            finalAction = "ALLOWED";
                            failed_rule_name = null;
                            failed_rule_id = 0;
                        } else {
                            //        logger.debug("FailedRule Categorization");
                            if (failed_rule_name.equalsIgnoreCase("EXIST_IN_GSMABLACKLIST_DB")
                                    || failed_rule_name.equalsIgnoreCase("EXIST_IN_BLACKLIST_DB")) {
                                finalAction = "BLOCKED";
                            } else if (period.equalsIgnoreCase("Grace")) {
                                finalAction = "SYS_REG";
                            } else if (period.equalsIgnoreCase("Post_Grace")) {
                                finalAction = "USER_REG";
                                sendMessageToMsisdn(conn, device_info.get("MSISDN"), device_info.get("IMEI"));
                            }
                        }
                    } else {
                        //    logger.debug("Foreign Sim Started " + Arrays.toString(data));
                        foreignMsisdn++;
                        device_info.put("msisdn_type", "ForeignSim");
                        if (enableForeignSimHandling.equals("False")) {
                            logger.debug("Foreign Sim Without Enable Foreign Sim ");
                            continue;
                        }
                    }
                    String gsmaTac = null;
                    if (validTacMap.containsKey(device_info.get("tac"))) {
                        gsmaTac = "V";
                        //     logger.debug("allocation_date after   : ----" + validTacMap.get(device_info.get("tac")));
                        if (validTacMap.get(device_info.get("tac")).before(new Date())) {
                            device_info.put("isUsedFlag", "false");
                        } else {
                            device_info.put("isUsedFlag", "true");
                        }
                    } else {
                        gsmaTac = "I";
                        //     logger.debug("allocation_date is null returning false  ");
                        device_info.put("isUsedFlag", "false");
                    }
                    //  logger.debug("Going To Insert /Update as Per Conditions for " + device_info.get("msisdn_type"));
                    output = checkDeviceUsageDB(conn, device_info.get("modified_imei"), device_info.get("MSISDN"), device_info.get("imei_arrival_time"), device_info.get("msisdn_type"), device_info);
                    if (output == 0) { // imei not found in usagedb
                        //      logger.debug("imei not found in usagedb");
                        my_query = getInsertUsageDbQuery(device_info, dateFunction, failed_rule_name, failed_rule_id, period, finalAction, failedRuleDate, server_origin, gsmaTac);
                        if (device_info.get("msisdn_type").equalsIgnoreCase("LocalSim")) {
                            usageInsert++;
                        } else {
                            usageInsertForeign++;
                        }
                    } else if (output == 1) { // new ArrivalTime came from file > arrival time in db already // imei found
                        // // with same msisdn update_raw_cdr_file_name , update_imei_arrival_time
                        //    logger.debug("new ArrivalTime  came  from file  >  arrival time in db already");
                        my_query = getUpdateUsageDbQueryWithRawCdrFileName(device_info, dateFunction, failed_rule_name, failed_rule_id, period, finalAction, failedRuleDate, server_origin, gsmaTac);
                        if (device_info.get("msisdn_type").equalsIgnoreCase("LocalSim")) {
                            usageUpdate++;
                        } else {
                            usageUpdateForeign++;
                        }
                    } else if (output == 3) { // imei found with same msisdn update_raw_cdr_file_name  update_imei_arrival_time
                        //       logger.debug("imei found with same msisdn  update_raw_cdr_file_name , update_imei_arrival_time");
                        my_query = getUpdateUsageDbQuery(device_info, dateFunction, failed_rule_name, failed_rule_id, period, finalAction, failedRuleDate, server_origin, gsmaTac);
                        if (device_info.get("msisdn_type").equalsIgnoreCase("LocalSim")) {
                            usageUpdate++;
                        } else {
                            usageUpdateForeign++;
                        }
                    } else if (output == 2) { // imei found with different msisdn
                        //      logger.debug("imei found with different msisdn");
                        output = checkDeviceDuplicateDB(conn, device_info.get("modified_imei"), device_info.get("MSISDN"), device_info.get("imei_arrival_time"), device_info.get("msisdn_type"), device_info);
                        switch (output) {
                            case 0:
                                my_query = getInsertDuplicateDbQuery(device_info, dateFunction, failed_rule_name, failed_rule_id, period, finalAction, failedRuleDate, server_origin, gsmaTac);
                                if (device_info.get("msisdn_type").equalsIgnoreCase("LocalSim")) {
                                    duplicateInsert++;
                                } else {
                                    duplicateInsertForeign++;
                                }
                                break;
                            case 1:
                                my_query = getUpdateDuplicateDbQueryWithRawCdrFileName(device_info, dateFunction, failed_rule_name, failed_rule_id, period, finalAction, failedRuleDate, server_origin, gsmaTac);
                                if (device_info.get("msisdn_type").equalsIgnoreCase("LocalSim")) {
                                    duplicateUpdate++;
                                } else {
                                    duplicateUpdateForeign++;
                                }
                                break;
                            default:
                                my_query = getUpdateDuplicateDbQuery(device_info, dateFunction, failed_rule_name, failed_rule_id, period, finalAction, failedRuleDate, server_origin, gsmaTac);
                                if (device_info.get("msisdn_type").equalsIgnoreCase("LocalSim")) {
                                    duplicateUpdate++;
                                } else {
                                    duplicateUpdateForeign++;
                                }
                                break;
                        }
                    }
                    logger.info("query : " + my_query);
                    if (my_query.contains("insert")) {
                        executorService.execute(new InsertDbDao(conn, my_query));
                    } else {
                        //    logger.debug(" writing query in file== " + my_query);
                        bw1.write(my_query + ";");
                        bw1.newLine();
                    }
                    logger.info("Remaining List :: " + (fileCount - (counter + errorCount)));
                } catch (Exception e) {
                    logger.error("Error in line -- " + Arrays.toString(data) + " [] Error " + e.getLocalizedMessage() + " [] " + e.getMessage() + "Total ErrorCount -- " + errorCount++);
                }

            } // While End
            executorService.shutdown();

            Date p2Endtime = new Date();
            cdrFileDetailsUpdate(conn, operator, device_info.get("file_name"), usageInsert, usageUpdate, duplicateInsert, duplicateUpdate, nullInsert, nullUpdate, p2Starttime, p2Endtime, "all", counter, device_info.get("raw_cdr_file_name"),
                    foreignMsisdn, server_origin, usageInsertForeign, usageUpdateForeign, duplicateInsertForeign, duplicateUpdateForeign, errorCount);
            new com.glocks.files.FileList().moveCDRFile(conn, fileName, operator, filePath, source);
            updateModuleAudit(conn, 200, "Success", "", insertedKey, executionStartTime, fileCount, errorCount);
        } catch (Exception e) {
            logger.error("Errors " + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            updateModuleAudit(conn, 500, "Failure", e.getLocalizedMessage(), insertedKey, executionStartTime, fileCount, errorCount);
        } finally {
            try {
                br.close();
                bw1.close();
            } catch (Exception e) {
                logger.error(".. " + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            }
        }
    }

    private static String getInsertUsageDbQuery(HashMap<String, String> device_info, String dateFunction, String failed_rule_name, int failed_rule_id, String period, String finalAction, String failedRuleDate, String server_origin, String gsmaTac) {
        String dbName = device_info.get("msisdn_type").equalsIgnoreCase("LocalSim")
                ? "  " + appdbName + ".active_unique_imei"
                : " " + appdbName + ".active_unique_foreign_imei";
        return " insert into " + dbName
                + " (actual_imei,msisdn,imsi,create_filename,update_filename,"
                + "updated_on,created_on,system_type,failed_rule_id,failed_rule_name,tac,period,action "
                + " , mobile_operator , record_type , failed_rule_date,  modified_on ,record_time, imei , raw_cdr_file_name , imei_arrival_time , "
                + "source, update_source, feature_name , server_origin , update_imei_arrival_time ,update_raw_cdr_file_name ,actual_operator,test_imei, is_used) "
                + " values('" + device_info.get("IMEI") + "'," + "'" + device_info.get("MSISDN") + "'," + "'" + device_info.get("IMSI") + "'," + "'" + device_info.get("file_name") + "'," + "'" + device_info.get("file_name") + "',"
                + "" + dateFunction + "," + "" + dateFunction + "," + "'" + device_info.get("system_type") + "'," + "'" + failed_rule_id + "'," + "'" + failed_rule_name + "'," + "'" + device_info.get("tac") + "'," + "'" + period + "'," + "'" + finalAction + "' , " + "'" + device_info.get("operator") + "' , "
                + "'" + device_info.get("record_type") + "'," + "" + failedRuleDate + "," + "" + dateFunction + "," + "'" + device_info.get("record_time") + "' , " + "'" + device_info.get("modified_imei") + "', " + "'" + device_info.get("raw_cdr_file_name") + "',"
                + "'" + device_info.get("imei_arrival_time") + "'," + "'" + device_info.get("source") + "' , " + "'" + device_info.get("source") + "' , " + "'" + gsmaTac + "' , " + "'" + server_origin
                + "' , " + "'" + device_info.get("imei_arrival_time") + "'," + "'" + device_info.get("raw_cdr_file_name") + "' , '" + device_info.get("actual_operator") + "'   , '" + device_info.get("testImeiFlag") + "'  , '" + device_info.get("isUsedFlag") + "'           )";
    }

    private static String getUpdateUsageDbQueryWithRawCdrFileName(HashMap<String, String> device_info, String dateFunction, String failed_rule_name, int failed_rule_id, String period, String finalAction, String failedRuleDate, String server_origin, String gsmaTac) {
        String dbName
                = device_info.get("msisdn_type").equalsIgnoreCase("LocalSim")
                ? "" + appdbName + ".active_unique_imei"
                : "" + appdbName + ".active_unique_foreign_imei";
        return "update " + dbName + " set " + "update_filename = '" + device_info.get("file_name") + "', updated_on=" + dateFunction + "" + ", modified_on=" + dateFunction + "" + ", failed_rule_date=" + failedRuleDate + "" + ", failed_rule_id='" + failed_rule_id + "', failed_rule_name='" + failed_rule_name + "',"
                + "period='" + period + "',update_raw_cdr_file_name='" + device_info.get("raw_cdr_file_name") + "',update_imei_arrival_time='" + device_info.get("imei_arrival_time") + "',update_source ='" + device_info.get("source") + "',server_origin ='" + server_origin + "',action='" + finalAction + "' , imsi = '" + device_info.get("IMSI")
                + "' , is_used = '" + device_info.get("isUsedFlag") + "'  , test_imei = '" + device_info.get("testImeiFlag") + "'   "
                + "  where imei ='" + device_info.get("modified_imei") + "'";
    }

    private static String getUpdateUsageDbQuery(HashMap<String, String> device_info, String dateFunction, String failed_rule_name, int failed_rule_id, String period, String finalAction, String failedRuleDate, String server_origin, String gsmaTac) {
        String dbName
                = device_info.get("msisdn_type").equalsIgnoreCase("LocalSim")
                ? "" + appdbName + ".active_unique_imei"
                : "" + appdbName + ".active_unique_foreign_imei";
        return "update " + dbName + " set " + "update_filename = '" + device_info.get("file_name") + "', updated_on=" + dateFunction + "" + ", modified_on=" + dateFunction + "" + ", failed_rule_date=" + failedRuleDate + ""
                + ", failed_rule_id='" + failed_rule_id + "', failed_rule_name='" + failed_rule_name + "',period='" + period + "',"
                + " update_raw_cdr_file_name='" + device_info.get("raw_cdr_file_name") + "',update_imei_arrival_time='" + device_info.get("imei_arrival_time")
                + "',     update_source ='" + device_info.get("source") + "',server_origin ='" + server_origin + "',action='" + finalAction
                + "', imsi = '" + device_info.get("IMSI") + "'  , is_used = '" + device_info.get("isUsedFlag") + "'   , test_imei = '" + device_info.get("testImeiFlag") + "'      where imei ='" + device_info.get("modified_imei") + "'  ";
    }

    private static String getInsertDuplicateDbQuery(HashMap<String, String> device_info, String dateFunction, String failed_rule_name, int failed_rule_id, String period, String finalAction, String failedRuleDate, String server_origin, String gsmaTac) {
        String dbName
                = device_info.get("msisdn_type").equalsIgnoreCase("LocalSim")
                ? "" + appdbName + ".active_imei_with_different_msisdn"
                : "" + appdbName + ".active_foreign_imei_with_different_msisdn";

        return "insert into  " + dbName
                + "  (actual_imei,msisdn,imsi,create_filename,update_filename,"
                + "updated_on,created_on,system_type,failed_rule_id,failed_rule_name,tac,period,action  "
                + " , mobile_operator , record_type , failed_rule_date,  modified_on  ,record_time, imei ,raw_cdr_file_name , imei_arrival_time , source ,update_source, feature_name ,server_origin "
                + "  , update_raw_cdr_file_name ,update_imei_arrival_time,actual_operator ,test_imei ,is_used ) "
                + "values('" + device_info.get("IMEI") + "'," + "'" + device_info.get("MSISDN") + "'," + "'" + device_info.get("IMSI") + "'," + "'" + device_info.get("file_name") + "',"
                + "'" + device_info.get("file_name") + "'," + "" + dateFunction + "," + "" + dateFunction + "," + "'" + device_info.get("system_type") + "'," + "'" + failed_rule_id + "'," + "'" + failed_rule_name + "'," + "'" + device_info.get("tac") + "'," + "'" + period + "',"
                + "'" + finalAction + "' , " + "'" + device_info.get("operator") + "' , " + "'" + device_info.get("record_type") + "' , " + "" + failedRuleDate + " , " + "" + dateFunction + ",  " + "'" + device_info.get("record_time") + "', " + "'" + device_info.get("modified_imei") + "', " + "'" + device_info.get("raw_cdr_file_name") + "',"
                + "'" + device_info.get("imei_arrival_time") + "'," + "'" + device_info.get("source") + "' , " + "'" + device_info.get("source") + "' , " + "'" + gsmaTac + "' , " + "'"
                + server_origin + "' , " + "'" + device_info.get("raw_cdr_file_name") + "'," + "'" + device_info.get("imei_arrival_time") + "'   ,  '" + device_info.get("actual_operator") + "'    ,  '" + device_info.get("testImeiFlag") + "'  ,  '" + device_info.get("isUsedFlag") + "'             )";
    }

    private static String getUpdateDuplicateDbQueryWithRawCdrFileName(HashMap<String, String> device_info, String dateFunction, String failed_rule_name, int failed_rule_id, String period, String finalAction, String failedRuleDate, String server_origin, String gsmaTac) {
        String dbName
                = device_info.get("msisdn_type").equalsIgnoreCase("LocalSim")
                ? "" + appdbName + ".active_imei_with_different_msisdn"
                : "" + appdbName + ".active_foreign_imei_with_different_msisdn";

        return "update " + dbName + " set " + "update_filename = '" + device_info.get("file_name") + "', updated_on=" + dateFunction + "" + ", modified_on=" + dateFunction
                + "" + ", failed_rule_id='" + failed_rule_id + "', failed_rule_name='" + failed_rule_name + "',period='" + period + "',update_raw_cdr_file_name='" + device_info.get("raw_cdr_file_name")
                + "',update_source ='" + device_info.get("source") + "',update_imei_arrival_time='" + device_info.get("imei_arrival_time") + "',server_origin='" + server_origin + "'      ,action='"
                + finalAction + "'  ,is_used='" + device_info.get("isUsedFlag") + "'   , test_imei = '" + device_info.get("testImeiFlag") + "'       , imsi = '" + device_info.get("IMSI") + "'    where msisdn='" + device_info.get("MSISDN") + "'  and imei='" + device_info.get("modified_imei") + "'";
    }

    private static String getUpdateDuplicateDbQuery(HashMap<String, String> device_info, String dateFunction, String failed_rule_name, int failed_rule_id, String period, String finalAction, String failedRuleDate, String server_origin, String gsmaTac) {
        String dbName = device_info.get("msisdn_type").equalsIgnoreCase("LocalSim")
                ? "" + appdbName + ".active_imei_with_different_msisdn"
                : "" + appdbName + ".active_foreign_imei_with_different_msisdn";

        return "update " + dbName + " set " + "update_filename = '" + device_info.get("file_name") + "', updated_on=" + dateFunction + "" + ", modified_on=" + dateFunction + "" + ", failed_rule_id='" + failed_rule_id + "', failed_rule_name='"
                + failed_rule_name + "',period='" + period + "'  , imsi='" + device_info.get("IMSI") + "'  ,  update_source ='" + device_info.get("source") + "',   server_origin='" + server_origin + "',action='" + finalAction + "' ,is_used='" + device_info.get("isUsedFlag") + "' , test_imei = '"
                + device_info.get("testImeiFlag") + "'        where msisdn='" + device_info.get("MSISDN") + "' and    imei='" + device_info.get("modified_imei") + "'";
    }

    private static int checkDeviceDuplicateDB(Connection conn, String imei, String msisdn, String imeiArrivalTime, String msisdnType, HashMap<String, String> device_info) {
        String dbName
                = msisdnType.equalsIgnoreCase("LocalSim")
                ? "" + appdbName + ".active_imei_with_different_msisdn"
                : "" + appdbName + ".active_foreign_imei_with_different_msisdn";
        String query = null;
        ResultSet rs1 = null;
        Statement stmt = null;
        int status = 0;
        try {
            Date imeiArrival = new SimpleDateFormat("yyyyMMdd").parse(imeiArrivalTime);
            query = "select * from " + dbName + " where imei ='" + imei + "' and msisdn = '" + msisdn + "'";
            logger.debug("Checking duplicate  db" + query);
            stmt = conn.createStatement();
            rs1 = stmt.executeQuery(query);
            while (rs1.next()) {
                if ((rs1.getString("update_imei_arrival_time") == null
                        || rs1.getString("update_imei_arrival_time").equals(""))
                        || (imeiArrival.compareTo(
                                new SimpleDateFormat("yyyy-MM-dd")
                                        .parse(rs1.getString("update_imei_arrival_time")))
                        > 0)) { // imei   found  with same msisdn
                    status = 1; // update_raw_cdr_file_name='" + device_info.get("raw_cdr_file_name")
                } else {
                    status = 3;
                }
                if (!rs1.getString("imsi").equalsIgnoreCase(device_info.get("IMSI"))) {
                    logger.debug(" Differentt IMSI, File imsi " + device_info.get("IMSI") + ", Db old imsi " + rs1.getString("imsi"));
                    insertIntoImsiChangeDB(conn, device_info, rs1.getString("imsi"), rs1.getString("update_imei_arrival_time"), msisdnType, dbName);
                }

            }
        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        } finally {
            try {
                rs1.close();
                stmt.close();
            } catch (SQLException e) {
                logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            }
        }
        return status;
    }

    private static int checkDeviceUsageDB(Connection conn, String imeiIndex, String msisdn, String imeiArrivalTime, String msisdnType, HashMap<String, String> device_info) {
        String query = null;
        ResultSet rs1 = null;
        Statement stmt = null;
        String dbName = msisdnType.equalsIgnoreCase("LocalSim")
                ? "" + appdbName + ".active_unique_imei"
                : "" + appdbName + ".active_unique_foreign_imei";
        int status = 0; // imei not found
        try {
            Date imeiArrival = new SimpleDateFormat("yyyyMMdd").parse(imeiArrivalTime);
            query = "select * from  " + dbName + " where imei ='" + imeiIndex + "'     ";
            stmt = conn.createStatement();
            rs1 = stmt.executeQuery(query);
            while (rs1.next()) {
                if (rs1.getString("msisdn").equalsIgnoreCase(msisdn)) {
                    if (!rs1.getString("imsi").equalsIgnoreCase(device_info.get("IMSI"))) {
                        logger.debug(" Differentt IMSI, File imsi " + device_info.get("IMSI") + ", Db old imsi " + rs1.getString("imsi"));
                        insertIntoImsiChangeDB(conn, device_info, rs1.getString("imsi"), rs1.getString("update_imei_arrival_time"), msisdnType, dbName);
                    }
                    if ((rs1.getString("update_imei_arrival_time") == null
                            || rs1.getString("update_imei_arrival_time").equals(""))
                            || (imeiArrival.compareTo(new SimpleDateFormat("yyyy-MM-dd").parse(rs1.getString("update_imei_arrival_time")))
                            > 0)) {
                        status = 1; // update_raw_cdr_file_name='" + device_info.get("raw_cdr_file_name")
                    } else {
                        status = 3; // not to update as UPDATE_IMEI_ARRIVAL_TIME is greater already
                    }
                } else {
                    status = 2; // // imei found with different msisdn
                }
            }
            if (!msisdnType.equalsIgnoreCase("LocalSim")) {
                logger.info("unique dbQuery " + query + "[0-Notfound,1,3-Found with same msisdn,2-Found with Diff msisdn] Result:" + status);
            }
            rs1.close();
            stmt.close();
        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        } finally {
            try {
                rs1.close();
                stmt.close();

            } catch (Exception e) {
                logger.error("  :/ " + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            }
        }
        return status;
    }

    public static String getSystemConfigDetailsByTag(Connection conn, String tag) {
        String value = null;
        String query = "select value from " + appdbName + ".sys_param where tag='" + tag + "'";
        logger.info("Query " + query);
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query);) {
            while (rs.next()) {
                value = rs.getString("value");
            }
            return value;
        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        }
        return value;
    }

    public static int checkDeviceNullDB(Connection conn, String msisdn) {
        String query = null;
        ResultSet rs1 = null;
        Statement stmt = null;
        int status = 0;
        try {
            query = "select * from " + appdbName + ".null_imei where msisdn='" + msisdn + "'";
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
            query = "select a.id as rule_id,a.name as rule_name,b.output as output,b.grace_action, b.post_grace_action, b.failed_rule_action_grace, b.failed_rule_action_post_grace " + " from " + appdbName + ".rule a, " + appdbName + ".feature_rule b where  a.name=b.name  and a.state='Enabled' and b.feature='CDR' and   b." + period
                    + "_action !='NA' order by b.rule_order asc";
            logger.info("Query is " + query);
            stmt = conn.createStatement();
            rs1 = stmt.executeQuery(query);
            while (rs1.next()) {
                if (rs1.getString("rule_name").equalsIgnoreCase("IMEI_LENGTH")) {
                    if (operator_tag.equalsIgnoreCase("GSM")) {
                        // Rule rule = new
                        // Rule(rs1.getString("rule_name"),rs1.getString("output"),rs1.getString("rule_id"),period,
                        // rs1.getString(period+"_action"));
                        Rule rule
                                = new Rule(
                                        rs1.getString("rule_name"),
                                        rs1.getString("output"),
                                        rs1.getString("rule_id"),
                                        period,
                                        rs1.getString(period + "_action"),
                                        rs1.getString("failed_rule_action_" + period));
                        rule_details.add(rule);
                    }
                } else {
                    // Rule rule = new
                    // Rule(rs1.getString("rule_name"),rs1.getString("output"),rs1.getString("rule_id"),period,
                    // rs1.getString(period+"_action"));
                    Rule rule
                            = new Rule(
                                    rs1.getString("rule_name"),
                                    rs1.getString("output"),
                                    rs1.getString("rule_id"),
                                    period,
                                    rs1.getString(period + "_action"),
                                    rs1.getString("failed_rule_action_" + period));
                    rule_details.add(rule);
                }
            }
            rs1.close();
            stmt.close();
        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        } finally {
            try {
                rs1.close();
                stmt.close();

            } catch (SQLException e) {
                logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            }
        }
        return rule_details;
    }

    private static void insertIntoImsiChangeDB(Connection conn, HashMap<String, String> device_info, String oldImsi, String oldImsiDate, String msisdnType, String dbComer) {
        String dbName
                = msisdnType.equalsIgnoreCase("LocalSim")
                ? "" + appdbName + ".active_imei_with_same_msisdn_different_imsi"
                : "" + appdbName + ".active_foreign_imei_with_same_msisdn_different_imsi";

        String value = " insert into " + dbName + " (imei ,msisdn,old_imsi,old_imsi_date,new_imsi, new_imsi_date,operator,file_name,created_on ,db_table ) values  ("
                + " '" + device_info.get("IMEI") + "', '" + device_info.get("MSISDN") + "', '" + oldImsi + "', '" + oldImsiDate + "', '" + device_info.get("IMSI") + "', "
                + " '" + device_info.get("imei_arrival_time") + "', '" + device_info.get("operator") + "',  '" + device_info.get("file_name") + "',  now() ,  '" + dbComer + "'  ) ";
        logger.info("New imsi Query ----" + value);
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(value);
        } catch (Exception e) {
            logger.error("[Query]" + value + " []" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        }
    }

    private static Map getActualOperator(Connection conn) {
        Map<String, String> operatorSeries = new HashMap<String, String>();
        String query = "select  series_start, operator_name from " + appdbName + ".operator_series";
        // logger.debug(" Operator query ----" + query);
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query);) {
            while (rs.next()) {
                operatorSeries.put(rs.getString("series_start"), rs.getString("operator_name"));
            }
        } catch (Exception e) {
            logger.error("[Query]" + query + " []" + "" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        }
        return operatorSeries;
    }

    private static String getTestImeis(Connection conn) {
        String value = "";
        String query = "select value from " + appdbName + ".sys_param where tag= 'TEST_IMEI_SERIES' ";
        logger.info("New  Query ----" + query);
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query);) {
            while (rs.next()) {
                value = rs.getString("value");
            }
        } catch (Exception e) {
            logger.error("[Query]" + query + " []" + "" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        }
        return value;
    }

    private static void insertTestImei(Connection conn, HashMap<String, String> device_info) {
        String qur = null;
        try (Statement stmt = conn.createStatement();) {
            qur = " insert into test_imei_details  " + "(imei ,IMSI, record_type , system_type , source,raw_cdr_file_name,imei_arrival_time ,operator, file_name ,msisdn, created_on , modified_on    )  values "
                    + "('" + device_info.get("modified_imei") + "' , '" + device_info.get("IMSI") + "', '" + device_info.get("record_type") + "' ,'" + device_info.get("system_type") + "' , '" + device_info.get("source") + "',  '" + device_info.get("raw_cdr_file_name") + "', "
                    + "'" + device_info.get("imei_arrival_time") + "', '" + device_info.get("operator") + "', '" + device_info.get("file_name") + "',   '" + device_info.get("MSISDN") + "', now(),  now()   ) ";
            logger.debug(".test_imei_details :: ." + qur);
            stmt.executeUpdate(qur);
        } catch (Exception e) {
            logger.error("Error " + e + "[Query]" + qur);
        }
    }

    static void cdrFileDetailsUpdate(Connection conn, String operator, String fileName, int usageInsert, int usageUpdate, int duplicateInsert, int duplicateUpdate, int nullInsert, int nullUpdate, Date P2StartTime, Date P2EndTime, String source, int counter, String raw_cdr_file_name,
            int foreignMsisdn, String server_origin, int usageInsertForeign, int usageUpdateForeign, int duplicateInsertForeign, int duplicateUpdateForeign, int errorCount) {
        String query = null;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Statement stmt = null;
        query = "insert into  " + appdbName + ".cdr_file_processed_detail (total_inserts_in_usage_db,total_updates_in_usage_db ,total_insert_in_dup_db , total_updates_in_dup_db , total_insert_in_null_db , total_update_in_null_db , startTime , endTime ,operator , file_name, total_records_count ,"
                + " raw_cdr_file_name  ,source  ,foreignMsisdn  , STATUS , server_origin , total_inserts_in_foreignusage_db,total_updates_in_foreignusage_db ,total_insert_in_foreigndup_db , total_updates_in_foreigndup_db,total_error_record_count ) "
                + "values(   '" + usageInsert + "' , '" + usageUpdate + "'  , '" + duplicateInsert + "' , '" + duplicateUpdate + "' " + " ,'" + nullInsert + "' ,'" + nullUpdate + "','" + df.format(P2StartTime) + "' , '" + df.format(P2EndTime) + "' ,   '" + operator + "', '" + fileName + "' , '"
                + (counter - 1) + "' , '" + raw_cdr_file_name + "' , '" + source + "'  , '" + foreignMsisdn + "' , 'End' ,  '" + server_origin + "'   ,   '" + usageInsertForeign + "' , '" + usageUpdateForeign + "'  , '" + duplicateInsertForeign + "' , '" + duplicateUpdateForeign + "'  , '" + errorCount + "'     )  ";
        logger.info(" query is " + query);

        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
        } catch (SQLException e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        } finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error("[Query]" + query + " []" + "" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            }
        }
    }

    private static void sendMessageToMsisdn(Connection conn, String msisdn, String imei) {

        MessageConfigurationDbDao messageConfigurationDbDao = new MessageConfigurationDbDao();
        PolicyBreachNotificationDao policyBreachNotificationDao = new PolicyBreachNotificationDao();
        MessageConfigurationDb messageDb = null;

        try {
            Optional<MessageConfigurationDb> messageDbOptional = messageConfigurationDbDao.getMessageDbTag(conn, "USER_REG_MESSAGE", appdbName);
            if (messageDbOptional.isPresent()) {
                messageDb = messageDbOptional.get();
                String message = messageDb.getValue().replace("<imei>", imei);
                PolicyBreachNotification policyBreachNotification
                        = new PolicyBreachNotification("SMS", message, "", msisdn, imei);
                policyBreachNotificationDao.insertNotification(conn, policyBreachNotification);
            }
        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        }
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
            query = "select value from " + appdbName + ".sys_param where tag='GRACE_PERIOD_END_DATE'";
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
        } finally {
            try {
                rs1.close();
                stmt.close();
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
            query = "select * from " + appdbName + ".sys_param_list_value where tag='OPERATORS' and interpretation='" + operator + "'";
            logger.debug("get operator tag [" + query + "]");
            stmt = conn.createStatement();
            rs1 = stmt.executeQuery(query);
            while (rs1.next()) {
                operator_tag = rs1.getString("tag_id");
            }
        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            operator_tag = "GSM"; // if no opertor found
        } finally {
            try {
                rs1.close();
                stmt.close();
            } catch (SQLException e) {
                logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
                // e.printStackTrace();
            }
        }
        return operator_tag;
    }

    private static BufferedWriter getSqlFileWriter(Connection conn, String operator, String source, String file) {
        BufferedWriter bw1 = null;
        try {
            String foldrName
                    = getFilePath(conn, "Sql_Query_Folder") + "/" + operator.toLowerCase() + "/"; //
            File file1 = new File(foldrName);
            if (!file1.exists()) {
                file1.mkdir();
            }
            foldrName += source + "/";
            file1 = new File(foldrName);
            if (!file1.exists()) {
                file1.mkdir();
            }
            String fileNameInput1 = foldrName + file + ".sql";
            logger.info("SQL_LOADER NAME ..  " + fileNameInput1);
            File fout1 = new File(fileNameInput1);
            FileOutputStream fos1 = new FileOutputStream(fout1, true);
            bw1 = new BufferedWriter(new OutputStreamWriter(fos1));
        } catch (Exception e) {
            logger.error("e " + e);
        }
        return bw1;
    }

    public static void renameSqlFile(Connection conn, String operator, String source, String file) {
        String foldrName = getFilePath(conn, "Sql_Query_Folder") + "/" + operator.toLowerCase() + "/"; //
        foldrName += source + "/";
        String fileNameInput1 = foldrName + file + ".sql";
        logger.info("SQL_    " + fileNameInput1);

        File oldName = new File(fileNameInput1);
        File newName = new File(foldrName + file + ".tmp");

        if (oldName.renameTo(newName)) {
            logger.debug("SQL    Renamed successfully");
        } else {
            logger.debug("Error while    Renaming");
        }
    }

    public static BufferedWriter getRuleFileWriter(Connection conn) {
        BufferedWriter bw1 = null;
        try {
            String foldrName = getFilePath(conn, "Sql_Query_Folder") + "/";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            File file1 = new File(foldrName);
            file1.mkdir();
            String fileNameInput1 = foldrName + "Rule_" + sdf.format(Calendar.getInstance().getTime()) + ".txt";
            logger.info("Rule NAME   " + fileNameInput1);
            File fout1 = new File(fileNameInput1);
            FileOutputStream fos1 = new FileOutputStream(fout1, true);
            bw1 = new BufferedWriter(new OutputStreamWriter(fos1));
        } catch (Exception e) {
            logger.error("e " + e);
        }
        return bw1;
    }

    private static String getFileDetaiRecords(Connection conn, String operator) {
        String basePath = "";
        String intermPath = "";

        BufferedReader br = null;
        try {
            basePath = getFilePath(conn, "smart_file_path");
            if (!basePath.endsWith("/")) {
                basePath += "/";
            }
            intermPath = basePath + operator.toLowerCase() + "/";

        } catch (Exception e) {
            logger.error("E " + e);
        }
        return intermPath;
    }

    private static String getFolderNameByOpertor(Connection conn, String intermPath, String opertor) {
        String query = null;
        String mainFolder = null;
        String folderList = null;
        Statement stmt = null;
        ResultSet rs = null;
        File fldr = null;
        try {
            query = "select value from " + appdbName + ".sys_param where tag= '" + opertor + "_folder_list'  ";
            logger.debug("query: " + query);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                folderList = rs.getString("value");
            }
            rs.close();
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
            rs.close();
            stmt.close();
        } catch (Exception e) {
            logger.error("Error : " + e);
            e.printStackTrace();
        }
        return mainFolder + "/";
    }

    private static int getExsistingSqlFileDetails(Connection conn, String operator, String source, String file) {
        int fileCount = 1;
        File file1 = null;
        try {
            String foldrName = getFilePath(conn, "Sql_Query_Folder") + "/" + operator.toLowerCase() + "/"; //
            foldrName += source + "/";
            String fileNameInput1 = foldrName + file + ".sql";
            try {
                logger.info("SQL " + fileNameInput1);
                file1 = new File(fileNameInput1);

                // BufferedReader reader = new BufferedReader(new FileReader(fileNameInput1));
                // int lines = 0;
                // while (reader.readLine() != null) {
                // lines++;
                // }
                // reader.close();
                File myObj = new File(fileNameInput1);
                if (myObj.delete())          ;

                // try (Stream<String> lines = Files.lines(file1.toPath())) {
                // fileCount = (int) lines.count();
                // logger.info("File Count of Sql File: " + fileCount);
                // }
            } catch (Exception e) {
                logger.error("File not   exist : " + e);
            }

        } catch (Exception e) {
            logger.error("Err0r : " + e);
        }
        return fileCount;
    }

    private static HashMap getValidTac(Connection conn) {
        HashMap<String, Date> validTacMap = new HashMap<>();
        String timePeriod = getSystemConfigDetailsByTag(conn, "IS_USED_EXTENDED_DAYS");
        logger.info("Time Period in days  : ----" + timePeriod);
        Date allocation_date = null;
        String query = "select  DATE_ADD(allocation_date, INTERVAL " + timePeriod + " DAY  )  as  allocation_date from " + appdbName + ".mobile_device_repository ";
        logger.info("Query ----" + query);
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query);) {
            while (rs.next()) {
                validTacMap.put(rs.getString("device_id"), rs.getDate("allocation_date"));
            }
        } catch (Exception e) {
            logger.error(query + "Err0r While featching Set of from mobile_device_repository " + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        }
        logger.info("Query completed");
        return validTacMap;
    }

//    private static HashMap getIsUsedDevice(Connection conn, String tac) {
//        String timePeriod = getSystemConfigDetailsByTag(conn, "IS_USED_EXTENDED_DAYS");
//        logger.info("Time Period in days  : ----" + timePeriod);
//        Date allocation_date = null;
//        String response = "false";
//        String query = "select  DATE_ADD(allocation_date, INTERVAL " + timePeriod + " DAY  )  as  allocation_date from " + appdbName + ".mobile_device_repository where device_id ='" + tac + "'";
//        logger.info("query  : ----" + query);
//        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query);) {
//            while (rs.next()) {
//                allocation_date = rs.getDate("allocation_date");
//            }
//            if (allocation_date == null || allocation_date.equals(null)) {
//                logger.info("allocation_date is null returning false  ");
//                response = "false";
//            } else {
//                logger.info("allocation_date after  " + timePeriod + " days : ----" + allocation_date);
//                if (allocation_date.before(new Date())) {
//                    response = "false";
//                } else {
//                    response = "true";
//                }
//            }
//            logger.debug("response : ----" + response);
//        } catch (Exception e) {
//            logger.error(e.getLocalizedMessage() + "" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
//        }
//        return response;
//    }
    public static String getFilePath(Connection conn, String tag_type) {
        String file_path = "";
        String query = null;
        ResultSet rs = null;
        Statement stmt = null;
        try {
            query = "select value from " + appdbName + ".sys_param where tag='" + tag_type + "'";
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

    public static int insertModuleAudit(Connection conn, String featureName, String processName) {
        int generatedKey = 0;
        String query = " insert into  " + auddbName + ".modules_audit_trail " + "(status_code,status,feature_name,"
                + "info, count2,action,"
                + "server_name,execution_time,module_name,failure_count) "
                + "values('201','Initial', '" + featureName + "', '" + processName + "' ,'0','Insert', '"
                + serverName + "','0','ETL','0')";
        logger.info(query);
        try (PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            //   logger.debug("Going to execute  ");
            ps.execute();
            //   logger.debug("Going for getGenerated key  ");
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                generatedKey = rs.getInt(1);
            }
            logger.info("Inserted record's ID: " + generatedKey);
            rs.close();
        } catch (Exception e) {
            logger.error("Failed  " + e);
        }
        return generatedKey;
    }

    public static void updateModuleAudit(Connection conn, int statusCode, String status, String errorMessage, int id, long executionStartTime, long numberOfRecord, int failureCount) {
        long executionFinishTiime = (new Date().getTime()) - executionStartTime;
        String query = null;
        try (Statement stmt = conn.createStatement()) {
            query = "update   " + auddbName + ".modules_audit_trail set status_code='" + statusCode + "',status='" + status + "',error_message='" + errorMessage + "', count='" + numberOfRecord + "',"
                    + "action='update',execution_time='" + executionFinishTiime + "',failure_count='" + failureCount + "' ,modified_on=CURRENT_TIMESTAMP where  id = " + id;
            logger.info(query);
            stmt.executeUpdate(query);
        } catch (Exception e) {
            logger.error(query + "Failed  " + e);
        }
    }

}
