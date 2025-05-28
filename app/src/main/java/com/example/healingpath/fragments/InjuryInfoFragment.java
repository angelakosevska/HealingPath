package com.example.healingpath.fragments;

import android.os.Bundle;
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
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class InjuryInfoFragment extends Fragment {
    private static final String ARG_INJURY_ID = "injury_id";
    private String injuryId;

    private TextView tvTitle, tvDescription;
    private SeekBar seekBarPain;
    private EditText etNoteInput;
    private Button btnSaveNote;

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

        if (injuryId != null) {
            loadInjuryInfo();
        }

        btnSaveNote.setOnClickListener(v -> saveNote());
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

        // Create note data
        Map<String, Object> note = new HashMap<>();
        note.put("text", noteText);
        note.put("timestamp", System.currentTimeMillis());
        note.put("painLevel", painLevel);

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
}



