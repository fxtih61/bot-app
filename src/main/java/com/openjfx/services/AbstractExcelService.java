package com.openjfx.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
   * Loads model objects from an Excel file.
   *
   * <p>The method:
   * <ol>
   *   <li>Reads the Excel file</li>
   *   <li>Maps column headers to model properties</li>
   *   <li>Converts each row to a model object</li>
   * </ol>
   *
   * @param path the path to the Excel file
   * @return a List of model objects
   * @throws IOException if there's an error reading the file
   */
  public List<T> loadFromExcel(String path) throws IOException {
    List<Map<String, String>> excelData = excelService.readExcelFile(path);
    System.out.println("Excel data: " + excelData);
    List<T> models = new ArrayList<>();

    if (excelData.isEmpty()) {
      return models;
    }

    // Extract column mappings from the first row
    Map<String, String> columnMappings = new HashMap<>();
    Map<String, String> firstRow = excelData.get(0);
    Map<String, String> columnPrefixes = getColumnPrefixes();

    for (Map.Entry<String, String> entry : columnPrefixes.entrySet()) {
      String key = entry.getKey();
      String prefix = entry.getValue();
      columnMappings.put(key, findColumn(firstRow, prefix));
    }

    // Process each row
    for (Map<String, String> row : excelData) {
      try {
        T model = createModelFromRow(row, columnMappings);
        if (model != null) {
          models.add(model);
        }
      } catch (Exception e) {
        System.err.println("Error processing row: " + row + " - " + e.getMessage());
      }
    }

    return models;
  }

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
}