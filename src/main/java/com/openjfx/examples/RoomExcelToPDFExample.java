package com.openjfx.examples;

import com.openjfx.services.ExcelService;
import com.openjfx.services.RoomService;

import java.io.IOException;
import java.util.*;

/**
 * This class demonstrates the creation of an Excel file containing room and schedule information.
 * The data is stored in a list of maps and then exported to an Excel file.
 *
 * @author batuhan
 */
public class RoomExcelToPDFExample {

    /**
     * Main method of the program. This is where the data for the table is created and exported to an Excel file.
     *
     * @param args Command-line arguments (not used in this example)
     *
     * @author batuhan
     */
    public static void main(String[] args) {
// List to store the table data
        List<Map<String,Object>> data = new ArrayList<>();

// Add sample data records
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

// Excel export
        RoomService roomPdfService = new RoomService(new ExcelService());
        try {
// Export the data to an Excel file
            roomPdfService.roomExportToPdf(data, "EXPORT BOT4 Room and Schedule Plan.pdf");
        } catch (IOException e) {
// Error handling if the export fails
            e.printStackTrace();
        }
    }

    /**
     * Adds a new row to the data list. The row contains the company name and the associated room and schedule information.
     *
     * @param data The list to which the row is added
     * @param unternehmen The name of the company
     * @param raumZeiten The room and schedule information for the company (can be empty)
     *
     * @author batuhan
     */
    private static void addRow(List<Map<String,Object>> data, String unternehmen, String... raumZeiten) {
        Map row = new LinkedHashMap<>();
        row.put("Unternehmen", unternehmen);
        for (int i = 0; i < raumZeiten.length; i++) {
            row.put("Zeit " + (i + 1), raumZeiten[i]);
        }
        data.add(row);
    }
}