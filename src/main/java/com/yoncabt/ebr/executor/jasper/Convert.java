/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.executor.jasper;

import java.util.ResourceBundle;

/**
 *
 * @author myururdurmaz
 */
public class Convert {

    /**
     * burası jrxml e gidecek verilerin tip dönüşümünü yapar.
     * @param val
     * @param valueClass
     * @return
     */
    public static <T> T to(Object val, Class<T> valueClass) {
        if(valueClass.isInstance(val)) {
            return (T) val;
        }
        if(ResourceBundle.class == valueClass) { //bunu dışarı çıkartayım
            return (T) val;
        }
        if(valueClass == Integer.class) {
            return (T) Integer.valueOf((String)val);
        }
        if(valueClass == Long.class) {
            return (T) Long.valueOf((String)val);
        }
        if(valueClass == Short.class) {
            return (T) Short.valueOf((String)val);
        }
        throw new IllegalArgumentException(val.getClass() + "->" + valueClass.getName());
    }

}
