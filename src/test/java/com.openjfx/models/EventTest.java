package com.openjfx.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EventTest {

  @Test
  void testEvent() {
    Event event = new Event(1, "Company", "Subject", 10, 5, "A");

    assertEquals(1, event.getId());
    assertEquals("Company", event.getCompany());
    assertEquals("Subject", event.getSubject());
    assertEquals(10, event.getMaxParticipants());
    assertEquals(5, event.getMinParticipants());
    assertEquals("A", event.getEarliestStart());

    event.setId(2);
    event.setCompany("Company 2");
    event.setSubject("Subject 2");
    event.setMaxParticipants(20);
    event.setMinParticipants(10);
    event.setEarliestStart("B");

    assertEquals(2, event.getId());
    assertEquals("Company 2", event.getCompany());
    assertEquals("Subject 2", event.getSubject());
    assertEquals(20, event.getMaxParticipants());
    assertEquals(10, event.getMinParticipants());
    assertEquals("B", event.getEarliestStart());
  }

  @Test
  void testEquals() {
    Event event1 = new Event(1, "Company", "Subject", 10, 5, "A");
    Event event2 = new Event(1, "Company", "Subject", 10, 5, "A");
    Event event3 = new Event(2, "Company 2", "Subject 2", 20, 10, "B");

    assertEquals(event1, event2);
    assertNotEquals(event1, event3);
  }

  @Test
  void testHashCode() {
    Event event1 = new Event(1, "Company", "Subject", 10, 5, "A");
    Event event2 = new Event(1, "Company", "Subject", 10, 5, "A");
    Event event3 = new Event(2, "Company 2", "Subject 2", 20, 10, "B");

    assertEquals(event1.hashCode(), event2.hashCode());
    assertNotEquals(event1.hashCode(), event3.hashCode());
  }

  @Test
  void testToString() {
    Event event = new Event(1, "Company", "Subject", 10, 5, "A");
    assertEquals(
        "Event{id=1, company='Company', subject='Subject', maxParticipants=10, minParticipants=5, earliestStart='A'}",
        event.toString());
  }
}
