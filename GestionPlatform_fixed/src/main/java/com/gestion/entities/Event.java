package com.gestion.entities;

public class Event {
    private int id;
    private String title;
    private String type;
    private String eventDate;
    private String location;
    private String status;

    public Event(int id, String title, String type, String eventDate, String location, String status) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.eventDate = eventDate;
        this.location = location;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return title + " - " + eventDate;
    }
}
