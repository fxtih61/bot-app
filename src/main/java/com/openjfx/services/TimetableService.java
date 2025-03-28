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
    }

    Map<String, List<EventRoomAssignment>> timeSlotAssignments = createTimetable(events, rooms,
        timeSlots, workshopsNeeded);
    saveTimeTableAssignments(timeSlotAssignments);
    printTimetable(timeSlotAssignments, timeSlots);

    // Print the timetable with company and subject information
    for (Map.Entry<String, List<EventRoomAssignment>> entry : timeSlotAssignments.entrySet()) {
      System.out.println("Time Slot: " + entry.getKey());
      for (EventRoomAssignment assignment : entry.getValue()) {
        System.out.println("Company: " + assignment.getEvent().getCompany() +
            " | Subject: " + assignment.getEvent().getSubject() +
            " | Room: " + assignment.getRoom().getName());
      }
    }

    return timeSlotAssignments;
  }

  /**
   * Creates a timetable for events based on room availability and time slots. This version ensures
   * consecutive timeslots for each event-subject combination.
   *
   * @author mian
   */
  private Map<String, List<EventRoomAssignment>> createTimetable(
      List<Event> events, List<Room> rooms, List<TimeSlot> timeSlots,
      Map<Integer, Integer> workshopsNeeded) {

    Map<String, List<EventRoomAssignment>> timeSlotAssignments = new HashMap<>();

    // Initialize time slot assignments
    timeSlots.forEach(slot -> timeSlotAssignments.put(slot.getSlot(), new ArrayList<>()));

    // Create a map to track which rooms are booked for each time slot
    Map<String, Set<Room>> bookedRooms = new HashMap<>();
    timeSlots.forEach(slot -> bookedRooms.put(slot.getSlot(), new HashSet<>()));

    // Sort events by workshop count (highest first) and earliest start time
    List<Event> sortedEvents = events.stream()
        .filter(e -> workshopsNeeded.getOrDefault(e.getId(), 0) > 0)
        .sorted((e1, e2) -> {
          int compare = workshopsNeeded.getOrDefault(e2.getId(), 0)
              .compareTo(workshopsNeeded.getOrDefault(e1.getId(), 0));
          if (compare == 0) {
            return e1.getEarliestStart().compareTo(e2.getEarliestStart());
          }
          return compare;
        })
        .collect(Collectors.toList());

    // Process each event independently
    for (Event event : sortedEvents) {
      int requiredWorkshops = workshopsNeeded.getOrDefault(event.getId(), 0);
      if (requiredWorkshops <= 0) {
        continue;
      }

      String earliestStart = event.getEarliestStart();

      // Find the index of the earliest start time slot
      int startSlotIndex = -1;
      for (int i = 0; i < timeSlots.size(); i++) {
        if (timeSlots.get(i).getSlot().compareTo(earliestStart) >= 0) {
          startSlotIndex = i;
          break;
        }
      }

      if (startSlotIndex == -1) {
        continue; // Skip if no valid start slot found
      }

      // Find a room that can be used for all required consecutive workshops
      Room selectedRoom = null;
      int consecutiveSlotStart = -1;

      // Try to find a room that works for all required consecutive slots
      for (Room room : rooms) {
        for (int i = startSlotIndex; i <= timeSlots.size() - requiredWorkshops; i++) {
          boolean roomAvailableForAll = true;

          // Check if this room is available for all required consecutive slots
          for (int j = 0; j < requiredWorkshops; j++) {
            String slotKey = timeSlots.get(i + j).getSlot();
            if (bookedRooms.get(slotKey).contains(room)) {
              roomAvailableForAll = false;
              break;
            }
          }

          if (roomAvailableForAll) {
            selectedRoom = room;
            consecutiveSlotStart = i;
            break;
          }
        }

        if (selectedRoom != null) {
          break; // Found a suitable room
        }
      }

      // If no room can accommodate all consecutive slots, try to find best available option
      if (selectedRoom == null) {
        // Find the room with the most available consecutive slots
        int maxConsecutiveSlots = 0;

        for (Room room : rooms) {
          for (int i = startSlotIndex; i < timeSlots.size(); i++) {
            int consecutiveCount = 0;
            for (int j = i; j < timeSlots.size(); j++) {
              String slotKey = timeSlots.get(j).getSlot();
              if (!bookedRooms.get(slotKey).contains(room)) {
                consecutiveCount++;
              } else {
                break;
              }
            }

            if (consecutiveCount > maxConsecutiveSlots) {
              maxConsecutiveSlots = consecutiveCount;
              selectedRoom = room;
              consecutiveSlotStart = i;
            }
          }
        }
      }

      // If we found a room, assign the event to consecutive time slots
      if (selectedRoom != null) {
        int assignedWorkshops = 0;
        for (int i = consecutiveSlotStart;
            i < timeSlots.size() && assignedWorkshops < requiredWorkshops;
            i++) {
          String slotKey = timeSlots.get(i).getSlot();

          // If room is already booked for this slot, skip it
          if (bookedRooms.get(slotKey).contains(selectedRoom)) {
            continue;
          }

          // Assign the event to this room and time slot
          EventRoomAssignment assignment = new EventRoomAssignment(event, selectedRoom);
          timeSlotAssignments.get(slotKey).add(assignment);
          bookedRooms.get(slotKey).add(selectedRoom);
          assignedWorkshops++;
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
          System.out.printf("Room %-15s : Event %d - %s - %s\n",
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