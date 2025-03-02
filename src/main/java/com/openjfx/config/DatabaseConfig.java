package com.openjfx.config;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.tools.Server;
import org.jetbrains.annotations.NotNull;

/**
 * Configuration class for database connection and schema initialization. Uses H2 database with
 * HikariCP connection pooling.
 * @author mian
 */
public class DatabaseConfig {

  /**
   * H2 TCP Server instance
   */
  private static Server h2TcpServer;
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
  private static final String DB_URL = "jdbc:h2:tcp://localhost:9092/./" + DB_DIR + "/" + DB_NAME;
  /**
   * HikariCP datasource for connection pooling
   */
  private static HikariDataSource dataSource;

  /**
   * Static initializer block that calls initializeDatabase() when the class is loaded
   * @author mian
   */
  static {
    initializeDatabase();
  }

  /**
   * Initializes the database by creating necessary directories and tables. Sets up HikariCP
   * connection pooling and creates tables for events, rooms, choices, and assignments.
   *
   * @throws RuntimeException if database initialization fails
   * @author mian
   */
  public static void initializeDatabase() {
    // Create data directory if it doesn't exist
    File dbDir = new File(DB_DIR);
    if (!dbDir.exists()) {
      dbDir.mkdirs();
    }

    try {
      // Start TCP server only if not already running
      h2TcpServer = Server.createTcpServer(
          "-tcpPort", "9092",
          "-tcpAllowOthers",
          "-tcpDaemon",
          "-ifNotExists"
      ).start();
      System.out.println("H2 TCP Server status: " + h2TcpServer.getStatus());
    } catch (SQLException e) {
      System.out.println("H2 TCP Server already running: " + e.getMessage());
    }

    // Configure HikariCP
    HikariConfig config = getHikariConfig();
    dataSource = new HikariDataSource(config);

    // Initialize schema
    try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
      createTables(stmt);

      // Initialize time slots
      TimeSlotInitializer.getInstance().initialize();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to initialize database", e);
    }
  }

  /**
   * Creates tables for events, rooms, choices, and assignments.
   *
   * @param stmt
   * @throws SQLException
   * @author mian
   */
  private static void createTables(Statement stmt) throws SQLException {
    // Events table based on Event model
    stmt.execute(
        "CREATE TABLE IF NOT EXISTS events (" +
            "id INTEGER PRIMARY KEY," +
            "company VARCHAR(255) NULL," +
            "subject VARCHAR(255) NULL," +
            "max_participants INTEGER NULL," +
            "min_participants INTEGER NULL," +
            "earliest_start VARCHAR(50) NULL" +
            ")");

    // Rooms table based on Room model
    stmt.execute(
        "CREATE TABLE IF NOT EXISTS rooms (" +
            "name VARCHAR(255) PRIMARY KEY," +
            "capacity INTEGER NOT NULL" +
            ")");

    // Choices table based on Choice model
    stmt.execute(
        "CREATE TABLE IF NOT EXISTS choices (" +
            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
            "class_ref VARCHAR(50) NOT NULL," +
            "first_name VARCHAR(255) NOT NULL," +
            "last_name VARCHAR(255) NOT NULL," +
            "choice1 VARCHAR(50)," +
            "choice2 VARCHAR(50)," +
            "choice3 VARCHAR(50)," +
            "choice4 VARCHAR(50)," +
            "choice5 VARCHAR(50)," +
            "choice6 VARCHAR(50)" +
            ")");

    // Save the time slots in the database
    stmt.execute(
        "CREATE TABLE IF NOT EXISTS timeslots (" +
            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
            "start_time VARCHAR(50) NOT NULL," +
            "end_time VARCHAR(50) NOT NULL," +
            "slot VARCHAR(50) NOT NULL" +
            ")");

    // Timetable assignments table for tracking event-room-time slot assignments
    stmt.execute(
        "CREATE TABLE IF NOT EXISTS timetable_assignments (" +
            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
            "event_id INTEGER NOT NULL," +
            "room_id VARCHAR(255) NOT NULL," +
            "time_slot VARCHAR(50) NOT NULL" +
            ")");

    // Student assignments table for tracking student-event assignments
    stmt.execute(
        "CREATE TABLE IF NOT EXISTS student_assignments (" +
            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
            "event_id INTEGER NOT NULL," +
            "first_name VARCHAR(255) NOT NULL," +
            "last_name VARCHAR(255) NOT NULL," +
            "class_ref VARCHAR(50) NOT NULL" +
            ")");

    // Workshop demand table for tracking workshop demand per event
    stmt.execute(
        "CREATE TABLE IF NOT EXISTS workshop_demand (" +
            "event_id INTEGER PRIMARY KEY," +
            "demand INTEGER NOT NULL," +
            "FOREIGN KEY (event_id) REFERENCES events(id)" +
            ")");
  }

  /**
   * Creates and configures a HikariCP configuration object with optimized settings.
   *
   * @return Configured HikariConfig instance
   * @throws RuntimeException if configuration creation fails
   *
   * Configuration details:
   * - Database connection:
   *   - jdbcUrl: H2 database URL
   *   - username: Database user
   *   - password: Database password
   *
   * - Connection pool settings:
   *   - maximumPoolSize: Maximum number of connections in the pool (10)
   *   - minimumIdle: Minimum number of idle connections (5)
   *   - idleTimeout: Maximum time a connection can remain idle (300000ms / 5 minutes)
   *   - connectionTimeout: Maximum time to wait for connection (20000ms / 20 seconds)
   *   - autoCommit: Enable automatic transaction commit
   *
   * - Performance optimizations:
   *   - cachePrepStmts: Enable prepared statement caching
   *   - prepStmtCacheSize: Number of prepared statements to cache (250)
   *   - prepStmtCacheSqlLimit: Maximum length of SQL string to cache (2048)
   *   - useServerPrepStmts: Use server-side prepared statements
   *   - useLocalSessionState: Avoid unnecessary round trips to server
   *   - rewriteBatchedStatements: Optimize batch operations
   *   - cacheResultSetMetadata: Cache ResultSet metadata
   *   - elideSetAutoCommits: Optimize autocommit calls
   *   - maintainTimeStats: Disable time statistics tracking
   *   @author mian
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
   * @author mian
   */
  public static Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

  /**
   * Closes the datasource and releases all resources. Should be called when shutting down the
   * application.
   * @author mian
   */
  public static void closeDataSource() {
    if (dataSource != null) {
      dataSource.close();
    }
    if (h2TcpServer != null && h2TcpServer.isRunning(true)) {
      h2TcpServer.stop();
      System.out.println("H2 TCP Server stopped");
    }
  }
}
