package com.openjfx.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class RoomExcelExportService {

    // Allgemeine Überschriften
    private static final List<String> GENERAL_HEADERS = Arrays.asList(
            "Organisationsplan für den Berufsorientierungstag",
            "8:30 bis 8:45 Uhr Begrüßung und Einführung in der Aula",
            "13:10 bis 13:20 Uhr Abschluss im Klassenverbund"
    );

    // Spaltenüberschriften (Uhrzeiten)
    private static final List<String> TIME_HEADERS = Arrays.asList(
            "", "8:45 - 9:30", "9:50 - 10:35",
            "10:35 - 11:20", "11:40 - 12:25", "12:25 - 13:10"
    );

    // Buchstaben (A, B, C, D, E) für die separate Zeile
    private static final List<String> LETTER_HEADERS = Arrays.asList(
            "", "A", "B", "C", "D", "E"
    );

    public void exportDataToExcel(List<Map<String, Object>> data, String filePath) throws IOException {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data list must not be null or empty");
        }

        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path must not be null or empty");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Raumplan");

            // Rahmenstil für alle Zellen (dünn)
            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);

            // Rahmenstil für den unteren Teil (dick)
            CellStyle thickBorderStyle = workbook.createCellStyle();
            thickBorderStyle.setBorderTop(BorderStyle.THICK); // Dicker Rahmen oben
            thickBorderStyle.setBorderBottom(BorderStyle.THICK); // Dicker Rahmen unten
            thickBorderStyle.setBorderLeft(BorderStyle.THICK); // Dicker Rahmen links
            thickBorderStyle.setBorderRight(BorderStyle.THICK); // Dicker Rahmen rechts

            // Hintergrundfarbe für Überschriften
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.cloneStyleFrom(borderStyle);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex()); // Graue Hintergrundfarbe
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.LEFT);
            headerStyle.setWrapText(true);

            // Schriftart für Überschriften (Uhrzeiten)
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 10); // Schriftgröße 10 für Uhrzeiten
            headerStyle.setFont(headerFont);

            // Stil für den Organisationsplan (dick und Schriftgröße 14)
            CellStyle orgPlanStyle = workbook.createCellStyle();
            orgPlanStyle.cloneStyleFrom(borderStyle);
            orgPlanStyle.setAlignment(HorizontalAlignment.LEFT);
            orgPlanStyle.setWrapText(true);
            Font orgPlanFont = workbook.createFont();
            orgPlanFont.setBold(true); // Dicke Schrift
            orgPlanFont.setFontHeightInPoints((short) 14); // Schriftgröße 14
            orgPlanStyle.setFont(orgPlanFont);

            // Stil für die Buchstaben (A, B, C, D, E)
            CellStyle letterStyle = workbook.createCellStyle();
            letterStyle.cloneStyleFrom(borderStyle);
            letterStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex()); // Graue Hintergrundfarbe
            letterStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            letterStyle.setAlignment(HorizontalAlignment.CENTER); // Zentrierte Ausrichtung
            Font letterFont = workbook.createFont();
            letterFont.setBold(true);
            letterFont.setFontHeightInPoints((short) 11); // Schriftgröße 11 für Buchstaben
            letterStyle.setFont(letterFont);

            // Stil für die unteren Daten (fett und dicker Rahmen)
            CellStyle boldThickBorderStyle = workbook.createCellStyle();
            boldThickBorderStyle.cloneStyleFrom(borderStyle); // Dicker Rahmen übernehmen
            boldThickBorderStyle.setBorderRight(BorderStyle.THICK);
            Font boldFont = workbook.createFont();
            boldFont.setBold(true); // Schrift fett
            boldThickBorderStyle.setFont(boldFont);

            // Stil für zentrierte Zellen (Räume)
            CellStyle centeredStyle = workbook.createCellStyle();
            centeredStyle.cloneStyleFrom(boldThickBorderStyle); // Dicker Rahmen und Fettschrift übernehmen
            centeredStyle.setBorderTop(BorderStyle.THIN);
            centeredStyle.setBorderBottom(BorderStyle.THIN);
            centeredStyle.setBorderLeft(BorderStyle.THIN);
            centeredStyle.setBorderRight(BorderStyle.THIN);
            centeredStyle.setAlignment(HorizontalAlignment.CENTER); // Zentrierte Ausrichtung

            // Stil für die allgemeinen Überschriften (dick, Schriftgröße 11 und grauer Hintergrund)
            CellStyle generalHeaderStyle = workbook.createCellStyle();
            generalHeaderStyle.cloneStyleFrom(headerStyle); // Graue Hintergrundfarbe übernehmen
            generalHeaderStyle.setAlignment(HorizontalAlignment.LEFT);
            generalHeaderStyle.setWrapText(true);
            Font generalHeaderFont = workbook.createFont();
            generalHeaderFont.setBold(true); // Dicke Schrift
            generalHeaderFont.setFontHeightInPoints((short) 11); // Schriftgröße 11
            generalHeaderStyle.setFont(generalHeaderFont);

            int rowIndex = 0;

            // Allgemeine Überschriften hinzufügen
            for (String header : GENERAL_HEADERS) {
                Row row = sheet.createRow(rowIndex++);
                Cell cell = row.createCell(0);
                cell.setCellValue(header);

                if (header.equals("Organisationsplan für den Berufsorientierungstag")) {
                    cell.setCellStyle(orgPlanStyle); // Stil für den Organisationsplan (dick und Schriftgröße 14)
                } else {
                    cell.setCellStyle(generalHeaderStyle); // Stil für die anderen Überschriften (dick, Schriftgröße 11 und grauer Hintergrund)
                }

                sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, TIME_HEADERS.size() - 1));

                // Leere Zeile nach "13:10 bis 13:20 Uhr Abschluss im Klassenverbund" einfügen
                if (header.equals("13:10 bis 13:20 Uhr Abschluss im Klassenverbund")) {
                    sheet.createRow(rowIndex++); // Leere Zeile hinzufügen
                }
            }

            // Spaltenüberschriften (Uhrzeiten) hinzufügen
            Row timeHeaderRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < TIME_HEADERS.size(); i++) {
                Cell cell = timeHeaderRow.createCell(i);
                cell.setCellValue(TIME_HEADERS.get(i));

                // Rahmenstil für die Uhrzeiten-Zeile anpassen (kein unterer Rahmen)
                CellStyle timeHeaderCellStyle = workbook.createCellStyle();
                timeHeaderCellStyle.cloneStyleFrom(headerStyle);
                timeHeaderCellStyle.setBorderBottom(BorderStyle.NONE); // Unteren Rahmen entfernen
                timeHeaderCellStyle.setBorderTop(BorderStyle.THICK);
                if (i == 0) { // first cell
                    timeHeaderCellStyle.setBorderRight(BorderStyle.THICK);
                }
                if (i == TIME_HEADERS.size() -1) { // last cell
                    timeHeaderCellStyle.setBorderRight(BorderStyle.THICK);
                }
                cell.setCellStyle(timeHeaderCellStyle);
            }

            // Buchstaben (A, B, C, D, E) in separater Zeile hinzufügen
            Row letterHeaderRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < LETTER_HEADERS.size(); i++) {
                Cell cell = letterHeaderRow.createCell(i);
                cell.setCellValue(LETTER_HEADERS.get(i));

                // Rahmenstil für die Buchstaben-Zeile anpassen (kein oberer Rahmen)
                CellStyle letterHeaderCellStyle = workbook.createCellStyle();
                letterHeaderCellStyle.cloneStyleFrom(letterStyle);
                letterHeaderCellStyle.setBorderTop(BorderStyle.NONE); // Oberen Rahmen entfernen
                if (i == 0) {
                    letterHeaderCellStyle.setBorderRight(BorderStyle.THICK);
                }
                if (i == LETTER_HEADERS.size() -1) { // last cell
                    letterHeaderCellStyle.setBorderRight(BorderStyle.THICK);
                }
                cell.setCellStyle(letterHeaderCellStyle);
            }

            // Daten hinzufügen (fett, dicker Rahmen und zentrierte Räume)
            int dataRowIndex = 0;
            for (Map<String, Object> rowData : data) {
                Row row = sheet.createRow(rowIndex++);
                dataRowIndex++;
                for (int i = 0; i < TIME_HEADERS.size(); i++) {
                    Cell cell = row.createCell(i);
                    String key = i == 0 ? "Unternehmen" : "Zeit " + i;
                    Object value = rowData.get(key);
                    String cellValue = value != null ? value.toString().trim() : ""; // Führende Apostrophe entfernen
                    // Zellenwert setzen
                    cell.setCellValue(cellValue);

                    // Stil anwenden
                    if (i == 0) {
                        cell.setCellStyle(boldThickBorderStyle); // Unternehmen (linksbündig, dicker Rahmen)
                    } else if (i < TIME_HEADERS.size() - 1) { // innere Zelle
                        cell.setCellStyle(centeredStyle); // Räume (zentriert, dicker Rahmen)
                    } else { // letzte zelle in der zeile
                        CellStyle centeredStyleLastCell = workbook.createCellStyle();
                        centeredStyleLastCell.cloneStyleFrom(centeredStyle);
                        centeredStyleLastCell.setBorderRight(BorderStyle.THICK);
                        cell.setCellStyle(centeredStyleLastCell); // Räume (zentriert, dicker Rahmen)
                    }

                    if (data.size() == dataRowIndex){
                        //System.out.println("rowData: "+ rowData);
                        CellStyle centeredStyleLastLineCell = workbook.createCellStyle();
                        centeredStyleLastLineCell.cloneStyleFrom(centeredStyle);
                        centeredStyleLastLineCell.setBorderBottom(BorderStyle.THICK);
                        cell.setCellStyle(centeredStyleLastLineCell); // Räume (zentriert, dicker Rahmen)
                        if (i == 0) {
                            CellStyle newStyle = workbook.createCellStyle();
                            newStyle.cloneStyleFrom(boldThickBorderStyle);
                            newStyle.setBorderBottom(BorderStyle.THICK);
                            cell.setCellStyle(newStyle);
                        }
                        if (i == TIME_HEADERS.size() - 1) {
                            CellStyle newStyle = workbook.createCellStyle();
                            newStyle.cloneStyleFrom(centeredStyle);
                            newStyle.setBorderBottom(BorderStyle.THICK);
                            newStyle.setBorderRight(BorderStyle.THICK);
                            cell.setCellStyle(newStyle);
                        }
                    }
                }
            }

            // Automatische Spaltenbreite anpassen
            for (int i = 0; i < TIME_HEADERS.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            // Datei speichern
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        }
    }
}