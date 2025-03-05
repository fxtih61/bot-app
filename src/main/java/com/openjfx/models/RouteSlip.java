package com.openjfx.models;

/**
 * Represents a student's route slip (Laufzettel) for events.
 *
 * @author mian
 */
public class RouteSlip {

  private String firstName;
  private String lastName;
  private String classRef;
  private int eventId;
  private String companyName;
  private String subject;
  private String roomName;
  private String timeSlot;
  private String timeRange;
  private int choicePriority;

  /**
   * Constructs a complete RouteSlip object.
   *
   * @param firstName      student's first name
   * @param lastName       student's last name
   * @param classRef       student's class
   * @param eventId        event ID
   * @param companyName    company name
   * @param subject        event subject/description
   * @param roomName       assigned room name
   * @param timeSlot       time slot identifier
   * @param timeRange      formatted time range
   * @param choicePriority which choice number this was (1-6)
   * @author mian
   */
  public RouteSlip(String firstName, String lastName, String classRef, int eventId,
      String companyName, String subject, String roomName, String timeSlot,
      String timeRange, int choicePriority) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.classRef = classRef;
    this.eventId = eventId;
    this.companyName = companyName;
    this.subject = subject;
    this.roomName = roomName;
    this.timeSlot = timeSlot;
    this.timeRange = timeRange;
    this.choicePriority = choicePriority;
  }

  // Getters and setters
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getClassRef() {
    return classRef;
  }

  public void setClassRef(String classRef) {
    this.classRef = classRef;
  }

  public int getEventId() {
    return eventId;
  }

  public void setEventId(int eventId) {
    this.eventId = eventId;
  }

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getRoomName() {
    return roomName;
  }

  public void setRoomName(String roomName) {
    this.roomName = roomName;
  }

  public String getTimeSlot() {
    return timeSlot;
  }

  public void setTimeSlot(String timeSlot) {
    this.timeSlot = timeSlot;
  }

  public String getTimeRange() {
    return timeRange;
  }

  public void setTimeRange(String timeRange) {
    this.timeRange = timeRange;
  }

  public int getChoicePriority() {
    return choicePriority;
  }

  public void setChoicePriority(int choicePriority) {
    this.choicePriority = choicePriority;
  }
}