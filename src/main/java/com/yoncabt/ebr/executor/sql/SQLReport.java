/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.executor.sql;

import com.yoncabt.ebr.ui.FieldType;
import com.yoncabt.ebr.ReportOutputFormat;
import com.yoncabt.ebr.ReportRequest;
import com.yoncabt.ebr.exceptions.ReportException;
import com.yoncabt.ebr.executor.BaseReport;
import com.yoncabt.ebr.executor.definition.ReportDefinition;
import com.yoncabt.ebr.executor.definition.ReportParam;
import com.yoncabt.ebr.executor.jasper.Convert;
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

    @Autowired
    private ReportLogger reportLogger;

    @Override
    public ReportDefinition loadDefinition(File file) throws IOException {
        setFile(file);
        String jsonFilePath = StringUtils.removeEnd(file.getAbsolutePath(), ".sql") + ".ebr.json";
        File jsonFile = new File(jsonFilePath);
        return super.loadDefinition(file, jsonFile);
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
                        String value = Convert.to(request.getReportParams().get(reportParam.getName()), String.class);
                        p.set(reportParam.getName(), value);
                        break;
                    }
                    case INTEGER: {
                        Integer value = Convert.to(request.getReportParams().get(reportParam.getName()), Integer.class);
                        p.set(reportParam.getName(), value);
                        break;
                    }
                    case LONG: {
                        Long value = Convert.to(request.getReportParams().get(reportParam.getName()), Long.class);
                        p.set(reportParam.getName(), value);
                        break;
                    }
                    case DOUBLE: {
                        Double value = Convert.to(request.getReportParams().get(reportParam.getName()), Double.class);
                        p.set(reportParam.getName(), value);
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
