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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.SeekBar;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healingpath.R;
import com.example.healingpath.ReminderReceiver;
import com.example.healingpath.models.ReminderModel;
import com.example.healingpath.repositories.ReminderRepository;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.content.Intent;


public class InjuryInfoFragment extends Fragment {
    private static final String ARG_INJURY_ID = "injury_id";
    private String injuryId;

    private TextView tvTitle, tvDescription;
    private SeekBar seekBarPain;
    private EditText etNoteInput;

    private ReminderRepository reminderRepository;
    private FirebaseAnalytics firebaseAnalytics;

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
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext());
        if (getArguments() != null) {
            injuryId = getArguments().getString(ARG_INJURY_ID);
        }
        reminderRepository = new ReminderRepository(requireContext(), injuryId);
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
        Button btnSaveNote = view.findViewById(R.id.btn_save_note);

        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {

                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
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

        Spinner spinnerMood = view.findViewById(R.id.spinner_mood);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.mood_options,
                R.layout.item_spinner
        );
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerMood.setAdapter(adapter);

        spinnerMood.setAdapter(adapter);
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
                input.setHint("e.g Appointment at 5pm");
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
                    reminder.put("type", "Custom");
                    reminder.put("note", note.isEmpty() ? "No note" : note);

                    String reminderId = FirebaseFirestore.getInstance()
                            .collection("tmp")
                            .document().getId();

                    ReminderModel reminderModel = new ReminderModel(note, date.getTimeInMillis(), reminderId);


                    reminderRepository.insert(reminderModel);
                    Bundle analyticsBundle = new Bundle();
                    analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "reminder_note");
                    analyticsBundle.putString("reminder_note", note);
                    analyticsBundle.putLong("reminder_time", date.getTimeInMillis());

                    firebaseAnalytics.logEvent("set_custom_reminder", analyticsBundle);

                    scheduleReminder(date.getTimeInMillis(), note, reminderId);

                    Toast.makeText(getContext(), "Reminder saved and scheduled!", Toast.LENGTH_SHORT).show();

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

        Spinner spinnerMood = requireView().findViewById(R.id.spinner_mood);
        String mood = spinnerMood.getSelectedItem().toString();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> note = new HashMap<>();
        note.put("note", noteText);
        note.put("timestamp", System.currentTimeMillis());
        note.put("pain", painLevel);
        note.put("mood", mood);

        etNoteInput.setEnabled(false);
        Bundle noteBundle = new Bundle();
        noteBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "injury_note");
        noteBundle.putString("injury_id", injuryId);
        noteBundle.putInt("pain_level", seekBarPain.getProgress());

        firebaseAnalytics.logEvent("save_note", noteBundle);

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
                    etNoteInput.setEnabled(true);
                    seekBarPain.setProgress(0);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Saved offline. Will sync later.", Toast.LENGTH_SHORT).show();
                    etNoteInput.setText("");
                    etNoteInput.setEnabled(true);
                });
    }


    private void scheduleReminder(long timestamp, String note, String reminderId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Intent intent = new Intent(getContext(), ReminderReceiver.class);
        intent.putExtra("note", note);
        intent.putExtra("reminderId", reminderId);
        intent.putExtra("injuryId", injuryId);
        intent.putExtra("userId", userId);

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
