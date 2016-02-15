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
    public static Object to(Object val, Class<?> valueClass) {
        if(val.getClass() == valueClass) {
            return val;
        }
        if(ResourceBundle.class == valueClass) { //bunu dışarı çıkartayım
            return val;
        }
        if(valueClass == Integer.class) {
            return Integer.valueOf((String)val);
        }
        if(valueClass == Long.class) {
            return Long.valueOf((String)val);
        }
        if(valueClass == Short.class) {
            return Short.valueOf((String)val);
        }
        throw new IllegalArgumentException(val.getClass() + "->" + valueClass.getName());
    }

}
