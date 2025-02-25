package com.openjfx.services;

import com.openjfx.config.DatabaseConfig;
import com.openjfx.models.Choice;
import com.openjfx.models.Event;
import com.openjfx.models.Room;
import com.openjfx.models.TimeSlot;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.sql.ResultSet;
import java.util.Set;

/**
 * Service class responsible for assigning students to events based on their choices and priorities.
 * The service handles the logic for distributing students across events while respecting maximum
 * capacity constraints and student preferences.
 *
 * <p>The assignment process follows these steps:
 * <ol>
 *   <li>Load student choices, events, and room data from Excel files</li>
 *   <li>Initialize empty slots for each event</li>
 *   <li>Assign students to their first choice events where possible</li>
 *   <li>Assign remaining students to their subsequent choices (2-6) if space is available</li>
 * </ol>
 *
 * <p>Example usage:
 * <pre>
 * AssignmentService service = new AssignmentService(choiceService, eventService, roomService);
 * service.runAssignment("choices.xlsx", "events.xlsx", "rooms.xlsx");
 * </pre>
 */
public class AssignmentService {

  private final ChoiceService choiceService;
  private final EventService eventService;
  private final RoomService roomService;

  // Track assignments: <EventID, List<StudentChoices>>
  private final Map<Integer, List<Choice>> assignments = new HashMap<>();

  /**
   * Constructs a new AssignmentService with the required dependencies.
   *
   * @param choiceService service for handling student choices data
   * @param eventService  service for handling event data
   * @param roomService   service for handling room data
   */
  public AssignmentService(ChoiceService choiceService, EventService eventService,
      RoomService roomService) {
    this.choiceService = choiceService;
    this.eventService = eventService;
    this.roomService = roomService;
  }

  /**
   * Executes the assignment process using data from the specified Excel files.
   *
   * @throws IOException if there is an error reading the Excel files
   */
  public void runAssignment()
      throws IOException {
    List<Choice> choices = choiceService.loadChoices();
    List<Event> events = eventService.loadEvents();
    List<Room> rooms = roomService.loadRooms();
    List<TimeSlot> timeSlots = loadTimeSlots();

    // Initializes empty assignment slots for each event.
    initializeEventslots(events);

    // Assigns students to their first choice events where possible.
    assignFirstChoices(choices, events);

    // Assigns remaining students to their subsequent choices (2-6) if space is available.
    assignRemainingChoices(choices, events);

    // Maps companies and counts how many students are assigned to each event.
    mapCompanies(events, choices);

    createTimetable(events, rooms, timeSlots, choices);

    //TODO: generate outputs(Schedule, Attendance list, etc.)
  }
  /**
   * Finds a suitable room that hasn't been assigned yet and meets capacity requirements.
   */
  private Room findSuitableRoom(List<Room> rooms, int requiredCapacity, Collection<Room> assignedRooms) {
    return rooms.stream()
        .filter(room -> room.getCapacity() >= requiredCapacity)
        .filter(room -> !assignedRooms.contains(room))
        .findFirst()
        .orElse(null);
  }

  /**
   * Initializes empty assignment slots for each event.
   *
   * @param events list of events to initialize slots for
   */
  private void initializeEventslots(List<Event> events) {
    for (Event event : events) {
      assignments.put(event.getId(), new ArrayList<>());
    }
  }

  /**
   * Assigns students to their first choice events where possible.
   *
   * @param choices list of student choices
   * @param events  list of available events
   */
  private void assignFirstChoices(List<Choice> choices, List<Event> events) {
    for (Choice choice : choices) {
      String firstChoice = choice.getChoice1();
      if (firstChoice.isEmpty()) {
        continue;
      }

      Event event = findEventByChoice(firstChoice, events);
      if (event == null || hasCapacity(event)) {
        continue;
      }

      assignments.get(event.getId()).add(choice);
    }
  }

  /**
   * Finds an event by the choice string provided.
   *
   * @param choice the choice string to search for
   * @param events the list of events to search
   * @return the event matching the choice, or null if not found
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
   * @param event the event to check
   * @return true if the event is full, false otherwise
   */
  private boolean hasCapacity(Event event) {
    int current = assignments.get(event.getId()).size();
    return current >= event.getMaxParticipants();
  }

  /**
   * Assigns remaining students to their subsequent choices (2-6) if space is available.
   *
   * @param choices list of student choices
   * @param events  list of available events
   */
  private void assignRemainingChoices(List<Choice> choices, List<Event> events) {
    for (int priority = 2; priority <= 6; priority++) {
      for (Choice choice : choices) {
        String choiceStr = getChoiceByPriority(choice, priority);
        if (choiceStr.isEmpty()) {
          continue;
        }

        Event event = findEventByChoice(choiceStr, events);
        if (event == null || hasCapacity(event)) {
          continue;
        }

        if (!isAlreadyAssigned(choice, event)) {
          assignments.get(event.getId()).add(choice);
        }
      }
    }
  }

  /**
   * Returns the choice string based on the priority provided.
   *
   * @param choice   the choice object to extract from
   * @param priority the priority of the choice to return
   * @return the choice string based on the priority
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
   * Checks if a student is already assigned to an event.
   *
   * @param student the student to check
   * @param event   the event to check
   * @return true if the student is already assigned, false otherwise
   */
  private boolean isAlreadyAssigned(Choice student, Event event) {
    return assignments.get(event.getId()).contains(student);
  }

  /**
   * Maps companies and counts how many students are assigned to each event.
   *
   * @param events list of events to analyze
   */
  private int mapCompanies(List<Event> events, List<Choice> allChoices) {
    Map<Integer, Integer> choiceCounts = new HashMap<>();

    // Count all choices for each event
    for (Choice choice : allChoices) {
      countEventChoice(choiceCounts, choice.getChoice1());
      countEventChoice(choiceCounts, choice.getChoice2());
      countEventChoice(choiceCounts, choice.getChoice3());
      countEventChoice(choiceCounts, choice.getChoice4());
      countEventChoice(choiceCounts, choice.getChoice5());
      countEventChoice(choiceCounts, choice.getChoice6());
    }

    int totalWorkshops = 0;

    // Calculate total workshops needed
    for (Event event : events) {
      int eventId = event.getId();
      int choiceCount = choiceCounts.getOrDefault(eventId, 0);
      int maxCapacity = event.getMaxParticipants();
      totalWorkshops += calculateAdditionalWorkshops(choiceCount, maxCapacity);
    }

    return totalWorkshops;
  }

  private int calculateAdditionalWorkshops(int demand, int capacity) {
    if (demand <= capacity) {
      return 1;
    }
    return (int) Math.ceil((double) (demand - capacity) / capacity) + 1;
  }

  private void countEventChoice(Map<Integer, Integer> counts, String choice) {
    if (choice == null || choice.isEmpty()) {
      return;
    }
    try {
      int eventId = Integer.parseInt(choice.replaceAll("[^0-9]", ""));
      counts.merge(eventId, 1, Integer::sum);
    } catch (NumberFormatException e) {
      // Ignore invalid choices
    }
  }

  private List<TimeSlot> loadTimeSlots() {
    String sql = "SELECT * FROM timeslots";
    List<TimeSlot> timeSlots = new ArrayList<>();

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {

      while (rs.next()) {
        TimeSlot slot = new TimeSlot(
            rs.getInt("id"),
            rs.getString("start_time"),
            rs.getString("end_time"),
            rs.getString("slot")
        );
        timeSlots.add(slot);
      }
    } catch (SQLException e) {
      System.err.println("Error loading time slots: " + e.getMessage());
      e.printStackTrace();
    }
    return timeSlots;
  }

  private void createTimetable(List<Event> events, List<Room> rooms, List<TimeSlot> timeSlots, List<Choice> allChoices) {
      Map<Integer, Integer> workshopsNeeded = new HashMap<>();
      Map<String, List<EventRoomAssignment>> timeSlotAssignments = new HashMap<>();
      Map<Integer, Room> companyRooms = new HashMap<>();

      // Initialize time slot assignments
      for (TimeSlot slot : timeSlots) {
          timeSlotAssignments.put(slot.getSlot(), new ArrayList<>());
      }

      // Calculate workshops needed for each event
      for (Event event : events) {
          int eventId = event.getId();
          int choiceCount = countChoicesForEvent(allChoices, eventId);
          int workshopCount = calculateAdditionalWorkshops(choiceCount, event.getMaxParticipants());
          workshopsNeeded.put(eventId, workshopCount);
      }

      // Print workshops needed
      System.out.println("\nWORKSHOPS NEEDED:");
      System.out.println("=================");
      workshopsNeeded.entrySet().stream()
          .sorted(Map.Entry.comparingByKey())
          .forEach(entry -> System.out.printf("Event %d: %d workshops\n", entry.getKey(), entry.getValue()));

      // Create a list of events that need workshops, sorted by workshop count
      List<Event> eventsNeedingRooms = events.stream()
          .filter(e -> workshopsNeeded.get(e.getId()) > 0)
          .sorted((e1, e2) -> workshopsNeeded.get(e2.getId()).compareTo(workshopsNeeded.get(e1.getId())))
          .collect(java.util.stream.Collectors.toList());

      // Assign rooms using round-robin approach
      List<Room> availableRooms = new ArrayList<>(rooms);
      int currentRoomIndex = 0;

      for (Event event : eventsNeedingRooms) {
          if (availableRooms.isEmpty()) {
              break;
          }
          companyRooms.put(event.getId(), availableRooms.get(currentRoomIndex));
          currentRoomIndex = (currentRoomIndex + 1) % availableRooms.size();
      }

      // For each time slot, schedule workshops for events that still need them
      Map<Integer, Integer> remainingWorkshops = new HashMap<>(workshopsNeeded);

      for (TimeSlot slot : timeSlots) {
          List<EventRoomAssignment> currentSlotAssignments = timeSlotAssignments.get(slot.getSlot());
          Set<Room> usedRoomsInSlot = new HashSet<>();

          // Schedule workshops for events that still need them
          for (Event event : events) {
              int eventId = event.getId();
              Room assignedRoom = companyRooms.get(eventId);

              if (assignedRoom != null && remainingWorkshops.get(eventId) > 0
                  && !usedRoomsInSlot.contains(assignedRoom)) {
                  currentSlotAssignments.add(new EventRoomAssignment(event, assignedRoom));
                  remainingWorkshops.put(eventId, remainingWorkshops.get(eventId) - 1);
                  usedRoomsInSlot.add(assignedRoom);
              }
          }
      }

      // Print timetable
      System.out.println("\nTIMETABLE:");
      System.out.println("==========");

      for (TimeSlot slot : timeSlots) {
          System.out.printf("\nTime Slot %s (%s - %s):\n",
              slot.getSlot(), slot.getStartTime(), slot.getEndTime());
          System.out.println("----------------------------");

          List<EventRoomAssignment> assignments = timeSlotAssignments.get(slot.getSlot());
          if (assignments.isEmpty()) {
              System.out.println("No events scheduled");
          } else {
              assignments.sort((a, b) -> a.room.getName().compareTo(b.room.getName()));
              for (EventRoomAssignment assignment : assignments) {
                  System.out.printf("Room %-15s : Event %d - %s (%s)\n",
                      assignment.room.getName(),
                      assignment.event.getId(),
                      assignment.event.getCompany(),
                      assignment.event.getSubject());
              }
          }
      }
  }

  private Room findAvailableRoom(List<Room> rooms, int requiredCapacity,
                               List<EventRoomAssignment> currentAssignments) {
      return rooms.stream()
          .filter(room -> room.getCapacity() >= requiredCapacity)
          .filter(room -> currentAssignments.stream()
              .noneMatch(assignment -> assignment.room.equals(room)))
          .findFirst()
          .orElse(null);
  }

  private static class EventRoomAssignment {
      final Event event;
      final Room room;

      EventRoomAssignment(Event event, Room room) {
          this.event = event;
          this.room = room;
      }
  }

  private void saveTimeTableAssignments(Map<String, List<Integer>> timeSlotAssignments,
      Map<Integer, Room> companyRooms) {
    String sql = "INSERT INTO timetable (event_id, room_id, time_slot) VALUES (?, ?, ?)";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      conn.setAutoCommit(false);

      for (Map.Entry<String, List<Integer>> entry : timeSlotAssignments.entrySet()) {
        String timeSlot = entry.getKey();
        for (Integer eventId : entry.getValue()) {
          Room room = companyRooms.get(eventId);
          if (room != null) {
            pstmt.setInt(1, eventId);
            pstmt.setString(2, room.getName());
            pstmt.setString(3, timeSlot);
            pstmt.addBatch();
          }
        }
      }

      pstmt.executeBatch();
      conn.commit();
    } catch (SQLException e) {
      System.err.println("Error saving timetable assignments: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private int countChoicesForEvent(List<Choice> choices, int eventId) {
    Map<Integer, Integer> counts = new HashMap<>();
    for (Choice choice : choices) {
      countEventChoice(counts, choice.getChoice1());
      countEventChoice(counts, choice.getChoice2());
      countEventChoice(counts, choice.getChoice3());
      countEventChoice(counts, choice.getChoice4());
      countEventChoice(counts, choice.getChoice5());
      countEventChoice(counts, choice.getChoice6());
    }
    return counts.getOrDefault(eventId, 0);
  }
}
