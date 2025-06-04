package com.example.healingpath.fragments;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healingpath.viewmodels.ReminderViewModel;
import com.example.healingpath.viewmodels.ReminderViewModelFactory;


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
    private ReminderViewModel viewModel;
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

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.recycler_view_reminders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReminderAdapter(reminderList);
        recyclerView.setAdapter(adapter);
        adapter.setOnReminderLongClickListener((reminder, position) -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Reminder")
                    .setMessage("Are you sure you want to delete this reminder?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        viewModel.deleteReminder(reminder);
                        Toast.makeText(requireContext(), "Reminder deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        ReminderViewModelFactory factory = new ReminderViewModelFactory(requireActivity().getApplication(), injuryId);
        viewModel = new ViewModelProvider(this, factory).get(ReminderViewModel.class);

        viewModel.getReminders().observe(getViewLifecycleOwner(), reminders -> {
            reminderList.clear();
            reminderList.addAll(reminders);
            adapter.notifyDataSetChanged();
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
