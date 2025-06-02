package com.example.healingpath.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.healingpath.dao.NoteDao;
import com.example.healingpath.dao.ReminderDao;
import com.example.healingpath.models.Injury;
import com.example.healingpath.dao.InjuryDao;
import com.example.healingpath.models.NoteItem;
import com.example.healingpath.models.ReminderModel;

@Database(entities = {Injury.class, NoteItem.class, ReminderModel.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract InjuryDao injuryDao();
    public abstract NoteDao noteDao();
    public abstract ReminderDao reminderDao(); // ⬅️ ADD THIS
}
