package com.example.veteranrecommendationcanteen.UI;

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

import com.example.veteranrecommendationcanteen.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogIn extends AppCompatActivity {

    EditText loginEmail, loginPassword;
    Button loginButton;
    TextView signupRedirectText, forgotPassword;
    FirebaseAuth mAuth;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        loginEmail = findViewById(R.id.email);
        loginPassword = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signupRedirectText = findViewById(R.id.signupRedirectText);
        forgotPassword = findViewById(R.id.forgotPassword);

        loginButton.setOnClickListener(view -> loginUser());

        signupRedirectText.setOnClickListener(view -> {
            Intent intent = new Intent(LogIn.this, SignUp.class);
            startActivity(intent);
        });

        setupClickableSignupText();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn && currentUser != null) {
            startActivity(new Intent(LogIn.this, MainPage.class));
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
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("userEmail", user.getEmail());
                        editor.apply();

                        Toast.makeText(LogIn.this, "LogIn Berhasil!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LogIn.this, MainPage.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("userEmail", user.getEmail());
                        startActivity(intent);
                        finish();
                    } else {
                        mAuth.signOut();
                        Toast.makeText(LogIn.this, "Verifikasi email Anda sebelum login!", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Toast.makeText(LogIn.this, "LogIn Gagal! Cek email dan password.", Toast.LENGTH_SHORT).show();
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
                Intent intent = new Intent(LogIn.this, SignUp.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(LogIn.this, R.color.Primary));
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
