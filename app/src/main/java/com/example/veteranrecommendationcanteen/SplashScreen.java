package com.example.veteranrecommendationcanteen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.veteranrecommendationcanteen.UI.LogIn;
import com.example.veteranrecommendationcanteen.UI.MainPage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        androidx.core.splashscreen.SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final boolean isLoggedIn;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        if (currentUser != null && !currentUser.isEmailVerified()) {
            FirebaseAuth.getInstance().signOut();
            isLoggedIn = false;

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isLoggedIn", false);
            editor.apply();
        } else {
            isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (isLoggedIn) {
                intent = new Intent(SplashScreen.this, MainPage.class);
            } else {
                intent = new Intent(SplashScreen.this, LogIn.class);
            }
            startActivity(intent);
            finish();
        }, 2000);
    }
}
