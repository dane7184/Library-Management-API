package com.library.report.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

/**
 * Generic helper: writes a bold, frozen header row + one row per item into an .xlsx byte array.
 * Numbers, money and dates are written as real typed cells so they sort and sum in Excel.
 */
@Component
public class ExcelExporter {

    /** Excel column width unit is 1/256 of a character; cap so one long title can't blow up a column. */
    private static final int MIN_CHARS = 6;
    private static final int MAX_CHARS = 60;

    public <T> byte[] export(String sheetName, List<String> headers, List<T> rows,
                             Function<T, List<Object>> rowMapper) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet(sheetName);
            Styles styles = new Styles(wb);
            int[] widths = new int[headers.size()];

            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell c = header.createCell(i);
                c.setCellValue(headers.get(i));
                c.setCellStyle(styles.header);
                widths[i] = headers.get(i).length();
            }

            int r = 1;
            for (T item : rows) {
                Row row = sheet.createRow(r++);
                List<Object> values = rowMapper.apply(item);
                for (int i = 0; i < values.size(); i++) {
                    int len = write(row.createCell(i), values.get(i), styles);
                    if (i < widths.length) {
                        widths[i] = Math.max(widths[i], len);
                    }
                }
            }

            // Computed widths instead of autoSizeColumn, which needs AWT fonts (often missing in containers).
            for (int i = 0; i < widths.length; i++) {
                int chars = Math.min(MAX_CHARS, Math.max(MIN_CHARS, widths[i] + 2));
                sheet.setColumnWidth(i, chars * 256);
            }
            sheet.createFreezePane(0, 1);
            if (!headers.isEmpty()) {
                sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, Math.max(0, r - 1), 0, headers.size() - 1));
            }

            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to build Excel report", e);
        }
    }

    /** Writes one value as a typed cell; returns its approximate display width in characters. */
    private static int write(Cell cell, Object value, Styles styles) {
        switch (value) {
            case null -> {
                cell.setBlank();
                return 0;
            }
            case BigDecimal d -> {
                cell.setCellValue(d.doubleValue());
                cell.setCellStyle(styles.money);
                return d.toPlainString().length() + 1;
            }
            case Number n -> {
                cell.setCellValue(n.doubleValue());
                return n.toString().length();
            }
            case LocalDate d -> {
                cell.setCellValue(d);
                cell.setCellStyle(styles.date);
                return 10;
            }
            case LocalDateTime dt -> {
                cell.setCellValue(dt);
                cell.setCellStyle(styles.dateTime);
                return 16;
            }
            case Enum<?> e -> {
                cell.setCellValue(e.name());
                return e.name().length();
            }
            default -> {
                String s = value.toString();
                cell.setCellValue(s);
                return s.length();
            }
        }
    }

    private static final class Styles {
        final CellStyle header;
        final CellStyle money;
        final CellStyle date;
        final CellStyle dateTime;

        Styles(Workbook wb) {
            DataFormat fmt = wb.createDataFormat();

            Font bold = wb.createFont();
            bold.setBold(true);
            header = wb.createCellStyle();
            header.setFont(bold);
            header.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            money = wb.createCellStyle();
            money.setDataFormat(fmt.getFormat("#,##0.00"));

            date = wb.createCellStyle();
            date.setDataFormat(fmt.getFormat("yyyy-mm-dd"));

            dateTime = wb.createCellStyle();
            dateTime.setDataFormat(fmt.getFormat("yyyy-mm-dd hh:mm"));
        }
    }
}
