package com.openjfx.services;

import com.openjfx.config.DatabaseConfig;
import com.openjfx.models.Event;
import com.openjfx.models.EventRoomAssignment;
import com.openjfx.models.StudentAssignment;
import com.openjfx.models.WorkshopDemand;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
   * @author mian
   */
  private List<StudentAssignment> unassignedStudents = new ArrayList<>();

  public Map<StudentAssignment, EventRoomAssignment> mapStudentsToTimetable() {
    Map<StudentAssignment, EventRoomAssignment> studentTimetableMap = new HashMap<>();
    unassignedStudents.clear(); // Reset unassigned students list

    try (Connection conn = DatabaseConfig.getConnection()) {
      conn.setAutoCommit(false); // Start transaction

      List<StudentAssignment> studentAssignments = getAllStudentAssignments();
      List<EventRoomAssignment> timetableAssignments = getAllTimetableAssignments();

      // Track room occupancy
      Map<String, Map<String, Integer>> roomOccupancy = new HashMap<>();
      // Track student time slots to prevent double booking
      Map<String, Map<String, StudentAssignment>> studentTimeSlots = new HashMap<>();

      // First clear any existing time slot and room assignments
      // This prevents carrying over old assignments that might cause conflicts
      clearExistingAssignments(conn, studentAssignments);

      // Group students by their unique identifier
      Map<String, List<StudentAssignment>> studentEventMap = new HashMap<>();
      for (StudentAssignment student : studentAssignments) {
        String studentId =
            student.getFirstName() + "_" + student.getLastName() + "_" + student.getClassRef();
        studentEventMap.computeIfAbsent(studentId, k -> new ArrayList<>()).add(student);
        studentTimeSlots.putIfAbsent(studentId, new HashMap<>());
      }

      // Create event assignments map for faster lookup
      Map<Integer, List<EventRoomAssignment>> eventAssignments = new HashMap<>();
      for (EventRoomAssignment assignment : timetableAssignments) {
        int eventId = assignment.getEvent().getId();
        eventAssignments.computeIfAbsent(eventId, k -> new ArrayList<>()).add(assignment);
      }

      // Process student assignments in order of choice number (prioritize students' preferred choices)
      List<StudentAssignment> sortedAssignments = new ArrayList<>(studentAssignments);
      sortedAssignments.sort(Comparator.comparing(assignment -> {
        Integer choiceNo = assignment.getChoiceNo();
        return choiceNo == null ? Integer.MAX_VALUE : choiceNo;
      }));

      // First pass: Assign students based on their choice priority
      for (StudentAssignment student : sortedAssignments) {
        String studentId =
            student.getFirstName() + "_" + student.getLastName() + "_" + student.getClassRef();
        int eventId = student.getEventId();

        List<EventRoomAssignment> possibleAssignments = eventAssignments.getOrDefault(eventId,
            Collections.emptyList());

        // Sort assignments by room capacity (largest rooms first)
        possibleAssignments.sort((a1, a2) ->
            Integer.compare(a2.getRoom().getCapacity(), a1.getRoom().getCapacity()));

        boolean assigned = false;
        for (EventRoomAssignment assignment : possibleAssignments) {
          String timeSlot = assignment.getTimeSlot();
          String roomId = assignment.getRoom().getName();

          // Skip if student already has an assignment in this time slot
          if (studentTimeSlots.get(studentId).containsKey(timeSlot)) {
            continue;
          }

          // Check room capacity
          int currentOccupancy = roomOccupancy
              .computeIfAbsent(roomId, k -> new HashMap<>())
              .getOrDefault(timeSlot, 0);

          if (currentOccupancy < assignment.getRoom().getCapacity()) {
            // Update tracking structures BEFORE database to ensure in-memory state is consistent
            roomOccupancy.get(roomId).put(timeSlot, currentOccupancy + 1);
            studentTimeSlots.get(studentId).put(timeSlot, student);
            studentTimetableMap.put(student, assignment);

            // Assign student to this time slot and room in database
            updateStudentAssignment(conn, student, timeSlot, roomId);

            assigned = true;
            break;
          }
        }

        if (!assigned) {
          // No suitable assignment found, add to unassigned list
          unassignedStudents.add(student);
        }
      }

      // Verify no student is assigned to multiple events at the same time
      boolean noConflicts = verifyNoTimeConflicts(studentTimeSlots);
      if (!noConflicts) {
        System.err.println("ERROR: Time conflicts detected during assignment. Rolling back.");
        conn.rollback();
        return new HashMap<>(); // Return empty map to indicate failure
      }

      // If there are unassigned students, we'll handle them with the conflict resolution service
      if (!unassignedStudents.isEmpty()) {
        System.out.println("Found " + unassignedStudents.size() +
            " unassigned students. These will be handled by conflict resolution.");
      }

      conn.commit();
    } catch (SQLException e) {
      System.err.println("Error updating student assignments: " + e.getMessage());
      e.printStackTrace();
    }

    printUnassignedStudents();

    // Run conflict resolution after initial mapping
    runConflictResolution();

    return studentTimetableMap;
  }

  /**
   * Clears existing time slot and room assignments to prevent conflicts with old data
   *
   * @param conn        Database connection
   * @param assignments List of student assignments to clear
   * @throws SQLException if database error occurs
   *
   * @author mian
   */
  private void clearExistingAssignments(Connection conn, List<StudentAssignment> assignments)
      throws SQLException {
    String sql = "UPDATE student_assignments SET time_slot = NULL, room_id = NULL " +
        "WHERE first_name = ? AND last_name = ? AND event_id = ?";

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      for (StudentAssignment student : assignments) {
        pstmt.setString(1, student.getFirstName());
        pstmt.setString(2, student.getLastName());
        pstmt.setInt(3, student.getEventId());
        pstmt.addBatch();
      }
      pstmt.executeBatch();
    }
  }

  /**
   * Verifies that no student is assigned to multiple events in the same time slot
   *
   * @param studentTimeSlots Map of student IDs to their time slot assignments
   * @return true if no conflicts exist
   *
   * @author mian
   */
  private boolean verifyNoTimeConflicts(
      Map<String, Map<String, StudentAssignment>> studentTimeSlots) {
    boolean noConflicts = true;

    for (Map.Entry<String, Map<String, StudentAssignment>> entry : studentTimeSlots.entrySet()) {
      String studentId = entry.getKey();
      Map<String, StudentAssignment> assignments = entry.getValue();

      // Each time slot should have exactly one assignment
      for (Map.Entry<String, StudentAssignment> timeSlotEntry : assignments.entrySet()) {
        String timeSlot = timeSlotEntry.getKey();
        StudentAssignment assignment = timeSlotEntry.getValue();

        // Check that this student has only one assignment in this time slot
        if (assignment == null) {
          System.err.println(
              "Error: Student " + studentId + " has null assignment in time slot " + timeSlot);
          noConflicts = false;
        }
      }
    }

    return noConflicts;
  }

  /**
   * Runs the conflict resolution service to handle unassigned students.
   *
   * @author mian
   */
  private void runConflictResolution() {
    if (unassignedStudents.isEmpty()) {
      return;
    }

    ConflictResolutionService conflictService = new ConflictResolutionService();
    boolean success = conflictService.resolveConflicts();

    if (success) {
      System.out.println("Conflict resolution completed successfully.");
      // Verify student schedules
      boolean allValid = conflictService.verifyStudentSchedules();
      if (allValid) {
        System.out.println("All student schedules have been validated without conflicts.");
      } else {
        System.out.println("Some student schedules still have conflicts after resolution.");
      }
    } else {
      System.out.println("Conflict resolution was not fully successful.");
    }
  }

  private void updateStudentAssignment(Connection conn, StudentAssignment student,
      String timeSlot, String roomId) throws SQLException {
    String sql = "UPDATE student_assignments SET time_slot = ?, room_id = ? " +
        "WHERE first_name = ? AND last_name = ? AND event_id = ?";

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, timeSlot);
      pstmt.setString(2, roomId);
      pstmt.setString(3, student.getFirstName());
      pstmt.setString(4, student.getLastName());
      pstmt.setInt(5, student.getEventId());
      pstmt.executeUpdate();
    }
  }

  private void printUnassignedStudents() {
    if (unassignedStudents.isEmpty()) {
      System.out.println("\n=== All students were successfully assigned ===");
      return;
    }

    System.out.println("\n=== Unassigned Students Due to Timeslot Conflicts ===");
    System.out.println("Total unassigned: " + unassignedStudents.size());

    // Group by class for better readability
    Map<String, List<StudentAssignment>> byClass = new HashMap<>();
    unassignedStudents.forEach(student ->
        byClass.computeIfAbsent(student.getClassRef(), k -> new ArrayList<>()).add(student)
    );

    for (Map.Entry<String, List<StudentAssignment>> entry : byClass.entrySet()) {
      System.out.println("\nClass: " + entry.getKey());
      for (StudentAssignment student : entry.getValue()) {
        System.out.printf("  %s %s (Event ID: %d)%n",
            student.getFirstName(),
            student.getLastName(),
            student.getEventId());
      }
    }
  }


  /**
   * Resolves conflicts for unassigned students and updates all necessary data.
   *
   * @return true if all conflicts were successfully resolved
   * @author mian
   */
  public boolean resolveUnassignedStudentConflicts() {
    ConflictResolutionService resolutionService = new ConflictResolutionService();
    boolean resolved = resolutionService.resolveConflicts();

    if (resolved) {
      System.out.println("Successfully resolved all student conflicts.");
      // Verify all students have valid schedules
      boolean valid = resolutionService.verifyStudentSchedules();
      if (valid) {
        System.out.println("All student schedules are valid after conflict resolution.");
      } else {
        System.out.println("Some student schedules still have issues after conflict resolution.");
      }
      return true;
    } else {
      System.out.println("Some conflicts could not be resolved. Manual adjustment may be needed.");
      return false;
    }
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

  public boolean generateAndMapStudentTimetables() {
    try {
      Map<StudentAssignment, EventRoomAssignment> mappings = mapStudentsToTimetable();

      // Check for time conflicts after initial mapping
      boolean hasConflicts = checkForTimeConflicts();

      // If there are conflicts or unassigned students, resolve them
      if (hasConflicts || !unassignedStudents.isEmpty()) {
        System.out.println("Found scheduling conflicts or unassigned students. Resolving...");
        boolean resolved = resolveUnassignedStudentConflicts();
        if (!resolved) {
          System.err.println("Failed to resolve all student scheduling conflicts");
          return false;
        }
      }
      return true;
    } catch (Exception e) {
      System.err.println("Error generating student timetable mappings: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Checks for time conflicts where students are assigned to multiple events at the same time
   *
   * @return true if conflicts were found
   * @author mian
   */
  private boolean checkForTimeConflicts() {
    // Get all student assignments
    List<StudentAssignment> allAssignments = getAllStudentAssignments();

    // Group by student
    Map<String, Map<String, List<StudentAssignment>>> studentTimeSlotMap = new HashMap<>();

    for (StudentAssignment assignment : allAssignments) {
      if (assignment.getTimeSlot() == null) {
        continue;
      }

      String studentId = assignment.getFirstName() + "_" + assignment.getLastName() + "_"
          + assignment.getClassRef();
      String timeSlot = assignment.getTimeSlot();

      studentTimeSlotMap
          .computeIfAbsent(studentId, k -> new HashMap<>())
          .computeIfAbsent(timeSlot, k -> new ArrayList<>())
          .add(assignment);
    }

    // Check for conflicts
    boolean hasConflicts = false;
    for (Map.Entry<String, Map<String, List<StudentAssignment>>> studentEntry : studentTimeSlotMap.entrySet()) {
      String studentId = studentEntry.getKey();
      Map<String, List<StudentAssignment>> timeSlotMap = studentEntry.getValue();

      for (Map.Entry<String, List<StudentAssignment>> timeSlotEntry : timeSlotMap.entrySet()) {
        String timeSlot = timeSlotEntry.getKey();
        List<StudentAssignment> assignments = timeSlotEntry.getValue();

        if (assignments.size() > 1) {
          hasConflicts = true;
          System.out.println("Conflict found: Student " + studentId +
              " is assigned to " + assignments.size() + " events at time slot " + timeSlot);

          // Add all but the first assignment to unassigned list
          for (int i = 1; i < assignments.size(); i++) {
            unassignedStudents.add(assignments.get(i));
          }
        }
      }
    }

    return hasConflicts;
  }

  /**
   * Clears a student assignment by setting time_slot and room_id to NULL
   *
   * @author mian
   */
  private void clearAssignment(Connection conn, StudentAssignment assignment) throws SQLException {
    String sql = "UPDATE student_assignments SET time_slot = NULL, room_id = NULL " +
        "WHERE first_name = ? AND last_name = ? AND event_id = ?";

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, assignment.getFirstName());
      pstmt.setString(2, assignment.getLastName());
      pstmt.setInt(3, assignment.getEventId());
      pstmt.executeUpdate();
    }
  }

}