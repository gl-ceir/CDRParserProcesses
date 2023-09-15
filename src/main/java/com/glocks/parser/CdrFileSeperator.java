///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.glocks.parser;
//
//import com.glocks.files.FileList;
//import static com.glocks.parser.CdrParserProcess.CdrParserProces;
//import static com.glocks.parser.CdrParserProcess.logger;
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.FileReader;
//import java.io.OutputStreamWriter;
//import java.nio.file.Files;
//import java.sql.Connection;
//import java.sql.ResultSet;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.stream.Stream;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
///**
// *
// * @author maverick
// */
//public class CdrFileSeperator {
//
//     static Logger logger = LogManager.getLogger(CdrFileSeperator.class);
//     static StackTraceElement l = new Exception().getStackTrace()[0];
//
//     public static void main(String args[]) {
//          Connection conn = null;
//          logger.info(" CdrFileSeperator.class");
//          conn = (Connection) new com.glocks.db.MySQLConnection().getConnection();
////          String operator = args[0];
//          String filePath = null;
//          if (args[0] == null) {
//               logger.error("Enter the Correct File Path");
//          } else {
//               filePath = args[0].trim();
//          }
//          if (!filePath.endsWith("/")) {
//               filePath += "/";
//          }
//          CdrParserProces(conn, filePath);
//     }
//
//     public static void CdrParserProces(Connection conn, String filePath) {
//          logger.info(" FilePath :" + filePath);
//          String source = null;
//          String operator = null;
//          if (filePath != null) {
//               String[] arrOfStr = filePath.split("/", 0);
//               int val = 0;
//               for (int i = (arrOfStr.length - 1); i >= 0; i--) {
//                    if (val == 1) {
//                         source = arrOfStr[i];
//                    }
//                    if (val == 2) {
//                         operator = arrOfStr[i].toUpperCase();
//                    }
//                    val++;
//               }
//          }
//          String fileName = new FileList().readOldestOneFile(filePath);
//          logger.info(" FilePath :" + filePath + "; FileName:" + fileName + ";source : " + source + " ; Operator : " + operator);
//          getFileSeperatorStart(operator, conn, filePath, source, fileName);
//          System.exit(0);
//     }
//
//     private static void getFileSeperatorStart(String operator, Connection conn, String filePath, String source, String fileName) {
//
//          File file = null;
//          String line = null;
//          String[] data = null;
//          BufferedReader br = null;
//          FileReader fr = null;
//          try {
//               BufferedWriter successBw = successFileWriter(conn, filePath, fileName);
//               BufferedWriter failBw = failFileWriter(conn, filePath, fileName);
//               BufferedWriter errorBw = errorFileWriter(conn, filePath, fileName);
//               file = new File(filePath + fileName);
//               int fileCount = 0;
//               try (Stream<String> lines = Files.lines(file.toPath())) {
//                    fileCount = (int) lines.count();
//                    logger.info("File Count: " + fileCount);
//               } catch (Exception e) {
//                    logger.warn("" + e);
//               }
//               fr = new FileReader(file);
//               br = new BufferedReader(fr);
////               HashMap<String, String> device_info = new HashMap<String, String>();
//               Statement stmt2 = null;
//               stmt2 = conn.createStatement();
//               ResultSet result1 = null;
//               String res = null;
//               String headers = br.readLine();
//
//               failBw.write(headers);
//               successBw.newLine();
//               successBw.write(headers);
//               failBw.newLine();
//               errorBw.write(headers);
//               errorBw.newLine();
//
//               while ((line = br.readLine()) != null) {
//                    data = line.split(",", -1);
////                    device_info.put("IMEI", data[0].trim());
////                    device_info.put("IMSI", data[1].trim());
////                    device_info.put("MSISDN", (data[2].trim().startsWith("19") ? data[2].trim().substring(2) : data[2]).trim());
////                    device_info.put("record_type", data[3].trim());
////                    device_info.put("system_type", data[4].trim());
//                    logger.info(" :: " + line);
//                    if ((data[0].trim() == null || data[0].trim() == "") && (data[2].trim() == null || data[2].trim() == "")) {
//                         logger.info("Error File ");
//                         errorBw.write(line);
//                         errorBw.newLine();
//                    } else if ((data[0].trim() == null || data[0].trim() == "") || (data[2].trim() == null || data[2].trim() == "")) {
//                         logger.info(" Any one is Missing  ");
//                         String type = null;
//                         String typeVal = null;
//                         if (data[0].trim() == null || data[0].trim() == "") {
//                              type = "MSISDN";
//                              typeVal = data[2].trim();
//                         }
//                         if (data[2].trim() == null || data[2].trim() == "") {
//                              type = "IMEI";
//                              typeVal = data[0].trim();
//                         }
//
//                         String qry = " select sum (cnt) from  (select count  (" + type + ") as cnt  from device_usage_db where " + type + " = '" + typeVal + "'  "
//                                 + " union select count  (" + type + ")  as cnt from device_duplicate_db where " + type + " = '" + typeVal + "'  ) a  ";
//
//                         logger.info(qry);
//                         result1 = stmt2.executeQuery(qry);
//                         int res1 = 0;
//                         try {
//                              while (result1.next()) {
//                                   res1 = result1.getInt(1);
//                              }
//                         } catch (Exception e) {
//                              logger.error("" + e);
//                         }
//                         if (res1 == 0) {
//                              logger.info("Fail File  11 ");
//                              failBw.write(line);
//                              failBw.newLine();
//                         } else {
//                              logger.info("Success File  11 ");
//                              successBw.write(line);
//                              successBw.newLine();
//                         }
//                    } else {
//
//                         String qry = " select sum (cnt) from  (select count  (imei) as cnt  from device_usage_db where imei = '" + data[0] + "' and MSISDN = '" + data[2] + "' "
//                                 + " union select count  (imei)  as cnt from device_duplicate_db where imei = '" + data[0] + "' and MSISDN = '" + data[2] + "' ) a  ";
//
//                         logger.info(qry);
//                         result1 = stmt2.executeQuery(qry);
//                         int res1 = 0;
//                         try {
//                              while (result1.next()) {
//                                   res1 = result1.getInt(1);
//                              }
//                         } catch (Exception e) {
//                              logger.error("" + e);
//                         }
//                         if (res1 == 0) {
//                              logger.info("fail File  22 ");
//                              failBw.write(line);
//                              failBw.newLine();
//                         } else {
//                              logger.info("Success File  22 ");
//                              successBw.write(line);
//                              successBw.newLine();
//                         }
//                    }
//               }
//               br.close();
//               successBw.close();
//               failBw.close();
//               errorBw.close();
//               conn.commit();
//               conn.close();
//          } catch (Exception e) {
//               logger.info(e);
//          }
//     }
//
//     private static BufferedWriter errorFileWriter(Connection conn, String filePth, String file) {
//          BufferedWriter bw1 = null;
//          try {
//               String fileNameInput1 = filePth + file + "_Error";
//               logger.info("Error file NAME   " + fileNameInput1);
//               File fout1 = new File(fileNameInput1);
//               FileOutputStream fos1 = new FileOutputStream(fout1, true);
//               bw1 = new BufferedWriter(new OutputStreamWriter(fos1));
//          } catch (Exception e) {
//               logger.error("error ex " + e);
//          }
//          return bw1;
//     }
//
//     private static BufferedWriter successFileWriter(Connection conn, String filePth, String file) {
//          BufferedWriter bw1 = null;
//          try {
//               String fileNameInput1 = filePth + file + "_Success";
//               logger.info("Success file NAME   " + fileNameInput1);
//               File fout1 = new File(fileNameInput1);
//               FileOutputStream fos1 = new FileOutputStream(fout1, true);
//               bw1 = new BufferedWriter(new OutputStreamWriter(fos1));
//          } catch (Exception e) {
//               logger.error("Succeess ex " + e);
//          }
//          return bw1;
//     }
//
//     private static BufferedWriter failFileWriter(Connection conn, String filePth, String file) {
//          BufferedWriter bw1 = null;
//          try {
//               String fileNameInput1 = filePth + file + "_Fail";
//               logger.info("Fail file NAME   " + fileNameInput1);
//               File fout1 = new File(fileNameInput1);
//               FileOutputStream fos1 = new FileOutputStream(fout1, true);
//               bw1 = new BufferedWriter(new OutputStreamWriter(fos1));
//          } catch (Exception e) {
//               logger.error("fail ex " + e);
//          }
//          return bw1;
//     }
//
//}
