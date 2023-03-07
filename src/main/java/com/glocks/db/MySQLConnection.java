package com.glocks.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Objects;
import com.glocks.constants.PropertyReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
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
           final String JDBC_DRIVER = propertyReader.getConfigPropValue("jdbc_driver").trim();
            final String DB_URL = propertyReader.getConfigPropValue("db_url").trim();
            final String USER = propertyReader.getConfigPropValue("dbUsername").trim();
           
            final String passwordDecryptor = propertyReader.getPropValue("password_decryptor").trim();
            logger.debug("passwordDecryptor ." + passwordDecryptor);
            final String PASS = getPassword(passwordDecryptor);
            logger.debug(JDBC_DRIVER + " :: " + DB_URL + " :: " + USER + " :: " + PASS);
            logger.debug("Connnection  Init " + java.time.LocalDateTime.now());
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            conn.setAutoCommit(false);
            logger.info("Connnection created successfully " + conn + " .. " + java.time.LocalDateTime.now());
            return conn;
        } catch (Exception e) {
            logger.error(" Error : : " + e + " :  " +  e.getLocalizedMessage());
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error(" SQLException : " + ex + " :  " + java.time.LocalDateTime.now());
            }
            System.exit(0);
            return null;
        }
    }
    
        String getPassword(String passwordDecryptor) {

        String line = null;
        String response = null;
        try {
            String cmd = "java -jar " + passwordDecryptor + "  spring.datasource.password";
            logger.debug("cmd to  run::" + cmd);
            Process pro = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(pro.getInputStream()));
            while ((line = in.readLine()) != null) {
                logger.debug("Response::" + line);
                response = line;
            }
            return response;
        } catch (Exception e) {
            logger.info("Error  getPassword " + e);
            e.printStackTrace();
            return null;
        }
    }
}
