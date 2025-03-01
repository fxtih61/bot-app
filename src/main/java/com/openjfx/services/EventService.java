package com.openjfx.services;

import com.openjfx.config.DatabaseConfig;
import com.openjfx.models.Event;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service class for handling Event-related Excel operations. This class extends
 * AbstractExcelService to provide specific functionality for reading and writing Event data from/to
 * Excel files.
 *
 * <p>The service maps Excel columns to Event properties using German column headers:
 * <ul>
 *   <li>"nr" → Event ID (required)</li>
 *   <li>"max" → Maximum participants (optional)</li>
 *   <li>"min" → Minimum participants (optional)</li>
 *   <li>"unternehmen" → Company name (optional)</li>
 *   <li>"fachrichtung" → Subject area (optional)</li>
 *   <li>"frühester zeitpunkt" → Earliest start time (optional)</li>
 * </ul>
 *
 * <p>Only the ID field is truly required. Other fields will use default values if empty.
 *
 * <p>Example usage:
 * <pre>
 * EventService eventService = new EventService(new ExcelService());
 * List<Event> events = eventService.loadFromExcel("path/to/excel.xlsx");
 * </pre>
 *
 * @author mian
 */

public class EventService extends AbstractExcelService<Event> {

  // Default values for empty fields
  private static final String DEFAULT_EMPTY_STRING = "Empty";
  private static final int DEFAULT_MIN = 0;
  private static final int DEFAULT_MAX = 0;

  /**
   * Constructs a new EventService with the specified Excel service.
   *
   * @param excelService the Excel service to use for file operations
   * @author mian
   */
  public EventService(ExcelService excelService) {
    super(excelService);
  }

  /**
   * Defines the required fields for an Event object.
   *
   * @return a List of required field names
   * @author mian
   */
  @Override
  protected List<String> getRequiredFields() {
    return List.of("id");
  }

  /**
   * Defines the mapping between internal property names and Excel column prefixes. The column
   * prefixes are case-insensitive partial matches for Excel column headers.
   *
   * @return a Map containing the property-to-column prefix mappings
   * @author mian
   */
  @Override
  protected Map<String, String> getColumnPrefixes() {
    return Map.of(
        "id", "nr",
        "max", "max.",
        "min", "min.",
        "company", "unternehmen",
        "subject", "fachrichtung",
        "time", "frühester zeitpunkt"
    );
  }


  /**
   * Creates an Event object from a row of Excel data.
   *
   * <p>Required fields are:
   * <ul>
   *   <li>ID (numeric)</li>
   * </ul>
   *
   * <p>Optional fields with default values:
   * <ul>
   *   <li>Maximum participants (default: 0)</li>
   *   <li>Minimum participants (default: 0)</li>
   *   <li>Company name (default: "Empty")</li>
   *   <li>Subject area (default: "Empty")</li>
   *   <li>Earliest start time (default: "Empty")</li>
   * </ul>
   *
   * @param row            the row data from Excel
   * @param columnMappings the mappings between internal names and actual Excel columns
   * @return a new Event object, or null if the ID is missing or invalid
   * @author mian
   */
  @Override
  protected Event createModelFromRow(Map<String, String> row, Map<String, String> columnMappings) {
    // Get the ID (the only truly required field)
    String idStr = row.get(columnMappings.get("id"));

    // Throw an exception if the ID is missing
    if (idStr == null || idStr.trim().isEmpty()) {
      throw new IllegalArgumentException("Missing required field: id");
    }

    try {
      // Parse ID - this is the only required field
      int id = Integer.parseInt(idStr.trim());

      // Get optional fields with defaults if missing
      String company = getOptionalStringValue(row, columnMappings, "company", DEFAULT_EMPTY_STRING);
      String subject = getOptionalStringValue(row, columnMappings, "subject", DEFAULT_EMPTY_STRING);
      String time = getOptionalStringValue(row, columnMappings, "time", DEFAULT_EMPTY_STRING);

      // Parse numeric fields with defaults
      int max = getOptionalIntValue(row, columnMappings, "max", DEFAULT_MAX);
      int min = getOptionalIntValue(row, columnMappings, "min", DEFAULT_MIN);

      // Create the Event with all fields (using defaults for missing ones)
      return new Event(id, company, subject, max, min, time);

    } catch (NumberFormatException e) {
      System.err.println("Error parsing ID in row: " + row + " - " + e.getMessage());
      return null;
    }
  }

  /**
   * Helper method to get an optional string value with a default if missing.
   *
   * @param row            the row data
   * @param columnMappings column mappings
   * @param field          the field name
   * @param defaultValue   default value to use if missing
   * @return the string value or default
   * @author mian
   */
  private String getOptionalStringValue(Map<String, String> row, Map<String, String> columnMappings,
      String field, String defaultValue) {
    String value = row.get(columnMappings.get(field));
    return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
  }

  /**
   * Helper method to get an optional integer value with a default if missing.
   *
   * @param row            the row data
   * @param columnMappings column mappings
   * @param field          the field name
   * @param defaultValue   default value to use if missing
   * @return the integer value or default
   * @author mian
   */
  private int getOptionalIntValue(Map<String, String> row, Map<String, String> columnMappings,
      String field, int defaultValue) {
    String value = row.get(columnMappings.get(field));
    if (value == null || value.trim().isEmpty()) {
      return defaultValue;
    }

    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      System.err.println("Error parsing " + field + " value '" + value + "', using default");
      return defaultValue;
    }
  }

  /**
   * Converts an Event object to a map of column names and values for Excel export.
   *
   * @param event the Event object to convert
   * @return a Map containing the column names and values for Excel export
   * @author mian
   */
  @Override
  protected Map<String, Object> convertModelToRow(Event event) {
    return Map.of(
        "NR.", event.getId(),
        "Unternehmen", event.getCompany(),
        "Fachrichtung", event.getSubject(),
        "Max.", event.getMaxParticipants(),
        "Min.", event.getMinParticipants(),
        "Frühester Zeitpunkt", event.getEarliestStart()
    );
  }

  /**
   * Saves an Event object to the database.
   *
   * @param event the Event object to save
   * @author mian
   */
  public void saveEvent(Event event) {
    // SQL query to insert a new event into the events table
    String sql = "INSERT INTO events ("
        + "id, "
        + "company, "
        + "subject, "
        + "max_participants, "
        + "min_participants, "
        + "earliest_start) "
        + "VALUES (?, ?, ?, ?, ?, ?)";

    try (Connection conn = DatabaseConfig.getConnection()) {
      // Disable auto-commit to manage transactions manually
      conn.setAutoCommit(false);

      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        // Set the parameters for the PreparedStatement
        pstmt.setInt(1, event.getId());
        pstmt.setString(2, event.getCompany());
        pstmt.setString(3, event.getSubject());
        pstmt.setInt(4, event.getMaxParticipants());
        pstmt.setInt(5, event.getMinParticipants());
        pstmt.setString(6, event.getEarliestStart());

        // Execute the update and get the number of affected rows
        int result = pstmt.executeUpdate();

        // Commit the transaction
        conn.commit();

      } catch (SQLException e) {
        // Rollback the transaction in case of an error
        conn.rollback();

        // Log the error message and stack trace
        System.err.println("Error saving event, transaction rolled back: " + e.getMessage());
        e.printStackTrace();
      }
    } catch (SQLException e) {
      // Log the database connection error message and stack trace
      System.err.println("Database connection error: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Deletes an Event object from the database.
   *
   * @author mian
   */
  public void clearEvents() {
    String delete = "DELETE FROM events";

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
   * Loads the events from the database
   *
   * @return a list of events
   * @author mian
   */
  public List<Event> loadEvents() {
    // SQL query to select all events from the events table
    String sql = "SELECT * FROM events";

    List<Event> events = new ArrayList<>();

    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

      // Iterate over the result set and create Event objects
      while (rs.next()) {
        int id = rs.getInt("id");
        String company = rs.getString("company");
        String subject = rs.getString("subject");
        int maxParticipants = rs.getInt("max_participants");
        int minParticipants = rs.getInt("min_participants");
        String earliestStart = rs.getString("earliest_start");

        Event event = new Event(id, company, subject, maxParticipants, minParticipants,
            earliestStart);
        events.add(event);
      }

    } catch (SQLException e) {
      // Log the database connection error message and stack trace
      System.err.println("Database connection error: " + e.getMessage());
      e.printStackTrace();
    }
    return events;
  }


}