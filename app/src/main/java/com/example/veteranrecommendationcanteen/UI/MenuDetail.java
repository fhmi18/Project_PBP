package com.example.veteranrecommendationcanteen.UI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.veteranrecommendationcanteen.ImageSliderAdapter;
import com.example.veteranrecommendationcanteen.MenuItem;
import com.example.veteranrecommendationcanteen.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MenuDetail extends AppCompatActivity {

    private ViewPager2 imageSliderViewPager;
    private TabLayout tabLayoutIndicator;
    private ImageSliderAdapter sliderAdapter;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private List<String> imageUrls = new ArrayList<>();

    private TextView foodName, foodDesc, rating, discountedPrice, originalPrice, canteenNameTextView, totalLikeTextView;
    private ImageButton btnClose, btnLike, btnFavorite;
    private Button btnSend, btnLookReview;
    private EditText commentBox;
    private RatingBar ratingBar;

    private FirebaseFirestore db;
    private boolean isLiked = false;
    private boolean isFavorited = false;
    private DocumentReference menuDocRef;
    private String campusId, canteenId, categoryPath, menuId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_menu);

        campusId = getIntent().getStringExtra("CAMPUS_ID");
        canteenId = getIntent().getStringExtra("CANTEEN_ID");
        categoryPath = getIntent().getStringExtra("CATEGORY_PATH");
        menuId = getIntent().getStringExtra("MENU_ID");

        if (campusId == null || canteenId == null || categoryPath == null || menuId == null) {
            Toast.makeText(this, "Error: Menu data is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        db = FirebaseFirestore.getInstance();
        menuDocRef = db.collection("kampus").document(campusId)
                .collection("kantin").document(canteenId)
                .collection(categoryPath).document(menuId);

        fetchMenuDetails();
        btnClose.setOnClickListener(v -> finish());
        setupLookReviewButton();
    }

    private void initializeViews() {
        imageSliderViewPager = findViewById(R.id.imageSliderViewPager);
        tabLayoutIndicator = findViewById(R.id.tabLayoutIndicator);
        foodName = findViewById(R.id.foodName);
        foodDesc = findViewById(R.id.foodDesc);
        rating = findViewById(R.id.rating);
        discountedPrice = findViewById(R.id.discountedPrice);
        originalPrice = findViewById(R.id.originalPrice);
        btnClose = findViewById(R.id.btnClose);
        canteenNameTextView = findViewById(R.id.canteenName);
        btnLike = findViewById(R.id.btnLike);
        btnFavorite = findViewById(R.id.btnFavorite);
        totalLikeTextView = findViewById(R.id.totalLike);
        commentBox = findViewById(R.id.commentBox);
        ratingBar = findViewById(R.id.ratingBar);
        btnSend = findViewById(R.id.btnSend);
        btnLookReview = findViewById(R.id.btnLookReview);
    }

    private void fetchMenuDetails() {
        menuDocRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) { return; }
            if (documentSnapshot != null && documentSnapshot.exists()) {
                MenuItem menuItem = documentSnapshot.toObject(MenuItem.class);
                if (menuItem != null) {
                    populateUI(menuItem);
                    setupCanteenLink();
                    setupLikeButton();
                    setupFavoriteButton();
                    setupReviewSystem();
                    addHistory();
                }
            } else {
                Toast.makeText(this, "Menu not found.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUI(MenuItem menuItem) {
        foodName.setText(menuItem.getName());
        foodDesc.setText(menuItem.getDescription());
        rating.setText("Rating: " + menuItem.getRating());
        discountedPrice.setText(String.format(Locale.US, "Rp%,d", menuItem.getPrice()));
        originalPrice.setVisibility(View.GONE);

        Number likeCount = menuItem.getTotalLike();
        totalLikeTextView.setText(String.format(Locale.US, "%,d orang menyukai menu ini.", likeCount != null ? likeCount.longValue() : 0));

        if (menuItem.getGambarUrl() != null && !menuItem.getGambarUrl().isEmpty()) {
            imageUrls.clear();
            imageUrls.addAll(menuItem.getGambarUrl());
            setupImageSlider();
        }
    }

    private void setupLikeButton() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DocumentReference likedMenuRef = db.collection("users").document(user.getUid())
                .collection("liked_menus").document(menuId + "_" + canteenId);
        likedMenuRef.get().addOnSuccessListener(documentSnapshot -> {
            isLiked = documentSnapshot.exists();
            updateLikeButtonUI();
        });

        btnLike.setOnClickListener(v -> handleLikeClick(likedMenuRef));
    }

    private void updateLikeButtonUI() {
        if (isLiked) {
            btnLike.setImageResource(R.drawable.ic_liked);
        } else {
            btnLike.setImageResource(R.drawable.ic_like);
        }
    }

    private void handleLikeClick(DocumentReference likedMenuRef) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in to like a menu.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLike.setEnabled(false);
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot menuSnapshot = transaction.get(menuDocRef);
            Long currentLikes = menuSnapshot.getLong("jumlah_like");
            long likeCount = currentLikes != null ? currentLikes : 0;

            long newLikes = isLiked ? Math.max(0, likeCount - 1) : likeCount + 1;
            transaction.update(menuDocRef, "jumlah_like", newLikes);
            return null;
        }).addOnSuccessListener(aVoid -> {
            if (isLiked) {
                likedMenuRef.delete();
            } else {
                Map<String, Object> likeData = new HashMap<>();
                likeData.put("campusId", campusId);
                likedMenuRef.set(likeData);
            }

            isLiked = !isLiked;
            updateLikeButtonUI();
            btnLike.setEnabled(true);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to update like.", Toast.LENGTH_SHORT).show();
            btnLike.setEnabled(true);
        });
    }

    private void setupFavoriteButton() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DocumentReference favoriteRef = db.collection("users").document(user.getUid())
                .collection("favorites").document(menuId + "_" + canteenId);
        favoriteRef.get().addOnSuccessListener(documentSnapshot -> {
            isFavorited = documentSnapshot.exists();
            updateFavoriteButtonUI();
        });

        btnFavorite.setOnClickListener(v -> handleFavoriteClick(favoriteRef));
    }

    private void updateFavoriteButtonUI() {
        if (isFavorited) {
            btnFavorite.setImageResource(R.drawable.ic_loved);
            btnFavorite.setColorFilter(ContextCompat.getColor(this, R.color.Primary));
        } else {
            btnFavorite.setImageResource(R.drawable.ic_love);
        }
    }

    private void handleFavoriteClick(DocumentReference favoriteRef) {
        btnFavorite.setEnabled(false);

        if (isFavorited) {
            favoriteRef.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    isFavorited = false;
                    updateFavoriteButtonUI();
                    Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to remove favorite", Toast.LENGTH_SHORT).show();
                }
                btnFavorite.setEnabled(true);
            });
        } else {
            Map<String, Object> favoriteData = new HashMap<>();
            favoriteData.put("menuId", menuId);
            favoriteData.put("campusId", campusId);
            favoriteData.put("canteenId", canteenId);
            favoriteData.put("categoryPath", categoryPath);
            favoriteData.put("addedAt", FieldValue.serverTimestamp());

            favoriteRef.set(favoriteData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    isFavorited = true;
                    updateFavoriteButtonUI();
                    Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to add favorite", Toast.LENGTH_SHORT).show();
                }
                btnFavorite.setEnabled(true);
            });
        }
    }

    private void setupReviewSystem() {
        btnSend.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Please log in to submit a review.", Toast.LENGTH_SHORT).show();
                return;
            }

            String comment = commentBox.getText().toString().trim();
            float rating = ratingBar.getRating();
            if (rating == 0 || TextUtils.isEmpty(comment)) {
                Toast.makeText(this, "Please provide rating and comment.", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSend.setEnabled(false);

            String userId = user.getUid();
            String reviewId = "u_" + userId + "_" + System.currentTimeMillis();

            Map<String, Object> reviewData = new HashMap<>();
            reviewData.put("user_id", userId);
            reviewData.put("komentar", comment);
            reviewData.put("rating", rating);
            reviewData.put("waktu", FieldValue.serverTimestamp());

            menuDocRef.collection("ulasan").document(reviewId)
                    .set(reviewData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Review submitted successfully!", Toast.LENGTH_SHORT).show();
                        commentBox.setText("");
                        ratingBar.setRating(0);
                        btnSend.setEnabled(true);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to submit review.", Toast.LENGTH_SHORT).show();
                        btnSend.setEnabled(true);
                    });
        });
    }

    private void addHistory() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        DocumentReference historyRef = db.collection("users").document(user.getUid())
                .collection("history").document(menuId + "_" + canteenId);
        Map<String, Object> historyData = new HashMap<>();
        historyData.put("menuId", menuId);
        historyData.put("campusId", campusId);
        historyData.put("canteenId", canteenId);
        historyData.put("categoryPath", categoryPath);
        historyData.put("visitedAt", FieldValue.serverTimestamp());
        historyRef.set(historyData);
    }

    private void setupImageSlider() {
        sliderAdapter = new ImageSliderAdapter(this, imageUrls, position -> {
            Intent intent = new Intent(MenuDetail.this, FullscreenImageActivity.class);
            intent.putExtra(FullscreenImageActivity.EXTRA_IMAGE_URL, imageUrls.get(position));
            startActivity(intent);
        });
        imageSliderViewPager.setAdapter(sliderAdapter);
        new TabLayoutMediator(tabLayoutIndicator, imageSliderViewPager, (tab, position) -> {}).attach();
        if (imageUrls.size() > 1) {
            setupAutoSlider();
        }
    }

    private void setupAutoSlider() {
        sliderRunnable = () -> {
            int currentItem = imageSliderViewPager.getCurrentItem();
            int nextItem = (currentItem + 1) % imageUrls.size();
            imageSliderViewPager.setCurrentItem(nextItem, true);
        };
        imageSliderViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 5000);
            }
        });
    }

    private void setupCanteenLink() {
        db.collection("kampus").document(campusId).collection("kantin").document(canteenId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String canteenName = documentSnapshot.getString("nama_kantin");
                        canteenNameTextView.setText(canteenName);
                        canteenNameTextView.setOnClickListener(v -> {
                            Intent intent = new Intent(MenuDetail.this, CanteenDetail.class);
                            intent.putExtra("CAMPUS_ID", campusId);
                            intent.putExtra("CANTEEN_ID", canteenId);
                            startActivity(intent);
                        });
                    }
                });
    }

    private void setupLookReviewButton() {
        btnLookReview.setOnClickListener(v -> {
            Intent intent = new Intent(MenuDetail.this, ReviewList.class);
            intent.putExtra("CAMPUS_ID", campusId);
            intent.putExtra("CANTEEN_ID", canteenId);
            intent.putExtra("CATEGORY_PATH", categoryPath);
            intent.putExtra("MENU_ID", menuId);
            startActivity(intent);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sliderRunnable != null && imageUrls.size() > 1) {
            sliderHandler.postDelayed(sliderRunnable, 5000);
        }
    }
}