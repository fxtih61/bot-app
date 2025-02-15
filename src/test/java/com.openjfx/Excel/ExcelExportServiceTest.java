package com.openjfx.Excel;

import com.openjfx.services.ExcelExportService;
import com.openjfx.services.ExcelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ExcelExportServiceTest {

    /**
     * Dummy implementation of ExcelService for testing purposes.
     * This class tracks whether createExcelFile() was called and stores the input parameters.
     */
    private static class DummyExcelService extends ExcelService {
        boolean createExcelFileCalled = false;
        List<Map<String, Object>> capturedData;
        String capturedFilePath;

        @Override
        public void createExcelFile(List<Map<String, Object>> data, String filePath) throws IOException {
            createExcelFileCalled = true;
            capturedData = data;
            capturedFilePath = filePath;
        }
    }

    private DummyExcelService dummyExcelService;
    private ExcelExportService excelExportService;

    @BeforeEach
    public void setup() {
        // Initialize the dummy service and inject it into ExcelExportService
        dummyExcelService = new DummyExcelService();
        excelExportService = new ExcelExportService(dummyExcelService);
    }

    @Test
    public void testExportDataToExcel_withNullData() {
        // Test: Passing null data should throw IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> excelExportService.exportDataToExcel(null, "employees.xlsx"));
        assertEquals("Data list must not be null or empty", exception.getMessage());
    }

    @Test
    public void testExportDataToExcel_withEmptyData() {
        // Test: Passing an empty data list should throw IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> excelExportService.exportDataToExcel(Collections.emptyList(), "employees.xlsx"));
        assertEquals("Data list must not be null or empty", exception.getMessage());
    }

    @Test
    public void testExportDataToExcel_withInvalidHeader() {
        // Test: Passing a data list where the first row has no headers should throw IllegalArgumentException
        List<Map<String, Object>> data = new ArrayList<>();
        data.add(new HashMap<>()); // Empty map for headers

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> excelExportService.exportDataToExcel(data, "employees.xlsx"));
        assertEquals("Data must contain at least one row with valid headers", exception.getMessage());
    }

    @Test
    public void testExportDataToExcel_withNullFilePath() {
        // Test: Passing a null file path should throw IllegalArgumentException
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("Header", "Value");
        data.add(row);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> excelExportService.exportDataToExcel(data, null));
        assertEquals("File path must not be null or empty", exception.getMessage());
    }

    @Test
    public void testExportDataToExcel_withBlankFilePath() {
        // Test: Passing a blank file path should throw IllegalArgumentException
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("Header", "Value");
        data.add(row);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> excelExportService.exportDataToExcel(data, "   "));
        assertEquals("File path must not be null or empty", exception.getMessage());
    }

    @Test
    public void testExportDataToExcel_withValidData() throws IOException {
        // Test: With valid data, the ExcelService's createExcelFile() should be called
        // and we use the already existing file "employees.xlsx" as the target file.
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("Name", "Alice");
        row.put("Age", 30);
        data.add(row);

        // Use the already existing file "employees.xlsx"
        String filePath = "employees.xlsx";

        // If the file already exists, you might want to delete it before testing.
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }

        // Call the export method
        excelExportService.exportDataToExcel(data, filePath);

        // Verify that the dummy ExcelService was called and captured the correct values
        assertTrue(dummyExcelService.createExcelFileCalled, "createExcelFile should be called");
        assertEquals(filePath, dummyExcelService.capturedFilePath, "The file path should match");
        assertEquals(data, dummyExcelService.capturedData, "The exported data should match");
    }
}
