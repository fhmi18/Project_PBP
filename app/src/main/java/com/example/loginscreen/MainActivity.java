package com.example.loginscreen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    TextView welcomeText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View logoutButton = findViewById(R.id.logoutButton);

        welcomeText = findViewById(R.id.welcomeText);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        if (username != null && !username.isEmpty()) {
            welcomeText.setText("Welcome, " + username + "!");
        } else {
            Toast.makeText(this, "No username received!", Toast.LENGTH_SHORT).show();
        }

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

    }
}
