package com.glocks.db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Objects;
import com.glocks.constants.PropertyReader;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MySQLConnection {

     Logger logger = LogManager.getLogger(MySQLConnection.class);

     public static PropertyReader propertyReader;

     public Connection getConnection() {
          if (Objects.isNull(propertyReader)) {
               propertyReader = new PropertyReader();
          }
          Connection conn = null;
          try {
               final String JDBC_DRIVER = propertyReader.getPropValue("jdbc_driver").trim();
               final String DB_URL =propertyReader.getPropValue("db_url").trim();
               final String USER = propertyReader.getPropValue("username").trim();
               final String PASS = propertyReader.getPropValue("password").trim();
               logger.debug(JDBC_DRIVER + " :: " + DB_URL + " :: " + USER + " :: " + PASS);
               logger.debug("Connnection  Init " + java.time.LocalDateTime.now());
               Class.forName(JDBC_DRIVER);
               conn = DriverManager.getConnection(DB_URL, USER, PASS);
               conn.setAutoCommit(false);
               logger.info("Connnection created successfully " + conn + " .. " + java.time.LocalDateTime.now());
               return conn;
          } catch (Exception e) {
               logger.error(" Error : : " + e + " :  " + java.time.LocalDateTime.now() );
               try {
                    conn.close();
               } catch (SQLException ex) {
                    logger.error(" SQLException : " + ex + " :  " + java.time.LocalDateTime.now());
               }
               System.exit(0);
               return null;
          }
     }
}
