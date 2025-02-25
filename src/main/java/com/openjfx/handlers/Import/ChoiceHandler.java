package com.openjfx.handlers.Import;

import com.openjfx.models.Choice;
import com.openjfx.services.ChoiceService;
import com.openjfx.services.ExcelService;
import com.openjfx.utils.TempFileManager;
import javafx.util.Pair;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Handler for importing Choice data from an Excel file.
 */
public class ChoiceHandler implements Handler<Choice> {

  private final ChoiceService choiceService;

  /**
   * Constructs a ChoiceImportHandler with the specified ExcelService.
   *
   * @param excelService the Excel service to use for importing data
   */
  public ChoiceHandler(ExcelService excelService) {
    this.choiceService = new ChoiceService(excelService);
  }

  /**
   * Gets the columns to be displayed in the table.
   *
   * @return a list of pairs where each pair contains the column name and the corresponding property
   * name
   */
  @Override
  public List<Pair<String, String>> getColumns() {
    return List.of(
        new Pair<>("Class", "classRef"),
        new Pair<>("First Name", "firstName"),
        new Pair<>("Last Name", "lastName"),
        new Pair<>("Choice 1", "choice1"),
        new Pair<>("Choice 2", "choice2"),
        new Pair<>("Choice 3", "choice3"),
        new Pair<>("Choice 4", "choice4"),
        new Pair<>("Choice 5", "choice5"),
        new Pair<>("Choice 6", "choice6")
    );
  }

  /**
   * Loads the choice data to be displayed in the table.
   *
   * @return a list of choice data items
   */
  @Override
  public List<Choice> loadData() {
    return choiceService.loadChoices();
  }

  /**
   * Imports choice data from the specified file.
   *
   * @param selectedFile the file to import data from
   * @throws IOException if an I/O error occurs during import
   */
  @Override
  public void importData(File selectedFile) throws IOException {
    File tempFile = TempFileManager.createTempFile(selectedFile);
    try {
      List<Choice> choices = choiceService.loadFromExcel(new File(tempFile.getAbsolutePath()));

      if (choices.isEmpty()) {
        throw new IllegalArgumentException("The Excel file contains no data rows");
      }

      // Clear existing data before importing new data
      clearData();
      choices.forEach(choiceService::saveChoice);
    } finally {
      TempFileManager.deleteTempFile(tempFile);
    }
  }

  /**
   * Checks if the given choice matches the search term.
   *
   * @param choice     the choice to check
   * @param searchTerm the search term to match against
   * @return true if the choice matches the search term, false otherwise
   */
  @Override
  public boolean matchesSearch(Choice choice, String searchTerm) {
    String lowerTerm = searchTerm.toLowerCase();
    return choice.getClassRef().toLowerCase().contains(lowerTerm) ||
        choice.getFirstName().toLowerCase().contains(lowerTerm) ||
        choice.getLastName().toLowerCase().contains(lowerTerm) ||
        matchesChoiceNumber(choice, lowerTerm);
  }

  /**
   * Checks if any of the choice numbers match the search term.
   *
   * @param choice     the choice to check
   * @param searchTerm the search term to match against
   * @return true if any choice number matches the search term, false otherwise
   */
  private boolean matchesChoiceNumber(Choice choice, String searchTerm) {
    return choice.getChoice1().toLowerCase().contains(searchTerm) ||
        choice.getChoice2().toLowerCase().contains(searchTerm) ||
        choice.getChoice3().toLowerCase().contains(searchTerm) ||
        choice.getChoice4().toLowerCase().contains(searchTerm) ||
        choice.getChoice5().toLowerCase().contains(searchTerm) ||
        choice.getChoice6().toLowerCase().contains(searchTerm);
  }

  /**
   * Gets the text to be displayed on the import button.
   *
   * @return the import button text
   */
  @Override
  public String getImportButtonText() {
    return "Import Choices";
  }

  /**
   * Clears the existing choice data.
   */
  @Override
  public void clearData() {
    choiceService.clearChoices();
  }

  /**
   * Gets the Excel service used for import operations.
   *
   * @return the Excel service
   */
  @Override
  public ExcelService getExcelService() {
    return choiceService.getExcelService();
  }
}