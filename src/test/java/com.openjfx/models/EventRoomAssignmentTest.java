package com.openjfx.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * The `EventRoomAssignmentTest` class contains tests for the `EventRoomAssignment` class.
 *
 * @author mian
 */
public class EventRoomAssignmentTest {

  @Test
  void constructorWithNullEvent_initializesCorrectly() {
    Room room = new Room("RoomA", 30);
    EventRoomAssignment assignment = new EventRoomAssignment(null, room);
    assertNull(assignment.getEvent());
    assertEquals(room, assignment.getRoom());
    assertNull(assignment.getCompany());
  }

  @Test
  void setRoom_updatesRoom() {
    Event event = new Event(1, "Event1", "CompanyX", 10, 20, "Description1");
    Room room1 = new Room("RoomA", 30);
    Room room2 = new Room("RoomB", 40);
    EventRoomAssignment assignment = new EventRoomAssignment(event, room1);
    assignment.setRoom(room2);
    assertEquals(room2, assignment.getRoom());
  }

  @Test
  void setTimeSlot_updatesTimeSlot() {
    Event event = new Event(1, "Event1", "CompanyX", 10, 20, "Description1");
    Room room = new Room("RoomA", 30);
    EventRoomAssignment assignment = new EventRoomAssignment(event, room);
    assignment.setTimeSlot("09:00-10:00");
    assertEquals("09:00-10:00", assignment.getTimeSlot());
  }

  @Test
  void setCompany_updatesCompany() {
    Event event = new Event(1, "Event1", "CompanyX", 10, 20, "Description1");
    Room room = new Room("RoomA", 30);
    EventRoomAssignment assignment = new EventRoomAssignment(event, room);
    assignment.setCompany("CompanyY");
    assertEquals("CompanyY", assignment.getCompany());
  }
}