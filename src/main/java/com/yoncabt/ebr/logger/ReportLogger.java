/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.logger;

import com.yoncabt.ebr.ReportOutputFormat;
import com.yoncabt.ebr.ReportRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author myururdurmaz
 */
public interface ReportLogger {

    void logReport(ReportRequest request,
            ReportOutputFormat outputFormat,
            InputStream reportData) throws IOException;

    byte[] getReportData(String uuid) throws IOException;
}
