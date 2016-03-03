/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.executor;

import com.yoncabt.ebr.executor.definition.ReportDefinition;
import com.yoncabt.ebr.executor.definition.ReportParam;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author myururdurmaz
 */
public abstract class BaseReport {

    public BaseReport() {
    }

    private void readCommon(ReportParam<?> rp, JSONObject field) {
        rp.setRaw(field.optBoolean("raw", false));
        rp.setLabel(field.getString("label"));
        rp.setName(field.getString("name"));
        rp.setFormat(field.optString("format", null));
    }

    public ReportDefinition loadDefinition(File reportFile, File jsonFile) throws AssertionError, IOException, JSONException {
        ReportDefinition ret = new ReportDefinition(reportFile);
        if (!jsonFile.exists()) {
            ret.setCaption(jsonFile.getName().replace(".ebr.json", ""));
            return ret;
        }
        String jsonComment = FileUtils.readFileToString(jsonFile, "utf-8");
        JSONObject jsonObject = new JSONObject(jsonComment);
        ret.setCaption(jsonObject.optString("title", "NOT ITTLE"));
        ret.setDataSource(jsonObject.optString("datasource", "default"));
        ret.setTextEncoding(jsonObject.optString("text-encoding", "utf-8"));
        ret.setTextTemplate(jsonObject.optString("text-template", "SUITABLE"));
        if (jsonObject.has("fields")) {
            JSONArray fieldsArray = jsonObject.getJSONArray("fields");
            for (int i = 0; i < fieldsArray.length(); i++) {
                JSONObject field = fieldsArray.getJSONObject(i);
                String type = field.getString("type");
                switch (type) {
                    case "date":
                        {
                            ReportParam<Date> rp = new ReportParam<>(Date.class);
                            readCommon(rp, field);
                            ret.getReportParams().add(rp);
                            break;
                        }
                    case "string":
                        {
                            ReportParam<String> rp = new ReportParam<>(String.class);
                            readCommon(rp, field);
                            ret.getReportParams().add(rp);
                            break;
                        }
                    case "int":
                        {
                            ReportParam<Integer> rp = new ReportParam<>(Integer.class);
                            readCommon(rp, field);
                            int min = field.has("min") ? field.getInt("min") : Integer.MIN_VALUE;
                            int max = field.has("max") ? field.getInt("max") : Integer.MAX_VALUE;
                            rp.setMax(max);
                            rp.setMin(min);
                            ret.getReportParams().add(rp);
                            break;
                        }
                    case "long":
                        {
                            ReportParam<Long> rp = new ReportParam<>(Long.class);
                            readCommon(rp, field);
                            long min = field.has("min") ? field.getLong("min") : Long.MIN_VALUE;
                            long max = field.has("max") ? field.getLong("max") : Long.MAX_VALUE;
                            rp.setMax(max);
                            rp.setMin(min);
                            ret.getReportParams().add(rp);
                            break;
                        }
                    default:
                        {
                            throw new AssertionError(type);
                        }
                }
            }
        }
        return ret;
    }

}
