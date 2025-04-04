package com.openjfx.examples;

import com.openjfx.services.FulfillmentScoreService;
import com.openjfx.services.StudentAssignmentService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreExportExampleExcel {

    public static void main(String[] args) {
        // Create sample data structure
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
        students.add(createStudent("WG223", "Thomas", "Schmidt", 80, 70, 60, 50, 0, 0, 260, 86.7, 500, 300));

        // Build data structure
        scoreData.put("Headers", headers);
        scoreData.put("Students", students);

        // Export to Excel
        FulfillmentScoreService service = new FulfillmentScoreService(new StudentAssignmentService());
        try {
            service.exportScoreData("Fulfillment_Scores_Export.xlsx", scoreData);
            System.out.println("Excel file successfully created!");
        } catch (IOException e) {
            System.err.println("Error exporting to Excel: " + e.getMessage());
        }
    }

    private static Map<String, Object> createStudent(String classRef, String firstName, String lastName,
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