package com.openjfx.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Initializes time slots in the database when the application starts.
 * This class follows the singleton pattern to ensure the initialization happens only once.
 * @author mian
 */
public class TimeSlotInitializer {

  private static final TimeSlotInitializer INSTANCE = new TimeSlotInitializer();
  private boolean initialized = false;

  /**
   * Private constructor to prevent instantiation
   * @author mian
   */
  private TimeSlotInitializer() {}

  /**
   * Get the singleton instance
   * @return TimeSlotInitializer instance
   * @author mian
   *
   */
  public static TimeSlotInitializer getInstance() {
    return INSTANCE;
  }

  /**
   * Initialize time slots in the database.
   * This method checks if time slots are already present, and if not, inserts them.
   * @author mian
   */
  public void initialize() {
    if (initialized) {
      return;
    }

    try (Connection conn = DatabaseConfig.getConnection()) {
      // Check if time slots are already populated
      if (!isTimeSlotTablePopulated(conn)) {
        insertTimeSlots(conn);
        System.out.println("Time slots initialized successfully");
      } else {
        System.out.println("Time slots already exist, skipping initialization");
      }
      initialized = true;
    } catch (SQLException e) {
      System.err.println("Failed to initialize time slots: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Check if the time slot table already has data
   * @param conn Database connection
   * @return true if the table has records, false otherwise
   * @throws SQLException if a database error occurs
   * @author mian
   */
  boolean isTimeSlotTablePopulated(Connection conn) throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM timeslots")) {
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return rs.getInt(1) > 0;
      }
    }
    return false;
  }

  /**
   * Insert predefined time slots into the database
   * @param conn Database connection
   * @throws SQLException if a database error occurs
   * @author mian
   */
  void insertTimeSlots(Connection conn) throws SQLException {
    List<TimeSlot> timeSlots = getDefaultTimeSlots();

    String sql = "INSERT INTO timeslots (start_time, end_time, slot) VALUES (?, ?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      for (TimeSlot slot : timeSlots) {
        stmt.setString(1, slot.startTime);
        stmt.setString(2, slot.endTime);
        stmt.setString(3, slot.slot);
        stmt.addBatch();
      }
      stmt.executeBatch();
    }
  }

  /**
   * Create a list of default time slots
   * @return List of TimeSlot objects
   * @author mian
   */
  List<TimeSlot> getDefaultTimeSlots() {
    List<TimeSlot> slots = new ArrayList<>();

    slots.add(new TimeSlot("8:45", "9:30", "A"));
    slots.add(new TimeSlot("9:50", "10:35", "B"));
    slots.add(new TimeSlot("10:35", "11:20", "C"));
    slots.add(new TimeSlot("11:40", "12:25", "D"));
    slots.add(new TimeSlot("12:25", "13:10", "E"));

    return slots;
  }

  /**
   * Simple class to hold time slot data
   * @author mian
   */
  private static class TimeSlot {
    String startTime;
    String endTime;
    String slot;

    TimeSlot(String startTime, String endTime, String slot) {
      this.startTime = startTime;
      this.endTime = endTime;
      this.slot = slot;
    }
  }
}