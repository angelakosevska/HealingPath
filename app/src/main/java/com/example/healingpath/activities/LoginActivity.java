package com.example.healingpath.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healingpath.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If the user is already logged in, go directly to the MainActivity
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister); // "Don't have an account? Register"

        // Set OnClickListener for the Login button
        buttonLogin.setOnClickListener(v -> loginUser());

        // Set clickable behavior for "Register" link
        String text = "Don't have an account? Register";  // The full sentence
        SpannableString spannableString = new SpannableString(text);

        // Find the starting and ending index of the word "Register"
        int start = text.indexOf("Register");
        int end = start + "Register".length();


        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // When the user clicks on "Register", navigate to RegisterActivity
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        int registerColor = getResources().getColor(R.color.colorPrimaryLight);
        spannableString.setSpan(new ForegroundColorSpan(registerColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        textViewRegister.setText(spannableString);
        textViewRegister.setMovementMethod(LinkMovementMethod.getInstance());  // Make it clickable
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate email and password
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Attempt login with Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Navigate to MainActivity if login is successful
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        // Show error message if login fails
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
