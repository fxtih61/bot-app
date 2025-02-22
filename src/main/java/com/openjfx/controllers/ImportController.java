package com.openjfx.controllers;

import com.openjfx.models.Event;
import com.openjfx.services.*;
import com.openjfx.utils.TempFileManager;
import java.io.File;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.List;

/**
 * Controller class for handling import operations and dynamic UI updates.
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

  private EventService eventService;
  private ChoiceService choiceService;
  private RoomService roomService;

  /**
   * Initializes the controller, setting up services and event handlers.
   */
  public void initialize() {
    ExcelService excelService = new ExcelService(); // Shared instance
    eventService = new EventService(excelService);
    choiceService = new ChoiceService(excelService);
    roomService = new RoomService(excelService);

    // Use method references for event handlers
    eventsButton.setOnAction(this::showEventsTable);
    choicesButton.setOnAction(this::showChoicesTable);
    roomsButton.setOnAction(this::showRoomsTable);

    showEventsTable(null);
  }

  /**
   * Displays the events table and sets up the import button for events.
   *
   * @param event the action event
   */
  private void showEventsTable(ActionEvent event) {
    setupTable(
        List.of(
            new Pair<>("ID", "id"),
            new Pair<>("Company", "company"),
            new Pair<>("Subject", "subject"),
            new Pair<>("Max Participants", "maxParticipants"),
            new Pair<>("Min Participants", "minParticipants"),
            new Pair<>("Earliest Start", "earliestStart")
        ),
        eventService.loadEvents()
    );
    setupImportButton("Import Events", e -> {
      try {
        importEvents(e);
      } catch (IOException ex) {
        System.err.println("Error importing events: " + ex.getMessage());
      }
    });
  }

  /**
   * Displays the choices table and sets up the import button for choices.
   *
   * @param event the action event
   */
  private void showChoicesTable(ActionEvent event) {
    setupTable(
        List.of(
            new Pair<>("Class", "classRef"),
            new Pair<>("First Name", "firstName"),
            new Pair<>("Last Name", "lastName"),
            new Pair<>("Choice 1", "choice1"),
            new Pair<>("Choice 2", "choice2"),
            new Pair<>("Choice 3", "choice3"),
            new Pair<>("Choice 4", "choice4"),
            new Pair<>("Choice 5", "choice5"),
            new Pair<>("Choice 6", "choice6")
        ),
        choiceService.loadChoices()
    );
    setupImportButton("Import Choices", this::importChoices);
  }

  /**
   * Displays the rooms table and sets up the import button for rooms.
   *
   * @param event the action event
   */
  private void showRoomsTable(ActionEvent event) {
    setupTable(
        List.of(
            new Pair<>("Room", "name"),
            new Pair<>("Capacity", "capacity")
        ),
        roomService.loadRooms()
    );
    setupImportButton("Import Rooms", this::importRooms);
  }

  /**
   * Sets up the table view with the specified columns and items.
   *
   * @param columns the list of column name and property pairs
   * @param items   the list of items to display in the table
   */
  private void setupTable(List<Pair<String, String>> columns, List<?> items) {
    tableView.getColumns().clear();
    tableView.getItems().clear();

    for (Pair<String, String> column : columns) {
      TableColumn<Object, Object> col = new TableColumn<>(column.getKey());
      col.setCellValueFactory(new PropertyValueFactory<>(column.getValue()));
      tableView.getColumns().add(col);
    }

    tableView.getItems().addAll(items);
  }

  /**
   * Sets up the import button with the specified text and action handler.
   *
   * @param text    the button text
   * @param handler the action handler for the button
   */
  private void setupImportButton(String text, EventHandler<ActionEvent> handler) {
    importButton.setText(text);
    importButton.setOnAction(handler);
  }

  /**
   * Handles the import of events.
   *
   * @param event the action event
   */
  private void importEvents(ActionEvent event) throws IOException {
    FileSelecterService fileSelecter = new FileSelecterService();
    Stage stage = (Stage) importButton.getScene().getWindow();
    File selectedFile = fileSelecter.selectFile(stage);

    if (selectedFile != null) {
      File tempFile = null;
      try {
        tempFile = TempFileManager.createTempFile(selectedFile);
        List<Event> events = eventService.loadFromExcel(tempFile.getAbsolutePath());

        // Clear existing events before importing new ones
        eventService.clearEvents();

        // Save new events
        for (Event e : events) {
          eventService.saveEvent(e);
        }

        showEventsTable(null);
      } finally {
        TempFileManager.deleteTempFile(tempFile);
      }
    }
  }

  /**
   * Handles the import of choices.
   *
   * @param event the action event
   */
  private void importChoices(ActionEvent event) {
    //TODO: Implement choices import logic
  }

  /**
   * Handles the import of rooms.
   *
   * @param event the action event
   */
  private void importRooms(ActionEvent event) {
    //TODO: Implement rooms import logic
  }
}