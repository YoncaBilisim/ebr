/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.abys.report.ws;

import com.yoncabt.abys.core.util.ABYSConf;
import com.yoncabt.abys.report.ReportIDGenerator;
import com.yoncabt.abys.report.ReportRequest;
import com.yoncabt.abys.report.ReportResponse;
import com.yoncabt.abys.report.executor.ReportList;
import com.yoncabt.abys.report.executor.ReportTask;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @RequestMapping(
            value = {"/ws/1.0/request"},
            method = RequestMethod.POST,
            produces = "application/json",
            consumes = "application/json")
    public ResponseEntity<ReportResponse> request(
            @RequestBody(required = true) ReportRequest req
    ) {
        ReportTask task = context.getBean(ReportTask.class);
        if(StringUtils.isBlank(req.getDatasourceName()))
            req.setDatasourceName("default");

        if(StringUtils.isBlank(req.getLocale()))
            req.setLocale(ABYSConf.INSTANCE.getValue("report.locale.default", "tr_TR"));

        req.setUuid(reportIDGenerator.generate());
        task.setRequest(req);
        requestList.add(task);
        if (req.isAsync()) {
            executor.execute(task);
            ReportResponse res = new ReportResponse();
            res.setUuid(req.getUuid());
            if(task.getException() != null) {
                task.getResponse().setExceptionLog(task.getException() + "\n" + ExceptionUtils.getFullStackTrace(task.getException()));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(task.getResponse());
            }
            return ResponseEntity.status(HttpStatus.PROCESSING).body(res);
        } else {
            //hemen çalışacak olanlar buraya
            task.run();
            if(task.getException() != null) {
                task.getResponse().setExceptionLog(task.getException() + "\n" + ExceptionUtils.getFullStackTrace(task.getException()));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(task.getResponse());
            }
            return ResponseEntity.status(HttpStatus.OK).body(task.getResponse());
        }
    }
}
