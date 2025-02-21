package com.openjfx.Excel;

import com.openjfx.services.ExcelService;
import com.openjfx.services.RoomExcelExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RoomExcelExportServiceTest {

    private RoomExcelExportService exportService;
    private ExcelService excelService;

    @BeforeEach
    void setUp() {
        excelService = new ExcelService();
        exportService = new RoomExcelExportService(excelService);
    }

    @Test
    void testExportDataToExcelWithHeaders_Success(@TempDir Path tempDir) throws IOException {
        // Beispielhafte Daten
        List<Map<String, Object>> data = new ArrayList<>();
        List<String> generalHeaders = Arrays.asList("Test Header 1", "Test Header 2");
        List<String> headers = Arrays.asList("Unternehmen", "Zeit 1", "Zeit 2");

        // Beispielhafte Datensätze
        addRow(data, headers, "Firma A", "101", "102");
        addRow(data, headers, "Firma B", "103", "104");

        // Temporäre Datei
        Path filePath = tempDir.resolve("TestExport.xlsx");

        // Export aufrufen
        exportService.exportDataToExcelWithHeaders(data, generalHeaders, headers, filePath.toString());

        // Datei sollte existieren
        File file = filePath.toFile();
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }

    @Test
    void testExportDataToExcelWithHeaders_EmptyData_ThrowsException() {
        List<Map<String, Object>> data = new ArrayList<>();
        List<String> generalHeaders = Arrays.asList("Test Header 1");
        List<String> headers = Arrays.asList("Unternehmen", "Zeit 1");

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                exportService.exportDataToExcelWithHeaders(data, generalHeaders, headers, "Test.xlsx"));

        assertEquals("Data list must not be null or empty", exception.getMessage());
    }

    @Test
    void testExportDataToExcelWithHeaders_NullData_ThrowsException() {
        List<String> generalHeaders = Arrays.asList("Header");
        List<String> headers = Arrays.asList("Unternehmen", "Zeit 1");

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                exportService.exportDataToExcelWithHeaders(null, generalHeaders, headers, "Test.xlsx"));

        assertEquals("Data list must not be null or empty", exception.getMessage());
    }

    @Test
    void testExportDataToExcelWithHeaders_NullHeaders_ThrowsException() {
        List<Map<String, Object>> data = new ArrayList<>();
        addRow(data, Arrays.asList("Unternehmen", "Zeit 1"), "Firma", "101");

        List<String> generalHeaders = Arrays.asList("Header");

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                exportService.exportDataToExcelWithHeaders(data, generalHeaders, null, "Test.xlsx"));

        assertEquals("Headers must not be null or empty", exception.getMessage());
    }

    @Test
    void testExportDataToExcelWithHeaders_NullFilePath_ThrowsException() {
        List<Map<String, Object>> data = new ArrayList<>();
        List<String> generalHeaders = Arrays.asList("Header");
        List<String> headers = Arrays.asList("Unternehmen", "Zeit 1");

        addRow(data, headers, "Firma A", "101");

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                exportService.exportDataToExcelWithHeaders(data, generalHeaders, headers, null));

        assertEquals("File path must not be null or empty", exception.getMessage());
    }

    private static void addRow(List<Map<String, Object>> data, List<String> headers, String... values) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            row.put(headers.get(i), i < values.length ? values[i] : "");
        }
        data.add(row);
    }
}
