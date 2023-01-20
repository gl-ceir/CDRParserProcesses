/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.glocks.service;

/**
 *
 * @author maverick
 */
public class GsmaTacDb {
     
    	 
	 
	private String brand_name;
	private String model_name;
	private String OPERATING_SYSTEM;
	private String EQUIPMENT_TYPE;
	private String device_id;

     public String getBrand_name() {
          return brand_name;
     }

     public void setBrand_name(String brand_name) {
          this.brand_name = brand_name;
     }

     public String getModel_name() {
          return model_name;
     }

     public void setModel_name(String model_name) {
          this.model_name = model_name;
     }

     public String getOPERATING_SYSTEM() {
          return OPERATING_SYSTEM;
     }

     public void setOPERATING_SYSTEM(String OPERATING_SYSTEM) {
          this.OPERATING_SYSTEM = OPERATING_SYSTEM;
     }

     public String getEQUIPMENT_TYPE() {
          return EQUIPMENT_TYPE;
     }

     public void setEQUIPMENT_TYPE(String EQUIPMENT_TYPE) {
          this.EQUIPMENT_TYPE = EQUIPMENT_TYPE;
     }

     public String getDevice_id() {
          return device_id;
     }

     public void setDevice_id(String device_id) {
          this.device_id = device_id;
     }

     public GsmaTacDb(String brand_name, String model_name, String OPERATING_SYSTEM, String EQUIPMENT_TYPE, String device_id) {
          this.brand_name = brand_name;
          this.model_name = model_name;
          this.OPERATING_SYSTEM = OPERATING_SYSTEM;
          this.EQUIPMENT_TYPE = EQUIPMENT_TYPE;
          this.device_id = device_id;
     }

     public GsmaTacDb() {
     }
 
     
     
   
   
}
