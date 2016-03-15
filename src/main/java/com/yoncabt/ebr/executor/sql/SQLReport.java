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
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author myururdurmaz
 */
@Component
@Scope(value = "request")
public class SQLReport extends BaseReport {

    private File file;

    public ReportDefinition loadDefinition(File file) throws IOException {
        this.file = file;
        String jsonFilePath = StringUtils.removeEnd(file.getAbsolutePath(), ".sql") + ".ebr.json";
        File jsonFile = new File(jsonFilePath);
        return super.loadDefinition(file, jsonFile);
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
