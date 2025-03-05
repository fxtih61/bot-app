package com.openjfx.Excel;

import com.openjfx.services.RoomExcelExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the functionality of the RoomExcelExportService.
 *
 * @author leon
 */
class RoomExcelExportServiceTest {

    private RoomExcelExportService exportService;
    private List<Map<String, Object>> testData;

    @BeforeEach
    void setUp() {
        // Initialize the export service and test data
        exportService = new RoomExcelExportService();
        testData = new ArrayList<>();

        // Add sample data for testing
        addRow(testData, "Zentis", "209", "", "", "", "");
        addRow(testData, "Babor Kosmetik", "109", "109", "", "", "");
        addRow(testData, "RWTH Aachen", "", "", "108", "108", "108");
    }

    /**
     * Helper method to add a row to the data list.
     *
     * @param data       The list to which the row is added
     * @param company    The name of the company
     * @param roomTimes  The room and schedule information for the company (can be empty)
     */
    private void addRow(List<Map<String, Object>> data, String company, String... roomTimes) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("Company", company);
        for (int i = 0; i < roomTimes.length; i++) {
            row.put("Time " + (i + 1), roomTimes[i]);
        }
        data.add(row);
    }

    @Test
    void testExportDataToExcel(@TempDir Path tempDir) throws IOException {
        // Create a temporary file path for the Excel file
        Path excelFilePath = tempDir.resolve("test_export.xlsx");
        File excelFile = excelFilePath.toFile();

        // Export the test data to the Excel file
        exportService.exportDataToExcel(testData, excelFile.getAbsolutePath());

        // Verify that the file exists
        assertTrue(excelFile.exists(), "The Excel file should exist.");

        // Verify that the file is not empty
        assertTrue(excelFile.length() > 0, "The Excel file should not be empty.");
    }

    @Test
    void testExportDataToExcelWithEmptyData(@TempDir Path tempDir) throws IOException {
        // Create a temporary file path for the Excel file
        Path excelFilePath = tempDir.resolve("test_empty_export.xlsx");
        File excelFile = excelFilePath.toFile();

        // Export an empty list to the Excel file
        exportService.exportDataToExcel(new ArrayList<>(), excelFile.getAbsolutePath());

        // Verify that the file exists
        assertTrue(excelFile.exists(), "The Excel file should exist.");

        // Verify that the file is not empty (even an empty table generates a file)
        assertTrue(excelFile.length() > 0, "The Excel file should not be empty.");
    }

    @Test
    void testExportDataToExcelWithNullData(@TempDir Path tempDir) {
        // Create a temporary file path for the Excel file
        Path excelFilePath = tempDir.resolve("test_null_export.xlsx");
        File excelFile = excelFilePath.toFile();

        // Attempt to export null data
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            exportService.exportDataToExcel(null, excelFile.getAbsolutePath());
        });

        // Verify the exception message
        String expectedMessage = "Data list cannot be null";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage), "The exception message should indicate that the data list cannot be null.");
    }
}