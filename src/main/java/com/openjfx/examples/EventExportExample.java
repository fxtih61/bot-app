package com.openjfx.examples;

import com.openjfx.services.EventExcelExportService;
import com.openjfx.services.ExcelService;

import java.io.IOException;
import java.util.*;

public class EventExportExample {

    public static void main(String[] args) {
        // Beispielhafte Daten erstellen
        List<Map<String, Object>> data = new ArrayList<>();

        // Eventname und Uhrzeit als eigene Zeilen hinzufügen
        addRow(data, Collections.singletonList("Veranstaltung"), "Babor Kosmetik");
        addRow(data, Collections.singletonList("Uhrzeit"), "9:45-9:30");

        // Spaltenüberschriften explizit definieren
        List<String> headers = Arrays.asList("Klasse", "Name", "Vorname", "Anwesend?");
        addRow(data, headers, headers.toArray(new String[0])); // Kopfzeile in die Daten aufnehmen

        // Teilnehmerdaten hinzufügen
        addRow(data, headers, "ASS221", "Iskender", "Iclal Ece", "");
        addRow(data, headers, "ASS221", "Karakas", "Berfin", "");
        addRow(data, headers, "HÖH221", "Bendels", "Lea", "");
        addRow(data, headers, "HÖH221", "Luwawu", "Espoir", "");
        addRow(data, headers, "HÖH224", "Erol", "Iclal", "");
        addRow(data, headers, "HÖH224", "Erten", "Volkan Burak", "");
        addRow(data, headers, "HÖH224", "Gümez", "Koray", "");
        addRow(data, headers, "HÖH224", "Korkut", "Pelsin", "");
        addRow(data, headers, "WG221", "Ömer", "Diler", "");
        addRow(data, headers, "WG221", "Tourniaire", "Serafina", "");

        // ExcelService und EventExcelExportService instanziieren
        ExcelService excelService = new ExcelService(); // Angenommen, ExcelService ist bereits implementiert
        EventExcelExportService exportService = new EventExcelExportService(excelService);

        // Daten exportieren
        try {
            exportService.exportEventData("Event_Export.xlsx", data);
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
        data.add(row);
    }
}
