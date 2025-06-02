package com.example.healingpath.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.healingpath.viewmodels.ReminderViewModel;

public class ReminderViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final String injuryId;

    public ReminderViewModelFactory(Application application, String injuryId) {
        this.application = application;
        this.injuryId = injuryId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ReminderViewModel.class)) {
            return (T) new ReminderViewModel(application, injuryId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
