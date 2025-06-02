package com.example.healingpath.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes",
        foreignKeys = @ForeignKey(
                entity = Injury.class,
                parentColumns = "id",
                childColumns = "injuryId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("injuryId")}
)
public class NoteItem {

    @PrimaryKey
    @NonNull
    private String id;  // Use Firestore-compatible unique ID

    private String injuryId;
    private String note;
    private int pain;
    private long timestamp;
    private String mood;
    private boolean synced;

    public NoteItem() {
    }

    public NoteItem(@NonNull String id, String injuryId, String note, int pain, long timestamp, String mood, boolean synced) {
        this.id = id;
        this.injuryId = injuryId;
        this.note = note;
        this.pain = pain;
        this.timestamp = timestamp;
        this.mood = mood;
        this.synced = synced;
    }


    // Getters
    @NonNull
    public String getId() {
        return id;
    }

    public String getInjuryId() {
        return injuryId;
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

    public boolean isSynced() {
        return synced;
    }

    // Setters
    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setInjuryId(String injuryId) {
        this.injuryId = injuryId;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setPain(int pain) {
        this.pain = pain;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }
}
