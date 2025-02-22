package com.openjfx.services;

import com.openjfx.models.Room;
import java.sql.Statement;
import java.util.Map;
import com.openjfx.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for handling Room-related Excel operations. This class extends AbstractExcelService
 * to provide specific functionality for reading and writing Room data from/to Excel files.
 *
 * <p>The service maps Excel columns to Room properties using German column headers:
 * <ul>
 *   <li>"raum" → Room name</li>
 *   <li>"kapazität" → Room capacity</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * RoomService roomService = new RoomService(new ExcelService());
 * List<Room> rooms = roomService.loadFromExcel("path/to/excel.xlsx");
 * </pre>
 */
public class RoomService extends AbstractExcelService<Room> {

  /**
   * Constructs a new RoomService with the specified Excel service.
   *
   * @param excelService the Excel service to use for file operations
   */
  public RoomService(ExcelService excelService) {
    super(excelService);
  }

  /**
   * Defines the mapping between internal property names and Excel column prefixes. The column
   * prefixes are case-insensitive partial matches for Excel column headers.
   *
   * @return a Map containing the property-to-column prefix mappings
   */
  @Override
  protected Map<String, String> getColumnPrefixes() {
    return Map.of(
        "name", "raum",
        "capacity", "kapazität"
    );
  }

  /**
   * Creates a Room object from a row of Excel data.
   *
   * <p>Required fields are:
   * <ul>
   *   <li>Name (string)</li>
   *   <li>Capacity (numeric)</li>
   * </ul>
   *
   * @param row            the row data from Excel
   * @param columnMappings the mappings between internal names and actual Excel columns
   * @return a new Room object, or null if the row data is invalid
   */
  @Override
  protected Room createModelFromRow(Map<String, String> row, Map<String, String> columnMappings) {
    String name = row.get(columnMappings.get("name"));
    String capacityStr = row.get(columnMappings.get("capacity"));

    if (name == null || capacityStr == null) {
      System.err.println("Skipping row due to null values: " + row);
      return null;
    }

    try {
      return new Room(
          name.trim(),
          Integer.parseInt(capacityStr.trim())
      );
    } catch (NumberFormatException e) {
      System.err.println("Error parsing row: " + row + " - " + e.getMessage());
      return null;
    }
  }

  /**
   * Converts a Room object to a map of column names and values for Excel export.
   *
   * @param room the Room object to convert
   * @return a Map containing the column names and values for Excel export
   */
  @Override
  protected Map<String, Object> convertModelToRow(Room room) {
    return Map.of(
        "Raum", room.getName(),
        "Kapazität", room.getCapacity()
    );
  }

  /**
   * Saves a Room object to the database.
   *
   * @param room the Room object to save
   */
  public void saveRoom(Room room) {
    String sql = "INSERT INTO rooms ("
        + "name, "
        + "capacity) "
        + "VALUES (?, ?)";

    try (Connection conn = DatabaseConfig.getConnection()) {
      conn.setAutoCommit(false);

      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, room.getName());
        pstmt.setInt(2, room.getCapacity());

        int result = pstmt.executeUpdate();
        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        System.err.println("Error saving room, transaction rolled back: " + e.getMessage());
        e.printStackTrace();
      }
    } catch (SQLException e) {
      System.err.println("Database connection error: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Deletes all rooms from the database.
   */
  public void clearRooms() {
    String delete = "DELETE FROM rooms";

    try (Connection conn = DatabaseConfig.getConnection();
      Statement stmt = conn.createStatement()) {
      conn.setAutoCommit(false);
      stmt.executeUpdate(delete);
      conn.commit();
    } catch (SQLException e) {
      System.err.println("Error clearing events: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Loads all rooms from the database.
   *
   * @return a list of Room objects
   */
  public List<Room> loadRooms() {
    String sql = "SELECT * FROM rooms";
    List<Room> rooms = new ArrayList<>();

    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        String name = rs.getString("name");
        int capacity = rs.getInt("capacity");

        Room room = new Room(name, capacity);
        rooms.add(room);
      }

    } catch (SQLException e) {
      System.err.println("Database connection error: " + e.getMessage());
      e.printStackTrace();
    }
    return rooms;
  }

}