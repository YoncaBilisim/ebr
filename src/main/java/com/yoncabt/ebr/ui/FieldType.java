/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.ui;

import java.math.BigDecimal;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

/**
 *
 * @author myururdurmaz
 */
public enum FieldType {

    STRING(String.class),
    INTEGER(Integer.class),
    LONG(Long.class),
    DOUBLE(Double.class),
    BIGDECIMAL(BigDecimal.class),
    DATE(java.util.Date.class);

    private Class javaType;

    private FieldType(Class javaType) {
        this.javaType = javaType;
    }

    public Class getJavaType() {
        return javaType;
    }

    public static FieldType valueOfJavaTypeName(String name) {
        for (FieldType value : values()) {
            if (value.getJavaType().getName().equals(name)) {
                return value;
            }
        }
        throw new IllegalArgumentException(name);
    }

    /**
     * eski raprlar için
     *
     * @param name
     * @return
     */
    public static FieldType valueOfJSONName(String name) {
        switch (name) {
            case "int":
                return INTEGER;
            case "long":
                return LONG;
            case "date":
                return DATE;
            case "double":
                return DOUBLE;
            case "string":
                return STRING;
            default:
                return valueOf(name);
        }
    }

    public static FieldType valueOfJavaType(Class type) {
        return valueOfJavaTypeName(type.getName());
    }

    public static FieldType valueOf(ResultSetMetaData md, int column) throws SQLException {
        if (md.getColumnType(column) == Types.VARCHAR || md.getColumnType(column) == Types.CHAR) {
            return STRING;
        } else if (md.getColumnType(column) == Types.DATE || md.getColumnType(column) == Types.TIMESTAMP) {
            return DATE;
        } else if (md.getColumnType(column) == Types.NUMERIC) {
            if (md.getScale(column) > 0) {
                return DOUBLE;
            } else if (md.getPrecision(column) > 9) {
                return LONG;
            } else {
                return INTEGER;
            }
        } else {
            throw new AssertionError(md.getColumnTypeName(column));
        }
    }
}
