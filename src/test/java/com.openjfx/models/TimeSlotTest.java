package com.openjfx.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test class for the TimeSlot model class.
 *
 * @author mian
 */
public class TimeSlotTest {

  @Test
  void constructorWithAllFields_initializesCorrectly() {
    TimeSlot timeSlot = new TimeSlot(1, "08:00", "09:00", "A");
    assertEquals(1, timeSlot.getId());
    assertEquals("08:00", timeSlot.getStartTime());
    assertEquals("09:00", timeSlot.getEndTime());
    assertEquals("A", timeSlot.getSlot());
  }

  @Test
  void constructorWithoutId_initializesCorrectly() {
    TimeSlot timeSlot = new TimeSlot("08:00", "09:00", "A");
    assertEquals("08:00", timeSlot.getStartTime());
    assertEquals("09:00", timeSlot.getEndTime());
    assertEquals("A", timeSlot.getSlot());
  }

  @Test
  void equals_sameObject_returnsTrue() {
    TimeSlot timeSlot = new TimeSlot(1, "08:00", "09:00", "A");
    assertTrue(timeSlot.equals(timeSlot));
  }

  @Test
  void equals_differentObjectSameValues_returnsTrue() {
    TimeSlot timeSlot1 = new TimeSlot(1, "08:00", "09:00", "A");
    TimeSlot timeSlot2 = new TimeSlot(1, "08:00", "09:00", "A");
    assertTrue(timeSlot1.equals(timeSlot2));
  }

  @Test
  void equals_differentObjectDifferentValues_returnsFalse() {
    TimeSlot timeSlot1 = new TimeSlot(1, "08:00", "09:00", "A");
    TimeSlot timeSlot2 = new TimeSlot(2, "09:00", "10:00", "B");
    assertFalse(timeSlot1.equals(timeSlot2));
  }

  @Test
  void hashCode_sameValues_returnsSameHashCode() {
    TimeSlot timeSlot1 = new TimeSlot(1, "08:00", "09:00", "A");
    TimeSlot timeSlot2 = new TimeSlot(1, "08:00", "09:00", "A");
    assertEquals(timeSlot1.hashCode(), timeSlot2.hashCode());
  }

  @Test
  void hashCode_differentValues_returnsDifferentHashCode() {
    TimeSlot timeSlot1 = new TimeSlot(1, "08:00", "09:00", "A");
    TimeSlot timeSlot2 = new TimeSlot(2, "09:00", "10:00", "B");
    assertNotEquals(timeSlot1.hashCode(), timeSlot2.hashCode());
  }

  @Test
  void toString_returnsCorrectStringRepresentation() {
    TimeSlot timeSlot = new TimeSlot(1, "08:00", "09:00", "A");
    String expected = "TimeSlot{id=1, startTime='08:00', endTime='09:00', slot='A'}";
    assertEquals(expected, timeSlot.toString());
  }
}