/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr;

import org.springframework.http.MediaType;

/**
 *
 * @author myururdurmaz
 */
public enum ReportOutputFormat {

    pdf("application/pdf", "Adobe Pdf (.pdf)", false),
    html("text/html;charset=UTF-8", "HTML", false),
    xls("application/vnd.ms-excel", "Microsoft Excel (.xls)", false),
    xlsx("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Microsoft Excel (.xlsx)", false),
    rtf("text/rtf", "Rich Text File (.rtf)", false),
    csv("text/csv", "CSV", true),
    xml("text/xml", "XML", false),
    txt("text/plain", "TXT", true),
    docx("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "Microsoft Word (.docx)", false),
    odt("application/vnd.oasis.opendocument.tex", "Open Office (.odt)", false),
    ods("application/vnd.oasis.opendocument.spreadsheet", "Open Office (.ods)", false),
    jprint("application/jprint", "JRPRINT", true);

    private String mimeType;
    private String typeName;
    private boolean text;

    private ReportOutputFormat(String mimeType, String typeName, boolean text) {
        this.mimeType = mimeType;
        this.typeName = typeName;
        this.text = text;
    }

    public String getMimeType() {
        return mimeType;
    }

    public MediaType getMediaType() {
        return MediaType.parseMediaType(mimeType);
    }

    public boolean isText() {
        return text;
    }

    public String getTypeName() {
        return typeName;
    }
}
