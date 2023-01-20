package com.glocks.parser;

import java.sql.Connection;

public class Rule {
	String rule_name;
	String output;
	String ruleid;
	String period;
	String action;
	String failed_rule_aciton;
     
	
	public Rule(String rule_name, String output, String ruleid, String period, String action,String failed_rule_action){
		this.rule_name = rule_name;
		this.output = output;
		this.ruleid = ruleid;
		this.period = period;
		this.action = action;
		this.failed_rule_aciton = failed_rule_action;
	}
}
