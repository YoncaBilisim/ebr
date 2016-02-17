/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.executor.jasper;

import com.yoncabt.abys.core.util.ABYSConf;
import com.yoncabt.ebr.executor.BaseReport;
import com.yoncabt.ebr.executor.definition.ReportDefinition;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author myururdurmaz
 */
public class JasperReport extends BaseReport {


    private String name;

    public JasperReport(String name) {
        this.name = name;
    }

    public ReportDefinition loadDefinition() throws IOException {
        String jsonFileName = name.substring(0, name.lastIndexOf(".jrxml")) + ".ebr.json";
        File reportDir = new File(ABYSConf.INSTANCE.getValue("report.jrxml.path", "/home/myururdurmaz/reports"));
        File jsonFile = new File(reportDir, jsonFileName);
        return loadDefinition(name, jsonFile);
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
