package com.example.veteranrecommendationcanteen.UI.MainFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.veteranrecommendationcanteen.R;
import com.example.veteranrecommendationcanteen.UI.AccountSettings;
import com.example.veteranrecommendationcanteen.UI.LogIn;
import com.example.veteranrecommendationcanteen.UI.ProfileSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ImageView profileImage;
    private TextView tvName, tvUsername;
    private Button btnRatings;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        initializeViews(view);

        if (currentUser == null) {
            startActivity(new Intent(getContext(), LogIn.class));
            requireActivity().finish();
            return;
        }

        loadProfileData();
        setupButtons(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentUser != null) {
            loadProfileData();
        }
    }

    private void initializeViews(View view) {
        profileImage = view.findViewById(R.id.profile_image);
        tvName = view.findViewById(R.id.tv_name);
        tvUsername = view.findViewById(R.id.tv_username);
        btnRatings = view.findViewById(R.id.btn_ratings);
    }

    private void loadProfileData() {
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        tvName.setText(documentSnapshot.getString("nama"));
                        tvUsername.setText(documentSnapshot.getString("user_name"));

                        String photoUrl = documentSnapshot.getString("profile_photo");
                        if (getContext() != null && photoUrl != null && !photoUrl.isEmpty()) {
                            Glide.with(getContext())
                                    .load(photoUrl)
                                    .placeholder(R.drawable.ic_profile)
                                    .into(profileImage);
                        }
                    } else {
                        Toast.makeText(getContext(), "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load user data.", Toast.LENGTH_SHORT).show();
                });

        db.collectionGroup("ulasan")
                .whereEqualTo("user_id", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int ratingsCount = queryDocumentSnapshots.size();
                    btnRatings.setText(ratingsCount + " Ratings");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to count ratings.", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupButtons(View view) {
        TextView btnAccountSettings = view.findViewById(R.id.btn_account_settings);
        ImageButton btnProfileSettings = view.findViewById(R.id.btn_edit_profile);

        View.OnClickListener toAccountListener = v -> {
            startActivity(new Intent(getContext(), AccountSettings.class));
        };
        View.OnClickListener toProfileListener = v -> {
            startActivity(new Intent(getContext(), ProfileSettings.class));
        };


        btnAccountSettings.setOnClickListener(toAccountListener);
        btnProfileSettings.setOnClickListener(toProfileListener);

        TextView btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), LogIn.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}