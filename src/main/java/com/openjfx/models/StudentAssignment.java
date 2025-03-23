package com.openjfx.models;

import java.util.Objects;

/**
 * Model class representing a student assignment to an event. Tracks which student is assigned to
 * which event.
 *
 * @author mian
 */
public class StudentAssignment {

  private int eventId;
  private String companyName;
  private String firstName;
  private String lastName;
  private String classRef;
  private String subject;
  private String timeSlot;
  private String roomId;

  /**
   * Constructs a new StudentAssignment with the specified attributes.
   *
   * @param eventId   the assignment ID
   * @param eventId   the event ID
   * @param firstName the student's first name
   * @param lastName  the student's last name
   * @param classRef  the class reference
   * @author mian
   */
  public StudentAssignment(int eventId, String firstName, String lastName, String classRef,
      String companyName, String subject) {
    this.eventId = eventId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.classRef = classRef;
    this.companyName = companyName;
    this.subject = subject;
  }

  /**
   * Constructs a new StudentAssignment without an ID (for new assignments).
   *
   * @param eventId   the event ID
   * @param firstName the student's first name
   * @param lastName  the student's last name
   * @param classRef  the class reference
   * @author mian
   */
  public StudentAssignment(int eventId, String firstName, String lastName, String classRef) {
    this.eventId = eventId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.classRef = classRef;
  }

  /**
   * Gets the company name.
   *
   * @return the company name
   * @author mian
   */
  public String getCompanyName() {
    return companyName;
  }

  /**
   * Sets the company name.
   *
   * @param companyName the company name
   * @author mian
   */
  public void setCompanyName(String companyName) {
    this.companyName = companyName;
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
   * Gets the student's first name.
   *
   * @return the first name
   * @author mian
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Sets the student's first name.
   *
   * @param firstName the first name
   * @author mian
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * Gets the student's last name.
   *
   * @return the last name
   * @author mian
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * Sets the student's last name.
   *
   * @param lastName the last name
   * @author mian
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * Gets the class reference.
   *
   * @return the class reference
   * @author mian
   */
  public String getClassRef() {
    return classRef;
  }

  /**
   * Sets the class reference.
   *
   * @param classRef the class reference
   * @author mian
   */
  public void setClassRef(String classRef) {
    this.classRef = classRef;
  }

  /**
   * Gets the subject.
   *
   * @return the subject
   * @author mian
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Sets the subject.
   *
   * @param subject the subject
   * @author mian
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Gets the time slot.
   *
   * @return the time slot
   */
  public String getTimeSlot() {
    return timeSlot;
  }

  /**
   * Sets the time slot.
   *
   * @param timeSlot the time slot
   */
  public void setTimeSlot(String timeSlot) {
    this.timeSlot = timeSlot;
  }

  /**
   * Gets the room ID.
   *
   * @return the room ID
   */
  public String getRoomId() {
    return roomId;
  }

  /**
   * Sets the room ID.
   *
   * @param roomId the room ID
   */
  public void setRoomId(String roomId) {
    this.roomId = roomId;
  }

  /**
   * Checks if this StudentAssignment is equal to another object.
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
    StudentAssignment that = (StudentAssignment) o;
    return
        eventId == that.eventId &&
            Objects.equals(firstName, that.firstName) &&
            Objects.equals(lastName, that.lastName) &&
            Objects.equals(classRef, that.classRef)
            && Objects.equals(companyName, that.companyName)
            && Objects.equals(subject, that.subject);
  }

  /**
   * Generates a hash code for this StudentAssignment.
   *
   * @return the hash code
   * @author mian
   */
  @Override
  public int hashCode() {
    return Objects.hash(eventId, firstName, lastName, classRef, companyName, subject);
  }

  /**
   * Returns a string representation of this StudentAssignment.
   *
   * @return the string representation
   * @author mian
   */
  @Override
  public String toString() {
    return "StudentAssignment{" +
        "eventId=" + eventId +
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", classRef='" + classRef + '\'' +
        ", companyName='" + companyName + '\'' +
        ", subject='" + subject + '\'' +
        '}';
  }
}