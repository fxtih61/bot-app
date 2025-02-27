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

  /**
   * Constructs a new AssignmentService with the required dependencies.
   *
   * @author mian
   */
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
   * Executes the assignment process using data from the database.
   *
   * @throws IOException if there is an error reading data
   * @author mian
   */
  public void runAssignment() throws IOException {
    // Load all required data
    List<Choice> choices = choiceService.loadChoices();
    List<Event> events = eventService.loadEvents();
    List<Room> rooms = roomService.loadRooms();
    List<TimeSlot> timeSlots = timeSlotService.loadTimeSlots();

    // Assign students to events based on their choices
    Map<Integer, List<Choice>> assignments = studentAssignmentService.assignStudentsToEvents(
        choices, events);

    // Calculate workshop demand based on student choices
    Map<Integer, Integer> workshopsNeeded = workshopDemandService.calculateWorkshopsNeeded(events,
        choices);

    // Create and save timetable
    timetableService.createAndSaveTimetable(events, rooms, timeSlots, workshopsNeeded);

    // TODO: Generate outputs (Schedule, Attendance list, etc.)
  }
}