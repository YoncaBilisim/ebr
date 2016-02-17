/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.executor.jasper;

import com.yoncabt.abys.core.util.ABYSConf;
import com.yoncabt.ebr.executor.BaseReport;
import com.yoncabt.ebr.executor.definition.ReportDefinition;
import com.yoncabt.ebr.executor.definition.ReportParam;
import com.yoncabt.ebr.executor.definition.ReportType;
import java.io.File;
import java.io.IOException;
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
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.JRElementsVisitor;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

/**
 *
 * @author myururdurmaz
 */
public class JasperReport extends BaseReport {

    private File file;

    public JasperReport(File file) {
        this.file = file;
    }

    public ReportDefinition loadDefinition() throws IOException, JRException {
        String jsonFileName = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(".jrxml")) + ".ebr.json";
        File jsonFile;
        jsonFile = new File(jsonFileName);
        final ReportDefinition ret = loadDefinition(file, jsonFile);
        ret.setReportType(ReportType.JASPER);

        net.sf.jasperreports.engine.JasperReport jasperReport = (net.sf.jasperreports.engine.JasperReport) JRLoader.loadObject(compileIfRequired(file));
        for (JRParameter param : jasperReport.getParameters()) {
            if (!param.isForPrompting()) {
                continue;
            }
            ReportParam rp = new ReportParam(param.getValueClass());
            rp.setName(param.getName());
            rp.setLabel(param.getName());
            ret.getReportParams().add(rp);
        }

        return ret;
    }

    public static File getJasperFile(String fileName) {
        File jasperBase = new File(ABYSConf.INSTANCE.getValue("report.jrxml.path", "/usr/local/reports"));
        File jasperFile = new File(jasperBase, fileName.replace(".jrxml", ".jasper"));
        return jasperFile;
    }

    public static File getReportFile(String fileName) {
        File jrxmlBase = new File(ABYSConf.INSTANCE.getValue("report.jrxml.path", "/usr/local/reports"));
        File jrxmlFile = new File(jrxmlBase, fileName);
        return jrxmlFile;
    }

    public static synchronized File compileIfRequired(File jrxmlFile) throws JRException {
        System.out.println(jrxmlFile + " compile");
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

}
