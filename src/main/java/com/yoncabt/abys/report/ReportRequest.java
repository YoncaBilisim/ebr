/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.abys.report;

import java.util.HashMap;
import java.util.Map;

/**
 * rapor isteği için request sınıfı
 * @author myururdurmaz
 */
public class ReportRequest {

    private Map<String, Object> reportParams = new HashMap<>();
    /**
     * eğer <code>true</code> ise asenkron çalışacakltır. değilse hemen çıktı üretecektir
     */
    private boolean async;
    /**
     * eğer emaila dresi varsa bitince mail atılabilir
     */
    private String email;
    /**
     * zamanlanmış rapor için kullanılacak
     */
    private long scheduleTime;
    /**
     * bunu birden fazla veritabanında çalıştırabilmek için kullanqcağım.<br />
     * eğer verilmezse default isimli kullanılsın<br />
     * <br />
     * abys.conf içerisinde<br />
     * <pre>    report.datasource.datasourceName.url=veritabanı urlsi</pre>
     * <pre>    report.datasource.datasourceName.user=veritabanı kullanıcı</pre>
     * <pre>    report.datasource.datasourceName.pass=veritabanı şifre</pre>
     * hatta farklı veritabanları da kullanılabilsin
     * <pre>    report.datasource.datasourceName.driver=jdbc driver</pre>
     */
    private String datasourceName;

    /**
     * hangi rapor ?
     */
    private String report;

    /**
     * dosya türünü belirlemek için. pdf, xls ...
     */
    private String extension;

    private String uuid;

    private String locale;

    /**
     * @return the reportParams
     */
    public Map<String, Object> getReportParams() {
        return reportParams;
    }

    /**
     * @param reportParams the reportParams to set
     */
    public void setReportParams(Map<String, Object> reportParams) {
        this.reportParams = reportParams;
    }

    /**
     * @return the async
     */
    public boolean isAsync() {
        return async;
    }

    /**
     * @param async the async to set
     */
    public void setAsync(boolean async) {
        this.async = async;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the scheduleTime
     */
    public long getScheduleTime() {
        return scheduleTime;
    }

    /**
     * @param scheduleTime the scheduleTime to set
     */
    public void setScheduleTime(long scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    /**
     * @return the datasourceName
     */
    public String getDatasourceName() {
        return datasourceName;
    }

    /**
     * @param datasourceName the datasourceName to set
     */
    public void setDatasourceName(String datasourceName) {
        this.datasourceName = datasourceName;
    }

    /**
     * @return the report
     */
    public String getReport() {
        return report;
    }

    /**
     * @param report the report to set
     */
    public void setReport(String report) {
        this.report = report;
    }

    /**
     * @return the extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * @param extension the extension to set
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }

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
     * @return the locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * @param locale the locale to set
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }
}
