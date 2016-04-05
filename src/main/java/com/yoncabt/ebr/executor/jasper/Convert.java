/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.executor.jasper;

import java.math.BigDecimal;
import java.util.ResourceBundle;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author myururdurmaz
 */
public class Convert {

    /**
     * burası jrxml e gidecek verilerin tip dönüşümünü yapar.
     *
     * @param <T>
     * @param val
     * @param valueClass
     * @return
     */
    public static <T> T to(Object val, Class<T> valueClass) {
        if (valueClass.isInstance(val)) {
            return (T) val;
        }
        if (ResourceBundle.class == valueClass) { //bunu dışarı çıkartayım
            return (T) val;
        }
        if (val instanceof String) {
            if(Number.class.isAssignableFrom(valueClass))
                return str2Number((String)val, valueClass);
        }
        throw new IllegalArgumentException(val.getClass() + "->" + valueClass.getName());
    }

    private static <T> T str2Number(String val, Class<T> valueClass) {
        if (StringUtils.isBlank(val)) {
            return null;
        }
        if (valueClass == Integer.class) {
            return (T) Integer.valueOf(val);
        }
        if (valueClass == Long.class) {
            return (T) Long.valueOf(val);
        }
        if (valueClass == Short.class) {
            return (T) Short.valueOf(val);
        }
        if (valueClass == Double.class) {
            return (T) Double.valueOf(val);
        }
        if (valueClass == BigDecimal.class) {
            return (T) new BigDecimal(val);
        }
        throw new IllegalArgumentException(val.getClass() + "->" + valueClass.getName());
    }

}
