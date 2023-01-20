package com.glocks.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.glocks.pojo.TypeApprovedDb;

public class TypeApprovalDbDao {

     static Logger logger = LogManager.getLogger(TypeApprovalDbDao.class);
     static String GENERIC_DATE_FORMAT = "dd-MM-yyyy";

     public Optional<TypeApprovedDb> getTypeApprovedDbTxnId(Connection conn, String managementDb, String txnId) {
          ResultSet rs = null;
          Statement stmt = null;
          String query = "select id, txn_id, user_id, tac "
                  + "from type_approved_db where txn_id='" + txnId + "'";

          logger.info("Query [" + query + "]");
          // System.out.println("Query ["+query+"]");

          try {
               stmt = conn.createStatement();
               rs = stmt.executeQuery(query);

               if (rs.next()) {
                    return Optional.of(new TypeApprovedDb(rs.getLong("id"), rs.getString("txn_id"),
                            rs.getLong("user_id"), rs.getString("tac")));
               }
          } catch (Exception e) {
               logger.info(e.getMessage(), e);
               e.printStackTrace();
          } finally {
               try {
                    if (Objects.nonNull(stmt)) {
                         stmt.close();
                    }
               } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                    e.printStackTrace();
               }
          }

          return Optional.empty();
     }

     public void updateTypeApprovedDb(Connection conn, TypeApprovedDb typeApprovedDb) {
          PreparedStatement preparedStatement = null;
          String query = "update type_approved_db set approve_status=? where txn_id=?";

          // System.out.println("update type_approved_db [" + query + " ]");
          logger.info("update type_approved_db [" + query + "]");

          try {
               preparedStatement = conn.prepareStatement(query);
               preparedStatement.setInt(1, typeApprovedDb.getApproveStatus());
               preparedStatement.setString(2, typeApprovedDb.getTxnId());

               preparedStatement.execute();

               // System.out.println("update type_approved_db succesfully.");
          } catch (SQLException e) {
               logger.error(e.getMessage(), e);
               e.printStackTrace();
          } finally {
               try {
                    if (Objects.nonNull(preparedStatement)) {
                         preparedStatement.close();
                    }
               } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                    e.printStackTrace();
               }
          }
     }

     public void updateTypeApproveByTxnId(Connection conn, String txnId) {
          Statement stmt = null;
          try {
               String query = "update type_approved_db set delete_flag = 4 where txn_id = '" + txnId + "'";
               stmt = conn.createStatement();
               ResultSet rs = stmt.executeQuery(query);
               rs.close();
               stmt.close();
          } catch (Exception e) {
               logger.info(e.getMessage(), e);
               e.printStackTrace();
          }

     }
}

