/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.executor.definition;

import com.yoncabt.ebr.ui.FieldType;
import com.yoncabt.ebr.ui.InputType;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author myururdurmaz
 * @param <T>
 */
public class ReportParam<T> {

    private Class<T> javaType;
    private FieldType fieldType;
    private InputType inputType;
    private String name;
    private String label;
    private String format;
    private T min;
    private T max;
    private T defaultValue;
    private boolean raw;
    private Map<Object, String> lovData = new LinkedHashMap<>();

    public ReportParam(Class<T> type) {
        this.javaType = type;
        this.fieldType = FieldType.valueOfJavaType(type);
    }

    /**
     * @return the javaType
     */
    public Class<T> getJavaType() {
        return javaType;
    }

    /**
     * @param javaType the javaType to set
     */
    public void setJavaType(Class<T> javaType) {
        this.javaType = javaType;
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

    /**
     * @return the fieldType
     */
    public FieldType getFieldType() {
        return fieldType;
    }

    /**
     * @param fieldType the fieldType to set
     */
    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    /**
     * @return the lovData
     */
    public Map<Object, String> getLovData() {
        return lovData;
    }

    /**
     * @param lovData the lovData to set
     */
    public void setLovData(Map<Object, String> lovData) {
        this.lovData = lovData;
    }

    /**
     * @return the inputType
     */
    public InputType getInputType() {
        return inputType;
    }

    /**
     * @param inputType the inputType to set
     */
    public void setInputType(InputType inputType) {
        this.inputType = inputType;
    }
}
