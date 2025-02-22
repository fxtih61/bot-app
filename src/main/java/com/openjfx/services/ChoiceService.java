package com.openjfx.services;

import com.openjfx.config.DatabaseConfig;
import com.openjfx.models.Choice;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service class for handling Choice-related Excel operations. This class extends
 * AbstractExcelService to provide specific functionality for reading and writing Choice data
 * from/to Excel files.
 *
 * <p>The service maps Excel columns to Choice properties using German column headers:
 * <ul>
 *   <li>"klasse" → Class reference</li>
 *   <li>"vorname" → First name</li>
 *   <li>"name" → Last name</li>
 *   <li>"wahl 1" → First choice</li>
 *   <li>"wahl 2" → Second choice</li>
 *   <li>"wahl 3" → Third choice</li>
 *   <li>"wahl 4" → Fourth choice</li>
 *   <li>"wahl 5" → Fifth choice</li>
 *   <li>"wahl 6" → Sixth choice</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * ChoiceService choiceService = new ChoiceService(new ExcelService());
 * List<Choice> choices = choiceService.loadFromExcel("path/to/excel.xlsx");
 * </pre>
 */
public class ChoiceService extends AbstractExcelService<Choice> {

  /**
   * Constructs a new ChoiceService with the specified Excel service.
   *
   * @param excelService the Excel service to use for file operations
   */
  public ChoiceService(ExcelService excelService) {
    super(excelService);
  }

  /**
   * Defines the mapping between internal property names and Excel column prefixes. The column
   * prefixes are case-insensitive partial matches for Excel column headers.
   *
   * @return a Map containing the property-to-column prefix mappings
   */
  @Override
  protected Map<String, String> getColumnPrefixes() {
    return Map.of(
        "classRef", "klasse",
        "firstName", "vorname",
        "lastName", "name",
        "choice1", "wahl 1",
        "choice2", "wahl 2",
        "choice3", "wahl 3",
        "choice4", "wahl 4",
        "choice5", "wahl 5",
        "choice6", "wahl 6"
    );
  }

  /**
   * Creates a Choice object from a row of Excel data.
   *
   * <p>Required fields are:
   * <ul>
   *   <li>Class reference (string)</li>
   *   <li>First name (string)</li>
   *   <li>Last name (string)</li>
   * </ul>
   *
   * <p>Optional fields are:
   * <ul>
   *   <li>Choice 1-6 (strings): Student's preferences in order</li>
   * </ul>
   *
   * @param row            the row data from Excel
   * @param columnMappings the mappings between internal names and actual Excel columns
   * @return a new Choice object, or null if the row data is invalid
   */
  @Override
  protected Choice createModelFromRow(Map<String, String> row, Map<String, String> columnMappings) {
    String classRef = row.get(columnMappings.get("classRef"));
    String firstName = row.get(columnMappings.get("firstName"));
    String lastName = row.get(columnMappings.get("lastName"));

    // Required fields check
    if (classRef == null || firstName == null || lastName == null) {
      System.err.println("Error creating Choice object: missing required fields" + row);
      return null;
    }

    // Optional fields with null handling
    String choice1 = row.get(columnMappings.get("choice1"));
    String choice2 = row.get(columnMappings.get("choice2"));
    String choice3 = row.get(columnMappings.get("choice3"));
    String choice4 = row.get(columnMappings.get("choice4"));
    String choice5 = row.get(columnMappings.get("choice5"));
    String choice6 = row.get(columnMappings.get("choice6"));

    try {
      return new Choice(
          classRef.trim(),
          firstName.trim(),
          lastName.trim(),
          choice1 != null ? choice1.trim() : "",
          choice2 != null ? choice2.trim() : "",
          choice3 != null ? choice3.trim() : "",
          choice4 != null ? choice4.trim() : "",
          choice5 != null ? choice5.trim() : "",
          choice6 != null ? choice6.trim() : ""
      );
    } catch (Exception e) {
      System.err.println("Error creating Choice object: " + e.getMessage() + " - " + row);
      return null;
    }
  }

  /**
   * Converts a Choice object to a map of column names and values for Excel export.
   *
   * @param choice the Choice object to convert
   * @return a Map containing the column names and values for Excel export
   */
  @Override
  protected Map<String, Object> convertModelToRow(Choice choice) {
    return Map.of(
        "klasse", choice.getClassRef(),
        "vorname", choice.getFirstName(),
        "name", choice.getLastName(),
        "wahl 1", choice.getChoice1(),
        "wahl 2", choice.getChoice2(),
        "wahl 3", choice.getChoice3(),
        "wahl 4", choice.getChoice4(),
        "wahl 5", choice.getChoice5(),
        "wahl 6", choice.getChoice6()
    );
  }

  /**
   * Saves a Choice object to the database.
   *
   * @param choice the Choice object to save
   */
  public void saveChoice(Choice choice) {
    // SQL query to insert a new choice into the choices table
    String sql = "INSERT INTO choices ("
        + "class_ref, "
        + "first_name, "
        + "last_name, "
        + "choice1, "
        + "choice2, "
        + "choice3, "
        + "choice4, "
        + "choice5, "
        + "choice6) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = DatabaseConfig.getConnection()) {
      // Disable auto-commit to manage transactions manually
      conn.setAutoCommit(false);

      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        // Set the parameters for the PreparedStatement
        pstmt.setString(1, choice.getClassRef());
        pstmt.setString(2, choice.getFirstName());
        pstmt.setString(3, choice.getLastName());
        pstmt.setString(4, choice.getChoice1());
        pstmt.setString(5, choice.getChoice2());
        pstmt.setString(6, choice.getChoice3());
        pstmt.setString(7, choice.getChoice4());
        pstmt.setString(8, choice.getChoice5());
        pstmt.setString(9, choice.getChoice6());

        // Execute the update and get the number of affected rows
        int result = pstmt.executeUpdate();

        // Commit the transaction
        conn.commit();

      } catch (SQLException e) {
        // Rollback the transaction in case of an error
        conn.rollback();

        // Log the error message and stack trace
        System.err.println("Error saving choice, transaction rolled back: " + e.getMessage());
        e.printStackTrace();
      }
    } catch (SQLException e) {
      // Log the database connection error message and stack trace
      System.err.println("Database connection error: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Deletes a Choice object from the database.
   */

  public void clearChoices() {
    String delete = "DELETE FROM choices";

    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement()) {
      conn.setAutoCommit(false);
      stmt.executeUpdate(delete);
      conn.commit();
    } catch (SQLException e) {
      System.err.println("Error clearing events: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Load the choices from the database
   * @return a list of choices
   */

  public List<Choice> loadChoices() {
    String sql = "SELECT * FROM choices";

    List<Choice> choices = new ArrayList<>();

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {
      // Iterate over the ResultSet and create a new Choice object for each row
      while (rs.next()) {
        Choice choice = new Choice(
            rs.getString("class_ref"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("choice1"),
            rs.getString("choice2"),
            rs.getString("choice3"),
            rs.getString("choice4"),
            rs.getString("choice5"),
            rs.getString("choice6")
        );

        // Add the Choice object to the list
        choices.add(choice);
      }
    } catch (SQLException e) {
      System.err.println("Database connection error: " + e.getMessage());
      e.printStackTrace();
    }
    return choices;
  }
}