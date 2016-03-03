/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.executor.jasper;

import com.yoncabt.abys.core.util.EBRConf;
import com.yoncabt.abys.core.util.EBRParams;
import com.yoncabt.abys.core.util.log.FLogManager;
import com.yoncabt.ebr.ReportOutputFormat;
import com.yoncabt.ebr.ReportRequest;
import com.yoncabt.ebr.executor.definition.ReportDefinition;
import com.yoncabt.ebr.jdbcbridge.YoncaConnection;
import com.yoncabt.ebr.logger.ReportLogger;
import com.yoncabt.ebr.util.ASCIIFier;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.inject.Singleton;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
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
import net.sf.jasperreports.engine.fill.JRAbstractLRUVirtualizer;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.ExporterOutput;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author myururdurmaz
 */
@Singleton
@Component
public class YoncaJasperReports {

    @Autowired
    private ReportLogger reportLogger;
    
    private static FLogManager logManager = FLogManager.getLogger(YoncaJasperReports.class);

    /**
     * new Locale("tr_TR") not running for decimal formats
     *
     * @param locale
     * @return
     */
    private Locale parseLocale(String locale) {
        String[] p = locale.split("_");
        return new Locale(p[0], p[1]);
    }

    public void exportTo(
            ReportRequest request,
            ReportOutputFormat outputFormat,
            YoncaConnection connection,
            ReportDefinition reportDefinition) throws JRException, IOException {
        Map<String, Object> params = request.getReportParams();
        String locale = request.getLocale();
        String uuid = request.getUuid();

        // önce genel parametreleri dolduralım. logo_path falan gibi
        EBRConf.INSTANCE.getMap().entrySet().stream().forEach((es) -> {
            String key = es.getKey();
            if (key.startsWith("report.jrproperties.")) {
                key = key.substring("report.jrproperties.".length());
                String value = es.getValue();
                DefaultJasperReportsContext.getInstance().setProperty(key, value);
            }
            if (key.startsWith("report.params.")) {
                key = key.substring("report.params.".length());
                String value = es.getValue();
                params.put(key, value);
            }
        });
        params.put("__extension", outputFormat.name());
        params.put("__start_time", System.currentTimeMillis());

        File jrxmlFile = reportDefinition.getFile();
        //alttaki satır tehlikeli olabilir mi ?
        String resourceFileName = "messages_" + locale + ".properties";
        try {
            File resourceFile = new File(jrxmlFile.getParentFile(), resourceFileName);
            Properties properties = new Properties();
            try (FileInputStream fis = new FileInputStream(resourceFile)) {
                properties.load(fis);
            }
            ResourceBundle rb = new ResourceBundle() {

                @Override
                protected Object handleGetObject(String key) {
                    return properties.get(key);
                }
 
                @Override
                public Enumeration<String> getKeys() {
                    return (Enumeration<String>) ((Enumeration<?>) properties.keys());
                }
            };
            // FIXME yerelleştime dosyaları buradan okunacak
            params.put(JRParameter.REPORT_RESOURCE_BUNDLE, rb);
        } catch (FileNotFoundException e) {
            logManager.info(resourceFileName + " file does not found!");
        }
        
        params.put(JRParameter.REPORT_LOCALE, parseLocale(locale));

        String virtDir = EBRConf.INSTANCE.getValue(EBRParams.REPORTS_VIRTUALIZER_DIRECTORY, "/tmp/ebr/virtualizer");
        int maxSize = EBRConf.INSTANCE.getValue(EBRParams.REPORTS_VIRTUALIZER_MAXSIZE, Integer.MAX_VALUE);
        JRAbstractLRUVirtualizer virtualizer = new JRFileVirtualizer(maxSize, virtDir);
        params.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(com.yoncabt.ebr.executor.jasper.JasperReport.compileIfRequired(jrxmlFile));
        for (JRParameter param : jasperReport.getParameters()) {
            Object val = params.get(param.getName());
            if (val == null) {
                continue;
            }
            params.put(param.getName(), Convert.to(val, param.getValueClass()));
        }

        JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport,
                /*jasper parametreleri dğeiştiriyor*/ new HashMap<>(params),
                connection);

        File outBase = new File(EBRConf.INSTANCE.getValue(EBRParams.REPORTS_OUT_PATH, "/usr/local/reports/out"));
        outBase.mkdirs();
        File exportReportFile = new File(outBase, uuid + "." + outputFormat.name());
        Exporter exporter;
        ExporterOutput output;
        switch (outputFormat) {
            case pdf:
                exporter = new JRPdfExporter();
                output = new SimpleOutputStreamExporterOutput(exportReportFile);
                break;
            case html:
                exporter = new HtmlExporter();
                output = new SimpleHtmlExporterOutput(exportReportFile);
                break;
            case xls:
                exporter = new JRXlsExporter();
                output = new SimpleOutputStreamExporterOutput(exportReportFile);
                break;
            case xlsx:
                exporter = new JRXlsxExporter();
                output = new SimpleOutputStreamExporterOutput(exportReportFile);
                break;
            case rtf:
                exporter = new JRRtfExporter();
                output = new SimpleWriterExporterOutput(exportReportFile);
                break;
            case csv:
                exporter = new JRCsvExporter();
                output = new SimpleOutputStreamExporterOutput(exportReportFile);
                break;
            case xml:
                exporter = new JRXmlExporter();
                output = new SimpleOutputStreamExporterOutput(exportReportFile);
                break;
            case docx:
                exporter = new JRDocxExporter();
                output = new SimpleOutputStreamExporterOutput(exportReportFile);
                break;
            case odt:
                exporter = new JROdtExporter();
                output = new SimpleOutputStreamExporterOutput(exportReportFile);
                break;
            case ods:
                exporter = new JROdsExporter();
                output = new SimpleOutputStreamExporterOutput(exportReportFile);
                break;
            case jprint:
            case txt:
                exporter = new JRTextExporter();
                output = new SimpleWriterExporterOutput(exportReportFile);
                putTextParams((JRTextExporter) exporter, params, "SUITABLE");
                break;
            default:
                throw new AssertionError(outputFormat.toString() + " not supported");
        }
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));

        exporter.setExporterOutput(output);
        exporter.exportReport();
        if (outputFormat.isText() && !"utf-8".equals(reportDefinition.getTextEncoding())) {
            String reportData = FileUtils.readFileToString(exportReportFile, "utf-8");
            if ("ascii".equals(reportDefinition.getTextEncoding())) {
                FileUtils.write(exportReportFile, ASCIIFier.ascii(reportData));
            } else {
                FileUtils.write(exportReportFile, reportData, reportDefinition.getTextEncoding());
            }
        }

        try (FileInputStream fis = new FileInputStream(exportReportFile)) {
            reportLogger.logReport(request, outputFormat, fis);
        }
        exportReportFile.delete();
    }

    private void putTextParams(JRTextExporter exporter, Map<String, Object> params, String textTemplate) {
        EBRConf.INSTANCE.getMap().entrySet().stream().forEach((entry) -> {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.startsWith("report.texttemplate." + textTemplate)) {
                String jrKey = key.replace("report.texttemplate." + textTemplate + ".", "");
                params.put(jrKey, value);
            }
        });
        exporter.setConfiguration(new YoncaTextExporterConfiguration(textTemplate));
        exporter.setConfiguration(new YoncaTextReportConfiguration(textTemplate));
    }
}
