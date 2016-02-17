/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.executor.sql;

import com.yoncabt.ebr.executor.BaseReport;
import com.yoncabt.ebr.executor.definition.ReportDefinition;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author myururdurmaz
 */
public class SQLReport extends BaseReport {

    private File file;

    public SQLReport(File file) {
        this.file = file;
    }

    public ReportDefinition loadDefinition() throws IOException {
        String jsonFilePath = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(".sql")) + ".ebr.json";
        File jsonFile = new File(jsonFilePath);
        return loadDefinition(file, jsonFile);
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
