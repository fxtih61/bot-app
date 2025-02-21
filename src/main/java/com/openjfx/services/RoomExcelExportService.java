package com.openjfx.services;

import java.io.IOException;
import java.util.*;

public class RoomExcelExportService {

    private final ExcelService excelService;

    public RoomExcelExportService(ExcelService excelService) {
        this.excelService = excelService;
    }

    public void exportDataToExcelWithHeaders(
            List<Map<String, Object>> data,
            List<String> generalHeaders,
            List<String> headers,
            String filePath
    ) throws IOException {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data list must not be null or empty");
        }

        if (generalHeaders == null || generalHeaders.isEmpty()) {
            throw new IllegalArgumentException("General headers must not be null or empty");
        }

        if (headers == null || headers.isEmpty()) {
            throw new IllegalArgumentException("Headers must not be null or empty");
        }

        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path must not be null or empty");
        }

        // Erstellen einer neuen Liste mit Kopfzeilen und Daten
        List<Map<String, Object>> formattedData = new ArrayList<>();

        // Allgemeine Überschriften als separate Zeilen hinzufügen
        for (String header : generalHeaders) {
            Map<String, Object> headerRow = new LinkedHashMap<>();
            // Füge die allgemeine Überschrift als eigenen Eintrag hinzu
            headerRow.put("Header", header); // Verwende einen eigenen Schlüssel für allgemeine Überschriften
            formattedData.add(headerRow);
        }

        // Leere Zeile zwischen allgemeinen Überschriften und Spaltenüberschriften
        formattedData.add(new LinkedHashMap<>());

        // Spaltenüberschriften als erste Datenzeile hinzufügen
        Map<String, Object> columnHeaderRow = new LinkedHashMap<>();
        for (String header : headers) {
            columnHeaderRow.put(header, header); // Füge jede Spaltenüberschrift hinzu
        }
        formattedData.add(columnHeaderRow);

        // Daten hinzufügen
        formattedData.addAll(data);

        // Excel-Datei erstellen
        excelService.createExcelFileCustom(formattedData, filePath);
    }
}