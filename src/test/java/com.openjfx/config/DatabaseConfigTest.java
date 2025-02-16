package com.openjfx.config;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DatabaseConfigTest {

  @BeforeAll
  public void setup() {
    DatabaseConfig.initializeDatabase();
  }

  @Test
  @Order(1)
  public void initializeDatabase_createsTablesSuccessfully() throws SQLException {
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement()) {
      assertTrue(stmt.execute("SELECT 1 FROM events"));
      assertTrue(stmt.execute("SELECT 1 FROM rooms"));
      assertTrue(stmt.execute("SELECT 1 FROM choices"));
      assertTrue(stmt.execute("SELECT 1 FROM assignments"));
    }
  }

  @Test
  @Order(2)
  public void getConnection_returnsValidConnection() throws SQLException {
    try (Connection conn = DatabaseConfig.getConnection()) {
      assertNotNull(conn);
      assertFalse(conn.isClosed());
    }
  }

  @Test
  @Order(3)
  public void closeDataSource_closesConnectionPool() throws SQLException {
    DatabaseConfig.closeDataSource();
    assertThrows(SQLException.class, DatabaseConfig::getConnection);
  }
}