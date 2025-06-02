package com.example.healingpath.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "notes")
public class NoteItem {
    @PrimaryKey(autoGenerate = true)
    private String note;
    private int pain;
    private long timestamp;
    private String mood;

    public NoteItem() {}

    public NoteItem(String note, int pain, long timestamp, String mood) {
        this.note = note;
        this.pain = pain;
        this.timestamp = timestamp;
        this.mood = mood;
    }

    public String getNote() {
        return note;
    }

    public int getPain() {
        return pain;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getMood() {
        return mood;
    }
}
