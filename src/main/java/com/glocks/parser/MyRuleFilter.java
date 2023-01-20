package com.glocks.parser;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

 

public class MyRuleFilter {

	Logger logger = LogManager.getLogger(MyRuleFilter.class);
	public HashMap getMyRule(Connection conn,HashMap<String, String> device_info, ArrayList<Rule> rulelist){
		HashMap<String, String> rule_detail = new HashMap<String, String>();
		for (Rule rule : rulelist) {
			device_info.put("rule_name", rule.rule_name);
			device_info.put("output", rule.output);
			device_info.put("ruleid", rule.ruleid);
			device_info.put("period", rule.period);
			device_info.put("action", rule.action);
			rule_detail = checkMyRule(conn ,device_info);
			if(rule_detail.get("rule_name") != null ){
				logger.info("Rule Detials"+rule_detail+rule_detail.get("rule_name"));
				logger.info("Breaking the Rules");
				break;
			}
		}
		return rule_detail;
	}

	private HashMap<String, String> checkMyRule(Connection conn,HashMap<String, String> device_info) {
		HashMap<String, String> my_rule_details = new HashMap<>();
		ResultSet rs = null;
		Statement stmt = null;

		if(device_info.get("rule_name").equalsIgnoreCase("LBD")){
			logger.info("Checking LBD");
			String query = "select lawful_device_status from device_db where imei_esn_meid='"+device_info.get("servedIMEI")+"';";
			try {
				stmt=conn.createStatement();
				rs=stmt.executeQuery(query);
				while(rs.next()){
					if(Integer.parseInt(rs.getString("lawful_device_status"))==10){

						my_rule_details.put("rule_name", device_info.get("rule_name"));
						my_rule_details.put("rule_id", device_info.get("rule_id"));
						ResultSet rs1 = null;
						Statement stmt1 = null;
						String insertQuery = "insert into";
						logger.info("Going to Insert into history table");
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		else if(device_info.get("rule_name").equalsIgnoreCase("IMEI_LENGTH")){
			if(device_info.get("servedIMEI").length()==15 ||device_info.get("servedIMEI").length()==16){
				logger.info("Getting Valid Length"+device_info.get("servedIMEI"));
			}
			else{
				my_rule_details.put("rule_name", device_info.get("rule_name"));
				my_rule_details.put("rule_id", device_info.get("rule_id"));
				String insertQuery = "Insert into deviceinvalid_db ()";
				logger.info("Invalid Length"+device_info.get("servedIMEI"));
			}
		}
		
		return my_rule_details;
	}

}
