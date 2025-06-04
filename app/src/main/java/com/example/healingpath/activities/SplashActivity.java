package com.example.healingpath.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healingpath.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends BaseActivity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logoImage = findViewById(R.id.logoImage);
        TextView welcomeText = findViewById(R.id.welcomeText);
        progressBar = findViewById(R.id.progressBar);

        fadeInView(logoImage, 0);
        fadeInView(welcomeText, 800);
        showProgressBar();

        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            Intent intent;

            if (currentUser != null) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }
    private void fadeInView(View view, long delay) {
        view.setVisibility(View.VISIBLE);
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(1000);
        anim.setStartOffset(delay);
        anim.setFillAfter(true);
        view.startAnimation(anim);

    }

    private void showProgressBar() {
        new Handler().postDelayed(() -> progressBar.setVisibility(View.VISIBLE), 1600);
    }
}
