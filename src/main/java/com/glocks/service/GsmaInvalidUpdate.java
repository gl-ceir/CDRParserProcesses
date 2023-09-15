/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.glocks.service;

import com.gl.Rule_engine_Old.RuleEngineApplication;
import com.glocks.parser.CEIRFeatureFileFunctions;
import com.glocks.parser.HexFileReader;
import static com.glocks.service.ImeiDetailsReport.logger;
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
public class GsmaInvalidUpdate {

     static Logger logger = LogManager.getLogger(GsmaInvalidUpdate.class);

     public static void main(String args[]) {

          Connection conn = null;
          logger.info(" GsmaInvalidUpdate.class");
//          conn = (Connection) new com.glocks.db.MySQLConnection().getConnection();
          GsmaInvalidUpdateServcImpl(conn);
          System.exit(0);
     }

     private static void GsmaInvalidUpdateServcImpl(Connection conn) {

          Statement stmt = null;
          Statement stmt1 = null;
          String raw_query = " select tac from device_usage_db where tac not in (select tac from gsma_invalid_tac_db union select device_id from gsma_tac_db )";
          ResultSet rs = null;
          try {
               logger.info(" " + raw_query);
               stmt = conn.createStatement();
               stmt1 = conn.createStatement();
               rs = stmt.executeQuery(raw_query);
               String qry = null;
               String dateFunction = Util.defaultNowDate();
               while (rs.next()) {
                    logger.info(" InvTac " + rs.getString("tac"));
                    qry = "  insert into gsma_invalid_tac_db "
                            + "( tac  , created_on , modified_on  )"
                            + " values ( '" + rs.getString("tac") + "'  , " + dateFunction + " , " + dateFunction + "       )   ";
                    logger.info("" + qry);
                    stmt1.executeUpdate(qry);
                    conn.commit();
               }

          } catch (Exception e) {
               logger.error("   " + e);
          } finally {
               try {
                    rs.close();
                    stmt.close();
                    conn.close();
               } catch (SQLException ex) {
				/*
				 * java.util.logging.LogManager.getLogger(GsmaInvalidUpdate.class.getName()).log
				 * (Level.SEVERE, null, ex);
				 */
            	   org.apache.logging.log4j.LogManager.getLogger(CEIRFeatureFileFunctions.class.getName());
               }
          }

     }

}
