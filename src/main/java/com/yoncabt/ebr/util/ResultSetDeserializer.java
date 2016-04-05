/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.util;

import com.yoncabt.ebr.FieldType;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author myururdurmaz
 */
public class ResultSetDeserializer {

    private JSONObject jo;

    private List<FieldType> types = new ArrayList<>();
    private List<String> names = new ArrayList<>();

    public ResultSetDeserializer(InputStream is) throws IOException {
        jo = new JSONObject(IOUtils.toString(is, "utf-8"));
        readTypes();
    }

    public List<FieldType> getTypes() {
        return types;
    }

    public List<String> getNames() {
        return names;
    }

    public List<Object[]> getData() {
        List<Object[]> ret = new ArrayList<>();
        JSONArray arr = jo.getJSONArray("values");
        for (int i = 0; i < arr.length(); i++) {
            List<Object> column = new ArrayList<>();
            JSONArray row = arr.getJSONArray(i);
            for (int j = 0; j < types.size(); j++) {
                if (row.isNull(j)) {
                    column.add(null);
                } else {
                    switch (types.get(j)) {
                        case DATE:
                            column.add(new Date(row.getLong(j)));
                            break;
                        case STRING:
                            column.add(row.getString(j));
                            break;
                        case INTEGER:
                            column.add(row.getInt(j));
                            break;
                        case LONG:
                            column.add(row.getLong(j));
                            break;
                        case DOUBLE:
                            column.add(row.getDouble(j));
                            break;
                        default:
                            throw new AssertionError();
                    }
                }
            }
            ret.add(column.toArray());
        }
        return ret;
    }

    private void readTypes() {
        JSONArray arr = jo.getJSONArray("types");
        for (int i = 0; i < arr.length(); i++) {
            String type = arr.getString(i);
            types.add(FieldType.valueOf(type));
        }
        arr = jo.getJSONArray("names");
        for (int i = 0; i < arr.length(); i++) {
            String name = arr.getString(i);
            names.add(name);
        }
    }
}
