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
}
