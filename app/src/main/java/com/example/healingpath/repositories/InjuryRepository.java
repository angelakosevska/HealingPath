package com.example.healingpath.repositories;

import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.LiveData;

import com.example.healingpath.dao.InjuryDao;
import com.example.healingpath.database.DatabaseClient;
import com.example.healingpath.models.Injury;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InjuryRepository {
    private final Context context;
    private final InjuryDao injuryDao;
    private final FirebaseFirestore firestore;
    private final String userId;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public InjuryRepository(Context context) {
        this.context = context.getApplicationContext();
        injuryDao = DatabaseClient.getInstance(context).getAppDatabase().injuryDao();
        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
    public void syncPendingInjuriesToFirestore() {
        executor.execute(() -> {
            List<Injury> unsyncedInjuries = injuryDao.getUnsyncedInjuries();
            for (Injury injury : unsyncedInjuries) {
                firestore.collection("users")
                        .document(userId)
                        .collection("injuries")
                        .document(injury.getId())
                        .set(injury)
                        .addOnSuccessListener(aVoid -> {
                            injury.setSynced(true);
                            executor.execute(() -> injuryDao.update(injury)); // update Room
                        });
            }
        });
    }

    // Get all injuries (LiveData from Room)
    public LiveData<List<Injury>> getAllInjuries() {
        return injuryDao.getInjuriesForUser(userId);
    }

    // Insert injury into local Room DB
    public void insertLocal(Injury injury) {
        executor.execute(() -> injuryDao.insert(injury));
    }

    public void syncFromFirestore() {
        firestore.collection("users")
                .document(userId)
                .collection("injuries")
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Injury injury = doc.toObject(Injury.class);
                        injury.setId(doc.getId());
                        insertLocal(injury);
                    }
                });
    }

    public void addInjury(Injury injury) {
        insertLocal(injury);

        firestore.collection("users")
                .document(userId)
                .collection("injuries")
                .document(injury.getId())
                .set(injury)
                .addOnSuccessListener(aVoid -> {
                    injury.setSynced(true);
                    executor.execute(() -> injuryDao.update(injury));
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "Injury synced successfully!", Toast.LENGTH_SHORT).show()
                    );
                })
                .addOnFailureListener(e -> {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "You're offline. Injury will sync later.", Toast.LENGTH_SHORT).show()
                    );
                });
    }
    public void deleteInjury(Injury injury) {
        executor.execute(() -> {

            injuryDao.delete(injury);

            firestore.collection("users")
                    .document(userId)
                    .collection("injuries")
                    .document(injury.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                                Toast.makeText(context, "Injury deleted successfully!", Toast.LENGTH_SHORT).show()
                        );
                    })
                    .addOnFailureListener(e -> {
                        new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                                Toast.makeText(context, "Failed to delete injury. Will retry later.", Toast.LENGTH_SHORT).show()
                        );
                        e.printStackTrace();
                    });
        });
    }

}
