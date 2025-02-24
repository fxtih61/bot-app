package com.openjfx.controllers.Import;

import com.openjfx.models.Event;
import com.openjfx.services.EventService;
import com.openjfx.services.ExcelService;
import com.openjfx.utils.TempFileManager;
import javafx.util.Pair;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class EventImportHandler implements ImportHandler<Event> {
  private final EventService eventService;

  public EventImportHandler(ExcelService excelService) {
    this.eventService = new EventService(excelService);
  }

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

  @Override
  public List<Event> loadData() {
    return eventService.loadEvents();
  }

  @Override
  public void importData(File selectedFile) throws IOException {
    File tempFile = TempFileManager.createTempFile(selectedFile);
    try {
      List<Event> events = eventService.loadFromExcel(tempFile.getAbsolutePath());
      clearData();
      events.forEach(eventService::saveEvent);
    } finally {
      TempFileManager.deleteTempFile(tempFile);
    }
  }

  @Override
  public boolean matchesSearch(Event event, String searchTerm) {
    String lowerTerm = searchTerm.toLowerCase();
    return String.valueOf(event.getId()).contains(lowerTerm) ||
        event.getCompany().toLowerCase().contains(lowerTerm) ||
        event.getSubject().toLowerCase().contains(lowerTerm);
  }

  @Override
  public String getImportButtonText() {
    return "Import Events";
  }

  @Override
  public void clearData() {
    eventService.clearEvents();
  }

  @Override
  public ExcelService getExcelService() {
    return eventService.getExcelService();
  }
}