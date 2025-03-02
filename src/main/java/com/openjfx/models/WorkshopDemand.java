package com.openjfx.models;

import java.util.Objects;

/**
 * Model class representing workshop demand for events. Tracks how many students have requested each
 * event.
 *
 * @author mian
 */
public class WorkshopDemand {

  private int eventId;
  private int demand;
  private String companyName;

  /**
   * Constructs a new WorkshopDemand with the specified attributes.
   *
   * @param eventId the event ID
   * @param demand  the number of students demanding this workshop
   * @author mian
   */
  public WorkshopDemand(int eventId, int demand, String companyName) {
    this.eventId = eventId;
    this.demand = demand;
    this.companyName = companyName;
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
   * Gets the demand value.
   *
   * @return the demand value
   * @author mian
   */
  public int getDemand() {
    return demand;
  }

  /**
   * Sets the demand value.
   *
   * @param demand the demand value
   * @author mian
   */
  public void setDemand(int demand) {
    this.demand = demand;
  }

  /**
   * Checks if this WorkshopDemand is equal to another object.
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
    WorkshopDemand that = (WorkshopDemand) o;
    return eventId == that.eventId && demand == that.demand;
  }

  /**
   * Generates a hash code for this WorkshopDemand.
   *
   * @return the hash code
   * @author mian
   */
  @Override
  public int hashCode() {
    return Objects.hash(eventId, demand);
  }

  /**
   * Returns a string representation of this WorkshopDemand.
   *
   * @return the string representation
   * @author mian
   */
  @Override
  public String toString() {
    return "WorkshopDemand{" +
        "eventId=" + eventId +
        ", demand=" + demand +
        '}';
  }
}