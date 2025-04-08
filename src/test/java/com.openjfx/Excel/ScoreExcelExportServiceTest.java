package com.openjfx.Excel;

import com.openjfx.services.FulfillmentScoreService;
import com.openjfx.services.StudentAssignmentService;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ScoreExcelExportServiceTest {

    /**
     * Tests that the service creates an Excel file with the correct structure,
     * including headers, data, and styling.
     *
     * @param tempDir temporary directory provided by JUnit for test files
     * @throws IOException if there's an error creating or reading the Excel file
     *
     * @author leon
     */
    @Test
    void shouldCreateExcelFileWithCorrectStructure(@TempDir Path tempDir) throws IOException {
        // Given
        FulfillmentScoreService service = new FulfillmentScoreService(new StudentAssignmentService());
        String filename = tempDir.resolve("test_scores.xlsx").toString();
        Map<String, Object> testData = createTestData();

        // When
        service.exportScoreData(filename, testData);

        // Then
        File outputFile = new File(filename);
        assertTrue(outputFile.exists(), "Excel file should be created");

        try (Workbook workbook = WorkbookFactory.create(outputFile)) {
            Sheet sheet = workbook.getSheet("Fulfillment Scores");
            assertNotNull(sheet, "Sheet should exist");

            // Verify header row
            Row headerRow = sheet.getRow(0);
            assertEquals("Class", headerRow.getCell(0).getStringCellValue());
            assertEquals("First Name", headerRow.getCell(1).getStringCellValue());
            assertEquals("Last Name", headerRow.getCell(2).getStringCellValue());
            assertEquals("Score 1", headerRow.getCell(3).getStringCellValue());

            // Verify data rows
            Row dataRow1 = sheet.getRow(1);
            assertEquals("MATH101", dataRow1.getCell(0).getStringCellValue());
            assertEquals("John", dataRow1.getCell(1).getStringCellValue());
            assertEquals("Doe", dataRow1.getCell(2).getStringCellValue());
            assertEquals(95.0, dataRow1.getCell(3).getNumericCellValue(), 0.001);

            // Verify styling
            CellStyle headerStyle = headerRow.getCell(0).getCellStyle();
            assertEquals(IndexedColors.GREY_25_PERCENT.getIndex(),
                    headerStyle.getFillForegroundColor());
            assertTrue(workbook.getFontAt(headerStyle.getFontIndex()).getBold());
        }
    }

    /**
     * Tests that the service correctly handles empty data input by creating
     * a file with only headers.
     *
     * @param tempDir temporary directory provided by JUnit for test files
     * @throws IOException if there's an error creating or reading the Excel file
     *
     * @author leon
     */
    @Test
    void shouldHandleEmptyData(@TempDir Path tempDir) throws IOException {
        // Given
        FulfillmentScoreService service = new FulfillmentScoreService(new StudentAssignmentService());
        String filename = tempDir.resolve("empty_test.xlsx").toString();
        Map<String, Object> emptyData = new HashMap<>();
        emptyData.put("Headers", List.of(Map.of("Class", "Class")));
        emptyData.put("Students", List.of());

        // When
        service.exportScoreData(filename, emptyData);

        // Then
        File outputFile = new File(filename);
        assertTrue(outputFile.exists());

        try (Workbook workbook = WorkbookFactory.create(outputFile)) {
            Sheet sheet = workbook.getSheet("Fulfillment Scores");
            assertEquals(1, sheet.getPhysicalNumberOfRows()); // Only header row
        }
    }

    /**
     * Creates test data for the Excel export tests.
     *
     * @return a Map containing sample headers and student data
     *
     * @author leon
     */
    private Map<String, Object> createTestData() {
        Map<String, Object> testData = new HashMap<>();

        testData.put("Headers", List.of(
                Map.of("Class", "Class"),
                Map.of("First Name", "First Name"),
                Map.of("Last Name", "Last Name"),
                Map.of("Score 1", "Score 1")
        ));

        testData.put("Students", List.of(
                createStudent("MATH101", "John", "Doe", 95),
                createStudent("PHYS202", "Jane", "Smith", 88)
        ));

        return testData;
    }

    /**
     * Creates a sample student record for testing.
     *
     * @param className the class name for the student
     * @param firstName the first name of the student
     * @param lastName the last name of the student
     * @param score the score of the student
     * @return a Map representing a student record
     *
     * @author leon
     */
    private Map<String, Object> createStudent(String className, String firstName,
                                              String lastName, int score) {
        Map<String, Object> student = new HashMap<>();
        student.put("Class", className);
        student.put("First Name", firstName);
        student.put("Last Name", lastName);
        student.put("Score 1", score);
        return student;
    }
}