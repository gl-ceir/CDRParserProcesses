package com.glocks.constants;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;


@Component
@PropertySources({
    @PropertySource(value = {"file:application.properties"}, ignoreResourceNotFound = true),
    @PropertySource(value = {"file:configuration.properties"}, ignoreResourceNotFound = true)
})

public class PropertyReader {

    @Value("${appdbName}")
    public String appdbName;

    @Value("${repdbName}")
    public String repdbName;

    @Value("${auddbName}")
    public String auddbName;

    @Value("${oamdbName}")
    public String oamdbName;
    
    

    private InputStream inputStream;
    Properties prop;
    private static final Logger logger = LogManager.getLogger(PropertyReader.class);

    public String getConfigPropValue(String key) throws IOException {
        prop = loadProperties(System.getenv("APP_HOME") + "/configuration/configuration.properties");
        if (Objects.nonNull(prop)) {
            return prop.getProperty(key);
        } else {
            return null;
        }
    }

    public String getPropValue(String key) throws IOException {
        prop = loadProperties(System.getProperty("user.dir") + "/conf/config.properties");
        if (Objects.nonNull(prop)) {
            return prop.getProperty(key)
                    .replace("${APP_HOME}", System.getenv("APP_HOME"))
                    .replace("${DATA_HOME}", System.getenv("DATA_HOME"));
        } else {
            return null;
        }
    }

    Properties loadProperties(String propFileName) {
        try {
            prop = new Properties();
            inputStream = new FileInputStream(propFileName);
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
        } catch (IOException io) {
            logger.error(io.toString(), (Throwable) io);
        }
        return prop;
    }

//    Properties loadProperties() {
//        try {
//            prop = new Properties();
//            String currentDirectory = System.getProperty("user.dir");
//            String propFileName = currentDirectory + "/conf/config.properties";
//            inputStream = new FileInputStream(propFileName);
//            if (inputStream != null) {
//                prop.load(inputStream);
//            } else {
//                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
//            }
//        } catch (IOException io) {
//            logger.error(io.toString(), (Throwable) io);
//            //System.exit(-1);
//        }
//        return prop;
//    }
}

//jdbc_driver=oracle.jdbc.driver.OracleDriver
//db_url=jdbc:jdbc:oracle:thin:@172.24.1.96:1521/dbdmcrac
//username=CEIRCONFIG
//password=CEIRCONFIG

