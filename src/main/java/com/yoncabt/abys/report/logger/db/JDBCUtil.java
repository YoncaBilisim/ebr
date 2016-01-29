/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.abys.report.logger.db;

import com.yoncabt.abys.core.util.ABYSConf;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.inject.Singleton;
import org.springframework.stereotype.Component;

/**
 *
 * @author myururdurmaz
 */
@Component
@Singleton
public class JDBCUtil {

    public Connection connect(String driver, String url, String user, String pass) throws SQLException {
        try {
            DriverManager.registerDriver((Driver) Class.forName(driver).newInstance());
            return DriverManager.getConnection(url, user, pass);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new SQLException(ex);
        }
    }

    public Connection connect(String dataSource) throws SQLException {
        return connect(
                ABYSConf.INSTANCE.getValue("report.datasource." + dataSource + ".driver", ""),
                ABYSConf.INSTANCE.getValue("report.datasource." + dataSource + ".url", ""),
                ABYSConf.INSTANCE.getValue("report.datasource." + dataSource + ".user", ""),
                ABYSConf.INSTANCE.getValue("report.datasource." + dataSource + ".pass", ""));
    }
}
