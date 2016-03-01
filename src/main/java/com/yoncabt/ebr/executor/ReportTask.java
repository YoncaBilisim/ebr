/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.executor;

import com.yoncabt.abys.core.util.log.FLogManager;
import com.yoncabt.ebr.ReportOutputFormat;
import com.yoncabt.ebr.ReportRequest;
import com.yoncabt.ebr.ReportResponse;
import com.yoncabt.ebr.executor.definition.ReportDefinition;
import com.yoncabt.ebr.executor.jasper.JasperReport;
import com.yoncabt.ebr.executor.jasper.YoncaJasperReports;
import com.yoncabt.ebr.jdbcbridge.JDBCUtil;
import com.yoncabt.ebr.jdbcbridge.YoncaConnection;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author myururdurmaz
 */
@Component
@Scope("prototype")
public class ReportTask implements Runnable {

    private volatile Status status = Status.WAIT;

    @Autowired
    private YoncaJasperReports jasperReports;

    @Autowired
    private YoncaMailSender mailSender;

    @Autowired
    private JDBCUtil jdbcutil;

    private static FLogManager logManager = FLogManager.getLogger(ReportTask.class);

    private ReportRequest request;

    private ReportResponse response;

    private Exception exception;

    private YoncaConnection connection;

    private long started;

    private long ended;

    @Override
    public void run() {
        if (status == Status.CANCEL) {
            return;
        }
        status = Status.RUN;
        logManager.info(request.getUuid() + " başladı");
        synchronized (this) {
            started = System.currentTimeMillis();
        }
        response = new ReportResponse();
        response.setUuid(request.getUuid());
        try {
            connection = jdbcutil.connect(request);
            //FIXME support for sql
            JasperReport jr = new JasperReport(JasperReport.getReportFile(request.getReport()));
            ReportDefinition definition = jr.loadDefinition();
            definition.setDataSource(request.getDatasourceName());
            jasperReports.exportTo(request.getReportParams(), ReportOutputFormat.valueOf(request.getExtension()), connection, request.getLocale(), request.getUuid(), definition);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (!StringUtils.isBlank(request.getEmail())) {
                mailSender.send(request.getEmail(), "Raporunuz ektedir", Collections.singletonMap(request.getUuid() + "." + request.getExtension(), baos.toByteArray()));
            }
            logManager.info(request.getUuid() + " bitti");
            status = Status.FINISH;
        } catch (Exception ex) {
            status = Status.EXCEPTION;
            logManager.error(request.getUuid() + " hata", ex);
            synchronized (this) {
                exception = ex;
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        synchronized (this) {
            ended = System.currentTimeMillis();
        }
    }

    public ReportRequest getRequest() {
        return request;
    }

    public void setRequest(ReportRequest request) {
        this.request = request;
    }

    /**
     * @return the exception
     */
    public Exception getException() {
        return exception;
    }

    public ReportResponse getResponse() {
        return response;
    }

    public YoncaConnection getYoncaConnection() {
        return connection;
    }

    public long getStarted() {
        return started;
    }

    public long getEnded() {
        return ended;
    }

    public void cancel() {
        if (status != Status.FINISH) {
            status = Status.CANCEL;
        }
        if (getYoncaConnection() != null) {
            getYoncaConnection().cancel();
        }
    }

    /**
     * @return the status
     */
    public Status getStatus() {
        return status;
    }
}
