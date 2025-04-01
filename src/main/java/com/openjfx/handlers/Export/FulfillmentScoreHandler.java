package com.openjfx.handlers.Export;

import com.openjfx.dao.FulfillmentScoreDAO;
import com.openjfx.handlers.Import.Handler;
import com.openjfx.models.FulfillmentScore;
import com.openjfx.services.ExcelService;
import com.openjfx.services.FulfillmentScoreService;
import com.openjfx.services.StudentAssignmentService;
import java.sql.SQLException;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handler class for managing fulfillment score data and display.
 *
 * <p>This class implements the Handler interface for FulfillmentScore objects,
 * providing functionality to load, display, and search fulfillment scores.</p>
 *
 * @author mian
 */
public class FulfillmentScoreHandler implements Handler<FulfillmentScore> {

  private final FulfillmentScoreService fulfillmentScoreService;
  private final ExcelService excelService;
  private final FulfillmentScoreDAO fulfillmentScoreDAO;

  /**
   * Constructs a new FulfillmentScoreHandler with the specified ExcelService.
   *
   * @param excelService the ExcelService to use for data export
   * @author mian
   */
  public FulfillmentScoreHandler(ExcelService excelService) {
    this.excelService = excelService;
    StudentAssignmentService studentAssignmentService = new StudentAssignmentService();
    this.fulfillmentScoreService = new FulfillmentScoreService(studentAssignmentService);
    this.fulfillmentScoreDAO = new FulfillmentScoreDAO();
  }

  /**
   * Gets the column definitions for the fulfillment score table.
   *
   * @return List of column name and property pairs
   * @author mian
   */
  @Override
  public List<Pair<String, String>> getColumns() {
    List<Pair<String, String>> columns = new ArrayList<>();
    columns.add(new Pair<>("Class", "classRef"));
    columns.add(new Pair<>("First Name", "firstName"));
    columns.add(new Pair<>("Last Name", "lastName"));
    columns.add(new Pair<>("Choice 1 Score", "choice1Score"));
    columns.add(new Pair<>("Choice 2 Score", "choice2Score"));
    columns.add(new Pair<>("Choice 3 Score", "choice3Score"));
    columns.add(new Pair<>("Choice 4 Score", "choice4Score"));
    columns.add(new Pair<>("Choice 5 Score", "choice5Score"));
    columns.add(new Pair<>("Choice 6 Score", "choice6Score"));
    columns.add(new Pair<>("Total Score", "studentTotalScore"));
    columns.add(new Pair<>("Overall %", "overallFulfillmentPercentage"));
    columns.add(new Pair<>("Class Total", "totalScore"));
    columns.add(new Pair<>("Max Possible", "maxPossibleScore"));
    return columns;
  }

  /**
   * Loads all fulfillment scores from the database.
   *
   * @return List of FulfillmentScore objects
   * @author mian
   */
  @Override
  public List<FulfillmentScore> loadData() {
    try {
      return fulfillmentScoreDAO.getAllFulfillmentScores();
    } catch (SQLException e) {
      System.err.println("Error loading fulfillment scores: " + e.getMessage());
      e.printStackTrace();
      return new ArrayList<>(); // Return empty list on error
    }
  }

  /**
   * Import functionality is not supported for fulfillment scores.
   *
   * @param selectedFile unused
   * @throws IOException never thrown
   * @author mian
   */
  @Override
  public void importData(File selectedFile) throws IOException {
    // Not needed
  }

  /**
   * Checks if a fulfillment score matches the search term.
   *
   * @param item       the FulfillmentScore to check
   * @param searchTerm the term to search for
   * @return true if the item matches the search term
   * @author mian
   */
  @Override
  public boolean matchesSearch(FulfillmentScore item, String searchTerm) {
    String lowerTerm = searchTerm.toLowerCase();
    return item.getFirstName().toLowerCase().contains(lowerTerm) ||
        item.getLastName().toLowerCase().contains(lowerTerm) ||
        item.getClassRef().toLowerCase().contains(lowerTerm);
  }

  /**
   * Gets the text for the import button.
   *
   * @return button text string
   * @author mian
   */
  @Override
  public String getImportButtonText() {
    return "Export Fulfillment Scores";
  }

  /**
   * Clear functionality is not supported for fulfillment scores.
   *
   * @author mian
   */
  @Override
  public void clearData() {
    // Not needed
  }

  /**
   * Gets the ExcelService instance used by this handler.
   *
   * @return ExcelService instance
   * @author mian
   */
  @Override
  public ExcelService getExcelService() {
    return this.excelService;
  }
}