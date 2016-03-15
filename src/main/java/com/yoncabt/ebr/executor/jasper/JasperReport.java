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
import com.yoncabt.ebr.executor.BaseReport;
import com.yoncabt.ebr.executor.definition.ReportDefinition;
import com.yoncabt.ebr.executor.definition.ReportParam;
import com.yoncabt.ebr.executor.definition.ReportType;
import com.yoncabt.ebr.jdbcbridge.pool.EBRConnection;
import com.yoncabt.ebr.logger.ReportLogger;
import com.yoncabt.ebr.util.ASCIIFier;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import net.sf.jasperreports.crosstabs.JRCrosstab;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
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
import net.sf.jasperreports.engine.fill.JRAbstractLRUVirtualizer;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.jasperreports.engine.util.JRElementsVisitor;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.ExporterOutput;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author myururdurmaz
 */
@Component
@Scope(value = "request")
public class JasperReport extends BaseReport {

    private static FLogManager logManager = FLogManager.getLogger(JasperReport.class);

    private File file;

    @Autowired
    private ReportLogger reportLogger;

    public File getJasperFile(String fileName) {
        File jasperBase = new File(EBRConf.INSTANCE.getValue(EBRParams.REPORTS_JRXML_PATH, "/usr/local/reports"));
        File jasperFile = new File(jasperBase, fileName.replace(".jrxml", ".jasper"));
        return jasperFile;
    }

    public static File getReportFile(String fileName) {
        File jrxmlBase = new File(EBRConf.INSTANCE.getValue(EBRParams.REPORTS_JRXML_PATH, "/usr/local/reports"));
        File jrxmlFile = new File(jrxmlBase, fileName);
        return jrxmlFile;
    }

    public static synchronized File compileIfRequired(File jrxmlFile) throws JRException {
        logManager.info(jrxmlFile + " compile");
        File jasperFile = new File(jrxmlFile.getAbsolutePath().replace(".jrxml", ".jasper"));
        jasperFile.getParentFile().mkdirs();
        if (jrxmlFile.lastModified() > jasperFile.lastModified()) {
            JasperDesign jasperDesign = JRXmlLoader.load(jrxmlFile.getAbsolutePath());
            net.sf.jasperreports.engine.JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
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
                    String subReportName = subreport.getExpression().getText().replace("repo:", "");
                    File subReportFile = new File(jrxmlFile.getParentFile(), subReportName);
                    try {
                        //Sometimes the same subreport can be used multiple times, but
                        //there is no need to compile multiple times
                        // burada tam path bulmak gerekebilir
                        compileIfRequired(subReportFile.getAbsoluteFile());
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

    public ReportDefinition loadDefinition(File file) throws IOException, JRException {
        this.file = file;
        String jsonFileName = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(".jrxml")) + ".ebr.json";
        File jsonFile;
        jsonFile = new File(jsonFileName);
        final ReportDefinition ret = super.loadDefinition(file, jsonFile);
        ret.setReportType(ReportType.JASPER);

        net.sf.jasperreports.engine.JasperReport jasperReport = (net.sf.jasperreports.engine.JasperReport) JRLoader.loadObject(compileIfRequired(file));
        for (JRParameter param : jasperReport.getParameters()) {
            if (!param.isForPrompting() || param.isSystemDefined()) {
                continue;
            }
            ReportParam rp = new ReportParam(param.getValueClass());
            rp.setName(param.getName());
            if (param.getDefaultValueExpression() != null && StringUtils.isNotBlank(param.getDefaultValueExpression().getText())) {
                // FIXME alttaki değer script olabilir çalıştırılması gerekebilir
                rp.setDefaultValue(Convert.to(StringUtils.strip(param.getDefaultValueExpression().getText(), "\""), param.getValueClass()));
            }
            rp.setLabel(param.getName());
            ret.getReportParams().add(rp);
        }

        return ret;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    public void exportTo(
            ReportRequest request,
            ReportOutputFormat outputFormat,
            EBRConnection connection,
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
        params.put(JRParameter.REPORT_LOCALE, LocaleUtils.toLocale(locale));

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

        String virtDir = EBRConf.INSTANCE.getValue(EBRParams.REPORTS_VIRTUALIZER_DIRECTORY, "/tmp/ebr/virtualizer");
        int maxSize = EBRConf.INSTANCE.getValue(EBRParams.REPORTS_VIRTUALIZER_MAXSIZE, Integer.MAX_VALUE);
        JRAbstractLRUVirtualizer virtualizer = new JRFileVirtualizer(maxSize, virtDir);
        params.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

        net.sf.jasperreports.engine.JasperReport jasperReport = (net.sf.jasperreports.engine.JasperReport) JRLoader.loadObject(com.yoncabt.ebr.executor.jasper.JasperReport.compileIfRequired(jrxmlFile));
        for (JRParameter param : jasperReport.getParameters()) {
            Object val = params.get(param.getName());
            if (val == null) {
                continue;
            }
            params.put(param.getName(), Convert.to(val, param.getValueClass()));
        }
        reportLogger.logReport(request, outputFormat, new ByteArrayInputStream(new byte[0]));

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
                putTextParams((JRTextExporter) exporter, params, reportDefinition.getTextTemplate());
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
