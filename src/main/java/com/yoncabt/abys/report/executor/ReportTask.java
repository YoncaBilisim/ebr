/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.abys.report.executor;

import com.yoncabt.abys.report.ReportOutputFormat;
import com.yoncabt.abys.report.ReportRequest;
import com.yoncabt.abys.report.ReportResponse;
import com.yoncabt.abys.report.YoncaJasperReports;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import net.sf.jasperreports.engine.JRException;
import org.apache.tomcat.util.http.fileupload.IOUtils;
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

    @Autowired
    private YoncaJasperReports jasperReports;

    private ReportRequest request;

    private ReportResponse response;

    private Exception exception;

    @Override
    public void run() {
        response = new ReportResponse();
        response.setUuid(request.getUuid());
        Connection con = null;
        try (InputStream is = jasperReports.exportTo(request.getReport(), request.getReportParams(), ReportOutputFormat.valueOf(request.getExtension()), con, request.getUuid());){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(is, baos);
            response.setOutput(baos.toByteArray());//burası rai doldurabilir dikakt etmek lazım. bunun yerine diske bir yere yazıp istenince vermek daha mantıklı olabilir
        } catch (JRException | IOException ex) {
            exception = ex;
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
}
