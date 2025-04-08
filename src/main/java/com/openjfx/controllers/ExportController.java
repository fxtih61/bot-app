package com.openjfx.controllers;

import com.openjfx.dao.FulfillmentScoreDAO;
import com.openjfx.handlers.Export.AssignmentHandler;
import com.openjfx.handlers.Export.FulfillmentScoreHandler;
import com.openjfx.handlers.Export.RoomPlanHandler;
import com.openjfx.handlers.Export.WorkshopDemandHandler;
import com.openjfx.handlers.Import.Handler;
import com.openjfx.models.Event;
import com.openjfx.models.StudentAssignment;
import com.openjfx.models.WorkshopDemand;
import com.openjfx.services.*;
import java.sql.SQLException;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Pair;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller class for the Export view. This class handles the UI logic for the Export view,
 * including switching between different data views, filtering data, and exporting data to Excel or
 * PDF.
 *
 * @author mian
 */

public class ExportController {

  public Button FulfillmentScore;
  @FXML
  private Button RoomTimePlanButton;
  @FXML
  private Button PresenceListButton;
  @FXML
  private Button AssignmentButton;
  @FXML
  private TextField searchField;
  @FXML
  private TableView<?> tableView;
  @FXML
  private StackPane tableContainer;
  @FXML
  private ComboBox<String> eventFilterComboBox;
  @FXML
  private MenuButton ExportButtonRoom;
  @FXML
  private MenuItem exportToExcelMenuItemRoom;
  @FXML
  private MenuItem exportToPdfMenuItemRoom;
  @FXML
  private MenuButton ExportButtonRoutingSlip;
  @FXML
  private MenuItem exportToExcelMenuItemRoutingSlip;
  @FXML
  private MenuItem exportToPdfMenuItemRoutingSlip;
  @FXML
  private MenuButton ExportButtonAttendanceList;
  @FXML
  private MenuItem exportToExcelMenuItemAttendanceList;
  @FXML
  private MenuItem exportToPdfMenuItemAttendanceList;
  @FXML
  private MenuButton ExportButtonFulfilmentScore;
  @FXML
  private MenuItem exportToExcelMenuItemFulfilmentScore;
  @FXML
  private MenuItem exportToPdfMenuItemFulfilmentScore;

  private boolean dataVerified = false;
  private final EventService eventService;
  private final AssignmentService assignmentService;
  private final WorkshopDemandService workshopDemandService;
  private final TimetableService timetableService;
  private final ExcelService excelService;
  private final StudentTimetableMappingService studentTimetableMappingService;
  private final StudentAssignmentService studentAssignmentService;
  private final FulfillmentScoreService fulfillmentScoreService;
  private WorkshopDemandHandler workshopDemandHandler;
  private Handler<?> currentHandler;
  private AssignmentHandler assignmentHandler;
  private RoomPlanHandler roomPlanHandler;
  private RoomService roomService;
  private FulfillmentScoreHandler fulfillmentScoreHandler;

  // Track whether each step has been executed
  private boolean assignmentsGenerated = false;
  private boolean workshopDemandGenerated = false;
  private boolean timetableGenerated = false;
  private boolean studentTimetableMappingGenerated = false;
  private boolean fulfillmentScoreGenerated = false;

  /**
   * Constructor that initializes the required services for the controller.
   *
   * @author mian
   */
  public ExportController() {
    this.excelService = new ExcelService();
    this.workshopDemandService = new WorkshopDemandService();
    this.timetableService = new TimetableService();
    this.eventService = new EventService(this.excelService);
    this.roomService = new RoomService(this.excelService);

    // Initialize services for AssignmentService
    ChoiceService choiceService = new ChoiceService(this.excelService);
    EventService eventService = new EventService(this.excelService);
    RoomService roomService = new RoomService(this.excelService);
    TimeSlotService timeSlotService = new TimeSlotService();
    StudentAssignmentService studentAssignmentService = new StudentAssignmentService();

    this.assignmentService = new AssignmentService(
        choiceService, eventService, roomService, timeSlotService,
        studentAssignmentService, this.timetableService, this.workshopDemandService);

    this.assignmentHandler = new AssignmentHandler(this.excelService, this.timetableService);
    this.roomPlanHandler = new RoomPlanHandler(this.timetableService, this.excelService,
        this.roomService);
    this.workshopDemandHandler = new WorkshopDemandHandler(this.assignmentService,
        this.excelService);
    this.studentTimetableMappingService = new StudentTimetableMappingService();
    this.studentAssignmentService = new StudentAssignmentService();
    this.fulfillmentScoreService = new FulfillmentScoreService(this.studentAssignmentService);
    this.fulfillmentScoreHandler = new FulfillmentScoreHandler(this.excelService);
  }

  /**
   * Initializes the controller, sets up the UI components.
   *
   * @author mian
   */
  @FXML
  public void initialize() {
    setupButtons();
    setupSearchField();
    setupEventFilter();

    // Check if data already exists and update status flags
    checkExistingData();

    // Set default handler to AssignmentHandler
    switchHandler(assignmentHandler, AssignmentButton);

    // Begin data generation once only if needed
    Platform.runLater(() -> {
      if (!assignmentsGenerated || !workshopDemandGenerated ||
          !timetableGenerated || !studentTimetableMappingGenerated ||
          !fulfillmentScoreGenerated) {
        initializeData();
      }
    });
  }

  /**
   * Initialize data for the first view if needed
   *
   * @author mian
   */
  private void initializeData() {
    // Skip processing if already verified and all data is present
    if (dataVerified && assignmentsGenerated && workshopDemandGenerated &&
        timetableGenerated && studentTimetableMappingGenerated &&
        fulfillmentScoreGenerated) {
      return;
    }

    if (!assignmentsGenerated) {
      try {
        assignmentService.loadAllDataAndAssignStudents();
        assignmentsGenerated = true;
      } catch (IOException ex) {
        showErrorAlert("Error generating assignments", ex.getMessage());
        ex.printStackTrace();
        return; // Don't proceed if assignment generation fails
      }
    }

    if (!workshopDemandGenerated) {
      try {
        assignmentService.calculateWorkshopDemandOnly();
        workshopDemandGenerated = true;
      } catch (IOException ex) {
        showErrorAlert("Error calculating workshop demand", ex.getMessage());
        ex.printStackTrace();
        return; // Don't proceed if demand calculation fails
      }
    }

    if (!timetableGenerated) {
      try {
        Map<Integer, Integer> workshopDemand = assignmentService.loadWorkshopDemand();
        assignmentService.createAndSaveTimetable(workshopDemand);
        timetableGenerated = true;
      } catch (IOException ex) {
        showErrorAlert("Error creating timetable", ex.getMessage());
        ex.printStackTrace();
        return; // Don't proceed if timetable creation fails
      }
    }

    if (!studentTimetableMappingGenerated) {
      try {
        showGeneratingAlert("Mapping students to timetable, please wait...");
        boolean success = studentTimetableMappingService.generateAndMapStudentTimetables();
        studentTimetableMappingGenerated = success;
        if (!success) {
          showErrorAlert("Warning",
              "Some student assignments could not be completed. Manual adjustments may be needed.");
        }
      } catch (Exception ex) {
        showErrorAlert("Error mapping students to timetable", ex.getMessage());
        ex.printStackTrace();
        return;
      }
    }

    if (!fulfillmentScoreGenerated) {
      try {
        showGeneratingAlert("Calculating fulfillment scores, please wait...");
        double fulfillmentScore = fulfillmentScoreService.calculateFulfillmentScore();
        fulfillmentScoreGenerated = true;
        showInfoAlert("Fulfillment Score",
            String.format("Overall fulfillment score: %.2f%%", fulfillmentScore));
      } catch (Exception ex) {
        showErrorAlert("Error calculating fulfillment scores", ex.getMessage());
        ex.printStackTrace();
      }
    }

    // Refresh the table to show the generated data
    refreshTable();
  }

  /**
   * Sets up the event filter dropdown.
   *
   * @author mian
   */
  private void setupEventFilter() {
    // Add "All Events" option first
    eventFilterComboBox.getItems().add("All Events");

    // Load events from database and add to dropdown
    try {
      List<String> eventNames = eventService.loadEvents().stream()
          .map(Event::getCompany)
          .collect(Collectors.toList());
      eventFilterComboBox.getItems().addAll(eventNames);
    } catch (Exception e) {
      System.err.println("Error loading events: " + e.getMessage());
    }

    // Set default selection
    eventFilterComboBox.setValue("All Events");

    // Add listener for selection changes
    eventFilterComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
      filterTableByEvent(newValue);
    });
  }

  /**
   * Filters the table data based on selected event and search term.
   *
   * @param eventName the selected event name, or "All Events" for no filtering
   * @author mian
   */
  @SuppressWarnings("unchecked")
  private void filterTableByEvent(String eventName) {
    if (currentHandler != null) {
      // Get current search term
      String searchTerm = searchField.getText();

      if ("All Events".equals(eventName) && (searchTerm == null || searchTerm.isEmpty())) {
        // If "All Events" is selected and no search term, show all data
        refreshTable();
        return;
      }

      List<?> allItems = currentHandler.loadData();
      List<Object> filteredItems = allItems.stream()
          .filter(item -> {
            // Apply event filter if not "All Events"
            boolean matchesEvent = "All Events".equals(eventName) ||
                matchesEventFilter(item, eventName);

            // Apply search filter if there's a search term
            boolean matchesSearch = searchTerm == null || searchTerm.isEmpty() ||
                ((Handler<Object>) currentHandler).matchesSearch(item, searchTerm);

            return matchesEvent && matchesSearch;
          })
          .collect(Collectors.toList());

      // Update table with filtered items
      TableView<Object> table = (TableView<Object>) tableView;
      table.getItems().clear();
      table.getItems().addAll(filteredItems);
    }
  }

  /**
   * Checks if an item matches the event filter.
   *
   * @param item      the data item to check
   * @param eventName the event name to filter by
   * @return true if the item is associated with the specified event
   * @author mian
   */
  private boolean matchesEventFilter(Object item, String eventName) {
    // Handle different types of data
    if (item instanceof StudentAssignment) {
      // For StudentAssignment, filter by event (company name)
      StudentAssignment assignment = (StudentAssignment) item;
      return assignment.getCompanyName() != null &&
          assignment.getCompanyName().contains(eventName);
    } else if (item instanceof WorkshopDemand) {
      // For WorkshopDemand, filter by company name
      WorkshopDemand demand = (WorkshopDemand) item;
      return demand.getCompanyName() != null &&
          demand.getCompanyName().contains(eventName);
    } else if (item instanceof Map) {
      // For map-based data like RoomPlanHandler
      Map<String, String> mapItem = (Map<String, String>) item;
      String company = mapItem.get("company");
      return company != null && company.contains(eventName);
    }
    return false;
  }

  /**
   * Check if data already exists in the database for each handler
   *
   * @author mian
   */
  private void checkExistingData() {
    // Check if handlers have data
    assignmentsGenerated = assignmentHandler.hasAssignmentData();
    workshopDemandGenerated = workshopDemandHandler.hasWorkshopDemandData();
    timetableGenerated = roomPlanHandler.hasTimetableData();

    try {
      FulfillmentScoreDAO fulfillmentScoreDAO = new FulfillmentScoreDAO();
      fulfillmentScoreGenerated = fulfillmentScoreDAO.hasFulfillmentScores();
      if (fulfillmentScoreGenerated) {
        System.out.println("Fulfillment scores already exist in database");
      }
    } catch (SQLException e) {
      System.err.println("Error checking fulfillment scores: " + e.getMessage());
      fulfillmentScoreGenerated = false;
    }

    // Check if students are already mapped to timetables
    if (assignmentsGenerated && timetableGenerated) {
      List<StudentAssignment> assignments = studentTimetableMappingService.getAllStudentAssignments();
      studentTimetableMappingGenerated = !assignments.isEmpty() &&
          assignments.stream()
              .allMatch(a -> a.getTimeSlot() != null && a.getRoomId() != null);

      if (studentTimetableMappingGenerated) {
        System.out.println(
            "Student timetable mapping verified: Students already assigned to rooms and time slots");
      }
    }

    // Set dataVerified flag to true once we've checked
    dataVerified = true;
  }

  /**
   * Sets up the search field functionality.
   *
   * @author mian
   */
  private void setupSearchField() {
    searchField.textProperty().addListener((observable, oldValue, newValue) -> {
      performSearch(newValue);
    });
  }

  /**
   * Sets up the button actions.
   *
   * @author mian | leon
   */
  private void setupButtons() {
    // Assignment button - only switch view
    AssignmentButton.setOnAction(e -> switchHandler(assignmentHandler, AssignmentButton));

    // Room time plan button - only switch view
    RoomTimePlanButton.setOnAction(e -> switchHandler(roomPlanHandler, RoomTimePlanButton));

    // Fulfillment score button - only switch view
    FulfillmentScore.setOnAction(e -> switchHandler(fulfillmentScoreHandler, FulfillmentScore));

    exportToExcelMenuItemRoom.setOnAction(e -> exportData("excelRoom", ""));
    exportToPdfMenuItemRoom.setOnAction(e -> exportData("pdfRoom", ""));

    exportToExcelMenuItemAttendanceList.setOnAction(e -> exportData("excelAttendanceList", ""));
    exportToPdfMenuItemAttendanceList.setOnAction(e -> exportData("pdfAttendanceList", ""));

    exportToExcelMenuItemRoutingSlip.setOnAction(
        e -> exportData("excelRoutingSlip", searchField.getText()));
    exportToPdfMenuItemRoutingSlip.setOnAction(
        e -> exportData("pdfRoutingSlip", searchField.getText()));

    exportToExcelMenuItemFulfilmentScore.setOnAction(
            e -> exportData("excelFulfilmentScore", searchField.getText()));
    exportToPdfMenuItemFulfilmentScore.setOnAction(
            e -> exportData("pdfFulfilmentScore", searchField.getText()));
  }

  /**
   * Exports the table data in the specified format.
   *
   * @param format      The export format ("excelRoom", "pdfRoom", "excelAttendanceList",
   *                    "pdfAttendanceList", "excelRoutingSlip", "pdfRoutingSlip")
   * @param searchField the value of the search fields
   * @author mian | leon
   */
  private void exportData(String format, String searchField) {
    String filterName = eventFilterComboBox.getValue();

    if (currentHandler != null) {
      List<?> dataToExport;

      if (tableView.getItems().isEmpty()
          || tableView.getItems().size() != currentHandler.loadData().size()) {
        // If the table is filtered, export only the visible data
        dataToExport = new ArrayList<>(tableView.getItems());
      } else {
        // If the table is not filtered, export all data
        dataToExport = currentHandler.loadData();
      }

      switch (format) {
        case "excelRoom":
          handleExcelExport(dataToExport, filterName, currentHandler);
          break;
        case "pdfRoom":
          handlePdfExport(dataToExport, filterName, currentHandler);
          break;
        case "excelAttendanceList":
          handleAttendanceListExport(dataToExport, filterName);
          break;
        case "pdfAttendanceList":
          handleAttendanceListExportPDF(dataToExport, filterName);
          break;
        case "excelRoutingSlip":
          handleRoutingSlipExport(dataToExport, searchField);
          break;
        case "pdfRoutingSlip":
          handleRoutingSlipExportPDF(dataToExport, searchField);
          break;
        case "excelFulfilmentScore":
          handleFulfilmentScoreExport(dataToExport, searchField);
          break;
        case "pdfFulfilmentScore":
          handleFulfilmentScoreExportPDF(dataToExport, searchField);
          break;
      }
    }
  }

  /**
   * Shows an informational alert with the specified message.
   *
   * @param message the alert message
   * @author mian
   */
  private void showGeneratingAlert(String message) {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("Processing");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.show();
  }

  /**
   * Shows an error alert with the specified title and message.
   *
   * @param title   the alert title
   * @param message the alert message
   * @author mian
   */
  private void showErrorAlert(String title, String message) {
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  /**
   * Shows an info alert with the specified title and message.
   *
   * @param title   the alert title
   * @param message the alert message
   * @author mian
   */
  private void showInfoAlert(String title, String message) {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  /**
   * Performs search filtering on the table based on the search term.
   *
   * @param term the search term
   * @author mian
   */
  @SuppressWarnings("unchecked")
  private void performSearch(String term) {
    if (currentHandler != null) {
      String eventFilter = eventFilterComboBox.getValue();

      // Reset event filter to "All Events" if search field is cleared
      if (term == null || term.isEmpty()) {
        if (!eventFilter.equals("All Events")) {
          eventFilterComboBox.setValue("All Events");
        } else {
          refreshTable();
        }
        return;
      }

      if ("All Events".equals(eventFilter)) {
        // If "All Events" is selected, just filter by search term
        List<?> allItems = currentHandler.loadData();
        List<Object> filteredItems = allItems.stream()
            .filter(item -> {
              // Use explicit casting to handle the wildcard capture
              Handler<Object> typedHandler = (Handler<Object>) currentHandler;
              return typedHandler.matchesSearch(item, term);
            })
            .collect(Collectors.toList());

        // Cast tableView to use proper generic type
        TableView<Object> table = (TableView<Object>) tableView;
        table.getItems().clear();
        table.getItems().addAll(filteredItems);
      } else {
        // If a specific event is selected, apply both filters
        filterTableByEvent(eventFilter);
      }
    }
  }

  /**
   * Refreshes the table with the current handler's data.
   *
   * @author mian
   */
  private void refreshTable() {
    if (currentHandler != null) {
      List<Pair<String, String>> columns = currentHandler.getColumns();
      List<?> items = currentHandler.loadData();
      setupTable(columns, items);
    }
  }

  /**
   * Sets up the table with the specified columns and items.
   *
   * @param columns column definitions
   * @param items   data items
   * @author mian
   */
  @SuppressWarnings("unchecked")
  private void setupTable(List<Pair<String, String>> columns, List<?> items) {
    tableView.getColumns().clear();
    tableView.getItems().clear();

    boolean isMapData = !items.isEmpty() && items.get(0) instanceof Map;
    double columnWidth = tableView.getWidth() / columns.size();

    TableView<Object> table = (TableView<Object>) tableView;
    TableColumn<Object, Object> overallColumn = null;

    for (Pair<String, String> column : columns) {
      TableColumn<Object, Object> tableColumn = new TableColumn<>(column.getKey());

      if (column.getValue().equals("overallFulfillmentPercentage")) {
        // Format percentage with 2 decimal places
        tableColumn.setCellFactory(tc -> new TableCell<Object, Object>() {
          @Override
          protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
              setText(null);
            } else {
              setText(String.format("%.2f%%", (Double) item));
            }
          }
        });

        // Add sort comparator for percentage values
        tableColumn.setComparator((o1, o2) -> {
          if (o1 == null && o2 == null) {
            return 0;
          }
          if (o1 == null) {
            return -1;
          }
          if (o2 == null) {
            return 1;
          }
          return Double.compare((Double) o2, (Double) o1); // Reverse order for descending sort
        });

        overallColumn = tableColumn;
      }

      if (isMapData) {
        tableColumn.setCellValueFactory(cellData -> {
          Map<String, Object> rowMap = (Map<String, Object>) cellData.getValue();
          Object cellValue = rowMap.get(column.getValue());
          return new javafx.beans.property.SimpleObjectProperty<>(
              cellValue != null ? cellValue : "");
        });
      } else {
        tableColumn.setCellValueFactory(new PropertyValueFactory<>(column.getValue()));
      }

      tableColumn.setPrefWidth(columnWidth);
      tableColumn.setResizable(true);
      table.getColumns().add(tableColumn);
    }

    table.getItems().addAll((List<Object>) items);

    // Apply sorting if it's the FulfillmentScoreHandler and we found the overall column
    if (currentHandler instanceof FulfillmentScoreHandler && overallColumn != null) {
      table.getSortOrder().clear();
      overallColumn.setSortType(TableColumn.SortType.DESCENDING);
      table.getSortOrder().add(overallColumn);
      table.sort();
    }

    tableView.widthProperty().addListener((obs, oldVal, newVal) -> {
      double newWidth = newVal.doubleValue() / columns.size();
      tableView.getColumns().forEach(column -> column.setPrefWidth(newWidth));
    });
  }

  /**
   * Sets the active button style.
   *
   * @param button the button to set as active
   * @author mian
   */
  private void setActiveButton(Button button) {
    // Remove active class from all buttons
    if (RoomTimePlanButton != null) {
      RoomTimePlanButton.getStyleClass().remove("button-active");
    }
    if (PresenceListButton != null) {
      PresenceListButton.getStyleClass().remove("button-active");
    }
    if (FulfillmentScore != null) {
      FulfillmentScore.getStyleClass().remove("button-active");
    }
    if (AssignmentButton != null) {
      AssignmentButton.getStyleClass().remove("button-active");
    }

    // Add active class to selected button
    if (button != null) {
      button.getStyleClass().add("button-active");
    }
  }

  /**
   * Switches the current handler and updates the UI accordingly, preserving the current event
   * filter selection.
   *
   * @param handler      the new handler
   * @param activeButton the button associated with the new handler
   * @author mian | leon
   */
  private void switchHandler(Handler<?> handler, Button activeButton) {
    currentHandler = handler;
    setActiveButton(activeButton);

    // Reset all export buttons visibility first
    resetAllExportButtons();

    // Show only the relevant export buttons
    if (handler instanceof AssignmentHandler) {
      setupAssignmentExportButtons();
    }
    else if (handler instanceof RoomPlanHandler) {
      setupRoomExportButtons();
    }
    else if (handler instanceof FulfillmentScoreHandler) {
      setupFulfillmentExportButtons();
    }

    // Get current event filter before refreshing
    String selectedEvent = eventFilterComboBox.getValue();

    // If "All Events" is selected, just refresh the table normally
    if ("All Events".equals(selectedEvent)) {
      refreshTable();
    } else {
      // First load the data, then apply the event filter
      refreshTable();
      // Apply the current event filter to the new handler's data
      filterTableByEvent(selectedEvent);
    }
  }
  /**
   * Resets the visibility of all export buttons by hiding them.
   * This ensures a clean state before showing view-specific buttons.
   *
   * @author leon
   */
  private void resetAllExportButtons() {
    ExportButtonRoom.setVisible(false);
    ExportButtonRoom.setManaged(false);
    ExportButtonRoutingSlip.setVisible(false);
    ExportButtonRoutingSlip.setManaged(false);
    ExportButtonAttendanceList.setVisible(false);
    ExportButtonAttendanceList.setManaged(false);
    ExportButtonFulfilmentScore.setVisible(false);
    ExportButtonFulfilmentScore.setManaged(false);
  }

  /**
   * Configures and displays the export buttons specific to the Assignment view.
   * Shows buttons for exporting routing slips and attendance lists.
   *
   * @author leon
   */
  private void setupAssignmentExportButtons() {
    ExportButtonRoutingSlip.setVisible(true);
    ExportButtonRoutingSlip.setManaged(true);
    ExportButtonAttendanceList.setVisible(true);
    ExportButtonAttendanceList.setManaged(true);
  }

  /**
   * Configures and displays the export buttons specific to the Room view.
   * Shows the button for exporting room plans.
   *
   * @author leon
   */
  private void setupRoomExportButtons() {
    ExportButtonRoom.setVisible(true);
    ExportButtonRoom.setManaged(true);
  }

  /**
   * Configures and displays the export buttons specific to the Fulfillment Score view.
   * Shows the button for exporting fulfillment score reports.
   *
   * @author leon
   */
  private void setupFulfillmentExportButtons() {
    ExportButtonFulfilmentScore.setVisible(true);
    ExportButtonFulfilmentScore.setManaged(true);
  }

  /**
   * Handles the export of room data to Excel format
   *
   * @param dataToExport   The room data to export
   * @param filterName     The filter name to include in the filename
   * @param currentHandler The RoomPlanHandler instance for processing
   *
   * @author leon
   */
  private void handleExcelExport(Object dataToExport, String filterName, Object currentHandler) {
    if (!(currentHandler instanceof RoomPlanHandler)) {
      return;
    }

    List<Map<String, Object>> data = roomService.prepareDataForExport((List<Object>) dataToExport);
    String filePath = roomService.getFilePath() + "_" + filterName + ".xlsx";

    try {
      ((RoomPlanHandler) currentHandler).exportRooms(data, filterName);
      showInfoAlert("Export Successful",
          "Data has been successfully exported to file: '" + filePath + "'");
    } catch (IOException e) {
      showErrorAlert("File Error",
          "Could not export to the file: " + filePath + ", " + e.getMessage());
    }
  }

  /**
   * Handles the export of room data to PDF format
   *
   * @param dataToExport   The room data to export
   * @param filterName     The filter name to include in the filename
   * @param currentHandler The RoomPlanHandler instance for processing
   *
   * @author leon
   */
  private void handlePdfExport(Object dataToExport, String filterName, Object currentHandler) {
    if (!(currentHandler instanceof RoomPlanHandler)) return;

    List<Map<String, Object>> data = roomService.prepareDataForExport((List<Object>) dataToExport);
    String filePath = roomService.getFilePath() + "_" + filterName + ".pdf";

    try {
      ((RoomPlanHandler) currentHandler).exportRoomsPDF(data, filterName);
      showInfoAlert("Export Successful",
              "Data has been successfully exported to file: '" + filePath + "'");
    } catch (IOException e) {
      showErrorAlert("File Error",
              "Could not export to the file: " + filePath + ", " + e.getMessage());
    }
  }


  /**
   * Handles the export of attendance list data to Excel format
   *
   * @param dataToExport The attendance data to export
   * @param filterName   The filter name (must not be "All Events")
   *
   * @author leon
   */
  private void handleAttendanceListExport(Object dataToExport, String filterName) {
    if (filterName.equals("All Events")) {
      showErrorAlert("Choice Error", "Please select a specific event and not All Events.");
      return;
    }

    Map<String, Object> data = (Map<String, Object>) timetableService.prepareDataForExportForAttendanceList(
        (List<Object>) dataToExport);
    String filePath = timetableService.getFilePathEvent() + "_" + filterName + ".xlsx";

    try {
      assignmentHandler.exportEvents(data, filterName);
      showInfoAlert("Export Successful",
          "Data has been successfully exported to file: '" + filePath + "'");
    } catch (IOException e) {
      showErrorAlert("File Error",
          "Could not export to the file: " + filePath + e.getMessage());
    }
  }

  /**
   * Handles the export of attendance list data to PDF format
   *
   * @param dataToExport The attendance data to export
   * @param filterName   The filter name (must not be "All Events")
   *
   * @author leon
   */
  private void handleAttendanceListExportPDF(Object dataToExport, String filterName) {
    if (filterName.equals("All Events")) {
      showErrorAlert("Choice Error", "Please select a specific event and not All Events.");
      return;
    }

    Map<String, Object> data = (Map<String, Object>) timetableService.prepareDataForExportForAttendanceList(
            (List<Object>) dataToExport);
    String filePath = timetableService.getFilePathEvent() + "_" + filterName + ".pdf";

    try {
      assignmentHandler.exportEventsPDF(data, filterName);
      showInfoAlert("Export Successful",
              "Data has been successfully exported to file: '" + filePath + "'");
    } catch (IOException e) {
      showErrorAlert("File Error",
              "Could not export to the file: " + filePath + e.getMessage());
    }
  }

  /**
   * Handles the export of routing slip data to Excel format
   *
   * @param dataToExport The routing slip data to export
   * @param searchField  The search field to include in the filename
   * @author leon
   */
  private void handleRoutingSlipExport(Object dataToExport, String searchField) {
    Map<String, Object> preparedData = timetableService.prepareDataForExportForRoutingSlip(
        (List<Object>) dataToExport);
    String filePath = timetableService.getFilePathChoices() +
        (searchField.isEmpty() ? ".xlsx" : "_" + searchField + ".xlsx");

    try {
      assignmentHandler.exportChoices(preparedData, searchField);
      showInfoAlert("Export Successful",
          "Data has been successfully exported to file: '" + filePath + "'");
    } catch (IOException e) {
      showErrorAlert("File Error",
          "Could not export to the file: " + filePath + "'" + e.getMessage());
    }
  }
  /**
   * Handles the export of routing slip data to Pdf format
   *
   * @param dataToExport The routing slip data to export
   * @param searchField  The search field to include in the filename
   * @author leon
   */
  private void handleRoutingSlipExportPDF(Object dataToExport, String searchField) {
    Map<String, Object> preparedData = timetableService.prepareDataForExportForRoutingSlip(
            (List<Object>) dataToExport);
    String filePath = timetableService.getFilePathChoices() +
            (searchField.isEmpty() ? ".pdf" : "_" + searchField + ".pdf");

    try {
      assignmentHandler.exportChoicesPDF(preparedData, searchField);
      showInfoAlert("Export Successful",
              "Data has been successfully exported to file: '" + filePath + "'");
    } catch (IOException e) {
      showErrorAlert("File Error",
              "Could not export to the file: " + filePath + "'" + e.getMessage());
    }
  }

  /**
   * Handles the export of fulfilment score data to Excel format
   *
   * @param dataToExport The fulfilment score data to export
   * @param searchField  The search field to include in the filename
   * @author leon
   */
  private void handleFulfilmentScoreExport(Object dataToExport, String searchField) {
    Map<String, Object> preparedData = fulfillmentScoreService.prepareDataForExportForFulfillmentScore(
            (List<Object>) dataToExport);
    String filePath = fulfillmentScoreService.getFilePathScore() +
            (searchField.isEmpty() ? ".xlsx" : "_" + searchField + ".xlsx");

    try {
      fulfillmentScoreHandler.exportScore(preparedData, searchField);
      showInfoAlert("Export Successful",
              "Data has been successfully exported to file: '" + filePath + "'");
    } catch (IOException e) {
      showErrorAlert("File Error",
              "Could not export to the file: " + filePath + "'" + e.getMessage());
    }
  }

  /**
   * Handles the export of fulfilment score data to PDF format
   *
   * @param dataToExport The fulfilment score data to export
   * @param searchField  The search field to include in the filename
   * @author leon
   */
  private void handleFulfilmentScoreExportPDF(Object dataToExport, String searchField) {
    Map<String, Object> preparedData = fulfillmentScoreService.prepareDataForExportForFulfillmentScore(
            (List<Object>) dataToExport);
    String filePath = fulfillmentScoreService.getFilePathScore() +
            (searchField.isEmpty() ? ".pdf" : "_" + searchField + ".pdf");

    try {
      fulfillmentScoreHandler.exportScorePDF(preparedData, searchField);
      showInfoAlert("Export Successful",
              "Data has been successfully exported to file: '" + filePath + "'");
    } catch (IOException e) {
      showErrorAlert("File Error",
              "Could not export to the file: " + filePath + "'" + e.getMessage());
    }
  }

}