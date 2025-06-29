package com.example.veteranrecommendationcanteen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.veteranrecommendationcanteen.UI.LogIn;
import com.example.veteranrecommendationcanteen.UI.MainPage;

public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null && !currentUser.isEmailVerified()) {
                FirebaseAuth.getInstance().signOut();
                isLoggedIn = false;
            }

            Intent intent;
            if (isLoggedIn && currentUser != null) {
                intent = new Intent(SplashScreen.this, MainPage.class);
            } else {
                intent = new Intent(SplashScreen.this, LogIn.class);
            }

            startActivity(intent);
            finish();
        }, 2000);
    }
}
