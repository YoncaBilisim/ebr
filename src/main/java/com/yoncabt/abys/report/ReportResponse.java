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
public class ReportResponse {

    /**
     * rapora özel uuid. loglamada ve asenkron çalışmada kullanılacak
     */
    private String uuid;
    /**
     * rapor çıktısı. veya urlden de istenebilir
     */
    private byte[] output;
    /**
     * hatanın logu
     */
    private String exceptionLog;

    /**
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the output
     */
    public byte[] getOutput() {
        return output;
    }

    /**
     * @param output the output to set
     */
    public void setOutput(byte[] output) {
        this.output = output;
    }

    /**
     * @return the exceptionLog
     */
    public String getExceptionLog() {
        return exceptionLog;
    }

    /**
     * @param exceptionLog the exceptionLog to set
     */
    public void setExceptionLog(String exceptionLog) {
        this.exceptionLog = exceptionLog;
    }
}
