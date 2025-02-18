package com.openjfx.services;

import com.openjfx.models.Choice;
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
}