package com.library.report.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/** Generic helper: writes a bold header row + one row per item into an .xlsx byte array. */
@Component
public class ExcelExporter {

    public <T> byte[] export(String sheetName, List<String> headers, List<T> rows,
                             Function<T, List<Object>> rowMapper) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet(sheetName);
            CellStyle bold = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            bold.setFont(font);

            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell c = header.createCell(i);
                c.setCellValue(headers.get(i));
                c.setCellStyle(bold);
            }
            int r = 1;
            for (T item : rows) {
                Row row = sheet.createRow(r++);
                List<Object> values = rowMapper.apply(item);
                for (int i = 0; i < values.size(); i++) {
                    Object v = values.get(i);
                    row.createCell(i).setCellValue(v == null ? "" : v.toString());
                }
            }
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to build Excel report", e);
        }
    }
}
