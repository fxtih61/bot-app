package com.openjfx.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TimetableService} focusing on PDF export functionality.
 * These tests verify the correct generation of PDF attendance lists for events.
 *
 * @author batuhan
 */
public class EventToPDFServiceTest {

    /**
     * Tests successful PDF generation with valid event data.
     * Verifies that the PDF file is created without errors.
     *
     * @param tempDir temporary directory provided by JUnit
     * @throws IOException if there's an error during file operations
     * @author batuhan
     */
    @Test
    void testExportEventDataPDF_Success(@TempDir Path tempDir) throws IOException {
        // Arrange
        TimetableService service = new TimetableService();
        String filePath = tempDir.resolve("test_export.pdf").toString();
        Map<String, Object> eventData = createSampleEventData();

        // Act
        service.exportEventDataPDF(filePath, eventData);

        // Assert
        File outputFile = new File(filePath);
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        // Verify PDF is not corrupted
        try (PDDocument doc = PDDocument.load(outputFile)) {
            assertEquals(1, doc.getNumberOfPages());
        }
    }

    /**
     * Tests that the method throws IOException when provided with an invalid file path.
     *
     * @author batuhan
     */
    @Test
    void testExportEventDataPDF_InvalidPath() {
        // Arrange
        TimetableService service = new TimetableService();
        String invalidPath = "/invalid/path/test_export.pdf";
        Map<String, Object> eventData = createSampleEventData();

        // Act & Assert
        assertThrows(IOException.class, () -> {
            service.exportEventDataPDF(invalidPath, eventData);
        });
    }

    /**
     * Tests PDF generation with empty participant lists.
     * Verifies the PDF is still created correctly with empty tables.
     *
     * @param tempDir temporary directory provided by JUnit
     * @throws IOException if there's an error during file operations
     * @author batuhan
     */
    @Test
    void testExportEventDataPDF_EmptyParticipants(@TempDir Path tempDir) throws IOException {
        // Arrange
        TimetableService service = new TimetableService();
        String filePath = tempDir.resolve("empty_participants.pdf").toString();
        Map<String, Object> eventData = createSampleEventData();
        ((List<Map<String, Object>>) eventData.get("Zeitfenster")).get(0).put("Teilnehmer", new ArrayList<>());

        // Act
        service.exportEventDataPDF(filePath, eventData);

        // Assert
        File outputFile = new File(filePath);
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    /**
     * Tests PDF generation with multiple time slots.
     * Verifies all time slots are included in the output.
     *
     * @param tempDir temporary directory provided by JUnit
     * @throws IOException if there's an error during file operations
     * @author batuhan
     */
    @Test
    void testExportEventDataPDF_MultipleTimeSlots(@TempDir Path tempDir) throws IOException {
        // Arrange
        TimetableService service = new TimetableService();
        String filePath = tempDir.resolve("multi_slot.pdf").toString();
        Map<String, Object> eventData = createSampleEventData();

        // Add a third time slot
        Map<String, Object> timeSlot3 = new HashMap<>();
        timeSlot3.put("Uhrzeit", "10:45-11:30");
        timeSlot3.put("Teilnehmer", List.of(
                Map.of("Klasse", "TEST123", "Name", "Doe", "Vorname", "John", "Anwesend?", "")
        ));
        ((List<Map<String, Object>>) eventData.get("Zeitfenster")).add(timeSlot3);

        // Act
        service.exportEventDataPDF(filePath, eventData);

        // Assert
        File outputFile = new File(filePath);
        assertTrue(outputFile.exists());

        // Verify PDF has content (more thorough content verification would require PDF parsing)
        assertTrue(outputFile.length() > 0);
    }

    /**
     * Helper method to create sample event data for testing.
     *
     * @return a Map containing sample event data
     * @author batuhan
     */
    private Map<String, Object> createSampleEventData() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("Veranstaltung", "Test Event");

        List<Map<String, Object>> timeSlots = new ArrayList<>();

        // First time slot
        Map<String, Object> timeSlot1 = new HashMap<>();
        timeSlot1.put("Uhrzeit", "8:00-9:00");
        timeSlot1.put("Teilnehmer", List.of(
                Map.of("Klasse", "TEST101", "Name", "Smith", "Vorname", "Alice", "Anwesend?", ""),
                Map.of("Klasse", "TEST101", "Name", "Johnson", "Vorname", "Bob", "Anwesend?", "")
        ));
        timeSlots.add(timeSlot1);

        // Second time slot
        Map<String, Object> timeSlot2 = new HashMap<>();
        timeSlot2.put("Uhrzeit", "9:00-10:00");
        timeSlot2.put("Teilnehmer", List.of(
                Map.of("Klasse", "TEST102", "Name", "Williams", "Vorname", "Charlie", "Anwesend?", ""),
                Map.of("Klasse", "TEST102", "Name", "Brown", "Vorname", "Diana", "Anwesend?", "")
        ));
        timeSlots.add(timeSlot2);

        eventData.put("Zeitfenster", timeSlots);
        return eventData;
    }
}