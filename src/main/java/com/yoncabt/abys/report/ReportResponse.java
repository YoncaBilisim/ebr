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
}