/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.glocks.service;

import com.gl.Rule_engine_Old.RuleEngineApplication;
import com.glocks.parser.CEIRFeatureFileFunctions;
import com.glocks.parser.HexFileReader;
import com.glocks.util.Util;
import java.io.BufferedWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author maverick
 */
public class ImeiDetailsReport {

     static Logger logger = LogManager.getLogger(LuhnCheckService.class);

     public static void main(String args[]) {

          Connection conn = null;
          logger.info(" ImeiDetailsReport.class");
         //    conn = (Connection) new com.glocks.db.MySQLConnection().getConnection();
          ImeiDetailsReportServcImpl(conn);
          System.exit(0);
     }

     private static void ImeiDetailsReportServcImpl(Connection conn) {
          List<GsmaTacDb> gsmaTacDb = new LinkedList<>();
          Statement stmt = null;
          Statement stmt1 = null;
          Statement stmt2 = null;
          String raw_query = "select * from device_usage_db  where imei not in( select imei from imei_details_report) and imei is not null  ";
          ResultSet rs = null;
          ResultSet rs1 = null;
          try {

               stmt2 = conn.createStatement();
               String qury = "select * from gsma_tac_db";
               rs1 = stmt2.executeQuery(qury);
               logger.info(" " + qury);

               while (rs1.next()) {

                    gsmaTacDb.add(new GsmaTacDb((rs1.getString("BRAND_NAME") == null ? "" : rs1.getString("BRAND_NAME")), (rs1.getString("MODEL_NAME") == null ? "" : rs1.getString("MODEL_NAME")), (rs1.getString("OPERATING_SYSTEM") == null ? "" : rs1.getString("OPERATING_SYSTEM")),
                            (rs1.getString("EQUIPMENT_TYPE") == null ? "" : rs1.getString("EQUIPMENT_TYPE")), (rs1.getString("DEVICE_ID") == null ? "" : rs1.getString("DEVICE_ID"))));
               }

               stmt = conn.createStatement();
               stmt1 = conn.createStatement();

               rs = stmt.executeQuery(raw_query);
               BufferedWriter bw = null;
               String qry = null;
               String dateFunction = Util.defaultNowDate();
               int btchcount = 0;

               String osName = "";
               String brandName = "";
               String modelName = "";
               String equipType = "";

               logger.info("test");
               logger.info(" " + raw_query);
               while (rs.next()) {
                    String type = rs.getString("imei").contains("\\w+\\.?") ? "Meid" : "Imei";
                    String[] my_arr = {"IMEI_LUHN_CHECK", "1", "CDR", rs.getString("imei"), "4", "5", "6", "7", "8", "IMEI"};
                    String luhnoutput = RuleEngineApplication.startRuleEngine(my_arr, conn, bw);

                    for (GsmaTacDb gsmaTac : gsmaTacDb) {
                         if (gsmaTac.getDevice_id().equals(rs.getString("tac"))) {
                              osName = gsmaTac.getOPERATING_SYSTEM();
                              brandName = gsmaTac.getBrand_name();
                              equipType = gsmaTac.getEQUIPMENT_TYPE();
                              modelName = gsmaTac.getModel_name();
                         }
                    }

                    String cntQury = "  (select count(*) from device_duplicate_db where imei = '" + rs.getString("imei") + "' )  ";
//                    String brandQuy = " ( SELECT BRAND_NAME FROM gsma_tac_db where  device_id = '" + rs.getString("tac") + "' )  ";
//                    String modelQuy = " ( SELECT model_NAME FROM gsma_tac_db where  device_id = '" + rs.getString("tac") + "' )  ";
//                    String osQuy = " ( SELECT OPERATING_SYSTEM FROM gsma_tac_db where  device_id = '" + rs.getString("tac") + "' )  ";
//                    String equipQuy = " ( SELECT EQUIPMENT_TYPE FROM gsma_tac_db where  device_id = '" + rs.getString("tac") + "' )  ";
                    String unMsisdnQry = "( select   msisdn from device_usage_db where imei not in ( select imei from device_duplicate_db )  and imei =  '" + rs.getString("imei") + "'   )  ";
                    String creationdate = "TO_DATE('" + rs.getString("CREATED_ON") + "','YYYY-MM-DD HH24:MI:SS')";

                    qry = "  insert into imei_details_report "
                            + "( tac , type , imei , length , luhnPassStatus , duplicateCount ,"
                            + " source , brand, model , os , equipmement_type ,gsma_valid , "
                            + " unique_msisdn , file_name , creation_date , created_on , modified_on  )"
                            + " values ( '" + rs.getString("tac") + "' ,  '" + type + "' ,  '" + rs.getString("imei") + "', '" + rs.getString("imei").length() + "' , '" + luhnoutput + "' , " + cntQury + ",  "
                            + " '" + rs.getString("MOBILE_OPERATOR") + "' , '" + brandName + "' ,     '" + modelName + "' ,    '" + osName + "' , '" + equipType + "' ,  '" + rs.getString("feature_name") + "' ,  "
                            + "   " + unMsisdnQry + " ,     '" + rs.getString("UPDATE_FILENAME") + "' , " + creationdate + " ,    " + dateFunction + ", " + dateFunction + "    ) ";
                    logger.info("" + qry);

                    stmt1.addBatch(qry);
                    btchcount++;

                    if (btchcount == 10) {
                         stmt1.executeBatch();
                         conn.commit();
                         btchcount = 0;
                    }
               }
               stmt1.executeBatch();
               conn.commit();

          } catch (Exception e) {
               logger.error("   " + e);
          } finally {
               try {
                    rs1.close();
                    rs.close();
                    stmt.close();
                    conn.close();
               } catch (SQLException ex) {
				/*
				 * java.util.logging.LogManager.getLogger(HexFileReader.class.getName()).log(
				 * Level.SEVERE, null, ex);
				 */
            	   org.apache.logging.log4j.LogManager.getLogger(CEIRFeatureFileFunctions.class.getName());
               }
          }

     }

}
