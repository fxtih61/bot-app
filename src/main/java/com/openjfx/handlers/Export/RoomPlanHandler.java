package com.openjfx.handlers.Export;

import com.openjfx.handlers.Import.Handler;
import com.openjfx.services.*;
import com.openjfx.models.EventRoomAssignment;
import javafx.util.Pair;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class RoomPlanHandler implements Handler<EventRoomAssignment> {

  private final AssignmentService assignmentService;
  private final TimetableService timetableService;
  private final ExcelService excelService;

  public RoomPlanHandler(TimetableService timetableService, ExcelService excelService) {
    this.timetableService = timetableService;
    this.excelService = excelService;
    ChoiceService choiceService = new ChoiceService(excelService);
    EventService eventService = new EventService(excelService);
    RoomService roomService = new RoomService(excelService);
    TimeSlotService timeSlotService = new TimeSlotService();
    StudentAssignmentService studentAssignmentService = new StudentAssignmentService();
    WorkshopDemandService workshopDemandService = new WorkshopDemandService();

    this.assignmentService = new AssignmentService(
        choiceService, eventService, roomService, timeSlotService,
        studentAssignmentService, timetableService, workshopDemandService
    );
  }

  @Override
  public List<Pair<String, String>> getColumns() {
    return List.of(
        new Pair<>("Room", "room"),
        new Pair<>("Time Slot", "timeSlot"),
        new Pair<>("Event", "event"),
        new Pair<>("Company", "company")
    );
  }

  @Override
  public List<EventRoomAssignment> loadData() {
    return timetableService.loadTimeTableAssignments();
  }

  @Override
  public void importData(File selectedFile) throws IOException {
    // Not needed for export
  }

  @Override
  public boolean matchesSearch(EventRoomAssignment item, String searchTerm) {
    String lowerTerm = searchTerm.toLowerCase();
    return item.roomProperty().get().getName().toLowerCase().contains(lowerTerm) ||
        item.eventProperty().get().getCompany().toLowerCase().contains(lowerTerm);
  }

  @Override
  public String getImportButtonText() {
    return "Export Room Plan";
  }

  @Override
  public void clearData() {
    // Not needed for export
  }

  @Override
  public ExcelService getExcelService() {
    return this.excelService;
  }
}