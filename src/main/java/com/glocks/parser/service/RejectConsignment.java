package com.glocks.parser.service;

import java.sql.Connection;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.glocks.parser.Rule;

public class RejectConsignment {
	static Logger logger = LogManager.getLogger(RejectConsignment.class);

	public void process(Connection conn, String operator, String sub_feature, ArrayList<Rule> rulelist, String txnId, 
			String operator_tag, String usertypeName ){
		new ConsignmentDelete().process(conn, operator, sub_feature, rulelist, txnId, operator_tag, usertypeName);
	}
}
