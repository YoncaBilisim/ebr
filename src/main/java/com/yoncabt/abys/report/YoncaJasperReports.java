/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.abys.report;

import com.yoncabt.abys.core.util.ABYSConf;
import com.yoncabt.abys.report.logger.ReportLogger;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Map;
import javax.inject.Singleton;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author myururdurmaz
 */
@Singleton
public class YoncaJasperReports {

    @Autowired
    private ReportLogger reportLogger;

    public InputStream exportTo(
            String reportName,
            Map<String, Object> params,
            ReportOutputFormat outputFormat,
            Connection connection,
            String uuid) throws JRException, IOException {

        JasperPrint jasperPrint = JasperFillManager.fillReport(compileIfRequired(reportName).getAbsolutePath(), params, connection);

        Exporter exporter;
        switch (outputFormat) {
            case pdf:
                exporter = new JRPdfExporter();
                break;
            case html:
                exporter = new HtmlExporter();
                break;
            case xls:
                exporter = new JRXlsExporter();
                break;
            case xlsx:
                exporter = new JRXlsxExporter();
                break;
            case rtf:
                exporter = new JRRtfExporter();
                break;
            case csv:
                exporter = new JRCsvExporter();
                break;
            case xml:
                exporter = new JRXmlExporter();
                break;
            case docx:
                exporter = new JRDocxExporter();
                break;
            case odt:
                exporter = new JROdtExporter();
                break;
            case ods:
                exporter = new JROdsExporter();
                break;
            case jprint:
                exporter = new JRTextExporter();
                break;
            default:
                throw new AssertionError(outputFormat.toString() + " not supported");
        }
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        File outBase = new File(ABYSConf.INSTANCE.getValue("report.out.path", "/usr/local/reports/out"));

        File exportReportFile = new File(outBase, uuid + "." + outputFormat.name());

        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(exportReportFile));
        exporter.exportReport();

        try (FileInputStream fis = new FileInputStream(exportReportFile)) {
            reportLogger.logReport(uuid, params, outputFormat, fis);
        }

        return new FileInputStream(exportReportFile);
    }

    private synchronized File compileIfRequired(String fileName) throws JRException {
        File jrxmlBase = new File(ABYSConf.INSTANCE.getValue("report.jrmxl.path", "/usr/local/reports"));
        File jasperBase = new File(ABYSConf.INSTANCE.getValue("report.jasper.path", "/usr/local/reports"));
        File jrxmlFile = new File(jrxmlBase, fileName);
        File jasperFile = new File(jasperBase, fileName.replace(".jrxml", ".jasper"));
        if (jrxmlFile.lastModified() > jasperFile.lastModified()) {
            JasperCompileManager.compileReportToFile(
                    jrxmlFile.getAbsolutePath(),
                    jasperFile.getAbsolutePath());
        }
        return jasperFile;
    }
}
