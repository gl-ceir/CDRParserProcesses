package com.glocks.parser.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.glocks.dao.MessageConfigurationDbDao;
import com.glocks.dao.NotificationDao;
import com.glocks.dao.TypeApprovalDbDao;
import com.glocks.dao.UserWithProfileDao;
import com.glocks.parser.CEIRFeatureFileFunctions;
import com.glocks.parser.CEIRFeatureFileParser;
import com.glocks.parser.ErrorFileGenrator;
import com.glocks.parser.Rule;
import com.glocks.pojo.HttpResponse;
import com.glocks.pojo.MessageConfigurationDb;
import com.glocks.pojo.Notification;
import com.glocks.pojo.TypeApprovedDb;
import com.glocks.pojo.UserWithProfile;
import com.glocks.resttemplate.TacApiConsumer;
import com.gl.Rule_engine_Old.RuleEngineApplication;
import java.io.BufferedWriter;

public class RegisterTac {

    static Logger logger = LogManager.getLogger(RegisterTac.class);

    public void process(Connection conn, String operator, String sub_feature, ArrayList<Rule> rulelist, String txnId,
            String operator_tag, String usertypeName) {

        CEIRFeatureFileFunctions ceirfunction = new CEIRFeatureFileFunctions();

        TypeApprovalDbDao typeApprovalDbDao = new TypeApprovalDbDao();
        MessageConfigurationDbDao messageConfigurationDbDao = new MessageConfigurationDbDao();
        NotificationDao notificationDao = new NotificationDao();
        UserWithProfileDao userWithProfileDao = new UserWithProfileDao();
        TacApiConsumer tacApiConsumer = new TacApiConsumer();
        ErrorFileGenrator err = new ErrorFileGenrator();
        try {
            // Fetch type approved details.
            Optional<TypeApprovedDb> typeApprovedDbOptional = typeApprovalDbDao.getTypeApprovedDbTxnId(conn, "", txnId);
            if (typeApprovedDbOptional.isPresent()) {
                MessageConfigurationDb messageDb = null;
                BufferedWriter bw = null;
                int resultValue = 2;
                String action_output = null;
                String errorFilePath = CEIRFeatureFileParser.getErrorFilePath(conn);
                TypeApprovedDb typeApprovedDb = typeApprovedDbOptional.get();
                HttpResponse httpResponse = tacApiConsumer.approveReject(typeApprovedDb.getTxnId(), 1);

                if (httpResponse.getErrorCode() != 200) {
                    // TODO Add to the Alert.
                    logger.info("Approve_Reject API for type_approved_db  at Processing State is failed. Response[" + httpResponse + "]");
                    return;
                }
                // gET form rule

                String[] ruleArr = {"EXISTS_IN_TYPE_APPROVED_TAC", "1", "TAC", typeApprovedDb.getTac()};   // typeApproceTac with status =3 
                action_output = RuleEngineApplication.startRuleEngine(ruleArr, conn, bw);
                logger.info("EXISTS_IN_TYPE_APPROV  result " + action_output);
                if (action_output.equalsIgnoreCase("NO")) {   // tac Format
                    String[] ruleArr1 = {"TAC_FORMAT", "1", "TAC", typeApprovedDb.getTac()};
                    action_output = RuleEngineApplication.startRuleEngine(ruleArr1, conn, bw);
                    logger.info("TAC_FORMAT result" + action_output);
                    if (action_output.equalsIgnoreCase("YES")) {
                        String[] ruleArr2 = {"EXISTS_IN_GSMA_TAC_DB", "1", "TAC", typeApprovedDb.getTac()};
                        action_output = RuleEngineApplication.startRuleEngine(ruleArr2, conn, bw);
                        logger.info(" EXISTS_IN_GSMA_TAC_DB result " + action_output);
                        if (action_output.equalsIgnoreCase("YES")) {
                            resultValue = 3;
                        } else {
                            err.writeErrorMessageInFile(errorFilePath, txnId, "TAC No.:" + typeApprovedDb.getTac() + " ,  Error CODE :CON_RULE_0003   , Error Description :  TAC  is not approved from GSMA ");
                            resultValue = 2;
                        }
                    } else {
                        err.writeErrorMessageInFile(errorFilePath, txnId, "TAC No.: " + typeApprovedDb.getTac() + " ,  Error CODE : CON_RULE_0014  , Error Description :  Tac Format is not as per Specifications ");
                        resultValue = 2;
                    }
                } else {
                    resultValue = 3;
                }
                //3 Pass   //2 Fail
                httpResponse = tacApiConsumer.approveReject(typeApprovedDb.getTxnId(), resultValue);
                if (httpResponse.getErrorCode() != 200) {
                    // TODO Add to the Alert.
                    logger.info("Approve_Reject API for type_approved_db is failed. Response[" + httpResponse + "]");
                    return;
                }

                
                // Get users Profile.
//                UserWithProfile userWithProfile = userWithProfileDao.getUserWithProfileById(conn, typeApprovedDb.getUserId());
//                // Read message
//                Optional<MessageConfigurationDb> messageDbOptional = messageConfigurationDbDao.getMessageDbTag(conn, "TAC_PROCESS_SUCCESFUL_MAIL_TO_USER");
//                if (messageDbOptional.isPresent()) {     // 1Success ,
//                    messageDb = messageDbOptional.get();
//                    String message = messageDb.getValue().replace("<tac>", typeApprovedDb.getTac());
//                    message = message.replace("<txn_name>", txnId);
//                    message = message.replace("<first name>", userWithProfile.getFirstName());
//                    // Saving in notification
//                    Notification notification = new Notification("EMAIL", message, typeApprovedDb.getUserId(),
//                            21L,
//                            "Type Approved",
//                            "Register",
//                            txnId,
//                            messageDb.getSubject().replace("<XXX>", txnId),
//                            "", //roleType
//                            userWithProfile.getUsertypeName() // receiverUserType
//                    );
//                    notificationDao.insertNotification(conn, notification);
//                } else {
//                    logger.warn("No message is configured for tag [TAC_PROCESS_SUCCESFUL_MAIL_TO_USER]");
//                    // System.out.println("No message is configured for tag [TAC_PROCESS_SUCCESFUL_MAIL_TO_USER]");
//                }
                
                
                

                ceirfunction.updateFeatureFileStatus(conn, txnId, 4, operator, sub_feature);
            } else {
                logger.info("Txn_id [" + txnId + "] is is not present in type_approved_db.");
                // System.out.println("Txn_id [" + txnId + "] is is not present in type_approved_db.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
    }
}













