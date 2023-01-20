package com.glocks.parser.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.glocks.dao.TypeApprovalDbDao;
import com.glocks.parser.CEIRFeatureFileFunctions;
import com.glocks.parser.Rule;
import com.glocks.pojo.HttpResponse;
import com.glocks.pojo.TypeApprovedDb;
import com.glocks.resttemplate.TacApiConsumer;

public class WithdrawnTac {

    static Logger logger = LogManager.getLogger(WithdrawnTac.class);

    public WithdrawnTac() {

    }

    public void process(Connection conn, String operator, String sub_feature, ArrayList<Rule> rulelist, String txnId,
            String operator_tag, String usertypeName) {

//		CEIRFeatureFileFunctions ceirfunction = new CEIRFeatureFileFunctions();
        TypeApprovalDbDao typeApprovalDbDao = new TypeApprovalDbDao();
//        TacApiConsumer tacApiConsumer = new TacApiConsumer();

        try {
//            Optional<TypeApprovedDb> typeApprovedDbOptional = typeApprovalDbDao.getTypeApprovedDbTxnId(conn, "", txnId);

            
            typeApprovalDbDao.updateTypeApproveByTxnId(conn,txnId);
            
            
//            if (typeApprovedDbOptional.isPresent()) {
//                TypeApprovedDb typeApprovedDb = typeApprovedDbOptional.get();
//                HttpResponse httpResponse = tacApiConsumer.delete(txnId, typeApprovedDb.getUserId(),
//                        "CEIRSYSTEM", 4);    // CEIRSYSTEM    SystemAdmin    //2
//                if (httpResponse.getStatusCode() != 200) {
//                    // TODO Add to the Alert.
//                    logger.info("Delete API for type_approved_db is failed. Response[" + httpResponse + "]");
//                    return;
//                } else {
//                    logger.info("Delete API for type_approved_db is Success. Response[" + httpResponse + "]");
//                    return;
//                }
//            } else {
//                logger.info("type_approved_db has no record for txn_id[" + txnId + "]");
//                return;
//            }

            
            
            // ceirfunction.updateFeatureFileStatus(conn, txnId, 2, operator, sub_feature);	
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
    }
}


// set flag 2 , delete from table : delete from typeAproveDb via txnId update  4







