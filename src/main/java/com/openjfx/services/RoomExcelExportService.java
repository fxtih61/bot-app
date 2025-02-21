package com.openjfx.services;

import com.openjfx.services.ExcelService;

import java.io.IOException;
import java.util.*;

public class ExcelExportService {

    private final ExcelService excelService;

    public ExcelExportService(ExcelService excelService) {
        this.excelService = excelService;
    }

    public void exportDataFromImage(String filePath) throws IOException {
        // Manuelle Strukturierung der Daten basierend auf dem hochgeladenen Bild
        List<Map<String, Object>> data = new ArrayList<>();

        // Spaltenüberschriften (Uhrzeiten)
        List<String> headers = Arrays.asList(
                "Unternehmen", "8:45 - 9:30 (A)", "9:50 - 10:35 (B)",
                "10:35 - 11:20 (C)", "11:40 - 12:25 (D)", "12:25 - 13:10 (E)"
        );

        // Beispielhafte Datensätze aus dem Bild
        addRow(data, headers, "Zentis", "209", "", "", "", "");
        addRow(data, headers, "Babor Kosmetik", "109", "109", "", "108", "");
        addRow(data, headers, "RWTH Aachen", "", "", "108", "", "101");
        addRow(data, headers, "Sparkasse Aachen", "102", "102", "102", "", "");
        addRow(data, headers, "Auto Thüllen", "101", "101", "101", "", "");
        addRow(data, headers, "Finanzamt", "103", "103", "103", "103", "103");
        addRow(data, headers, "Zoll Aachen", "113", "113", "113", "113", "");
        addRow(data, headers, "Polizei", "Aula", "Aula", "Aula", "Aula", "");
        addRow(data, headers, "RWTH-Studienberatung", "", "209", "209", "", "");
        addRow(data, headers, "BWL, Wirtschaftsrecht", "106", "", "106", "", "106");

        // Bestehende Methode zur Excel-Erstellung aufrufen
        exportDataToExcel(data, filePath);
    }

    private void addRow(List<Map<String, Object>> data, List<String> headers, String... values) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            row.put(headers.get(i), i < values.length ? values[i] : "");
        }
        data.add(row);
    }

    public void exportDataToExcel(List<Map<String, Object>> data, String filePath) throws IOException {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data list must not be null or empty");
        }

        Map<String, Object> firstRow = data.get(0);
        if (firstRow == null || firstRow.isEmpty()) {
            throw new IllegalArgumentException("Data must contain at least one row with valid headers");
        }

        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path must not be null or empty");
        }

        excelService.createExcelFile(data, filePath);
    }
}
