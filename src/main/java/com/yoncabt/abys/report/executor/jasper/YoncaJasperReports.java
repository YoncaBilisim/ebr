/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.abys.report.executor.jasper;

import com.yoncabt.abys.core.util.ABYSConf;
import com.yoncabt.abys.report.ReportOutputFormat;
import com.yoncabt.abys.report.jdbcbridge.YoncaConnection;
import com.yoncabt.abys.report.logger.fs.FileSystemReportLogger;
import com.yoncabt.abys.report.logger.ReportLogger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Singleton;
import net.sf.jasperreports.crosstabs.JRCrosstab;
import net.sf.jasperreports.engine.JRBreak;
import net.sf.jasperreports.engine.JRChart;
import net.sf.jasperreports.engine.JRComponentElement;
import net.sf.jasperreports.engine.JRElementGroup;
import net.sf.jasperreports.engine.JREllipse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRFrame;
import net.sf.jasperreports.engine.JRGenericElement;
import net.sf.jasperreports.engine.JRImage;
import net.sf.jasperreports.engine.JRLine;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRRectangle;
import net.sf.jasperreports.engine.JRStaticText;
import net.sf.jasperreports.engine.JRSubreport;
import net.sf.jasperreports.engine.JRTextField;
import net.sf.jasperreports.engine.JRVisitor;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
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
import net.sf.jasperreports.engine.util.JRElementsVisitor;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.ExporterOutput;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import oracle.jdbc.OracleDriver;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author myururdurmaz
 */
@Singleton
@Component
public class YoncaJasperReports {
    public static void main(String[] args) {
        try {
            System.setProperty("report.jrmxl.path", "/home/myururdurmaz/reports");
            System.setProperty("report.jasper.path", "/home/myururdurmaz/reports");
            System.setProperty("report.out.path", "/tmp");

            YoncaJasperReports jasperReports = new YoncaJasperReports();
            jasperReports.reportLogger = new FileSystemReportLogger();

            Map<String, Object> params = new HashMap<>();
            params.put("CORP_NAME", "DENEM A.Ş");
            params.put("LANG", 1);
            params.put("LOGO_PATH", "/tmp/logo.png");
            params.put("OPTIONAL_PARAMETER", " and rownum = 1");
            params.put("TITLE_ONE", "Başılk 1");
            params.put("TITLE_TWO", "balık 2");

            DriverManager.registerDriver(new OracleDriver());
            YoncaConnection con = new YoncaConnection(
                    DriverManager.getConnection("jdbc:oracle:thin:@localhost:41521:yonca", "SMS_TEST", "SMS"));
            try (InputStream exportTo = jasperReports.exportTo("İş Emri Raporları/Kelepce Muhur Raporu/Kelepce_Muhur_Raporu.jrxml", params, ReportOutputFormat.odt, con, "en_US", "deneme rapor " + System.currentTimeMillis());
                    FileOutputStream fos = new FileOutputStream("/tmp/rapor1")) {
                IOUtils.copy(exportTo, fos);
            }
        } catch (Exception ex) {
            Logger.getLogger(YoncaJasperReports.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Autowired
    private ReportLogger reportLogger;

    public InputStream exportTo(
            String reportName,
            Map<String, Object> params,
            ReportOutputFormat outputFormat,
            YoncaConnection connection,
            String locale,
            String uuid) throws JRException, IOException {

        // önce genel parametreleri dolduralım. logo_path falan gibi
        ABYSConf.INSTANCE.getMap().entrySet().stream().forEach((es) -> {
            String key = es.getKey();
            if (key.startsWith("report.params.")) {
                key = key.substring("report.params.".length());
                String value = es.getValue();
                params.put(key, value);
            }
        });
        params.put("__extension", outputFormat.name());
        params.put("__start_time", System.currentTimeMillis());

        File jrxmlFile = getJrxmlFile(reportName);
        //alttaki satır tehlikeli olabilir mi ?
        File resourceFile = new File(jrxmlFile.getParentFile(), "messages_" + locale + ".properties");
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
        params.put(JRParameter.REPORT_LOCALE, new Locale("tr_TR"));

        JasperPrint jasperPrint = JasperFillManager.fillReport(
                compileIfRequired(reportName).getAbsolutePath(),
                /*jasper parametreleri dğeiştiriyor*/ new HashMap<>(params),
                connection);

        File outBase = new File(ABYSConf.INSTANCE.getValue("report.out.path", "/usr/local/reports/out"));
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
                output = new SimpleOutputStreamExporterOutput(exportReportFile);
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
                output = new SimpleOutputStreamExporterOutput(exportReportFile);
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
                putTextParams((JRTextExporter)exporter, params, "SUITABLE");
                break;
            default:
                throw new AssertionError(outputFormat.toString() + " not supported");
        }
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));

        exporter.setExporterOutput(output);
        exporter.exportReport();

        try (FileInputStream fis = new FileInputStream(exportReportFile)) {
            reportLogger.logReport(uuid, params, outputFormat, fis);
        }

        try {
            return new FileInputStream(exportReportFile);
        } finally {
            exportReportFile.delete();// bu hile sadece linuxta çalışır :D. linux stream kapatılana kadar dosyayı tutacaktır.
        }
    }

    private synchronized File compileIfRequired(String fileName) throws JRException {
        File jrxmlFile = getJrxmlFile(fileName);
        File jasperFile = getJasperFile(fileName);
        jasperFile.getParentFile().mkdirs();
        if (jrxmlFile.lastModified() > jasperFile.lastModified()) {
            JasperDesign jasperDesign = JRXmlLoader.load(jrxmlFile.getAbsolutePath());
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            JRSaver.saveObject(jasperReport, jasperFile.getAbsolutePath());
            //toLog("Saving compiled report to: " + jasperFile.getAbsolutePath());
            //Compile sub reports
            JRElementsVisitor.visitReport(jasperReport, new JRVisitor() {

                @Override
                public void visitBreak(JRBreak breakElement) {
                }

                @Override
                public void visitChart(JRChart chart) {
                }

                @Override
                public void visitCrosstab(JRCrosstab crosstab) {
                }

                @Override
                public void visitElementGroup(JRElementGroup elementGroup) {
                }

                @Override
                public void visitEllipse(JREllipse ellipse) {
                }

                @Override
                public void visitFrame(JRFrame frame) {
                }

                @Override
                public void visitImage(JRImage image) {
                }

                @Override
                public void visitLine(JRLine line) {
                }

                @Override
                public void visitRectangle(JRRectangle rectangle) {
                }

                @Override
                public void visitStaticText(JRStaticText staticText) {
                }

                @Override
                public void visitSubreport(JRSubreport subreport) {
                    String expression = subreport.getExpression().getText().replace(".jasper", "");

                    StringTokenizer st = new StringTokenizer(expression, "\"/");
                    String subReportName = null;
                    while (st.hasMoreTokens()) {
                        subReportName = st.nextToken();
                    }
                    try {
                        //Sometimes the same subreport can be used multiple times, but
                        //there is no need to compile multiple times
                        // burada tam path bulmak gerekebilir
                        compileIfRequired(subReportName + ".jrxml");
                    } catch (JRException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public void visitTextField(JRTextField textField) {
                }

                @Override
                public void visitComponentElement(JRComponentElement componentElement) {
                }

                @Override
                public void visitGenericElement(JRGenericElement element) {
                }

            });
            JasperCompileManager.compileReportToFile(
                    jrxmlFile.getAbsolutePath(),
                    jasperFile.getAbsolutePath());
        }
        return jasperFile;
    }

    private File getJasperFile(String fileName) {
        File jasperBase = new File(ABYSConf.INSTANCE.getValue("report.jasper.path", "/usr/local/reports"));
        File jasperFile = new File(jasperBase, fileName.replace(".jrxml", ".jasper"));
        return jasperFile;
    }

    private File getJrxmlFile(String fileName) {
        File jrxmlBase = new File(ABYSConf.INSTANCE.getValue("report.jrmxl.path", "/usr/local/reports"));
        File jrxmlFile = new File(jrxmlBase, fileName);
        return jrxmlFile;
    }


    private void putTextParams(JRTextExporter exporter, Map<String, Object> params, String textTemplate) {
        ABYSConf.INSTANCE.getMap().entrySet().stream().forEach((entry) -> {
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
