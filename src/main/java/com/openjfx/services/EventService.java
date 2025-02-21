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
 *   <li>"nr" → Event ID</li>
 *   <li>"max" → Maximum participants</li>
 *   <li>"event" → Event</li>
 *   <li>"unternehmen" → Company name</li>
 *   <li>"fachrichtung" → Subject area</li>
 *   <li>"frühester zeitpunkt" → Earliest start time</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * EventService eventService = new EventService(new ExcelService());
 * List<Event> events = eventService.loadFromExcel("path/to/excel.xlsx");
 * </pre>
 */

public class EventService extends AbstractExcelService<Event> {

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
        "id", "NR.",
        "max", "Max. teilnehmer",
        "event", "Anzahl. Veranstaltungen",
        "company", "Unternehmen",
        "subject", "Fachrichtung",
        "time", "Frühester Zeitpunkt"
    );
  }

  /**
   * Safely parses an integer from a string, with fallback values.
   *
   * @param value the string to parse
   * @param defaultValue the default value to return if parsing fails
   * @return the parsed integer or the default value
   */
  private int safeParseInt(String value, int defaultValue) {
    if (value == null || value.trim().isEmpty()) {
      return defaultValue;
    }

    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      // Handle non-numeric values (like "A")
      return defaultValue;
    }
  }


  /**
   * Creates an Event object from a row of Excel data.
   *
   * <p>Required fields are:
   * <ul>
   *   <li>ID (numeric)</li>
   *   <li>Maximum participants (numeric)</li>
   *   <li>Event (numeric)</li>
   * </ul>
   *
   * <p>Optional fields are:
   * <ul>
   *   <li>Company name (string)</li>
   *   <li>Subject area (string)</li>
   *   <li>Earliest start time (string)</li>
   * </ul>
   *
   * @param row            the row data from Excel
   * @param columnMappings the mappings between internal names and actual Excel columns
   * @return a new Event object, or null if the row data is invalid
   */

  @Override
  protected Event createModelFromRow(Map<String, String> row, Map<String, String> columnMappings) {
    String idStr = row.get(columnMappings.get("id"));
    String maxStr = row.get(columnMappings.get("max"));
    String eventStr = row.get(columnMappings.get("event"));
    String company = row.get(columnMappings.get("company"));
    String subject = row.get(columnMappings.get("subject"));
    String time = row.get(columnMappings.get("time"));

    // Only ID is truly required
    if (idStr == null || idStr.trim().isEmpty()) {
      System.err.println("Skipping row due to missing ID: " + row);
      return null;
    }

    try {
      int id = Integer.parseInt(idStr.trim());
      int max = safeParseInt(maxStr, 0);
      int event = safeParseInt(eventStr, 0);

      return new Event(
          id,
          company != null ? company : "",
          subject != null ? subject : "",
          max,
          event,
          time != null ? time : ""
      );
    } catch (NumberFormatException e) {
      System.err.println("Error parsing ID in row: " + row + " - " + e.getMessage());
      return null;
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
        "Max. teilnehmer", event.getMaxParticipants(),
        "Anzahl. Veranstaltungen", event.getEvent(),
        "Frühester Zeitpunkt", event.getEarliestStart()
    );
  }
}