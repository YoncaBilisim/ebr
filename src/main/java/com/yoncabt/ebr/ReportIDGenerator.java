/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 *
 * @author myururdurmaz
 */
@Component
public class ReportIDGenerator {
    private AtomicLong counter = new AtomicLong();
    public String generate() {
        String key = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        key = String.format("%s.%07d", key, counter.incrementAndGet());
        return key;
    }
}
