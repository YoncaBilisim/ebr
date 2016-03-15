/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.executor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import org.springframework.stereotype.Component;

/**
 *
 * @author myururdurmaz
 */
@Singleton
@Component
public class ReportList {

    private final List<ReportTask> requestList = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, ReportTask> requestMap = Collections.synchronizedMap(new HashMap<>());

    public void add(ReportTask task) {
        synchronized(requestList) {
            clear();
            requestMap.put(task.getRequest().getUuid(), task);
            requestList.add(task);
        }
    }

    public ReportTask get(String uuid) {
        return requestMap.get(uuid);
    }

    public ReportTask remove(String uuid) {
        return requestMap.remove(uuid);
    }

    public ReportTask get(int id) {
        return requestList.get(id);
    }

    public List<ReportTask> get() {
        return Collections.unmodifiableList(requestList);
    }

    public int count() {
        return requestList.size();
    }

    public List<String> getAllIds() {
        return new ArrayList<>(requestMap.keySet());
    }

    public void clear() {
        List<ReportTask> toBoRemove = new ArrayList<>();
        for (ReportTask task : requestList) {
            if(task.isSentToClient()) {
                toBoRemove.add(task);
            }
        }
        for (ReportTask task : toBoRemove) {
            requestMap.remove(task.getRequest().getUuid());
        }
        requestList.removeAll(toBoRemove);
    }
}
