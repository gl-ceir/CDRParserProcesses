/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class DvcUsageDb2 {

     static Logger logger = LogManager.getLogger(LuhnCheckService.class);

     public static void main(String args[]) {

          Connection conn = null;
          logger.info(" DvcUsageDb2.class");
         //    conn = (Connection) new com.glocks.db.MySQLConnection().getConnection();
          ImeiDetailsReportServcImpl(conn);
          System.exit(0);
     }

     static void ImeiDetailsReportServcImpl(Connection conn) {

          Statement stmt = null;
          Statement stmt1 = null;
//          String raw_query = "select * from device_usage_db where length(IMEI) in(14,15,16)   and id  >=  " + start + "   and id <  " + end + "  and    msisdn not in ( select msisdn from device_usage_db_01 )  ";
//          String raw_query = "     select  * from device_usage_db where  length(IMEI) in(14,15,16)    and IMEI not in (select imei from device_usage_db_01 ) and msisdn like '855%'  ";
          String raw_query = " select * from device_usage_db where length(IMEI) in(14,15,16) and msisdn like '855%'  and concat(substr(IMEI,1,14) ,MSISDN) not in ( select concat(substr(IMEI,1,14) ,MSISDN) from device_usage_db_01 )    ";
          ResultSet rs = null;
          try {
               logger.info(" " + raw_query);
               stmt = conn.createStatement();
               stmt1 = conn.createStatement();
               rs = stmt.executeQuery(raw_query);
               BufferedWriter bw = null;
               String my_query = null;
               int output = 0;
               int count = 0;

               while (rs.next()) {

                    output = checkDeviceUsageDB(conn, rs.getString("imei").substring(0, 14), rs.getString("msisdn"));

                    if (output == 0) {  // not exist    // insert usageDb

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
                         try{
                         stmt1.executeUpdate(my_query);
                         conn.commit();
                         }catch(Exception e){
                               logger .debug("usages error" + e);
                         }

                    }
                    if (output == 1) {  // exist with same msisdn
                    }
                    if (output == 2) {  // exist but different msisdn   // insertDuplicate

                         my_query = "insert into device_duplicate_db_01 (imei,msisdn,imsi,create_filename,update_filename,"
                                 + "updated_on,created_on,system_type,failed_rule_id,failed_rule_name,tac,period,action "
                                 + " , mobile_operator , record_type , failed_rule_date,  modified_on ,record_time  ,tax_paid,  imei_index) "
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
                                 + "'" + rs.getString("TAX_PAID") + "' , "
                                 + "'" + rs.getString("imei").substring(0, 14) + "'  "
                                 + ")";
                         logger.info("" + my_query);
                         try{
                         stmt1.executeUpdate(my_query);
                         conn.commit();
                         }catch(Exception e ){
                         logger .debug("duplicate error" + e);
                         }

                    }

                    logger.info(" count " + count++);
               }
          } catch (Exception e) {
               logger.error("   " + e);
          } finally {
               try {
                    rs.close();
                    stmt.close();
                    conn.close();
               } catch (SQLException ex) {
                  //  java.util.logging.LogManager.getLogger(DvcUsageDb2.class.getName()).log(Level.SEVERE, null, ex);
            	   org.apache.logging.log4j.LogManager.getLogger(CEIRFeatureFileFunctions.class.getName());
               }
          }

     }

     static int checkDeviceUsageDB(Connection conn, String imeiIndex, String msisdn) {
          String query = null;
          ResultSet rs1 = null;
          Statement stmt = null;
          int status = 0;                                                         // imei not found
          try {
               query = "select * from device_usage_db_01 where imei_index ='" + imeiIndex + "'";
               logger.debug("   " + query);
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

}
