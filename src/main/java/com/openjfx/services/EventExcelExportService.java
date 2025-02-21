package com.openjfx.services;

import java.io.IOException;
import java.util.*;

public class EventExcelExportService {

    private final ExcelService excelService;

    public EventExcelExportService(ExcelService excelService) {
        this.excelService = excelService;
    }

    public void exportEventData(String filePath, List<Map<String, Object>> data) throws IOException {
        if (data.isEmpty()) {
            throw new IllegalArgumentException("Datenliste darf nicht leer sein.");
        }

        // Eventname und Uhrzeit aus den ersten zwei Zeilen extrahieren
        String eventName = data.get(0).getOrDefault("Veranstaltung", "Unbekannte Veranstaltung").toString();
        String timeSlot = data.get(1).getOrDefault("Uhrzeit", "Unbekannte Uhrzeit").toString();

        // Entfernen der ersten beiden Zeilen, damit sie nicht doppelt exportiert werden
        List<Map<String, Object>> formattedData = new ArrayList<>(data.subList(2, data.size()));

        // Veranstaltungsname und Uhrzeit als separate Zeilen hinzufügen
        Map<String, Object> eventRow = new LinkedHashMap<>();
        eventRow.put("Veranstaltung", eventName);
        formattedData.add(0, eventRow);

        Map<String, Object> timeRow = new LinkedHashMap<>();
        timeRow.put("Uhrzeit", timeSlot);
        formattedData.add(1, timeRow);

        // Excel-Datei erstellen
        excelService.createExcelFileCustom(formattedData, filePath);
    }
}
