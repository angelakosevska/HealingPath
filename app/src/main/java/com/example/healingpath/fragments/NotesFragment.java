package com.example.healingpath.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healingpath.R;
import com.example.healingpath.adapters.NotesAdapter;
import com.example.healingpath.models.NoteItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotesFragment extends Fragment {

    private static final String ARG_INJURY_ID = "injury_id";

    private String injuryId;
    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private List<NoteItem> noteList = new ArrayList<>();


    public static NotesFragment newInstance(String injuryId) {
        NotesFragment fragment = new NotesFragment();
        Bundle args = new Bundle();
        args.putString("injury_id", injuryId);
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        recyclerView = view.findViewById(R.id.rv_notes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotesAdapter(noteList);
        recyclerView.setAdapter(adapter);
        adapter.setOnNoteLongClickListener((note, position) -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Note")
                    .setMessage("Are you sure you want to delete this note?")
                    .setPositiveButton("Yes", (dialog, which) -> deleteNote(note, position))
                    .setNegativeButton("No", null)
                    .show();
        });
        loadNotes();

        return view;
    }


    @SuppressLint("NotifyDataSetChanged")
    private void loadNotes() {
        if (injuryId == null) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("injuries")
                .document(injuryId)
                .collection("dailyNotes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    noteList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        NoteItem note = doc.toObject(NoteItem.class);
                        note.setId(doc.getId());
                        noteList.add(note);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
    private void deleteNote(NoteItem note, int position) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("injuries")
                .document(injuryId)
                .collection("dailyNotes")
                .document(note.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    noteList.remove(position);
                    adapter.notifyItemRemoved(position);
                });
    }
}
