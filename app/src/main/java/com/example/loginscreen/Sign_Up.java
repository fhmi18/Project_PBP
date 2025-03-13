package com.example.loginscreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Sign_Up extends AppCompatActivity {

    EditText signupConfirmPassword, signupUsername, signupPassword;
    TextView loginRedirectText;
    Button signupButton;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signupUsername = findViewById(R.id.username);
        signupPassword = findViewById(R.id.password);
        signupConfirmPassword = findViewById(R.id.conpassword);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        signupButton = findViewById(R.id.SignUpButton);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");


                String username = signupUsername.getText().toString().trim();
                String password = signupPassword.getText().toString().trim();
                String confirmPassword = signupConfirmPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(Sign_Up.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(Sign_Up.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(Sign_Up.this, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }


                MethodAuth methodAuth = new MethodAuth(username, "******", "******");
                reference.child(username).setValue(methodAuth);

                Toast.makeText(Sign_Up.this, "You have signed up successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Sign_Up.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Sign_Up.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
    }
}