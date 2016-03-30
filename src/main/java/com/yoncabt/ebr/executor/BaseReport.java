/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.executor;

import com.yoncabt.ebr.FieldType;
import com.yoncabt.ebr.ReportOutputFormat;
import com.yoncabt.ebr.ReportRequest;
import com.yoncabt.ebr.exceptions.ReportException;
import com.yoncabt.ebr.executor.definition.ReportDefinition;
import com.yoncabt.ebr.executor.definition.ReportParam;
import com.yoncabt.ebr.jdbcbridge.pool.EBRConnection;
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
                FieldType type = FieldType.valueOfJSONName(field.getString("type"));
                switch (type) {
                    case DATE:
                        {
                            ReportParam<Date> rp = new ReportParam<>(Date.class);
                            readCommon(rp, field);
                            if(field.has("default-value"))
                                rp.setDefaultValue(new Date(field.getLong("default-value")));
                            ret.getReportParams().add(rp);
                            break;
                        }
                    case STRING:
                        {
                            ReportParam<String> rp = new ReportParam<>(String.class);
                            readCommon(rp, field);
                            if(field.has("default-value"))
                                rp.setDefaultValue(field.getString("default-value"));
                            ret.getReportParams().add(rp);
                            break;
                        }
                    case INTEGER:
                        {
                            ReportParam<Integer> rp = new ReportParam<>(Integer.class);
                            readCommon(rp, field);
                            int min = field.has("min") ? field.getInt("min") : Integer.MIN_VALUE;
                            int max = field.has("max") ? field.getInt("max") : Integer.MAX_VALUE;
                            rp.setMax(max);
                            rp.setMin(min);
                            if(field.has("default-value"))
                                rp.setDefaultValue(field.getInt("default-value"));
                            ret.getReportParams().add(rp);
                            break;
                        }
                    case LONG:
                        {
                            ReportParam<Long> rp = new ReportParam<>(Long.class);
                            readCommon(rp, field);
                            long min = field.has("min") ? field.getLong("min") : Long.MIN_VALUE;
                            long max = field.has("max") ? field.getLong("max") : Long.MAX_VALUE;
                            rp.setMax(max);
                            rp.setMin(min);
                            if(field.has("default-value"))
                                rp.setDefaultValue(field.getLong("default-value"));
                            ret.getReportParams().add(rp);
                            break;
                        }
                    case DOUBLE:
                        {
                            ReportParam<Double> rp = new ReportParam<>(Double.class);
                            readCommon(rp, field);
                            double min = field.has("min") ? field.getLong("min") : Double.MIN_VALUE;
                            double max = field.has("max") ? field.getLong("max") : Double.MAX_VALUE;
                            rp.setMax(max);
                            rp.setMin(min);
                            if(field.has("default-value"))
                                rp.setDefaultValue(field.getDouble("default-value"));
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

    public abstract void exportTo(ReportRequest request, ReportOutputFormat outputFormat, EBRConnection connection, ReportDefinition reportDefinition) throws ReportException, IOException;

}
