package com.openjfx.examples;

import com.openjfx.services.ChoiceExcelExportService;

import java.io.IOException;
import java.util.*;

/**
 * This class demonstrates the creation of an Excel file containing choice information.
 * The data is stored in a list of maps and then exported to an Excel file.
 *
 * @author leon
 */
public class ChoiceExportExample {

    /**
     * Main method of the program. This is where the data for the table is created and exported to an Excel file.
     *
     * @param args Command-line arguments (not used in this example)
     */
    public static void main(String[] args) {
        // Create example data
        List<Map<String, Object>> data = new ArrayList<>();

        // Example 1: Jane Doe
        addRow(data, "08:45-9:30", "103", "Finanzamt", "duales Studium Dipl. Finanzwirt/-in", "3", "Doe, Jane", "ASS221");
        addRow(data, "9:50-10:35", "103", "Finanzamt", "Ausbildung Finanzwirt/-in", "1", "Doe, Jane", "ASS221");
        addRow(data, "10:35-11:20", "102", "Aachener Sparkasse", "Bankkaufleute", "5", "Doe, Jane", "ASS221");
        addRow(data, "11:40-12:25", "102", "Justizvollzugsanstalt", "Beamter im allgemeinen Vollzugsdienst, Dipl-Verwaltungswirt (FH)", "4", "Doe, Jane", "ASS221");
        addRow(data, "12:25-13:10", "107", "Debeka", "Kaufleute für Versicherungen und Finanzen", "2", "Doe, Jane", "ASS221");

        ChoiceExcelExportService exportService = new ChoiceExcelExportService();

        // Export data
        try {
            exportService.exportChoiceData("EXPORT_BOT6_Laufzettel.xlsx", data);
            System.out.println("Daten erfolgreich exportiert.");
        } catch (IOException e) {
            System.err.println("Fehler beim Export der Daten: " + e.getMessage());
        }
    }

    /**
     * Adds a new row to the data list representing a participant's choice.
     *
     * @param data          The list to which the new row will be added.
     * @param zeit          The time slot for the event.
     * @param raum          The room where the event takes place.
     * @param veranstaltung         The organization hosting the event.
     * @param beschreibung   The description of the event.
     * @param wunsch    The priority ranking of the choice.
     * @param name          The participant's name.
     * @param klasse     The class the participant belongs to.
     *
     * @author leon
     */
    public static void addRow(List<Map<String, Object>> data, String zeit, String raum, String veranstaltung, String beschreibung, String wunsch, String name, String klasse) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("Zeit", zeit);
        row.put("Raum", raum);
        row.put("Veranstaltung", veranstaltung);
        row.put("Beschreibung", beschreibung);
        row.put("Wunsch", wunsch);
        row.put("Name", name);
        row.put("Klasse", klasse);
        data.add(row);
    }
}