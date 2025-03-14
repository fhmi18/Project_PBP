package com.example.loginscreen;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.util.Patterns;
import android.text.TextPaint;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;

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
    private DatabaseReference databaseReference;

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
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        signUpButton.setOnClickListener(view -> {
            signUpButton.setEnabled(false);
            registerUser();
        });

        setupClickableLoginText();
    }

    private void registerUser() {
        String emailInput = email.getText().toString().trim();
        String passwordInput = password.getText().toString().trim();
        String confirmPasswordInput = conpassword.getText().toString().trim();

        if (!validateInputs(emailInput, passwordInput, confirmPasswordInput)) {
            signUpButton.setEnabled(true);
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
                                            showSnackbar("Registrasi berhasil! Cek email untuk verifikasi.");
                                            saveUserData(user.getUid(), emailInput);
                                        } else {
                                            showSnackbar("Gagal mengirim email verifikasi.");
                                        }
                                    });
                        }

                        progressBar.setVisibility(View.GONE);
                        startActivity(new Intent(Sign_Up.this, Login.class));
                        finish();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        showSnackbar("Registrasi gagal: " + task.getException().getMessage());
                        signUpButton.setEnabled(true);
                    }
                });
    }

    private boolean validateInputs(String emailInput, String passwordInput, String confirmPasswordInput) {
        if (emailInput.isEmpty()) {
            email.setError("Email tidak boleh kosong!");
            email.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            email.setError("Masukkan email yang valid!");
            email.requestFocus();
            return false;
        }

        if (passwordInput.isEmpty()) {
            password.setError("Password tidak boleh kosong!");
            password.requestFocus();
            return false;
        }

        if (passwordInput.length() < 6) {
            password.setError("Password harus minimal 6 karakter!");
            password.requestFocus();
            return false;
        }

        if (!passwordInput.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")) {
            password.setError("Gunakan kombinasi huruf besar, kecil, dan angka!");
            password.requestFocus();
            return false;
        }

        if (!passwordInput.equals(confirmPasswordInput)) {
            conpassword.setError("Password tidak cocok!");
            conpassword.requestFocus();
            return false;
        }

        return true;
    }

    private void saveUserData(String userId, String email) {
        User user = new User(userId, email);
        databaseReference.child(userId).setValue(user).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                showSnackbar("Gagal menyimpan data pengguna.");
            }
        });
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_LONG).show();
    }

    private void setupClickableLoginText() {
        String fullText = "Already have an account? Login now";
        SpannableString spannableString = new SpannableString(fullText);
        int startIndex = fullText.indexOf("Login now");

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(Sign_Up.this, Login.class));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(Sign_Up.this, R.color.Primary));
                ds.setUnderlineText(false);
            }
        };

        spannableString.setSpan(clickableSpan, startIndex, startIndex + "Login now".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        loginRedirectText.setText(spannableString);
        loginRedirectText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    static class User {
        public String userId, email;

        public User(String userId, String email) {
            this.userId = userId;
            this.email = email;
        }
    }
}
