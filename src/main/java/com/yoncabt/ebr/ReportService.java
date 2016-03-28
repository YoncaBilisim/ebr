package com.yoncabt.ebr;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import com.yoncabt.abys.core.util.EBRConf;
import com.yoncabt.abys.core.util.EBRParams;
import com.yoncabt.abys.core.util.log.FLogManager;
import com.yoncabt.ebr.executor.ReportList;
import com.yoncabt.ebr.executor.ReportTask;
import com.yoncabt.ebr.executor.Status;
import com.yoncabt.ebr.jdbcbridge.pool.DataSourceManager;
import com.yoncabt.ebr.logger.ReportLogger;

@Component
public class ReportService {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ThreadPoolTaskExecutor executor;

    @Autowired
    private ThreadPoolTaskScheduler scheduler;

    @Autowired
    private ReportList requestList;

    @Autowired
    private ReportIDGenerator reportIDGenerator;

    @Autowired
    private ReportLogger reportLogger;

    @Autowired
    private DataSourceManager dataSourceManager;

    private static FLogManager logManager = FLogManager.getLogger(ReportTask.class);

    public List<String> dataSourceNames() {
        return new ArrayList<>(dataSourceManager.getDataSourceNames());
    }

    public Status status(String requestId) {
        ReportTask task = requestList.get(requestId);
        if (task == null) {//başlamamış
            logManager.info("status query :YOK !!! " + requestId);
            return null;
        }
        logManager.info("status query :" + task.getRequest().getUuid());
        synchronized (task) {
            if (task.getStarted() == 0) {//başlamamış
                logManager.info("status query :" + task.getRequest().getUuid() + " :başlamış");
                return Status.WAIT;
            }
            if (task.getEnded() == 0) {//devam ediyor
                logManager.info("status query :" + task.getRequest().getUuid() + " :devam ediyor");
                return Status.RUN;
            }
            if (task.getException() != null) {
                logManager.info("status query :" + task.getRequest().getUuid() + " :hata");
                return Status.EXCEPTION;
            }
            logManager.info("status query :" + task.getRequest().getUuid() + " :bitmiş");
            return Status.FINISH;
        }
    }

    public List<String> reports() {
        return requestList.getAllIds();
    }

    public ReportTask detail(String requestId) {
        ReportTask task = requestList.get(requestId);
        if (task == null) {//başlamamış
            logManager.info("output :YOK !!! " + requestId);
        }
        return task;
    }

    public void cancel(String requestId) {
        ReportTask task = requestList.get(requestId);
        if (task == null) {//başlamamış
            logManager.info("output :YOK !!! " + requestId);
            throw new NoSuchElementException(requestId);
        }
        task.cancel();
        task.setSentToClient(true);
    }

    public byte[] output(String requestId) throws IOException {
        ReportTask task = requestList.get(requestId);
        if (task == null) {
            logManager.info("output :YOK !!! " + requestId);
            throw new NoSuchElementException(requestId);
        }
        task.setSentToClient(true);
        logManager.info("output :" + task.getRequest().getUuid());
        return reportLogger.getReportData(requestId);
    }

    public String error(String requestId) throws IOException {
        ReportTask task = requestList.get(requestId);
        if (task == null) {
            logManager.info("output :YOK !!! " + requestId);
            throw new NoSuchElementException(requestId);
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        task.getException().printStackTrace(pw);
        pw.flush();
        task.setSentToClient(true);
        return sw.getBuffer().toString();
    }

    @SuppressWarnings("ThrowableResultIgnored")
    public ReportTask request(ReportRequest req) {
        ReportTask task = context.getBean(ReportTask.class);
        if (StringUtils.isBlank(req.getLocale())) {
            req.setLocale(EBRConf.INSTANCE.getValue(EBRParams.REPORTS_DEFAULT_LOCALE, "tr_TR"));
        }
        if (StringUtils.isBlank(req.getUuid())) {
            req.setUuid(reportIDGenerator.generate());
        }
        if (FilenameUtils.getExtension(req.getReport()).isEmpty()) {
            req.setReport(req.getReport() + ".jrxml");
        }
        task.setRequest(req);
        requestList.add(task);
        if (req.getScheduleTime() > 0) {
            task.setStatus(Status.SCHEDULED);
            scheduler.schedule(task, new Date(req.getScheduleTime()));
            ReportResponse res = new ReportResponse();
            res.setUuid(req.getUuid());
            return task;
        } else if (req.isAsync()) {
            executor.execute(task);
            ReportResponse res = new ReportResponse();
            res.setUuid(req.getUuid());
            if (task.getException() != null) {
                task.getResponse().setExceptionLog(task.getException() + "\n" + ExceptionUtils.getFullStackTrace(task.getException()));
                return task;
            }
            return task;
        } else {
            //hemen çalışacak olanlar buraya
            task.run();
            if (task.getException() != null) {
                task.getResponse().setExceptionLog(task.getException() + "\n" + ExceptionUtils.getFullStackTrace(task.getException()));
                return task;
            }
            return task;
        }
    }
}
