/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.glocks.service;

import com.gl.Rule_engine_Old.RuleEngineApplication;
import com.glocks.parser.CEIRFeatureFileFunctions;
import com.glocks.parser.HexFileReader;
import java.io.BufferedWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LuhnCheckService {

//     public static void main(String[] args) {
     static Logger logger = LogManager.getLogger(LuhnCheckService.class);

     public static void main(String args[]) {

          Connection conn = null;
          logger.info(" LuhnCheckService.class");
          conn = (Connection) new com.glocks.db.MySQLConnection().getConnection();
          luhnCheckServcImpl(conn);
          System.exit(0);
     }

     static void luhnCheckServcImpl(Connection conn) {
          Statement stmt = null;
          String raw_query = "select imei from device_usage_db  where imei is not null  ";
          ResultSet resul5 = null;
          try {
               logger.info(" " + raw_query);
               stmt = conn.createStatement();
               resul5 = stmt.executeQuery(raw_query);
               String imei = null;
               int TotalIMEI = 0;
               int TotalNull = 0;
               int totalIMEIwith15digit = 0;
               int totalIMEIwith16digit = 0;
               int totalIMEILuhnFailed = 0;
               int totalIMEI0End = 0;
               int totallenth16IMEI0End = 0;
               int totalIMEIlengthNot1516 = 0;
               BufferedWriter bw = null;
               try {
                    while (resul5.next()) {

                         TotalIMEI++;
                         imei = resul5.getString(1);
                         if (imei != null || imei.equals("null")) {

                              logger.info("imei " + imei);
                              if (imei.length() == 15) {
                                   totalIMEIwith15digit++;

                                   String[] my_arr = {"IMEI_LUHN_CHECK", "1", "CDR", imei, "4", "5", "6", "7", "8", "IMEI"};
                                   logger.info("my_arr   " + my_arr);
                                   String output = RuleEngineApplication.startRuleEngine(my_arr, conn, bw);

                                   if (output.equalsIgnoreCase("No")) {
                                        totalIMEILuhnFailed++;
                                   }

                                   if (imei.endsWith("0")) {
                                        totalIMEI0End++;
                                   }

                              } else if (imei.length() == 16) {
                                   totalIMEIwith16digit++;
                                   if (imei.endsWith("0")) {
                                        totallenth16IMEI0End++;
                                   }
                              } else {
                                   totalIMEIlengthNot1516++;
                              }

                         } else {
                              TotalNull++;
                         }
                    }

                    logger.info("LuhnCheckServiceEndResult ");
                    logger.info("TotalIMEI " + TotalIMEI);
                    logger.info("totalIMEIwith15digit " + totalIMEIwith15digit);
                    logger.info("totalIMEI with 16 digit " + totalIMEIwith16digit);
                    logger.info("Length 15 with Luhn Failed " + totalIMEILuhnFailed);
                    logger.info(" Length 15 with Ending 0 :" + totalIMEI0End);
                    logger.info(" Length 16 with Ending 0 :" + totallenth16IMEI0End);
                    logger.info("total IMEI length Not1516 " + totalIMEIlengthNot1516);

               } catch (Exception e) {
                    logger.info("   " + e);
               }
               stmt.close();
          } catch (Exception e) {
               logger.error("  " + e);
          } finally {
               try {
                    resul5.close();
                    stmt.close();
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
