package com.yoncabt.abys.core.util;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.yoncabt.ebr.executor.jasper.Convert;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 *
 * @author myururdurmaz
 */
public class YoncaGridXLSExporter {

    public static Button createDownloadButton(final Grid grid, String fileName) {
        Button downloadButton = new Button(FontAwesome.DOWNLOAD);
        downloadButton.setData(grid);

        StreamResource myResource = new StreamResource(() -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                saveGridToFile((Grid) downloadButton.getData(), baos);
                return new ByteArrayInputStream(baos.toByteArray());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }, fileName);
        FileDownloader fileDownloader = new FileDownloader(myResource);
        fileDownloader.extend(downloadButton);
        return downloadButton;
    }

    public static void saveGridToFile(Grid grid, OutputStream out) throws IOException {
        List<Object[]> headers = new ArrayList<>();
        List<Object[]> data = new ArrayList<>();
        List<Object[]> footers = new ArrayList<>();

        for (int i = 0; i < grid.getHeaderRowCount(); i++) {
            Object[] row = new Object[grid.getColumns().size()];
            for (int j = 0; j < grid.getColumns().size(); j++) {
                Grid.Column column = grid.getColumns().get(j);
                row[j] = grid.getHeaderRow(i).getCell(column.getPropertyId()).getText();
            }
            headers.add(row);
        }

        for (Object itemId : grid.getContainerDataSource().getItemIds()) {
            Item gridRow = grid.getContainerDataSource().getItem(itemId);
            Object[] row = new Object[grid.getColumns().size()];
            for (int j = 0; j < grid.getColumns().size(); j++) {
                Property itemProperty = gridRow.getItemProperty(gridRow.getItemPropertyIds().toArray()[j]);
                row[j] = itemProperty.getValue();
            }
            data.add(row);
        }

        for (int i = 0; i < grid.getFooterRowCount(); i++) {
            Object[] row = new Object[grid.getColumns().size()];
            for (int j = 0; j < grid.getColumns().size(); j++) {
                Grid.Column column = grid.getColumns().get(j);
                row[j] = grid.getFooterRow(i).getCell(column.getPropertyId()).getText();
            }
            footers.add(row);
        }
        saveGridToFile(headers, data, footers, out);
    }

    public static void saveGridToFile(List<Object[]> headers, List<Object[]> data, List<Object[]> footers, OutputStream out) throws IOException {
        Workbook wb = new HSSFWorkbook();
        Sheet s = wb.createSheet();

        Font headerFont = wb.createFont();
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        CellStyle headerCellStyle = wb.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

        int rownum = 0;
        for (Object[] header : headers) {
            Row row = s.createRow(rownum++);
            for (int j = 0; j < header.length; j++) {
                //header ekle
                Cell cell = row.createCell(j);
                cell.setCellStyle(headerCellStyle);
                cell.setCellValue(Convert.to(header[j], String.class));
            }
        }

        for (Object[] row : data) {
            Row xlsRow = s.createRow(rownum++);
            for (int j = 0; j < row.length; j++) {
                Object cell = row[j];
                Cell xlsCell = xlsRow.createCell(j);
                if (cell instanceof Integer) {
                    xlsCell.setCellValue((Integer) cell);
                } else if (cell instanceof Double) {
                    xlsCell.setCellValue((Double) cell);
                } else if (cell instanceof String) {
                    xlsCell.setCellValue((String) cell);
                } else {
                    xlsCell.setCellValue(Util.nvl(cell, (Object) "").toString());
                }
            }
        }

        for (Object[] footer : footers) {
            Row row = s.createRow(rownum++);
            for (int j = 0; j < footer.length; j++) {
                //header ekle
                Cell cell = row.createCell(j);
                cell.setCellStyle(headerCellStyle);
                cell.setCellValue(Convert.to(footer[j], String.class));
            }
        }

        wb.write(out);
        out.close();
    }
}
