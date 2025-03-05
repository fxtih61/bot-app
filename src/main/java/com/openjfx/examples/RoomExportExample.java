package com.openjfx.examples;

import com.openjfx.services.AssignmentService;
import com.openjfx.services.RoomExcelExportService;
import com.openjfx.services.TimeSlotService;
import com.openjfx.services.TimetableService;

import java.io.IOException;
import java.util.*;

public class RoomExportExample {

    public static void main(String[] args) {
        // Daten für die Tabelle
        List<Map<String, Object>> data = new ArrayList<>();

        //TimetableService timeSlotService = new TimetableService();
        //timeSlotService.loadTimeTableAssignments();

        // Beispielhafte Datensätze hinzufügen
        addRow(data, "Zentis", "209", "", "", "", "");
        addRow(data, "Babor Kosmetik", "109", "109", "", "", "");
        addRow(data, "RWTH Aachen", "", "", "108", "108", "108");
        addRow(data, "Aldi", "", "", "", "101", "101");
        addRow(data, "Bauhaus", "", "112", "", "", "");
        addRow(data, "Sparkasse Aachen", "102", "102", "102", "", "");
        addRow(data, "Api Computerhandels GmbH", "", "", "109", "", "");
        addRow(data, "Debeka", "", "", "", "107", "107");
        addRow(data, "Steuerberaterkammer Köln", "", "", "008", "008", "008");
        addRow(data, "Rechtsanwaltsberufe", "", "", "112", "112", "112");
        addRow(data, "Notarberufe", "112", "", "", "", "");
        addRow(data, "Spedition Hammer Advanced Logistics", "", "108", "", "", "");
        addRow(data, "Inform", "107", "107", "107", "", "");
        addRow(data, "Auto Thüllen", "101", "101", "101", "", "");
        addRow(data, "StädteRegion Aachen", "", "", "", "109", "109");
        addRow(data, "Finanzamt", "103", "103", "103", "", "");
        addRow(data, "Finanzamt", "", "", "", "103", "103");
        addRow(data, "Zoll Aachen", "113", "113", "113", "113", "");
        addRow(data, "Polizei", "Aula", "Aula", "Aula", "Aula", "Aula");
        addRow(data, "Feuerwehr Aachen", "", "", "", "102", "102");
        addRow(data, "OGS/ Kita", "", "", "", "", "113");
        addRow(data, "FH Aachen - Studienberatung", "008", "008", "", "", "");
        addRow(data, "RWTH-Studienberatung", "", "209", "209", "", "");
        addRow(data, "Lehramt Berufskolleg", "108", "", "", "", "");
        addRow(data, "Soziale Arbeit", "106", "", "", "", "");
        addRow(data, "BWL, Wirtschaftsrecht und Global Business and Economics", "", "106", "106", "106", "");
        addRow(data, "EVA", "", "", "", "", "106");

        // Excel-Export
        RoomExcelExportService exportService = new RoomExcelExportService();
        try {
            exportService.exportDataToExcel(data, "EXPORT BOT4 Raum- und Zeitplan.xlsx");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addRow(List<Map<String, Object>> data, String unternehmen, String... raumZeiten) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("Unternehmen", unternehmen);
        for (int i = 0; i < raumZeiten.length; i++) {
            row.put("Zeit " + (i + 1), raumZeiten[i]);
        }
        data.add(row);
    }
}