/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.abys.report.logger.db;

import com.yoncabt.abys.core.util.ABYSConf;
import com.yoncabt.abys.report.ReportOutputFormat;
import com.yoncabt.abys.report.logger.ReportLogger;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseDBReportLogger implements ReportLogger {

    private Connection connection;

    @Autowired
    private JDBCUtil jdbcutil;

    @PostConstruct
    private void init() {
        try {
            connection = jdbcutil.connect("logger");
        } catch (SQLException ex) {
            throw new Error(ex);
        }
    }

    @PreDestroy
    private void destroy() throws SQLException {
        connection.close();
    }

    @Override
    public void logReport(String uuid, Map<String, Object> reportParams, ReportOutputFormat outputFormat, InputStream reportData) throws IOException {
        String table = ABYSConf.INSTANCE.getValue("report.dblogger.tableName", "log_reports");
        String sql = String.format("insert into %s ("
                + "id, report, time_stamp, "
                + "request, data) "
                + "values("
                + "?, ?, ?, "
                + "?, ?)", table);
        try {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                //ps.setString(1, id)
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(BaseDBReportLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public byte[] getReportData(String uuid) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
