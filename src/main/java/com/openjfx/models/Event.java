package com.openjfx.models;

import java.util.Objects;

/**
 * The `Event` class represents an event with various attributes such as company, subject, maximum
 * and minimum participants, and the earliest start time.
 */
public class Event {

  private int id;
  private String company;
  private String subject;
  private int maxParticipants;
  private int minParticipants;
  private String earliestStart;

  /**
   * Constructs a new `Event` with the specified attributes.
   *
   * @param id              the unique identifier of the event
   * @param company         the company hosting the event
   * @param subject         the subject of the event
   * @param maxParticipants the maximum number of participants allowed
   * @param minParticipants the minimum number of participants required
   * @param earliestStart   the earliest start time of the event
   */
  public Event(int id, String company, String subject, int maxParticipants, int minParticipants,
      String earliestStart) {
    this.id = id;
    this.company = company;
    this.subject = subject;
    this.maxParticipants = maxParticipants;
    this.minParticipants = minParticipants;
    this.earliestStart = earliestStart;
  }

  /**
   * Returns the unique identifier of the event.
   *
   * @return the unique identifier of the event
   */
  public int getId() {
    return id;
  }

  /**
   * Sets the unique identifier of the event.
   *
   * @param id the new unique identifier of the event
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * Returns the company hosting the event.
   *
   * @return the company hosting the event
   */
  public String getCompany() {
    return company;
  }

  /**
   * Sets the company hosting the event.
   *
   * @param company the new company hosting the event
   */
  public void setCompany(String company) {
    this.company = company;
  }

  /**
   * Returns the subject of the event.
   *
   * @return the subject of the event
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Sets the subject of the event.
   *
   * @param subject the new subject of the event
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Returns the maximum number of participants allowed.
   *
   * @return the maximum number of participants allowed
   */
  public int getMaxParticipants() {
    return maxParticipants;
  }

  /**
   * Sets the maximum number of participants allowed.
   *
   * @param maxParticipants the new maximum number of participants allowed
   */
  public void setMaxParticipants(int maxParticipants) {
    this.maxParticipants = maxParticipants;
  }

  /**
   * Returns the minimum number of participants required.
   *
   * @return the minimum number of participants required
   */
  public int getMinParticipants() {
    return minParticipants;
  }

  /**
   * Sets the minimum number of participants required.
   *
   * @param minParticipants the new minimum number of participants required
   */
  public void setMinParticipants(int minParticipants) {
    this.minParticipants = minParticipants;
  }

  /**
   * Returns the earliest start time of the event.
   *
   * @return the earliest start time of the event
   */
  public String getEarliestStart() {
    return earliestStart;
  }

  /**
   * Sets the earliest start time of the event.
   *
   * @param earliestStart the new earliest start time of the event
   */
  public void setEarliestStart(String earliestStart) {
    this.earliestStart = earliestStart;
  }

  /**
   * Indicates whether some other object is "equal to" this one. The `id` field is used for equality
   * comparison.
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
    Event event = (Event) o;
    return id == event.id;
  }

  /**
   * Returns a hash code value for the object. The `id` field is used for generating the hash code.
   *
   * @return a hash code value for this object
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  /**
   * Returns a string representation of the object.
   *
   * @return a string representation of the object
   */
  @Override
  public String toString() {
    return "id=" + id +
        ", company='" + company + '\'' +
        ", subject='" + subject + '\'' +
        ", maxParticipants=" + maxParticipants +
        ", minParticipants=" + minParticipants +
        ", earliestStart='" + earliestStart + '\'' +
        '}';
  }
}