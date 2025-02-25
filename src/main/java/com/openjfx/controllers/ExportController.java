package com.openjfx.controllers;

import com.openjfx.handlers.Import.Handler;
import com.openjfx.handlers.Export.RoomTimeHandler;
import com.openjfx.services.ExcelService;
import java.util.List;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
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

  /**
   * Constructor initializes the export handlers with the Excel service.
   *
   * @author mian
   */
  public ExportController() {
    ExcelService excelService = new ExcelService();

  }

  /**
   * Initializes the controller, setting up the search field and buttons.
   *
   * @author mian
   */
  @FXML
  public void initialize() {
    setupSearchField();
    // setupButtons();
    // switchHandler(eventHandler, eventsButton);
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
   * @author mian
  private void setupButtons() {
  AssignmentButton.setOnAction(e -> switchHandler(AssignmentHandler, AssignmentButton));
  }
   */


  /**
   * Performs search on the current handler's data based on the search term.
   *
   * @param term the search term
   * @author mian
   */
  @SuppressWarnings("unchecked")
  private void performSearch(String term) {
    if (term.isEmpty()) {
      refreshTable();
      return;
    }

    Handler<Object> handler = (Handler<Object>) currentHandler;
    List<?> filtered = handler.loadData().stream()
        .filter(item -> handler.matchesSearch(item, term))
        .collect(Collectors.toList());

    setupTable(handler.getColumns(), filtered);
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
    refreshTable();
  }
}
