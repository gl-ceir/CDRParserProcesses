///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.glocks.service;
//
//import com.glocks.parser.CEIRFeatureFileFunctions;
//import com.glocks.util.Util;
//import java.sql.Connection;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.logging.Level;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
///**
// *
// * @author maverick
// */
//public class GsmaInvalidInsert2 {
//
//     static Logger logger = LogManager.getLogger(GsmaInvalidUpdate.class);
//
//     public static void main(String args[]) {
//
//          Connection conn = null;
//          logger.info(" GsmaInvalidUpdate.class");
//          conn = (Connection) new com.glocks.db.MySQLConnection().getConnection();
//          GsmaInvalidUpdateServcImpl(conn);
//          System.exit(0);
//     }
//
//     private static void GsmaInvalidUpdateServcImpl(Connection conn) {
//
//          Statement stmt = null;
//          Statement stmt1 = null;
//          Statement stmt2 = null;
//          String raw_query = "  select distinct tac  from device_usage_db  ";
//          ResultSet rs = null;
//          ResultSet rs1 = null;
//          try {
//               List<String> gsmaTac = new LinkedList<>();
//               logger.info(" " + raw_query);
//               stmt = conn.createStatement();
//               stmt1 = conn.createStatement();
//               stmt2 = conn.createStatement();
//               rs = stmt.executeQuery(raw_query);
//               String qry = null;
//               String dateFunction = Util.defaultNowDate();
//               while (rs.next()) {
//                    gsmaTac.add(rs.getString("tac"));
//               }
//               rs.close();
//               logger.info(" dvcUsage Finish");
//               rs1 = stmt2.executeQuery("select tac from gsma_invalid_tac_db union select device_id from gsma_tac_db");
//               while (rs1.next()) {
//                    gsmaTac.remove(rs1.getString(1));
//               }
//               logger.info(" gsmaTacz Finish  with  length " + gsmaTac.size() );
//
//               for (String tacs : gsmaTac) {
//                    logger.info(" InvTac " + tacs);
//                    qry = "  insert into gsma_invalid_tac_db "
//                            + "( tac  , created_on , modified_on  )"
//                            + " values ( '" + tacs + "'  , " + dateFunction + " , " + dateFunction + "       )   ";
//                    logger.info("" + qry);
//                    stmt1.executeUpdate(qry);
//                    conn.commit();
//               }
//
//          } catch (Exception e) {
//               logger.error("   " + e);
//          } finally {
//               try {
//                    rs.close();
//                    rs1.close();
//                    stmt.close();
//                    stmt1.close();
//                    conn.close();
//               } catch (SQLException ex) {
//				/*
//				 * java.util.logging.LogManager.getLogger(GsmaInvalidUpdate.class.getName()).log
//				 * (Level.SEVERE, null, ex);
//				 */
//            	   org.apache.logging.log4j.LogManager.getLogger(CEIRFeatureFileFunctions.class.getName());
//               }
//          }
//
//     }
//
//}
