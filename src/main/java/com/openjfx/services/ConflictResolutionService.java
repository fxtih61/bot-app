package com.openjfx.services;

import com.openjfx.config.DatabaseConfig;
import com.openjfx.models.Event;
import com.openjfx.models.EventRoomAssignment;
import com.openjfx.models.StudentAssignment;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service to resolve conflicts in student timetable assignments. Identifies and resolves scheduling
 * conflicts for students who couldn't be assigned.
 *
 * @author mian
 */
public class ConflictResolutionService {

  private final StudentAssignmentService studentAssignmentService;
  private final WorkshopDemandService workshopDemandService;
  private final TimetableService timetableService;
  private final EventService eventService;

  /**
   * Constructs a new ConflictResolutionService with required dependencies.
   *
   * @author mian
   */
  public ConflictResolutionService() {
    this.studentAssignmentService = new StudentAssignmentService();
    this.workshopDemandService = new WorkshopDemandService();
    this.timetableService = new TimetableService();
    ExcelService excelService = new ExcelService();
    this.eventService = new EventService(excelService);
  }

  /**
   * Resolves conflicts for all unassigned students.
   *
   * @return true if all conflicts were successfully resolved
   * @author mian
   */
  public boolean resolveConflicts() {
    List<StudentAssignment> allStudents = studentAssignmentService.getAllAssignments();
    List<EventRoomAssignment> allTimetableAssignments = timetableService.loadTimeTableAssignments();
    List<Event> allEvents = eventService.loadEvents();

    // Get unassigned students (those with null timeSlot or roomId)
    List<StudentAssignment> unassignedStudents = allStudents.stream()
        .filter(s -> s.getTimeSlot() == null || s.getRoomId() == null)
        .collect(Collectors.toList());

    if (unassignedStudents.isEmpty()) {
      System.out.println("No unassigned students found.");
      return true;
    }

    System.out.println("Found " + unassignedStudents.size() + " unassigned students to resolve.");

    // Group students by their unique identifier
    Map<String, List<StudentAssignment>> studentAssignmentsMap = new HashMap<>();
    for (StudentAssignment assignment : allStudents) {
      String studentId = assignment.getFirstName() + "_" + assignment.getLastName() + "_"
          + assignment.getClassRef();
      studentAssignmentsMap.computeIfAbsent(studentId, k -> new ArrayList<>()).add(assignment);
    }

    // Group timetable assignments by event ID
    Map<Integer, List<EventRoomAssignment>> timetableByEvent = new HashMap<>();
    for (EventRoomAssignment assignment : allTimetableAssignments) {
      timetableByEvent.computeIfAbsent(assignment.getEvent().getId(), k -> new ArrayList<>())
          .add(assignment);
    }

    // Track student time slots to avoid conflicts
    Map<String, Set<String>> studentTimeSlots = buildStudentTimeSlotsMap(allStudents);

    // Track room occupancy
    Map<String, Map<String, Integer>> roomOccupancy = buildRoomOccupancyMap(allStudents);

    // Process each unassigned student
    boolean allResolved = true;
    try (Connection conn = DatabaseConfig.getConnection()) {
      conn.setAutoCommit(false);

      for (StudentAssignment unassigned : unassignedStudents) {
        boolean resolved = resolveStudentConflict(
            conn,
            unassigned,
            timetableByEvent,
            studentTimeSlots,
            roomOccupancy,
            studentAssignmentsMap,
            allEvents
        );

        if (!resolved) {
          System.out.println("Could not resolve conflict for student: " +
              unassigned.getFirstName() + " " + unassigned.getLastName() +
              " (Event ID: " + unassigned.getEventId() + ")");
          allResolved = false;
        }
      }

      // Update workshop demands based on new assignments
      if (allResolved) {
        conn.commit();
        updateWorkshopDemand();
      } else {
        conn.rollback();
      }

      return allResolved;
    } catch (SQLException e) {
      System.err.println("Error in conflict resolution: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Builds a map of student time slots to check for conflicts.
   *
   * @param allStudents list of all student assignments
   * @return map of student IDs to their assigned time slots
   * @author mian
   */
  private Map<String, Set<String>> buildStudentTimeSlotsMap(List<StudentAssignment> allStudents) {
    Map<String, Set<String>> studentTimeSlots = new HashMap<>();

    for (StudentAssignment assignment : allStudents) {
      if (assignment.getTimeSlot() != null) {
        String studentId = assignment.getFirstName() + "_" + assignment.getLastName() + "_"
            + assignment.getClassRef();
        studentTimeSlots.computeIfAbsent(studentId, k -> new HashSet<>())
            .add(assignment.getTimeSlot());
      }
    }

    return studentTimeSlots;
  }

  /**
   * Builds a map of room occupancy by time slot.
   *
   * @param allStudents list of all student assignments
   * @return nested map of room IDs and time slots to current occupancy
   * @author mian
   */
  private Map<String, Map<String, Integer>> buildRoomOccupancyMap(
      List<StudentAssignment> allStudents) {
    Map<String, Map<String, Integer>> roomOccupancy = new HashMap<>();

    for (StudentAssignment assignment : allStudents) {
      if (assignment.getRoomId() != null && assignment.getTimeSlot() != null) {
        roomOccupancy.computeIfAbsent(assignment.getRoomId(), k -> new HashMap<>())
            .merge(assignment.getTimeSlot(), 1, Integer::sum);
      }
    }

    return roomOccupancy;
  }

  /**
   * Resolves a conflict for a specific student assignment.
   *
   * @param conn                  database connection
   * @param unassigned            the unassigned student assignment
   * @param timetableByEvent      map of event IDs to their timetable assignments
   * @param studentTimeSlots      map of student IDs to their assigned time slots
   * @param roomOccupancy         map of room occupancy by time slot
   * @param studentAssignmentsMap map of student IDs to their assignments
   * @param allEvents             list of all events
   * @return true if the conflict was resolved
   * @throws SQLException if a database error occurs
   * @author mian
   */
  private boolean resolveStudentConflict(
      Connection conn,
      StudentAssignment unassigned,
      Map<Integer, List<EventRoomAssignment>> timetableByEvent,
      Map<String, Set<String>> studentTimeSlots,
      Map<String, Map<String, Integer>> roomOccupancy,
      Map<String, List<StudentAssignment>> studentAssignmentsMap,
      List<Event> allEvents) throws SQLException {

    String studentId =
        unassigned.getFirstName() + "_" + unassigned.getLastName() + "_" + unassigned.getClassRef();
    Set<String> assignedTimeSlots = studentTimeSlots.getOrDefault(studentId, new HashSet<>());

    // Try to assign to the same event but in a different time slot
    List<EventRoomAssignment> eventAssignments = timetableByEvent.getOrDefault(
        unassigned.getEventId(), new ArrayList<>());

    // Sort room assignments by capacity (largest first)
    eventAssignments.sort(
        (a1, a2) -> Integer.compare(a2.getRoom().getCapacity(), a1.getRoom().getCapacity()));

    // First attempt: Try the same event in a different time slot
    for (EventRoomAssignment assignment : eventAssignments) {
      String timeSlot = assignment.getTimeSlot();
      if (!assignedTimeSlots.contains(timeSlot)) {
        String roomId = assignment.getRoom().getName();
        int currentOccupancy = roomOccupancy.getOrDefault(roomId, new HashMap<>())
            .getOrDefault(timeSlot, 0);
        int roomCapacity = assignment.getRoom().getCapacity();

        if (currentOccupancy < roomCapacity) {
          updateStudentAssignment(conn, unassigned, timeSlot, roomId);

          // Update tracking maps
          assignedTimeSlots.add(timeSlot);
          studentTimeSlots.put(studentId, assignedTimeSlots);
          roomOccupancy.computeIfAbsent(roomId, k -> new HashMap<>())
              .put(timeSlot, currentOccupancy + 1);

          System.out.println("Resolved: " + unassigned.getFirstName() + " " +
              unassigned.getLastName() + " assigned to event " + unassigned.getEventId() +
              " in time slot " + timeSlot + " (room " + roomId + ")");
          return true;
        }
      }
    }

    // Second attempt: Try a different event
    if (studentAssignmentsMap.containsKey(studentId)) {
      return tryReplaceExistingAssignment(
          conn, unassigned, timetableByEvent, studentTimeSlots,
          roomOccupancy, studentAssignmentsMap, allEvents
      );
    }

    return false;
  }

  /**
   * Tries to replace an existing assignment with a new one to resolve conflicts.
   *
   * @param conn                  database connection
   * @param unassigned            the unassigned student assignment
   * @param timetableByEvent      map of event IDs to their timetable assignments
   * @param studentTimeSlots      map of student IDs to their assigned time slots
   * @param roomOccupancy         map of room occupancy by time slot
   * @param studentAssignmentsMap map of student IDs to their assignments
   * @param allEvents             list of all events
   * @return true if replacement was successful
   * @throws SQLException if a database error occurs
   * @author mian
   */
  private boolean tryReplaceExistingAssignment(
      Connection conn,
      StudentAssignment unassigned,
      Map<Integer, List<EventRoomAssignment>> timetableByEvent,
      Map<String, Set<String>> studentTimeSlots,
      Map<String, Map<String, Integer>> roomOccupancy,
      Map<String, List<StudentAssignment>> studentAssignmentsMap,
      List<Event> allEvents) throws SQLException {

    String studentId =
        unassigned.getFirstName() + "_" + unassigned.getLastName() + "_" + unassigned.getClassRef();
    List<StudentAssignment> existingAssignments = studentAssignmentsMap.get(studentId);

    // Sort by choice number (higher = less preferred, 0 = forced assignment)
    List<StudentAssignment> sortedAssignments = new ArrayList<>(existingAssignments);
    sortedAssignments.sort((a1, a2) -> {
      Integer c1 = a1.getChoiceNo() == null ? 0 : a1.getChoiceNo();
      Integer c2 = a2.getChoiceNo() == null ? 0 : a2.getChoiceNo();
      return c2.compareTo(c1); // Reverse sort: higher choice numbers first (less preferred)
    });

    // Try to assign to a completely new event
    boolean assigned = tryAssignToNewEvent(
        conn, unassigned, timetableByEvent, studentTimeSlots, roomOccupancy, allEvents
    );

    if (assigned) {
      return true;
    }

    // Try to replace one of the existing assignments
    for (StudentAssignment existing : sortedAssignments) {
      if (existing.getTimeSlot() == null) {
        continue; // Skip other unassigned ones
      }

      // We can't replace itself
      if (existing.getEventId() == unassigned.getEventId()) {
        continue;
      }

      // Try to find a new time slot for the original unassigned event
      List<EventRoomAssignment> eventAssignments = timetableByEvent.getOrDefault(
          unassigned.getEventId(), new ArrayList<>());
      for (EventRoomAssignment assignment : eventAssignments) {
        String timeSlot = assignment.getTimeSlot();

        // Skip time slots that are already assigned to this student
        if (studentTimeSlots.getOrDefault(studentId, new HashSet<>()).contains(timeSlot)) {
          continue;
        }

        String roomId = assignment.getRoom().getName();
        int currentOccupancy = roomOccupancy.getOrDefault(roomId, new HashMap<>())
            .getOrDefault(timeSlot, 0);
        int roomCapacity = assignment.getRoom().getCapacity();

        if (currentOccupancy < roomCapacity) {
          // Update the originally unassigned event
          updateStudentAssignment(conn, unassigned, timeSlot, roomId);

          // Update tracking maps
          Set<String> timeSlots = studentTimeSlots.computeIfAbsent(studentId, k -> new HashSet<>());
          timeSlots.add(timeSlot);
          roomOccupancy.computeIfAbsent(roomId, k -> new HashMap<>())
              .put(timeSlot, currentOccupancy + 1);

          System.out.println("Resolved through replacement: " + unassigned.getFirstName() +
              " " + unassigned.getLastName() + " assigned to event " +
              unassigned.getEventId() + " in time slot " + timeSlot +
              " (room " + roomId + ")");
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Tries to assign the student to a new event.
   *
   * @param conn             database connection
   * @param unassigned       the unassigned student assignment
   * @param timetableByEvent map of event IDs to their timetable assignments
   * @param studentTimeSlots map of student IDs to their assigned time slots
   * @param roomOccupancy    map of room occupancy by time slot
   * @param allEvents        list of all events
   * @return true if assignment was successful
   * @throws SQLException if a database error occurs
   * @author mian
   */
  private boolean tryAssignToNewEvent(
      Connection conn,
      StudentAssignment unassigned,
      Map<Integer, List<EventRoomAssignment>> timetableByEvent,
      Map<String, Set<String>> studentTimeSlots,
      Map<String, Map<String, Integer>> roomOccupancy,
      List<Event> allEvents) throws SQLException {

    String studentId =
        unassigned.getFirstName() + "_" + unassigned.getLastName() + "_" + unassigned.getClassRef();
    Set<String> assignedTimeSlots = studentTimeSlots.getOrDefault(studentId, new HashSet<>());

    // Try to find an alternative event
    for (Event event : allEvents) {
      // Skip if it's the same event or already assigned to this student
      if (event.getId() == unassigned.getEventId()) {
        continue;
      }

      List<EventRoomAssignment> eventAssignments = timetableByEvent.getOrDefault(event.getId(),
          new ArrayList<>());
      for (EventRoomAssignment assignment : eventAssignments) {
        String timeSlot = assignment.getTimeSlot();

        // Skip time slots that are already assigned to this student
        if (assignedTimeSlots.contains(timeSlot)) {
          continue;
        }

        String roomId = assignment.getRoom().getName();
        int currentOccupancy = roomOccupancy.getOrDefault(roomId, new HashMap<>())
            .getOrDefault(timeSlot, 0);
        int roomCapacity = assignment.getRoom().getCapacity();

        if (currentOccupancy < roomCapacity) {
          // Create new assignment for the new event
          deleteStudentAssignment(conn, unassigned);
          createNewStudentAssignment(
              conn,
              unassigned.getFirstName(),
              unassigned.getLastName(),
              unassigned.getClassRef(),
              event.getId(),
              timeSlot,
              roomId,
              0 // Forced assignment
          );

          // Update tracking maps
          assignedTimeSlots.add(timeSlot);
          studentTimeSlots.put(studentId, assignedTimeSlots);
          roomOccupancy.computeIfAbsent(roomId, k -> new HashMap<>())
              .put(timeSlot, currentOccupancy + 1);

          System.out.println("Resolved by new event: " + unassigned.getFirstName() +
              " " + unassigned.getLastName() + " assigned to new event " +
              event.getId() + " in time slot " + timeSlot +
              " (room " + roomId + ")");
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Updates a student assignment with new time slot and room.
   *
   * @param conn       database connection
   * @param assignment the student assignment to update
   * @param timeSlot   the new time slot
   * @param roomId     the new room ID
   * @throws SQLException if a database error occurs
   * @author mian
   */
  private void updateStudentAssignment(
      Connection conn,
      StudentAssignment assignment,
      String timeSlot,
      String roomId) throws SQLException {

    String sql = "UPDATE student_assignments SET time_slot = ?, room_id = ? " +
        "WHERE first_name = ? AND last_name = ? AND event_id = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, timeSlot);
      stmt.setString(2, roomId);
      stmt.setString(3, assignment.getFirstName());
      stmt.setString(4, assignment.getLastName());
      stmt.setInt(5, assignment.getEventId());
      stmt.executeUpdate();
    }
  }

  /**
   * Deletes a student assignment from the database.
   *
   * @param conn       database connection
   * @param assignment the student assignment to delete
   * @throws SQLException if a database error occurs
   * @author mian
   */
  private void deleteStudentAssignment(Connection conn, StudentAssignment assignment)
      throws SQLException {
    String sql = "DELETE FROM student_assignments " +
        "WHERE first_name = ? AND last_name = ? AND event_id = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, assignment.getFirstName());
      stmt.setString(2, assignment.getLastName());
      stmt.setInt(3, assignment.getEventId());
      stmt.executeUpdate();
    }
  }

  /**
   * Creates a new student assignment in the database.
   *
   * @param conn      database connection
   * @param firstName student's first name
   * @param lastName  student's last name
   * @param classRef  student's class reference
   * @param eventId   event ID
   * @param timeSlot  time slot
   * @param roomId    room ID
   * @param choiceNo  choice number (0 for forced assignments)
   * @throws SQLException if a database error occurs
   * @author mian
   */
  private void createNewStudentAssignment(
      Connection conn,
      String firstName,
      String lastName,
      String classRef,
      int eventId,
      String timeSlot,
      String roomId,
      int choiceNo) throws SQLException {

    // FIXED: Removed company and subject columns, which don't exist in the table
    String sql = "INSERT INTO student_assignments " +
        "(event_id, first_name, last_name, class_ref, time_slot, room_id, choice_no) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, eventId);
      stmt.setString(2, firstName);
      stmt.setString(3, lastName);
      stmt.setString(4, classRef);
      stmt.setString(5, timeSlot);
      stmt.setString(6, roomId);
      stmt.setInt(7, choiceNo);
      stmt.executeUpdate();
    }
  }

  /**
   * Updates workshop demand based on the current student assignments.
   *
   * @author mian
   */
  private void updateWorkshopDemand() {
    // Load current student assignments
    List<StudentAssignment> allAssignments = studentAssignmentService.getAllAssignments();

    // Calculate demand by event
    Map<Integer, Integer> demand = new HashMap<>();
    for (StudentAssignment assignment : allAssignments) {
      demand.merge(assignment.getEventId(), 1, Integer::sum);
    }

    // Save updated demand to database
    workshopDemandService.saveDemandToDatabase(demand);

    System.out.println("Workshop demand has been updated based on resolved assignments.");
  }

  /**
   * Verify all students have exactly 5 events assigned and no time slot conflicts.
   *
   * @return true if all students have valid schedules
   * @author mian
   */
  public boolean verifyStudentSchedules() {
    List<StudentAssignment> allAssignments = studentAssignmentService.getAllAssignments();

    // Group by student
    Map<String, List<StudentAssignment>> studentAssignmentsMap = new HashMap<>();
    for (StudentAssignment assignment : allAssignments) {
      String studentId = assignment.getFirstName() + "_" + assignment.getLastName() + "_"
          + assignment.getClassRef();
      studentAssignmentsMap.computeIfAbsent(studentId, k -> new ArrayList<>()).add(assignment);
    }

    boolean allValid = true;
    for (Map.Entry<String, List<StudentAssignment>> entry : studentAssignmentsMap.entrySet()) {
      String studentId = entry.getKey();
      List<StudentAssignment> assignments = entry.getValue();

      // Check assignment count
      if (assignments.size() != 5) {
        System.out.println("Student " + studentId + " has " + assignments.size() +
            " assignments instead of 5.");
        allValid = false;
        continue;
      }

      // Check for time slot conflicts
      Set<String> timeSlots = new HashSet<>();
      boolean hasTimeSlotConflict = false;

      for (StudentAssignment assignment : assignments) {
        if (assignment.getTimeSlot() == null) {
          System.out.println("Student " + studentId +
              " has an assignment without a time slot (event " +
              assignment.getEventId() + ")");
          allValid = false;
          hasTimeSlotConflict = true;
          break;
        }

        if (timeSlots.contains(assignment.getTimeSlot())) {
          System.out.println("Student " + studentId +
              " has a time slot conflict at " +
              assignment.getTimeSlot());
          allValid = false;
          hasTimeSlotConflict = true;
          break;
        }

        timeSlots.add(assignment.getTimeSlot());
      }

      if (!hasTimeSlotConflict) {
        // Check for room assignments
        for (StudentAssignment assignment : assignments) {
          if (assignment.getRoomId() == null) {
            System.out.println("Student " + studentId +
                " has an assignment without a room (event " +
                assignment.getEventId() + ")");
            allValid = false;
            break;
          }
        }
      }
    }

    if (allValid) {
      System.out.println("All student schedules are valid: each student has exactly 5 events " +
          "with no time slot conflicts.");
    }

    return allValid;
  }
}