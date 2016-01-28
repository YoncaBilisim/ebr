/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.abys.report.logger.fs;

import com.yoncabt.abys.core.util.ABYSConf;
import com.yoncabt.abys.report.ReportOutputFormat;
import com.yoncabt.abys.report.logger.ReportLogger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

/**
 *
 * @author myururdurmaz
 */
@Component
public class FileSystemReportLogger implements ReportLogger {

    @Override
    public void logReport(String uuid, Map<String, Object> reportParams, ReportOutputFormat outputFormat, InputStream reportData) throws IOException {
        File saveDir = new File(ABYSConf.INSTANCE.getValue("report.fslogger.path", "/tmp"));
        saveDir.mkdirs();
        boolean compress = ABYSConf.INSTANCE.getValue("report.fslogger.compress", true);
        OutputStream osReport;
        OutputStream osParams;
        if (compress) {
            osReport = new GZIPOutputStream(new FileOutputStream(new File(saveDir, uuid + ".gz")));
            osParams = new GZIPOutputStream(new FileOutputStream(new File(saveDir, uuid + ".json.gz")));
        } else {
            osReport = new FileOutputStream(new File(saveDir, uuid));
            osParams = new FileOutputStream(new File(saveDir, uuid + ".json"));
        }
        IOUtils.copy(reportData, osReport);
        JSONObject jo = new JSONObject(reportParams);
        try (OutputStreamWriter osw = new OutputStreamWriter(osParams, "utf-8")) {
            jo.write(osw);
        }
        osReport.close();
        osParams.close();
    }

    @Override
    public byte[] getReportData(String uuid) throws IOException {
        //burada sıkıştırma özelliği açıkken kpatılmış olabilir diye kontrol yapıyorum
        File saveDir = new File(ABYSConf.INSTANCE.getValue("report.fslogger.path", "/tmp"));
        File reportFile = new File(saveDir, uuid + ".gz");
        if (reportFile.exists()) {
            try (FileInputStream fis = new FileInputStream(reportFile);
                    GZIPInputStream gzis = new GZIPInputStream(fis);) {
                return IOUtils.toByteArray(gzis);
            }
        } else {
            reportFile = new File(saveDir, uuid);
            try (FileInputStream fis = new FileInputStream(reportFile)) {
                return IOUtils.toByteArray(fis);
            }
        }
    }

}
