package com.openjfx.handlers.Import;

import com.openjfx.models.Event;
import com.openjfx.services.EventService;
import com.openjfx.services.ExcelService;
import com.openjfx.utils.TempFileManager;
import javafx.util.Pair;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Handler for importing Event data from an Excel file.
 *
 * @author mian
 */
public class EventHandler implements Handler<Event> {

  private final EventService eventService;

  /**
   * Constructs an EventImportHandler with the specified ExcelService.
   *
   * @param excelService the Excel service to use for importing data
   * @author mian
   */
  public EventHandler(ExcelService excelService) {
    this.eventService = new EventService(excelService);
  }

  /**
   * Gets the columns to be displayed in the table.
   *
   * @return a list of pairs where each pair contains the column name and the corresponding property
   * name
   * @author mian
   */
  @Override
  public List<Pair<String, String>> getColumns() {
    return List.of(
        new Pair<>("ID", "id"),
        new Pair<>("Company", "company"),
        new Pair<>("Subject", "subject"),
        new Pair<>("Max Participants", "maxParticipants"),
        new Pair<>("Min Participants", "minParticipants"),
        new Pair<>("Earliest Start", "earliestStart")
    );
  }

  /**
   * Loads the event data to be displayed in the table.
   *
   * @return a list of event data items
   * @author mian
   */
  @Override
  public List<Event> loadData() {
    return eventService.loadEvents();
  }

  /**
   * Imports event data from the specified file.
   *
   * @param selectedFile the file to import data from
   * @throws IOException if an I/O error occurs during import
   * @author mian
   */
  @Override
  public void importData(File selectedFile) throws IOException {
    File tempFile = TempFileManager.createTempFile(selectedFile);
    try {
      List<Event> events = eventService.loadFromExcel(tempFile);

      if (events.isEmpty()) {
        throw new IllegalArgumentException("No valid events found in the file");
      }

      // Clear existing events and save new ones
      clearData();
      events.forEach(eventService::saveEvent);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Failed to import events: " + e.getMessage());
    } finally {
      TempFileManager.deleteTempFile(tempFile);
    }
  }

  /**
   * Checks if the given event matches the search term.
   *
   * @param event      the event to check
   * @param searchTerm the search term to match against
   * @return true if the event matches the search term, false otherwise
   * @author mian
   */
  @Override
  public boolean matchesSearch(Event event, String searchTerm) {
    String lowerTerm = searchTerm.toLowerCase();
    return String.valueOf(event.getId()).contains(lowerTerm) ||
        event.getCompany().toLowerCase().contains(lowerTerm) ||
        event.getSubject().toLowerCase().contains(lowerTerm);
  }

  /**
   * Gets the text to be displayed on the import button.
   *
   * @return the import button text
   * @author mian
   */
  @Override
  public String getImportButtonText() {
    return "Import Events";
  }

  /**
   * Clears the existing event data.
   *
   * @author mian
   */
  @Override
  public void clearData() {
    eventService.clearEvents();
  }

  /**
   * Gets the Excel service used for import operations.
   *
   * @return the Excel service
   * @author mian
   */
  @Override
  public ExcelService getExcelService() {
    return eventService.getExcelService();
  }
}