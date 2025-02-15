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
 *   <li>"min" → Minimum participants</li>
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
        "id", "nr",
        "max", "max",
        "min", "min",
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
   *   <li>Maximum participants (numeric)</li>
   *   <li>Minimum participants (numeric)</li>
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
    String minStr = row.get(columnMappings.get("min"));
    String company = row.get(columnMappings.get("company"));
    String subject = row.get(columnMappings.get("subject"));
    String time = row.get(columnMappings.get("time"));

    if (idStr == null || maxStr == null || minStr == null) {
      System.err.println("Skipping row due to null values: " + row);
      return null;
    }

    try {
      return new Event(
          Integer.parseInt(idStr.trim()),
          company != null ? company : "",
          subject != null ? subject : "",
          Integer.parseInt(maxStr.trim()),
          Integer.parseInt(minStr.trim()),
          time != null ? time : ""
      );
    } catch (NumberFormatException e) {
      System.err.println("Error parsing row: " + row + " - " + e.getMessage());
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
        "Max.", event.getMaxParticipants(),
        "Min.", event.getMinParticipants(),
        "Frühester Zeitpunkt", event.getEarliestStart()
    );
  }
}