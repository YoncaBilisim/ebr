package com.yoncabt.abys.core.util;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
                saveGridToFile((Grid)downloadButton.getData(), baos);
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
        Workbook wb = new HSSFWorkbook();
        Sheet s = wb.createSheet();

        Font headerFont = wb.createFont();
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        CellStyle headerCellStyle = wb.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

        int rownum = 0;
        for (int i = 0; i < grid.getHeaderRowCount(); i++) {
            Row row = s.createRow(rownum++);
            for (int j = 0; j < grid.getColumns().size(); j++) {
                Grid.Column column = grid.getColumns().get(j);
                //header ekle
                Cell cell = row.createCell(j);
                cell.setCellStyle(headerCellStyle);
                cell.setCellValue(grid.getHeaderRow(i).getCell(column.getPropertyId()).getText());
            }
        }

        for (Object itemId : grid.getContainerDataSource().getItemIds()) {
            Row row = s.createRow(rownum++);
            Item gridRow = grid.getContainerDataSource().getItem(itemId);

            for (int j = 0; j < grid.getColumns().size(); j++) {
                Cell cell = row.createCell(j);
                Property itemProperty = gridRow.getItemProperty(gridRow.getItemPropertyIds().toArray()[j]);
                if (itemProperty.getValue() instanceof Integer) {
                    cell.setCellValue((Integer) itemProperty.getValue());
                } else if (itemProperty.getValue() instanceof Double) {
                    cell.setCellValue((Double) itemProperty.getValue());
                } else if (itemProperty.getValue() instanceof String) {
                    cell.setCellValue((String) itemProperty.getValue());
                } else {
                    cell.setCellValue(Util.nvl(itemProperty.getValue(), (Object) "").toString());
                }
            }
        }

        for (int i = 0; i < grid.getFooterRowCount(); i++) {
            Row row = s.createRow(rownum++);
            for (int j = 0; j < grid.getColumns().size(); j++) {
                Grid.Column column = grid.getColumns().get(j);
                //header ekle
                Cell cell = row.createCell(j);
                cell.setCellStyle(headerCellStyle);
                cell.setCellValue(grid.getFooterRow(i).getCell(column.getPropertyId()).getText());
            }
        }

        wb.write(out);
        out.close();
    }
}
