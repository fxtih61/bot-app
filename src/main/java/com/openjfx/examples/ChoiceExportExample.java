package com.openjfx.examples;

import com.openjfx.services.ChoiceExcelExportService;
import com.openjfx.services.ExcelService;

import java.io.IOException;
import java.util.*;

public class ChoiceExportExample {

    public static void main(String[] args) {
        // Beispielhafte Daten erstellen
        List<Map<String, Object>> data = new ArrayList<>();
        List<String> headers = Arrays.asList("Zeit", "Raum", "Veranstaltung", "Wunsch");

        // Beispiel 1: Jane Doe
        addRow(data, headers, "08:45-9:30", "103", "Finanzamt", "3", "Jane Doe", "ASS221");
        addRow(data, headers, "9:50-10:35", "103", "Finanzamt", "1", "Jane Doe", "ASS221");
        addRow(data, headers, "10:35-11:20", "102", "Aachener Sparkasse", "5", "Jane Doe", "ASS221");
        addRow(data, headers, "11:40-12:25", "102", "Justizvollzugsanstalt", "4", "Jane Doe", "ASS221");
        addRow(data, headers, "12:25-13:10", "107", "Debeka", "2", "Jane Doe", "ASS221");

        // Beispiel 2: Max Mustermann
        addRow(data, headers, "08:45-9:30", "106", "Soziale Arbeit", "6", "Max Mustermann", "ASS221");
        addRow(data, headers, "9:50-10:35", "008", "FH Aachen - Studienberatung", "3", "Max Mustermann", "ASS221");
        addRow(data, headers, "10:35-11:20", "112", "Rechtsanwaltsberufe", "1", "Max Mustermann", "ASS221");
        addRow(data, headers, "11:40-12:25", "113", "Zoll Aachen", "-", "Max Mustermann", "ASS221");
        addRow(data, headers, "12:25-13:10", "Aula", "Polizei", "2", "Max Mustermann", "ASS221");

        // ExcelService und ChoiceExcelExportService instanziieren
        ExcelService excelService = new ExcelService(); // Angenommen, ExcelService ist bereits implementiert
        ChoiceExcelExportService exportService = new ChoiceExcelExportService(excelService);

        // Daten exportieren
        try {
            exportService.exportChoiceData("Choice_Export.xlsx", data, headers);
            System.out.println("Daten erfolgreich exportiert.");
        } catch (IOException e) {
            System.err.println("Fehler beim Export der Daten: " + e.getMessage());
        }
    }

    public static void addRow(List<Map<String, Object>> data, List<String> headers, String... values) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            row.put(headers.get(i), i < values.length ? values[i] : "");
        }
        row.put("Name", values[values.length - 2]);
        row.put("Klasse", values[values.length - 1]);
        data.add(row);
    }
}