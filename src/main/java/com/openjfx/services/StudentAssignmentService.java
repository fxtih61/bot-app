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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service responsible for assigning students to events based on their choices and priorities.
 *
 * @author mian
 */
public class StudentAssignmentService {

  private Map<String, Map<Integer, Integer>> studentEventChoiceNumbers;

  public StudentAssignmentService() {
    this.studentEventChoiceNumbers = new HashMap<>();
  }

  /**
   * Assigns students to events based on their choices and the available capacity. Each student must
   * be assigned to exactly 5 events.
   *
   * @param choices list of student choices
   * @param events  list of available events
   * @return a map of event IDs to the list of students assigned to each event
   */
  public Map<Integer, List<Choice>> assignStudentsToEvents(List<Choice> choices,
      List<Event> events) {
    this.studentEventChoiceNumbers.clear();
    Map<Integer, List<Choice>> assignments = new HashMap<>();
    Map<String, Integer> studentAssignmentCount = new HashMap<>();
    Map<String, Set<Integer>> studentAssignedEvents = new HashMap<>();

    initializeEventSlots(assignments, events);
    for (Choice choice : choices) {
      String studentId =
          choice.getFirstName() + "_" + choice.getLastName() + "_" + choice.getClassRef();
      studentAssignmentCount.put(studentId, 0);
      studentAssignedEvents.put(studentId, new HashSet<>());
      studentEventChoiceNumbers.put(studentId, new HashMap<>());
    }

    // STEP 1: First choices
    for (Choice choice : choices) {
      String studentId =
          choice.getFirstName() + "_" + choice.getLastName() + "_" + choice.getClassRef();
      String firstChoice = choice.getChoice1();
      if (!firstChoice.isEmpty()) {
        Event event = findEventByChoice(firstChoice, events);
        if (event != null) {
          assignments.get(event.getId()).add(choice);
          studentAssignmentCount.put(studentId, 1);
          studentAssignedEvents.get(studentId).add(event.getId());
          studentEventChoiceNumbers.get(studentId).put(event.getId(), 1);
        }
      }
    }

    // STEP 2: Process remaining choices by priority
    List<StudentChoicePriority> allChoices = new ArrayList<>();
    for (Choice choice : choices) {
      String studentId =
          choice.getFirstName() + "_" + choice.getLastName() + "_" + choice.getClassRef();
      for (int priority = 2; priority <= 6; priority++) {
        String choiceStr = getChoiceByPriority(choice, priority);
        if (!choiceStr.isEmpty()) {
          Event event = findEventByChoice(choiceStr, events);
          if (event != null) {
            allChoices.add(new StudentChoicePriority(studentId, choice, event.getId(), priority));
          }
        }
      }
    }

    Collections.sort(allChoices, Comparator.comparingInt(StudentChoicePriority::getPriority));

    for (StudentChoicePriority scp : allChoices) {
      String studentId = scp.getStudentId();
      Choice choice = scp.getChoice();
      int eventId = scp.getEventId();

      if (studentAssignmentCount.get(studentId) >= 5 ||
          studentAssignedEvents.get(studentId).contains(eventId)) {
        continue;
      }

      assignments.get(eventId).add(choice);
      studentAssignmentCount.put(studentId, studentAssignmentCount.get(studentId) + 1);
      studentAssignedEvents.get(studentId).add(eventId);
      studentEventChoiceNumbers.get(studentId).put(eventId, scp.getPriority());
    }

    // STEP 3: Force assign remaining
    for (Choice choice : choices) {
      String studentId =
          choice.getFirstName() + "_" + choice.getLastName() + "_" + choice.getClassRef();
      int assignmentCount = studentAssignmentCount.get(studentId);

      if (assignmentCount >= 5) {
        continue;
      }

      List<EventPriority> remainingChoices = new ArrayList<>();
      for (int priority = 2; priority <= 6; priority++) {
        String choiceStr = getChoiceByPriority(choice, priority);
        if (!choiceStr.isEmpty()) {
          Event event = findEventByChoice(choiceStr, events);
          if (event != null && !studentAssignedEvents.get(studentId).contains(event.getId())) {
            remainingChoices.add(new EventPriority(event, priority));
          }
        }
      }

      Collections.sort(remainingChoices, Comparator.comparingInt(EventPriority::getPriority));

      for (EventPriority ep : remainingChoices) {
        if (assignmentCount >= 5) {
          break;
        }

        Event event = ep.getEvent();
        assignments.get(event.getId()).add(choice);
        assignmentCount++;
        studentAssignmentCount.put(studentId, assignmentCount);
        studentAssignedEvents.get(studentId).add(event.getId());
        studentEventChoiceNumbers.get(studentId).put(event.getId(), ep.getPriority());
      }

      // Force assign to any remaining events if still needed
      if (assignmentCount < 5) {
        for (Event event : events) {
          if (assignmentCount >= 5) {
            break;
          }

          if (!studentAssignedEvents.get(studentId).contains(event.getId())) {
            assignments.get(event.getId()).add(choice);
            assignmentCount++;
            studentAssignmentCount.put(studentId, assignmentCount);
            studentAssignedEvents.get(studentId).add(event.getId());
            studentEventChoiceNumbers.get(studentId).put(event.getId(), 0);
          }
        }
      }
    }

    return assignments;
  }

  /**
   * Ensures that every student is assigned to exactly 5 events. If a student has less than 5
   * events, they will be forcibly assigned to available events.
   */
  private void ensureExactlyFiveAssignments(Map<Integer, List<Choice>> assignments,
      List<Choice> choices,
      List<Event> events, Map<String, Integer> studentAssignmentCount) {

    for (Choice choice : choices) {
      String studentId =
          choice.getFirstName() + "_" + choice.getLastName() + "_" + choice.getClassRef();
      int assignmentCount = studentAssignmentCount.get(studentId);

      // If student has less than 5 assignments, forcibly assign them to events with capacity
      while (assignmentCount < 5) {
        for (Event event : events) {
          if (!hasReachedCapacity(assignments, event) && !isAlreadyAssigned(assignments, choice,
              event)) {
            assignments.get(event.getId()).add(choice);
            assignmentCount++;
            studentAssignmentCount.put(studentId, assignmentCount);
            break;
          }
        }

        // If we still haven't reached 5 and went through all events, we'll need to override capacity for some events
        if (assignmentCount < 5) {
          for (Event event : events) {
            if (!isAlreadyAssigned(assignments, choice, event)) {
              assignments.get(event.getId()).add(choice);
              assignmentCount++;
              studentAssignmentCount.put(studentId, assignmentCount);
              break;
            }
          }
        }

        // Safety check to prevent infinite loop if there are less than 5 unique events
        if (assignmentCount < 5 && events.size() < 5) {
          break;
        }
      }
    }
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
    String sql = "INSERT INTO student_assignments (event_id, first_name, last_name, class_ref, choice_no) VALUES (?, ?, ?, ?, ?)";

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

          // Determine choice number
          String studentId =
              student.getFirstName() + "_" + student.getLastName() + "_" + student.getClassRef();
          int choiceNo = studentEventChoiceNumbers.getOrDefault(studentId, new HashMap<>())
              .getOrDefault(eventId, 0);
          stmt.setInt(5, choiceNo);

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
    String sql =
        "SELECT sa.event_id, sa.first_name, sa.last_name, sa.class_ref, " +
            "e.company, e.subject, sa.time_slot, sa.room_id, sa.choice_no " +
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
        assignment.setTimeSlot(rs.getString("time_slot"));
        assignment.setRoomId(rs.getString("room_id"));
        assignment.setChoiceNo(rs.getInt("choice_no"));
        assignments.add(assignment);
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return assignments;
  }

  /**
   * Converts database assignments to the required map format.
   *
   * @return map of event IDs to their assigned choices
   * @author mian
   */
  public Map<Integer, List<Choice>> getAssignmentsAsMap() {
    List<StudentAssignment> assignments = getAllAssignments();
    Map<Integer, List<Choice>> assignmentMap = new HashMap<>();

    for (StudentAssignment assignment : assignments) {
      Choice choice = new Choice();
      choice.setFirstName(assignment.getFirstName());
      choice.setLastName(assignment.getLastName());
      choice.setClassRef(assignment.getClassRef());
      choice.setChoice1(String.valueOf(assignment.getEventId()));

      assignmentMap
          .computeIfAbsent(assignment.getEventId(), k -> new ArrayList<>())
          .add(choice);
    }

    return assignmentMap;
  }


  // Helper class to track student choices with priorities
  private static class StudentChoicePriority {

    private final String studentId;
    private final Choice choice;
    private final int eventId;
    private final int priority;

    public StudentChoicePriority(String studentId, Choice choice, int eventId, int priority) {
      this.studentId = studentId;
      this.choice = choice;
      this.eventId = eventId;
      this.priority = priority;
    }

    public String getStudentId() {
      return studentId;
    }

    public Choice getChoice() {
      return choice;
    }

    public int getEventId() {
      return eventId;
    }

    public int getPriority() {
      return priority;
    }
  }

  // Helper class to track events with priorities
  private static class EventPriority {

    private final Event event;
    private final int priority;

    public EventPriority(Event event, int priority) {
      this.event = event;
      this.priority = priority;
    }

    public Event getEvent() {
      return event;
    }

    public int getPriority() {
      return priority;
    }
  }
}