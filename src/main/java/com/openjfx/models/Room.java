package com.openjfx.models;

import java.util.Objects;

/**
 * The `Room` class represents a room with a name and capacity. It is used to model rooms in an
 * application. The `name` field is used as the primary key for identifying rooms.
 */
public class Room {

  private String name;
  private int capacity;

  /**
   * Constructs a new `Room` with the specified name and capacity.
   *
   * @param name     the name of the room
   * @param capacity the capacity of the room
   */
  public Room(String name, int capacity) {
    this.name = name;
    this.capacity = capacity;
  }

  /**
   * Returns the name of the room.
   *
   * @return the name of the room
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the room.
   *
   * @param name the new name of the room
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the capacity of the room.
   *
   * @return the capacity of the room
   */
  public int getCapacity() {
    return capacity;
  }

  /**
   * Sets the capacity of the room.
   *
   * @param capacity the new capacity of the room
   */
  public void setCapacity(int capacity) {
    this.capacity = capacity;
  }

  /**
   * Indicates whether some other object is "equal to" this one. The `name` field is used for
   * equality comparison.
   *
   * @param o the reference object with which to compare
   * @return `true` if this object is the same as the obj argument; `false` otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Room room = (Room) o;
    return Objects.equals(name, room.name);
  }

  /**
   * Returns a hash code value for the object. The `name` field is used for generating the hash
   * code.
   *
   * @return a hash code value for this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  /**
   * Returns a string representation of the object.
   *
   * @return a string representation of the object
   */
  @Override
  public String toString() {
    return "Room{" +
        "name='" + name + '\'' +
        ", capacity=" + capacity +
        '}';
  }
}