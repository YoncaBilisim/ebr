/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.ws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.yoncabt.abys.core.util.log.FLogManager;
import com.yoncabt.ebr.ReportOutputFormat;
import com.yoncabt.ebr.ReportRequest;
import com.yoncabt.ebr.ReportResponse;
import com.yoncabt.ebr.ReportService;
import com.yoncabt.ebr.executor.ReportTask;
import com.yoncabt.ebr.executor.definition.ReportDefinition;
import net.sf.jasperreports.engine.JRException;

/**
 *
 * @author myururdurmaz
 */
@RestController
public class ReportWS {

    @Autowired
    private ReportService reportService;

    private static final FLogManager logManager = FLogManager.getLogger(ReportTask.class);

    @RequestMapping(
            value = {"/ws/1.0/dataSourceNames"},
            method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity<List<String>> dataSourceNames() {
        return ResponseEntity.ok(new ArrayList<>(reportService.dataSourceNames()));
    }

    @RequestMapping(
            value = {"/ws/1.0/reportList"},
            method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity<List<ReportDefinition>> reportList() throws IOException, JRException {
        return ResponseEntity.ok(reportService.reportList());
    }

    @RequestMapping(
            value = {"/ws/1.0/status/{requestId}"},
            method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity<ReportResponse> status(
            @PathVariable("requestId") String requestId
    ) {
        ReportTask detail = reportService.detail(requestId);
        if (detail == null) {//başlamamış
            logManager.info("status query :YOK !!! " + requestId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        logManager.info("status query :" + requestId);
        switch (detail.getStatus()) {
            case WAIT:
                logManager.info(detail.getRequest().getReport() + " status query :" + requestId + " :başlamış");
                return ResponseEntity.status(HttpStatus.CREATED).body(null);

            case RUN:
                logManager.info(detail.getRequest().getReport() + " status query :" + requestId + " :devam ediyor");
                return ResponseEntity.status(HttpStatus.PROCESSING).body(null);

            case EXCEPTION:
                logManager.info(detail.getRequest().getReport() + " status query :" + requestId + " :hata");
                return ResponseEntity.status(420).body(null);// 420 Method Failure

            case FINISH:
                logManager.info(detail.getRequest().getReport() + " status query :" + requestId + " :bitmiş");
                return ResponseEntity.status(HttpStatus.OK).body(null);

            case CANCEL:
                logManager.info(detail.getRequest().getReport() + " status query :" + requestId + " :iptal");
                return ResponseEntity.status(HttpStatus.OK).body(null);

            case SCHEDULED:
                logManager.info(detail.getRequest().getReport() + " status query :" + requestId + " :başlamış");
                return ResponseEntity.status(HttpStatus.CREATED).body(null);
            default:
                throw new IllegalArgumentException(detail.getStatus().name());
        }
    }

    @RequestMapping(
            value = {"/ws/1.0/reports"},
            method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity<List<String>> reports() {
        return ResponseEntity.ok(reportService.reports());
    }

    @RequestMapping(
            value = {"/ws/1.0/detail/{requestId}"},
            method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity<ReportTask> detail(
            @PathVariable("requestId") String requestId
    ) {
        ReportTask task = reportService.detail(requestId);
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
        try {
            reportService.cancel(requestId);
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (NoSuchElementException e) {
            logManager.info("rapor :YOK !!! " + requestId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @RequestMapping(
            value = {"/ws/1.0/output/{requestId}"},
            method = RequestMethod.GET)
    public ResponseEntity<byte[]> output(
            @PathVariable("requestId") String requestId
    ) throws IOException {
        try {
            byte[] output = reportService.output(requestId);
            ReportTask task = reportService.detail(requestId);
            logManager.info("output :" + requestId);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(ReportOutputFormat.valueOf(task.getRequest().getExtension()).getMediaType())
                    .lastModified(task.getEnded())
                    .header("Content-Disposition", "inline; filename=" + requestId + "." + task.getRequest().getExtension())
                    .body(output);
        } catch (NoSuchElementException e) {
            logManager.info("output :YOK !!! " + requestId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @RequestMapping(
            value = {"/ws/1.0/error/{requestId}"},
            method = RequestMethod.GET,
            produces = "text/plain")
    public ResponseEntity<String> error(
            @PathVariable("requestId") String requestId
    ) throws IOException {
        return ResponseEntity.status(HttpStatus.OK).body(reportService.error(requestId));
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
        ReportTask res = reportService.request(req);
        return ResponseEntity.status(res.getStatus().getHttpStatus()).body(res.getResponse());
    }
}
