package com.openjfx.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test class for the TimetableAssignment model class.
 *
 * @author mian
 */
public class TimetableAssignmentTest {

  @Test
  void constructorWithAllFields_initializesCorrectly() {
    TimetableAssignment assignment = new TimetableAssignment(1, 101, "RoomA", "08:00-09:00");
    assertEquals(1, assignment.getId());
    assertEquals(101, assignment.getEventId());
    assertEquals("RoomA", assignment.getRoomId());
    assertEquals("08:00-09:00", assignment.getTimeSlot());
  }

  @Test
  void constructorWithoutId_initializesCorrectly() {
    TimetableAssignment assignment = new TimetableAssignment(101, "RoomA", "08:00-09:00");
    assertEquals(101, assignment.getEventId());
    assertEquals("RoomA", assignment.getRoomId());
    assertEquals("08:00-09:00", assignment.getTimeSlot());
  }

  @Test
  void equals_sameObject_returnsTrue() {
    TimetableAssignment assignment = new TimetableAssignment(1, 101, "RoomA", "08:00-09:00");
    assertTrue(assignment.equals(assignment));
  }

  @Test
  void equals_differentObjectSameValues_returnsTrue() {
    TimetableAssignment assignment1 = new TimetableAssignment(1, 101, "RoomA", "08:00-09:00");
    TimetableAssignment assignment2 = new TimetableAssignment(1, 101, "RoomA", "08:00-09:00");
    assertTrue(assignment1.equals(assignment2));
  }

  @Test
  void equals_differentObjectDifferentValues_returnsFalse() {
    TimetableAssignment assignment1 = new TimetableAssignment(1, 101, "RoomA", "08:00-09:00");
    TimetableAssignment assignment2 = new TimetableAssignment(2, 102, "RoomB", "09:00-10:00");
    assertFalse(assignment1.equals(assignment2));
  }

  @Test
  void hashCode_sameValues_returnsSameHashCode() {
    TimetableAssignment assignment1 = new TimetableAssignment(1, 101, "RoomA", "08:00-09:00");
    TimetableAssignment assignment2 = new TimetableAssignment(1, 101, "RoomA", "08:00-09:00");
    assertEquals(assignment1.hashCode(), assignment2.hashCode());
  }

  @Test
  void hashCode_differentValues_returnsDifferentHashCode() {
    TimetableAssignment assignment1 = new TimetableAssignment(1, 101, "RoomA", "08:00-09:00");
    TimetableAssignment assignment2 = new TimetableAssignment(2, 102, "RoomB", "09:00-10:00");
    assertNotEquals(assignment1.hashCode(), assignment2.hashCode());
  }

  @Test
  void toString_returnsCorrectStringRepresentation() {
    TimetableAssignment assignment = new TimetableAssignment(1, 101, "RoomA", "08:00-09:00");
    String expected = "TimetableAssignment{id=1, eventId=101, roomId='RoomA', timeSlot='08:00-09:00'}";
    assertEquals(expected, assignment.toString());
  }
}