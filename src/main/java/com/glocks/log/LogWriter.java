package com.glocks.log;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogWriter {

//    static String currentDirectory = System.getProperty("user.dir");
     static String logPath = "/u02/ceirdata/BackendProcess/CeirParser" + "/logs/";  //            /home/ceirapp/ceir/ceir_parser/webAction/logs/"; //   /home/ceirapp/ceir_parser/METFONE/logs/";    

     public String getLogPath() {
          return logPath;
     }

     public boolean writeLog(String cdrFileName, String fileSize, String cdrCount, String parserStartTime, String parserEndTime, String cdrStartTime, String cdrEndTime, String successCount, String failCount, String dbInsertCount, String dbSize) {
          boolean result = false;
          PrintWriter pw = null;
          File file = null;
          String log = null;
          String fileName = null;
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
          try {
               fileName = "CEIR_CDR_File_" + sdf.format(Calendar.getInstance().getTime()) + ".log";
               file = new File(logPath);
               if (!file.exists()) {
                    file.mkdir();
               }
               file = new File(logPath + fileName);
               pw = new PrintWriter(new BufferedWriter(new FileWriter(logPath + fileName, true)));
               if (file.length() == 0) {
                    pw.println("Parser_Start_Time,Parser_End_time,File_Name,File_Size_In_MB,CDR_Count,CDR_Start_Time,CDR_End_time,S_Count,F_Count,DB_Insert_Count,DB_Size");
               }
               log = parserStartTime + "," + parserEndTime + "," + cdrFileName + "," + fileSize + "," + cdrCount + "," + cdrStartTime + "," + cdrEndTime + "," + successCount + "," + failCount + "," + dbInsertCount + "," + dbSize;
               //pw.println("\n");
               pw.println(log);
               pw.flush();
          } catch (Exception e) {
               e.printStackTrace();
               result = true;
          } finally {
               try {
                    if (pw != null) {
                         pw.close();
                    }
               } catch (Exception ex) {
               }
          }
          return result;
     }

     public void writeEvents(FileWriter pw, String servedIMEI, String recordType, // Write what is in CDR file (COMMENTTED)   && also writes What rules aplied on CDR File Records  
             String servedIMSI, String servedMSISDN, String systemType, String operator, String file_name,
             String record_time, String type, String rule_id, String rule_name, String status, String time) {
          try {
               pw.write(servedIMEI + "," + recordType + "," + servedIMSI + "," + servedMSISDN + "," + systemType + "," + operator + ","
                       + file_name + "," + record_time + "," + type + "," + rule_id + "," + rule_name + "," + status + "," + time + String.format("%n"));
               pw.flush();
          } catch (Exception e) {
               e.printStackTrace();
          }
     }

     public void writeFeatureEvents(FileWriter pw, String IMEIESNMEID, String DeviceType, // Writes Rules appled on NONCDR records    &&    writes what records are processed on  NON-CDR P2  (Commented)
             String MultipleSIMStatus, String SNofDevice, String Devicelaunchdate, String DeviceStatus, String txn_id,
             String operator, String file_name, String type, String rule_id, String rule_name, String status, String time) {
          try {
               pw.write(IMEIESNMEID + "," + DeviceType + "," + MultipleSIMStatus + "," + SNofDevice + "," + Devicelaunchdate + "," + DeviceStatus + ","
                       + txn_id + "," + operator + "," + file_name + "," + type + "," + rule_id + "," + rule_name + "," + status + "," + time + String.format("%n"));

               pw.flush();
          } catch (Exception e) {
               e.printStackTrace();
          }
     }

}
