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
   * Prepares data for export in attendance list format, grouping participants by time slots
   * and sorting them in chronological order (A → B → C → D → E).
   *
   * @param dataToExport List of objects to be exported (expected to contain StudentAssignment instances)
   * @return Map containing the prepared export data with sorted time slots
   *
   * @author leon
   */
  public Map<String, Object> prepareDataForExportForAttendanceList(List<Object> dataToExport) {
    if (dataToExport == null || dataToExport.isEmpty()) {
      return Collections.emptyMap();
    }

    // Define time slot mapping with proper chronological order
    Map<String, String> timeSlotMapping = new LinkedHashMap<>(); // Maintains insertion order
    timeSlotMapping.put("A", "08:45-09:30");
    timeSlotMapping.put("B", "09:50-10:35");
    timeSlotMapping.put("C", "10:35-11:20");
    timeSlotMapping.put("D", "11:40-12:25");
    timeSlotMapping.put("E", "12:25-13:10");

    // Create a reverse mapping from time range to slot for sorting
    Map<String, String> timeRangeToSlot = new HashMap<>();
    timeSlotMapping.forEach((slot, range) -> timeRangeToSlot.put(range, slot));

    Map<String, Object> eventData = new HashMap<>();
    Map<String, List<Map<String, String>>> timeSlotMap = new HashMap<>();

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

      String timeSlotValue = timeSlotMapping.getOrDefault(
              assignment.getTimeSlot(),
              assignment.getTimeSlot()
      );

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

    // Sort time slots chronologically using the predefined order
    List<Map<String, Object>> timeSlots = timeSlotMap.entrySet().stream()
            .sorted((e1, e2) -> {
              // Get the original slot letters for comparison
              String slot1 = timeRangeToSlot.getOrDefault(e1.getKey(), e1.getKey());
              String slot2 = timeRangeToSlot.getOrDefault(e2.getKey(), e2.getKey());
              // Compare based on natural order of slot letters (A, B, C, D, E)
              return slot1.compareTo(slot2);
            })
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
  private static final List<String> HEADERS = Arrays.asList("Zeit", "Raum", "Veranstaltung", "Beschreibung", "Wunsch");

  /**
   * Exports the provided data to an Excel file.
   *
   * @param filePath The path where the Excel file will be saved.
   * @param data     The data to be exported.
   * @throws IOException If an I/O error occurs.
   *
   * @author leon
   */
  public void exportChoiceData(String filePath, List<Map<String, Object>> data) throws IOException {
    if (data.isEmpty()) {
      throw new IllegalArgumentException("Data list must not be empty.");
    }

    // Group data by class and name
    Map<String, Map<String, List<Map<String, Object>>>> groupedData = groupDataByClassAndName(data);

    // Export the grouped data to an Excel file
    exportDataToExcelForChoices(groupedData, filePath);
  }

  /**
   * Groups the data by class and name.
   *
   * @param data The data to be grouped.
   * @return A map containing the grouped data.
   *
   * @author leon
   */
  private Map<String, Map<String, List<Map<String, Object>>>> groupDataByClassAndName(List<Map<String, Object>> data) {
    Map<String, Map<String, List<Map<String, Object>>>> groupedData = new LinkedHashMap<>();
    for (Map<String, Object> row : data) {
      String klasse = (String) row.get("Klasse");
      String name = (String) row.get("Name");

      // Remove the last two columns (Name and Klasse) from the data rows
      Map<String, Object> cleanedRow = new LinkedHashMap<>(row);
      cleanedRow.remove("Name");
      cleanedRow.remove("Klasse");

      groupedData.computeIfAbsent(klasse, k -> new LinkedHashMap<>())
              .computeIfAbsent(name, n -> new ArrayList<>())
              .add(cleanedRow);
    }
    return groupedData;
  }

  /**
   * Exports the grouped data to an Excel file.
   *
   * @param groupedData The grouped data to be exported.
   * @param filePath    The path where the Excel file will be saved.
   * @throws IOException If an I/O error occurs.
   *
   * @author leon
   */
  private void exportDataToExcelForChoices(Map<String, Map<String, List<Map<String, Object>>>> groupedData, String filePath) throws IOException {
    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Data");

      // Create styles for headers and data
      CellStyle headerStyle = createHeaderStyleForChoices(workbook);
      CellStyle dataStyle = createDataStyleForChoices(workbook);
      CellStyle grayCellStyle = createGrayCellStyleForChoices(workbook);

      int rowIndex = 0;

      // Iterate through the grouped data
      for (Map.Entry<String, Map<String, List<Map<String, Object>>>> klasseEntry : groupedData.entrySet()) {
        String klasse = klasseEntry.getKey();
        Map<String, List<Map<String, Object>>> nameMap = klasseEntry.getValue();

        // Write class as a header
        rowIndex = writeClassHeaderForChoices(sheet, klasse, headerStyle, rowIndex);

        // Iterate through the names
        for (Map.Entry<String, List<Map<String, Object>>> nameEntry : nameMap.entrySet()) {
          String name = nameEntry.getKey();
          List<Map<String, Object>> rows = nameEntry.getValue();

          // Write name as a header
          rowIndex = writeNameHeaderForChoices(sheet, name, headerStyle, rowIndex);

          // Write column headers
          rowIndex = writeColumnHeadersForChoices(sheet, headerStyle, rowIndex);

          // Color the cell at A3 gray
          rowIndex = colorA3CellGrayForChoices(sheet, grayCellStyle, rowIndex);

          // Write data rows
          rowIndex = writeDataRowsForChoices(sheet, rows, dataStyle, rowIndex);

          // Add an empty row between names
          rowIndex++;
        }
      }

      // Adjust column widths
      autoSizeColumnsForChoices(sheet);

      // Manually adjust the "Beschreibung" column width
      adjustBeschreibungColumnWidthForChoices(sheet);

      // Save the file
      saveWorkbook(workbook, filePath);
    }
  }

  /**
   * Creates a style for the headers.
   *
   * @param workbook The workbook to create the style in.
   * @return The created header style.
   *
   * @author leon
   */
  private CellStyle createHeaderStyleForChoices(Workbook workbook) {
    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerFont.setFontHeightInPoints((short) 12);

    CellStyle headerStyle = workbook.createCellStyle();
    headerStyle.setFont(headerFont);
    headerStyle.setAlignment(HorizontalAlignment.CENTER);
    headerStyle.setBorderTop(BorderStyle.THIN);
    headerStyle.setBorderBottom(BorderStyle.THIN);
    headerStyle.setBorderLeft(BorderStyle.THIN);
    headerStyle.setBorderRight(BorderStyle.THIN);
    headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

    return headerStyle;
  }

  /**
   * Creates a style for the data cells.
   *
   * @param workbook The workbook to create the style in.
   * @return The created data style.
   *
   * @author leon
   */
  private CellStyle createDataStyleForChoices(Workbook workbook) {
    Font dataFont = workbook.createFont();
    dataFont.setFontHeightInPoints((short) 12);

    CellStyle dataStyle = workbook.createCellStyle();
    dataStyle.setFont(dataFont);
    dataStyle.setBorderTop(BorderStyle.THIN);
    dataStyle.setBorderBottom(BorderStyle.THIN);
    dataStyle.setBorderLeft(BorderStyle.THIN);
    dataStyle.setBorderRight(BorderStyle.THIN);

    return dataStyle;
  }

  /**
   * Creates a style for the gray cell at A3.
   *
   * @param workbook The workbook to create the style in.
   * @return The created gray cell style.
   *
   * @author leon
   */
  private CellStyle createGrayCellStyleForChoices(Workbook workbook) {
    Font dataFont = workbook.createFont();
    dataFont.setFontHeightInPoints((short) 12);

    CellStyle grayCellStyle = workbook.createCellStyle();
    grayCellStyle.setFont(dataFont);
    grayCellStyle.setBorderTop(BorderStyle.THIN);
    grayCellStyle.setBorderBottom(BorderStyle.THIN);
    grayCellStyle.setBorderLeft(BorderStyle.THIN);
    grayCellStyle.setBorderRight(BorderStyle.THIN);
    grayCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    grayCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

    return grayCellStyle;
  }

  /**
   * Writes the class header to the sheet.
   *
   * @param sheet      The sheet to write to.
   * @param klasse     The class name.
   * @param headerStyle The style for the header.
   * @param rowIndex   The current row index.
   * @return The updated row index.
   *
   * @author leon
   */
  private int writeClassHeaderForChoices(Sheet sheet, String klasse, CellStyle headerStyle, int rowIndex) {
    Row klasseRow = sheet.createRow(rowIndex++);
    Cell klasseCell = klasseRow.createCell(0);
    klasseCell.setCellValue(klasse);
    klasseCell.setCellStyle(headerStyle);
    return rowIndex;
  }

  /**
   * Writes the name header to the sheet.
   *
   * @param sheet      The sheet to write to.
   * @param name       The name.
   * @param headerStyle The style for the header.
   * @param rowIndex   The current row index.
   * @return The updated row index.
   *
   * @author leon
   */
  private int writeNameHeaderForChoices(Sheet sheet, String name, CellStyle headerStyle, int rowIndex) {
    Row nameRow = sheet.createRow(rowIndex++);
    Cell nameCell = nameRow.createCell(0);
    nameCell.setCellValue(name);
    nameCell.setCellStyle(headerStyle);
    return rowIndex;
  }

  /**
   * Writes the column headers to the sheet.
   *
   * @param sheet      The sheet to write to.
   * @param headerStyle The style for the headers.
   * @param rowIndex   The current row index.
   * @return The updated row index.
   *
   * @author leon
   */
  private int writeColumnHeadersForChoices(Sheet sheet, CellStyle headerStyle, int rowIndex) {
    Row headerRow = sheet.createRow(rowIndex++);
    for (int i = 0; i < HEADERS.size(); i++) {
      Cell cell = headerRow.createCell(i + 1);
      cell.setCellValue(HEADERS.get(i));
      cell.setCellStyle(headerStyle);
    }
    return rowIndex;
  }

  /**
   * Colors the cell at A3 gray.
   *
   * @param sheet         The sheet to write to.
   * @param grayCellStyle The style for the gray cell.
   * @param rowIndex      The current row index.
   * @return The updated row index.
   *
   * @author leon
   */
  private int colorA3CellGrayForChoices(Sheet sheet, CellStyle grayCellStyle, int rowIndex) {
    Row headerRow = sheet.getRow(rowIndex - 1); // Get the header row
    Cell a3Cell = headerRow.createCell(0); // First column (A3)
    a3Cell.setCellStyle(grayCellStyle); // Gray background, but empty
    return rowIndex;
  }

  /**
   * Writes the data rows to the sheet.
   *
   * @param sheet     The sheet to write to.
   * @param rows      The data rows.
   * @param dataStyle The style for the data cells.
   * @param rowIndex  The current row index.
   * @return The updated row index.
   *
   * @author leon
   */
  private int writeDataRowsForChoices(Sheet sheet, List<Map<String, Object>> rows, CellStyle dataStyle, int rowIndex) {
    char rowLabel = 'A'; // Start with 'A'
    for (Map<String, Object> row : rows) {
      Row dataRow = sheet.createRow(rowIndex++);
      Cell labelCell = dataRow.createCell(0); // Row label in the first column
      labelCell.setCellValue(String.valueOf(rowLabel)); // Row label (A, B, C, ...)
      labelCell.setCellStyle(dataStyle);

      int cellIndex = 1; // Start at 1, since the first column contains the row label
      for (String header : HEADERS) {
        Cell cell = dataRow.createCell(cellIndex++);
        Object value = row.get(header);
        cell.setCellValue(value != null ? value.toString() : "");
        cell.setCellStyle(dataStyle);
      }
      rowLabel++; // Next letter
    }
    return rowIndex;
  }

  /**
   * Automatically adjusts the column widths.
   *
   * @param sheet The sheet to adjust.
   *
   * @author leon
   */
  private void autoSizeColumnsForChoices(Sheet sheet) {
    for (int i = 0; i < HEADERS.size() + 1; i++) { // +1 for the row label column
      sheet.autoSizeColumn(i);
    }
  }

  /**
   * Manually adjusts the width of the "Beschreibung" column.
   *
   * @param sheet The sheet to adjust.
   *
   * @author leon
   */
  private void adjustBeschreibungColumnWidthForChoices(Sheet sheet) {
    int beschreibungIndex = HEADERS.indexOf("Beschreibung") + 1; // +1 for the row label column
    if (beschreibungIndex != -1) {
      sheet.setColumnWidth(beschreibungIndex, 20000); // Width for the "Beschreibung" column
    }
  }

  /**
   * The file path where the exported Excel file will be saved for Choices.
   *
   */
  private String filePathChoices = "EXPORT BOT6 Laufzettel";

  /**
   * Returns the file path to which the data will be exported for the Choices.
   *
   * @return The file path as a string.
   *
   * @author leon
   */
  public String getFilePathChoices() {
        return filePathChoices;
  }


  /**
   * Prepares data for export in routing slip format by converting StudentAssignment objects
   * into a structured map with time slots converted to time ranges.
   *
   * @param dataToExport List of objects to be exported (expected to contain StudentAssignment instances)
   * @return Map containing the prepared export data under the "data" key
   *
   * @author leon
   */
  public Map<String, Object> prepareDataForExportForRoutingSlip(List<Object> dataToExport) {
    // Initialize list to hold all converted assignment records
    List<Map<String, Object>> exportData = new ArrayList<>();

    // Process each item in the input list
    for (Object item : dataToExport) {
      // Only process StudentAssignment objects
      if (item instanceof StudentAssignment) {
        StudentAssignment assignment = (StudentAssignment) item;

        // Create a new row with ordered columns for the export
        Map<String, Object> row = new LinkedHashMap<>();

        // Convert time slot to time range
        String timeRange;
        switch(assignment.getTimeSlot()) {
          case "A": timeRange = "08:00-08:45"; break;
          case "B": timeRange = "08:45-09:30"; break;
          case "C": timeRange = "09:50-10:35"; break;
          case "D": timeRange = "10:35-11:20"; break;
          case "E": timeRange = "11:40-12:25"; break;
          case "F": timeRange = "12:25-13:10"; break;
          default: timeRange = assignment.getTimeSlot(); // Return original if no match
        }

        row.put("Zeit", timeRange);
        row.put("Raum", assignment.getRoomId());
        row.put("Veranstaltung", assignment.getCompanyName());
        row.put("Beschreibung", assignment.getSubject());
        row.put("Wunsch", assignment.getChoiceNo());
        row.put("Name", assignment.getLastName() + ", " + assignment.getFirstName());
        row.put("Klasse", assignment.getClassRef());

        exportData.add(row);
      }
    }

    // Return the data in a structured map (grouping happens later during export)
    Map<String, Object> result = new HashMap<>();
    result.put("data", exportData);
    return result;
  }


}