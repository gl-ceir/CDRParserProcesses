/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.glocks.parser;

import com.glocks.constants.PropertyReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author user
 */
public class ErrorFileGenrator {
     
    static StackTraceElement l = new Exception().getStackTrace()[0];
    public static PropertyReader propertyReader;

    static Logger logger = LogManager.getLogger(ErrorFileGenrator.class);

    public void gotoErrorFile(Connection conn, String txn_id, String errorString) {
        try {
            logger.info("Error File init");
            Statement stmt = conn.createStatement();
            String qury = "select value from  system_configuration_db  where tag  = 'system_error_filepath' ";
            ResultSet resultmsdn = null;
            resultmsdn = stmt.executeQuery(qury);
            String errorPath = null;
            try {
                while (resultmsdn.next()) {
                    errorPath = resultmsdn.getString(1);
                }
            } catch (Exception e) {
                logger.error("Error...errorPath." + e);
            }
            logger.debug("fileString is " + errorString);
            try {
                File file = new File(errorPath + txn_id);
                file.mkdir();
                String fileNameInput = errorPath + txn_id + "/" + txn_id + "_error.csv";       // 
                String fileName = txn_id + "_error.csv";
                String filePath = errorPath + txn_id ; 
                logger.info("fileNameInput...." + fileNameInput);
                File fout = new File(fileNameInput);
                FileOutputStream fos = new FileOutputStream(fout, true);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

                bw.write(errorString);
                bw.newLine();
                bw.close();

                redudencyApiConnect(fileName, txn_id,  errorPath + txn_id + "/");
            } catch (Exception e) {
                logger.error("exception at File..." + e);
            }

        } catch (Exception e) {
            logger.error("Exception ..." + e);
        }
    }

    public void gotoErrorFilewithList(String errorPath, String txn_id, ArrayList<String> fileLines) {
        try {
            File file = new File(errorPath + txn_id);
            file.mkdir();
            logger.info(" mkdir done ");
            String fileNameInput = errorPath + txn_id + "/" + txn_id + "_error.csv";
            logger.info(" fileNameInput Erorr file name  " + fileNameInput);
            File fout = new File(fileNameInput);
            FileOutputStream fos = new FileOutputStream(fout, true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            for (String val : fileLines) {
                bw.write(val);
                bw.newLine();
            }
            bw.close();
            redudencyApiConnect(txn_id + "_error.csv", txn_id,  errorPath + txn_id + "/");
        } catch (Exception e) {
            logger.error("Error + gotoErrorFilewithList " + e);
        }
    }

    public void redudencyApiConnect(String fileName, String txn_id, String fileNameInput) {

        String tag = "http://$LOCAL_IP:9502/CEIR/uploadedFile/save";
        logger.info("  uploadedFile tag  " + tag);
        String aa = "";
       propertyReader = new PropertyReader();
        try {
            aa = propertyReader.getConfigPropValue("localIp").trim();
            logger.info("  .. " + aa);
        } catch (Exception ex) {
            logger.info("       " + ex);
        }
//          aa = "172.24.2.57";
        tag = tag.replace("$LOCAL_IP", aa);
        String serverId = aa.contains("57") ? "1" : "2";
        
        String responseBody = "{\n"
                + "\"fileName\": \"" + fileName + "\",\n"
                + "\"txnId\": \"" + txn_id + "\",\n"
                + "\"filePath\": \"" + fileNameInput + "\",\n"
                + "\"serverId\": " + serverId + " \n"
                + "}";
        logger.info("  after Replace  " + tag);
        logger.info("responseBody after Replace  " + responseBody);

        HttpApiConnecter(tag, responseBody);

    }

    public void writeErrorMessageInFile(String errorPath, String txn_id, String errorString) {
        try {
            File file = new File(errorPath + txn_id);
            file.mkdir();
            String fileNameInput = errorPath + txn_id + "/" + txn_id + "_error.csv";       // 
            logger.info("fileNameInput...." + fileNameInput);
            logger.info("errorString...." + errorString);
            File fout = new File(fileNameInput);
            FileOutputStream fos = new FileOutputStream(fout, true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(errorString);
            bw.newLine();
            bw.close();
                 redudencyApiConnect(txn_id + "_error.csv", txn_id, errorPath + txn_id + "/");
        } catch (Exception e) {
            logger.error("exception at File..." + e);
        }

    }

    public void apiConnectionErrorFileReader() {
        try {
            ArrayList<String> coll = new ArrayList<String>();
            String currentDirectory = System.getProperty("user.dir");
            String fileName = currentDirectory + "/conf/apiConnectionTag.txt";
            File file = null;
            String line = null;
            String data[] = null;
            BufferedReader br = null;
            FileReader fr = null;
            file = new File(fileName);
            String tag = null;
            String responseBody = null;
            logger.info("fileNameInput @ apiConnectionErrorFileReader...." + fileName);
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            int a = 0;
            while ((line = br.readLine()) != null) {
                if (line.trim().length() > 0) {
                    if (a == 0) {
                        data = line.split("@", 2);
                        tag = data[0];
                        responseBody = data[1];
                    } else {
                        coll.add(line);
                    }
                    a++;
                }
            }
            br.close();
            fr.close();
            logger.info("--- > " + tag + " :: " + responseBody);
            FileWriter writer = new FileWriter(fileName);
            for (String lineS : coll) {
                writer.write(lineS);
                writer.write("\n");
            }
            writer.close();
            if (tag != null || tag.equals("")) {
                new CEIRFeatureFileFunctions().HttpApiConnecter(tag, responseBody);
            }
        } catch (Exception e) {
            logger.warn("No File Found ");
//               logger.error(" " + e);
        }

    }

    public void apiConnectionErrorFileWriter(String tag, String responseBody) {
        try {
            String currentDirectory = System.getProperty("user.dir");
            String fileNameInput = currentDirectory + "/conf/apiConnectionTag.txt";
            logger.info("fileNameInput @ apiConnectionErrorFileWriter...." + fileNameInput);
            logger.info("-- > " + tag);
            logger.info("-- > " + responseBody);
            File fout = new File(fileNameInput);
            FileOutputStream fos = new FileOutputStream(fout, true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(tag + "@" + responseBody);
            bw.newLine();
            bw.close();
        } catch (Exception x) {
            logger.error(" :: " + x);

        }

    }

    public void HttpApiConnecter(String tag, String responseBody) {
        try {
            URL url = new URL(tag);
            HttpURLConnection hurl = (HttpURLConnection) url.openConnection();
            hurl.setRequestMethod("POST");
            hurl.setDoOutput(true);
            hurl.setRequestProperty("Content-Type", "application/json");
            hurl.setRequestProperty("Accept", "application/json");
            OutputStreamWriter osw = new OutputStreamWriter(hurl.getOutputStream());
            osw.write(responseBody);
            osw.flush();
            osw.close();
            logger.info("DatA Putted");
            hurl.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(hurl.getInputStream()));
            String temp = null;
            StringBuilder sb = new StringBuilder();
            while ((temp = in.readLine()) != null) {
                sb.append(temp).append(" ");
            }
            String result = sb.toString();
            in.close();
            logger.info("OUTPUT result is .." + result);
        } catch (Exception e) {
            logger.error(responseBody + "  " + e);
            new ErrorFileGenrator().apiConnectionErrorFileWriter(tag, responseBody);
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        }
    }

    public HttpURLConnection getHttpConnection(String url, String type) {
        URL uri = null;
        HttpURLConnection con = null;
        try {
            uri = new URL(url);
            con = (HttpURLConnection) uri.openConnection();
            con.setRequestMethod(type); // type: POST, PUT, DELETE, GET
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setConnectTimeout(60000); // 60 secs...
            con.setReadTimeout(60000); // 60 secs
            con.setRequestProperty("Accept-Encoding", "application/json");
            con.setRequestProperty("Content-Type", "application/json");
        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
        }
        return con;
    }

}
