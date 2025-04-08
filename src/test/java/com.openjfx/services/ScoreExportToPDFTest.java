package com.openjfx.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FulfillmentScoreServiceTest {

    @TempDir
    Path tempDir;

    /**
     * Tests that the PDF export functionality creates a file with valid data.
     *
     * @throws IOException if there's an error during file operations
     *
     * @author leon
     */
    @Test
    void exportScoreDataToPDF_createsFileWithValidData() throws IOException {
        // Arrange
        FulfillmentScoreService service = new FulfillmentScoreService(new StudentAssignmentService());
        Path outputPath = tempDir.resolve("test_output.pdf");
        Map<String, Object> scoreData = createSampleScoreData();

        // Act
        service.exportScoreDataToPDF(outputPath.toString(), scoreData);

        // Assert
        assertTrue(Files.exists(outputPath));
        assertTrue(Files.size(outputPath) > 0);

        // Verify PDF content structure
        try (PDDocument document = PDDocument.load(outputPath.toFile())) {
            assertEquals(1, document.getNumberOfPages());
        }
    }

    /**
     * Tests that the PDF export handles an empty student list correctly.
     *
     * @throws IOException if there's an error during file operations
     *
     * @author leon
     */

    @Test
    void exportScoreDataToPDF_handlesEmptyStudentList() throws IOException {
        // Arrange
        FulfillmentScoreService service = new FulfillmentScoreService(new StudentAssignmentService());
        Path outputPath = tempDir.resolve("empty_students.pdf");
        Map<String, Object> scoreData = createSampleScoreData();
        ((List<?>) scoreData.get("Students")).clear();

        // Act
        service.exportScoreDataToPDF(outputPath.toString(), scoreData);

        // Assert
        assertTrue(Files.exists(outputPath));
        try (PDDocument document = PDDocument.load(outputPath.toFile())) {
            assertEquals(1, document.getNumberOfPages());
        }
    }

    /**
     * Tests that the PDF export creates multiple pages when many students are present.
     *
     * @throws IOException if there's an error during file operations
     *
     * @author leon
     */
    @Test
    void exportScoreDataToPDF_createsMultiplePagesForManyStudents() throws IOException {
        // Arrange
        FulfillmentScoreService service = new FulfillmentScoreService(new StudentAssignmentService());
        Path outputPath = tempDir.resolve("multi_page.pdf");
        Map<String, Object> scoreData = createSampleScoreData();

        // Add enough students to force multiple pages (about 40 students should do it)
        List<Map<String, Object>> students = (List<Map<String, Object>>) scoreData.get("Students");
        for (int i = 0; i < 40; i++) {
            students.add(createStudent("CLASS" + i, "First" + i, "Last" + i,
                    80, 70, 60, 50, 40, 30, 330, 82.5, 400, 400));
        }

        // Act
        service.exportScoreDataToPDF(outputPath.toString(), scoreData);

        // Assert
        assertTrue(Files.exists(outputPath));
        try (PDDocument document = PDDocument.load(outputPath.toFile())) {
            assertTrue(document.getNumberOfPages() > 1);
        }
    }

    /**
     * Tests that the PDF export throws an exception when given an invalid path.
     *
     * @author leon
     */
    @Test
    void exportScoreDataToPDF_throwsExceptionForInvalidPath() {
        // Arrange
        FulfillmentScoreService service = new FulfillmentScoreService(new StudentAssignmentService());
        String invalidPath = "/invalid/path/test.pdf";
        Map<String, Object> scoreData = createSampleScoreData();

        // Act & Assert
        assertThrows(IOException.class, () -> {
            service.exportScoreDataToPDF(invalidPath, scoreData);
        });
    }

    /**
     * Tests that the PDF export throws an exception when required headers are missing.
     *
     * @author leon
     */

    @Test
    void exportScoreDataToPDF_handlesMissingHeaders() {
        // Arrange
        FulfillmentScoreService service = new FulfillmentScoreService(new StudentAssignmentService());
        Path outputPath = tempDir.resolve("missing_headers.pdf");
        Map<String, Object> scoreData = createSampleScoreData();
        scoreData.remove("Headers");

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            service.exportScoreDataToPDF(outputPath.toString(), scoreData);
        });
    }

    /**
     * Tests that the PDF export handles student data with missing values.
     *
     * @throws IOException if there's an error during file operations
     *
     * @author leon
     */
    @Test
    void exportScoreDataToPDF_handlesMissingValuesInStudentData() throws IOException {
        // Arrange
        FulfillmentScoreService service = new FulfillmentScoreService(new StudentAssignmentService());
        Path outputPath = tempDir.resolve("missing_values.pdf");
        Map<String, Object> scoreData = createSampleScoreData();

        // Create a student with missing values
        Map<String, Object> student = new HashMap<>();
        student.put("Class", "TEST123");
        student.put("First Name", "Partial");
        student.put("Last Name", "Student");
        ((List<Map<String, Object>>) scoreData.get("Students")).add(student);

        // Act
        service.exportScoreDataToPDF(outputPath.toString(), scoreData);

        // Assert
        assertTrue(Files.exists(outputPath));
    }

    /**
     * Creates sample score data for testing purposes.
     *
     * @return Map containing sample score data with headers and student entries
     *
     * @author leon
     */
    private Map<String, Object> createSampleScoreData() {
        Map<String, Object> scoreData = new HashMap<>();

        // Create column headers
        List<Map<String, String>> headers = new ArrayList<>();
        headers.add(Map.of("Class", "Class"));
        headers.add(Map.of("First Name", "First Name"));
        headers.add(Map.of("Last Name", "Last Name"));
        headers.add(Map.of("Choice 1 Score", "Choice 1 Score"));
        headers.add(Map.of("Choice 2 Score", "Choice 2 Score"));
        headers.add(Map.of("Choice 3 Score", "Choice 3 Score"));
        headers.add(Map.of("Choice 4 Score", "Choice 4 Score"));
        headers.add(Map.of("Choice 5 Score", "Choice 5 Score"));
        headers.add(Map.of("Choice 6 Score", "Choice 6 Score"));
        headers.add(Map.of("Total Score", "Total Score"));
        headers.add(Map.of("Overall %", "Overall %"));
        headers.add(Map.of("Class Total", "Class Total"));
        headers.add(Map.of("Max Possible", "Max Possible"));

        // Add sample student data
        List<Map<String, Object>> students = new ArrayList<>();
        students.add(createStudent("ASS221", "Max", "Mustermann", 95, 85, 0, 0, 0, 0, 180, 90.0, 500, 200));
        students.add(createStudent("HÖH222", "Anna", "Musterfrau", 100, 90, 80, 0, 0, 0, 270, 90.0, 500, 300));

        // Build data structure
        scoreData.put("Headers", headers);
        scoreData.put("Students", students);

        return scoreData;
    }

    /**
     * Creates a sample student entry with the given parameters.
     *
     * @param classRef the class reference
     * @param firstName the student's first name
     * @param lastName the student's last name
     * @param score1 choice 1 score
     * @param score2 choice 2 score
     * @param score3 choice 3 score
     * @param score4 choice 4 score
     * @param score5 choice 5 score
     * @param score6 choice 6 score
     * @param totalScore the total score
     * @param overallPercent the overall percentage
     * @param classTotal the class total
     * @param maxPossible the maximum possible score
     * @return Map containing the student data
     *
     * @author leon
     */

    private Map<String, Object> createStudent(String classRef, String firstName, String lastName,
                                              int score1, int score2, int score3, int score4, int score5, int score6,
                                              int totalScore, double overallPercent, int classTotal, int maxPossible) {
        Map<String, Object> student = new HashMap<>();
        student.put("Class", classRef);
        student.put("First Name", firstName);
        student.put("Last Name", lastName);
        student.put("Choice 1 Score", score1);
        student.put("Choice 2 Score", score2);
        student.put("Choice 3 Score", score3);
        student.put("Choice 4 Score", score4);
        student.put("Choice 5 Score", score5);
        student.put("Choice 6 Score", score6);
        student.put("Total Score", totalScore);
        student.put("Overall %", overallPercent);
        student.put("Class Total", classTotal);
        student.put("Max Possible", maxPossible);
        return student;
    }
}