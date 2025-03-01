package com.openjfx.models;

/**
 * Represents a row in the timetable display, containing information about an event scheduled in a
 * specific room at a specific time slot. This class is used to represent the assignment results in
 * a user-friendly format.
 *
 * @author mian
 */
public class TimetableRow {

  private String timeSlot;
  private String roomName;
  private int roomCapacity;
  private int eventId;
  private String company;
  private String subject;

  /**
   * Constructs a new TimetableRow with the specified attributes.
   *
   * @param timeSlot     the time slot identifier for the scheduled event
   * @param roomName     the name of the assigned room
   * @param roomCapacity the capacity of the assigned room
   * @param eventId      the unique identifier of the scheduled event
   * @param company      the company hosting the event
   * @param subject      the subject of the event
   * @author mian
   */
  public TimetableRow(String timeSlot, String roomName, int roomCapacity,
      int eventId, String company, String subject) {
    this.timeSlot = timeSlot;
    this.roomName = roomName;
    this.roomCapacity = roomCapacity;
    this.eventId = eventId;
    this.company = company;
    this.subject = subject;
  }

  /**
   * Returns the time slot identifier for the scheduled event.
   *
   * @return the time slot identifier
   * @author mian
   */
  public String getTimeSlot() {
    return timeSlot;
  }

  /**
   * Returns the name of the assigned room.
   *
   * @return the room name
   * @author mian
   */
  public String getRoomName() {
    return roomName;
  }

  /**
   * Returns the capacity of the assigned room.
   *
   * @return the room capacity
   * @author mian
   */
  public int getRoomCapacity() {
    return roomCapacity;
  }

  /**
   * Returns the unique identifier of the scheduled event.
   *
   * @return the event ID
   * @author mian
   */
  public int getEventId() {
    return eventId;
  }

  /**
   * Returns the company hosting the event.
   *
   * @return the company name
   * @author mian
   */
  public String getCompany() {
    return company;
  }

  /**
   * Returns the subject of the event.
   *
   * @return the event subject
   * @author mian
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Sets the time slot identifier for the scheduled event.
   *
   * @param timeSlot the new time slot identifier
   * @author mian
   */
  public void setTimeSlot(String timeSlot) {
    this.timeSlot = timeSlot;
  }

  /**
   * Sets the name of the assigned room.
   *
   * @param roomName the new room name
   * @author mian
   */
  public void setRoomName(String roomName) {
    this.roomName = roomName;
  }

  /**
   * Sets the capacity of the assigned room.
   *
   * @param roomCapacity the new room capacity
   * @author mian
   */
  public void setRoomCapacity(int roomCapacity) {
    this.roomCapacity = roomCapacity;
  }

  /**
   * Sets the unique identifier of the scheduled event.
   *
   * @param eventId the new event ID
   * @author mian
   */
  public void setEventId(int eventId) {
    this.eventId = eventId;
  }

  /**
   * Sets the company hosting the event.
   *
   * @param company the new company name
   * @author mian
   */
  public void setCompany(String company) {
    this.company = company;
  }

  /**
   * Sets the subject of the event.
   *
   * @param subject the new event subject
   * @author mian
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }
}