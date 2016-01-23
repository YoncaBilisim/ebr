/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.abys.core.util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author myururdurmaz
 */
public enum ABYSConf {

    INSTANCE;

    private Map<String, String> map;

    private final Object reloadLock;

    private long lastModified = 0;

    private ABYSConf() {
        reloadLock = new Object();
        reload();
    }

    private void reload() {
        synchronized (reloadLock) {
            File confFile = getConfFile();
            Map<String, String> tmp = new HashMap<>();
            if (confFile.exists() && confFile.isFile() && confFile.canRead()) {
                lastModified = confFile.lastModified();
                //System.out.println(confFile.getAbsolutePath() + ": okunuyor");
                try {
                    List<String> lines = FileUtils.readLines(confFile, "utf-8");
                    for (String line : lines) {
                        //System.out.println("ABYSConf:" + line);
                        if (StringUtils.isBlank(line) || line.trim().charAt(0) == '#') {
                            continue;
                        }
                        String[] kv = line.split("=", 2);
                        if (kv.length != 2) {
                            continue;
                        }
                        final String key = kv[0].trim();
                        final String value = kv[1].trim();
                        tmp.put(key, value);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ABYSConf.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                //System.out.println(confFile.getAbsolutePath() + ": OKUNMUYOR");
            }
            map = tmp;
            for (Map.Entry<String, String> entrySet : map.entrySet()) {
                String key = entrySet.getKey();
                String value = entrySet.getValue();
                if (key.startsWith("system.")) {
                    System.setProperty(key.substring("system.".length()), value);
                }
            }
        }
    }

    private File getConfFile() {
        String confFilePath = System.getProperty("abys_conf_file", new File(System.getProperty("user.home"), "abys.conf").getAbsolutePath());
        File confFile = new File(confFilePath);
        return confFile;
    }

    private void reloadIfRequired() {
        File confFile = getConfFile();
        if (confFile.exists() && confFile.isFile() && confFile.canRead() && confFile.lastModified() > lastModified) {
            reload();
        }
    }

    public String getValue(String key, String defaultValue) {
        reloadIfRequired();
        if (map.containsKey(key)) {
            return map.get(key);
        }
        return System.getProperty(key, defaultValue);
    }

    public boolean getValue(String key, boolean defaultValue) {
        reloadIfRequired();
        if (map.containsKey(key)) {
            return Boolean.valueOf(map.get(key));
        }
        return Boolean.valueOf(System.getProperty(key, Boolean.toString(defaultValue)));
    }

    public double getValue(String key, double defaultValue) {
        reloadIfRequired();
        if (map.containsKey(key)) {
            return Double.valueOf(map.get(key));
        }
        return Double.valueOf(System.getProperty(key, Double.toString(defaultValue)));
    }

    public int getValue(String key, int defaultValue) {
        reloadIfRequired();
        if (map.containsKey(key)) {
            return Integer.valueOf(map.get(key));
        }
        return Integer.valueOf(System.getProperty(key, Integer.toString(defaultValue)));
    }

    public long getValue(String key, long defaultValue) {
        reloadIfRequired();
        if (map.containsKey(key)) {
            return Long.valueOf(map.get(key));
        }
        return Integer.valueOf(System.getProperty(key, Long.toString(defaultValue)));
    }

    public Map<String, String> getMap() {
        reloadIfRequired();
        return Collections.unmodifiableMap(map);
    }

}

