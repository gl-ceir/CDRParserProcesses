/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.glocks.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.Map;


//ETl-Class
public class InsertDbDao implements Runnable {

    static Logger logger = LogManager.getLogger(InsertDbDao.class);

    String query;
    Connection conn;
    Map<String, String> map;

    public InsertDbDao() {
    }

    public InsertDbDao(Connection conn, String query) {
        this.conn = conn;
        this.query = query;
    }

    public InsertDbDao(Connection conn, String query, Map<String, String> map) {
        this.conn = conn;
        this.query = query;
        this.map = map;
    }

    public void insertIntoTable(Connection conn, String query) {
        try (Statement stmtNew = conn.createStatement()) {
            stmtNew.executeUpdate(query);
        } catch (Exception e) {
            logger.error("[]" + query + "[] Error occured in Thread while inserting query  -- " + e.getLocalizedMessage() + "At ---" + e);

        }
    }

    @Override
    public void run() {
        logger.info("[RUNNABLE Query]" + query);
        try (Statement stmtNew = conn.createStatement()) {
            stmtNew.executeUpdate(query);
        } catch (Exception e) {
            logger.error("[]" + query + "[] Error occured in Thread while inserting query  -- " + e.getLocalizedMessage() + "At ---" + e);
        }
    }

    public void run1() {
        logger.info("[RUNNABLE Query]" + query);
        try (Statement stmtNew = conn.createStatement()) {
            stmtNew.executeUpdate(query);
        } catch (SQLIntegrityConstraintViolationException e) {
           if( checkIfPresentINDup(conn, map))
           {   }else{}

        } catch (Exception e) {
            logger.error("[]" + query + "[] Error occured in Thread while inserting query  -- " + e.getLocalizedMessage() + "At ---" + e);
        }
    }




    private boolean checkIfPresentINDup(Connection conn, Map<String, String> map) {
        return false;
    }

}
