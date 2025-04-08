package com.openjfx.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for the TimeSlotInitializer class.
 *
 * @author mian
 */
class TimeSlotInitializerTest {

  @Mock
  private Connection mockConnection;
  @Mock
  private PreparedStatement mockPreparedStatement;
  @Mock
  private ResultSet mockResultSet;

  private TimeSlotInitializer timeSlotInitializer;

  @BeforeEach
  void setUp() throws SQLException {
    MockitoAnnotations.openMocks(this);
    timeSlotInitializer = TimeSlotInitializer.getInstance();
    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
  }

  @Test
  void initialize_whenTimeSlotsAlreadyPopulated_shouldNotInsertTimeSlots() throws SQLException {
    when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true);
    when(mockResultSet.getInt(1)).thenReturn(5);

    timeSlotInitializer.initialize();

    verify(mockPreparedStatement, never()).executeBatch();
  }

  @Test
  void initialize_whenSQLExceptionThrown_shouldPrintErrorMessage() throws SQLException {
    when(mockConnection.prepareStatement(anyString())).thenThrow(
        new SQLException("Database error"));

    assertDoesNotThrow(() -> timeSlotInitializer.initialize());
  }

  @Test
  void getDefaultTimeSlots_shouldReturnListOfTimeSlots() {
    assertEquals(5, timeSlotInitializer.getDefaultTimeSlots().size());
  }

  @Test
  void insertTimeSlots_shouldExecuteBatch() throws SQLException {
    timeSlotInitializer.insertTimeSlots(mockConnection);

    verify(mockPreparedStatement).executeBatch();
  }

  @Test
  void getInstance_shouldReturnSingletonInstance() {
    TimeSlotInitializer instance1 = TimeSlotInitializer.getInstance();
    TimeSlotInitializer instance2 = TimeSlotInitializer.getInstance();

    assertSame(instance1, instance2);
  }
}