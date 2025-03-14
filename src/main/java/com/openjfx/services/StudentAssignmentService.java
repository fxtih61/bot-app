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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for assigning students to events based on their choices and priorities.
 *
 * @author mian
 */
public class StudentAssignmentService {

  /**
   * Assigns students to events based on their choices and the available capacity.
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

    // Initialize empty assignment slots for each event
    initializeEventSlots(assignments, events);

    // Assign students to their first choice events where possible
    assignFirstChoices(assignments, choices, events);

    // Assign remaining students to their subsequent choices (2-6) if space is available
    assignRemainingChoices(assignments, choices, events);

    return assignments;
  }

  /**
   * Initializes empty assignment slots for each event.
   *
   * @author mian
   */
  private void initializeEventSlots(Map<Integer, List<Choice>> assignments, List<Event> events) {
    for (Event event : events) {
      assignments.put(event.getId(), new ArrayList<>());
    }
  }

  /**
   * Assigns students to their first choice events where possible.
   *
   * @author mian
   */
  private void assignFirstChoices(Map<Integer, List<Choice>> assignments, List<Choice> choices,
      List<Event> events) {
    for (Choice choice : choices) {
      String firstChoice = choice.getChoice1();
      if (firstChoice.isEmpty()) {
        continue;
      }

      Event event = findEventByChoice(firstChoice, events);
      if (event == null) {
        continue;
      }

      if (!hasReachedCapacity(assignments, event)) {
        assignments.get(event.getId()).add(choice);
      }
    }
  }

  /**
   * Assigns remaining students to their subsequent choices (2-6) if space is available.
   *
   * @author mian
   */
  private void assignRemainingChoices(Map<Integer, List<Choice>> assignments, List<Choice> choices,
      List<Event> events) {
    for (int priority = 2; priority <= 6; priority++) {
      for (Choice choice : choices) {
        String choiceStr = getChoiceByPriority(choice, priority);
        if (choiceStr.isEmpty()) {
          continue;
        }

        Event event = findEventByChoice(choiceStr, events);
        if (event == null) {
          continue;
        }

        if (!hasReachedCapacity(assignments, event) && !isAlreadyAssigned(assignments, choice,
            event)) {
          assignments.get(event.getId()).add(choice);
        }
      }
    }
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
   * Checks if a student is already assigned to an event.
   *
   * @author mian
   */
  private boolean isAlreadyAssigned(Map<Integer, List<Choice>> assignments, Choice student,
      Event event) {
    return assignments.get(event.getId()).contains(student);
  }

  /**
   * Returns the choice string based on the priority provided.
   *
   * @author mian
   */
  private String getChoiceByPriority(Choice choice, int priority) {
    switch (priority) {
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
    String sql = "INSERT INTO student_assignments (event_id, first_name, last_name, class_ref) VALUES (?, ?, ?, ?)";

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
          stmt.setInt(1, eventId);
          stmt.setString(2, student.getFirstName());
          stmt.setString(3, student.getLastName());
          stmt.setString(4, student.getClassRef());
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
   * Retrieves all student assignments from the database.
   *
   * @return list of student assignments
   * @author mian
   */
  public List<StudentAssignment> getAllAssignments() {
    String sql = "SELECT sa.event_id, sa.first_name, sa.last_name, sa.class_ref, e.company, e.subject " +
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
            rs.getString("subject")
        );
        assignments.add(assignment);
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return assignments;
  }
}