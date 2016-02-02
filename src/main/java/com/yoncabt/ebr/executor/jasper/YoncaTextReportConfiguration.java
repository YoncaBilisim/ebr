/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.executor.jasper;

import com.yoncabt.abys.core.util.ABYSConf;
import net.sf.jasperreports.export.SimpleTextReportConfiguration;

/**
 *
 * @author myururdurmaz
 */
public class YoncaTextReportConfiguration extends SimpleTextReportConfiguration {

    private String template;

    public YoncaTextReportConfiguration(String template) {
        this.template = template;
    }

    @Override
    public Float getCharWidth() {
        return ABYSConf.INSTANCE.getValue("report.texttemplate." + template + ".CHAR_WITDH", super.getCharWidth());
    }

    @Override
    public Float getCharHeight() {
        return ABYSConf.INSTANCE.getValue("report.texttemplate." + template + ".CHAR_HEIGHT", super.getCharHeight());
    }

    @Override
    public Integer getPageWidthInChars() {
        return ABYSConf.INSTANCE.getValue("report.texttemplate." + template + ".PAGE_WIDTH_CHARS", super.getPageWidthInChars());
    }

    @Override
    public Integer getPageHeightInChars() {
        return ABYSConf.INSTANCE.getValue("report.texttemplate." + template + ".PAGE_HEIGHT_CHARS", super.getPageHeightInChars());
    }

}
