package com.openjfx.services;

import com.openjfx.config.DatabaseConfig;
import com.openjfx.models.TimeSlot;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for loading and managing time slots.
 *
 * @author mian
 */
public class TimeSlotService {

  /**
   * Loads time slots from the database.
   *
   * @return a list of time slots
   * @author mian
   */
  public List<TimeSlot> loadTimeSlots() {
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
}