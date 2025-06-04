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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.healingpath.R;
import com.example.healingpath.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends BaseActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Skip login if already authenticated
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
        textViewRegister = findViewById(R.id.textViewRegister);

        buttonLogin.setOnClickListener(v -> loginUser());
        findViewById(R.id.buttonLoginAsGuest).setOnClickListener(v -> signInAnonymously());
        findViewById(R.id.buttonLoginWithGoogle).setOnClickListener(v -> signInWithGoogle());

        // Set up the clickable and colored "Register" text
        String text = getString(R.string.register_prompt).replaceAll("<b>", "").replaceAll("</b>", "");
        String clickableText = getString(R.string.register_text);

        SpannableString spannableString = new SpannableString(text);

        int start = text.indexOf(clickableText);
        int end = start + clickableText.length();

// Make "Register" clickable
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

// Apply custom color from colors.xml
        int registerColor = ContextCompat.getColor(this, R.color.colorPrimaryLight);
        spannableString.setSpan(new ForegroundColorSpan(registerColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

// Set the styled text on the TextView
        textViewRegister.setText(spannableString);
        textViewRegister.setMovementMethod(LinkMovementMethod.getInstance());


        // Google Sign-In Options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))  // from google-services.json
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Signed in as Guest", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Guest login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInWithGoogle() {
        // Always show account picker
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveNewUserIfNeeded(user);
                        Toast.makeText(this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Firebase sign in failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveNewUserIfNeeded(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = user.getUid();

        db.collection("users").document(uid).get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                String fullName = user.getDisplayName();
                String[] parts = fullName != null ? fullName.split(" ", 2) : new String[]{"", ""};
                String firstName = parts.length > 0 ? parts[0] : "";
                String lastName = parts.length > 1 ? parts[1] : "";
                String email = user.getEmail() != null ? user.getEmail() : "";

                User newUser = new User(firstName, lastName, "", email);

                db.collection("users").document(uid).set(newUser)
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed to save user profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }
}
//showing extra screen
//package com.example.healingpath.activities;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.Spannable;
//import android.text.SpannableString;
//import android.text.method.LinkMovementMethod;
//import android.text.style.ClickableSpan;
//import android.text.style.ForegroundColorSpan;
//import android.util.Patterns;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.healingpath.R;
//import com.firebase.ui.auth.AuthUI;
//import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
//import com.firebase.ui.auth.IdpResponse;
//import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.Arrays;
//import java.util.List;
//
//public class LoginActivity extends AppCompatActivity {
//
//    private EditText editTextEmail, editTextPassword;
//    private Button buttonLogin;
//    private TextView textViewRegister;
//    private FirebaseAuth mAuth;
//    private ActivityResultLauncher<Intent> signInLauncher;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // Auto-skip if already signed in
//        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
//            return;
//        }
//
//        setContentView(R.layout.activity_login);
//
//        mAuth = FirebaseAuth.getInstance();
//
//        editTextEmail = findViewById(R.id.editTextEmail);
//        editTextPassword = findViewById(R.id.editTextPassword);
//        buttonLogin = findViewById(R.id.buttonLogin);
//        textViewRegister = findViewById(R.id.textViewRegister);
//
//        buttonLogin.setOnClickListener(v -> loginUser());
//        findViewById(R.id.buttonLoginAsGuest).setOnClickListener(v -> signInAnonymously());
//        findViewById(R.id.buttonLoginWithGoogle).setOnClickListener(v -> signInWithGoogle());
//
//        // Register link
//        String text = "Don't have an account? Register";
//        SpannableString spannableString = new SpannableString(text);
//        int start = text.indexOf("Register");
//        int end = start + "Register".length();
//        spannableString.setSpan(new ClickableSpan() {
//            @Override
//            public void onClick(View widget) {
//                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
//            }
//        }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        int registerColor = getResources().getColor(R.color.colorPrimaryLight);
//        spannableString.setSpan(new ForegroundColorSpan(registerColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        textViewRegister.setText(spannableString);
//        textViewRegister.setMovementMethod(LinkMovementMethod.getInstance());
//
//        // Modern sign-in launcher
//        signInLauncher = registerForActivityResult(
//                new FirebaseAuthUIActivityResultContract(),
//                this::onSignInResult
//        );
//    }
//
//    private void loginUser() {
//        String email = editTextEmail.getText().toString().trim();
//        String password = editTextPassword.getText().toString().trim();
//
//        if (email.isEmpty() || password.isEmpty()) {
//            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        mAuth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        startActivity(new Intent(this, MainActivity.class));
//                        finish();
//                    } else {
//                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
//                    }
//                });
//    }
//
//    private void signInAnonymously() {
//        mAuth.signInAnonymously()
//                .addOnCompleteListener(this, task -> {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(LoginActivity.this, "Signed in as Guest", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
//                        finish();
//                    } else {
//                        Toast.makeText(LoginActivity.this, "Guest login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
//                    }
//                });
//    }
//
//    private void signInWithGoogle() {
//        List<AuthUI.IdpConfig> providers = Arrays.asList(
//                new AuthUI.IdpConfig.GoogleBuilder().build()
//        );
//
//        Intent signInIntent = AuthUI.getInstance()
//                .createSignInIntentBuilder()
//                .setAvailableProviders(providers)
//                .setIsSmartLockEnabled(false)
//                .setAlwaysShowSignInMethodScreen(true)
//                .build();
//
//        signInLauncher.launch(signInIntent);
//    }
//
//
//    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
//        IdpResponse response = result.getIdpResponse();
//        if (result.getResultCode() == RESULT_OK) {
//            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//
//            // 🔽 Save user to Firestore if new
//            FirebaseFirestore db = FirebaseFirestore.getInstance();
//            String uid = user.getUid();
//
//            db.collection("users").document(uid).get().addOnSuccessListener(snapshot -> {
//                if (!snapshot.exists()) {
//                    String fullName = user.getDisplayName();
//                    String[] parts = fullName != null ? fullName.split(" ", 2) : new String[]{"", ""};
//
//                    String firstName = parts.length > 0 ? parts[0] : "";
//                    String lastName = parts.length > 1 ? parts[1] : "";
//                    String email = user.getEmail() != null ? user.getEmail() : "";
//
//                    com.example.healingpath.models.User newUser = new com.example.healingpath.models.User(firstName, lastName, "", email);
//
//                    db.collection("users").document(uid).set(newUser)
//                            .addOnSuccessListener(aVoid -> {
//                                // Optional success log
//                            })
//                            .addOnFailureListener(e -> {
//                                Toast.makeText(this, "Failed to save user profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                            });
//                }
//            });
//
//            Toast.makeText(this, "Welcome " + (user.getDisplayName() != null ? user.getDisplayName() : "user"), Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
//        } else {
//            String error = (response != null && response.getError() != null)
//                    ? response.getError().getMessage()
//                    : "Sign-in canceled";
//            Toast.makeText(this, "Sign-in failed: " + error, Toast.LENGTH_LONG).show();
//        }
//    }
//
//
//}