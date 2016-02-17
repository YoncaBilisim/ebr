/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.util;

/**
 *
 * @author myururdurmaz
 */
public class ASCIIFier {
    private static final String FROM = "ÜĞİŞÇÖüğışçö";
    private static final String TO = "UGISCOugisco";
    public static String ascii(String s) {
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if(FROM.indexOf(c) > -1)
                chars[i] = TO.charAt(FROM.indexOf(c));
        }
        return new String(chars);
    }
}
