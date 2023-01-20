package com.glocks.parser.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.glocks.dao.ConsignmentMgmtDao;
import com.glocks.dao.DeviceCustomDbDao;
import com.glocks.dao.DeviceDbDao;
import com.glocks.dao.DeviceImporterDbDao;
import com.glocks.dao.SourceTacInactiveInfoDao;
import com.glocks.parser.Rule;
import com.glocks.pojo.DeviceImporterDb;

public class ConsignmentDelete {

     static Logger logger = LogManager.getLogger(ConsignmentDelete.class);

     public void process(Connection conn, String operator, String subFeature, ArrayList<Rule> rulelist, String txnId, String operator_tag,
             String usertypeName) {

          DeviceDbDao deviceDbDao = new DeviceDbDao();
          DeviceImporterDbDao deviceImporterDbDao = new DeviceImporterDbDao();
          ConsignmentMgmtDao consignmentMgmtDao = new ConsignmentMgmtDao();
          DeviceCustomDbDao deviceCustomDbDao = new DeviceCustomDbDao();
          SourceTacInactiveInfoDao sourceTacInactiveInfoDao = new SourceTacInactiveInfoDao();
logger.info("Consignment Delete started");
          try {
//               List<DeviceDb> deviceDbs = deviceDbDao.getDeviceDbByTxnId(conn, "", txnId);
//			deviceDbDao.insertDeviceDbAud(conn, deviceDbs);
               List<DeviceImporterDb> deviceImporterDbs = deviceImporterDbDao.getDeviceImporterDbByTxnId(conn, txnId);
               for (DeviceImporterDb dvcImpDb : deviceImporterDbs) {
                    deviceDbDao.deleteDevicesFromDeviceDb(conn, dvcImpDb.getImeiEsnMeid());
               }
               logger.info("dvc Db Completed");
//			logger.info("deviceImporterDbs" + deviceImporterDbs);
//			deviceImporterDbDao.insertDeviceImporterDbAud(conn, deviceImporterDbs);
               deviceImporterDbDao.deleteDevicesFromDeviceImporterDb(conn, txnId);
               logger.info("dvc Importer Db Completed");
//               List<DeviceCustomDb> deviceCustomDbs = deviceCustomDbDao.getDeviceCustomDbByTxnId(conn, txnId);
//			logger.info("deviceCustomDbs" + deviceCustomDbs);
//			deviceCustomDbDao.insertDeviceCustomDbAud(conn, deviceCustomDbs);
               deviceCustomDbDao.deleteDevicesFromDeviceCustomDb(conn, txnId);
               logger.info("dvc Custom Db Completed");

               sourceTacInactiveInfoDao.deleteFromSourceTacInactiveInfo(conn, txnId);

               logger.info("dvc Source Tac Inactive Db Completed");

               consignmentMgmtDao.updateConsignmentMgmtDeleteFlag(conn, txnId, 2);

          } catch (Exception e) {
               logger.error(e.getMessage(), e);
          }
     }
}






