package com.openjfx.examples;

import com.openjfx.services.ExcelService;
import com.openjfx.services.RoomExcelExportService;
import java.io.IOException;
import java.util.*;

public class RoomExportExample {

    public static void main(String[] args) {
        // Beispielhafte Daten
        List<Map<String, Object>> data = new ArrayList<>();

        // Allgemeine Überschriften
        List<String> generalHeaders = Arrays.asList(
                "Organisationsplan für den Berufsorientierungstag",
                "8:30 bis 8:45 Uhr Begrüßung und Einführung in der Aula",
                "13:10 bis 13:20 Uhr Abschluss im Klassenverbund"
        );

        // Spaltenüberschriften (Uhrzeiten)
        List<String> headers = Arrays.asList(
                "Unternehmen", "8:45 - 9:30", "9:50 - 10:35",
                "10:35 - 11:20", "11:40 - 12:25", "12:25 - 13:10"
        );

        // Beispielhafte Datensätze
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

        // Excel-Export
        RoomExcelExportService exportService = new RoomExcelExportService(new ExcelService());
        try {
            exportService.exportDataToExcelWithHeaders(data, generalHeaders, headers, "Room_Export.xlsx");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addRow(List<Map<String, Object>> data, List<String> headers, String... values) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            row.put(headers.get(i), i < values.length ? values[i] : "");
        }
        data.add(row);
    }
}