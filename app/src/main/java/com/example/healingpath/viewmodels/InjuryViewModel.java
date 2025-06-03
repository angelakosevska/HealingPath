package com.example.healingpath.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.healingpath.models.Injury;
import com.example.healingpath.repositories.InjuryRepository;

import java.util.List;

public class InjuryViewModel extends AndroidViewModel {

    private final InjuryRepository repository;
    private final LiveData<List<Injury>> allInjuries;

    public InjuryViewModel(@NonNull Application application) {
        super(application);
        repository = new InjuryRepository(application);
        allInjuries = repository.getAllInjuries();
        repository.syncFromFirestore();
        repository.syncPendingInjuriesToFirestore(); // Sync Room -> Firestore
    }

    public LiveData<List<Injury>> getAllInjuries() {
        return allInjuries;
    }

    public void addInjury(Injury injury) {
        repository.addInjury(injury); // adds to Firestore + Room
    }
    public void deleteInjury(Injury injury) {
        repository.deleteInjury(injury);
    }


}
