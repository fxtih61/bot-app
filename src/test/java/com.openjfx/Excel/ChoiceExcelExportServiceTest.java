package com.openjfx.Excel;

import com.openjfx.services.ChoiceExcelExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains unit tests for the {@link ChoiceExcelExportService} class.
 * It tests the functionality of exporting data to an Excel file.
 *
 * @author Leon
 */
class ChoiceExcelExportServiceTest {

    private ChoiceExcelExportService exportService;
    private List<Map<String, Object>> testData;

    /**
     * Sets up the test environment before each test.
     * Initializes the {@link ChoiceExcelExportService} and prepares test data.
     *
     * @author Leon
     */
    @BeforeEach
    void setUp() {
        exportService = new ChoiceExcelExportService();
        testData = new ArrayList<>();

        // Create sample data for testing
        addRow(testData, "08:45-9:30", "103", "Finanzamt", "duales Studium Dipl. Finanzwirt/-in", "3", "Doe, Jane", "ASS221");
        addRow(testData, "9:50-10:35", "103", "Finanzamt", "Ausbildung Finanzwirt/-in", "1", "Doe, Jane", "ASS221");
        addRow(testData, "10:35-11:20", "102", "Aachener Sparkasse", "Bankkaufleute", "5", "Doe, Jane", "ASS221");
    }

    /**
     * Tests the successful export of data to an Excel file.
     * Verifies that the file is created and not empty.
     *
     * @throws IOException If an I/O error occurs during the test.
     * @author Leon
     */
    @Test
    void testExportChoiceData_Success() throws IOException {
        String filePath = "test_export.xlsx";

        // Export the data to an Excel file
        exportService.exportChoiceData(filePath, testData);

        // Verify that the file was created
        File file = new File(filePath);
        assertTrue(file.exists(), "The Excel file should be created.");

        // Verify that the file is not empty
        assertTrue(file.length() > 0, "The Excel file should not be empty.");

        // Clean up: Delete the test file after the test
        file.delete();
    }

    /**
     * Tests the export functionality with an empty data list.
     * Verifies that an {@link IllegalArgumentException} is thrown.
     *
     * @author Leon
     */
    @Test
    void testExportChoiceData_EmptyData() {
        String filePath = "test_export_empty.xlsx";
        List<Map<String, Object>> emptyData = new ArrayList<>();

        // Verify that an exception is thrown when the data list is empty
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            exportService.exportChoiceData(filePath, emptyData);
        });

        // Verify the exception message
        assertEquals("Data list must not be empty.", exception.getMessage(), "The exception message should match.");
    }

    /**
     * Tests the export functionality with invalid file paths.
     * Verifies that an {@link IOException} is thrown for invalid paths.
     *
     * @author Leon
     */
    @Test
    void testExportChoiceData_InvalidFilePath() {
        String invalidFilePath = "/invalid/path/test_export.xlsx";

        // Verify that an exception is thrown for an invalid file path
        assertThrows(IOException.class, () -> {
            exportService.exportChoiceData(invalidFilePath, testData);
        }, "An IOException should be thrown for an invalid file path.");
    }

    /**
     * Helper method to add a row of data to the test data list.
     *
     * @param data        The list to which the row will be added.
     * @param zeit        The time value.
     * @param raum        The room value.
     * @param veranstaltung The event value.
     * @param beschreibung The description value.
     * @param wunsch      The wish value.
     * @param name        The name value.
     * @param klasse      The class value.
     * @author Leon
     */
    private void addRow(List<Map<String, Object>> data, String zeit, String raum, String veranstaltung, String beschreibung, String wunsch, String name, String klasse) {
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