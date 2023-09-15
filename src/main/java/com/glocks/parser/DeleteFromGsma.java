
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.glocks.parser;

import com.glocks.constants.PropertyReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author maverick
 */
public class DeleteFromGsma {

     static Logger logger = LogManager.getLogger(DeleteFromBlackList.class);
     static StackTraceElement l = new Exception().getStackTrace()[0];
     public static PropertyReader propertyReader;

     public static void main(String args[]) {
          Connection conn = null;
         //   conn = (Connection) new com.glocks.db.MySQLConnection().getConnection();

          try {
               deleteProcess(conn);
          } catch (Exception e) {
               try {
                    conn.rollback();
               } catch (SQLException ex) {
                    logger.error("" + e);
               }
          } finally {
               try {
                    conn.commit();
                    conn.close();
               } catch (SQLException ex) {
                    logger.error(ex);
               }

               System.exit(0);
          }
     }

     private static void deleteProcess(Connection conn) {
          Statement stmt = null;
//          String raw_query = "delete  from black_list   where CREATED_ON <  ( current_timestamp  - ( select value  from system_configuration_db where tag = 'GSMA_BL_MAX_RETENTION_PERIOD_DAYS_IN_DB'  )   ) ";
          String raw_query = "delete  from gsma_invalid_tac_db   where CREATED_ON <  ( current_timestamp  - ( select value  from system_configuration_db where tag = 'GSMA_TAC_MAX_RETENTION_PERIOD_DAYS_IN_DB'  )   ) ";
          try {
               logger.info(" " + raw_query);
               stmt = conn.createStatement();
               stmt.executeUpdate(raw_query);
          } catch (Exception e) {
               logger.warn("  " + e);
          } finally {
               try {
                    stmt.close();
               } catch (SQLException ex) {
               }
          }

     }
}
