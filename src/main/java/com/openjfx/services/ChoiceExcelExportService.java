package com.openjfx.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * This class provides functionality to export data to an Excel file.
 * It groups data by class and name, and formats the Excel sheet accordingly.
 *
 * @author leon
 */
public class ChoiceExcelExportService {

    private static final List<String> HEADERS = Arrays.asList("Zeit", "Raum", "Veranstaltung", "Beschreibung", "Wunsch");

    /**
     * Exports the provided data to an Excel file.
     *
     * @param filePath The path where the Excel file will be saved.
     * @param data     The data to be exported.
     * @throws IOException If an I/O error occurs.
     *
     * @author leon
     */
    public void exportChoiceData(String filePath, List<Map<String, Object>> data) throws IOException {
        if (data.isEmpty()) {
            throw new IllegalArgumentException("Data list must not be empty.");
        }

        // Group data by class and name
        Map<String, Map<String, List<Map<String, Object>>>> groupedData = groupDataByClassAndName(data);

        // Export the grouped data to an Excel file
        exportDataToExcel(groupedData, filePath);
    }

    /**
     * Groups the data by class and name.
     *
     * @param data The data to be grouped.
     * @return A map containing the grouped data.
     *
     * @author leon
     */
    private Map<String, Map<String, List<Map<String, Object>>>> groupDataByClassAndName(List<Map<String, Object>> data) {
        Map<String, Map<String, List<Map<String, Object>>>> groupedData = new LinkedHashMap<>();
        for (Map<String, Object> row : data) {
            String klasse = (String) row.get("Klasse");
            String name = (String) row.get("Name");

            // Remove the last two columns (Name and Klasse) from the data rows
            Map<String, Object> cleanedRow = new LinkedHashMap<>(row);
            cleanedRow.remove("Name");
            cleanedRow.remove("Klasse");

            groupedData.computeIfAbsent(klasse, k -> new LinkedHashMap<>())
                    .computeIfAbsent(name, n -> new ArrayList<>())
                    .add(cleanedRow);
        }
        return groupedData;
    }

    /**
     * Exports the grouped data to an Excel file.
     *
     * @param groupedData The grouped data to be exported.
     * @param filePath    The path where the Excel file will be saved.
     * @throws IOException If an I/O error occurs.
     *
     * @author leon
     */
    private void exportDataToExcel(Map<String, Map<String, List<Map<String, Object>>>> groupedData, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");

            // Create styles for headers and data
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle grayCellStyle = createGrayCellStyle(workbook);

            int rowIndex = 0;

            // Iterate through the grouped data
            for (Map.Entry<String, Map<String, List<Map<String, Object>>>> klasseEntry : groupedData.entrySet()) {
                String klasse = klasseEntry.getKey();
                Map<String, List<Map<String, Object>>> nameMap = klasseEntry.getValue();

                // Write class as a header
                rowIndex = writeClassHeader(sheet, klasse, headerStyle, rowIndex);

                // Iterate through the names
                for (Map.Entry<String, List<Map<String, Object>>> nameEntry : nameMap.entrySet()) {
                    String name = nameEntry.getKey();
                    List<Map<String, Object>> rows = nameEntry.getValue();

                    // Write name as a header
                    rowIndex = writeNameHeader(sheet, name, headerStyle, rowIndex);

                    // Write column headers
                    rowIndex = writeColumnHeaders(sheet, headerStyle, rowIndex);

                    // Color the cell at A3 gray
                    rowIndex = colorA3CellGray(sheet, grayCellStyle, rowIndex);

                    // Write data rows
                    rowIndex = writeDataRows(sheet, rows, dataStyle, rowIndex);

                    // Add an empty row between names
                    rowIndex++;
                }
            }

            // Adjust column widths
            autoSizeColumns(sheet);

            // Manually adjust the "Beschreibung" column width
            adjustBeschreibungColumnWidth(sheet);

            // Save the file
            saveWorkbook(workbook, filePath);
        }
    }

    /**
     * Creates a style for the headers.
     *
     * @param workbook The workbook to create the style in.
     * @return The created header style.
     *
     * @author leon
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        return headerStyle;
    }

    /**
     * Creates a style for the data cells.
     *
     * @param workbook The workbook to create the style in.
     * @return The created data style.
     *
     * @author leon
     */
    private CellStyle createDataStyle(Workbook workbook) {
        Font dataFont = workbook.createFont();
        dataFont.setFontHeightInPoints((short) 12);

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setFont(dataFont);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        return dataStyle;
    }

    /**
     * Creates a style for the gray cell at A3.
     *
     * @param workbook The workbook to create the style in.
     * @return The created gray cell style.
     *
     * @author leon
     */
    private CellStyle createGrayCellStyle(Workbook workbook) {
        Font dataFont = workbook.createFont();
        dataFont.setFontHeightInPoints((short) 12);

        CellStyle grayCellStyle = workbook.createCellStyle();
        grayCellStyle.setFont(dataFont);
        grayCellStyle.setBorderTop(BorderStyle.THIN);
        grayCellStyle.setBorderBottom(BorderStyle.THIN);
        grayCellStyle.setBorderLeft(BorderStyle.THIN);
        grayCellStyle.setBorderRight(BorderStyle.THIN);
        grayCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        grayCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        return grayCellStyle;
    }

    /**
     * Writes the class header to the sheet.
     *
     * @param sheet      The sheet to write to.
     * @param klasse     The class name.
     * @param headerStyle The style for the header.
     * @param rowIndex   The current row index.
     * @return The updated row index.
     *
     * @author leon
     */
    private int writeClassHeader(Sheet sheet, String klasse, CellStyle headerStyle, int rowIndex) {
        Row klasseRow = sheet.createRow(rowIndex++);
        Cell klasseCell = klasseRow.createCell(0);
        klasseCell.setCellValue(klasse);
        klasseCell.setCellStyle(headerStyle);
        return rowIndex;
    }

    /**
     * Writes the name header to the sheet.
     *
     * @param sheet      The sheet to write to.
     * @param name       The name.
     * @param headerStyle The style for the header.
     * @param rowIndex   The current row index.
     * @return The updated row index.
     *
     * @author leon
     */
    private int writeNameHeader(Sheet sheet, String name, CellStyle headerStyle, int rowIndex) {
        Row nameRow = sheet.createRow(rowIndex++);
        Cell nameCell = nameRow.createCell(0);
        nameCell.setCellValue(name);
        nameCell.setCellStyle(headerStyle);
        return rowIndex;
    }

    /**
     * Writes the column headers to the sheet.
     *
     * @param sheet      The sheet to write to.
     * @param headerStyle The style for the headers.
     * @param rowIndex   The current row index.
     * @return The updated row index.
     *
     * @author leon
     */
    private int writeColumnHeaders(Sheet sheet, CellStyle headerStyle, int rowIndex) {
        Row headerRow = sheet.createRow(rowIndex++);
        for (int i = 0; i < HEADERS.size(); i++) {
            Cell cell = headerRow.createCell(i + 1);
            cell.setCellValue(HEADERS.get(i));
            cell.setCellStyle(headerStyle);
        }
        return rowIndex;
    }

    /**
     * Colors the cell at A3 gray.
     *
     * @param sheet         The sheet to write to.
     * @param grayCellStyle The style for the gray cell.
     * @param rowIndex      The current row index.
     * @return The updated row index.
     *
     * @author leon
     */
    private int colorA3CellGray(Sheet sheet, CellStyle grayCellStyle, int rowIndex) {
        Row headerRow = sheet.getRow(rowIndex - 1); // Get the header row
        Cell a3Cell = headerRow.createCell(0); // First column (A3)
        a3Cell.setCellStyle(grayCellStyle); // Gray background, but empty
        return rowIndex;
    }

    /**
     * Writes the data rows to the sheet.
     *
     * @param sheet     The sheet to write to.
     * @param rows      The data rows.
     * @param dataStyle The style for the data cells.
     * @param rowIndex  The current row index.
     * @return The updated row index.
     *
     * @author leon
     */
    private int writeDataRows(Sheet sheet, List<Map<String, Object>> rows, CellStyle dataStyle, int rowIndex) {
        char rowLabel = 'A'; // Start with 'A'
        for (Map<String, Object> row : rows) {
            Row dataRow = sheet.createRow(rowIndex++);
            Cell labelCell = dataRow.createCell(0); // Row label in the first column
            labelCell.setCellValue(String.valueOf(rowLabel)); // Row label (A, B, C, ...)
            labelCell.setCellStyle(dataStyle);

            int cellIndex = 1; // Start at 1, since the first column contains the row label
            for (String header : HEADERS) {
                Cell cell = dataRow.createCell(cellIndex++);
                Object value = row.get(header);
                cell.setCellValue(value != null ? value.toString() : "");
                cell.setCellStyle(dataStyle);
            }
            rowLabel++; // Next letter
        }
        return rowIndex;
    }

    /**
     * Automatically adjusts the column widths.
     *
     * @param sheet The sheet to adjust.
     *
     * @author leon
     */
    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < HEADERS.size() + 1; i++) { // +1 for the row label column
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Manually adjusts the width of the "Beschreibung" column.
     *
     * @param sheet The sheet to adjust.
     *
     * @author leon
     */
    private void adjustBeschreibungColumnWidth(Sheet sheet) {
        int beschreibungIndex = HEADERS.indexOf("Beschreibung") + 1; // +1 for the row label column
        if (beschreibungIndex != -1) {
            sheet.setColumnWidth(beschreibungIndex, 20000); // Width for the "Beschreibung" column
        }
    }

    /**
     * Saves the workbook to the specified file path.
     *
     * @param workbook The workbook to save.
     * @param filePath The path where the file will be saved.
     * @throws IOException If an I/O error occurs.
     *
     * @author leon
     */
    private void saveWorkbook(Workbook workbook, String filePath) throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }
    }
}