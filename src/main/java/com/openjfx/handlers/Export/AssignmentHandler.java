package com.openjfx.handlers.Export;

import com.openjfx.handlers.Import.Handler;
import com.openjfx.models.Choice;
import com.openjfx.models.StudentAssignment;
import com.openjfx.services.*;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles displaying and managing student assignments to events.
 *
 * @author mian
 */
public class AssignmentHandler implements Handler<StudentAssignment> {

  private final AssignmentService assignmentService;
  private final StudentAssignmentService studentAssignmentService;
  private final ExcelService excelService;
  private final TimetableService timetableService;

  /**
   * Constructs a new AssignmentHandler with the necessary services.
   *
   * @param excelService the Excel service for export operations
   * @author mian
   */
  public AssignmentHandler(ExcelService excelService, TimetableService timetableService) {
    this.excelService = excelService;
    this.timetableService = timetableService;
    this.studentAssignmentService = new StudentAssignmentService();

    // Initialize services needed for Assignment Service
    ChoiceService choiceService = new ChoiceService(excelService);
    EventService eventService = new EventService(excelService);
    RoomService roomService = new RoomService(excelService);
    TimeSlotService timeSlotService = new TimeSlotService();
    WorkshopDemandService workshopDemandService = new WorkshopDemandService();

    this.assignmentService = new AssignmentService(
        choiceService, eventService, roomService, timeSlotService,
        studentAssignmentService, timetableService, workshopDemandService
    );
  }

  /**
   * Checks if there are student assignments in the database.
   *
   * @return true if assignments exist, false otherwise
   * @author mian
   */
  public boolean hasAssignmentData() {
    List<StudentAssignment> assignments = studentAssignmentService.getAllAssignments();
    return !assignments.isEmpty();
  }

  /**
   * Runs the student assignment process.
   *
   * @throws IOException if an error occurs during the process
   * @author mian
   */
  public void generateStudentAssignments() throws IOException {
    assignmentService.loadAllDataAndAssignStudents();
  }

  /**
   * Returns the column definitions for the student assignments table.
   *
   * @return list of column name-property pairs
   * @author mian
   */
  @Override
  public List<Pair<String, String>> getColumns() {
    List<Pair<String, String>> columns = new ArrayList<>();
    columns.add(new Pair<>("Company", "companyName"));
    columns.add(new Pair<>("Subject", "subject"));
    columns.add(new Pair<>("First Name", "firstName"));
    columns.add(new Pair<>("Last Name", "lastName"));
    columns.add(new Pair<>("Class", "classRef"));
    columns.add(new Pair<>("Time Slot", "timeSlot"));
    columns.add(new Pair<>("Room", "roomId"));
    columns.add(new Pair<>("Choice #", "choiceNo")); // Add this line
    return columns;
  }

  /**
   * Loads student assignment data from the database.
   *
   * @return list of student assignments
   * @author mian
   */
  @Override
  public List<StudentAssignment> loadData() {
    return studentAssignmentService.getAllAssignments();
  }

  /**
   * Import functionality is not needed for this handler.
   *
   * @param selectedFile file to import data from (not used)
   * @throws IOException if an I/O error occurs
   * @author mian
   */
  @Override
  public void importData(File selectedFile) throws IOException {
    // Not needed for this handler
  }

  /**
   * Checks if a student assignment matches the provided search term.
   *
   * @param item       the student assignment to check
   * @param searchTerm the term to search for
   * @return true if the assignment matches the search term, false otherwise
   * @author mian
   */
  @Override
  public boolean matchesSearch(StudentAssignment item, String searchTerm) {
    String lowerTerm = searchTerm.toLowerCase();
    return
        (item.getCompanyName() != null && item.getCompanyName().toLowerCase().contains(lowerTerm))
            ||
            (item.getSubject() != null && item.getSubject().toLowerCase().contains(lowerTerm)) ||
            item.getFirstName().toLowerCase().contains(lowerTerm) ||
            item.getLastName().toLowerCase().contains(lowerTerm) ||
            item.getClassRef().toLowerCase().contains(lowerTerm) ||
            (item.getTimeSlot() != null && item.getTimeSlot().toLowerCase().contains(lowerTerm)) ||
            (item.getRoomId() != null && item.getRoomId().toLowerCase().contains(lowerTerm)) ||
            item.getChoiceNo().toString().contains(searchTerm);
  }

  /**
   * Returns the text to display on the button.
   *
   * @return the button text
   * @author mian
   */
  @Override
  public String getImportButtonText() {
    return "Export Student Assignments";
  }

  /**
   * Clear functionality is not needed for this handler.
   *
   * @author mian
   */
  @Override
  public void clearData() {
    // Not needed for this handler
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
   * Exports Event data to an Excel file. This method calls the exportEventData() function from the
   * TimetableService to generate and save the event data in Excel format.
   *
   * @param data       The room data to be exported as a list of maps.
   * @param filterName The addition to the file path
   * @throws IOException If an error occurs during export.
   *
   * @author leon
   */
  public void exportEvents(Map<String, Object> data, String filterName) throws IOException {
    String filePath = timetableService.getFilePathEvent() + "_" + filterName + ".xlsx";
    timetableService.exportEventData(filePath,data);
  }

  /**
   * Exports Event data to an PDF file. This method calls the exportAttendanceListPDF() function from the
   * TimetableService to generate and save the event data in PDF format.
   *
   * @param data       The room data to be exported as a list of maps.
   * @param filterName The addition to the file path
   * @throws IOException If an error occurs during export.
   *
   * @author leon
   */
  public void exportEventsPDF(Map<String, Object> data, String filterName) throws IOException {
    String filePath = timetableService.getFilePathEvent() + "_" + filterName + ".pdf";
    timetableService.exportEventDataPDF(filePath,data);
  }

  /**
   * Exports Event data to an Excel file. This method calls the exportChoiceData() function from the
   * TimetableService to generate and save the choice data in Excel format.
   *
   * @param data       The room data to be exported as a list of maps.
   * @param filterName The addition to the file path
   * @throws IOException If an error occurs during export.
   *
   * @author leon
   */
  public void exportChoices(Map<String, Object> data, String filterName) throws IOException {
    String filePath = timetableService.getFilePathChoices() + "_" + filterName + ".xlsx";
    List<Map<String, Object>> exportData = (List<Map<String, Object>>) data.get("data");
    timetableService.exportChoiceData(filePath, exportData);
  }

  /**
   * Exports Event data to a PDF file. This method calls the exportChoiceDataPDF() function from the
   * TimetableService to generate and save the choice data in PDF format.
   *
   * @param data       The room data to be exported as a list of maps.
   * @param filterName The addition to the file path
   * @throws IOException If an error occurs during export.
   *
   * @author leon
   */
  public void exportChoicesPDF(Map<String, Object> data, String filterName) throws IOException {
    String filePath = timetableService.getFilePathChoices() + "_" + filterName + ".pdf";
    List<Map<String, Object>> exportData = (List<Map<String, Object>>) data.get("data");
    timetableService.exportChoiceDataPDF(filePath, exportData);
  }


}