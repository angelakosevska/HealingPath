package com.example.healingpath.models;
public class ReminderModel {
    private String note;
    private long timestamp;
    private String reminderId;

    public ReminderModel(String note, long timestamp, String reminderId) {
        this.note = note;
        this.timestamp = timestamp;
        this.reminderId = reminderId;
    }

    public String getNote() {
        return note;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getReminderId() {
        return reminderId;
    }
}
