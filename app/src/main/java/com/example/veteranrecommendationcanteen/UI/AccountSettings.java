package com.example.veteranrecommendationcanteen.UI;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.veteranrecommendationcanteen.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class AccountSettings extends AppCompatActivity {

    private EditText etEmail, etUsername, etPassword, etConfirmPassword;
    private Button btnSave, btnCancel;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        initializeViews();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserData();

        btnSave.setOnClickListener(v -> showReauthenticationDialog());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void loadUserData() {
        etEmail.setText(currentUser.getEmail());

        DocumentReference userDocRef = db.collection("users").document(currentUser.getUid());
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                etUsername.setText(documentSnapshot.getString("user_name"));
            }
        });
    }

    private void showReauthenticationDialog() {
        EditText passwordInput = new EditText(this);
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setHint("Masukkan password Anda");
        passwordInput.setPadding(40, 30, 40, 30);

        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Identitas")
                .setMessage("Untuk alasan keamanan, masukkan password Anda.")
                .setView(passwordInput)
                .setPositiveButton("Konfirmasi", (dialog, which) -> {
                    String enteredPassword = passwordInput.getText().toString().trim();
                    if (enteredPassword.isEmpty()) {
                        Toast.makeText(this, "Password tidak boleh kosong.", Toast.LENGTH_SHORT).show();
                    } else {
                        reauthenticateUser(enteredPassword);
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void reauthenticateUser(String password) {
        String email = currentUser.getEmail();
        if (email == null) {
            Toast.makeText(this, "Email tidak ditemukan.", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        currentUser.reauthenticate(credential)
                .addOnSuccessListener(authResult -> validateAndSaveChanges())
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Autentikasi gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void validateAndSaveChanges() {
        String newEmail = etEmail.getText().toString().trim();
        String newUsername = etUsername.getText().toString().trim();
        String newPassword = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newEmail) || TextUtils.isEmpty(newUsername)) {
            Toast.makeText(this, "Email dan Username tidak boleh kosong.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.isEmpty()) {
            if (newPassword.length() < 6) {
                Toast.makeText(this, "Password minimal 6 karakter.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Password dan konfirmasi tidak cocok.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        db.collection("users")
                .whereEqualTo("user_name", newUsername)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean usernameTaken = false;
                    for (var doc : querySnapshot.getDocuments()) {
                        if (!doc.getId().equals(currentUser.getUid())) {
                            usernameTaken = true;
                            break;
                        }
                    }
                    if (usernameTaken) {
                        Toast.makeText(this, "Username sudah digunakan.", Toast.LENGTH_SHORT).show();
                    } else {
                        updateUserData(newEmail, newUsername, newPassword);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal memvalidasi username: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateUserData(String newEmail, String newUsername, String newPassword) {
        if (!newEmail.equals(currentUser.getEmail())) {
            @SuppressWarnings("deprecation")
            Runnable updateEmail = () -> currentUser.updateEmail(newEmail)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Email diperbarui.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal update email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            updateEmail.run();
        }

        if (!TextUtils.isEmpty(newPassword)) {
            currentUser.updatePassword(newPassword)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Password diperbarui.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal update password: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        DocumentReference userDocRef = db.collection("users").document(currentUser.getUid());
        Map<String, Object> updates = new HashMap<>();
        updates.put("user_name", newUsername);

        userDocRef.set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal update profil: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
