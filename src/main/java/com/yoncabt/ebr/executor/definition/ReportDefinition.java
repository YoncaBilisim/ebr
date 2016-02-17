/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.executor.definition;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author myururdurmaz
 */
public class ReportDefinition {

    private String caption;
    private List<ReportParam> reportParams = new ArrayList<>();
    private ReportType reportType;
    private String dataSource;
    private String textEncoding;
    private String name;

    public ReportDefinition(String name) {
        this.name = name;
    }

    public List<ReportParam> getReportParams() {
        return reportParams;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * @param reportParams the reportParams to set
     */
    public void setReportParams(List<ReportParam> reportParams) {
        this.reportParams = reportParams;
    }

    /**
     * @return the reportType
     */
    public ReportType getReportType() {
        return reportType;
    }

    /**
     * @param reportType the reportType to set
     */
    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    /**
     * @return the dataSource
     */
    public String getDataSource() {
        return dataSource;
    }

    /**
     * @param dataSource the dataSource to set
     */
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * @return the textEncoding
     */
    public String getTextEncoding() {
        return textEncoding;
    }

    /**
     * @param textEncoding the textEncoding to set
     */
    public void setTextEncoding(String textEncoding) {
        this.textEncoding = textEncoding;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

}
