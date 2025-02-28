package com.openjfx.controllers;

import com.openjfx.handlers.Export.RoomPlanHandler;
import com.openjfx.handlers.Import.Handler;
import com.openjfx.services.ExcelService;
import com.openjfx.services.TimetableService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.Pair;

/**
 * Controller for the export view, handling the export of data to Excel files.
 *
 * @author mian
 */

public class ExportController {

  @FXML
  private TableView<Object> tableView;
  @FXML
  private Button RoomTimePlanButton;
  @FXML
  private Button PresenceListButton;
  @FXML
  private Button RouteSlipButton;
  @FXML
  private Button ExportButton;
  @FXML
  private Button AssignmentButton;
  @FXML
  private TextField searchField;

  private Handler<?> currentHandler;
  private final RoomPlanHandler roomPlanHandler;
  @FXML
  private Button generateTimetableButton;
  @FXML
  private StackPane tableContainer;

  /**
   * Constructor initializes the export handlers with the Excel service.
   *
   * @author mian
   */
  public ExportController() {
    TimetableService timetableService = new TimetableService();
    ExcelService excelService = new ExcelService();
    roomPlanHandler = new RoomPlanHandler(timetableService, excelService);
  }

  /**
   * Initializes the controller, setting up the search field and buttons.
   *
   * @author mian
   */
  @FXML
  public void initialize() {
    setupSearchField();
    setupButtons();

    // Set RoomTimePlanButton as default selection
    RoomTimePlanButton.fire();
  }

  /**
   * Sets up the search field to perform search on text change.
   *
   * @author mian
   */
  private void setupSearchField() {
    searchField.textProperty().addListener((obs, oldVal, newVal) ->
        performSearch(newVal.trim())
    );
  }

  /**
   * Sets up the buttons to switch handlers and handle import actions.
   *
   * @author mian
   */
  private void setupButtons() {
    RoomTimePlanButton.setOnAction(e -> switchHandler(roomPlanHandler, RoomTimePlanButton));

    // Create generate timetable button
    generateTimetableButton = new Button("Generate Timetable");
    generateTimetableButton.getStyleClass().add("generate-button");
    generateTimetableButton.setOnAction(e -> {
      roomPlanHandler.generateTimetable();
      checkAndDisplayTimetableData();
    });

  }

  /**
   * Performs search on the current handler's data based on the search term.
   *
   * @param term the search term
   * @author mian
   */
  @SuppressWarnings("unchecked")
  private void performSearch(String term) {
    if (term.isEmpty()) {
      if (currentHandler instanceof RoomPlanHandler) {
        checkAndDisplayTimetableData();
      } else {
        refreshTable();
      }
      return;
    }

    if (currentHandler instanceof RoomPlanHandler) {
      // Special handling for RoomPlanHandler which uses Map data
      List<Map<String, String>> allData = ((RoomPlanHandler) currentHandler).loadData();
      List<Map<String, String>> filteredData = allData.stream()
          .filter(item -> ((RoomPlanHandler) currentHandler).matchesSearch(item, term))
          .collect(Collectors.toList());

      // Use the specialized setup method
      TableView<Map<String, String>> mapTableView =
          (TableView<Map<String, String>>) (TableView<?>) tableView;
      mapTableView.getItems().clear();
      mapTableView.getItems().addAll(filteredData);
    } else {
      // Normal handling for other handlers
      Handler<Object> handler = (Handler<Object>) currentHandler;
      List<?> filtered = handler.loadData().stream()
          .filter(item -> handler.matchesSearch(item, term))
          .collect(Collectors.toList());

      setupTable(handler.getColumns(), filtered);
    }
  }

  /**
   * Refreshes the table with the current handler's data.
   *
   * @author mian
   */
  private void refreshTable() {
    setupTable(currentHandler.getColumns(), currentHandler.loadData());
  }

  /**
   * Sets up the table columns and items.
   *
   * @param columns the columns to set up
   * @param items   the items to display in the table
   * @author mian
   */
  private void setupTable(List<Pair<String, String>> columns, List<?> items) {
    tableView.getColumns().clear();
    tableView.getItems().clear();

    columns.forEach(pair -> {
      TableColumn<Object, Object> col = new TableColumn<>(pair.getKey());
      col.setCellValueFactory(new PropertyValueFactory<>(pair.getValue()));
      col.setPrefWidth(tableView.getWidth() / columns.size()); // Set dynamic width
      col.setResizable(true); // Allow resizing
      tableView.getColumns().add(col);
    });

    tableView.getItems().addAll(items);

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
    List.of(RoomTimePlanButton, PresenceListButton, RouteSlipButton, ExportButton, AssignmentButton)
        .forEach(b ->
            b.getStyleClass().remove("button-active")
        );
    button.getStyleClass().add("button-active");
  }

  /**
   * Switches the current import handler and refreshes the table.
   *
   * @param handler      the new import handler
   * @param activeButton the button associated with the new handler
   * @author mian
   */
  private void switchHandler(Handler<?> handler, Button activeButton) {
    currentHandler = handler;
    setActiveButton(activeButton);

    if (handler instanceof RoomPlanHandler) {
      checkAndDisplayTimetableData();
    } else {
      // Use the generic setup for other handlers
      refreshTable();
    }
  }

  /**
   * Checks if timetable data exists and either displays it or shows the generate button
   * accordingly.
   *
   * @author mian
   */
  private void checkAndDisplayTimetableData() {
    if (roomPlanHandler.hasTimetableData()) {
      // Show table with data
      tableView.setVisible(true);
      if (tableContainer.getChildren().contains(generateTimetableButton)) {
        tableContainer.getChildren().remove(generateTimetableButton);
      }

      // Use the specialized setup method for RoomPlanHandler
      ((RoomPlanHandler) currentHandler).setupTableWithMapData(
          (TableView<Map<String, String>>) (TableView<?>) tableView);
    } else {
      // Show generate button
      tableView.setVisible(false);
      if (!tableContainer.getChildren().contains(generateTimetableButton)) {
        tableContainer.getChildren().add(generateTimetableButton);
      }
    }
  }
}
