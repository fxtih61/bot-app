package com.openjfx.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Tests for the DatabaseConfig class.
 *
 * @author mian
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DatabaseConfigTest {

  @BeforeAll
  public void setup() {
    DatabaseConfig.initializeDatabase();
  }

  @Test
  @Order(1)
  public void testEventsTableExists() throws SQLException {
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement()) {
      assertTrue(stmt.execute("SELECT 1 FROM events"), "Events table should exist");
    }
  }

  @Test
  @Order(2)
  public void testRoomsTableExists() throws SQLException {
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement()) {
      assertTrue(stmt.execute("SELECT 1 FROM rooms"), "Rooms table should exist");
    }
  }

  @Test
  @Order(3)
  public void testChoicesTableExists() throws SQLException {
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement()) {
      assertTrue(stmt.execute("SELECT 1 FROM choices"), "Choices table should exist");
    }
  }

  @Test
  @Order(4)
  public void testTimeslotsTableExists() throws SQLException {
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement()) {
      assertTrue(stmt.execute("SELECT 1 FROM timeslots"), "Timeslots table should exist");
    }
  }

  @Test
  @Order(5)
  public void testTimetableAssignmentsTableExists() throws SQLException {
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement()) {
      assertTrue(stmt.execute("SELECT 1 FROM timetable_assignments"),
          "Timetable assignments table should exist");
    }
  }

  @Test
  @Order(6)
  public void testStudentAssignmentsTableExists() throws SQLException {
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement()) {
      assertTrue(stmt.execute("SELECT 1 FROM student_assignments"),
          "Student assignments table should exist");
    }
  }

  @Test
  @Order(7)
  public void testWorkshopDemandTableExists() throws SQLException {
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement()) {
      assertTrue(stmt.execute("SELECT 1 FROM workshop_demand"),
          "Workshop demand table should exist");
    }
  }

  @Test
  @Order(8)
  public void initializeDatabase_createsTablesSuccessfully() throws SQLException {
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement()) {
      assertTrue(stmt.execute("SELECT 1 FROM events"));
      assertTrue(stmt.execute("SELECT 1 FROM rooms"));
      assertTrue(stmt.execute("SELECT 1 FROM choices"));
      assertTrue(stmt.execute("SELECT 1 FROM timeslots"));
      assertTrue(stmt.execute("SELECT 1 FROM timetable_assignments"));
      assertTrue(stmt.execute("SELECT 1 FROM student_assignments"));
      assertTrue(stmt.execute("SELECT 1 FROM workshop_demand"));

    }
  }

  @Test
  @Order(9)
  public void getConnection_returnsValidConnection() throws SQLException {
    try (Connection conn = DatabaseConfig.getConnection()) {
      assertNotNull(conn);
      assertFalse(conn.isClosed());
    }
  }

  @Test
  @Order(10)
  public void closeDataSource_closesConnectionPool() throws SQLException {
    DatabaseConfig.closeDataSource();
    assertThrows(SQLException.class, DatabaseConfig::getConnection);
  }
}