package com.openjfx.controllers;

import com.openjfx.handlers.Export.AssignmentHandler;
import com.openjfx.handlers.Export.RoomPlanHandler;
import com.openjfx.handlers.Export.WorkshopDemandHandler;
import com.openjfx.handlers.Import.Handler;
import com.openjfx.models.Event;
import com.openjfx.models.StudentAssignment;
import com.openjfx.models.WorkshopDemand;
import com.openjfx.services.*;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExportController {

  @FXML
  private Button RoomTimePlanButton;
  @FXML
  private Button PresenceListButton;
  @FXML
  private Button RouteSlipButton;
  @FXML
  private Button AssignmentButton;
  @FXML
  private Button WorkshopDemandButton;
  @FXML
  private TextField searchField;
  @FXML
  private TableView<?> tableView;
  @FXML
  private StackPane tableContainer;
  @FXML
  private ComboBox<String> eventFilterComboBox;
  @FXML
  private MenuButton ExportButton;
  @FXML
  private MenuItem exportToExcelMenuItem;
  @FXML
  private MenuItem exportToPdfMenuItem;

  private final EventService eventService;
  private final AssignmentService assignmentService;
  private final WorkshopDemandService workshopDemandService;
  private final TimetableService timetableService;
  private final ExcelService excelService;
  private WorkshopDemandHandler workshopDemandHandler;
  private Handler<?> currentHandler;
  private AssignmentHandler assignmentHandler;
  private RoomPlanHandler roomPlanHandler;
  private RoomService roomService;

  // Track whether each step has been executed
  private boolean assignmentsGenerated = false;
  private boolean workshopDemandGenerated = false;
  private boolean timetableGenerated = false;

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

    this.assignmentHandler = new AssignmentHandler(this.excelService);
    this.roomPlanHandler = new RoomPlanHandler(this.timetableService, this.excelService, this.roomService);
    this.workshopDemandHandler = new WorkshopDemandHandler(this.assignmentService,
        this.excelService);
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

    // Begin data generation automatically if needed
    Platform.runLater(this::initializeData);
  }

  /**
   * Initialize data for the first view if needed
   */
  private void initializeData() {
    if (!assignmentsGenerated) {
      try {
        assignmentService.loadAllDataAndAssignStudents();
        assignmentsGenerated = true;
        refreshTable(); // Refresh the table to show the generated data
        showInfoAlert("Data Generated", "Student assignments have been generated successfully.");
      } catch (IOException ex) {
        showErrorAlert("Error generating assignments", ex.getMessage());
        ex.printStackTrace();
      }
    }
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
   */
  private void checkExistingData() {
    assignmentsGenerated = assignmentHandler.hasAssignmentData();
    workshopDemandGenerated = workshopDemandHandler.hasWorkshopDemandData();
    timetableGenerated = roomPlanHandler.hasTimetableData();
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
   * @author mian
   */
  private void setupButtons() {
    // Assignment button - check data before handling
    AssignmentButton.setOnAction(e -> {
      // Always check current state of data first
      checkExistingData();

      if (!assignmentsGenerated) {
        try {
          showGeneratingAlert("Generating student assignments...");
          assignmentService.loadAllDataAndAssignStudents();
          assignmentsGenerated = true;
          showInfoAlert("Data Generated", "Student assignments have been generated successfully.");
        } catch (IOException ex) {
          showErrorAlert("Error generating assignments", ex.getMessage());
          ex.printStackTrace();
          return; // Don't proceed if assignment generation fails
        }
      }

      switchHandler(assignmentHandler, AssignmentButton);
    });

    // Workshop demand button - check data before handling
    WorkshopDemandButton.setOnAction(e -> {
      // Always check current state of data first
      checkExistingData();

      if (!assignmentsGenerated) {
        try {
          showGeneratingAlert("Generating student assignments first...");
          assignmentService.loadAllDataAndAssignStudents();
          assignmentsGenerated = true;
          showInfoAlert("Data Generated", "Student assignments have been generated successfully.");
        } catch (IOException ex) {
          showErrorAlert("Error generating assignments", ex.getMessage());
          ex.printStackTrace();
          return; // Don't proceed if assignment generation fails
        }
      }

      if (!workshopDemandGenerated) {
        try {
          showGeneratingAlert("Calculating workshop demand...");
          assignmentService.calculateWorkshopDemandOnly();
          workshopDemandGenerated = true;
        } catch (IOException ex) {
          showErrorAlert("Error calculating workshop demand", ex.getMessage());
          ex.printStackTrace();
          return; // Don't proceed if demand calculation fails
        }
      }

      switchHandler(workshopDemandHandler, WorkshopDemandButton);
    });

    // Room time plan button - check data before handling
    RoomTimePlanButton.setOnAction(e -> {
      // Always check current state of data first
      checkExistingData();

      if (!assignmentsGenerated) {
        try {
          showGeneratingAlert("Generating student assignments first...");
          assignmentService.loadAllDataAndAssignStudents();
          assignmentsGenerated = true;
          showInfoAlert("Data Generated", "Student assignments have been generated successfully.");
        } catch (IOException ex) {
          showErrorAlert("Error generating assignments", ex.getMessage());
          ex.printStackTrace();
          return; // Don't proceed if assignment generation fails
        }
      }

      if (!workshopDemandGenerated) {
        try {
          showGeneratingAlert("Calculating workshop demand...");
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
          showGeneratingAlert("Creating room timetable...");
          Map<Integer, Integer> workshopDemand = assignmentService.loadWorkshopDemand();
          assignmentService.createAndSaveTimetable(workshopDemand);
          timetableGenerated = true;
        } catch (IOException ex) {
          showErrorAlert("Error creating timetable", ex.getMessage());
          ex.printStackTrace();
          return; // Don't proceed if timetable creation fails
        }
      }

      switchHandler(roomPlanHandler, RoomTimePlanButton);
    });

    exportToExcelMenuItem.setOnAction(e -> {
      exportData("excel");
    });

    exportToPdfMenuItem.setOnAction(e -> {
      exportData("pdf");
    });
  }

  /**
   * Exports the table data in the specified format.
   *
   * @param format the export format ("excel" or "pdf")
   * @author mian
   */
  private void exportData(String format) {
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

      if (format.equals("excel")) {
        if (currentHandler instanceof RoomPlanHandler) {
          List<Map<String, Object>> data = roomService.prepareDataForExport((List<Object>) dataToExport);

          try {
            // Export the data to an Excel file
            roomPlanHandler.exportRooms(data);
            showInfoAlert("Export Successful", "Data has been successfully exported to file: '" + roomService.getFilePath() + "'");
          } catch (IOException e) {
            // Error handling if the export fails
            showErrorAlert("File Error", "Could not export to the the file : " + roomService.getFilePath() + ", " + e.getMessage());
          }
        }
      } else if (format.equals("pdf")) {
           //exportToPdf(dataToExport);
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

    // See if we're dealing with maps (like in RoomPlanHandler) or regular objects
    boolean isMapData = !items.isEmpty() && items.get(0) instanceof Map;

    // Calculate column width based on table width
    double columnWidth = tableView.getWidth() / columns.size();

    for (Pair<String, String> column : columns) {
      TableColumn<Object, Object> tableColumn = new TableColumn<>(column.getKey());

      if (isMapData) {
        // For Map data, use a custom cell value factory
        tableColumn.setCellValueFactory(cellData -> {
          Map<String, Object> rowMap = (Map<String, Object>) cellData.getValue();
          Object cellValue = rowMap.get(column.getValue());
          return new javafx.beans.property.SimpleObjectProperty<>(
              cellValue != null ? cellValue : "");
        });
      } else {
        // For regular objects, use PropertyValueFactory
        tableColumn.setCellValueFactory(new PropertyValueFactory<>(column.getValue()));
      }

      tableColumn.setPrefWidth(columnWidth);
      tableColumn.setResizable(true);
      ((TableView<Object>) tableView).getColumns().add(tableColumn);
    }

    // Add items to the table
    TableView<Object> typedTableView = (TableView<Object>) tableView;
    typedTableView.getItems().addAll((List<Object>) items);

    // Add a listener to adjust column widths when the table is resized
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
    if (RouteSlipButton != null) {
      RouteSlipButton.getStyleClass().remove("button-active");
    }
    if (AssignmentButton != null) {
      AssignmentButton.getStyleClass().remove("button-active");
    }
    if (WorkshopDemandButton != null) {
      WorkshopDemandButton.getStyleClass().remove("button-active");
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
   * @author mian
   */
  private void switchHandler(Handler<?> handler, Button activeButton) {
    currentHandler = handler;
    setActiveButton(activeButton);

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
}