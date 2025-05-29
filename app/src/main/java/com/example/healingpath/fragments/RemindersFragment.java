package com.example.healingpath.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healingpath.R;
import com.example.healingpath.adapters.ReminderAdapter;
import com.example.healingpath.models.ReminderModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RemindersFragment extends Fragment {
    private static final String ARG_INJURY_ID = "injury_id";
    private String injuryId;

    private RecyclerView recyclerView;
    private ReminderAdapter adapter;
    private List<ReminderModel> reminderList = new ArrayList<>();
    private String userId;

    public static RemindersFragment newInstance(String injuryId) {
        RemindersFragment fragment = new RemindersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INJURY_ID, injuryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            injuryId = getArguments().getString(ARG_INJURY_ID);
        }
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.recycler_view_reminders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReminderAdapter(reminderList);
        recyclerView.setAdapter(adapter);
        adapter.setOnReminderLongClickListener((reminder, position) -> {
            deleteReminder(reminder, position);
        });

        loadRemindersForInjury(injuryId);
    }

    private void loadRemindersForInjury(String injuryId) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("injuries")
                .document(injuryId)
                .collection("reminders")
                .get()
                .addOnSuccessListener(reminderSnapshot -> {
                    reminderList.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : reminderSnapshot.getDocuments()) {
                        Long timestamp = doc.getLong("timestamp");
                        String note = doc.getString("note");
                        String reminderId = doc.getId(); // ✅ Firestore doc ID

                        if (timestamp != null && note != null) {
                            reminderList.add(new ReminderModel(note, timestamp, reminderId));
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load reminders", Toast.LENGTH_SHORT).show();
                });
    }
    private void deleteReminder(ReminderModel reminder, int position) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("injuries")
                .document(injuryId)
                .collection("reminders")
                .document(reminder.getReminderId())
                .delete()
                .addOnSuccessListener(unused -> {
                    reminderList.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(getContext(), "Reminder deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to delete reminder", Toast.LENGTH_SHORT).show();
                });
    }

}
