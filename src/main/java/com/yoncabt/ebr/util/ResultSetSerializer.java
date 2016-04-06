package com.yoncabt.ebr.util;

import com.yoncabt.ebr.ui.FieldType;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.json.JSONWriter;

/**
 *
 * @author myururdurmaz
 */
public class ResultSetSerializer {

    private ResultSet res;
    private JSONWriter jw;
    private File file;
    private List<String> columnNames = new ArrayList<>();

    private List<FieldType> types = new ArrayList<>();

    public ResultSetSerializer(ResultSet res, File file) {
        this.res = res;
        this.file = file;
    }

    public File serialize() throws SQLException, IOException {
        try (FileWriterWithEncoding fw = new FileWriterWithEncoding(file, "utf-8")) {
            jw = new JSONWriter(fw);
            jw.object();
            writeDefinition();
            jw.key("values");
            writeData();
            jw.endObject();
        }
        return file;
    }

    private void writeDefinition() throws SQLException {
        List<String> typeNames = new ArrayList<>();
        ResultSetMetaData md = res.getMetaData();

        for (int i = 0; i < md.getColumnCount(); i++) {
            columnNames.add(md.getColumnName(i + 1));
            types.add(FieldType.valueOf(md, i + 1));
            typeNames.add(FieldType.valueOf(md, i + 1).name());
        }
        jw.key("names").value(columnNames);
        jw.key("types").value(typeNames);
    }

    private void writeData() throws SQLException {
        jw.array();
        while (res.next()) {
            writeColumn();
        }
        jw.endArray();
    }

    private void writeColumn() throws SQLException {
        List<Object> column = new ArrayList<>(types.size());
        for (int i = 0; i < types.size(); i++) {
            final FieldType type = types.get(i);
            Object value;
            value = readValue(res, columnNames.get(i), type);
            column.add(value);
        }
        jw.value(column);
    }

    public static Object readValue(ResultSet res, String columnName, final FieldType type) throws AssertionError, SQLException {
        Object value;
        if (res.getString(columnName) == null) {
            value = null;
        } else {
            switch (type) {
                case DATE:
                    value = res.getDate(columnName).getTime();
                    break;
                case STRING:
                    value = res.getString(columnName);
                    break;
                case INTEGER:
                    value = res.getInt(columnName);
                    break;
                case LONG:
                    value = res.getLong(columnName);
                    break;
                case DOUBLE:
                    value = res.getDouble(columnName);
                    break;
                default:
                    throw new AssertionError();
            }
        }
        return value;
    }
}
