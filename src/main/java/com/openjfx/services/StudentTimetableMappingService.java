package com.openjfx.services;

import com.openjfx.config.DatabaseConfig;
import com.openjfx.models.Event;
import com.openjfx.models.EventRoomAssignment;
import com.openjfx.models.StudentAssignment;
import com.openjfx.models.WorkshopDemand;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
   * Maps students to timetable assignments and updates the database.
   *
   * @return Map of student IDs to their assigned timetable entries
   * @author mian
   */
  public Map<StudentAssignment, EventRoomAssignment> mapStudentsToTimetable() {
    Map<StudentAssignment, EventRoomAssignment> studentTimetableMap = new HashMap<>();

    try (Connection conn = DatabaseConfig.getConnection()) {
      // Prepare update statement
      String updateSQL = "UPDATE student_assignments SET time_slot = ?, room_id = ? WHERE first_name = ? AND last_name = ? AND event_id = ?";
      PreparedStatement pstmt = conn.prepareStatement(updateSQL);

      // Get all required data
      List<StudentAssignment> studentAssignments = getAllStudentAssignments();
      List<WorkshopDemand> workshopDemands = getAllWorkshopDemands();
      List<EventRoomAssignment> timetableAssignments = getAllTimetableAssignments();
      List<Event> events = getAllEvents();

      // Track room occupancy and student time slots (same as before)
      Map<EventRoomAssignment, Integer> roomOccupancy = new HashMap<>();
      timetableAssignments.forEach(assignment -> roomOccupancy.put(assignment, 0));
      Map<String, Set<String>> studentTimeSlots = new HashMap<>();

      // Create event assignments map (same as before)
      Map<String, List<EventRoomAssignment>> eventAssignments = new HashMap<>();
      for (EventRoomAssignment assignment : timetableAssignments) {
        String compositeKey = String.valueOf(assignment.getEvent().getId());
        eventAssignments.computeIfAbsent(compositeKey, k -> new ArrayList<>())
            .add(assignment);
      }

      // Group students by event (same as before)
      Map<String, List<StudentAssignment>> studentsByEvent = new HashMap<>();
      for (StudentAssignment assignment : studentAssignments) {
        String compositeKey = String.valueOf(assignment.getEventId());
        studentsByEvent.computeIfAbsent(compositeKey, k -> new ArrayList<>())
            .add(assignment);
      }

      // Process each event
      for (Event event : events) {
        String compositeKey = String.valueOf(event.getId());
        List<StudentAssignment> studentsForEvent = new ArrayList<>(
            studentsByEvent.getOrDefault(compositeKey, new ArrayList<>()));
        Collections.shuffle(studentsForEvent);
        List<EventRoomAssignment> assignmentsForEvent = eventAssignments.getOrDefault(compositeKey,
            new ArrayList<>());

        if (studentsForEvent.isEmpty() || assignmentsForEvent.isEmpty()) {
          continue;
        }

        // Sort room assignments by capacity
        assignmentsForEvent.sort(
            (a1, a2) -> Integer.compare(a2.getRoom().getCapacity(), a1.getRoom().getCapacity()));

        // Assign students to rooms
        for (StudentAssignment student : studentsForEvent) {
          String studentId = student.getFirstName() + "_" + student.getLastName();
          if (!studentTimeSlots.containsKey(studentId)) {
            studentTimeSlots.put(studentId, new HashSet<>());
          }

          for (EventRoomAssignment roomAssignment : assignmentsForEvent) {
            String timeSlot = roomAssignment.getTimeSlot();
            int currentOccupancy = roomOccupancy.get(roomAssignment);
            int roomCapacity = roomAssignment.getRoom().getCapacity();

            if (!studentTimeSlots.get(studentId).contains(timeSlot)
                && currentOccupancy < roomCapacity) {
              // Update database
              pstmt.setString(1, timeSlot);
              pstmt.setString(2, roomAssignment.getRoom().getName());
              pstmt.setString(3, student.getFirstName());
              pstmt.setString(4, student.getLastName());
              pstmt.setInt(5, student.getEventId());
              pstmt.executeUpdate();

              // Update tracking maps
              studentTimetableMap.put(student, roomAssignment);
              roomOccupancy.put(roomAssignment, currentOccupancy + 1);
              studentTimeSlots.get(studentId).add(timeSlot);
              break;
            }
          }
        }
      }

      pstmt.close();
    } catch (SQLException e) {
      System.err.println("Error updating student assignments: " + e.getMessage());
      e.printStackTrace();
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
          assignment.getEvent().getId() + " - " + assignment.getEvent().getCompany() + " - "
              + assignment.getEvent().getSubject(),
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