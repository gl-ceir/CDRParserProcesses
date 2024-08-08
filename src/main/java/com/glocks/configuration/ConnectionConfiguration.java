package com.glocks.configuration;

import java.sql.Connection;
import java.sql.SQLException;
import org.springframework.stereotype.Repository;
//
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.orm.jpa.EntityManagerFactoryInfo;

@Repository
public class ConnectionConfiguration {
	
	@PersistenceContext
    private EntityManager em;
	
	public Connection getConnection() {
		EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) em.getEntityManagerFactory();
	    try {
			return info.getDataSource().getConnection();
                        
		} catch (SQLException e) {
			return null;
		}
	}
	
}
