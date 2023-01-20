package com.glocks.resttemplate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.glocks.constants.PropertyReader;
import com.glocks.http.HttpURLConnectionExample;
import com.glocks.pojo.HttpResponse;
import com.glocks.pojo.TacApproveRequest;
import com.google.gson.Gson;
public class TacApiConsumer {

    private final static Logger logger = LogManager.getLogger(TacApiConsumer.class);

    PropertyReader propertyReader;
    Gson gson;

    public TacApiConsumer() {
        propertyReader = new PropertyReader();
    gson= new Gson(); 

    }

    public HttpResponse delete(String txnId, Long userId, String userType, int deleteFlag) {

        try {
            String uri = propertyReader.getPropValue("api.tac.delete")
                    + "?txnId=" + txnId + "&"
                    + "userId=" + userId + "&"
                    + "userType=" + userType + "&"
                    + "deleteFlag=" + deleteFlag;   // 2
            logger.info("Request Body : " + uri);
            String response = HttpURLConnectionExample.sendPOST(uri);
            HttpResponse httpResponse = gson.fromJson(response, HttpResponse.class);
            return httpResponse;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public HttpResponse approveReject(String txnId, Integer status) {   //

        try {
             String uri = propertyReader.getPropValue("api.tac.approve_reject");
            TacApproveRequest tacApproveRequest = new TacApproveRequest(txnId, status);

            String body = gson.toJson(tacApproveRequest, TacApproveRequest.class);
            logger.info("Request Body : " + body);

            String response = HttpURLConnectionExample.sendPOST(uri, body);
            logger.info("Response : " + response);
            HttpResponse httpResponse = gson.fromJson(response, HttpResponse.class);
            return httpResponse;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}



