package com.openjfx.services;

import java.io.IOException;
import java.util.*;

public class ChoiceExcelExportService {

    private final ExcelService excelService;

    public ChoiceExcelExportService(ExcelService excelService) {
        this.excelService = excelService;
    }

    public void exportChoiceData(String filePath, List<Map<String, Object>> data, List<String> headers) throws IOException {
        if (data.isEmpty()) {
            throw new IllegalArgumentException("Datenliste darf nicht leer sein.");
        }

        // Daten nach Klasse und Name gruppieren
        Map<String, Map<String, List<Map<String, Object>>>> groupedData = new LinkedHashMap<>();
        for (Map<String, Object> row : data) {
            String klasse = (String) row.get("Klasse");
            String name = (String) row.get("Name");

            // Entferne die letzten beiden Spalten (Name und Klasse) aus den Datenzeilen
            Map<String, Object> cleanedRow = new LinkedHashMap<>(row);
            cleanedRow.remove("Name");
            cleanedRow.remove("Klasse");

            groupedData.computeIfAbsent(klasse, k -> new LinkedHashMap<>())
                    .computeIfAbsent(name, n -> new ArrayList<>())
                    .add(cleanedRow);
        }

        // Excel-Datei exportieren
        exportDataToExcel(groupedData, headers, filePath);
    }

    public void exportDataToExcel(Map<String, Map<String, List<Map<String, Object>>>> groupedData, List<String> headers, String filePath) throws IOException {
        // Erstellen einer neuen Liste mit Kopfzeilen und Daten
        List<Map<String, Object>> formattedData = new ArrayList<>();

        // Durchlaufe die gruppierten Daten
        for (Map.Entry<String, Map<String, List<Map<String, Object>>>> klasseEntry : groupedData.entrySet()) {
            String klasse = klasseEntry.getKey();
            Map<String, List<Map<String, Object>>> nameMap = klasseEntry.getValue();

            // Klasse als Überschrift hinzufügen
            Map<String, Object> klasseRow = new LinkedHashMap<>();
            klasseRow.put("Klasse", klasse);
            formattedData.add(klasseRow);

            // Durchlaufe die Namen
            for (Map.Entry<String, List<Map<String, Object>>> nameEntry : nameMap.entrySet()) {
                String name = nameEntry.getKey();
                List<Map<String, Object>> rows = nameEntry.getValue();

                // Name als Überschrift hinzufügen
                Map<String, Object> nameRow = new LinkedHashMap<>();
                nameRow.put("Name", name);
                formattedData.add(nameRow);

                // Spaltenüberschriften hinzufügen
                Map<String, Object> headerRow = new LinkedHashMap<>();
                for (String header : headers) {
                    headerRow.put(header, header);
                }
                formattedData.add(headerRow);

                // Datenzeilen hinzufügen
                formattedData.addAll(rows);

                // Leere Zeile zwischen den Namen hinzufügen
                formattedData.add(new LinkedHashMap<>());
            }
        }

        // Excel-Datei erstellen
        excelService.createExcelFileCustom(formattedData, filePath);
    }
}