package com.openjfx.dao;

import com.openjfx.config.DatabaseConfig;
import com.openjfx.models.FulfillmentScore;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for handling fulfillment score persistence operations.
 *
 * <p>This class provides methods to interact with the fulfillment_scores table
 * in the database, handling the storage of student choice fulfillment data.</p>
 *
 * @author mian
 */
public class FulfillmentScoreDAO {

  /**
   * Saves a fulfillment score record to the database.
   *
   * <p>This method persists a FulfillmentScore object to the fulfillment_scores table,
   * including all student choice scores, calculation metrics, and timestamps.</p>
   *
   * @param score the FulfillmentScore object containing the data to be saved
   * @throws SQLException if a database access error occurs or the SQL statement fails
   * @author mian
   */
  public void saveFulfillmentScore(FulfillmentScore score) throws SQLException {
    String sql = "INSERT INTO fulfillment_scores (student_id, class_ref, first_name, last_name, " +
        "choice1_score, choice2_score, choice3_score, choice4_score, choice5_score, choice6_score, "
        +
        "student_total_score, calculation_timestamp, overall_fulfillment_percentage, " +
        "total_students, total_score, max_possible_score) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, score.getStudentId());
      stmt.setString(2, score.getClassRef());
      stmt.setString(3, score.getFirstName());
      stmt.setString(4, score.getLastName());
      stmt.setInt(5, score.getChoice1Score());
      stmt.setInt(6, score.getChoice2Score());
      stmt.setInt(7, score.getChoice3Score());
      stmt.setInt(8, score.getChoice4Score());
      stmt.setInt(9, score.getChoice5Score());
      stmt.setInt(10, score.getChoice6Score());
      stmt.setInt(11, score.getStudentTotalScore());
      stmt.setTimestamp(12, Timestamp.valueOf(score.getCalculationTimestamp()));
      stmt.setDouble(13, score.getOverallFulfillmentPercentage());
      stmt.setInt(14, score.getTotalStudents());
      stmt.setInt(15, score.getTotalScore());
      stmt.setDouble(16, score.getMaxPossibleScore());

      stmt.executeUpdate();
    }
  }

  /**
   * Retrieves all fulfillment scores from the database.
   *
   * @return List of FulfillmentScore objects
   * @throws SQLException if a database access error occurs
   * @author mian
   */
  public List<FulfillmentScore> getAllFulfillmentScores() throws SQLException {
    List<FulfillmentScore> scores = new ArrayList<>();
    String sql = "SELECT * FROM fulfillment_scores";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        FulfillmentScore score = new FulfillmentScore();
        score.setId(rs.getInt("id"));
        score.setStudentId(rs.getString("student_id"));
        score.setClassRef(rs.getString("class_ref"));
        score.setFirstName(rs.getString("first_name"));
        score.setLastName(rs.getString("last_name"));
        score.setChoice1Score(rs.getInt("choice1_score"));
        score.setChoice2Score(rs.getInt("choice2_score"));
        score.setChoice3Score(rs.getInt("choice3_score"));
        score.setChoice4Score(rs.getInt("choice4_score"));
        score.setChoice5Score(rs.getInt("choice5_score"));
        score.setChoice6Score(rs.getInt("choice6_score"));
        score.setStudentTotalScore(rs.getInt("student_total_score"));
        score.setCalculationTimestamp(rs.getTimestamp("calculation_timestamp").toLocalDateTime());
        score.setOverallFulfillmentPercentage(rs.getDouble("overall_fulfillment_percentage"));
        score.setTotalStudents(rs.getInt("total_students"));
        score.setTotalScore(rs.getInt("total_score"));
        score.setMaxPossibleScore(rs.getDouble("max_possible_score"));
        scores.add(score);
      }
    }

    return scores;
  }

  /**
   * Checks if there are any fulfillment scores in the database.
   *
   * @return true if there are fulfillment scores, false otherwise
   * @throws SQLException if a database access error occurs
   * @author mian
   */
  public boolean hasFulfillmentScores() throws SQLException {
    String sql = "SELECT COUNT(*) FROM fulfillment_scores";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      if (rs.next()) {
        return rs.getInt(1) > 0;
      }
      return false;
    }
  }
}