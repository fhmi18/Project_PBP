package com.example.veteranrecommendationcanteen.UI;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.veteranrecommendationcanteen.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ProfileSettings extends AppCompatActivity {

    private EditText etNama;
    private Button btnSave, btnCancel;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DocumentReference userDocRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userDocRef = db.collection("users").document(currentUser.getUid());

        initializeViews();
        loadUserData();
        setupButtons();
    }

    private void initializeViews() {
        etNama = findViewById(R.id.etNama);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    /**
     * Mengambil data nama dari Firestore dan menampilkannya di EditText.
     */
    private void loadUserData() {
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String currentName = documentSnapshot.getString("nama");
                etNama.setText(currentName);
            } else {
                Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Mengatur listener untuk tombol Simpan dan Batal.
     */
    private void setupButtons() {
        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String newName = etNama.getText().toString().trim();

            Map<String, Object> updates = new HashMap<>();
            updates.put("nama", newName);

            userDocRef.set(updates, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}