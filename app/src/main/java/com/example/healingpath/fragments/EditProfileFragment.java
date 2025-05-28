package com.example.healingpath.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
// import android.widget.ImageView; // Commented out
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healingpath.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
// import com.google.firebase.storage.StorageReference; // Commented out

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private EditText editTextFirstName, editTextLastName, editTextEmail, editTextDOB;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // private static final int PICK_IMAGE_REQUEST = 1; // Commented out
    // private Uri imageUri; // Commented out
    // private ImageView imageViewEditPhoto; // Commented out
    // private StorageReference storageRef; // Commented out

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextFirstName = view.findViewById(R.id.editTextFirstName);
        editTextLastName = view.findViewById(R.id.editTextLastName);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextDOB = view.findViewById(R.id.editTextDOB);
        Button saveButton = view.findViewById(R.id.buttonSaveProfile);

        // Make DOB field show calendar when clicked
        editTextDOB.setFocusable(false);
        editTextDOB.setClickable(true);
        editTextDOB.setOnClickListener(v -> showDatePickerDialog());

        loadUserData();

        /*
        imageViewEditPhoto = view.findViewById(R.id.imageViewEditPhoto); // Commented out
        storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().getReference("profile_pics");
        imageViewEditPhoto.setOnClickListener(v -> openFileChooser());
        */

        saveButton.setOnClickListener(v -> saveProfileChanges());
    }

    /*
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageViewEditPhoto.setImageURI(imageUri); // Show preview in ImageView
        }
    }
    */

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    // Format: dd/MM/yyyy
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    editTextDOB.setText(formattedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        editTextFirstName.setText(documentSnapshot.getString("firstName"));
                        editTextLastName.setText(documentSnapshot.getString("lastName"));
                        editTextEmail.setText(documentSnapshot.getString("email"));
                        editTextDOB.setText(documentSnapshot.getString("dob"));
                    }
                });
    }

    private void saveProfileChanges() {
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String dob = editTextDOB.getText().toString().trim();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("email", email);
        updates.put("dob", dob);


        updateUserData(currentUser.getUid(), updates);
    }

    private void updateUserData(String uid, Map<String, Object> updates) {
        db.collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();

                        Bundle result = new Bundle();
                        result.putBoolean("profileUpdated", true);
                        getParentFragmentManager().setFragmentResult("editProfileResult", result);

                        getParentFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
