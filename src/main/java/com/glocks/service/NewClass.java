/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.glocks.service;

import static com.glocks.service.GsmaInvalidUpdate.logger;

import com.glocks.parser.CEIRFeatureFileFunctions;
import com.glocks.util.Util;
import java.io.BufferedWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author maverick
 */
public class NewClass {

     public static void main(String args[]) {

          Connection conn = null;
          logger.info(" GsmaInvalidUpdate.class");
          conn = (Connection) new com.glocks.db.MySQLConnection().getConnection();
          GsmaInvalidUpdateServcImpl(conn);
          System.exit(0);
     }

     private static void GsmaInvalidUpdateServcImpl(Connection conn) {

          Statement stmt = null;
          Statement stmt1 = null;
          String qry = "  select imei  from imei_details_report group by imei  having count(imei) > 1 ";
          ResultSet rs = null;
          List<String> dblmei = new LinkedList<>();
          try {

               stmt = conn.createStatement();
               stmt1 = conn.createStatement();
               rs = stmt.executeQuery(qry);
               while (rs.next()) {
                    dblmei.add(rs.getString("imei"));
               }
               logger.info("total dbl Count " + dblmei.size());

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
