/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.ws;

import com.yoncabt.abys.core.util.ABYSConf;
import com.yoncabt.abys.core.util.EBRParams;
import com.yoncabt.abys.core.util.log.FLogManager;
import com.yoncabt.ebr.ReportIDGenerator;
import com.yoncabt.ebr.ReportRequest;
import com.yoncabt.ebr.ReportResponse;
import com.yoncabt.ebr.executor.ReportList;
import com.yoncabt.ebr.executor.ReportTask;
import com.yoncabt.ebr.logger.ReportLogger;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author myururdurmaz
 */
@RestController
public class ReportWS {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private TaskExecutor executor;

    @Autowired
    private ReportList requestList;

    @Autowired
    private ReportIDGenerator reportIDGenerator;

    @Autowired
    private ReportLogger reportLogger;

    private static FLogManager logManager = FLogManager.getLogger(ReportTask.class);

    @RequestMapping(
            value = {"/ws/1.0/status/{requestId}"},
            method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity<ReportResponse> status(
            @PathVariable("requestId") String requestId
    ) {
        ReportTask task = requestList.get(requestId);
        if (task == null) {//başlamamış
            logManager.info("status query :YOK !!! " + requestId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        logManager.info("status query :" + task.getRequest().getUuid());
        synchronized (task) {
            if (task.getStarted() == 0) {//başlamamış
                logManager.info("status query :" + task.getRequest().getUuid() + " :başlamış");
                return ResponseEntity.status(HttpStatus.CREATED).body(null);
            }
            if (task.getEnded() == 0) {//devam ediyor
                logManager.info("status query :" + task.getRequest().getUuid() + " :devam ediyor");
                return ResponseEntity.status(HttpStatus.PROCESSING).body(null);
            }
            if (task.getException() != null) {
                logManager.info("status query :" + task.getRequest().getUuid() + " :hata");
                return ResponseEntity.status(420).body(null);//420 Method Failure
            }
            logManager.info("status query :" + task.getRequest().getUuid() + " :bitmiş");
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
    }

    @RequestMapping(
            value = {"/ws/1.0/reports"},
            method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity<List<String>> reports() {
        return ResponseEntity.ok(requestList.getAllIds());
    }

    @RequestMapping(
            value = {"/ws/1.0/detail/{requestId}"},
            method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity<ReportTask> detail(
            @PathVariable("requestId") String requestId
    ) {
        ReportTask task = requestList.get(requestId);
        if (task == null) {//başlamamış
            logManager.info("output :YOK !!! " + requestId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @RequestMapping(
            value = {"/ws/1.0/cancel/{requestId}"},
            method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity<ReportTask> cancel(
            @PathVariable("requestId") String requestId
    ) {
        ReportTask task = requestList.get(requestId);
        if (task == null) {//başlamamış
            logManager.info("output :YOK !!! " + requestId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        task.cancel();
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @RequestMapping(
            value = {"/ws/1.0/output/{requestId}"},
            method = RequestMethod.GET,
            produces = "application/octet-stream")
    public ResponseEntity<byte[]> output(
            @PathVariable("requestId") String requestId
    ) throws IOException {
        ReportTask task = requestList.get(requestId);
        if (task == null) {//başlamamış
            logManager.info("output :YOK !!! " + requestId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        logManager.info("output :" + task.getRequest().getUuid());
        return ResponseEntity.status(HttpStatus.OK).body(reportLogger.getReportData(requestId));
    }

    @RequestMapping(
            value = {"/ws/1.0/error/{requestId}"},
            method = RequestMethod.GET,
            produces = "text/plain")
    public ResponseEntity<String> error(
            @PathVariable("requestId") String requestId
    ) throws IOException {
        ReportTask task = requestList.get(requestId);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        task.getException().printStackTrace(pw);
        pw.flush();
        return ResponseEntity.status(HttpStatus.OK).body(sw.getBuffer().toString());
    }

    @RequestMapping(
            value = {"/ws/1.0/request"},
            method = RequestMethod.POST,
            produces = "application/json",
            consumes = "application/json")
    @SuppressWarnings("ThrowableResultIgnored")
    public ResponseEntity<ReportResponse> request(
            @RequestBody(required = true) ReportRequest req
    ) {
        ReportTask task = context.getBean(ReportTask.class);
        if (StringUtils.isBlank(req.getDatasourceName())) {
            req.setDatasourceName("default");
        }
        if (StringUtils.isBlank(req.getLocale())) {
            req.setLocale(ABYSConf.INSTANCE.getValue(EBRParams.REPORTS_DEFAULT_LOCALE, "tr_TR"));
        }
        if (StringUtils.isBlank(req.getUuid())) {
            req.setUuid(reportIDGenerator.generate());
        }
        if (!req.getReport().endsWith(".jrxml")) {
            req.setReport(req.getReport() + ".jrxml");
        }
        task.setRequest(req);
        requestList.add(task);
        if (req.isAsync()) {
            executor.execute(task);
            ReportResponse res = new ReportResponse();
            res.setUuid(req.getUuid());
            if (task.getException() != null) {
                task.getResponse().setExceptionLog(task.getException() + "\n" + ExceptionUtils.getFullStackTrace(task.getException()));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(task.getResponse());
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } else {
            //hemen çalışacak olanlar buraya
            task.run();
            if (task.getException() != null) {
                task.getResponse().setExceptionLog(task.getException() + "\n" + ExceptionUtils.getFullStackTrace(task.getException()));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(task.getResponse());
            }
            return ResponseEntity.status(HttpStatus.OK).body(task.getResponse());
        }
    }
}
