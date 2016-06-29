/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.executor;

import com.yoncabt.ebr.ui.FieldType;
import com.yoncabt.ebr.ReportOutputFormat;
import com.yoncabt.ebr.ReportRequest;
import com.yoncabt.ebr.exceptions.ReportException;
import com.yoncabt.ebr.executor.definition.ReportDefinition;
import com.yoncabt.ebr.executor.definition.ReportParam;
import com.yoncabt.ebr.jdbcbridge.pool.DataSourceManager;
import com.yoncabt.ebr.jdbcbridge.pool.EBRConnection;
import com.yoncabt.ebr.ui.InputType;
import com.yoncabt.ebr.util.ResultSetSerializer;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author myururdurmaz
 */
public abstract class BaseReport {

    @Autowired
    private DataSourceManager dataSourceManager;

    private File file;

    public BaseReport() {
    }

    private void readCommon(ReportDefinition ret, ReportParam<?> rp, JSONObject field) {
        rp.setRaw(field.optBoolean("raw", false));
        rp.setLabel(field.getString("label"));
        rp.setName(field.getString("name"));
        rp.setFormat(field.optString("format", null));
        ret.getReportParams().add(rp);
        InputType inputType = InputType.valueOf(field.optString("input-type", InputType.INPUT.name()));
        rp.setInputType(inputType);
        FieldType fieldType = FieldType.valueOfJSONName(field.getString("type"));
        rp.setFieldType(fieldType);
        if (inputType == InputType.COMBO) {
            if (field.has("lov-data")) {
                JSONArray lovData = field.getJSONArray("lov-data");
                for (int i = 0; i < lovData.length(); i++) {
                    Object value = lovData.getJSONObject(i).get("value");
                    String text = lovData.getJSONObject(i).getString("text");
                    rp.getLovData().put(value, text);
                }
            }
            if (field.has("lov-query")) {
                String sql = field.getString("lov-query");
                try (EBRConnection con = dataSourceManager.get(ret.getDataSource(), "EBR", ret.getFile().getAbsolutePath(), "FILL-COMBO");
                        Statement st = con.createStatement();
                        ResultSet res = st.executeQuery(sql)) {
                    while (res.next()) {
                        String text = (String) ResultSetSerializer.readValue(res, "text", FieldType.STRING);
                        Object value = ResultSetSerializer.readValue(res, "value", fieldType);
                        rp.getLovData().put(value, text);
                    }
                } catch (SQLException ex) {
                    throw new ReportException(ex);
                }
            }
        }
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
                FieldType fieldType = FieldType.valueOfJSONName(field.getString("type"));
                switch (fieldType) {
                    case DATE: {
                        ReportParam<Date> rp = new ReportParam<>(Date.class);
                        readCommon(ret, rp, field);
                        if (field.has("default-value")) {
                            rp.setDefaultValue(new Date(field.getLong("default-value")));
                        }
                        break;
                    }
                    case STRING: {
                        ReportParam<String> rp = new ReportParam<>(String.class);
                        readCommon(ret, rp, field);
                        if (field.has("default-value")) {
                            rp.setDefaultValue(field.getString("default-value"));
                        }
                        break;
                    }
                    case INTEGER: {
                        ReportParam<Integer> rp = new ReportParam<>(Integer.class);
                        readCommon(ret, rp, field);
                        int min = field.has("min") ? field.getInt("min") : Integer.MIN_VALUE;
                        int max = field.has("max") ? field.getInt("max") : Integer.MAX_VALUE;
                        rp.setMax(max);
                        rp.setMin(min);
                        if (field.has("default-value")) {
                            rp.setDefaultValue(field.getInt("default-value"));
                        }
                        break;
                    }
                    case LONG: {
                        ReportParam<Long> rp = new ReportParam<>(Long.class);
                        readCommon(ret, rp, field);
                        long min = field.has("min") ? field.getLong("min") : Long.MIN_VALUE;
                        long max = field.has("max") ? field.getLong("max") : Long.MAX_VALUE;
                        rp.setMax(max);
                        rp.setMin(min);
                        if (field.has("default-value")) {
                            rp.setDefaultValue(field.getLong("default-value"));
                        }
                        break;
                    }
                    case DOUBLE: {
                        ReportParam<Double> rp = new ReportParam<>(Double.class);
                        readCommon(ret, rp, field);
                        double min = field.has("min") ? field.getLong("min") : Double.MIN_VALUE;
                        double max = field.has("max") ? field.getLong("max") : Double.MAX_VALUE;
                        rp.setMax(max);
                        rp.setMin(min);
                        if (field.has("default-value")) {
                            rp.setDefaultValue(field.getDouble("default-value"));
                        }
                        break;
                    }
                    default: {
                        throw new AssertionError(fieldType);
                    }
                }
            }
        }
        return ret;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    public abstract void exportTo(ReportRequest request, ReportOutputFormat outputFormat, EBRConnection connection, ReportDefinition reportDefinition) throws ReportException, IOException;

    public abstract ReportDefinition loadDefinition(File file) throws IOException;
}
