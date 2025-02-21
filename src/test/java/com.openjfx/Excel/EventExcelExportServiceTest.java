package com.openjfx.Excel;

import com.openjfx.services.EventExcelExportService;
import com.openjfx.services.ExcelService;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class EventExcelExportServiceTest {

    private static final String TEST_FILE_PATH = "test_event_export.xlsx";
    private EventExcelExportService exportService;
    private ExcelService excelService;

    @BeforeEach
    void setUp() {
        excelService = new ExcelService(); // Echte Instanz von ExcelService verwenden
        exportService = new EventExcelExportService(excelService);
    }

    @AfterEach
    void tearDown() {
        File file = new File(TEST_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testExportEventData_SuccessfulExport() {
        List<Map<String, Object>> data = new ArrayList<>();

        // Fügen von Beispiel-Daten für den Export
        addRow(data, Collections.singletonList("Veranstaltung"), "Test Event");
        addRow(data, Collections.singletonList("Uhrzeit"), "10:00-11:00");
        List<String> headers = Arrays.asList("Klasse", "Name", "Vorname", "Anwesend?");
        addRow(data, headers, headers.toArray(new String[0]));
        addRow(data, headers, "WG221", "Müller", "Max", "");

        assertDoesNotThrow(() -> exportService.exportEventData(TEST_FILE_PATH, data));

        File file = new File(TEST_FILE_PATH);
        assertTrue(file.exists(), "Die exportierte Datei sollte existieren.");
        assertTrue(file.length() > 0, "Die exportierte Datei sollte nicht leer sein.");
    }

    @Test
    void testExportEventData_EmptyDataThrowsException() {
        List<Map<String, Object>> emptyData = new ArrayList<>();
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> exportService.exportEventData(TEST_FILE_PATH, emptyData));

        assertEquals("Datenliste darf nicht leer sein.", exception.getMessage());
    }

    private void addRow(List<Map<String, Object>> data, List<String> headers, String... values) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            row.put(headers.get(i), i < values.length ? values[i] : "");
        }
        data.add(row);
    }
}
