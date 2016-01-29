/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.abys.core.util;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    private Connection connection;

    private String tableName;
    private String tableKeyColumn;
    private String tableValueColumn;

    private ABYSConf() {
        reloadLock = new Object();
        reload();
    }

    private void reload() {
        // 2 defa çalışmasın burası
        synchronized (reloadLock) {
            File confFile = getConfFile();
            Map<String, String> tmp = new HashMap<>();
            if (confFile.exists() && confFile.isFile() && confFile.canRead()) {
                lastModified = confFile.lastModified();
                try {
                    List<String> lines = FileUtils.readLines(confFile, "utf-8");
                    for (String line : lines) {
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
            reconnectToDb(tmp);
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

    /**
     * veritabanına tekrar bağlanır
     *
     * @param tmp bilgilerin okunacağı map
     */
    private void reconnectToDb(Map<String, String> tmp) {
        if (tmp.containsKey("abysconf.connection.url")) {
            try {
                if (connection != null) {
                    connection.close();
                }
                DriverManager.registerDriver((Driver) Class.forName(tmp.get("abysconf.connection.driver")).newInstance());
                connection = DriverManager.getConnection(tmp.get("abysconf.connection.url"), tmp.get("abysconf.connection.user"), tmp.get("abysconf.connection.pass"));
                tableName = tmp.get("abysconf.connection.tableName");
                tableKeyColumn = tmp.get("abysconf.connection.tableKeyColumn");
                tableValueColumn = tmp.get("abysconf.connection.tableValueColumn");
            } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(ABYSConf.class.getName()).log(Level.SEVERE, null, ex);
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
        return getValueFromAll(key, defaultValue);
    }

    public boolean getValue(String key, boolean defaultValue) {
        return Boolean.valueOf(getValueFromAll(key, Boolean.toString(defaultValue)));
    }

    public double getValue(String key, double defaultValue) {
        return Double.valueOf(getValueFromAll(key, Double.toString(defaultValue)));
    }

    public int getValue(String key, int defaultValue) {
        return Integer.valueOf(getValueFromAll(key, Integer.toString(defaultValue)));
    }

    public long getValue(String key, long defaultValue) {
        return Integer.valueOf(getValueFromAll(key, Long.toString(defaultValue)));
    }

    /**
     * önce sistem pproperty denenir, yoksa abys.conf, o da yoksa veritabanından
     * denenir
     *
     * @param key
     * @param defaultValue
     * @return
     */
    private String getValueFromAll(String key, String defaultValue) {
        reloadIfRequired();
        try {
            return getValueFromSystem(key);
        } catch (ValueNotFoundException ex) {
            try {
                return getValueFromMap(key);
            } catch (ValueNotFoundException ex1) {
                try {
                    return getValueFromDB(key);
                } catch (SQLException sqle) {
                    Logger.getLogger(ABYSConf.class.getName()).log(Level.SEVERE, null, sqle);
                    //ne kadar hoşuma gitmse de burda değeri döneceğim
                    return defaultValue;
                } catch (ValueNotFoundException ex2) {
                    return defaultValue;
                }
            }
        }
    }

    private String getValueFromSystem(String key) throws ValueNotFoundException {
        if (System.getProperties().containsKey(key)) {
            return System.getProperty(key);
        }
        throw new ValueNotFoundException();
    }

    private String getValueFromMap(String key) throws ValueNotFoundException {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        throw new ValueNotFoundException();
    }

    private String getValueFromDB(String key) throws SQLException, ValueNotFoundException {
        checkConnection();
        if (connection != null) {
            try (PreparedStatement st = connection.prepareStatement("select " + tableKeyColumn + " as key, " + tableValueColumn + " as val from " + tableName + " where " + tableKeyColumn + " = ?")) {
                st.setString(1, key);
                try (ResultSet res = st.executeQuery()) {
                    if (res.next()) {
                        return res.getString("key");
                    }
                }
            }
        }
        throw new ValueNotFoundException();
    }

    public Map<String, String> getMap() {
        reloadIfRequired();
        return Collections.unmodifiableMap(map);
    }

    private void checkConnection() {
        synchronized (reloadLock) {
            try {
                if (!connection.isValid(1 /*burası saniye dikkat*/)) {
                    reconnectToDb(map);//tekra bağlanır
                }
            } catch (SQLException ex) {
                //parametre negatif ise bu hatayı verirmiş doğal olarak atlanabilir
            }
        }
    }

    private class ValueNotFoundException extends Exception {

        public ValueNotFoundException(Throwable cause) {
            super(cause);
        }

        public ValueNotFoundException() {
        }

    }

}
