package com.example.healingpath.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.DatePickerDialog;

import java.util.Calendar;
import java.util.Locale;

import com.example.healingpath.models.User;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healingpath.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends BaseActivity {

    private EditText editTextFirstName, editTextLastName, editTextDOB, editTextEmailRegister, editTextPasswordRegister, editTextConfirmPasswordRegister;
    private Button buttonRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView textViewLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize EditTexts for the new fields
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextDOB = findViewById(R.id.editTextDOB);
        editTextDOB.setFocusable(false);
        editTextDOB.setOnClickListener(v -> showDatePickerDialog());
        editTextEmailRegister = findViewById(R.id.editTextEmailRegister);
        editTextPasswordRegister = findViewById(R.id.editTextPasswordRegister);
        editTextConfirmPasswordRegister = findViewById(R.id.editTextConfirmPasswordRegister);

        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin); // "Already have an account? Login"

        buttonRegister.setOnClickListener(v -> registerUser());

        // Set clickable "Login" text with color and click listener
        String text = "Already have an account? Login";  // Full sentence
        SpannableString spannableString = new SpannableString(text);

        // Find the start and end of the word "Login"
        int start = text.indexOf("Login");
        int end = start + "Login".length();

        // Make "Login" clickable
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // When clicked, navigate to LoginActivity
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish(); // Optional: close RegisterActivity so user can't return via back button
            }
        }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set color for the word "Login"
        int loginColor = getResources().getColor(R.color.colorPrimaryLight);
        spannableString.setSpan(new ForegroundColorSpan(loginColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the SpannableString to the TextView and enable click handling
        textViewLogin.setText(spannableString);
        textViewLogin.setMovementMethod(LinkMovementMethod.getInstance());  // Enable clickability
    }

    private void registerUser() {
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String dob = editTextDOB.getText().toString().trim();
        String email = editTextEmailRegister.getText().toString().trim();
        String password = editTextPasswordRegister.getText().toString().trim();
        String confirmPassword = editTextConfirmPasswordRegister.getText().toString().trim();

        // Check if fields are empty
        if (firstName.isEmpty() || lastName.isEmpty() || dob.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if email is valid
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check password strength (at least 6 characters)
        if (password.length() < 6) {
            Toast.makeText(this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Register user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // User registered successfully, save additional info to Firestore
                saveUserData(firstName, lastName, dob, email);
            } else {
                // Display Firebase error message
                Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Save additional user information (first name, last name, DOB) to Firestore
    private void saveUserData(String firstName, String lastName, String dob, String email) {
        // Create a map to store user data
        User user;
        user = new User(firstName, lastName, dob, email);

        // Get the current user's UID from Firebase Auth
        String userId = mAuth.getCurrentUser().getUid();

        // Save data to Firestore under a "users" collection
        db.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> {
                    // Successfully saved user data to Firestore, navigate to MainActivity
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Display error if saving to Firestore fails
                    Toast.makeText(RegisterActivity.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Check if the email is in a valid format
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    // Format date as dd/MM/yyyy
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    editTextDOB.setText(formattedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

}
