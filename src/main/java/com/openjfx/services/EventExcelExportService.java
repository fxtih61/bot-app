package com.openjfx.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This service class provides functionality to export event data, including time slots and participants,
 * to an Excel file. It uses Apache POI to create and format the Excel workbook.
 *
 * @author leon
 */
public class EventExcelExportService {

    /**
     * Exports the provided event data to an Excel file at the specified file path.
     *
     * @param filePath  The path where the Excel file will be saved.
     * @param eventData A map containing event details, including the event name, time slots, and participants.
     * @throws IOException If an I/O error occurs during file writing.
     * @throws IllegalArgumentException If the event data is empty.
     *
     * @author leon
     */
    public void exportEventData(String filePath, Map<String, Object> eventData) throws IOException {
        if (eventData.isEmpty()) {
            throw new IllegalArgumentException("Event data must not be empty.");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");

            int rowIndex = 0;

            // Style for the title "Anwesenheitsliste" with font size 16 (no borders)
            CellStyle titleStyle = createTitleStyle(workbook);

            // Style for the event name with font size 16 (no borders)
            CellStyle eventStyle = createEventStyle(workbook);

            // Style for the time slots with font size 11 (no borders)
            CellStyle timeStyle = createTimeStyle(workbook);

            // Style for the headers (with borders)
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Style for the data rows (with borders)
            CellStyle dataStyle = createDataStyle(workbook);

            // Add the title "Anwesenheitsliste"
            Row titleRow = sheet.createRow(rowIndex++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Anwesenheitsliste");
            titleCell.setCellStyle(titleStyle);

            // Add the event name
            Row eventRow = sheet.createRow(rowIndex++);
            Cell eventCell = eventRow.createCell(0);
            eventCell.setCellValue((String) eventData.get("Veranstaltung"));
            eventCell.setCellStyle(eventStyle);

            // Define headers for the table
            String[] headers = {"Klasse", "Name", "Vorname", "Anwesend?"};

            // Add time slots and participant data
            List<Map<String, Object>> timeSlots = (List<Map<String, Object>>) eventData.get("Zeitfenster");
            for (Map<String, Object> timeSlot : timeSlots) {
                // Add the time slot (without borders)
                Row timeRow = sheet.createRow(rowIndex++);
                Cell timeCell = timeRow.createCell(0);
                timeCell.setCellValue((String) timeSlot.get("Uhrzeit"));
                timeCell.setCellStyle(timeStyle);

                // Add headers (with borders)
                Row headerRow = sheet.createRow(rowIndex++);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle); // Borders for headers
                }

                // Add participant data (with borders)
                List<Map<String, String>> participants = (List<Map<String, String>>) timeSlot.get("Teilnehmer");
                for (Map<String, String> participant : participants) {
                    Row row = sheet.createRow(rowIndex++);
                    for (int i = 0; i < headers.length; i++) {
                        Cell cell = row.createCell(i);
                        cell.setCellValue(participant.get(headers[i]));
                        cell.setCellStyle(dataStyle); // Borders for data rows
                    }
                }

                // Add an empty row after each time slot
                sheet.createRow(rowIndex++);
            }

            // Adjust column widths
            adjustColumnWidths(sheet);

            // Save the workbook to the specified file path
            saveWorkbook(workbook, filePath);
        }
    }

    /**
     * Creates a cell style for the title with a font size of 16 and no borders.
     *
     * @param workbook The workbook to create the style in.
     * @return The created cell style.
     *
     * @author leon
     */
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        return titleStyle;
    }

    /**
     * Creates a cell style for the event name with a bold font size of 16 and no borders.
     *
     * @param workbook The workbook to create the style in.
     * @return The created cell style.
     *
     * @author leon
     */
    private CellStyle createEventStyle(Workbook workbook) {
        CellStyle eventStyle = workbook.createCellStyle();
        Font eventFont = workbook.createFont();
        eventFont.setFontHeightInPoints((short) 16);
        eventFont.setBold(true);
        eventStyle.setFont(eventFont);
        return eventStyle;
    }

    /**
     * Creates a cell style for the time slots with a bold font size of 11 and no borders.
     *
     * @param workbook The workbook to create the style in.
     * @return The created cell style.
     *
     * @author leon
     */
    private CellStyle createTimeStyle(Workbook workbook) {
        CellStyle timeStyle = workbook.createCellStyle();
        Font timeFont = workbook.createFont();
        timeFont.setFontHeightInPoints((short) 11);
        timeFont.setBold(true);
        timeStyle.setFont(timeFont);
        return timeStyle;
    }

    /**
     * Creates a cell style for the headers with bold text and thin borders.
     *
     * @param workbook The workbook to create the style in.
     * @return The created cell style.
     *
     * @author leon
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        return headerStyle;
    }

    /**
     * Creates a cell style for the data rows with thin borders.
     *
     * @param workbook The workbook to create the style in.
     * @return The created cell style.
     *
     * @author leon
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        return dataStyle;
    }

    /**
     * Adjusts the column widths for the sheet to ensure all data is visible.
     *
     * @param sheet The sheet to adjust the column widths for.
     *
     * @author leon
     */
    private void adjustColumnWidths(Sheet sheet) {
        sheet.setColumnWidth(0, 10 * 256); // Klasse
        sheet.setColumnWidth(1, 15 * 256); // Name
        sheet.setColumnWidth(2, 20 * 256); // Vorname
        sheet.setColumnWidth(3, 12 * 256); // Anwesend?
    }

    /**
     * Saves the workbook to the specified file path.
     *
     * @param workbook The workbook to save.
     * @param filePath The path where the workbook will be saved.
     * @throws IOException If an I/O error occurs during file writing.
     *
     * @author leon
     */
    private void saveWorkbook(Workbook workbook, String filePath) throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }
    }
}