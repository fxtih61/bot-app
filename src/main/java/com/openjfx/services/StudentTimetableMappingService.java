package com.openjfx.services;

import com.openjfx.models.Event;
import com.openjfx.models.EventRoomAssignment;
import com.openjfx.models.StudentAssignment;
import com.openjfx.models.WorkshopDemand;
import java.util.Collections;
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
   * @author mian
   */
  public List<Event> getAllEvents() {
    return eventService.loadEvents();
  }

  /**
   * Maps students to timetable assignments while respecting room capacity and preventing time
   * conflicts.
   *
   * @return Map of student IDs to their assigned timetable entries
   * @author mian
   */
  public Map<StudentAssignment, EventRoomAssignment> mapStudentsToTimetable() {
    // Get all required data
    List<StudentAssignment> studentAssignments = getAllStudentAssignments();
    List<WorkshopDemand> workshopDemands = getAllWorkshopDemands();
    List<EventRoomAssignment> timetableAssignments = getAllTimetableAssignments();
    List<Event> events = getAllEvents();

    // Result map to store student assignments to timetable slots
    Map<StudentAssignment, EventRoomAssignment> studentTimetableMap = new HashMap<>();

    // Track room occupancy for each event-room assignment
    Map<EventRoomAssignment, Integer> roomOccupancy = new HashMap<>();
    timetableAssignments.forEach(assignment -> roomOccupancy.put(assignment, 0));

    // Track which time slots are assigned to each student to avoid conflicts
    Map<String, Set<String>> studentTimeSlots = new HashMap<>();

    // Create a composite key of eventId, company, and subject for more accurate matching
    Map<String, List<EventRoomAssignment>> eventAssignments = new HashMap<>();
    for (EventRoomAssignment assignment : timetableAssignments) {
      int eventId = assignment.getEvent().getId();
      // Use only the eventId for more reliable matching
      String compositeKey = String.valueOf(eventId);
      eventAssignments.computeIfAbsent(compositeKey, k -> new java.util.ArrayList<>())
          .add(assignment);
    }

    // When grouping student assignments
    Map<String, List<StudentAssignment>> studentsByEvent = new HashMap<>();
    for (StudentAssignment assignment : studentAssignments) {
      int eventId = assignment.getEventId();
      // Use only the eventId for more reliable matching
      String compositeKey = String.valueOf(eventId);
      studentsByEvent.computeIfAbsent(compositeKey, k -> new java.util.ArrayList<>())
          .add(assignment);
    }

    System.out.println("Available student event keys: " + studentsByEvent.keySet());
    System.out.println("Available assignment event keys: " + eventAssignments.keySet());

    // Process each event with company and subject combinations
    for (Event event : events) {
      int eventId = event.getId();
      String company = event.getCompany();
      String subject = event.getSubject();
      String compositeKey = String.valueOf(eventId);

      List<StudentAssignment> studentsForEvent = new java.util.ArrayList<>(
          studentsByEvent.getOrDefault(compositeKey, new java.util.ArrayList<>()));
      Collections.shuffle(studentsForEvent);
      List<EventRoomAssignment> assignmentsForEvent = eventAssignments.getOrDefault(compositeKey,
          new java.util.ArrayList<>());

      if (studentsForEvent.isEmpty() || assignmentsForEvent.isEmpty()) {
        continue;
      }

      // Sort room assignments by capacity (largest first) to optimize space usage
      assignmentsForEvent.sort(
          (a1, a2) -> Integer.compare(a2.getRoom().getCapacity(), a1.getRoom().getCapacity()));

      // Assign students to rooms
      for (StudentAssignment student : studentsForEvent) {
        String studentId = student.getFirstName() + "_" + student.getLastName();

        // Initialize student time slots set if not exists
        if (!studentTimeSlots.containsKey(studentId)) {
          studentTimeSlots.put(studentId, new HashSet<>());
        }

        //TODO: Fix the warning message

        boolean assigned = false;
        StringBuilder conflictInfo = new StringBuilder();

        // Try to assign student to a room
        for (EventRoomAssignment roomAssignment : assignmentsForEvent) {
          String timeSlot = roomAssignment.getTimeSlot();
          int currentOccupancy = roomOccupancy.get(roomAssignment);
          int roomCapacity = roomAssignment.getRoom().getCapacity();

          // Check if student already has an assignment at this time slot
          if (studentTimeSlots.get(studentId).contains(timeSlot)) {
            conflictInfo.append("  Time slot ")
                .append(timeSlot)
                .append(" - Student already has another assignment\n");
            continue; // Skip this time slot, student already has an assignment
          }

          // Check if room has capacity
          if (currentOccupancy < roomCapacity) {
            // Assign student to this room
            studentTimetableMap.put(student, roomAssignment);
            roomOccupancy.put(roomAssignment, currentOccupancy + 1);
            studentTimeSlots.get(studentId).add(timeSlot);
            assigned = true;
            break;
          } else {
            conflictInfo.append("  Time slot ")
                .append(timeSlot)
                .append(" - Room is at capacity\n");
          }
        }

        if (!assigned) {
          // Could not assign student due to conflicts
          System.out.println("WARNING: Could not assign student " + student.getFirstName() + " "
              + student.getLastName() + " to event " + eventId + " due to:");
          System.out.println(conflictInfo);

          // Print student's current assignments
          System.out.println("  Student's current assignments: " +
              String.join(", ", studentTimeSlots.get(studentId)));
        }
      }
    }

    return studentTimetableMap;
  }

  /**
   * Prints the mapping of students to timetable assignments.
   *
   * @author mian
   */
  public void printStudentTimetableMapping() {
    Map<StudentAssignment, EventRoomAssignment> mapping = mapStudentsToTimetable();

    System.out.println("\n=== Student Timetable Mapping ===");
    System.out.println("Total assignments: " + mapping.size());

    // Group by time slot for better readability
    Map<String, List<Map.Entry<StudentAssignment, EventRoomAssignment>>> byTimeSlot = new HashMap<>();

    for (Map.Entry<StudentAssignment, EventRoomAssignment> entry : mapping.entrySet()) {
      String timeSlot = entry.getValue().getTimeSlot();
      byTimeSlot.computeIfAbsent(timeSlot, k -> new java.util.ArrayList<>()).add(entry);
    }

    // Print assignments by time slot
    for (String timeSlot : byTimeSlot.keySet().stream().sorted()
        .collect(java.util.stream.Collectors.toList())) {
      System.out.println("\nTime Slot: " + timeSlot);

      for (Map.Entry<StudentAssignment, EventRoomAssignment> entry : byTimeSlot.get(timeSlot)) {
        StudentAssignment student = entry.getKey();
        EventRoomAssignment assignment = entry.getValue();

        System.out.println(String.format("  Student: %s %s, Event: %s (%s), Room: %s",
            student.getFirstName(),
            student.getLastName(),
            assignment.getEvent().getId() + " - " + assignment.getEvent().getCompany() + " - "
                + assignment.getEvent().getSubject(),
            assignment.getCompany(),
            assignment.getRoom().getName()));
      }
    }

    // Print statistics
    System.out.println("\n=== Assignment Statistics ===");
    Map<EventRoomAssignment, Integer> roomOccupancy = new HashMap<>();
    for (EventRoomAssignment assignment : mapping.values()) {
      roomOccupancy.put(assignment, roomOccupancy.getOrDefault(assignment, 0) + 1);
    }

    for (Map.Entry<EventRoomAssignment, Integer> entry : roomOccupancy.entrySet()) {
      EventRoomAssignment assignment = entry.getKey();
      int count = entry.getValue();
      int capacity = assignment.getRoom().getCapacity();

      // In printStudentTimetableMapping method, update the statistics print:
      System.out.printf("Room: %s, Time Slot: %s, Event: %s, Occupancy: %d/%d (%.1f%%)%n",
          assignment.getRoom().getName(),
          assignment.getTimeSlot(), // Add this line
          assignment.getEvent().getId() + " - " + assignment.getEvent().getCompany() + " - " + assignment.getEvent().getSubject(),
          count,
          capacity,
          (count * 100.0 / capacity));
    }
  }

  /**
   * Prints all the data fetched from the services.
   *
   * @author mian
   */
  public void printAllData() {
    List<StudentAssignment> studentAssignments = getAllStudentAssignments();
    List<WorkshopDemand> workshopDemands = getAllWorkshopDemands();
    List<EventRoomAssignment> timetableAssignments = getAllTimetableAssignments();
    List<Event> events = getAllEvents();

    // Print data
    System.out.println("=== Student Assignments ===");
    studentAssignments.forEach(System.out::println);

    System.out.println("\n=== Workshop Demands ===");
    workshopDemands.forEach(System.out::println);

    System.out.println("\n=== Timetable Assignments ===");
    timetableAssignments.forEach(System.out::println);

    System.out.println("\n=== Events ===");
    events.forEach(System.out::println);
  }

}