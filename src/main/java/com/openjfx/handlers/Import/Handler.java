package com.openjfx.handlers.Import;

import com.openjfx.services.ExcelService;
import javafx.util.Pair;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Interface for handling import operations for different types of data.
 *
 * @param <T> the type of data to be imported
 * @author mian
 */
public interface Handler<T> {

  /**
   * Gets the columns to be displayed in the table.
   *
   * @return a list of pairs where each pair contains the column name and the corresponding property
   * name
   * @author mian
   */
  List<Pair<String, String>> getColumns();

  /**
   * Loads the data to be displayed in the table.
   *
   * @return a list of data items
   * @author mian
   */
  List<T> loadData();

  /**
   * Imports data from the specified file.
   *
   * @param selectedFile the file to import data from
   * @throws IOException if an I/O error occurs during import
   * @author mian
   */
  void importData(File selectedFile) throws IOException;

  /**
   * Checks if the given item matches the search term.
   *
   * @param item       the item to check
   * @param searchTerm the search term to match against
   * @return true if the item matches the search term, false otherwise
   * @author mian
   */
  boolean matchesSearch(T item, String searchTerm);

  /**
   * Gets the text to be displayed on the import button.
   *
   * @return the import button text
   * @author mian
   */
  String getImportButtonText();

  /**
   * Clears the existing data.
   *
   * @author mian
   */
  void clearData();

  /**
   * Gets the Excel service used for import operations.
   *
   * @return the Excel service
   * @author mian
   */
  ExcelService getExcelService();
}