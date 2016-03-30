/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.executor.sql;

import com.yoncabt.ebr.FieldType;
import com.yoncabt.ebr.ReportOutputFormat;
import com.yoncabt.ebr.ReportRequest;
import com.yoncabt.ebr.exceptions.ReportException;
import com.yoncabt.ebr.executor.BaseReport;
import com.yoncabt.ebr.executor.definition.ReportDefinition;
import com.yoncabt.ebr.executor.definition.ReportParam;
import com.yoncabt.ebr.jdbcbridge.JDBCNamedParameters;
import com.yoncabt.ebr.jdbcbridge.pool.EBRConnection;
import com.yoncabt.ebr.logger.ReportLogger;
import com.yoncabt.ebr.util.ResultSetSerializer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author myururdurmaz
 */
@Component
@Scope(value = "request")
public class SQLReport extends BaseReport {

    private File file;

    @Autowired
    private ReportLogger reportLogger;

    public ReportDefinition loadDefinition(File file) throws IOException {
        this.file = file;
        String jsonFilePath = StringUtils.removeEnd(file.getAbsolutePath(), ".sql") + ".ebr.json";
        File jsonFile = new File(jsonFilePath);
        return super.loadDefinition(file, jsonFile);
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public void exportTo(ReportRequest request, ReportOutputFormat outputFormat, EBRConnection con, ReportDefinition reportDefinition) throws ReportException, IOException {
        JDBCNamedParameters p = new JDBCNamedParameters(request.getReportQuery());
        for (ReportParam reportParam : reportDefinition.getReportParams()) {
            if (reportParam.isRaw()) {
                //
            } else {
                FieldType type = reportParam.getFieldType();
                switch (type) {
                    case STRING: {
                        String value = (String) request.getReportParams().get(reportParam.getName());
                        if (!StringUtils.isEmpty(value)) {
                            p.set(reportParam.getName(), value);
                        }
                        break;
                    }
                    case INTEGER: {
                        String value = (String) request.getReportParams().get(reportParam.getName());
                        if (!StringUtils.isEmpty(value)) {
                            p.set(reportParam.getName(), Integer.parseInt(value));
                        }
                        break;
                    }
                    case LONG: {
                        String value = (String) request.getReportParams().get(reportParam.getName());
                        if (!StringUtils.isEmpty(value)) {
                            p.set(reportParam.getName(), Long.parseLong(value));
                        }
                        break;
                    }
                    case DOUBLE: {
                        String value = (String) request.getReportParams().get(reportParam.getName());
                        p.set(reportParam.getName(), Double.parseDouble(value));
                        break;
                    }
                    case DATE: {
                        Date value = (Date) request.getReportParams().get(reportParam.getName());
                        p.set(reportParam.getName(), value);
                        break;
                    }
                    default:
                        throw new AssertionError(reportParam.getName() + " in tipi tanınmıyor :" + reportParam.getFieldType());
                }
            }
        }

        try (
                PreparedStatement st = p.prepare(con);
                ResultSet res = st.executeQuery()) {
            File tempFile = File.createTempFile(request.getUuid(), ".json");
            ResultSetSerializer ser = new ResultSetSerializer(res, tempFile);
            ser.serialize();

            try (FileInputStream fis = new FileInputStream(tempFile)) {
                reportLogger.logReport(request, outputFormat, fis);
            }
            tempFile.delete();
        } catch (SQLException ex) {
            throw new ReportException(ex);
        }
    }
}
