package com.example.loginscreen;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.TextPaint;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Sign_Up extends AppCompatActivity {

    private EditText email, password, conpassword;
    private Button signUpButton;
    private TextView loginRedirectText;
    private ProgressBar progressBar;
    private ImageButton backButton;
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
        backButton = findViewById(R.id.backButton);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        signUpButton.setOnClickListener(view -> {
            signUpButton.setEnabled(false);
            registerUser();
        });

        setupClickableLoginText();

        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(Sign_Up.this, Login.class);
            startActivity(intent);
            finish();
        });
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
                                            Toast.makeText(Sign_Up.this,
                                                    "Registrasi berhasil! Cek email untuk verifikasi.",
                                                    Toast.LENGTH_LONG).show();
                                            saveUserData(user.getUid(), emailInput);
                                        } else {
                                            showSnackbar("Gagal mengirim email verifikasi.");
                                        }
                                    });

                            // Panggil metode validasi email untuk memastikan pengguna telah verifikasi
                            validateEmailVerification();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        signUpButton.setEnabled(true);

                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            showSnackbar("Email sudah terdaftar. Silakan gunakan email lain.");
                        } else {
                            showSnackbar("Registrasi gagal: " + task.getException().getMessage());
                        }
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
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    private void validateEmailVerification() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    Toast.makeText(Sign_Up.this, "Email telah diverifikasi!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Sign_Up.this, Login.class));
                    finish();
                } else {
                    Toast.makeText(Sign_Up.this, "Silakan verifikasi email Anda terlebih dahulu.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void setupClickableLoginText() {
        String fullText = "Sudah punya akun? Login sekarang";
        SpannableString spannableString = new SpannableString(fullText);
        int startIndex = fullText.indexOf("Login sekarang");

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(Sign_Up.this, Login.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(Sign_Up.this, R.color.Primary));
                ds.setUnderlineText(false);
            }
        };

        spannableString.setSpan(clickableSpan, startIndex, startIndex + "Login sekarang".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
