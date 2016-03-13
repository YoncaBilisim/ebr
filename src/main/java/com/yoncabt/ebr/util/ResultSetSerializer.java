package com.yoncabt.ebr.util;

import com.yoncabt.ebr.FieldType;
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
        List<String> names = new ArrayList<>();
        List<String> typeNames = new ArrayList<>();
        ResultSetMetaData md = res.getMetaData();

        for (int i = 0; i < md.getColumnCount(); i++) {
            names.add(md.getColumnName(i + 1));
            types.add(FieldType.valueOf(md, i + 1));
            typeNames.add(FieldType.valueOf(md, i + 1).name());
        }
        jw.key("names").value(names);
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
            switch (types.get(i)) {
                case DATE:
                    column.add(res.getDate(i + 1).getTime());
                    break;
                case STRING:
                    column.add(res.getString(i + 1));
                    break;
                case INTEGER:
                    column.add(res.getInt(i + 1));
                    break;
                case LONG:
                    column.add(res.getLong(i + 1));
                    break;
                case DOUBLE:
                    column.add(res.getDouble(i + 1));
                    break;
                default:
                    throw new AssertionError();
            }
        }
        jw.value(column);
    }
}
