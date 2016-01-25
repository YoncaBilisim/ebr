/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.abys.report;

import com.yoncabt.abys.report.executor.ReportTask;
import com.yoncabt.abys.report.executor.ReportList;
import java.util.UUID;
import javax.ws.rs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author myururdurmaz
 */
@RestController
@Path(value = "/reportWs")
public class ReportWS {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private TaskExecutor executor;

    @Autowired
    private ReportList requestList;

    @RequestMapping(value = {
        "/request"
    }, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<ReportResponse> request(ReportRequest req) {
        ReportTask task = context.getBean(ReportTask.class);
        req.setUuid(UUID.randomUUID().toString());
        requestList.add(task);
        if (req.isAsync()) {
            task.setRequest(req);
            executor.execute(task);
            ReportResponse res = new ReportResponse();
            res.setUuid(req.getUuid());
            return ResponseEntity.status(HttpStatus.PROCESSING).body(res);
        } else {
            //hemen çalışacak olanlar buraya
            task.run();
            return ResponseEntity.status(HttpStatus.OK).body(task.getResponse());
        }
    }
}
