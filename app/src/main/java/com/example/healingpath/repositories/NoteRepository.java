package com.example.healingpath.repositories;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.lifecycle.LiveData;

import com.example.healingpath.dao.NoteDao;
import com.example.healingpath.database.DatabaseClient;
import com.example.healingpath.models.NoteItem;
import com.example.healingpath.utils.AppExecutors;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteRepository {

    private final NoteDao noteDao;
    private final FirebaseFirestore firestore;
    private final String userId;
    private final ExecutorService executor;
    private final Context context;

    public NoteRepository(Context context) {
        this.context = context.getApplicationContext();
        this.noteDao = DatabaseClient.getInstance(context).getAppDatabase().noteDao();
        this.firestore = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<NoteItem>> getNotesForInjury(String injuryId) {
        return noteDao.getNotesForInjury(injuryId);
    }

    public void insertLocal(NoteItem note) {
        executor.execute(() -> noteDao.insert(note));
    }

    public void addNote(NoteItem note) {
        insertLocal(note);

        firestore.collection("users")
                .document(userId)
                .collection("injuries")
                .document(note.getInjuryId())
                .collection("notes")
                .document(note.getId())
                .set(note)
                .addOnSuccessListener(aVoid -> {
                    note.setSynced(true);
                    executor.execute(() -> noteDao.update(note));
                })
                .addOnFailureListener(e -> new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "You're offline. Note will sync later.", Toast.LENGTH_SHORT).show()
                ));
    }
    public void deleteNote(NoteItem note) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            noteDao.delete(note);


            firestore.collection("users")
                    .document(userId)
                    .collection("injuries")
                    .document(note.getInjuryId())
                    .collection("notes")
                    .document(note.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                                Toast.makeText(context, "Note deleted successfully!", Toast.LENGTH_SHORT).show()
                        );
                    })
                    .addOnFailureListener(e -> {
                        new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                                Toast.makeText(context, "Failed to delete note. Will retry later.", Toast.LENGTH_SHORT).show()
                        );
                        e.printStackTrace();
                    });
        });
    }


    public void syncFromFirestore(String injuryId) {
        firestore.collection("users")
                .document(userId)
                .collection("injuries")
                .document(injuryId)
                .collection("notes")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        NoteItem note = doc.toObject(NoteItem.class);
                        note.setId(doc.getId());
                        note.setSynced(true);
                        insertLocal(note);
                    }
                });
    }

    public void syncPendingNotesToFirestore() {
        executor.execute(() -> {
            List<NoteItem> unsynced = noteDao.getUnsyncedNotes();
            for (NoteItem note : unsynced) {
                firestore.collection("users")
                        .document(userId)
                        .collection("injuries")
                        .document(note.getInjuryId())
                        .collection("notes")
                        .document(note.getId())
                        .set(note)
                        .addOnSuccessListener(aVoid -> {
                            note.setSynced(true);
                            executor.execute(() -> noteDao.update(note));
                        });
            }
        });
    }
}
