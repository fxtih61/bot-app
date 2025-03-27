package com.openjfx.services;

import com.openjfx.config.DatabaseConfig;
import com.openjfx.models.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
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

  /**
   * The file path where the exported Excel file will be saved for Events.
   *
   */
  private String filePathEvents = "EXPORT BOT5_Anwesenheitsliste";

    /**
   * Exports the provided event data to an Excel file at the specified file path.
   *
   * @param filePath  The path where the Excel file will be saved.
   * @param eventData A map containing event details, including the event name, time slots, and participants.
   * @throws IOException If an I/O error occurs during file writing.
   * @throws IllegalArgumentException If the event data is empty.
   *
   * @author leon
   */
  public void exportEventData(String filePath, Map<String, Object> eventData) throws IOException {
    if (eventData.isEmpty()) {
      throw new IllegalArgumentException("Event data must not be empty.");
    }

    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Data");

      int rowIndex = 0;

      // Style for the title "Anwesenheitsliste" with font size 16 (no borders)
      CellStyle titleStyle = createTitleStyle(workbook);

      // Style for the event name with font size 16 (no borders)
      CellStyle eventStyle = createEventStyle(workbook);

      // Style for the time slots with font size 11 (no borders)
      CellStyle timeStyle = createTimeStyle(workbook);

      // Style for the headers (with borders)
      CellStyle headerStyle = createHeaderStyle(workbook);

      // Style for the data rows (with borders)
      CellStyle dataStyle = createDataStyle(workbook);

      // Add the title "Anwesenheitsliste"
      Row titleRow = sheet.createRow(rowIndex++);
      Cell titleCell = titleRow.createCell(0);
      titleCell.setCellValue("Anwesenheitsliste");
      titleCell.setCellStyle(titleStyle);

      // Add the event name
      Row eventRow = sheet.createRow(rowIndex++);
      Cell eventCell = eventRow.createCell(0);
      eventCell.setCellValue((String) eventData.get("Veranstaltung"));
      eventCell.setCellStyle(eventStyle);

      // Define headers for the table
      String[] headers = {"Klasse", "Name", "Vorname", "Anwesend?"};

      // Add time slots and participant data
      List<Map<String, Object>> timeSlots = (List<Map<String, Object>>) eventData.get("Zeitfenster");
      for (Map<String, Object> timeSlot : timeSlots) {
        // Add the time slot (without borders)
        Row timeRow = sheet.createRow(rowIndex++);
        Cell timeCell = timeRow.createCell(0);
        timeCell.setCellValue((String) timeSlot.get("Uhrzeit"));
        timeCell.setCellStyle(timeStyle);

        // Add headers (with borders)
        Row headerRow = sheet.createRow(rowIndex++);
        for (int i = 0; i < headers.length; i++) {
          Cell cell = headerRow.createCell(i);
          cell.setCellValue(headers[i]);
          cell.setCellStyle(headerStyle); // Borders for headers
        }

        // Add participant data (with borders)
        List<Map<String, String>> participants = (List<Map<String, String>>) timeSlot.get("Teilnehmer");
        for (Map<String, String> participant : participants) {
          Row row = sheet.createRow(rowIndex++);
          for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(participant.get(headers[i]));
            cell.setCellStyle(dataStyle); // Borders for data rows
          }
        }

        // Add an empty row after each time slot
        sheet.createRow(rowIndex++);
      }

      // Adjust column widths
      adjustColumnWidths(sheet);

      // Save the workbook to the specified file path
      saveWorkbook(workbook, filePath);
    }
  }

  /**
   * Creates a cell style for the title with a font size of 16 and no borders.
   *
   * @param workbook The workbook to create the style in.
   * @return The created cell style.
   *
   * @author leon
   */
  private CellStyle createTitleStyle(Workbook workbook) {
    CellStyle titleStyle = workbook.createCellStyle();
    Font titleFont = workbook.createFont();
    titleFont.setFontHeightInPoints((short) 16);
    titleStyle.setFont(titleFont);
    return titleStyle;
  }

  /**
   * Creates a cell style for the event name with a bold font size of 16 and no borders.
   *
   * @param workbook The workbook to create the style in.
   * @return The created cell style.
   *
   * @author leon
   */
  private CellStyle createEventStyle(Workbook workbook) {
    CellStyle eventStyle = workbook.createCellStyle();
    Font eventFont = workbook.createFont();
    eventFont.setFontHeightInPoints((short) 16);
    eventFont.setBold(true);
    eventStyle.setFont(eventFont);
    return eventStyle;
  }

  /**
   * Creates a cell style for the time slots with a bold font size of 11 and no borders.
   *
   * @param workbook The workbook to create the style in.
   * @return The created cell style.
   *
   * @author leon
   */
  private CellStyle createTimeStyle(Workbook workbook) {
    CellStyle timeStyle = workbook.createCellStyle();
    Font timeFont = workbook.createFont();
    timeFont.setFontHeightInPoints((short) 11);
    timeFont.setBold(true);
    timeStyle.setFont(timeFont);
    return timeStyle;
  }

  /**
   * Creates a cell style for the headers with bold text and thin borders.
   *
   * @param workbook The workbook to create the style in.
   * @return The created cell style.
   *
   * @author leon
   */
  private CellStyle createHeaderStyle(Workbook workbook) {
    CellStyle headerStyle = workbook.createCellStyle();
    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerStyle.setFont(headerFont);
    headerStyle.setBorderTop(BorderStyle.THIN);
    headerStyle.setBorderBottom(BorderStyle.THIN);
    headerStyle.setBorderLeft(BorderStyle.THIN);
    headerStyle.setBorderRight(BorderStyle.THIN);
    return headerStyle;
  }

  /**
   * Creates a cell style for the data rows with thin borders.
   *
   * @param workbook The workbook to create the style in.
   * @return The created cell style.
   *
   * @author leon
   */
  private CellStyle createDataStyle(Workbook workbook) {
    CellStyle dataStyle = workbook.createCellStyle();
    dataStyle.setBorderTop(BorderStyle.THIN);
    dataStyle.setBorderBottom(BorderStyle.THIN);
    dataStyle.setBorderLeft(BorderStyle.THIN);
    dataStyle.setBorderRight(BorderStyle.THIN);
    return dataStyle;
  }

  /**
   * Adjusts the column widths for the sheet to ensure all data is visible.
   *
   * @param sheet The sheet to adjust the column widths for.
   *
   * @author leon
   */
  private void adjustColumnWidths(Sheet sheet) {
    sheet.setColumnWidth(0, 10 * 256); // Klasse
    sheet.setColumnWidth(1, 15 * 256); // Name
    sheet.setColumnWidth(2, 20 * 256); // Vorname
    sheet.setColumnWidth(3, 12 * 256); // Anwesend?
  }

  /**
   * Saves the workbook to the specified file path.
   *
   * @param workbook The workbook to save.
   * @param filePath The path where the workbook will be saved.
   * @throws IOException If an I/O error occurs during file writing.
   *
   * @author leon
   */
  private void saveWorkbook(Workbook workbook, String filePath) throws IOException {
    try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
      workbook.write(fileOut);
    }
  }


  /**
   * Prepares student assignment data for Excel export by time slots.
   *
   * @param dataToExport List of student assignments to process.
   * @return Structured data map for export, or empty map if invalid input.
   * @throws IllegalArgumentException If input contains invalid time slot codes.
   *
   * @author leon
   */
  public Map<String, Object> prepareDataForExport(List<Object> dataToExport) {
    if (dataToExport == null || dataToExport.isEmpty()) {
      return Collections.emptyMap();
    }

    Map<String, Object> eventData = new HashMap<>();
    Map<String, List<Map<String, String>>> timeSlotMap = new HashMap<>();

    Map<String, String> timeSlotMapping = Map.of(
            "A", "8:45-9:30",
            "B", "9:50-10:35",
            "C", "10:35-11:20",
            "D", "11:40-12:25",
            "E", "12:25-13:10"
    );

    String companyName = null;

    for (Object obj : dataToExport) {
      if (!(obj instanceof StudentAssignment)) {
        continue;
      }

      StudentAssignment assignment = (StudentAssignment) obj;

      if (companyName == null) {
        companyName = assignment.getCompanyName();
        eventData.put("Veranstaltung", companyName);
      }

      String timeSlotValue = timeSlotMapping.getOrDefault(assignment.getTimeSlot(), assignment.getTimeSlot());

      Map<String, String> participant = new HashMap<>();
      participant.put("Klasse", assignment.getClassRef());
      participant.put("Name", assignment.getLastName());
      participant.put("Vorname", assignment.getFirstName());
      participant.put("Anwesend?", "");

      timeSlotMap.computeIfAbsent(timeSlotValue, k -> new ArrayList<>()).add(participant);
    }

    if (companyName == null) {
      return Collections.emptyMap();
    }

    List<Map<String, Object>> timeSlots = timeSlotMap.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
              Map<String, Object> timeSlot = new HashMap<>();
              timeSlot.put("Uhrzeit", entry.getKey());
              timeSlot.put("Teilnehmer", entry.getValue());
              return timeSlot;
            })
            .collect(Collectors.toList());

    eventData.put("Zeitfenster", timeSlots);
    return eventData;
  }
  /**
   * Returns the file path to which the data will be exported for the Events.
   *
   * @return The file path as a string.
   *
   * @author leon
   */
  public String getFilePathEvent() {
    return filePathEvents;
  }
  

}