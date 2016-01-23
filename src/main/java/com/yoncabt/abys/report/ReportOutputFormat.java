/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.abys.report;

/**
 *
 * @author myururdurmaz
 */
public enum ReportOutputFormat {
    
    pdf("application/pdf"), 
    html("text/html;charset=UTF-8"), 
    xls("application/vnd.ms-excel"), 
    xlsx("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    rtf("text/rtf"), 
    csv("text/csv"), 
    xml("text/xml"), 
    docx("application/vnd.openxmlformats-officedocument.wordprocessingml.document"), 
    odt("application/vnd.oasis.opendocument.tex"),
    ods("application/vnd.oasis.opendocument.spreadsheet"),
    jprint("application/jprint");
    
    private String mimeType;

    private ReportOutputFormat(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }
    
    
}
