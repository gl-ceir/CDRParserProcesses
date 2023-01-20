package com.glocks.util;

import com.glocks.parser.CEIRFeatureFileFunctions;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Util {

   static  Logger logger = LogManager.getLogger(Util.class);

     public static String defaultDate(boolean isOracle) {
          if (isOracle) {
               SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
               String val = sdf.format(new Date());
               String date = "TO_DATE('" + val + "','YYYY-MM-DD HH24:MI:SS')";
               return date;
          } else {
               return "now()";
          }
     }

 
     public static long timeDiff(String created_on, String modified_on) {
          logger.debug(" CDate " + created_on);
          logger.debug(" MDate " + modified_on);
//          SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yy HH.mm.ss");
          SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-DD HH:MM:SS");
          Date d1 = null;
          Date d2 = null;
          long diff = 0L;
          try {
               d1 = format.parse(created_on);
               d2 = format.parse(modified_on);
               diff = d2.getTime() - d1.getTime();
          } catch (Exception e) {
               e.printStackTrace();
          }
          return diff;
     }

     public static String defaultDateNow(boolean isOracle) {
          if (isOracle) {
               SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
               String val = sdf.format(new Date());
              // String date = "TO_DATE('" + val + "','YYYY-MM-DD HH24:MI:SS')"; commented by sharad
               
               return val;

          } else {
               return "now()";
          }
     }

     // yyyy-MM-dd HH:mm:ss.SSS
     public static String defaultNowDate() {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");     //yyyy-MM-dd HH:mm:ss.SSS
          String val = sdf.format(new Date());
         // String date = "TO_DATE('" + val + "','YYYY-MM-DD HH24:MI:SS')"; commented by sharad
          return val;
     }

}




