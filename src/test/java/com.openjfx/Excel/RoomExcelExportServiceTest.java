package com.openjfx.Excel;

import com.openjfx.services.ExcelService;
import com.openjfx.services.RoomService;
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

    private RoomService exportService;
    private List<Map<String, Object>> testData;

    /**
     * Sets up the test environment before each test.
     *
     * @author leon
     */
    @BeforeEach
    void setUp() {
        // Initialize the export service and test data
        exportService = new RoomService(new ExcelService());
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
     *
     * @author leon
     */
    private void addRow(List<Map<String, Object>> data, String company, String... roomTimes) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("Unternehmen", company); // Use "Unternehmen" instead of "Company"
        for (int i = 0; i < roomTimes.length; i++) {
            row.put("Zeit " + (i + 1), roomTimes[i]); // Use "Zeit X" instead of "Time X"
        }
        data.add(row);
    }

    /**
     * Tests exporting data to an Excel file.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if there's an error during file operations
     *
     * @author leon
     */
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

    /**
     * Tests exporting empty data to Excel, expecting an exception.
     *
     * @param tempDir temporary directory for test files
     *
     * @author leon
     */
    @Test
    void testExportDataToExcelWithEmptyData(@TempDir Path tempDir) {
        // Create a temporary file path for the Excel file
        Path excelFilePath = tempDir.resolve("test_empty_export.xlsx");
        File excelFile = excelFilePath.toFile();

        // Attempt to export an empty list
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            exportService.exportDataToExcel(new ArrayList<>(), excelFile.getAbsolutePath());
        });

        // Verify the exception message
        String expectedMessage = "Data list must not be null or empty";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage),
                "The exception message should indicate that the data list cannot be empty.");
    }

    /**
     * Tests exporting null data to Excel, expecting an exception.
     *
     * @param tempDir temporary directory for test files
     *
     * @author leon
     */
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
        String expectedMessage = "Data list must not be null or empty";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage),
                "The exception message should indicate that the data list cannot be null.");
    }
}