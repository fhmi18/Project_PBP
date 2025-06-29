package com.example.veteranrecommendationcanteen.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.veteranrecommendationcanteen.R;
import com.example.veteranrecommendationcanteen.UI.MainFragment.HomeFragment;
import com.example.veteranrecommendationcanteen.UI.MainFragment.FavoriteFragment;
import com.example.veteranrecommendationcanteen.UI.MainFragment.HistoryFragment;
import com.example.veteranrecommendationcanteen.UI.MainFragment.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;

public class MainPage extends AppCompatActivity {

    private long backPressedTime;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setActiveNav("Home");

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("userEmail", "User");

        fragmentManager = getSupportFragmentManager();
        replaceFragment(new HomeFragment());

        ImageButton navLogo = findViewById(R.id.navLogo);
        navLogo.setOnClickListener(v -> {
            Intent intent = new Intent(MainPage.this, MyReviews.class);
            startActivity(intent);
        });

        findViewById(R.id.navHome).setOnClickListener(v -> {
            replaceFragment(new HomeFragment());
            setActiveNav("Home");
        });
        findViewById(R.id.navFavorite).setOnClickListener(v -> {
            replaceFragment(new FavoriteFragment());
            setActiveNav("Favorite");
        });
        findViewById(R.id.navHistory).setOnClickListener(v -> {
            replaceFragment(new HistoryFragment());
            setActiveNav("History");
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            replaceFragment(new ProfileFragment());
            setActiveNav("Profile");
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    finish();
                } else {
                    Toast.makeText(MainPage.this, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show();
                    backPressedTime = System.currentTimeMillis();
                }
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void setActiveNav(String active) {
        LinearLayout navHomeContainer = findViewById(R.id.navHomeContainer);
        ImageView navHome = findViewById(R.id.navHome);
        TextView navHomeText = findViewById(R.id.navHomeText);

        LinearLayout navFavoriteContainer = findViewById(R.id.navFavoriteContainer);
        ImageView navFavorite = findViewById(R.id.navFavorite);
        TextView navFavoriteText = findViewById(R.id.navFavoriteText);

        LinearLayout navHistoryContainer = findViewById(R.id.navHistoryContainer);
        ImageView navHistory = findViewById(R.id.navHistory);
        TextView navHistoryText = findViewById(R.id.navHistoryText);

        LinearLayout navProfileContainer = findViewById(R.id.navProfileContainer);
        ImageView navProfile = findViewById(R.id.navProfile);
        TextView navProfileText = findViewById(R.id.navProfileText);

        View backLogoLeft = findViewById(R.id.backgroundLogoLeft);
        View backLogoRight = findViewById(R.id.backgroundLogoRight);

        String[] ids = {"Home", "Favorite", "History", "Profile"};
        LinearLayout[] containers = {navHomeContainer, navFavoriteContainer, navHistoryContainer, navProfileContainer};
        ImageView[] icons = {navHome, navFavorite, navHistory, navProfile};
        TextView[] texts = {navHomeText, navFavoriteText, navHistoryText, navProfileText};

        for (int i = 0; i < ids.length; i++) {
            if (ids[i].equals(active)) {
                containers[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                icons[i].setColorFilter(ContextCompat.getColor(this, R.color.Primary));
                texts[i].setTextColor(ContextCompat.getColor(this, R.color.Primary));
            } else {
                containers[i].setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
                icons[i].setColorFilter(ContextCompat.getColor(this, R.color.white));
                texts[i].setTextColor(ContextCompat.getColor(this, R.color.white));
            }
        }

        if (active.equals(ids[1])) {
            backLogoLeft.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            backLogoRight.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        } else if (active.equals(ids[2])) {
            backLogoRight.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            backLogoLeft.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        } else {
            backLogoRight.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            backLogoLeft.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        }
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        Intent intent = new Intent(MainPage.this, LogIn.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
