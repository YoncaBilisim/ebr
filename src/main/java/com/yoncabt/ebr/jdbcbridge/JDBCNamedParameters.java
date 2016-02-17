/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.jdbcbridge;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author myururdurmaz
 */
public class JDBCNamedParameters {

    private String sql;
    private Map<String, Integer> intParams = new HashMap<>();
    private Map<String, Long> longParams = new HashMap<>();
    private Map<String, String> stringParams = new HashMap<>();
    private Map<String, Date> dateParams = new HashMap<>();

    public JDBCNamedParameters(String sql) {
        this.sql = sql;
    }

    public void set(String param, Integer value) {
        intParams.put(param, value);
    }

    public void set(String param, Long value) {
        longParams.put(param, value);
    }

    public void set(String param, String value) {
        stringParams.put(param, value);
    }

    public void set(String param, Date value) {
        dateParams.put(param, value);
    }

    public PreparedStatement prepare(Connection con) throws SQLException {
        List<String> paramNameList = new ArrayList<>();

        Pattern param = Pattern.compile(":[0-9a-zA-Z_]+");
        String pSql = sql;
        while (param.matcher(pSql).find()) {
            Matcher m = param.matcher(pSql);
            if (m.find()) {
                paramNameList.add(m.group().substring(1));//baştaki : temizliği
                pSql = m.replaceFirst("?");
            }
        }
        PreparedStatement ps = con.prepareStatement(pSql);
        for (int i = 0; i < paramNameList.size(); i++) {
            String paramName = paramNameList.get(i);
            if (intParams.containsKey(paramName)) {
                ps.setInt(i + 1, intParams.get(paramName));
            } else if (longParams.containsKey(paramName)) {
                ps.setLong(i + 1, longParams.get(paramName));
            } else if (stringParams.containsKey(paramName)) {
                ps.setString(i + 1, stringParams.get(paramName));
            } else if (dateParams.containsKey(paramName)) {
                ps.setDate(i + 1, new java.sql.Date(dateParams.get(paramName).getTime()));
            } else {
                throw new IllegalArgumentException(paramName + " bulunamadı");
            }
        }
        return ps;
    }
}
