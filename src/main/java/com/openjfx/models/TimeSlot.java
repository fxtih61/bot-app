package com.openjfx.models;

import java.util.Objects;

/**
 * Model class representing a time slot in the schedule. This class defines a specific time period
 * identified by a slot name, along with its start and end times.
 *
 * @author mian
 */
public class TimeSlot {

  private int id;
  private String startTime;
  private String endTime;
  private String slot;

  /**
   * Default constructor for creating an empty time slot.
   *
   * @author mian
   */
  public TimeSlot() {
  }

  /**
   * Constructor with all fields except id.
   *
   * @param startTime the start time of the slot
   * @param endTime   the end time of the slot
   * @param slot      the slot identifier (e.g., "A", "B", etc.)
   * @author mian
   */
  public TimeSlot(String startTime, String endTime, String slot) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.slot = slot;
  }

  /**
   * Constructor with all fields.
   *
   * @param id        the unique identifier
   * @param startTime the start time of the slot
   * @param endTime   the end time of the slot
   * @param slot      the slot identifier (e.g., "A", "B", etc.)
   * @author mian
   */
  public TimeSlot(int id, String startTime, String endTime, String slot) {
    this.id = id;
    this.startTime = startTime;
    this.endTime = endTime;
    this.slot = slot;
  }

  /**
   * Returns the unique identifier of the time slot.
   *
   * @return the unique identifier
   * @author mian
   */
  public int getId() {
    return id;
  }

  /**
   * Sets the unique identifier of the time slot.
   *
   * @param id the new unique identifier
   * @author mian
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * Returns the start time of the slot.
   *
   * @return the start time as a string
   * @author mian
   */
  public String getStartTime() {
    return startTime;
  }

  /**
   * Sets the start time of the slot.
   *
   * @param startTime the new start time
   * @author mian
   */
  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  /**
   * Returns the end time of the slot.
   *
   * @return the end time as a string
   * @author mian
   */
  public String getEndTime() {
    return endTime;
  }

  /**
   * Sets the end time of the slot.
   *
   * @param endTime the new end time
   * @author mian
   */
  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  /**
   * Returns the slot identifier (e.g., "A", "B", etc.).
   *
   * @return the slot identifier
   * @author mian
   */
  public String getSlot() {
    return slot;
  }

  /**
   * Sets the slot identifier.
   *
   * @param slot the new slot identifier
   * @author mian
   */
  public void setSlot(String slot) {
    this.slot = slot;
  }

  /**
   * Returns a string representation of the time slot.
   *
   * @return a string representation of the object
   * @author mian
   */
  @Override
  public String toString() {
    return "TimeSlot{" +
        "id=" + id +
        ", startTime='" + startTime + '\'' +
        ", endTime='" + endTime + '\'' +
        ", slot='" + slot + '\'' +
        '}';
  }

  /**
   * Indicates whether some other object is "equal to" this one. Two time slots are considered equal
   * if they have the same id, start time, end time, and slot.
   *
   * @param o the reference object with which to compare
   * @return true if this object is the same as the obj argument; false otherwise
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
    TimeSlot timeSlot = (TimeSlot) o;
    return id == timeSlot.id &&
        Objects.equals(startTime, timeSlot.startTime) &&
        Objects.equals(endTime, timeSlot.endTime) &&
        Objects.equals(slot, timeSlot.slot);
  }

  /**
   * Returns a hash code value for the time slot.
   *
   * @return a hash code value for this object
   * @author mian
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, startTime, endTime, slot);
  }
}