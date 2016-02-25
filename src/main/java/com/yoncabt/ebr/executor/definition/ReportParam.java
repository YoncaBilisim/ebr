/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.executor.definition;

/**
 *
 * @author myururdurmaz
 * @param <T>
 */
public class ReportParam<T> {

    private Class<T> type;
    private String name;
    private String label;
    private String format;
    private T min;
    private T max;
    private T defaultValue;
    private boolean raw;

    public ReportParam(Class<T> type) {
        this.type = type;
    }



    /**
     * @return the type
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Class<T> type) {
        this.type = type;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the format
     */
    public String getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * @return the min
     */
    public T getMin() {
        return min;
    }

    /**
     * @param min the min to set
     */
    public void setMin(T min) {
        this.min = min;
    }

    /**
     * @return the max
     */
    public T getMax() {
        return max;
    }

    /**
     * @param max the max to set
     */
    public void setMax(T max) {
        this.max = max;
    }

    /**
     * @return the defaultValue
     */
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return the raw
     */
    public boolean isRaw() {
        return raw;
    }

    /**
     * @param raw the raw to set
     */
    public void setRaw(boolean raw) {
        this.raw = raw;
    }
}
