package com.openjfx.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * This service class is responsible for exporting room and schedule data to an Excel file.
 *
 * @author leon
 */
public class RoomExcelExportService {

    // General headers for the Excel sheet
    private static final List<String> GENERAL_HEADERS = Arrays.asList(
            "Organisationsplan für den Berufsorientierungstag",
            "8:30 bis 8:45 Uhr Begrüßung und Einführung in der Aula",
            "13:10 bis 13:20 Uhr Abschluss im Klassenverbund"
    );

    // Column headers (time slots)
    private static final List<String> TIME_HEADERS = Arrays.asList(
            "", "8:45 - 9:30", "9:50 - 10:35",
            "10:35 - 11:20", "11:40 - 12:25", "12:25 - 13:10"
    );

    // Letters (A, B, C, D, E) for the separate row
    private static final List<String> LETTER_HEADERS = Arrays.asList(
            "", "A", "B", "C", "D", "E"
    );

    /**
     * Exports the provided data to an Excel file for rooms.
     *
     * @param data      The data to be exported (list of maps containing company and room/time information)
     * @param filePath  The name of the Excel file to be created
     * @throws IOException If an error occurs during file creation or writing
     *
     * @author leon
     */
    public void exportDataToExcel(List<Map<String, Object>> data, String filePath) throws IOException {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data list must not be null or empty");
        }

        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path must not be null or empty");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Raumplan");

            // Create styles for the Excel sheet
            Map<String, CellStyle> styles = createStyles(workbook);

            int rowIndex = 0;

            // Add general headers
            rowIndex = addGeneralHeaders(sheet, styles, rowIndex);

            // Add time and letter headers
            rowIndex = addTimeAndLetterHeaders(workbook, sheet, styles, rowIndex);

            // Add data rows
            addDataRows(workbook, sheet, styles, data, rowIndex);

            // Auto-size columns for better readability
            for (int i = 0; i < TIME_HEADERS.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            // Save the file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        }
    }

    /**
     * Creates and returns a map of styles for the Excel sheet.
     *
     * @param workbook The Excel workbook
     * @return A map of styles
     */
    private Map<String, CellStyle> createStyles(Workbook workbook) {
        Map<String, CellStyle> styles = new HashMap<>();

        // Thin border style for all cells
        CellStyle borderStyle = workbook.createCellStyle();
        borderStyle.setBorderTop(BorderStyle.THIN);
        borderStyle.setBorderBottom(BorderStyle.THIN);
        borderStyle.setBorderLeft(BorderStyle.THIN);
        borderStyle.setBorderRight(BorderStyle.THIN);

        // Thick border style for the bottom section
        CellStyle thickBorderStyle = workbook.createCellStyle();
        thickBorderStyle.setBorderTop(BorderStyle.THICK);
        thickBorderStyle.setBorderBottom(BorderStyle.THICK);
        thickBorderStyle.setBorderLeft(BorderStyle.THICK);
        thickBorderStyle.setBorderRight(BorderStyle.THICK);

        // Header style with grey background
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.cloneStyleFrom(borderStyle);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.LEFT);
        headerStyle.setWrapText(true);

        // Header font (bold, size 10)
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 10);
        headerStyle.setFont(headerFont);

        // Organizational plan style (bold, size 14)
        CellStyle orgPlanStyle = workbook.createCellStyle();
        orgPlanStyle.cloneStyleFrom(borderStyle);
        orgPlanStyle.setAlignment(HorizontalAlignment.LEFT);
        orgPlanStyle.setWrapText(true);
        Font orgPlanFont = workbook.createFont();
        orgPlanFont.setBold(true);
        orgPlanFont.setFontHeightInPoints((short) 14);
        orgPlanStyle.setFont(orgPlanFont);

        // Letter style (centered, grey background, bold, size 11)
        CellStyle letterStyle = workbook.createCellStyle();
        letterStyle.cloneStyleFrom(borderStyle);
        letterStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        letterStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        letterStyle.setAlignment(HorizontalAlignment.CENTER);
        Font letterFont = workbook.createFont();
        letterFont.setBold(true);
        letterFont.setFontHeightInPoints((short) 11);
        letterStyle.setFont(letterFont);

        // Bold and thick border style
        CellStyle boldThickBorderStyle = workbook.createCellStyle();
        boldThickBorderStyle.cloneStyleFrom(borderStyle);
        boldThickBorderStyle.setBorderRight(BorderStyle.THICK);
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldThickBorderStyle.setFont(boldFont);

        // Centered style for rooms
        CellStyle centeredStyle = workbook.createCellStyle();
        centeredStyle.cloneStyleFrom(boldThickBorderStyle);
        centeredStyle.setBorderTop(BorderStyle.THIN);
        centeredStyle.setBorderBottom(BorderStyle.THIN);
        centeredStyle.setBorderLeft(BorderStyle.THIN);
        centeredStyle.setBorderRight(BorderStyle.THIN);
        centeredStyle.setAlignment(HorizontalAlignment.CENTER);

        // General header style (bold, size 11, grey background)
        CellStyle generalHeaderStyle = workbook.createCellStyle();
        generalHeaderStyle.cloneStyleFrom(headerStyle);
        generalHeaderStyle.setAlignment(HorizontalAlignment.LEFT);
        generalHeaderStyle.setWrapText(true);
        Font generalHeaderFont = workbook.createFont();
        generalHeaderFont.setBold(true);
        generalHeaderFont.setFontHeightInPoints((short) 11);
        generalHeaderStyle.setFont(generalHeaderFont);

        // Add styles to the map
        styles.put("borderStyle", borderStyle);
        styles.put("thickBorderStyle", thickBorderStyle);
        styles.put("headerStyle", headerStyle);
        styles.put("orgPlanStyle", orgPlanStyle);
        styles.put("letterStyle", letterStyle);
        styles.put("boldThickBorderStyle", boldThickBorderStyle);
        styles.put("centeredStyle", centeredStyle);
        styles.put("generalHeaderStyle", generalHeaderStyle);

        return styles;
    }

    /**
     * Adds general headers to the Excel sheet.
     *
     * @param sheet      The Excel sheet
     * @param styles     A map of styles
     * @param rowIndex   The current row index
     * @return The updated row index
     */
    private int addGeneralHeaders(Sheet sheet, Map<String, CellStyle> styles, int rowIndex) {
        for (String header : GENERAL_HEADERS) {
            Row row = sheet.createRow(rowIndex++);
            Cell cell = row.createCell(0);
            cell.setCellValue(header);

            if (header.equals("Organisationsplan für den Berufsorientierungstag")) {
                cell.setCellStyle(styles.get("orgPlanStyle"));
            } else {
                cell.setCellStyle(styles.get("generalHeaderStyle"));
            }

            sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, TIME_HEADERS.size() - 1));

            // Add an empty row after "13:10 bis 13:20 Uhr Abschluss im Klassenverbund"
            if (header.equals("13:10 bis 13:20 Uhr Abschluss im Klassenverbund")) {
                sheet.createRow(rowIndex++);
            }
        }

        return rowIndex;
    }

    /**
     * Adds time and letter headers to the Excel sheet.
     *
     * @param workbook   The Excel workbook
     * @param sheet      The Excel sheet
     * @param styles     A map of styles
     * @param rowIndex   The current row index
     * @return The updated row index
     */
    private int addTimeAndLetterHeaders(Workbook workbook, Sheet sheet, Map<String, CellStyle> styles, int rowIndex) {
        // Add time headers
        Row timeHeaderRow = sheet.createRow(rowIndex++);
        for (int i = 0; i < TIME_HEADERS.size(); i++) {
            Cell cell = timeHeaderRow.createCell(i);
            cell.setCellValue(TIME_HEADERS.get(i));

            CellStyle timeHeaderCellStyle = workbook.createCellStyle();
            timeHeaderCellStyle.cloneStyleFrom(styles.get("headerStyle"));
            timeHeaderCellStyle.setBorderBottom(BorderStyle.NONE);
            timeHeaderCellStyle.setBorderTop(BorderStyle.THICK);
            if (i == 0) {
                timeHeaderCellStyle.setBorderRight(BorderStyle.THICK);
            }
            if (i == TIME_HEADERS.size() - 1) {
                timeHeaderCellStyle.setBorderRight(BorderStyle.THICK);
            }
            cell.setCellStyle(timeHeaderCellStyle);
        }

        // Add letter headers
        Row letterHeaderRow = sheet.createRow(rowIndex++);
        for (int i = 0; i < LETTER_HEADERS.size(); i++) {
            Cell cell = letterHeaderRow.createCell(i);
            cell.setCellValue(LETTER_HEADERS.get(i));

            CellStyle letterHeaderCellStyle = workbook.createCellStyle();
            letterHeaderCellStyle.cloneStyleFrom(styles.get("letterStyle"));
            letterHeaderCellStyle.setBorderTop(BorderStyle.NONE);
            if (i == 0) {
                letterHeaderCellStyle.setBorderRight(BorderStyle.THICK);
            }
            if (i == LETTER_HEADERS.size() - 1) {
                letterHeaderCellStyle.setBorderRight(BorderStyle.THICK);
            }
            cell.setCellStyle(letterHeaderCellStyle);
        }

        return rowIndex;
    }

    /**
     * Adds data rows to the Excel sheet.
     *
     * @param workbook   The Excel workbook
     * @param sheet      The Excel sheet
     * @param styles     A map of styles
     * @param data       The data to be added
     * @param rowIndex   The current row index
     */
    private void addDataRows(Workbook workbook, Sheet sheet, Map<String, CellStyle> styles, List<Map<String, Object>> data, int rowIndex) {
        int dataRowIndex = 0;
        for (Map<String, Object> rowData : data) {
            Row row = sheet.createRow(rowIndex++);
            dataRowIndex++;
            for (int i = 0; i < TIME_HEADERS.size(); i++) {
                Cell cell = row.createCell(i);
                String key = i == 0 ? "Unternehmen" : "Zeit " + i;
                Object value = rowData.get(key);
                String cellValue = value != null ? value.toString().trim() : "";
                cell.setCellValue(cellValue);

                if (i == 0) {
                    cell.setCellStyle(styles.get("boldThickBorderStyle"));
                } else if (i < TIME_HEADERS.size() - 1) {
                    cell.setCellStyle(styles.get("centeredStyle"));
                } else {
                    CellStyle centeredStyleLastCell = workbook.createCellStyle();
                    centeredStyleLastCell.cloneStyleFrom(styles.get("centeredStyle"));
                    centeredStyleLastCell.setBorderRight(BorderStyle.THICK);
                    cell.setCellStyle(centeredStyleLastCell);
                }

                if (data.size() == dataRowIndex) {
                    CellStyle centeredStyleLastLineCell = workbook.createCellStyle();
                    centeredStyleLastLineCell.cloneStyleFrom(styles.get("centeredStyle"));
                    centeredStyleLastLineCell.setBorderBottom(BorderStyle.THICK);
                    cell.setCellStyle(centeredStyleLastLineCell);
                    if (i == 0) {
                        CellStyle newStyle = workbook.createCellStyle();
                        newStyle.cloneStyleFrom(styles.get("boldThickBorderStyle"));
                        newStyle.setBorderBottom(BorderStyle.THICK);
                        cell.setCellStyle(newStyle);
                    }
                    if (i == TIME_HEADERS.size() - 1) {
                        CellStyle newStyle = workbook.createCellStyle();
                        newStyle.cloneStyleFrom(styles.get("centeredStyle"));
                        newStyle.setBorderBottom(BorderStyle.THICK);
                        newStyle.setBorderRight(BorderStyle.THICK);
                        cell.setCellStyle(newStyle);
                    }
                }
            }
        }
    }
}