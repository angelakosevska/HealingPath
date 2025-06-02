package com.example.healingpath.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.healingpath.models.NoteItem;
import com.example.healingpath.repositories.NoteRepository;

import java.util.List;

public class NoteViewModel extends AndroidViewModel {

    private final NoteRepository noteRepository;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        noteRepository = new NoteRepository(application);
    }

    public LiveData<List<NoteItem>> getNotesForInjury(String injuryId) {
        return noteRepository.getNotesForInjury(injuryId);
    }

    public void addNote(NoteItem note) {
        noteRepository.addNote(note);
    }

    public void syncNotesFromFirestore(String injuryId) {
        noteRepository.syncFromFirestore(injuryId);
    }

    public void syncPendingNotesToFirestore() {
        noteRepository.syncPendingNotesToFirestore();
    }
}
