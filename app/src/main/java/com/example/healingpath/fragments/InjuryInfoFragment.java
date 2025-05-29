package com.example.healingpath.fragments;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.SeekBar;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healingpath.R;
import com.example.healingpath.ReminderReceiver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.net.Uri;
import android.app.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class InjuryInfoFragment extends Fragment {
    private static final String ARG_INJURY_ID = "injury_id";
    private String injuryId;

    private TextView tvTitle, tvDescription;
    private SeekBar seekBarPain;
    private EditText etNoteInput;
    private Button btnSaveNote;
    private EditText etImageUrl;
    private Button btnUploadUrl;

    public static InjuryInfoFragment newInstance(String injuryId) {
        InjuryInfoFragment fragment = new InjuryInfoFragment();
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_injury_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitle = view.findViewById(R.id.tv_title);
        tvDescription = view.findViewById(R.id.tv_description);
        seekBarPain = view.findViewById(R.id.seekbar_pain);
        etNoteInput = view.findViewById(R.id.et_note_input);
        btnSaveNote = view.findViewById(R.id.btn_save_note);

        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Ask the user to allow exact alarms
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent); // or startActivityForResult if needed
                return; // Don't proceed until user accepts
            }
        }

        if (injuryId != null) {
            loadInjuryInfo();
        }

        btnSaveNote.setOnClickListener(v -> saveNote());

        Button btnSetReminder = view.findViewById(R.id.btn_set_reminder);
        btnSetReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });


    }

    private void showDateTimePicker() {
        Calendar currentDate = Calendar.getInstance();
        Calendar date = Calendar.getInstance();

        new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
            date.set(year, monthOfYear, dayOfMonth);

            new TimePickerDialog(getContext(), (view1, hourOfDay, minute) -> {
                date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                date.set(Calendar.MINUTE, minute);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Enter reminder note");

                final EditText input = new EditText(getContext());
                input.setHint("e.g. Take medication, Appointment at 5pm");
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                input.setLines(2);
                input.setMinLines(2);
                input.setMaxLines(4);
                builder.setView(input);

                builder.setPositiveButton("Save", (dialog, which) -> {
                    String note = input.getText().toString().trim();
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    Map<String, Object> reminder = new HashMap<>();
                    reminder.put("timestamp", date.getTimeInMillis());
                    reminder.put("type", "Custom"); // or use other values later
                    reminder.put("note", note.isEmpty() ? "No note" : note);

                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .collection("injuries")
                            .document(injuryId)
                            .collection("reminders")
                            .add(reminder)
                            .addOnSuccessListener(documentReference -> {
                                String reminderId = documentReference.getId();
                                scheduleReminder(date.getTimeInMillis(), note, reminderId);
                                Toast.makeText(getContext(), "Reminder saved and scheduled!", Toast.LENGTH_SHORT).show();
                            })


                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error saving reminder.", Toast.LENGTH_SHORT).show();
                            });
                });

                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                builder.show();

            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();

        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }


    private void loadInjuryInfo() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("injuries")
                .document(injuryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        String description = documentSnapshot.getString("description");
                        Long painLevel = documentSnapshot.getLong("painLevel");

                        tvTitle.setText(title != null ? title : "No Title");
                        tvDescription.setText(description != null ? description : "No Description");
                        seekBarPain.setProgress(painLevel != null ? painLevel.intValue() : 0);
                    }
                });
    }

    private void saveNote() {
        String noteText = etNoteInput.getText().toString().trim();
        int painLevel = seekBarPain.getProgress();

        if (noteText.isEmpty()) {
            Toast.makeText(getContext(), "Note cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> note = new HashMap<>();
        note.put("note", noteText);
        note.put("timestamp", System.currentTimeMillis());
        note.put("pain", painLevel);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("injuries")
                .document(injuryId)
                .collection("dailyNotes")
                .add(note)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(getContext(), "Note saved", Toast.LENGTH_SHORT).show();
                    etNoteInput.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error saving note", Toast.LENGTH_SHORT).show();
                });
    }

    private void scheduleReminder(long timestamp, String note, String reminderId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Intent intent = new Intent(getContext(), ReminderReceiver.class);
        intent.putExtra("note", note);
        intent.putExtra("reminderId", reminderId);
        intent.putExtra("injuryId", injuryId);
        intent.putExtra("userId", userId);  // ✅ Pass userId

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(),
                (int) timestamp,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    timestamp,
                    pendingIntent
            );
        }
    }


}
