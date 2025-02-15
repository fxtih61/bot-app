package com.openjfx.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RoomTest {

  @Test
  void testRoom() {
    Room room = new Room("112", 20);

    assertEquals("112", room.getName());
    assertEquals(20, room.getCapacity());

    room.setName("Class Room 2");
    room.setCapacity(30);

    assertEquals("Class Room 2", room.getName());
    assertEquals(30, room.getCapacity());
  }

  @Test
  void testEquals() {
    Room room1 = new Room("112", 20);
    Room room2 = new Room("112", 20);
    Room room3 = new Room("113", 20);

    assertEquals(room1, room2);
    assertNotEquals(room1, room3);
  }

  @Test
  void testHashCode() {
    Room room1 = new Room("112", 20);
    Room room2 = new Room("112", 20);
    Room room3 = new Room("113", 20);

    assertEquals(room1.hashCode(), room2.hashCode());
    assertNotEquals(room1.hashCode(), room3.hashCode());
  }

  @Test
  void testToString() {
    Room room = new Room("112", 20);
    assertEquals("Room{name='112', capacity=20}", room.toString());
  }
}
