package com.example.healingpath.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.healingpath.models.NoteItem;

import java.util.List;

@Dao
public interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NoteItem note);

    @Update
    void update(NoteItem note);

    @Delete
    void delete(NoteItem note);

    @Query("SELECT * FROM notes WHERE injuryId = :injuryId")
    LiveData<List<NoteItem>> getNotesForInjury(String injuryId);

    @Query("SELECT * FROM notes WHERE synced = 0")
    List<NoteItem> getUnsyncedNotes();
}

