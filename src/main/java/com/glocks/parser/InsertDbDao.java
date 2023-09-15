/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.glocks.parser;

import java.sql.Connection;
import java.sql.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author maverick
 */
public class InsertDbDao implements Runnable {

    static Logger logger = LogManager.getLogger(InsertDbDao.class);

    String query;
    Connection conn;

    public InsertDbDao(Connection conn, String query) {
        this.conn = conn;
        this.query = query;
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
}
