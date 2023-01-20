package com.glocks.service;

import com.glocks.parser.CEIRFeatureFileFunctions;
import com.glocks.util.Util;
import java.io.BufferedWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author maverick
 */
public class DvcDuplicate2 {

     static Logger logger = LogManager.getLogger(DvcDuplicate2.class);

     public static void main(String args[]) {

          Connection conn = null;
          logger.info(" DvcDuplicate2.class");
          conn = (Connection) new com.glocks.db.MySQLConnection().getConnection();
          ImeiDetailsReportServcImpl(conn);
          System.exit(0);
     }

     private static void ImeiDetailsReportServcImpl(Connection conn) {

          Statement stmt = null;
          Statement stmt1 = null;
          String raw_query = "select * from device_duplicate_db  where length(IMEI) in(14,15,16)  and  msisdn like '855%'    ";
          ResultSet rs = null;
          try {
               logger.info(" " + raw_query);
               stmt = conn.createStatement();
               stmt1 = conn.createStatement();
               rs = stmt.executeQuery(raw_query);
               BufferedWriter bw = null;
               String my_query = null;
               String dateFunction = Util.defaultNowDate();
               int output = 0;
               int usagOutput = 0;
               int count = 0;

               while (rs.next()) {

                    usagOutput = checkUsageDb(conn, rs.getString("imei").substring(0, 14));
                    if (usagOutput == 0) {

                         my_query = "insert into device_usage_db_01 (   imei,msisdn,imsi,create_filename,update_filename,"
                                 + "updated_on,created_on,system_type,failed_rule_id,failed_rule_name,tac,period,action "
                                 + " , mobile_operator , record_type , failed_rule_date,  modified_on ,record_time, FOREGIN_RULE ,tax_paid,  imei_index) "
                                 + "values('" + rs.getString("IMEI") + "',"
                                 + "'" + rs.getString("MSISDN") + "',"
                                 + "'" + rs.getString("IMSI") + "',"
                                 + "'" + rs.getString("create_filename") + "',"
                                 + "'" + rs.getString("update_filename") + "',"
                                 + "  '" + rs.getString("updated_on") + "'   ,"
                                 + "  TO_DATE('" + rs.getString("CREATED_ON") + "','YYYY-MM-DD HH24:MI:SS')   ,"
                                 + "'" + rs.getString("system_type") + "',"
                                 + "'" + rs.getString("failed_rule_id") + "',"
                                 + "'" + rs.getString("failed_rule_name") + "',"
                                 + "'" + rs.getString("tac") + "',"
                                 + "'" + rs.getString("period") + "',"
                                 + "'" + rs.getString("action") + "' , "
                                 + "'" + rs.getString("mobile_operator") + "' , "
                                 + "'" + rs.getString("record_type") + "',"
                                 + "'" + rs.getString("failed_rule_date") + "',"
                                 + "  TO_DATE('" + rs.getString("modified_on") + "','YYYY-MM-DD HH24:MI:SS')  ,"
                                 + "'" + rs.getString("record_time") + "' , "
                                 + "'" + rs.getString("FOREGIN_RULE") + "' , "
                                 + "'" + rs.getString("TAX_PAID") + "' , "
                                 + "'" + rs.getString("imei").substring(0, 14) + "'  "
                                 + ")";
                         logger.info("" + my_query);
                         try {
                              stmt1.executeUpdate(my_query);
                              conn.commit();
                         } catch (Exception e) {
                              logger.debug("usages error" + e);
                         }

                    } else {
                         output = checkDeviceDuplDB(conn, rs.getString("imei").substring(0, 14), rs.getString("msisdn"));
                         if (output == 0) {  // not exist    // insert usageDb

                              my_query = "insert into device_duplicate_db_01 (   imei,msisdn,imsi,create_filename,update_filename,"
                                      + "updated_on,created_on,system_type,failed_rule_id,failed_rule_name,tac,period,action "
                                      + " , mobile_operator , record_type , failed_rule_date,  modified_on ,record_time ,   imei_index) "
                                      + "values('" + rs.getString("IMEI") + "',"
                                      + "'" + rs.getString("MSISDN") + "',"
                                      + "'" + rs.getString("IMSI") + "',"
                                      + "'" + rs.getString("create_filename") + "',"
                                      + "'" + rs.getString("update_filename") + "',"
                                      + "  '" + rs.getString("updated_on") + "'   ,"
                                      + "  TO_DATE('" + rs.getString("CREATED_ON") + "','YYYY-MM-DD HH24:MI:SS')   ,"
                                      + "'" + rs.getString("system_type") + "',"
                                      + "'" + rs.getString("failed_rule_id") + "',"
                                      + "'" + rs.getString("failed_rule_name") + "',"
                                      + "'" + rs.getString("tac") + "',"
                                      + "'" + rs.getString("period") + "',"
                                      + "'" + rs.getString("action") + "' , "
                                      + "'" + rs.getString("mobile_operator") + "' , "
                                      + "'" + rs.getString("record_type") + "',"
                                      + "'" + rs.getString("failed_rule_date") + "',"
                                      + "  TO_DATE('" + rs.getString("modified_on") + "','YYYY-MM-DD HH24:MI:SS')  ,"
                                      + "'" + rs.getString("record_time") + "' , "
                                      //                                 + "'" + rs.getString("TAX_PAID") + "' , "
                                      + "'" + rs.getString("imei").substring(0, 14) + "'  "
                                      + ")";
                              try {
                                   logger.info("" + my_query);
                                   stmt1.executeUpdate(my_query);
                                   conn.commit();
                              } catch (Exception e) {
                                   logger.debug("  error" + e);
                              }
                         }
                         if (output == 1) {  // exist with same msisdn
                         }
                         if (output == 2) {  // exist but different msisdn   // insertDuplicate

                              my_query = "insert into device_duplicate_db_01 (imei,msisdn,imsi,create_filename,update_filename,"
                                      + "updated_on,created_on,system_type,failed_rule_id,failed_rule_name,tac,period,action "
                                      + " , mobile_operator , record_type , failed_rule_date,  modified_on ,record_time  ,   imei_index) "
                                      + "values('" + rs.getString("IMEI") + "',"
                                      + "'" + rs.getString("MSISDN") + "',"
                                      + "'" + rs.getString("IMSI") + "',"
                                      + "'" + rs.getString("create_filename") + "',"
                                      + "'" + rs.getString("update_filename") + "',"
                                      + "  '" + rs.getString("updated_on") + "'   ,"
                                      + "  TO_DATE('" + rs.getString("CREATED_ON") + "','YYYY-MM-DD HH24:MI:SS')   ,"
                                      + "'" + rs.getString("system_type") + "',"
                                      + "'" + rs.getString("failed_rule_id") + "',"
                                      + "'" + rs.getString("failed_rule_name") + "',"
                                      + "'" + rs.getString("tac") + "',"
                                      + "'" + rs.getString("period") + "',"
                                      + "'" + rs.getString("action") + "' , "
                                      + "'" + rs.getString("mobile_operator") + "' , "
                                      + "'" + rs.getString("record_type") + "',"
                                      + "'" + rs.getString("failed_rule_date") + "',"
                                      + "  TO_DATE('" + rs.getString("modified_on") + "','YYYY-MM-DD HH24:MI:SS')  ,"
                                      + "'" + rs.getString("record_time") + "' , "
                                      //                                 + "'" + rs.getString("TAX_PAID") + "' , "
                                      + "'" + rs.getString("imei").substring(0, 14) + "'  "
                                      + ")";
                              try {
                                   logger.info("" + my_query);
                                   stmt1.executeUpdate(my_query);
                                   conn.commit();
                              } catch (Exception e) {
                                   logger.debug("  error" + e);
                              }
                         }

                         logger.info(" count " + count++);

                    }
               }

          } catch (Exception e) {
               logger.error("   " + e);
          } finally {
               try {
                    rs.close();
                    stmt.close();
                    conn.close();
               } catch (SQLException ex) {
            	   org.apache.logging.log4j.LogManager.getLogger(CEIRFeatureFileFunctions.class.getName());
            	   //  java.util.logging.LogManager.getLogger(DvcDuplicate2.class.getName()).log(Level.SEVERE, null, ex);
               
               }
               
          }

     }

     private static int checkDeviceDuplDB(Connection conn, String imeiIndex, String msisdn) {
          String query = null;
          ResultSet rs1 = null;
          Statement stmt = null;
          int status = 0;                                                         // imei not found
          try {
               query = "select * from device_duplicate_db_01 where imei_index ='" + imeiIndex + "'";
               logger.debug("dvc db" + query);
               stmt = conn.createStatement();
               rs1 = stmt.executeQuery(query);
               while (rs1.next()) {
                    if (rs1.getString("msisdn").equalsIgnoreCase(msisdn)) {     // imei found with same msisdn 
                         status = 1;
                    } else {
                         status = 2;                                                 // imei found with different msisdn
                    }
               }
          } catch (Exception e) {
               logger.error("" + e);
          } finally {
               try {
                    rs1.close();
                    stmt.close();
               } catch (SQLException e) {
                    logger.error("" + e);
               }
          }
          return status;
     }

     private static int checkUsageDb(Connection conn, String imeiIndex) {
          String query = null;
          ResultSet rs1 = null;
          Statement stmt = null;
          int status = 0;                                                         // imei not found
          try {
               query = "select * from device_usage_db_01 where imei_index ='" + imeiIndex + "'";
               logger.debug("dvc db" + query);
               stmt = conn.createStatement();
               rs1 = stmt.executeQuery(query);
               while (rs1.next()) {
                    status = 1;
               }
          } catch (Exception e) {
               logger.error("" + e);
          } finally {
               try {
                    rs1.close();
                    stmt.close();
               } catch (SQLException e) {
                    logger.error("" + e);
               }
          }
          return status;

     }

}
