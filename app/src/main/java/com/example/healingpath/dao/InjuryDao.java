package com.example.healingpath.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.healingpath.models.Injury;

import java.util.List;

@Dao
public interface InjuryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Injury injury);

    @Update
    void update(Injury injury);

    @Delete
    void delete(Injury injury);


    @Query("SELECT * FROM injuries ORDER BY timestamp DESC")
    LiveData<List<Injury>> getAllInjuries();

    @Query("SELECT * FROM injuries WHERE id = :injuryId LIMIT 1")
    LiveData<Injury> getInjuryById(String injuryId);
    @Query("SELECT * FROM injuries WHERE is_synced = 0")
    List<Injury> getUnsyncedInjuries();

}
