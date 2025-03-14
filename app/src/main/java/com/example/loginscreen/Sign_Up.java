package com.example.loginscreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Sign_Up extends AppCompatActivity {

    private EditText email, password, conpassword;
    private Button signUpButton;
    private TextView loginRedirectText;
    private ProgressBar progressBar;

    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        conpassword = findViewById(R.id.conpassword);
        signUpButton = findViewById(R.id.SignUpButton);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();

        signUpButton.setOnClickListener(view -> registerUser());

        loginRedirectText.setOnClickListener(view -> {
            startActivity(new Intent(Sign_Up.this, Login.class));
        });
    }

    private void registerUser() {
        String emailInput = email.getText().toString().trim();
        String passwordInput = password.getText().toString().trim();
        String confirmPasswordInput = conpassword.getText().toString().trim();

        if (emailInput.isEmpty()) {
            email.setError("Email tidak boleh kosong!");
            email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            email.setError("Masukkan email yang valid!");
            email.requestFocus();
            return;
        }

        if (passwordInput.isEmpty()) {
            password.setError("Password tidak boleh kosong!");
            password.requestFocus();
            return;
        }

        if (passwordInput.length() < 6) {
            password.setError("Password harus minimal 6 karakter!");
            password.requestFocus();
            return;
        }

        if (!passwordInput.equals(confirmPasswordInput)) {
            conpassword.setError("Password tidak cocok!");
            conpassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(emailInput, passwordInput)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(Sign_Up.this, "Registrasi berhasil! Cek email untuk verifikasi.", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(Sign_Up.this, "Gagal mengirim email verifikasi.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                        progressBar.setVisibility(View.GONE);
                        startActivity(new Intent(Sign_Up.this, Login.class));
                        finish();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Sign_Up.this, "Registrasi gagal: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}