/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.logger;

import com.yoncabt.ebr.ReportOutputFormat;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 *
 * @author myururdurmaz
 */
public interface ReportLogger {

    void logReport(String uuid,
            Map<String, Object> reportParams,
            ReportOutputFormat outputFormat,
            InputStream reportData) throws IOException;

    byte[] getReportData(String uuid) throws IOException;
}
