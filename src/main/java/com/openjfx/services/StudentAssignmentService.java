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
    // Track how many events each student is assigned to
    Map<String, Integer> studentAssignmentCount = new HashMap<>();

    // Initialize empty assignment slots for each event
    initializeEventSlots(assignments, events);

    // Initialize student assignment counter
    for (Choice choice : choices) {
      String studentId =
          choice.getFirstName() + "_" + choice.getLastName() + "_" + choice.getClassRef();
      studentAssignmentCount.put(studentId, 0);
    }

    // Assign students to their first choice events where possible
    assignFirstChoices(assignments, choices, events, studentAssignmentCount);

    // Assign remaining choices (2-6) if space is available
    assignRemainingChoices(assignments, choices, events, studentAssignmentCount);

    // Distribute students without enough assignments to less occupied events
    distributeRemainingAssignments(assignments, choices, events, studentAssignmentCount);

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
      List<Event> events, Map<String, Integer> studentAssignmentCount) {
    for (Choice choice : choices) {
      String firstChoice = choice.getChoice1();
      if (firstChoice.isEmpty()) {
        continue;  // Skip but we'll handle them later
      }

      Event event = findEventByChoice(firstChoice, events);
      if (event == null) {
        continue;
      }

      // Always fulfill first choice if possible
      if (!hasReachedCapacity(assignments, event)) {
        assignments.get(event.getId()).add(choice);
        incrementStudentAssignmentCount(studentAssignmentCount, choice);
      }
    }
  }

  /**
   * Assigns remaining students to their subsequent choices (2-6) if space is available.
   *
   * @author mian
   */
  private void assignRemainingChoices(Map<Integer, List<Choice>> assignments, List<Choice> choices,
      List<Event> events, Map<String, Integer> studentAssignmentCount) {
    for (int priority = 2; priority <= 6; priority++) {
      for (Choice choice : choices) {
        // Skip if student already has 5 assignments
        if (getStudentAssignmentCount(studentAssignmentCount, choice) >= 5) {
          continue;
        }

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
          incrementStudentAssignmentCount(studentAssignmentCount, choice);
        }
      }
    }
  }

  /**
   * Distributes students without enough assignments to less occupied events.
   *
   * @param assignments            Map of event IDs to assigned student choices
   * @param choices                List of student choices
   * @param events                 List of available events
   * @param studentAssignmentCount Map of student IDs to their assignment count
   * @author mian
   */

  private void distributeRemainingAssignments(Map<Integer, List<Choice>> assignments,
      List<Choice> choices, List<Event> events, Map<String, Integer> studentAssignmentCount) {

    // First pass: Try to assign students who need more assignments to events with space
    for (Choice student : choices) {
      int assignmentCount = getStudentAssignmentCount(studentAssignmentCount, student);
      if (assignmentCount >= 5) {
        continue; // Skip students who already have 5 assignments
      }

      // Sort events by current occupancy (least to most occupied)
      List<Event> sortedEvents = new ArrayList<>(events);
      sortedEvents.sort((e1, e2) -> {
        int occupancy1 = assignments.get(e1.getId()).size();
        int occupancy2 = assignments.get(e2.getId()).size();
        return Integer.compare(occupancy1, occupancy2);
      });

      // Try to assign student to events with available capacity
      for (Event event : sortedEvents) {
        if (assignmentCount >= 5) {
          break; // Stop once student has 5 assignments
        }

        if (!isAlreadyAssigned(assignments, student, event) &&
            !hasReachedCapacity(assignments, event)) {
          // Assign student to this event
          assignments.get(event.getId()).add(student);
          assignmentCount++;
          incrementStudentAssignmentCount(studentAssignmentCount, student);
        }
      }
    }

    // Second pass: For students who still don't have 5 assignments, override capacity limits
    for (Choice student : choices) {
      int assignmentCount = getStudentAssignmentCount(studentAssignmentCount, student);
      if (assignmentCount >= 5) {
        continue; // Skip students who already have 5 assignments
      }

      System.out.println("Student " + student.getFirstName() + " " + student.getLastName() +
          " has only " + assignmentCount + " assignments. Overriding capacity constraints...");

      // Sort events by current occupancy percentage relative to max capacity
      List<Event> eventsByCapacity = new ArrayList<>(events);
      eventsByCapacity.sort((e1, e2) -> {
        double ratio1 = (double) assignments.get(e1.getId()).size() / e1.getMaxParticipants();
        double ratio2 = (double) assignments.get(e2.getId()).size() / e2.getMaxParticipants();
        return Double.compare(ratio1, ratio2);
      });

      // Force assignment to events with the lowest occupancy percentage
      for (Event event : eventsByCapacity) {
        if (assignmentCount >= 5) {
          break;
        }

        if (!isAlreadyAssigned(assignments, student, event)) {
          // Even if at capacity, assign student anyway
          assignments.get(event.getId()).add(student);
          assignmentCount++;
          incrementStudentAssignmentCount(studentAssignmentCount, student);

          if (assignments.get(event.getId()).size() > event.getMaxParticipants()) {
            System.out.println("Warning: Event " + event.getId() + " (" +
                event.getCompany() + ") exceeded capacity by " +
                (assignments.get(event.getId()).size() - event.getMaxParticipants()));
          }
        }
      }

      // Final check if student still doesn't have 5 assignments
      if (assignmentCount < 5) {
        System.err.println("ERROR: Could not assign 5 events to student: " +
            student.getFirstName() + " " + student.getLastName() +
            ". Only assigned to " + assignmentCount + " events.");
      }
    }
  }

  /**
   * Increments the assignment count for a student.
   *
   * @param studentAssignmentCount Map of student IDs to their assignment count
   * @param choice                 Student choice
   * @author mian
   */

  private void incrementStudentAssignmentCount(Map<String, Integer> studentAssignmentCount,
      Choice choice) {
    String studentId =
        choice.getFirstName() + "_" + choice.getLastName() + "_" + choice.getClassRef();
    studentAssignmentCount.put(studentId, studentAssignmentCount.getOrDefault(studentId, 0) + 1);
  }

  /**
   * Gets the assignment count for a student.
   *
   * @param studentAssignmentCount Map of student IDs to their assignment count
   * @param choice                 Student choice
   * @return the number of assignments for the student
   * @author mian
   */

  private int getStudentAssignmentCount(Map<String, Integer> studentAssignmentCount,
      Choice choice) {
    String studentId =
        choice.getFirstName() + "_" + choice.getLastName() + "_" + choice.getClassRef();
    return studentAssignmentCount.getOrDefault(studentId, 0);
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
    String sql = "SELECT sa.event_id, sa.first_name, sa.last_name, sa.class_ref, e.company " +
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
            rs.getString("company")
        );
        assignments.add(assignment);
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return assignments;
  }
}