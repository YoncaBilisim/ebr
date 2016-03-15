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
import com.yoncabt.ebr.jdbcbridge.pool.DataSourceManager;
import com.yoncabt.ebr.jdbcbridge.pool.EBRConnection;
import com.yoncabt.ebr.logger.ReportLogger;
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
public class ReportTask implements Runnable, Comparable<ReportTask> {

    private volatile Status status = Status.WAIT;

    @Autowired
    private JasperReport jasperReports;

    @Autowired
    private YoncaMailSender mailSender;

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private ReportLogger reportLogger;

    private static FLogManager logManager = FLogManager.getLogger(ReportTask.class);

    private ReportRequest request;

    private ReportResponse response;

    private Exception exception;

    private EBRConnection connection;

    private long started;

    private long ended;

    private boolean sentToClient;

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
            //FIXME support for sql
            JasperReport jr = new JasperReport();
            ReportDefinition definition = jr.loadDefinition(JasperReport.getReportFile(request.getReport()));
            if (StringUtils.isEmpty(request.getDatasourceName())) {
                request.setDatasourceName(definition.getDataSource());
            }
            if (StringUtils.isEmpty(request.getDatasourceName())) {
                request.setDatasourceName("default");
            }
            connection = dataSourceManager.get(request.getDatasourceName(), request.getUser(), "EBR", request.getReport());
            jasperReports.exportTo(request, ReportOutputFormat.valueOf(request.getExtension()), connection, definition);
            byte[] bytes = reportLogger.getReportData(request.getUuid());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(bytes, 0, bytes.length);
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

    public EBRConnection getYoncaConnection() {
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

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public int compareTo(ReportTask o) {
        return Long.valueOf(this.getStarted()).compareTo(o.getStarted());
    }

    /**
     * @return the sentToClient
     */
    public boolean isSentToClient() {
        return sentToClient;
    }

    /**
     * @param sentToClient the sentToClient to set
     */
    public void setSentToClient(boolean sentToClient) {
        this.sentToClient = sentToClient;
    }
}
