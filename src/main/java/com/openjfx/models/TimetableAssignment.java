package com.openjfx.models;

import java.util.Objects;

/**
 * Model class representing a timetable assignment. Tracks the assignment of events to rooms during
 * specific time slots.
 *
 * @author mian
 */
public class TimetableAssignment {

  private int id;
  private int eventId;
  private String roomId;
  private String timeSlot;

  /**
   * Constructs a new TimetableAssignment with the specified attributes.
   *
   * @param id       the assignment ID
   * @param eventId  the event ID
   * @param roomId   the room ID
   * @param timeSlot the time slot
   * @author mian
   */
  public TimetableAssignment(int id, int eventId, String roomId, String timeSlot) {
    this.id = id;
    this.eventId = eventId;
    this.roomId = roomId;
    this.timeSlot = timeSlot;
  }

  /**
   * Constructs a new TimetableAssignment without an ID (for new assignments).
   *
   * @param eventId  the event ID
   * @param roomId   the room ID
   * @param timeSlot the time slot
   * @author mian
   */
  public TimetableAssignment(int eventId, String roomId, String timeSlot) {
    this.eventId = eventId;
    this.roomId = roomId;
    this.timeSlot = timeSlot;
  }

  /**
   * Gets the assignment ID.
   *
   * @return the assignment ID
   * @author mian
   */
  public int getId() {
    return id;
  }

  /**
   * Sets the assignment ID.
   *
   * @param id the assignment ID
   * @author mian
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * Gets the event ID.
   *
   * @return the event ID
   * @author mian
   */
  public int getEventId() {
    return eventId;
  }

  /**
   * Sets the event ID.
   *
   * @param eventId the event ID
   * @author mian
   */
  public void setEventId(int eventId) {
    this.eventId = eventId;
  }

  /**
   * Gets the room ID.
   *
   * @return the room ID
   * @author mian
   */
  public String getRoomId() {
    return roomId;
  }

  /**
   * Sets the room ID.
   *
   * @param roomId the room ID
   * @author mian
   */
  public void setRoomId(String roomId) {
    this.roomId = roomId;
  }

  /**
   * Gets the time slot.
   *
   * @return the time slot
   * @author mian
   */
  public String getTimeSlot() {
    return timeSlot;
  }

  /**
   * Sets the time slot.
   *
   * @param timeSlot the time slot
   * @author mian
   */
  public void setTimeSlot(String timeSlot) {
    this.timeSlot = timeSlot;
  }

  /**
   * Checks if this TimetableAssignment is equal to another object.
   *
   * @param o the object to compare with
   * @return true if the objects are equal, false otherwise
   * @author mian
   */
  @Override
  public boolean equals(Object o) {
      if (this == o) {
          return true;
      }
      if (o == null || getClass() != o.getClass()) {
          return false;
      }
    TimetableAssignment that = (TimetableAssignment) o;
    return id == that.id &&
        eventId == that.eventId &&
        Objects.equals(roomId, that.roomId) &&
        Objects.equals(timeSlot, that.timeSlot);
  }

  /**
   * Generates a hash code for this TimetableAssignment.
   *
   * @return the hash code
   * @author mian
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, eventId, roomId, timeSlot);
  }

  /**
   * Returns a string representation of this TimetableAssignment.
   *
   * @return the string representation
   * @author mian
   */
  @Override
  public String toString() {
    return "TimetableAssignment{" +
        "id=" + id +
        ", eventId=" + eventId +
        ", roomId='" + roomId + '\'' +
        ", timeSlot='" + timeSlot + '\'' +
        '}';
  }
}