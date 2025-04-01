package com.openjfx.services;

import com.openjfx.models.StudentAssignment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FulfillmentScoreService {

  private final StudentAssignmentService studentAssignmentService;
  private static final int MAX_SCORE_PER_STUDENT = 21;
  private static final int[] WEIGHTS = {6, 5, 4, 3, 2, 1};

  public FulfillmentScoreService(StudentAssignmentService studentAssignmentService) {
    this.studentAssignmentService = studentAssignmentService;
  }

  public double calculateFulfillmentScore() {
    List<StudentAssignment> assignments = studentAssignmentService.getAllAssignments();

    Map<String, Map<Integer, Integer>> studentChoiceScores = new HashMap<>();
    Map<String, String> studentDetails = new HashMap<>();
    int totalStudents = 0;

    // Initialize and collect scores
    for (StudentAssignment assignment : assignments) {
      String studentId = getStudentId(assignment);
      String displayInfo = String.format("%s, %s %s",
          assignment.getClassRef(),
          assignment.getFirstName(),
          assignment.getLastName());
      studentDetails.put(studentId, displayInfo);

      studentChoiceScores.putIfAbsent(studentId, new HashMap<>());
      int choiceNo = assignment.getChoiceNo();

      if (choiceNo > 0 && choiceNo <= 6) {
        studentChoiceScores.get(studentId).put(choiceNo, WEIGHTS[choiceNo - 1]);
      }
    }

    // Calculate and print individual scores
    int totalScore = 0;
    System.out.println("Class, Name, Choice1, Choice2, Choice3, Choice4, Choice5, Choice6, Total");

    for (Map.Entry<String, Map<Integer, Integer>> entry : studentChoiceScores.entrySet()) {
      String studentId = entry.getKey();
      Map<Integer, Integer> scores = entry.getValue();
      int studentTotal = 0;

      StringBuilder scoreLine = new StringBuilder(studentDetails.get(studentId));

      // Add individual choice scores
      for (int i = 1; i <= 6; i++) {
        int score = scores.getOrDefault(i, 0);
        studentTotal += score;
        scoreLine.append(", ").append(score);
      }

      scoreLine.append(", ").append(studentTotal);
      System.out.println(scoreLine);
      totalScore += studentTotal;
    }

    totalStudents = studentChoiceScores.size();
    if (totalStudents == 0) {
      return 0.0;
    }

    double maxPossibleScore = totalStudents * MAX_SCORE_PER_STUDENT;
    double fulfillmentPercentage = (totalScore / maxPossibleScore) * 100;

    System.out.println("\nSummary:");
    System.out.printf("Fulfillment Score: %.2f%%\n", fulfillmentPercentage);
    System.out.printf("Total Score: %d\n", totalScore);
    System.out.printf("Total Students: %d\n", totalStudents);
    System.out.printf("Max Possible Score: %.0f\n", maxPossibleScore);

    return fulfillmentPercentage;
  }

  private String getStudentId(StudentAssignment assignment) {
    return String.format("%s_%s_%s",
        assignment.getFirstName(),
        assignment.getLastName(),
        assignment.getClassRef());
  }
}