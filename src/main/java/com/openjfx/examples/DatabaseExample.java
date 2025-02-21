package com.openjfx.examples;

import com.openjfx.config.DatabaseConfig;
import com.openjfx.models.Event;
import com.openjfx.models.Room;

import java.sql.*;

public class DatabaseExample {

  public static void main(String[] args) {
    try {
      // Initialize database
      DatabaseConfig.initializeDatabase();

      // Insert example data
      insertEvent(new Event(1, "Tech Corp", "Java Workshop", 30, 10, "09:00"));
      insertRoom(new Room("Room A101", 30));

      // Read data
      System.out.println("Reading events:");
      readEvents();
      System.out.println("\nReading rooms:");
      readRooms();

      // Update data
      updateEventMaxParticipants(1, 40);
      updateRoomCapacity("Room A101", 40);

      // Read updated data
      System.out.println("\nAfter updates:");
      readEvents();
      readRooms();

      // Clean up example data
      System.out.println("\nCleaning up example data...");
      deleteEvent(1);
      deleteRoom("Room A101");

      // Verify cleanup
      System.out.println("\nAfter cleanup:");
      readEvents();
      readRooms();

      // Close connection pool
      DatabaseConfig.closeDataSource();

    } catch (SQLException e) {
      System.err.println("Database error: " + e.getMessage());
    }
  }

  private static void insertEvent(Event event) throws SQLException {
    String sql = "INSERT INTO events (id, company, subject, max_participants, min_participants, earliest_start) VALUES (?, ?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, event.getId());
      pstmt.setString(2, event.getCompany());
      pstmt.setString(3, event.getSubject());
      pstmt.setInt(4, event.getMaxParticipants());
      pstmt.setInt(5, event.getMinParticipants());
      pstmt.setString(6, event.getEarliestStart());
      pstmt.executeUpdate();
    }
  }

  private static void insertRoom(Room room) throws SQLException {
    String sql = "INSERT INTO rooms (name, capacity) VALUES (?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, room.getName());
      pstmt.setInt(2, room.getCapacity());
      pstmt.executeUpdate();
    }
  }

  private static void readEvents() throws SQLException {
    String sql = "SELECT * FROM events";
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        System.out.printf("Event: %d, %s, %s, Max: %d, Min: %d, Start: %s%n",
            rs.getInt("id"),
            rs.getString("company"),
            rs.getString("subject"),
            rs.getInt("max_participants"),
            rs.getInt("min_participants"),
            rs.getString("earliest_start"));
      }
    }
  }

  private static void readRooms() throws SQLException {
    String sql = "SELECT * FROM rooms";
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        System.out.printf("Room: %s, Capacity: %d%n",
            rs.getString("name"),
            rs.getInt("capacity"));
      }
    }
  }

  private static void updateEventMaxParticipants(int id, int newMax) throws SQLException {
    String sql = "UPDATE events SET max_participants = ? WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, newMax);
      pstmt.setInt(2, id);
      pstmt.executeUpdate();
    }
  }

  private static void updateRoomCapacity(String name, int newCapacity) throws SQLException {
    String sql = "UPDATE rooms SET capacity = ? WHERE name = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, newCapacity);
      pstmt.setString(2, name);
      pstmt.executeUpdate();
    }
  }

  private static void deleteEvent(int id) throws SQLException {
    String sql = "DELETE FROM events WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, id);
      pstmt.executeUpdate();
    }
  }

  private static void deleteRoom(String name) throws SQLException {
    String sql = "DELETE FROM rooms WHERE name = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, name);
      pstmt.executeUpdate();
    }
  }
}