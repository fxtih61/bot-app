package com.openjfx.controllers.Import;

import com.openjfx.handlers.ImportHandler;
import com.openjfx.services.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javafx.util.Pair;

/**
 * Controller class for handling import operations in the application.
 */
public class ImportController {

  @FXML
  private TableView<Object> tableView;
  @FXML
  private Button eventsButton;
  @FXML
  private Button choicesButton;
  @FXML
  private Button roomsButton;
  @FXML
  private Button importButton;
  @FXML
  private TextField searchField;

  private ImportHandler<?> currentHandler;
  private final EventImportHandler eventHandler;
  private final ChoiceImportHandler choiceHandler;
  private final RoomImportHandler roomHandler;

  /**
   * Constructor initializes the import handlers with the Excel service.
   */
  public ImportController() {
    ExcelService excelService = new ExcelService();
    this.eventHandler = new EventImportHandler(excelService);
    this.choiceHandler = new ChoiceImportHandler(excelService);
    this.roomHandler = new RoomImportHandler(excelService);
  }

  /**
   * Initializes the controller, setting up the search field and buttons.
   */
  @FXML
  public void initialize() {
    setupSearchField();
    setupButtons();
    switchHandler(eventHandler, eventsButton);
  }

  /**
   * Sets up the search field to perform search on text change.
   */
  private void setupSearchField() {
    searchField.textProperty().addListener((obs, oldVal, newVal) ->
        performSearch(newVal.trim())
    );
  }

  /**
   * Sets up the buttons to switch handlers and handle import actions.
   */
  private void setupButtons() {
    eventsButton.setOnAction(e -> switchHandler(eventHandler, eventsButton));
    choicesButton.setOnAction(e -> switchHandler(choiceHandler, choicesButton));
    roomsButton.setOnAction(e -> switchHandler(roomHandler, roomsButton));
    importButton.setOnAction(this::handleImport);
  }

  /**
   * Switches the current import handler and refreshes the table.
   *
   * @param handler      the new import handler
   * @param activeButton the button associated with the new handler
   */
  private void switchHandler(ImportHandler<?> handler, Button activeButton) {
    currentHandler = handler;
    setActiveButton(activeButton);
    refreshTable();
  }

  /**
   * Sets the active button style.
   *
   * @param button the button to set as active
   */
  private void setActiveButton(Button button) {
    List.of(eventsButton, choicesButton, roomsButton).forEach(b ->
        b.getStyleClass().remove("button-active")
    );
    button.getStyleClass().add("button-active");
  }

  /**
   * Performs search on the current handler's data based on the search term.
   *
   * @param term the search term
   */
  @SuppressWarnings("unchecked")
  private void performSearch(String term) {
    if (term.isEmpty()) {
      refreshTable();
      return;
    }

    ImportHandler<Object> handler = (ImportHandler<Object>) currentHandler;
    List<?> filtered = handler.loadData().stream()
        .filter(item -> handler.matchesSearch(item, term))
        .collect(Collectors.toList());

    setupTable(handler.getColumns(), filtered);
  }

  /**
   * Refreshes the table with the current handler's data.
   */
  private void refreshTable() {
    setupTable(currentHandler.getColumns(), currentHandler.loadData());
  }

  /**
   * Sets up the table columns and items.
   *
   * @param columns the columns to set up
   * @param items   the items to display in the table
   */
  private void setupTable(List<Pair<String, String>> columns, List<?> items) {
    tableView.getColumns().clear();
    tableView.getItems().clear();

    columns.forEach(pair -> {
      TableColumn<Object, Object> col = new TableColumn<>(pair.getKey());
      col.setCellValueFactory(new PropertyValueFactory<>(pair.getValue()));
      tableView.getColumns().add(col);
    });

    tableView.getItems().addAll(items);
  }

  /**
   * Handles the import action, importing data from a selected file.
   *
   * @param event the action event
   */
  private void handleImport(ActionEvent event) {
    File file = new FileSelecterService().selectFile((Stage) importButton.getScene().getWindow());
    if (file == null) {
      return;
    }

    try {
      currentHandler.importData(file);
      refreshTable();
    } catch (IOException ex) {
      showError("Import Error", "Failed to import data: " + ex.getMessage());
    }
  }

  /**
   * Shows an error alert with the specified header and content.
   *
   * @param header  the header text
   * @param content the content text
   */
  private void showError(String header, String content) {
    Alert alert = new Alert(Alert.AlertType.ERROR, content, ButtonType.OK);
    alert.setHeaderText(header);
    alert.showAndWait();
  }
}