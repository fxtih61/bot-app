package com.openjfx.examples;

import com.openjfx.services.TimetableService;

import java.io.IOException;
import java.util.*;

/**
 * This class demonstrates how to export event attendance data to an Excel file.
 * It creates sample event data, including time slots and participants, and exports it using the {@link EventExcelExportService}.
 *
 * @author leon
 */
public class EventExportExample {

    /**
     * The main method that initializes sample event data and exports it to an Excel file.
     *
     * @param args Command-line arguments (not used in this example).
     *
     * @author leon
     */
    public static void main(String[] args) {
        // Create sample event data
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("Veranstaltung", "Babor Kosmetik"); // Event name

        // List to hold time slots for the event
        List<Map<String, Object>> timeSlots = new ArrayList<>();

        // First time slot: 9:45-9:30
        Map<String, Object> timeSlot1 = new HashMap<>();
        timeSlot1.put("Uhrzeit", "8:45-9:30"); // Time slot

        // List of participants for the first time slot
        List<Map<String, String>> participants1 = new ArrayList<>();
        participants1.add(Map.of("Klasse", "ASS221", "Name", "Iskender", "Vorname", "Iclal Ece", "Anwesend?", ""));
        participants1.add(Map.of("Klasse", "ASS221", "Name", "Karakas", "Vorname", "Berfin", "Anwesend?", ""));
        participants1.add(Map.of("Klasse", "HÖH221", "Name", "Bendels", "Vorname", "Lea", "Anwesend?", ""));
        participants1.add(Map.of("Klasse", "HÖH221", "Name", "Luwawu", "Vorname", "Espoir", "Anwesend?", ""));
        participants1.add(Map.of("Klasse", "HÖH224", "Name", "Erol", "Vorname", "Iclal", "Anwesend?", ""));
        participants1.add(Map.of("Klasse", "HÖH224", "Name", "Erten", "Vorname", "Volkan Burak", "Anwesend?", ""));
        participants1.add(Map.of("Klasse", "HÖH224", "Name", "Gümez", "Vorname", "Koray", "Anwesend?", ""));
        participants1.add(Map.of("Klasse", "HÖH224", "Name", "Korkut", "Vorname", "Pelsin", "Anwesend?", ""));
        participants1.add(Map.of("Klasse", "WG221", "Name", "Ömer", "Vorname", "Diler", "Anwesend?", ""));
        participants1.add(Map.of("Klasse", "WG221", "Name", "Tourniaire", "Vorname", "Serafina", "Anwesend?", ""));

        // Add participants to the first time slot
        timeSlot1.put("Teilnehmer", participants1);
        timeSlots.add(timeSlot1);

        // Second time slot: 9:50-10:35
        Map<String, Object> timeSlot2 = new HashMap<>();
        timeSlot2.put("Uhrzeit", "9:50-10:35"); // Time slot

        // List of participants for the second time slot
        List<Map<String, String>> participants2 = new ArrayList<>();
        participants2.add(Map.of("Klasse", "ASS221", "Name", "Kavak", "Vorname", "Hazal", "Anwesend?", ""));
        participants2.add(Map.of("Klasse", "ASS221", "Name", "Rausch", "Vorname", "Erik", "Anwesend?", ""));
        participants2.add(Map.of("Klasse", "ASS221", "Name", "Sengöz", "Vorname", "Aleyna", "Anwesend?", ""));
        participants2.add(Map.of("Klasse", "HÖH221", "Name", "Demir", "Vorname", "Rojin", "Anwesend?", ""));
        participants2.add(Map.of("Klasse", "HÖH222", "Name", "Rana", "Vorname", "Aisha", "Anwesend?", ""));
        participants2.add(Map.of("Klasse", "HÖH223", "Name", "Adeosun", "Vorname", "Oluwaseyi Kehinde", "Anwesend?", ""));
        participants2.add(Map.of("Klasse", "HÖH223", "Name", "Ali", "Vorname", "Lilian", "Anwesend?", ""));
        participants2.add(Map.of("Klasse", "HÖH223", "Name", "Aygün", "Vorname", "Zanya", "Anwesend?", ""));
        participants2.add(Map.of("Klasse", "HÖH223", "Name", "Dirki", "Vorname", "Khalil", "Anwesend?", ""));
        participants2.add(Map.of("Klasse", "HÖH223", "Name", "Hampeter", "Vorname", "Anna", "Anwesend?", ""));
        participants2.add(Map.of("Klasse", "HÖH223", "Name", "Kisoglu", "Vorname", "Hawin", "Anwesend?", ""));
        participants2.add(Map.of("Klasse", "HÖH223", "Name", "Kisoglu", "Vorname", "Rojin", "Anwesend?", ""));
        participants2.add(Map.of("Klasse", "HÖH224", "Name", "Mambor", "Vorname", "Celina", "Anwesend?", ""));
        participants2.add(Map.of("Klasse", "HÖH224", "Name", "Okur", "Vorname", "Ahmet Semih", "Anwesend?", ""));
        participants2.add(Map.of("Klasse", "HÖH224", "Name", "Trac", "Vorname", "Ton Ly David", "Anwesend?", ""));
        participants2.add(Map.of("Klasse", "WG221", "Name", "Schmücker", "Vorname", "Paul", "Anwesend?", ""));
        participants2.add(Map.of("Klasse", "WG221", "Name", "Schwartz", "Vorname", "Tim", "Anwesend?", ""));

        // Add participants to the second time slot
        timeSlot2.put("Teilnehmer", participants2);
        timeSlots.add(timeSlot2);

        // Add time slots to the event data
        eventData.put("Zeitfenster", timeSlots);

        // Export the data to an Excel file
        TimetableService exportService = new TimetableService();
        try {
            exportService.exportEventData("EXPORT BOT5_Anwesenheitslisten_je_Veranstaltung.xlsx", eventData);
            System.out.println("Data successfully exported.");
        } catch (IOException e) {
            System.err.println("Error exporting data: " + e.getMessage());
        }
    }
}