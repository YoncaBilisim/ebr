/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.abys.report;

import java.util.List;
import javax.ws.rs.Path;
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

    @RequestMapping(value = {
        "/request"
    }, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<ReportResponse>> request(ReportRequest rr) {
        return null;
    }
}
