package com.example.veteranreccanteen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    EditText loginEmail, loginPassword;
    Button loginButton;
    TextView signupRedirectText, forgotPassword;
    FirebaseAuth mAuth;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        loginEmail = findViewById(R.id.email);
        loginPassword = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signupRedirectText = findViewById(R.id.signupRedirectText);
        forgotPassword = findViewById(R.id.forgotPassword);

        loginButton.setOnClickListener(view -> loginUser());

        signupRedirectText.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, Sign_Up.class);
            startActivity(intent);
        });

        setupClickableSignupText();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Jika sudah login sebelumnya, langsung ke MainActivity
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(Login.this, Homepage.class));
            finish();
        }
    }

    private void loginUser() {
        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        if (email.isEmpty()) {
            loginEmail.setError("Email tidak boleh kosong!");
            loginEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            loginPassword.setError("Password tidak boleh kosong!");
            loginPassword.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    if (user.isEmailVerified()) {
                        // Simpan status login jika email sudah diverifikasi
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("userEmail", user.getEmail());
                        editor.apply();

                        Toast.makeText(Login.this, "Login Berhasil!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Login.this, Homepage.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("userEmail", user.getEmail());
                        startActivity(intent);
                        finish();
                    } else {
                        // Jika email belum diverifikasi, logout otomatis
                        mAuth.signOut();
                        Toast.makeText(Login.this, "Verifikasi email Anda sebelum login!", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Toast.makeText(Login.this, "Login Gagal! Cek email dan password.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickableSignupText() {
        String fullText = "Belum memiliki akun? Sign Up sekarang";
        SpannableString spannableString = new SpannableString(fullText);
        String clickableText = "Sign Up sekarang";
        int startIndex = fullText.indexOf(clickableText);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(Login.this, Sign_Up.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(Login.this, R.color.Primary));
                ds.setUnderlineText(false);
            }
        };
        spannableString.setSpan(
                clickableSpan,
                startIndex,
                startIndex + clickableText.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        signupRedirectText.setText(spannableString);
        signupRedirectText.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
