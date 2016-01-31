/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.abys.report.executor.jasper;

import com.yoncabt.abys.core.util.ABYSConf;
import net.sf.jasperreports.export.TextExporterConfiguration;
import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author myururdurmaz
 */
public class YoncaTextExporterConfiguration implements TextExporterConfiguration {

    private String template;

    public YoncaTextExporterConfiguration(String template) {
        this.template = template;
    }

    @Override
    public String getPageSeparator() {
        return StringEscapeUtils.unescapeJava(ABYSConf.INSTANCE.getValue("report.texttemplate." + template + ".BETWEEN_PAGES_TEXT", "\n\n"));
    }

    @Override
    public String getLineSeparator() {
        return StringEscapeUtils.unescapeJava(ABYSConf.INSTANCE.getValue("report.texttemplate." + template + ".LINE_SEPARATOR", "\n"));
    }

    @Override
    public Boolean isTrimLineRight() {
        return true;
    }

    @Override
    public Boolean isOverrideHints() {
        return true;//emin dğeilim
    }

}
