package com.example.healingpath.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healingpath.R;
import com.example.healingpath.adapters.InjuryAdapter;
import com.example.healingpath.models.Injury;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class InjuriesFragment extends Fragment {

    private FirebaseFirestore db;
    private InjuryAdapter adapter;
    private ArrayList<Injury> injuryList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_injuries, container, false);

        db = FirebaseFirestore.getInstance();
        injuryList = new ArrayList<>();

        RecyclerView recyclerView = root.findViewById(R.id.rv_injuries);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new InjuryAdapter(getContext(), injuryList, this::openInjuryDetailFragment);
        recyclerView.setAdapter(adapter);

        Button btnAddInjury = root.findViewById(R.id.btn_add_injury);
        btnAddInjury.setOnClickListener(v -> showAddInjuryDialog());

        loadInjuriesFromFirestore();

        return root;
    }

    private void openInjuryDetailFragment(Injury injury) {
        InjuryDetailFragment fragment = InjuryDetailFragment.newInstance(injury.getId());

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showAddInjuryDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_injury, null);
        EditText titleInput = view.findViewById(R.id.et_title);
        EditText descInput = view.findViewById(R.id.et_description);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnSave = view.findViewById(R.id.btn_save);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String desc = descInput.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            String id = db.collection("injuries").document().getId();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Injury injury = new Injury(id, title, desc, System.currentTimeMillis(), userId);

            db.collection("injuries").document(id).set(injury)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Injury added", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }

    private void loadInjuriesFromFirestore() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("injuries")
                .whereEqualTo("userId", currentUserId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    injuryList.clear();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Injury injury = doc.toObject(Injury.class);
                            injuryList.add(injury);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
