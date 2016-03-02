/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.jdbcbridge;

import com.yoncabt.abys.core.util.EBRConf;
import com.yoncabt.abys.core.util.log.FLogManager;
import com.yoncabt.ebr.ReportRequest;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.inject.Singleton;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 *
 * @author myururdurmaz
 */
@Component
@Singleton
public class JDBCUtil {

    private static FLogManager logManager = FLogManager.getLogger(JDBCUtil.class);

    public YoncaConnection connect(String driver, String url, String user, String pass, ReportRequest hint) throws SQLException {
        try {
            DriverManager.registerDriver((Driver) Class.forName(driver).newInstance());
            Connection connection = DriverManager.getConnection(url, user, pass);
            if (hint != null) {
                fillHint(connection, hint);
            }
            return new YoncaConnection(connection);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new SQLException(ex);
        }
    }

    public YoncaConnection connect(ReportRequest req) throws SQLException {
        return connect(req.getDatasourceName(), req);
    }

    public YoncaConnection connect(String dataSource) throws SQLException {
        return connect(dataSource, null);
    }

    public YoncaConnection connect(String dataSource, ReportRequest req) throws SQLException {
        final String driver = EBRConf.INSTANCE.getValue("report.datasource." + dataSource + ".driver", "");
        if (StringUtils.isEmpty(driver)) {
            throw new IllegalArgumentException("driver not found");
        }

        final String url = EBRConf.INSTANCE.getValue("report.datasource." + dataSource + ".url", "");
        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException("url not found");
        }

        final String user = EBRConf.INSTANCE.getValue("report.datasource." + dataSource + ".user", "");
        if (StringUtils.isEmpty(user)) {
            throw new IllegalArgumentException("user not found");
        }

        final String pass = EBRConf.INSTANCE.getValue("report.datasource." + dataSource + ".pass", "");
        if (StringUtils.isEmpty(pass)) {
            throw new IllegalArgumentException("pass not found");
        }

        return connect(driver, url, user, pass, req);
    }

    // bu metod biraz kötü
    private void fillHint(Connection connection, ReportRequest req) {
        if (connection.getClass().getName().startsWith("oracle")) { //diğer veritabanları için bakmak lazım
            try {
                CallableStatement call;
                call = connection.prepareCall("call dbms_application_info.set_client_info(?)");
                call.setString(1, req.getUser());
                call.execute();

                call = connection.prepareCall("call dbms_application_info.set_module(module_name => ?, action_name => ?)");
                call.setString(1, "EBR");
                call.setString(2, req.getUuid());
                call.execute();

            } catch (SQLException ex) {
                logManager.error(ex);
            }
        }
    }
}
