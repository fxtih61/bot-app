package com.openjfx.services;

import com.openjfx.config.DatabaseConfig;
import com.openjfx.models.Event;
import com.openjfx.models.EventRoomAssignment;
import com.openjfx.models.Room;
import com.openjfx.models.TimeSlot;
import com.openjfx.models.TimetableRow;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service responsible for creating and managing timetables.
 *
 * @author mian
 */
public class TimetableService {

  /**
   * Creates and saves a timetable for events based on room availability and time slots.
   *
   * @param events          list of events
   * @param rooms           list of rooms
   * @param timeSlots       list of time slots
   * @param workshopsNeeded map of event IDs to the number of workshops needed
   * @return a map of time slots to the list of event-room assignments
   * @author mian
   */
  /**
   * Creates and saves a timetable for events based on room availability and time slots.
   *
   * @param events          list of events
   * @param rooms           list of rooms
   * @param timeSlots       list of time slots
   * @param workshopsNeeded map of event IDs to the number of workshops needed (can be null)
   * @return a map of time slots to the list of event-room assignments
   * @author mian
   */
  public Map<String, List<EventRoomAssignment>> createAndSaveTimetable(
      List<Event> events, List<Room> rooms, List<TimeSlot> timeSlots,
      Map<Integer, Integer> workshopsNeeded) {

    // If no workshop demand data is provided, load it from the database
    if (workshopsNeeded == null || workshopsNeeded.isEmpty()) {
      WorkshopDemandService demandService = new WorkshopDemandService();
      workshopsNeeded = demandService.loadDemandFromDatabase();
      System.out.println("Loaded workshop demand data from database");
    }

    Map<String, List<EventRoomAssignment>> timeSlotAssignments = createTimetable(events, rooms,
        timeSlots, workshopsNeeded);
    saveTimeTableAssignments(timeSlotAssignments);
    printTimetable(timeSlotAssignments, timeSlots);

    return timeSlotAssignments;
  }

  /**
   * Creates a timetable for events based on room availability and time slots.
   *
   * @author mian
   */
  private Map<String, List<EventRoomAssignment>> createTimetable(
      List<Event> events, List<Room> rooms, List<TimeSlot> timeSlots,
      Map<Integer, Integer> workshopsNeeded) {

    Map<String, List<EventRoomAssignment>> timeSlotAssignments = new HashMap<>();
    Map<Integer, Room> companyRooms = new HashMap<>();

    // Initialize time slot assignments
    for (TimeSlot slot : timeSlots) {
      timeSlotAssignments.put(slot.getSlot(), new ArrayList<>());
    }

    // Print workshops needed
    System.out.println("\nWORKSHOPS NEEDED:");
    System.out.println("=================");
    workshopsNeeded.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .forEach(entry -> System.out.printf("Event %d: %d workshops\n", entry.getKey(),
            entry.getValue()));

    // Create a list of events that need workshops, sorted by workshop count
    List<Event> eventsNeedingRooms = events.stream()
        .filter(e -> workshopsNeeded.getOrDefault(e.getId(), 0) > 0)
        .sorted((e1, e2) -> workshopsNeeded.getOrDefault(e2.getId(), 0)
            .compareTo(workshopsNeeded.getOrDefault(e1.getId(), 0)))
        .collect(Collectors.toList());

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


        if (assignedRoom != null
            && remainingWorkshops.getOrDefault(eventId, 0) > 0
            && !usedRoomsInSlot.contains(assignedRoom)) {
          currentSlotAssignments.add(new EventRoomAssignment(event, assignedRoom));
          remainingWorkshops.put(eventId, remainingWorkshops.getOrDefault(eventId, 0) - 1);
          usedRoomsInSlot.add(assignedRoom);
        }
      }
    }

    return timeSlotAssignments;
  }

  /**
   * Saves timetable assignments to the database.
   *
   * @author mian
   */
  private void saveTimeTableAssignments(
      Map<String, List<EventRoomAssignment>> timeSlotAssignments) {
    String sql = "INSERT INTO timetable_assignments (event_id, room_id, time_slot) VALUES (?, ?, ?)";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      conn.setAutoCommit(false);

      for (Map.Entry<String, List<EventRoomAssignment>> entry : timeSlotAssignments.entrySet()) {
        String timeSlot = entry.getKey();
        for (EventRoomAssignment assignment : entry.getValue()) {
          pstmt.setInt(1, assignment.getEvent().getId());
          pstmt.setString(2, assignment.getRoom().getName());
          pstmt.setString(3, timeSlot);
          pstmt.addBatch();
        }
      }

      pstmt.executeBatch();
      conn.commit();
    } catch (SQLException e) {
      System.err.println("Error saving timetable assignments: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Prints the timetable for debugging purposes.
   *
   * @author mian
   */
  private void printTimetable(Map<String, List<EventRoomAssignment>> timeSlotAssignments,
      List<TimeSlot> timeSlots) {
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
        assignments.sort((a, b) -> a.getRoom().getName().compareTo(b.getRoom().getName()));
        for (EventRoomAssignment assignment : assignments) {
          System.out.printf("Room %-15s : Event %d - %s (%s)\n",
              assignment.getRoom().getName(),
              assignment.getEvent().getId(),
              assignment.getEvent().getCompany(),
              assignment.getEvent().getSubject());
        }
      }
    }
  }

  /**
   * Finds an available room with the required capacity.
   *
   * @author mian
   */
  private Room findAvailableRoom(List<Room> rooms, int requiredCapacity,
      List<EventRoomAssignment> currentAssignments) {
    return rooms.stream()
        .filter(room -> room.getCapacity() >= requiredCapacity)
        .filter(room -> currentAssignments.stream()
            .noneMatch(assignment -> assignment.getRoom().equals(room)))
        .findFirst()
        .orElse(null);
  }

  /**
   * Loads timetable assignments from the database.
   *
   * @return List of event-room assignments
   * @author mian
   */
  public List<EventRoomAssignment> loadTimeTableAssignments() {
    List<EventRoomAssignment> assignments = new ArrayList<>();
    String sql = "SELECT t.event_id, t.room_id, t.time_slot, " +
        "e.company, e.subject, e.max_participants, e.min_participants, e.earliest_start, " +
        "r.capacity " +
        "FROM timetable_assignments t " +
        "JOIN events e ON t.event_id = e.id " +
        "JOIN rooms r ON t.room_id = r.name " +
        "ORDER BY t.time_slot, t.room_id";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {

      while (rs.next()) {
        Event event = new Event(
            rs.getInt("event_id"),
            rs.getString("company"),
            rs.getString("subject"),
            rs.getInt("max_participants"),
            rs.getInt("min_participants"),
            rs.getString("earliest_start")
        );
        Room room = new Room(
            rs.getString("room_id"),
            rs.getInt("capacity")
        );
        EventRoomAssignment assignment = new EventRoomAssignment(event, room);
        assignment.setTimeSlot(rs.getString("time_slot"));
        assignments.add(assignment);
      }
    } catch (SQLException e) {
      System.err.println("Error loading timetable assignments: " + e.getMessage());
      e.printStackTrace();
    }
    return assignments;
  }

  /**
   * Converts EventRoomAssignment objects to TimetableRow objects for display.
   *
   * @return List of TimetableRow objects
   * @author mian
   */
  public List<TimetableRow> getTimetableRowsForDisplay() {
    List<EventRoomAssignment> assignments = loadTimeTableAssignments();
    List<TimetableRow> rows = new ArrayList<>();

    for (EventRoomAssignment assignment : assignments) {
      rows.add(new TimetableRow(
          assignment.getTimeSlot(),
          assignment.getRoom().getName(),
          assignment.getRoom().getCapacity(),
          assignment.getEvent().getId(),
          assignment.getEvent().getCompany(),
          assignment.getEvent().getSubject()
      ));
    }

    return rows;
  }
}