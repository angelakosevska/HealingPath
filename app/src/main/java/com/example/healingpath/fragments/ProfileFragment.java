package com.example.healingpath.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Typeface;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.healingpath.R;
import com.example.healingpath.activities.LoginActivity;
import com.example.healingpath.utils.LocaleHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
        TextView textEnglish = view.findViewById(R.id.textEnglish);
        TextView textMacedonian = view.findViewById(R.id.textMacedonian);


        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch switchLanguage = view.findViewById(R.id.switchLanguage);

        SharedPreferences prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        String currentLang = prefs.getString("app_language", "en");

        switchLanguage.setChecked(currentLang.equals("mk"));
        updateLanguageColors(switchLanguage.isChecked(), textEnglish, textMacedonian);

        switchLanguage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String selectedLanguage = isChecked ? "mk" : "en";

            if (!selectedLanguage.equals(currentLang)) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("app_language", selectedLanguage);
                editor.apply();


                requireActivity().recreate();
            }

            updateLanguageColors(isChecked, textEnglish, textMacedonian);
        });



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
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isAnonymous()) {
            // Guest user UI adjustments
            textViewFullName.setText("Guest");
            textViewDOB.setVisibility(View.GONE);
            textViewEmail.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);
            logoutButton.setText("Exit Guest Mode");
        } else {
            // Regular user: load profile data
            loadUserProfile();

            textViewDOB.setVisibility(View.VISIBLE);
            textViewEmail.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.VISIBLE);
            logoutButton.setText(getString(R.string.logout));
        }
    }

    private void updateLanguageColors(boolean isMacedonianSelected, TextView english, TextView macedonian) {
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.colorAccent);
        int defaultColor = ContextCompat.getColor(requireContext(), android.R.color.darker_gray);

        // Update colors
        english.setTextColor(isMacedonianSelected ? defaultColor : selectedColor);
        macedonian.setTextColor(isMacedonianSelected ? selectedColor : defaultColor);

        // Update font weight
        english.setTypeface(null, isMacedonianSelected ? Typeface.NORMAL : Typeface.BOLD);
        macedonian.setTypeface(null, isMacedonianSelected ? Typeface.BOLD : Typeface.NORMAL);
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

                    // Step 1: Sign out from Firebase
                    mAuth.signOut();

                    // Step 2: Sign out and revoke Google account access
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id)) // Make sure this matches the one in google-services.json
                            .requestEmail()
                            .build();

                    GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

                    // This ensures Smart Lock doesn't automatically sign in again
                    googleSignInClient.revokeAccess().addOnCompleteListener(task -> {
                        googleSignInClient.signOut().addOnCompleteListener(task2 -> {
                            // Then go to Login screen
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


}
