package com.example.healingpath.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.healingpath.models.ReminderModel;
import com.example.healingpath.repositories.ReminderRepository;

import java.util.List;

public class ReminderViewModel extends AndroidViewModel {
    private final ReminderRepository repository;
    private final LiveData<List<ReminderModel>> reminders;

    public ReminderViewModel(@NonNull Application application, String injuryId) {
        super(application);
        repository = new ReminderRepository(application, injuryId);
        reminders = repository.getAllReminders();
    }

    public LiveData<List<ReminderModel>> getReminders() {
        return reminders;
    }

    public void deleteReminder(ReminderModel reminder) {
        repository.delete(reminder);
    }
}
