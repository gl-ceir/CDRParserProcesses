package com.glocks.http;

import com.glocks.parser.service.WithdrawnTac;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpURLConnectionExample {

    static Logger logger = LogManager.getLogger(HttpURLConnectionExample.class);
    private static final String POST_PARAMS = "";

    public static String sendGET(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        StringBuffer response = new StringBuffer();

        // System.out.println("GET Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            logger.debug(response.toString());
        } else {
            logger.warn("GET request not worked");
        }

        return response.toString();

    }

    public static String sendPOST(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("POST");

        // For POST only - START
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(POST_PARAMS.getBytes());
        os.flush();
        os.close();
        // For POST only - END

        StringBuffer response = new StringBuffer();
        int responseCode = con.getResponseCode();
        logger.debug("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            logger.debug(response.toString());

        } else {
            logger.warn("POST request not worked");
        }

        return response.toString();
    }

    public static String sendPOST(String url, String body) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");

        con.setRequestMethod("POST");

        // For POST only - START
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        byte[] input = body.getBytes("utf-8");
        os.write(input, 0, input.length);
        os.flush();
        os.close();
        // For POST only - END

        StringBuffer response = new StringBuffer();
        int responseCode = con.getResponseCode();
        logger.debug("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            logger.debug(response.toString());

        } else {
            logger.debug("POST request not worked");
        }

        return response.toString();
    }

 
}
