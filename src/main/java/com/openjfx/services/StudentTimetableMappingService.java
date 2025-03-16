package com.openjfx.services;

import com.openjfx.models.Event;
import com.openjfx.models.EventRoomAssignment;
import com.openjfx.models.StudentAssignment;
import com.openjfx.models.WorkshopDemand;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service class that consolidates data from student assignments, workshop demand, and timetable
 * assignments for mapping purposes.
 *
 * @author mian
 */
public class StudentTimetableMappingService {

  private final StudentAssignmentService studentAssignmentService;
  private final WorkshopDemandService workshopDemandService;
  private final TimetableService timetableService;
  private final EventService eventService;


  /**
   * Constructs a new StudentTimetableMappingService with required dependencies.
   *
   * @author mian
   */
  public StudentTimetableMappingService() {
    this.studentAssignmentService = new StudentAssignmentService();
    this.workshopDemandService = new WorkshopDemandService();
    this.timetableService = new TimetableService();
    ExcelService excelService = new ExcelService();
    this.eventService = new EventService(excelService);
  }

  /**
   * Retrieves all student assignments from the database.
   *
   * @return List of student assignments
   * @author mian
   */
  public List<StudentAssignment> getAllStudentAssignments() {
    return studentAssignmentService.getAllAssignments();
  }

  /**
   * Retrieves all workshop demand data from the database.
   *
   * @return List of workshop demands
   * @author mian
   */
  public List<WorkshopDemand> getAllWorkshopDemands() {
    return workshopDemandService.getAllWorkshopDemands();
  }

  /**
   * Retrieves all timetable assignments from the database.
   *
   * @return List of event-room assignments
   * @author mian
   */
  public List<EventRoomAssignment> getAllTimetableAssignments() {
    return timetableService.loadTimeTableAssignments();
  }

  /**
   * Retrieve all the events from the database.
   *
   * @return List of events
   */
  public List<Event> getAllEvents() {
    return eventService.loadEvents();
  }

  /**
   * Maps students to timetable assignments considering event capacity, uniqueness constraints, and
   * time slot conflicts. Prints the resulting assignments without saving them.
   *
   * @author mian
   */
  public void printAllData() {
    List<StudentAssignment> studentAssignments = getAllStudentAssignments();
    List<EventRoomAssignment> timetableAssignments = getAllTimetableAssignments();
    List<Event> events = getAllEvents();

    // Create a map of event IDs to their max participants
    Map<Integer, Integer> eventCapacities = new HashMap<>();
    Map<Integer, Event> eventMap = new HashMap<>();
    for (Event event : events) {
      eventCapacities.put(event.getId(), event.getMaxParticipants());
      eventMap.put(event.getId(), event);
    }

    // Track current participant count for each timetable assignment
    Map<EventRoomAssignment, Integer> assignmentCounts = new HashMap<>();
    timetableAssignments.forEach(ta -> assignmentCounts.put(ta, 0));

    // Track which companies/subjects each student has been assigned to
    Map<String, Set<String>> studentAssignedEvents = new HashMap<>();

    // Track which time slots each student is assigned to
    Map<String, Set<String>> studentAssignedTimeSlots = new HashMap<>();

    // Process each student assignment
    for (StudentAssignment sa : studentAssignments) {
      String studentKey = sa.getFirstName() + "_" + sa.getLastName() + "_" + sa.getClassRef();
      studentAssignedEvents.putIfAbsent(studentKey, new HashSet<>());
      studentAssignedTimeSlots.putIfAbsent(studentKey, new HashSet<>());

      // Group timetable assignments by event ID for better distribution
      Map<Integer, List<EventRoomAssignment>> assignmentsByEvent = new HashMap<>();
      for (EventRoomAssignment ta : timetableAssignments) {
        int eventId = ta.getEvent().getId();
        assignmentsByEvent.computeIfAbsent(eventId, k -> new java.util.ArrayList<>()).add(ta);
      }

      // Check if there's a matching event for this student
      if (assignmentsByEvent.containsKey(sa.getEventId())) {
        Event event = eventMap.get(sa.getEventId());
        String eventKey = event.getCompany() + "_" + event.getSubject();

        // Check if student already assigned to this company/subject
        if (!studentAssignedEvents.get(studentKey).contains(eventKey)) {
          // Find the least filled time slot for this event that doesn't conflict
          EventRoomAssignment bestAssignment = null;
          int minCount = Integer.MAX_VALUE;

          for (EventRoomAssignment ta : assignmentsByEvent.get(sa.getEventId())) {
            int currentCount = assignmentCounts.get(ta);
            String timeSlot = ta.getTimeSlot();

            // Check if student is already assigned to this time slot
            if (!studentAssignedTimeSlots.get(studentKey).contains(timeSlot) &&
                currentCount < minCount &&
                currentCount < eventCapacities.get(event.getId())) {
              minCount = currentCount;
              bestAssignment = ta;
            }
          }

          // If we found a suitable time slot
          if (bestAssignment != null) {
            // Print the assignment
            System.out.printf("Student: %s %s (%s) -> Event: %s - %s, Room: %s, Time: %s%n",
                sa.getFirstName(),
                sa.getLastName(),
                sa.getClassRef(),
                event.getCompany(),
                event.getSubject(),
                bestAssignment.getRoom().getName(),
                bestAssignment.getTimeSlot()
            );

            // Update tracking
            studentAssignedEvents.get(studentKey).add(eventKey);
            // Track that this student is now assigned to this time slot
            studentAssignedTimeSlots.get(studentKey).add(bestAssignment.getTimeSlot());
            assignmentCounts.put(bestAssignment, assignmentCounts.get(bestAssignment) + 1);
          }
        }
      }
    }

    // Print summary statistics
    System.out.println("\nAssignment Summary:");
    for (EventRoomAssignment ta : timetableAssignments) {
      Event event = ta.getEvent();
      System.out.printf("%s - %s (Room: %s, Time: %s): %d/%d students assigned%n",
          event.getCompany(),
          event.getSubject(),
          ta.getRoom().getName(),
          ta.getTimeSlot(),
          assignmentCounts.get(ta),
          eventCapacities.get(event.getId())
      );
    }
  }

  //TODO: See why students are not being assigned to events
}