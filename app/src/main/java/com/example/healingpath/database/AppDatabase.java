package com.example.healingpath.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.healingpath.models.Injury;
import com.example.healingpath.dao.InjuryDao;

@Database(entities = {Injury.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract InjuryDao injuryDao();
}

