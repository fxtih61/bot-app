package com.openjfx.services;

import com.openjfx.models.Choice;
import com.openjfx.models.Event;
import com.openjfx.models.Room;
import com.openjfx.models.TimeSlot;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates the assignment process by delegating to specialized services.
 *
 * @author mian
 */
public class AssignmentService {

  private final ChoiceService choiceService;
  private final EventService eventService;
  private final RoomService roomService;
  private final TimeSlotService timeSlotService;
  private final StudentAssignmentService studentAssignmentService;
  private final TimetableService timetableService;
  private final WorkshopDemandService workshopDemandService;

  private List<Choice> choices;
  private List<Event> events;
  private List<Room> rooms;
  private List<TimeSlot> timeSlots;
  private Map<Integer, List<Choice>> studentAssignments;
  private Map<Integer, Integer> workshopDemand;

  public AssignmentService(
      ChoiceService choiceService,
      EventService eventService,
      RoomService roomService,
      TimeSlotService timeSlotService,
      StudentAssignmentService studentAssignmentService,
      TimetableService timetableService,
      WorkshopDemandService workshopDemandService) {
    this.choiceService = choiceService;
    this.eventService = eventService;
    this.roomService = roomService;
    this.timeSlotService = timeSlotService;
    this.studentAssignmentService = studentAssignmentService;
    this.timetableService = timetableService;
    this.workshopDemandService = workshopDemandService;
  }

  /**
   * Main orchestration method that executes the assignment process in order.
   *
   * @throws IOException if there is an error reading data
   * @author mian
   */
  public void runAssignment() throws IOException {
    loadAllData();
    assignStudents();
    calculateWorkshopDemand();
    createTimetable();
  }

  /**
   * Loads data and assigns students only (first step of the process).
   *
   * @throws IOException if there is an error reading data
   * @author mian
   */
  public void loadAllDataAndAssignStudents() throws IOException {
    loadAllData();
    assignStudents();
  }

  /**
   * Calculates workshop demand only (second step of the process).
   *
   * @throws IOException if there is an error reading data
   * @author mian
   */
  public void calculateWorkshopDemandOnly() throws IOException {
    if (choices == null || events == null) {
      loadAllData();
    }
    this.studentAssignments = studentAssignmentService.getAssignmentsAsMap();
    calculateWorkshopDemand();
  }

  /**
   * Loads all required data from various services.
   *
   * @throws IOException if there is an error reading data
   * @author mian
   */
  private void loadAllData() throws IOException {
    this.choices = choiceService.loadChoices();
    this.events = eventService.loadEvents();
    this.rooms = roomService.loadRooms();
    this.timeSlots = timeSlotService.loadTimeSlots();
  }

  /**
   * Assigns students to events based on their choices and saves the assignments to the database.
   *
   * @author mian
   */
  private void assignStudents() {
    this.studentAssignments = studentAssignmentService.assignStudentsToEvents(choices, events);
    // Save assignments to the database
    studentAssignmentService.saveAssignmentsToDatabase(studentAssignments);
  }

  /**
   * Calculates workshop demand based on student choices and saves to database.
   *
   * @author mian
   */
  private void calculateWorkshopDemand() {
      this.workshopDemand = workshopDemandService.calculateWorkshopsNeeded(events, studentAssignments);
      workshopDemandService.saveDemandToDatabase(workshopDemand);
  }

  /**
   * Creates and saves the timetable based on calculated data.
   *
   * @author mian
   */
  private void createTimetable() {
    timetableService.createAndSaveTimetable(events, rooms, timeSlots, workshopDemand);
  }

  /**
   * Returns the student assignments.
   *
   * @return a map of student IDs to their assigned choices
   * @author mian
   */
  public Map<Integer, List<Choice>> getStudentAssignments() {
    return studentAssignments;
  }

  /**
   * Returns the workshop demand.
   *
   * @return a map of event IDs to the number of students assigned to each event
   * @author mian
   */
  public Map<Integer, Integer> loadWorkshopDemand() {
    this.workshopDemand = workshopDemandService.loadDemandFromDatabase();
    return this.workshopDemand;
  }

  /**
   * Creates and saves the timetable based on provided workshop demand.
   *
   * @param workshopDemand map of event IDs to the number of students assigned to each event
   * @throws IOException if there is an error reading data
   * @author mian
   */
  public void createAndSaveTimetable(Map<Integer, Integer> workshopDemand) throws IOException {
    if (events == null || rooms == null || timeSlots == null) {
      loadAllData(); // Make sure data is loaded if not already
    }
    timetableService.createAndSaveTimetable(events, rooms, timeSlots, workshopDemand);
  }
}