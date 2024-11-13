package com.ccs.config.datasource;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import oracle.jdbc.datasource.impl.OracleDataSource;

@Configuration
@Profile("default")
public class CcsDbDataSourceConfig implements DataSourceConfig {	
    @Value("${ccs.datasource.url}")
    private String url;
    
    @Value("${ccs.datasource.username}")
    private String username;
    
    @Value("${ccs.datasource.password}")
    private String password;
    
    @Bean(name = "CcsDbDataSource")
    @Primary
    public DataSource getDataSource() {
    	OracleDataSource ds = null;
        
        try {
            ds = new OracleDataSource();
            
            ds.setURL(url);
            ds.setUser(username);
            ds.setPassword(password);
        } catch (SQLException ea) {
        	ea.printStackTrace();
        }
        
        return ds;
    }
}
