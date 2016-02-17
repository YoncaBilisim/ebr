/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr;

/**
 *
 * @author myururdurmaz
 */
public enum ReportOutputFormat {

    pdf("application/pdf", false),
    html("text/html;charset=UTF-8", false),
    xls("application/vnd.ms-excel", false),
    xlsx("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", false),
    rtf("text/rtf", false),
    csv("text/csv", true),
    xml("text/xml", false),
    txt("text/plain", true),
    docx("application/vnd.openxmlformats-officedocument.wordprocessingml.document", false),
    odt("application/vnd.oasis.opendocument.tex", false),
    ods("application/vnd.oasis.opendocument.spreadsheet", false),
    jprint("application/jprint", true);

    private String mimeType;
    private boolean text;

    private ReportOutputFormat(String mimeType, boolean text) {
        this.mimeType = mimeType;
        this.text = text;
    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isText() {
        return text;
    }

}
