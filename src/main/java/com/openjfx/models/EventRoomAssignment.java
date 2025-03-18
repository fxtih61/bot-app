package com.openjfx.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents an assignment of an event to a specific room and time slot. This class uses JavaFX
 * properties to support UI binding and updates.
 *
 * @author mian
 */
public class EventRoomAssignment {

  private final ObjectProperty<Event> event = new SimpleObjectProperty<>();
  private final ObjectProperty<Room> room = new SimpleObjectProperty<>();
  private final StringProperty timeSlot = new SimpleStringProperty();
  private final StringProperty company = new SimpleStringProperty();

  /**
   * Constructs a new EventRoomAssignment with the specified event and room. If an event is
   * provided, the company property is initialized from the event's company.
   *
   * @param event the event to be assigned
   * @param room  the room to which the event is assigned
   * @author mian
   */
  public EventRoomAssignment(Event event, Room room) {
    setEvent(event);
    setRoom(room);
    if (event != null) {
      setCompany(event.getCompany());
    }
  }

  /**
   * Returns the assigned event.
   *
   * @return the event
   * @author mian
   */
  public Event getEvent() {
    return event.get();
  }

  /**
   * Sets the assigned event.
   *
   * @param value the event to be assigned
   * @author mian
   */
  public void setEvent(Event value) {
    event.set(value);
  }

  /**
   * Returns the event property.
   *
   * @return the event property
   * @author mian
   */
  public ObjectProperty<Event> eventProperty() {
    return event;
  }

  /**
   * Returns the assigned room.
   *
   * @return the room
   * @author mian
   */
  public Room getRoom() {
    return room.get();
  }

  /**
   * Sets the assigned room.
   *
   * @param value the room to be assigned
   * @author mian
   */
  public void setRoom(Room value) {
    room.set(value);
  }

  /**
   * Returns the room property.
   *
   * @return the room property
   * @author mian
   */
  public ObjectProperty<Room> roomProperty() {
    return room;
  }

  /**
   * Returns the time slot identifier for this assignment.
   *
   * @return the time slot
   * @author mian
   */
  public String getTimeSlot() {
    return timeSlot.get();
  }

  /**
   * Sets the time slot identifier for this assignment.
   *
   * @param value the time slot to be set
   * @author mian
   */
  public void setTimeSlot(String value) {
    timeSlot.set(value);
  }

  /**
   * Returns the time slot property.
   *
   * @return the time slot property
   * @author mian
   */
  public StringProperty timeSlotProperty() {
    return timeSlot;
  }

  /**
   * Returns the company name associated with this assignment.
   *
   * @return the company name
   * @author mian
   */
  public String getCompany() {
    return company.get();
  }

  /**
   * Sets the company name associated with this assignment.
   *
   * @param value the company name to be set
   * @author mian
   */
  public void setCompany(String value) {
    company.set(value);
  }

  public Integer getEventId() {
    return this.event.get().getId();
  }

  /**
   * Returns the company property.
   *
   * @return the company property
   * @author mian
   */
  public StringProperty companyProperty() {
    return company;
  }

  @Override
  public String toString() {
    return "EventRoomAssignment{" +
        "event=" + event.get() +
        ", room=" + room.get() +
        ", timeSlot=" + timeSlot.get() +
        ", company=" + company.get() +
        '}';
  }
}