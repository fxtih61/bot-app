package com.openjfx.services;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;

/**
 * Test class for {@link TimetableService} functionality regarding choice data PDF export.
 * Contains unit tests to verify the PDF generation of choice data.
 *
 * @author batuhan
 */
public class ChoiceToPDFServiceTest {

    private final TimetableService service = new TimetableService();

    /**
     * Tests the successful generation of a PDF file with choice data.
     * Verifies that the PDF is created without errors and contains expected content.
     *
     * @throws IOException if there's an error during file operations
     * @author batuhan
     */
    @Test
    public void testExportChoiceDataPDF_Success(@TempDir Path tempDir) throws IOException {
        // Prepare test data
        List<Map<String, Object>> testData = createTestData();
        Path outputFile = tempDir.resolve("test_export.pdf");

        // Execute test
        service.exportChoiceDataPDF(outputFile.toString(), testData);

        // Verify results
        assertTrue(outputFile.toFile().exists());
        assertTrue(outputFile.toFile().length() > 0);
    }

    /**
     * Tests the handling of empty data input when generating PDF.
     * Verifies that the method handles empty lists appropriately.
     *
     * @throws IOException if there's an error during file operations
     * @author batuhan
     */
    @Test
    public void testExportChoiceDataPDF_EmptyData(@TempDir Path tempDir) throws IOException {
        // Prepare test data
        List<Map<String, Object>> emptyData = new ArrayList<>();
        Path outputFile = tempDir.resolve("empty_export.pdf");

        // Execute test
        service.exportChoiceDataPDF(outputFile.toString(), emptyData);

        // Verify results
        assertTrue(outputFile.toFile().exists());
        assertTrue(outputFile.toFile().length() > 0); // PDF should still be created
    }

    /**
     * Tests the PDF generation with null data input.
     * Verifies that the method throws appropriate exceptions.
     *
     * @author batuhan
     */
    @Test
    public void testExportChoiceDataPDF_NullData(@TempDir Path tempDir) {
        Path outputFile = tempDir.resolve("null_export.pdf");

        assertThrows(NullPointerException.class, () -> {
            service.exportChoiceDataPDF(outputFile.toString(), null);
        });
    }

    /**
     * Tests the handling of long text descriptions in PDF generation.
     * Verifies that text wrapping works correctly.
     *
     * @throws IOException if there's an error during file operations
     * @author batuhan
     */
    @Test
    public void testExportChoiceDataPDF_LongDescriptions(@TempDir Path tempDir) throws IOException {
        // Prepare test data with long description
        List<Map<String, Object>> testData = createTestData();
        String longDescription = "This is a very long description that should trigger the text wrapping functionality " +
                "in the PDF generation. It needs to be longer than the column width to properly test " +
                "the wrapping feature of the PDF export service.";
        testData.get(0).put("Beschreibung", longDescription);

        Path outputFile = tempDir.resolve("long_desc_export.pdf");

        // Execute test
        service.exportChoiceDataPDF(outputFile.toString(), testData);

        // Verify results
        assertTrue(outputFile.toFile().exists());
        assertTrue(outputFile.toFile().length() > 0);
    }

    /**
     * Creates sample test data for PDF export tests.
     *
     * @return List of sample choice data maps
     * @author batuhan
     */
    private List<Map<String, Object>> createTestData() {
        List<Map<String, Object>> data = new ArrayList<>();

        Map<String, Object> row1 = new LinkedHashMap<>();
        row1.put("Zeit", "08:45-9:30");
        row1.put("Raum", "103");
        row1.put("Veranstaltung", "Finanzamt");
        row1.put("Beschreibung", "duales Studium Dipl. Finanzwirt/-in");
        row1.put("Wunsch", "3");
        row1.put("Name", "Doe, Jane");
        row1.put("Klasse", "ASS221");
        data.add(row1);

        Map<String, Object> row2 = new LinkedHashMap<>();
        row2.put("Zeit", "9:50-10:35");
        row2.put("Raum", "103");
        row2.put("Veranstaltung", "Finanzamt");
        row2.put("Beschreibung", "Ausbildung Finanzwirt/-in");
        row2.put("Wunsch", "1");
        row2.put("Name", "Doe, Jane");
        row2.put("Klasse", "ASS221");
        data.add(row2);

        return data;
    }
}