package com.openjfx.handlers.Export;

import com.openjfx.handlers.Import.Handler;
import com.openjfx.models.TimeSlot;
import com.openjfx.models.TimetableRow;
import com.openjfx.services.*;
import javafx.util.Pair;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Handles the generation and display of room plans in the timetable export functionality. This
 * handler implements the generic Handler interface to manage room planning data with a map
 * structure for flexible display in the UI.
 *
 * @author mian
 */
public class RoomPlanHandler implements Handler<Map<String, String>> {

  private final AssignmentService assignmentService;
  private final TimetableService timetableService;
  private final TimeSlotService timeSlotService;
  private final ExcelService excelService;
  private final RoomExcelExportService roomExportService;

  /**
   * Constructs a new RoomPlanHandler with the necessary services.
   *
   * @param timetableService service for timetable data handling
   * @param excelService     service for Excel file operations
   * @author mian
   */
  public RoomPlanHandler(TimetableService timetableService, ExcelService excelService, RoomExcelExportService roomExportService) {
    this.timetableService = timetableService;
    this.excelService = excelService;
      this.roomExportService = roomExportService;
      this.timeSlotService = new TimeSlotService();
    ChoiceService choiceService = new ChoiceService(excelService);
    EventService eventService = new EventService(excelService);
    RoomService roomService = new RoomService(excelService);
    StudentAssignmentService studentAssignmentService = new StudentAssignmentService();
    WorkshopDemandService workshopDemandService = new WorkshopDemandService();

    this.assignmentService = new AssignmentService(
        choiceService, eventService, roomService, timeSlotService,
        studentAssignmentService, timetableService, workshopDemandService
    );
  }

  /**
   * Checks if there are timetable assignments in the database.
   *
   * @return true if assignments exist, false otherwise
   * @author mian
   */
  public boolean hasTimetableData() {
    List<TimetableRow> rows = timetableService.getTimetableRowsForDisplay();
    return !rows.isEmpty();
  }

  /**
   * Returns the column definitions for the room plan table. The first column is always the company
   * name, followed by columns for each time slot.
   *
   * @return list of column name-property pairs
   * @author mian
   */
  @Override
  public List<Pair<String, String>> getColumns() {
    List<Pair<String, String>> columns = new ArrayList<>();

    // First column is always company
    columns.add(new Pair<>("Company", "company"));

    // Get all time slots and add them as columns
    List<TimeSlot> timeSlots = timeSlotService.loadTimeSlots();
    for (TimeSlot slot : timeSlots) {
      String header = slot.getSlot() + " (" + slot.getStartTime() + "-" + slot.getEndTime() + ")";
      columns.add(new Pair<>(header, "slot_" + slot.getSlot()));
    }

    return columns;
  }

  /**
   * Generates timetable data by running the assignment process. This method triggers the assignment
   * algorithm to create room assignments.
   *
   * @author mian
   */
  public void generateTimetable() {
    try {
      assignmentService.runAssignment();
    } catch (IOException e) {
      System.err.println("Error generating timetable: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Sets up the table with appropriate columns and cell value factories for Map data. This
   * specialized setup method is used for displaying room plans in a table.
   *
   * @param tableView The TableView to configure
   * @author mian
   */
  public void setupTableWithMapData(TableView<Map<String, String>> tableView) {
    tableView.getColumns().clear();
    tableView.getItems().clear();

    // Get total columns count for width calculation
    List<TimeSlot> timeSlots = timeSlotService.loadTimeSlots();
    int totalColumns = timeSlots.size() + 1; // +1 for company column

    // Add company column first
    TableColumn<Map<String, String>, String> companyCol = new TableColumn<>("Company");
    companyCol.setCellValueFactory(
        data -> new SimpleStringProperty(data.getValue().get("company")));
    companyCol.setPrefWidth(tableView.getWidth() / totalColumns);
    companyCol.setResizable(true);
    tableView.getColumns().add(companyCol);

    // Add timeslot columns
    for (TimeSlot slot : timeSlots) {
      String header = slot.getSlot() + " (" + slot.getStartTime() + "-" + slot.getEndTime() + ")";
      String key = "slot_" + slot.getSlot();

      TableColumn<Map<String, String>, String> slotCol = new TableColumn<>(header);
      slotCol.setCellValueFactory(data ->
          new SimpleStringProperty(data.getValue().getOrDefault(key, "")));
      slotCol.setPrefWidth(tableView.getWidth() / totalColumns);
      slotCol.setResizable(true);
      tableView.getColumns().add(slotCol);
    }

    // Add data to table
    tableView.getItems().addAll(loadData());

    // Add a listener to adjust column widths when the table is resized
    tableView.widthProperty().addListener((obs, oldVal, newVal) -> {
      double newWidth = newVal.doubleValue() / totalColumns;
      tableView.getColumns().forEach(column -> column.setPrefWidth(newWidth));
    });
  }

  /**
   * Loads room plan data from the timetable service and formats it for display. The data is
   * organized by company and time slot for easy viewing in a table format.
   *
   * @return list of maps representing room assignments by company and time slot
   * @author mian
   */
  @Override
  public List<Map<String, String>> loadData() {
    List<TimetableRow> rows = timetableService.getTimetableRowsForDisplay();
    Map<String, Map<String, String>> companyRooms = new HashMap<>();

    // Group by company and organize by time slot
    for (TimetableRow row : rows) {
      String company = row.getCompany();
      String timeSlot = row.getTimeSlot();
      String roomInfo = row.getRoomName();

      companyRooms.putIfAbsent(company, new HashMap<>());
      companyRooms.get(company).put("company", company);
      companyRooms.get(company).put("slot_" + timeSlot, roomInfo);
    }

    return new ArrayList<>(companyRooms.values());
  }

  /**
   * Import functionality is not needed for this export handler.
   *
   * @param selectedFile file to import data from (not used)
   * @throws IOException if an I/O error occurs
   * @author mian
   */
  @Override
  public void importData(File selectedFile) throws IOException {
    // Not needed for export
  }

  /**
   * Checks if an item matches the provided search term.
   *
   * @param item       the map containing room plan data
   * @param searchTerm the term to search for
   * @return true if any value in the map contains the search term, false otherwise
   * @author mian
   */
  @Override
  public boolean matchesSearch(Map<String, String> item, String searchTerm) {
    String lowerTerm = searchTerm.toLowerCase();

    // Check if any value in the map contains the search term
    for (String value : item.values()) {
      if (value != null && value.toLowerCase().contains(lowerTerm)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the text to display on the import button.
   *
   * @return the button text
   * @author mian
   */
  @Override
  public String getImportButtonText() {
    return "Export Room Plan";
  }

  /**
   * Clear functionality is not needed for this export handler.
   *
   * @author mian
   */
  @Override
  public void clearData() {
    // Not needed for export
  }

  /**
   * Returns the Excel service used by this handler.
   *
   * @return the Excel service instance
   * @author mian
   */
  @Override
  public ExcelService getExcelService() {
    return this.excelService;
  }

  /**
   * Exports room data to an Excel file.
   * This method calls the exportDataToExcel() function from the roomExportService
   * to generate and save the room data in Excel format.
   *
   * @param data The room data to be exported as a list of maps.
   * @throws IOException If an error occurs during export.
   *
   * @author leon
   */
  public void exportRooms(List<Map<String, Object>> data) throws IOException {
    roomExportService.exportDataToExcel(data, roomExportService.getFilePath());
  }
}