package com.openjfx.Excel;

import com.openjfx.services.ChoiceExcelExportService;
import com.openjfx.services.ExcelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ChoiceExcelExportServiceTest {

    private ChoiceExcelExportService exportService;
    private ExcelService excelService;

    @BeforeEach
    void setUp() {
        excelService = new ExcelService(); // Angenommen, ExcelService ist bereits implementiert
        exportService = new ChoiceExcelExportService(excelService);
    }

    @Test
    void testExportChoiceData_Success() throws IOException {
        // Beispielhafte Daten erstellen
        List<Map<String, Object>> data = new ArrayList<>();
        List<String> headers = Arrays.asList("Zeit", "Raum", "Veranstaltung", "Wunsch");

        addRow(data, headers, "08:45-9:30", "103", "Finanzamt", "3", "Jane Doe", "ASS221");
        addRow(data, headers, "9:50-10:35", "103", "Finanzamt", "1", "Jane Doe", "ASS221");

        // Temporäre Datei für den Test erstellen
        File tempFile = File.createTempFile("Choice_Export_Test", ".xlsx");
        String filePath = tempFile.getAbsolutePath();

        // Daten exportieren
        exportService.exportChoiceData(filePath, data, headers);

        // Überprüfen, ob die Datei existiert
        assertTrue(tempFile.exists());

        // Datei nach dem Test löschen
        tempFile.delete();
    }

    @Test
    void testExportChoiceData_EmptyData() {
        List<Map<String, Object>> emptyData = new ArrayList<>();
        List<String> headers = Arrays.asList("Zeit", "Raum", "Veranstaltung", "Wunsch");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            exportService.exportChoiceData("Empty_Data_Test.xlsx", emptyData, headers);
        });

        String expectedMessage = "Datenliste darf nicht leer sein.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testExportChoiceData_Grouping() throws IOException {
        // Beispielhafte Daten erstellen
        List<Map<String, Object>> data = new ArrayList<>();
        List<String> headers = Arrays.asList("Zeit", "Raum", "Veranstaltung", "Wunsch");

        addRow(data, headers, "08:45-9:30", "103", "Finanzamt", "3", "Jane Doe", "ASS221");
        addRow(data, headers, "9:50-10:35", "103", "Finanzamt", "1", "Jane Doe", "ASS221");
        addRow(data, headers, "10:35-11:20", "102", "Aachener Sparkasse", "5", "Max Mustermann", "ASS221");

        // Temporäre Datei für den Test erstellen
        File tempFile = File.createTempFile("Choice_Export_Grouping_Test", ".xlsx");
        String filePath = tempFile.getAbsolutePath();

        // Daten exportieren
        exportService.exportChoiceData(filePath, data, headers);

        // Überprüfen, ob die Datei existiert
        assertTrue(tempFile.exists());

        // Hier könnten zusätzliche Überprüfungen durchgeführt werden, z.B. ob die Gruppierung korrekt ist
        // Dies würde jedoch eine detaillierte Analyse der Excel-Datei erfordern, die über den Rahmen dieses Tests hinausgeht.

        // Datei nach dem Test löschen
        tempFile.delete();
    }

    private void addRow(List<Map<String, Object>> data, List<String> headers, String... values) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            row.put(headers.get(i), i < values.length ? values[i] : "");
        }
        row.put("Name", values[values.length - 2]);
        row.put("Klasse", values[values.length - 1]);
        data.add(row);
    }
}