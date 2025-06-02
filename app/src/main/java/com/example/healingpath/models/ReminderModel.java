package com.example.healingpath.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "reminders")
public class ReminderModel {

    @PrimaryKey
    @NonNull
    private String reminderId;

    private String note;
    private long timestamp;

    public ReminderModel() {}

    public ReminderModel(String note, long timestamp, @NonNull String reminderId) {
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

    @NonNull
    public String getReminderId() {
        return reminderId;
    }

    public void setReminderId(@NonNull String reminderId) {
        this.reminderId = reminderId;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
