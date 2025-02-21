package com.openjfx.services;

import com.openjfx.models.Event;
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
   */
  public EventService(ExcelService excelService) {
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
        "id", "nr",
        "max", "max. teilnehmer", // Match "Max. Teilnehmer"
        "min", "min.",            // Match "Min."
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
   */
  @Override
  protected Event createModelFromRow(Map<String, String> row, Map<String, String> columnMappings) {
    // Get the ID (the only truly required field)
    String idStr = row.get(columnMappings.get("id"));

    // If ID is missing, skip this row
    if (idStr == null || idStr.trim().isEmpty()) {
      System.err.println("Skipping row due to missing ID: " + row);
      return null;
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
   * @param row the row data
   * @param columnMappings column mappings
   * @param field the field name
   * @param defaultValue default value to use if missing
   * @return the string value or default
   */
  private String getOptionalStringValue(Map<String, String> row, Map<String, String> columnMappings,
      String field, String defaultValue) {
    String value = row.get(columnMappings.get(field));
    return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
  }

  /**
   * Helper method to get an optional integer value with a default if missing.
   *
   * @param row the row data
   * @param columnMappings column mappings
   * @param field the field name
   * @param defaultValue default value to use if missing
   * @return the integer value or default
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
}