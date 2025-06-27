package com.example.veteranrecommendationcanteen.UI;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.veteranrecommendationcanteen.MenuItem;
import com.example.veteranrecommendationcanteen.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MenuDetail extends AppCompatActivity {

    private ImageView foodImage;
    private TextView foodName, foodDesc, discountedPrice, originalPrice;
    private ImageButton btnClose;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_menu);

        String campusId = getIntent().getStringExtra("CAMPUS_ID");
        String canteenId = getIntent().getStringExtra("CANTEEN_ID");
        String categoryPath = getIntent().getStringExtra("CATEGORY_PATH");
        String menuId = getIntent().getStringExtra("MENU_ID");

        if (campusId == null || canteenId == null || categoryPath == null || menuId == null) {
            Toast.makeText(this, "Error: Menu data is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        db = FirebaseFirestore.getInstance();

        fetchMenuDetails(campusId, canteenId, categoryPath, menuId);

        btnClose.setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        foodImage = findViewById(R.id.foodImage);
        foodName = findViewById(R.id.foodName);
        foodDesc = findViewById(R.id.foodDesc);
        discountedPrice = findViewById(R.id.discountedPrice);
        originalPrice = findViewById(R.id.originalPrice);
        btnClose = findViewById(R.id.btnClose);
    }

    private void fetchMenuDetails(String campusId, String canteenId, String categoryPath, String menuId) {
        DocumentReference menuDocRef = db.collection("kampus").document(campusId)
                .collection("kantin").document(canteenId)
                .collection(categoryPath).document(menuId);

        menuDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                MenuItem menuItem = documentSnapshot.toObject(MenuItem.class);
                if (menuItem != null) {
                    populateUI(menuItem);
                }
            } else {
                Toast.makeText(this, "Menu not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load menu details.", Toast.LENGTH_SHORT).show();
        });
    }

    private void populateUI(MenuItem menuItem) {
        foodName.setText(menuItem.getName());
        foodDesc.setText(menuItem.getDescription());
        discountedPrice.setText(String.format("Rp%,d", menuItem.getPrice()));
        originalPrice.setVisibility(View.GONE);

        Glide.with(this)
                .load(menuItem.getFotoUrl())
                .placeholder(R.drawable.app_logo)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(foodImage);
    }
}
