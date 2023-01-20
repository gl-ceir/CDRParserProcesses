package com.glocks.pojo;

import java.io.Serializable;

public class PolicyBreachNotification implements Serializable {

    private static final long serialVersionUID = 1L;

    private String channelType;
    private String message;
    private String subject;
    private String contactNumber;
    private String imei;
    private int retryCount;

    
    
    
    PolicyBreachNotification() {
    }

    public PolicyBreachNotification(String channelType, String message, String subject, String contactNumber, String imei) {

        this.retryCount = 0;
        this.channelType = channelType;
        this.message = message;
        this.subject = subject;
        this.contactNumber = contactNumber;
        this.imei = imei;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PolicyBreachNotification{channelType=").append(channelType);
        sb.append(", message=").append(message);
        sb.append(", subject=").append(subject);
        sb.append(", contactNumber=").append(contactNumber);
        sb.append(", imei=").append(imei);
        sb.append(", retryCount=").append(retryCount);
        sb.append('}');
        return sb.toString();
    }

}
