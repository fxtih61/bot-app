package com.openjfx.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;

/**
 * Abstract base class for Excel-related services that handle reading and writing data models. This
 * class provides common functionality for Excel operations while allowing specific implementations
 * for different model types.
 *
 * <p>The service handles:
 * <ul>
 *   <li>Reading data from Excel files and converting rows to model objects</li>
 *   <li>Writing model objects to Excel files</li>
 *   <li>Column mapping between Excel headers and model properties</li>
 * </ul>
 *
 * @param <T> the type of model this service handles
 */

public abstract class AbstractExcelService<T> {

  /**
   * The Excel service used for low-level file operations.
   */
  protected final ExcelService excelService;

  /**
   * Constructs a new AbstractExcelService with the specified Excel service.
   *
   * @param excelService the Excel service to use for file operations
   */
  public AbstractExcelService(ExcelService excelService) {
    this.excelService = excelService;
  }

  /**
   * Finds a column in the Excel data that matches the given prefix. The matching is
   * case-insensitive and uses the startsWith method.
   *
   * @param row          the row data containing column names
   * @param columnPrefix the prefix to match against column names
   * @return the matched column name, or an empty string if no match is found
   */
  protected String findColumn(Map<String, String> row, String columnPrefix) {
    return row.keySet().stream()
        .filter(key -> key.toLowerCase().trim().startsWith(columnPrefix))
        .findFirst()
        .orElse("");
  }

  /**
   * Imports data from an Excel file and validates required fields.
   *
   * @param excelFile the Excel file to import
   * @return the list of imported objects
   * @throws IOException              if an I/O error occurs
   * @throws IllegalArgumentException if the data is invalid or required fields are missing
   */
  public List<T> loadFromExcel(File excelFile) throws IOException, IllegalArgumentException {
    List<Map<String, String>> rows = excelService.readExcelFile(excelFile.getPath());
    if (rows.isEmpty()) {
      throw new IllegalArgumentException("The Excel file contains no data rows");
    }

    Map<String, String> columnMappings = new HashMap<>();
    Map<String, String> firstRow = rows.get(0);
    Map<String, String> columnPrefixes = getColumnPrefixes();

    for (Map.Entry<String, String> entry : columnPrefixes.entrySet()) {
      String key = entry.getKey();
      String prefix = entry.getValue();
      columnMappings.put(key, findColumn(firstRow, prefix));
    }

    validateRequiredColumns(columnMappings);

    List<T> importedObjects = new ArrayList<>();
    int rowNumber = 1;

    for (Map<String, String> row : rows) {
      rowNumber++;
      try {
        T model = createModelFromRow(row, columnMappings);
        if (model != null) {
          importedObjects.add(model);
        } else {
          throw new IllegalArgumentException("Invalid data in row " + rowNumber);
        }
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Error in row " + rowNumber + ": " + e.getMessage());
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "Error processing row " + rowNumber + ": " + e.getMessage());
      }
    }

    return importedObjects;
  }

  /**
   * Validates that all required columns are present in the column mappings.
   *
   * @param columnMappings the column mappings to validate
   * @throws IllegalArgumentException if required columns are missing
   */
  protected void validateRequiredColumns(Map<String, String> columnMappings)
      throws IllegalArgumentException {
    List<String> requiredFields = getRequiredFields();
    List<String> missingFields = new ArrayList<>();

    for (String field : requiredFields) {
      if (!columnMappings.containsKey(field) || columnMappings.get(field).isEmpty()) {
        missingFields.add(field);
      }
    }

    if (!missingFields.isEmpty()) {
      throw new IllegalArgumentException(
          "Missing required columns: " + String.join(", ", missingFields));
    }
  }

  /**
   * Gets the list of required fields for this model.
   *
   * @return the list of required field names
   */
  protected abstract List<String> getRequiredFields();

  /**
   * Saves a list of model objects to an Excel file.
   *
   * @param models the list of model objects to save
   * @param path   the path where the Excel file should be saved
   * @throws IOException if there's an error writing the file
   */
  public void saveToExcel(List<T> models, String path) throws IOException {
    List<Map<String, Object>> data = new ArrayList<>();
    for (T model : models) {
      data.add(convertModelToRow(model));
    }
    excelService.createExcelFile(data, path);
  }

  /**
   * Defines the mapping between model properties and Excel column prefixes. Must be implemented by
   * subclasses to specify their column mappings.
   *
   * @return a Map where keys are property names and values are column prefixes
   */
  protected abstract Map<String, String> getColumnPrefixes();

  /**
   * Creates a model object from a row of Excel data. Must be implemented by subclasses to handle
   * their specific model type.
   *
   * @param row            the row data from Excel
   * @param columnMappings the mappings between property names and Excel columns
   * @return a new model object, or null if the row data is invalid
   */
  protected abstract T createModelFromRow(Map<String, String> row,
      Map<String, String> columnMappings);

  /**
   * Converts a model object to a map of column names and values for Excel export. Must be
   * implemented by subclasses to handle their specific model type.
   *
   * @param model the model object to convert
   * @return a Map containing the column names and values for Excel export
   */
  protected abstract Map<String, Object> convertModelToRow(T model);

  public ExcelService getExcelService() {
    return excelService;
  }
}