package com.openjfx.config;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

/**
 * Configuration class for database connection and schema initialization. Uses H2 database with
 * HikariCP connection pooling.
 */
public class DatabaseConfig {

  /**
   * Directory where database files will be stored
   */
  private static final String DB_DIR = "data";
  /**
   * Name of the database
   */
  private static final String DB_NAME = "database";
  /**
   * JDBC URL for H2 database connection
   */
  private static final String DB_URL = "jdbc:h2:./" + DB_DIR + "/" + DB_NAME;
  /**
   * HikariCP datasource for connection pooling
   */
  private static HikariDataSource dataSource;

  /**
   * Static initializer block that calls initializeDatabase() when the class is loaded
   */
  static {
    initializeDatabase();
  }

  /**
   * Initializes the database by creating necessary directories and tables. Sets up HikariCP
   * connection pool and creates the following tables if they don't exist: - events: Stores event
   * information - rooms: Stores room information - choices: Stores student choices - assignments:
   * Tracks student-event assignments
   *
   * @throws RuntimeException if database initialization fails
   */
  public static void initializeDatabase() {
    // Create data directory if it doesn't exist
    File dbDir = new File(DB_DIR);
    if (!dbDir.exists()) {
      dbDir.mkdirs();
    }

    // Configure connection pool
    HikariConfig config = getHikariConfig();

    dataSource = new HikariDataSource(config);

    // Initialize database schema
    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement()) {

      // Events table based on Event model
      stmt.execute(
          "CREATE TABLE IF NOT EXISTS events (" +
              "    id INTEGER PRIMARY KEY," +
              "    company VARCHAR(255) NOT NULL," +
              "    subject VARCHAR(255) NOT NULL," +
              "    max_participants INTEGER NOT NULL," +
              "    min_participants INTEGER NOT NULL," +
              "    earliest_start VARCHAR(50) NOT NULL" +
              ")");

      // Rooms table based on Room model
      stmt.execute(
          "CREATE TABLE IF NOT EXISTS rooms (" +
              "    name VARCHAR(255) PRIMARY KEY," +
              "    capacity INTEGER NOT NULL" +
              ")");

      // Choices table based on Choice model
      stmt.execute(
          "CREATE TABLE IF NOT EXISTS choices (" +
              "    class_ref VARCHAR(50) NOT NULL," +
              "    first_name VARCHAR(255) NOT NULL," +
              "    last_name VARCHAR(255) NOT NULL," +
              "    choice1 VARCHAR(50)," +
              "    choice2 VARCHAR(50)," +
              "    choice3 VARCHAR(50)," +
              "    choice4 VARCHAR(50)," +
              "    choice5 VARCHAR(50)," +
              "    choice6 VARCHAR(50)," +
              "    PRIMARY KEY (class_ref)" +
              ")");

      // Assignments table for tracking student-event assignments
      stmt.execute(
          "CREATE TABLE IF NOT EXISTS assignments (" +
              "    event_id INTEGER NOT NULL," +
              "    student_id VARCHAR(50) NOT NULL," +
              "    choice_priority INTEGER NOT NULL," +
              "    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
              "    PRIMARY KEY (event_id, student_id)," +
              "    FOREIGN KEY (event_id) REFERENCES events(id)," +
              "    FOREIGN KEY (student_id) REFERENCES choices(class_ref)" +
              ")");


    } catch (SQLException e) {
      throw new RuntimeException("Failed to initialize database", e);
    }
  }

  /**
   * Returns a HikariConfig object with the necessary configuration for the H2 database.
   *
   * @return HikariConfig object with database configuration
   */
  private static @NotNull HikariConfig getHikariConfig() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(DB_URL);
    config.setUsername("thanos");
    config.setPassword("infinitystones");
    config.setMaximumPoolSize(10);
    config.setMinimumIdle(5);
    config.setIdleTimeout(300000);
    config.setConnectionTimeout(20000);
    config.setAutoCommit(true);
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    config.addDataSourceProperty("useServerPrepStmts", "true");
    config.addDataSourceProperty("useLocalSessionState", "true");
    config.addDataSourceProperty("rewriteBatchedStatements", "true");
    config.addDataSourceProperty("cacheResultSetMetadata", "true");
    config.addDataSourceProperty("elideSetAutoCommits", "true");
    config.addDataSourceProperty("maintainTimeStats", "false");
    return config;
  }

  /**
   * Gets a connection from the connection pool.
   *
   * @return Connection object from the pool
   * @throws SQLException if a database access error occurs
   */
  public static Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

  /**
   * Closes the datasource and releases all resources. Should be called when shutting down the
   * application.
   */
  public static void closeDataSource() {
    if (dataSource != null) {
      dataSource.close();
    }
  }
}
