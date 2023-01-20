package com.glocks.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.glocks.pojo.DeviceDb;

public class DeviceDbDao {

     static Logger logger = LogManager.getLogger(DeviceDbDao.class);
     static String GENERIC_DATE_FORMAT = "dd-MM-yyyy";

     public List<DeviceDb> getDeviceDbByTxnId(Connection conn, String managementDb, String txnId) {
          Statement stmt = null;
          ResultSet rs = null;
          String query = null;

          List<DeviceDb> deviceDbs = new LinkedList<>();
          try {
               query = "select id, created_on, modified_on, device_type, device_id_type, "
                       + "multiple_sim_status, sn_of_device, imei_esn_meid, DEVICE_LAUNCH_DATE as launch_date, "
                       + "device_status, tac, period, txn_id, state, feature_name "
                       + "from device_db "
                       + "where txn_id='" + txnId + "'";

               // System.out.println("Select Query on device_db ["+query+"]");
               logger.info("Select Query on device_db [" + query + "]");
               stmt = conn.createStatement();
               rs = stmt.executeQuery(query);

               while (rs.next()) {
//				 // System.out.println("Inside while of device_db.");		

                    deviceDbs.add(new DeviceDb(rs.getLong("id"), 0, rs.getString("created_on"), rs.getString("modified_on"),
                            rs.getString("device_type"), rs.getString("device_id_type"), rs.getString("multiple_sim_status"),
                            rs.getString("sn_of_device"), rs.getString("imei_esn_meid"), rs.getString("launch_date"),
                            rs.getString("device_status"), rs.getInt("tac"), rs.getString("period"), rs.getString("txn_id"),
                            rs.getInt("state"), rs.getString("feature_name")));
               }

          } catch (Exception e) {
               logger.error(e.getMessage(), e);
               e.printStackTrace();
          } finally {
               try {
                    rs.close();
                    stmt.close();
               } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                    e.printStackTrace();
               }
          }

          return deviceDbs;
     }

     public void deleteDevicesFromDeviceDb(Connection conn, String imei) {
          String query = "";
          Statement stmt = null;
          try {
               stmt = conn.createStatement();
               int counter = new com.glocks.parser.service.ConsignmentInsertUpdate().getCounterFromDeviceDb(conn, imei);
               logger.debug("imei  " + imei + " ; C0unter  is" + counter);
               if (counter <= 1) {
                    query = "delete from device_db where  imei_esn_meid = '" + imei + "'";
               }
               if (counter > 1) {
                    query = "update  device_db set counter = " + (counter - 1) + " where imei_esn_meid = '" + imei + "' ";
               }
               logger.info(" " + query);
               try {
                    stmt.executeUpdate(query);
                    logger.info("Imei in  device_db updated successfully.");
               } catch (Exception e) {
                    logger.error("." + e.getMessage(), e);
               }
          } catch ( Exception e) {
               logger.error("...." + e.getMessage(), e);
          } finally {
               try {
                    stmt.close();
               } catch ( Exception ex) {
                    logger.error("........." + ex.getMessage(), ex);
               }
          }
           
     }

//     public void insertDeviceDbAud(Connection conn, List<DeviceDb> deviceDbs) {
//          boolean isOracle = conn.toString().contains("oracle");
//          String dateFunction = Util.defaultDate(isOracle);
//
//          PreparedStatement preparedStatement = null;
//
//          String query = "insert into device_db_aud (id, rev, revtype, created_on, modified_on, device_type, device_id_type, "
//                  + "multiple_sim_status, sn_of_device, imei_esn_meid, device_launch_date, device_status, "
//                  + "tac, period, txn_id, state) values(";
//
//          if (isOracle) {
//               query = query + "DEVICE_DB_AUD_seq.nextVal,";
//          } else {
//               query = query + (getMaxIdDeviceDbAud(conn) + 1) + ",";
//          }
//
//          query = query + "?,2, current_timestamp,current_timestamp,?,?,?,?,?,?,?,?,?,?,?)";
//
//          logger.info("Add device_db_aud [" + query + "]");
//
//          try {
//               // System.out.println("sop2.1");
//               preparedStatement = conn.prepareStatement(query);
//
//               for (DeviceDb deviceDb : deviceDbs) {
//                    preparedStatement.setLong(1, deviceDb.getRev());
//                    preparedStatement.setString(2, deviceDb.getDeviceType());
//                    preparedStatement.setString(3, deviceDb.getDeviceIdType());
//                    preparedStatement.setString(4, deviceDb.getMultipleSimStatus());
//                    preparedStatement.setString(5, deviceDb.getSnOfDevice());
//                    preparedStatement.setString(6, deviceDb.getImeiEsnMeid());
//                    preparedStatement.setString(7, deviceDb.getDeviceLaunchDate());
//                    preparedStatement.setString(8, deviceDb.getDeviceStatus());
//                    preparedStatement.setInt(9, deviceDb.getTac());
//                    preparedStatement.setString(10, deviceDb.getPeriod());
//                    preparedStatement.setString(11, deviceDb.getTxnId());
//                    preparedStatement.setLong(12, deviceDb.getState());
//
//                    // System.out.println("Query " + preparedStatement);
//                    preparedStatement.addBatch();
//               }
//
//               preparedStatement.executeBatch();
//
//          } catch (SQLException e) {
//               logger.error(e.getMessage(), e);
//               e.printStackTrace();
//          } finally {
//               try {
//                    if (Objects.nonNull(preparedStatement)) {
//                         preparedStatement.close();
//                    }
//               } catch (SQLException e) {
//                    logger.error(e.getMessage(), e);
//                    e.printStackTrace();
//               }
//          }
//     }
//
// 
// public Long getMaxIdDeviceDbAud(Connection conn) {
//          Statement stmt = null;
//          ResultSet rs = null;
//          String query = null;
//          Long max = null;
//
//          try {
//               query = "select max(id) as max from device_db_aud";
//
//               logger.info("Query [" + query + "]");
//               stmt = conn.createStatement();
//               rs = stmt.executeQuery(query);
//
//               if (rs.next()) {
//                    max = rs.getLong("max");
//               } else {
//                    max = 0L;
//               }
//
//               logger.info("Next Id in device_db_aud[" + max + "]");
//               return max;
//          } catch (Exception e) {
//               logger.info("Exception in getMaxIdDeviceDbAud" + e);
//               e.printStackTrace();
//               return 0L;
//          } finally {
//               try {
//                    if (rs != null) {
//                         rs.close();
//                    }
//                    if (stmt != null) {
//                         stmt.close();
//                    }
//               } catch (SQLException e) {
//                    e.printStackTrace();
//                    logger.error(e.getMessage(), e);
//               }
//          }
//     }
//
}










