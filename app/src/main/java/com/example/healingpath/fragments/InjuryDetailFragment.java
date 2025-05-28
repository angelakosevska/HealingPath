package com.example.healingpath.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healingpath.R;
import com.example.healingpath.models.Injury;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class InjuryDetailFragment extends Fragment {

    private static final String ARG_INJURY_ID = "injury_id";
    private String injuryId;
    private TextView tvTitle, tvDescription;
    private SeekBar painSlider;
    private EditText etNote;
    private Button btnSaveNote;

    public static InjuryDetailFragment newInstance(String injuryId) {
        InjuryDetailFragment fragment = new InjuryDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INJURY_ID, injuryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            injuryId = getArguments().getString(ARG_INJURY_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_injury_detail, container, false);

        tvTitle = view.findViewById(R.id.tv_title);
        tvDescription = view.findViewById(R.id.tv_description);
        painSlider = view.findViewById(R.id.pain_slider);
        etNote = view.findViewById(R.id.et_note);
        btnSaveNote = view.findViewById(R.id.btn_save_note);

        loadInjuryDetails();

        btnSaveNote.setOnClickListener(v -> saveDailyNote());

        return view;
    }

    private void loadInjuryDetails() {
        FirebaseFirestore.getInstance().collection("injuries")
                .document(injuryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Injury injury = documentSnapshot.toObject(Injury.class);
                        if (injury != null) {
                            tvTitle.setText(injury.getTitle());
                            tvDescription.setText(injury.getDescription());
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading injury", Toast.LENGTH_SHORT).show());
    }

    private void saveDailyNote() {
        String noteText = etNote.getText().toString().trim();
        int painLevel = painSlider.getProgress();

        if (TextUtils.isEmpty(noteText)) {
            Toast.makeText(getContext(), "Note cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Map<String, Object> noteData = new HashMap<>();
        noteData.put("note", noteText);
        noteData.put("pain", painLevel);
        noteData.put("timestamp", System.currentTimeMillis());

        DocumentReference noteRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("injuries")
                .document(injuryId)
                .collection("dailyNotes")
                .document(todayDate);

        noteRef.set(noteData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Note saved", Toast.LENGTH_SHORT).show();
                    etNote.setText("");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to save note", Toast.LENGTH_SHORT).show());
    }
}
