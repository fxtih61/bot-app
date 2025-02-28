package com.openjfx.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class EventRoomAssignment {
    private final ObjectProperty<Event> event = new SimpleObjectProperty<>();
    private final ObjectProperty<Room> room = new SimpleObjectProperty<>();
    private final StringProperty timeSlot = new SimpleStringProperty();
    private final StringProperty company = new SimpleStringProperty();

    public EventRoomAssignment(Event event, Room room) {
        setEvent(event);
        setRoom(room);
        if (event != null) {
            setCompany(event.getCompany());
        }
    }

    public Event getEvent() { return event.get(); }
    public void setEvent(Event value) { event.set(value); }
    public ObjectProperty<Event> eventProperty() { return event; }

    public Room getRoom() { return room.get(); }
    public void setRoom(Room value) { room.set(value); }
    public ObjectProperty<Room> roomProperty() { return room; }

    public String getTimeSlot() { return timeSlot.get(); }
    public void setTimeSlot(String value) { timeSlot.set(value); }
    public StringProperty timeSlotProperty() { return timeSlot; }

    public String getCompany() { return company.get(); }
    public void setCompany(String value) { company.set(value); }
    public StringProperty companyProperty() { return company; }
}