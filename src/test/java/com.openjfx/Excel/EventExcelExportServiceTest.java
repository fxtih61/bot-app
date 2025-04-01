package com.openjfx.Excel;

import com.openjfx.services.TimetableService;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains unit tests for the {@link com.openjfx.services.TimetableService} class.
 * It verifies the functionality of exporting event data to an Excel file.
 *
 * @author leon
 */
class EventExcelExportServiceTest {
    private static final String TEST_FILE_PATH = "test_export.xlsx";
    private TimetableService exportService;
    private Map<String, Object> sampleEventData;

    /**
     * Sets up the test environment before each test execution.
     * Initializes the {@link TimetableService} and prepares sample event data.
     *
     * @author leon
     */
    @BeforeEach
    void setUp() {
        exportService = new TimetableService();
        sampleEventData = new HashMap<>();
        sampleEventData.put("Veranstaltung", "Test Event");

        List<Map<String, Object>> timeSlots = new ArrayList<>();
        Map<String, Object> timeSlot = new HashMap<>();
        timeSlot.put("Uhrzeit", "10:00-11:00");

        List<Map<String, String>> participants = new ArrayList<>();
        participants.add(Map.of("Klasse", "TEST101", "Name", "Mustermann", "Vorname", "Max", "Anwesend?", ""));
        participants.add(Map.of("Klasse", "TEST102", "Name", "Doe", "Vorname", "Jane", "Anwesend?", ""));

        timeSlot.put("Teilnehmer", participants);
        timeSlots.add(timeSlot);

        sampleEventData.put("Zeitfenster", timeSlots);
    }

    /**
     * Cleans up the test environment after each test execution.
     * Deletes the test Excel file if it exists.
     *
     * @author leon
     */
    @AfterEach
    void tearDown() {
        File file = new File(TEST_FILE_PATH);
        if (file.exists()) {
            assertTrue(file.delete(), "Test file should be deleted after test execution.");
        }
    }

    /**
     * Tests the successful creation of an Excel file by the {@link TimetableService}.
     * Verifies that the file is created without throwing exceptions.
     *
     * @author leon
     */
    @Test
    @DisplayName("Test exportEventData creates an Excel file successfully")
    void testExportEventData() {
        assertDoesNotThrow(() -> exportService.exportEventData(TEST_FILE_PATH, sampleEventData));
        File file = new File(TEST_FILE_PATH);
        assertTrue(file.exists(), "The exported file should exist after export.");
    }

    /**
     * Tests the behavior of {@link TimetableService} when provided with empty event data.
     * Verifies that an {@link IllegalArgumentException} is thrown.
     *
     * @author leon
     */
    @Test
    @DisplayName("Test exportEventData throws exception for empty event data")
    void testExportEventDataWithEmptyData() {
        Map<String, Object> emptyData = new HashMap<>();
        assertThrows(IllegalArgumentException.class, () -> exportService.exportEventData(TEST_FILE_PATH, emptyData));
    }
}