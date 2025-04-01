package com.openjfx.services;

import com.openjfx.dao.FulfillmentScoreDAO;
import com.openjfx.models.FulfillmentScore;
import com.openjfx.models.StudentAssignment;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for calculating and managing student fulfillment scores.
 *
 * <p>This service handles the calculation of student choice fulfillment scores
 * based on their assignments and choice preferences. It uses a weighted scoring system where
 * earlier choices are worth more points.</p>
 *
 * @author mian
 */
public class FulfillmentScoreService {

  private final StudentAssignmentService studentAssignmentService;
  private final FulfillmentScoreDAO fulfillmentScoreDAO;
  private static final int MAX_SCORE_PER_STUDENT = 21;
  private static final int[] WEIGHTS = {6, 5, 4, 3, 2, 1};

  /**
   * Constructs a new FulfillmentScoreService with the specified student assignment service.
   *
   * @author mian
   */
  public FulfillmentScoreService(StudentAssignmentService studentAssignmentService) {
    this.studentAssignmentService = studentAssignmentService;
    this.fulfillmentScoreDAO = new FulfillmentScoreDAO();
  }

  /**
   * Calculates the overall fulfillment score for all student assignments.
   *
   * <p>This method processes all student assignments, calculates individual and overall
   * fulfillment scores based on choice preferences, and persists the results. Each choice is
   * weighted differently, with earlier choices worth more points.</p>
   *
   * @return the overall fulfillment percentage as a double
   * @throws RuntimeException if saving fulfillment scores fails
   * @author mian
   */
  public double calculateFulfillmentScore() {
    List<StudentAssignment> assignments = studentAssignmentService.getAllAssignments();
    Map<String, Map<Integer, Integer>> studentChoiceScores = new HashMap<>();
    Map<String, StudentAssignment> studentDetails = new HashMap<>();

    // Initialize and collect scores
    for (StudentAssignment assignment : assignments) {
      String studentId = getStudentId(assignment);
      studentDetails.put(studentId, assignment);
      studentChoiceScores.putIfAbsent(studentId, new HashMap<>());

      int choiceNo = assignment.getChoiceNo();
      if (choiceNo > 0 && choiceNo <= 6) {
        studentChoiceScores.get(studentId).put(choiceNo, WEIGHTS[choiceNo - 1]);
      }
    }

    int totalStudents = studentChoiceScores.size();
    if (totalStudents == 0) {
      return 0.0;
    }

    int totalScore = 0;
    double maxPossibleScore = totalStudents * MAX_SCORE_PER_STUDENT;
    LocalDateTime calculationTime = LocalDateTime.now();

    // Calculate and save individual scores
    for (Map.Entry<String, Map<Integer, Integer>> entry : studentChoiceScores.entrySet()) {
      String studentId = entry.getKey();
      Map<Integer, Integer> scores = entry.getValue();
      StudentAssignment student = studentDetails.get(studentId);

      FulfillmentScore score = new FulfillmentScore();
      score.setStudentId(studentId);
      score.setClassRef(student.getClassRef());
      score.setFirstName(student.getFirstName());
      score.setLastName(student.getLastName());

      int studentTotal = 0;
      for (int i = 1; i <= 6; i++) {
        int choiceScore = scores.getOrDefault(i, 0);
        switch (i) {
          case 1:
            score.setChoice1Score(choiceScore);
            break;
          case 2:
            score.setChoice2Score(choiceScore);
            break;
          case 3:
            score.setChoice3Score(choiceScore);
            break;
          case 4:
            score.setChoice4Score(choiceScore);
            break;
          case 5:
            score.setChoice5Score(choiceScore);
            break;
          case 6:
            score.setChoice6Score(choiceScore);
            break;
        }
        studentTotal += choiceScore;
      }

      score.setStudentTotalScore(studentTotal);
      score.setCalculationTimestamp(calculationTime);
      score.setTotalStudents(totalStudents);
      score.setTotalScore(totalScore);
      score.setMaxPossibleScore(maxPossibleScore);

      totalScore += studentTotal;
      double fulfillmentPercentage = (totalScore / maxPossibleScore) * 100;
      score.setOverallFulfillmentPercentage(fulfillmentPercentage);

      try {
        fulfillmentScoreDAO.saveFulfillmentScore(score);
      } catch (SQLException e) {
        throw new RuntimeException("Failed to save fulfillment score", e);
      }
    }

    return (totalScore / maxPossibleScore) * 100;
  }

  /**
   * Generates a unique student identifier from assignment details.
   *
   * @param assignment the student assignment containing identification details
   * @return a unique string identifier combining first name, last name and class reference
   * @author mian
   */
  private String getStudentId(StudentAssignment assignment) {
    return String.format("%s_%s_%s",
        assignment.getFirstName(),
        assignment.getLastName(),
        assignment.getClassRef());
  }
}