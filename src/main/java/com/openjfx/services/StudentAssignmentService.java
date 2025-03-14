package com.openjfx.services;

import com.openjfx.config.DatabaseConfig;
import com.openjfx.models.Choice;
import com.openjfx.models.Event;
import com.openjfx.models.StudentAssignment;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for assigning students to events based on their choices and priorities. Each
 * student must be assigned to exactly 5 events.
 *
 * @author mian
 */
public class StudentAssignmentService {

  /**
   * Assigns students to events based on their preferences and the available capacity. Each student
   * must be assigned to exactly 5 events, with first choice always guaranteed.
   *
   * @param choices list of student choices
   * @param events  list of available events
   * @return a map of event IDs to the list of students assigned to each event
   * @author mian
   */
  public Map<Integer, List<Choice>> assignStudentsToEvents(List<Choice> choices,
      List<Event> events) {
    // Track assignments: <EventID, List<StudentChoices>>
    Map<Integer, List<Choice>> assignments = new HashMap<>();

    // Track assigned events for each student: <Student, List<AssignedEvents>>
    Map<Choice, List<Event>> studentAssignments = new HashMap<>();

    // Initialize student assignments tracker
    for (Choice choice : choices) {
      studentAssignments.put(choice, new ArrayList<>());
    }

    // Initialize empty assignment slots for each event
    for (Event event : events) {
      assignments.put(event.getId(), new ArrayList<>());
    }

    // STEP 1: Assign first choices (required)
    assignFirstChoices(assignments, choices, events, studentAssignments);

    // STEP 2: Process remaining choices with multiple passes and priority weighting
    for (int priority = 2; priority <= 6; priority++) {
      // Sort events by available capacity (highest first) for each priority level
      // Sort events by available capacity (highest first) for each priority level
      List<Event> sortedEvents = events.stream()
          .sorted(Comparator.comparingInt((Event e) ->
              (e.getMaxParticipants() - assignments.get(e.getId()).size())).reversed())
          .collect(Collectors.toList());

      // Create a list of students who still need more assignments
      List<Choice> studentsNeedingEvents = choices.stream()
          .filter(s -> studentAssignments.get(s).size() < 5)
          .collect(Collectors.toList());

      // First pass: Try to assign students to their exact choice at this priority level
      for (Choice student : studentsNeedingEvents) {
        // Skip if student already has 5 assignments
        if (studentAssignments.get(student).size() >= 5) {
          continue;
        }

        String choiceStr = getChoiceByPriority(student, priority);
        if (choiceStr.isEmpty()) {
          continue;
        }

        Event event = findEventByChoice(choiceStr, events);
        if (event == null) {
          continue;
        }

        // Check if student is already assigned to this event
        if (isStudentAlreadyAssignedToEvent(studentAssignments, student, event)) {
          continue;
        }

        // Only assign if there's capacity available
        if (!hasReachedCapacity(assignments, event)) {
          assignments.get(event.getId()).add(student);
          studentAssignments.get(student).add(event);
        }
      }
    }

    // STEP 3: For students who still don't have 5 events, try to assign them to any of their remaining preferences
    // that still has capacity without strict priority ordering
    for (Choice student : choices) {
      if (studentAssignments.get(student).size() >= 5) {
        continue;
      }

      // Make a list of all the student's choices that haven't been assigned yet
      List<Event> remainingChoices = new ArrayList<>();
      for (int priority = 2; priority <= 6; priority++) {
        String choiceStr = getChoiceByPriority(student, priority);
        if (!choiceStr.isEmpty()) {
          Event event = findEventByChoice(choiceStr, events);
          if (event != null && !isStudentAlreadyAssignedToEvent(studentAssignments, student,
              event)) {
            remainingChoices.add(event);
          }
        }
      }

      // Sort remaining choices by available capacity (most available first)
      remainingChoices.sort(Comparator.comparingInt((Event e) ->
          (e.getMaxParticipants() - assignments.get(e.getId()).size())).reversed());

      // Assign student to remaining choices that have capacity
      for (Event event : remainingChoices) {
        if (studentAssignments.get(student).size() >= 5) {
          break;
        }

        // Assign even if at capacity as we're still using their preferences
        assignments.get(event.getId()).add(student);
        studentAssignments.get(student).add(event);
      }
    }

    // STEP 4: Final pass - assign students to any events with capacity remaining
    assignRemainingStudentsToLeastFilledEvents(assignments, choices, events, studentAssignments);

    return assignments;
  }

  /**
   * Assigns students to their first choice events (must be fulfilled).
   *
   * @author mian
   */
  private void assignFirstChoices(Map<Integer, List<Choice>> assignments, List<Choice> choices,
      List<Event> events, Map<Choice, List<Event>> studentAssignments) {
    for (Choice choice : choices) {
      String firstChoiceStr = choice.getChoice1();
      if (firstChoiceStr.isEmpty()) {
        continue;
      }

      Event event = findEventByChoice(firstChoiceStr, events);
      if (event == null) {
        continue;
      }

      // First choice must be fulfilled, even if it exceeds capacity
      assignments.get(event.getId()).add(choice);
      studentAssignments.get(choice).add(event);
    }
  }

  /**
   * Assigns remaining students who don't have 5 events to the least filled events. This is the
   * final fallback to ensure each student gets exactly 5 events.
   *
   * @author mian
   */
  private void assignRemainingStudentsToLeastFilledEvents(Map<Integer, List<Choice>> assignments,
      List<Choice> choices, List<Event> events, Map<Choice, List<Event>> studentAssignments) {

    // Get a list of students who still need more assignments
    List<Choice> studentsNeedingEvents = choices.stream()
        .filter(s -> studentAssignments.get(s).size() < 5)
        .collect(Collectors.toList());

    // Sort students by how many more events they need (those needing more get priority)
    studentsNeedingEvents.sort(Comparator.comparingInt(s ->
        (5 - studentAssignments.get(s).size())));

    for (Choice student : studentsNeedingEvents) {
      // Continue assigning events until student has exactly 5
      while (studentAssignments.get(student).size() < 5) {
        // Find the event with the most remaining capacity that the student isn't already assigned to
        Event leastFilledEvent = findLeastFilledEvent(assignments, events, studentAssignments,
            student);

        if (leastFilledEvent != null) {
          assignments.get(leastFilledEvent.getId()).add(student);
          studentAssignments.get(student).add(leastFilledEvent);
        } else {
          // This should not happen if there are enough events
          System.err.println("WARNING: Unable to assign 5 events to student: " +
              student.getFirstName() + " " + student.getLastName());
          break;
        }
      }
    }
  }

  /**
   * Finds the event with the least number of assigned students that the given student is not
   * already assigned to.
   *
   * @author mian
   */
  private Event findLeastFilledEvent(Map<Integer, List<Choice>> assignments, List<Event> events,
      Map<Choice, List<Event>> studentAssignments, Choice student) {

    Event leastFilledEvent = null;
    int minAssignments = Integer.MAX_VALUE;

    for (Event event : events) {
      // Skip if student is already assigned to this event
      if (isStudentAlreadyAssignedToEvent(studentAssignments, student, event)) {
        continue;
      }

      int currentAssignments = assignments.get(event.getId()).size();
      if (currentAssignments < minAssignments) {
        minAssignments = currentAssignments;
        leastFilledEvent = event;
      }
    }

    return leastFilledEvent;
  }

  /**
   * Checks if a student is already assigned to a specific event.
   *
   * @author mian
   */
  private boolean isStudentAlreadyAssignedToEvent(Map<Choice, List<Event>> studentAssignments,
      Choice student, Event event) {
    List<Event> assignedEvents = studentAssignments.get(student);
    return assignedEvents != null && assignedEvents.stream()
        .anyMatch(e -> e.getId() == event.getId());
  }

  /**
   * Finds an event by the choice string provided.
   *
   * @author mian
   */
  private Event findEventByChoice(String choice, List<Event> events) {
    try {
      int eventId = Integer.parseInt(choice.replaceAll("[^0-9]", ""));
      return events.stream()
          .filter(e -> e.getId() == eventId)
          .findFirst()
          .orElse(null);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Checks if an event has reached its maximum capacity.
   *
   * @author mian
   */
  private boolean hasReachedCapacity(Map<Integer, List<Choice>> assignments, Event event) {
    int current = assignments.get(event.getId()).size();
    return current >= event.getMaxParticipants();
  }

  /**
   * Returns the choice string based on the priority provided.
   *
   * @author mian
   */
  private String getChoiceByPriority(Choice choice, int priority) {
    switch (priority) {
      case 1:
        return choice.getChoice1();
      case 2:
        return choice.getChoice2();
      case 3:
        return choice.getChoice3();
      case 4:
        return choice.getChoice4();
      case 5:
        return choice.getChoice5();
      case 6:
        return choice.getChoice6();
      default:
        return "";
    }
  }

  /**
   * Saves the student assignments to the database.
   *
   * @param assignments Map of event IDs to assigned student choices
   * @return true if saving was successful, false otherwise
   * @author mian
   */
  public boolean saveAssignmentsToDatabase(Map<Integer, List<Choice>> assignments) {
    String sql = "INSERT INTO student_assignments (event_id, first_name, last_name, class_ref, choice_number) VALUES (?, ?, ?, ?, ?)";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      conn.setAutoCommit(false);

      // Clear existing assignments first
      try (Statement clearStmt = conn.createStatement()) {
        clearStmt.execute("DELETE FROM student_assignments");
      }

      for (Map.Entry<Integer, List<Choice>> entry : assignments.entrySet()) {
        int eventId = entry.getKey();
        List<Choice> students = entry.getValue();

        for (Choice student : students) {
          // Determine which choice number this was for the student
          int choiceNumber = getChoiceNumber(student, eventId);

          stmt.setInt(1, eventId);
          stmt.setString(2, student.getFirstName());
          stmt.setString(3, student.getLastName());
          stmt.setString(4, student.getClassRef());
          stmt.setInt(5, choiceNumber);
          stmt.addBatch();
        }
      }

      stmt.executeBatch();
      conn.commit();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Determines which choice number (1-6) this event was for the student. Returns 0 if it wasn't one
   * of their choices.
   *
   * @author mian
   */
  private int getChoiceNumber(Choice student, int eventId) {
    String eventIdStr = String.valueOf(eventId);
    if (eventIdStr.equals(student.getChoice1())) {
      return 1;
    }
    if (eventIdStr.equals(student.getChoice2())) {
      return 2;
    }
    if (eventIdStr.equals(student.getChoice3())) {
      return 3;
    }
    if (eventIdStr.equals(student.getChoice4())) {
      return 4;
    }
    if (eventIdStr.equals(student.getChoice5())) {
      return 5;
    }
    if (eventIdStr.equals(student.getChoice6())) {
      return 6;
    }
    return 0; // Not one of their choices (assigned to meet 5-event requirement)
  }

  /**
   * Retrieves all student assignments from the database.
   *
   * @return list of student assignments
   * @author mian
   */
  public List<StudentAssignment> getAllAssignments() {
    String sql =
        "SELECT sa.event_id, sa.first_name, sa.last_name, sa.class_ref, sa.choice_number, " +
            "e.company, e.subject " +
            "FROM student_assignments sa " +
            "JOIN events e ON sa.event_id = e.id";
    List<StudentAssignment> assignments = new ArrayList<>();

    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        StudentAssignment assignment = new StudentAssignment(
            rs.getInt("event_id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("class_ref"),
            rs.getString("company"),
            rs.getString("subject"),
            rs.getInt("choice_number")
        );
        assignments.add(assignment);
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return assignments;
  }
}