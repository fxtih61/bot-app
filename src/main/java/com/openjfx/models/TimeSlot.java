package com.openjfx.models;

import java.util.Objects;

/**
 * Model class representing a time slot in the schedule.
 */
public class TimeSlot {

  private int id;
  private String startTime;
  private String endTime;
  private String slot;

  /**
   * Default constructor
   */
  public TimeSlot() {
  }

  /**
   * Constructor with all fields except id
   *
   * @param startTime the start time of the slot
   * @param endTime   the end time of the slot
   * @param slot      the slot identifier (e.g., "A", "B", etc.)
   */
  public TimeSlot(String startTime, String endTime, String slot) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.slot = slot;
  }

  /**
   * Constructor with all fields
   *
   * @param id        the unique identifier
   * @param startTime the start time of the slot
   * @param endTime   the end time of the slot
   * @param slot      the slot identifier (e.g., "A", "B", etc.)
   */
  public TimeSlot(int id, String startTime, String endTime, String slot) {
    this.id = id;
    this.startTime = startTime;
    this.endTime = endTime;
    this.slot = slot;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public String getSlot() {
    return slot;
  }

  public void setSlot(String slot) {
    this.slot = slot;
  }

  @Override
  public String toString() {
    return "TimeSlot{" +
        "id=" + id +
        ", startTime='" + startTime + '\'' +
        ", endTime='" + endTime + '\'' +
        ", slot='" + slot + '\'' +
        '}';
  }

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

  @Override
  public int hashCode() {
    return Objects.hash(id, startTime, endTime, slot);
  }
}