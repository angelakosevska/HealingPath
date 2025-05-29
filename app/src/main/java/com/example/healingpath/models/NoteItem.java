package com.example.healingpath.models;

public class NoteItem {
    private String note;       // match Firestore field name
    private int pain;          // match Firestore field name
    private long timestamp;

    public NoteItem() {}

    public NoteItem(String note, int pain, long timestamp) {
        this.note = note;
        this.pain = pain;
        this.timestamp = timestamp;
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
}
