package com.openjfx.models;

import java.util.Objects;

/**
 * Model class representing a student assignment to an event. Tracks which student is assigned to
 * which event.
 *
 * @author mian
 */
public class StudentAssignment {

  private int id;
  private int eventId;
  private String firstName;
  private String lastName;
  private String classRef;

  /**
   * Constructs a new StudentAssignment with the specified attributes.
   *
   * @param id        the assignment ID
   * @param eventId   the event ID
   * @param firstName the student's first name
   * @param lastName  the student's last name
   * @param classRef  the class reference
   * @author mian
   */
  public StudentAssignment(int id, int eventId, String firstName, String lastName,
      String classRef) {
    this.id = id;
    this.eventId = eventId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.classRef = classRef;
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
    return id == that.id &&
        eventId == that.eventId &&
        Objects.equals(firstName, that.firstName) &&
        Objects.equals(lastName, that.lastName) &&
        Objects.equals(classRef, that.classRef);
  }

  /**
   * Generates a hash code for this StudentAssignment.
   *
   * @return the hash code
   * @author mian
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, eventId, firstName, lastName, classRef);
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
        "id=" + id +
        ", eventId=" + eventId +
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", classRef='" + classRef + '\'' +
        '}';
  }
}