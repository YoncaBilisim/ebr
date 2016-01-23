/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.abys.report.logger;

import com.yoncabt.abys.report.ReportOutputFormat;
import java.io.InputStream;
import java.util.Map;

/**
 *
 * @author myururdurmaz
 */
public interface ReportLogger {
    void logReport(String uuid, Map<String, Object> reportParams, ReportOutputFormat outputFormat, InputStream reportData);
}
