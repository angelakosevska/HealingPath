package com.example.healingpath.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.healingpath.R;
import com.example.healingpath.activities.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView textViewFullName, textViewDOB, textViewEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        textViewFullName = view.findViewById(R.id.textViewFullName);
        textViewDOB = view.findViewById(R.id.textViewDOB);
        textViewEmail = view.findViewById(R.id.textViewEmail);

        Button logoutButton = view.findViewById(R.id.buttonLogout);
        ImageButton editButton = view.findViewById(R.id.buttonEditProfile);

        logoutButton.setOnClickListener(v -> showLogoutConfirmation());
        editButton.setOnClickListener(v -> openEditProfileFragment());
        getParentFragmentManager().setFragmentResultListener(
                "editProfileResult",
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    boolean updated = result.getBoolean("profileUpdated", false);
                    if (updated) {
                        loadUserProfile(); // Refresh the profile
                    }
                }
        );

        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String dob = documentSnapshot.getString("dob");
                        String email = documentSnapshot.getString("email");

                        textViewFullName.setText(firstName + " " + lastName);
                        textViewDOB.setText(dob);
                        textViewEmail.setText(email);
                    } else {
                        Toast.makeText(getContext(), "User profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void openEditProfileFragment() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, new EditProfileFragment());
        transaction.addToBackStack(null);

        transaction.commit();

    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(getContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}
