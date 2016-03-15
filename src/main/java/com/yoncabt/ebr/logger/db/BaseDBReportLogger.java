/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.logger.db;

import com.yoncabt.abys.core.util.EBRConf;
import com.yoncabt.abys.core.util.EBRParams;
import com.yoncabt.ebr.ReportOutputFormat;
import com.yoncabt.ebr.ReportRequest;
import com.yoncabt.ebr.jdbcbridge.pool.DataSourceManager;
import com.yoncabt.ebr.jdbcbridge.pool.EBRConnection;
import com.yoncabt.ebr.logger.ReportLogger;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <code>
 * CREATE TABLE LOG_REPORTS
 * (
 * ID VARCHAR2(30) NOT NULL
 * , REPORT_NAME VARCHAR2(512)
 * , TIME_STAMP DATE
 * , REQUEST_PARAMS CLOB
 * , REPORT_DATA BLOB
 * , FILE_EXTENSION VARCHAR2(20)
 * , DATA_SOURCE_NAME VARCHAR2(20)
 * , EMAIL VARCHAR2(150)
 * , REPORT_LOCALE VARCHAR2(10)
 * , REPORT_USER VARCHAR2(50)
 * , CONSTRAINT LOG_REPORTS_PK PRIMARY KEY
 * (
 * ID
 * )
 * ENABLE
 * );
 * </code>
 *
 * @author myururdurmaz
 */
public class BaseDBReportLogger implements ReportLogger {

    @Autowired
    private DataSourceManager dataSourceManager;

    @PostConstruct
    private void init() {
    }

    @PreDestroy
    private void destroy() throws SQLException {
    }

    @Override
    public void logReport(ReportRequest request, ReportOutputFormat outputFormat, InputStream reportData) throws IOException {
        String table = EBRConf.INSTANCE.getValue(EBRParams.REPORT_LOGGER_DBLOGGER_TABLENAME, "log_reports");
        String sql = String.format("update %s set report_data = ? where id = ?", table);

        try (EBRConnection con = dataSourceManager.get("dblogger", request.getUser(), "EBR", getClass().getSimpleName());){
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setBinaryStream(1, reportData);
                ps.setString(2, request.getUuid());
                if (ps.executeUpdate() == 1) {// daha önce loglanmış bir rapor ise sadece güncelle
                    con.commit();
                    return;
                }
                con.commit();
            }

            sql = String.format("insert into %s ("
                    + "id, report_name, time_stamp, "
                    + "request_params, report_data, file_extension,"
                    + "data_source_name, email, report_locale,"
                    + "report_user) "
                    + "values("
                    + "?, ?, ?,"
                    + "?, ?, ?,"
                    + "?, ?, ?,"
                    + "?)", table);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, request.getUuid());
                ps.setString(2, request.getReport());
                ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));

                JSONObject jo = new JSONObject(request.getReportParams());
                ps.setString(4, jo.toString(4));
                ps.setBinaryStream(5, reportData);
                ps.setString(6, request.getExtension());

                ps.setString(7, request.getDatasourceName());
                ps.setString(8, request.getEmail());
                ps.setString(9, request.getLocale());

                ps.setString(10, request.getUser());

                ps.executeUpdate();
                con.commit();
            }
        } catch (SQLException ex) {
            Logger.getLogger(BaseDBReportLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public byte[] getReportData(String uuid) throws IOException {
        String table = EBRConf.INSTANCE.getValue("report.dblogger.tableName", "log_reports");
        String sql = String.format("select report_data from %s where id = ?", table);
        try (EBRConnection con = dataSourceManager.get("dblogger", "--", "EBR", getClass().getSimpleName());
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid);
            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    return res.getBytes(1);
                }
            }
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
        throw new FileNotFoundException(uuid);
    }

}
