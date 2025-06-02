package com.example.healingpath.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.healingpath.dao.ReminderDao;
import com.example.healingpath.database.AppDatabase;
import com.example.healingpath.models.ReminderModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ReminderRepository {
    private final ReminderDao reminderDao;
    private final FirebaseFirestore firestore;
    private final String userId;
    private final String injuryId;

    public ReminderRepository(Context context, String injuryId) {
        AppDatabase db = com.example.healingpath.database.DatabaseClient
                .getInstance(context)
                .getAppDatabase();
        reminderDao = db.reminderDao();
        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.injuryId = injuryId;
    }

    public void insert(ReminderModel reminder) {
        new Thread(() -> {
            reminderDao.insert(reminder);
            syncToFirestore(reminder);
        }).start();
    }

    public void update(ReminderModel reminder) {
        new Thread(() -> {
            reminderDao.update(reminder);
            syncToFirestore(reminder);
        }).start();
    }

    public void delete(ReminderModel reminder) {
        new Thread(() -> {
            reminderDao.delete(reminder);
            deleteFromFirestore(reminder);
        }).start();
    }

    public LiveData<List<ReminderModel>> getAllReminders() {
        return reminderDao.getAllReminders();
    }

    private void syncToFirestore(ReminderModel reminder) {
        firestore.collection("users")
                .document(userId)
                .collection("injuries")
                .document(injuryId)
                .collection("reminders")
                .document(reminder.getReminderId())
                .set(reminder);
    }

    private void deleteFromFirestore(ReminderModel reminder) {
        firestore.collection("users")
                .document(userId)
                .collection("injuries")
                .document(injuryId)
                .collection("reminders")
                .document(reminder.getReminderId())
                .delete();
    }
}
