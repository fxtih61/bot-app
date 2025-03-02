package com.openjfx.handlers.Export;

import com.openjfx.handlers.Import.Handler;
import com.openjfx.models.WorkshopDemand;
import com.openjfx.services.AssignmentService;
import com.openjfx.services.ExcelService;
import com.openjfx.services.WorkshopDemandService;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler for displaying and managing workshop demand data.
 *
 * @author mian
 */
public class WorkshopDemandHandler implements Handler<WorkshopDemand> {

  private final WorkshopDemandService workshopDemandService;
  private final AssignmentService assignmentService;
  private final ExcelService excelService;

  /**
   * Constructs a new WorkshopDemandHandler with necessary services.
   *
   * @param assignmentService service for assignment calculations
   * @param excelService      service for Excel operations
   * @author mian
   */
  public WorkshopDemandHandler(AssignmentService assignmentService, ExcelService excelService) {
    this.assignmentService = assignmentService;
    this.excelService = excelService;
    this.workshopDemandService = new WorkshopDemandService();
  }

  /**
   * Returns column definitions for the workshop demand table.
   *
   * @return list of column name-property pairs
   * @author mian
   */
  @Override
  public List<Pair<String, String>> getColumns() {
    List<Pair<String, String>> columns = new ArrayList<>();
    columns.add(new Pair<>("Company", "companyName"));
    columns.add(new Pair<>("Demand", "demand"));
    return columns;
  }

  /**
   * Loads workshop demand data from the database. If no data exists, it triggers calculation
   * through the assignment service.
   *
   * @return list of workshop demand objects
   * @author mian
   */
  @Override
  public List<WorkshopDemand> loadData() {
    List<WorkshopDemand> workshopDemands = workshopDemandService.getAllWorkshopDemands();

    // If no data exists, try to calculate it
    if (workshopDemands.isEmpty()) {
      try {
        assignmentService.calculateWorkshopDemandOnly();
        // Reload data after calculation
        workshopDemands = workshopDemandService.getAllWorkshopDemands();
      } catch (IOException e) {
        System.err.println("Error calculating workshop demand: " + e.getMessage());
      }
    }

    return workshopDemands;
  }

  /**
   * Import functionality not needed for this handler.
   *
   * @param selectedFile file to import (not used)
   * @throws IOException if an I/O error occurs
   * @author mian
   */
  @Override
  public void importData(File selectedFile) throws IOException {
    // Not needed for this handler
  }

  /**
   * Checks if a workshop demand item matches the search term.
   *
   * @param item       the workshop demand item to check
   * @param searchTerm the search term
   * @return true if the item matches the search term
   * @author mian
   */
  @Override
  public boolean matchesSearch(WorkshopDemand item, String searchTerm) {
    String lowerTerm = searchTerm.toLowerCase();
    return
        (item.getCompanyName() != null && item.getCompanyName().toLowerCase().contains(lowerTerm))
            ||
            String.valueOf(item.getDemand()).contains(lowerTerm);
  }

  /**
   * Returns the text for the action button.
   *
   * @return the button text
   * @author mian
   */
  @Override
  public String getImportButtonText() {
    return "Export Workshop Demands";
  }

  /**
   * Clear functionality not needed for this handler.
   *
   * @author mian
   */
  @Override
  public void clearData() {
    // Not needed for this handler
  }

  /**
   * Returns the Excel service.
   *
   * @return the Excel service instance
   * @author mian
   */
  @Override
  public ExcelService getExcelService() {
    return this.excelService;
  }

  /**
   * Checks if there is workshop demand data in the database.
   *
   * @return true if data exists, false otherwise
   * @author mian
   */
  public boolean hasWorkshopDemandData() {
    return !workshopDemandService.getAllWorkshopDemands().isEmpty();
  }
}