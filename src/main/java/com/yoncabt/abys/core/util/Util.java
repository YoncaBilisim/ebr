/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.abys.core.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author myururdurmaz
 */
public class Util {
    private static final boolean test = new File("/tmp/ebt-test-mode").exists();
    public static boolean isTest(){
        return test;
    }

    public static List<Integer> parseSequence(String sequence, int maxCount) {
        if (StringUtils.isBlank(sequence)) {
            return Collections.EMPTY_LIST;
        }
        List<Integer> ret = new ArrayList<>();
        for (String item : sequence.split(",")) {
            if (item.contains("-")) {
                String[] split = item.split("-");
                int start = Integer.parseInt(split[0]);
                int end = Integer.parseInt(split[1]);
                for (int i = start; start < end && i <= end; i++) {
                    ret.add(i);
                    if (ret.size() > maxCount) {
                        throw new IllegalArgumentException(sequence);
                    }
                }
            } else {
                ret.add(Integer.parseInt(item));
                if (ret.size() > maxCount) {
                    throw new IllegalArgumentException(sequence);
                }
            }
        }
        return ret;
    }

    public static <T> T nvl(T v1, T v2){
        if (v1 == null) {
            return v2;
        }
        return v1;
    }

    public static String join(String seperator, String... args){
        StringBuilder sb =new StringBuilder();
        if(args.length > 0) {
            sb.append(nvl(args[0], ""));
        }
        for (int i = 1; i < args.length; i++) {
            sb.append(seperator).append(nvl(args[i], ""));
        }
        return sb.toString();
    }
}
