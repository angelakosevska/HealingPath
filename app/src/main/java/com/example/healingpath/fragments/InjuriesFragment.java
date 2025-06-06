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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healingpath.R;
import com.example.healingpath.adapters.InjuryAdapter;
import com.example.healingpath.models.Injury;
import com.example.healingpath.viewmodels.InjuryViewModel;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class InjuriesFragment extends Fragment {

    private InjuryViewModel viewModel;
    private InjuryAdapter adapter;
    private ArrayList<Injury> injuryList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_injuries, container, false);

        injuryList = new ArrayList<>();

        RecyclerView recyclerView = root.findViewById(R.id.rv_injuries);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new InjuryAdapter(
                getContext(),
                injuryList,
                this::openInjuryDetailFragment,
                this::confirmDeleteInjury
        );
        recyclerView.setAdapter(adapter);

        Button btnAddInjury = root.findViewById(R.id.btn_add_injury);
        btnAddInjury.setOnClickListener(v -> showAddInjuryDialog());


        viewModel = new ViewModelProvider(this).get(InjuryViewModel.class);
        viewModel.getAllInjuries().observe(getViewLifecycleOwner(), injuries -> {
            injuryList.clear();
            injuryList.addAll(injuries);
            adapter.notifyDataSetChanged();
        });

        return root;
    }
    private void deleteInjury(Injury injury) {
        viewModel.deleteInjury(injury); // Assuming your ViewModel handles Firestore removal
    }

    private void openInjuryDetailFragment(Injury injury) {
        InjuryDetailsFragment fragment = InjuryDetailsFragment.newInstance(injury.getId());

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
    private void confirmDeleteInjury(Injury injury) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Injury")
                .setMessage("Are you sure you want to delete this injury?")
                .setPositiveButton("Delete", (dialog, which) -> deleteInjury(injury))
                .setNegativeButton("Cancel", null)
                .show();
    }
    @Override
    public void onResume() {
        super.onResume();

        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(requireContext());
        analytics.logEvent("opened_injuries_fragment", null);
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

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String id = FirebaseFirestore.getInstance()
                    .collection("users").document(userId)
                    .collection("injuries").document().getId();

            Injury injury = new Injury(id, title, desc, System.currentTimeMillis(), userId);

            // Add and dismiss immediately (offline-first)
            viewModel.addInjury(injury);
            dialog.dismiss();
        });


        dialog.show();
    }


//    private void loadInjuriesFromFirestore() {
//        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        db.collection("users")
//                .document(currentUserId)
//                .collection("injuries")
//                .addSnapshotListener((snapshots, e) -> {
//                    if (e != null) {
//                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    injuryList.clear();
//                    if (snapshots != null) {
//                        for (QueryDocumentSnapshot doc : snapshots) {
//                            Injury injury = doc.toObject(Injury.class);
//                            injuryList.add(injury);
//                        }
//                        adapter.notifyDataSetChanged();
//                    }
//                });
//    }
}
