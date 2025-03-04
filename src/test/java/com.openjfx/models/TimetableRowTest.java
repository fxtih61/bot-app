package com.openjfx.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the TimetableRow class.
 *
 * @author mian
 */

public class TimetableRowTest {

  @Test
  void constructor_initializesCorrectly() {
    TimetableRow row = new TimetableRow("08:00-09:00", "RoomA", 50, 101, "CompanyX", "Math");
    assertEquals("08:00-09:00", row.getTimeSlot());
    assertEquals("RoomA", row.getRoomName());
    assertEquals(50, row.getRoomCapacity());
    assertEquals(101, row.getEventId());
    assertEquals("CompanyX", row.getCompany());
    assertEquals("Math", row.getSubject());
  }

  @Test
  void setTimeSlot_updatesTimeSlot() {
    TimetableRow row = new TimetableRow("08:00-09:00", "RoomA", 50, 101, "CompanyX", "Math");
    row.setTimeSlot("09:00-10:00");
    assertEquals("09:00-10:00", row.getTimeSlot());
  }

  @Test
  void setRoomName_updatesRoomName() {
    TimetableRow row = new TimetableRow("08:00-09:00", "RoomA", 50, 101, "CompanyX", "Math");
    row.setRoomName("RoomB");
    assertEquals("RoomB", row.getRoomName());
  }

  @Test
  void setRoomCapacity_updatesRoomCapacity() {
    TimetableRow row = new TimetableRow("08:00-09:00", "RoomA", 50, 101, "CompanyX", "Math");
    row.setRoomCapacity(100);
    assertEquals(100, row.getRoomCapacity());
  }

  @Test
  void setEventId_updatesEventId() {
    TimetableRow row = new TimetableRow("08:00-09:00", "RoomA", 50, 101, "CompanyX", "Math");
    row.setEventId(102);
    assertEquals(102, row.getEventId());
  }

  @Test
  void setCompany_updatesCompany() {
    TimetableRow row = new TimetableRow("08:00-09:00", "RoomA", 50, 101, "CompanyX", "Math");
    row.setCompany("CompanyY");
    assertEquals("CompanyY", row.getCompany());
  }

  @Test
  void setSubject_updatesSubject() {
    TimetableRow row = new TimetableRow("08:00-09:00", "RoomA", 50, 101, "CompanyX", "Math");
    row.setSubject("Science");
    assertEquals("Science", row.getSubject());
  }
}