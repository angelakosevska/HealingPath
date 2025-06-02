package com.example.healingpath.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.healingpath.models.ReminderModel;

import java.util.List;

@Dao
public interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ReminderModel reminder);

    @Update
    void update(ReminderModel reminder);

    @Delete
    void delete(ReminderModel reminder);

    @Query("SELECT * FROM reminders")
    LiveData<List<ReminderModel>> getAllReminders();

    @Query("SELECT * FROM reminders WHERE reminderId = :reminderId")
    ReminderModel getReminderById(String reminderId);
}
