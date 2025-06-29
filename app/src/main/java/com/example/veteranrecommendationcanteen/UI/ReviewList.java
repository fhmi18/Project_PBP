package com.example.veteranrecommendationcanteen.UI;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.veteranrecommendationcanteen.R;
import com.example.veteranrecommendationcanteen.ReviewAdapter;
import com.example.veteranrecommendationcanteen.ReviewItem;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReviewList extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private RecyclerView reviewsRecyclerView;
    private ReviewAdapter adapter;
    private TextView tvMenuName, tvCanteenName;
    private MaterialButtonToggleGroup toggleButton;

    private List<ReviewItem> allReviewsList = new ArrayList<>();
    private String menuId, canteenId, campusId, categoryPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_list);

        menuId = getIntent().getStringExtra("MENU_ID");
        canteenId = getIntent().getStringExtra("CANTEEN_ID");
        campusId = getIntent().getStringExtra("CAMPUS_ID");
        categoryPath = getIntent().getStringExtra("CATEGORY_PATH");

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        setupToolbar();
        initializeViews();
        setupRecyclerView();
        setupToggleButtons();

        fetchHeaderInfo();
        fetchReviews();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initializeViews() {
        tvMenuName = findViewById(R.id.tvMenuName);
        tvCanteenName = findViewById(R.id.tvCanteenName);
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        toggleButton = findViewById(R.id.toggleButton);
    }

    private void setupRecyclerView() {
        adapter = new ReviewAdapter(this, new ArrayList<>());
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecyclerView.setAdapter(adapter);
    }

    private void setupToggleButtons() {
        toggleButton.check(R.id.btnTopComments);
        toggleButton.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnTopComments) {
                    showTopComments();
                } else if (checkedId == R.id.btnMyComments) {
                    showMyComments();
                }
            }
        });
    }

    private void fetchHeaderInfo() {
        db.collection("kampus").document(campusId).collection("kantin").document(canteenId)
                .collection(categoryPath).document(menuId).get()
                .addOnSuccessListener(doc -> tvMenuName.setText(doc.getString("nama_menu")));

        db.collection("kampus").document(campusId).collection("kantin").document(canteenId).get()
                .addOnSuccessListener(doc -> tvCanteenName.setText(doc.getString("nama_kantin")));
    }

    private void fetchReviews() {
        DocumentReference menuRef = db.collection("kampus").document(campusId)
                .collection("kantin").document(canteenId)
                .collection(categoryPath).document(menuId);

        menuRef.collection("ulasan").orderBy("waktu", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(ulasanSnapshots -> {
                    if (ulasanSnapshots.isEmpty()) {
                        Toast.makeText(this, "No reviews yet.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<Task<DocumentSnapshot>> userTasks = new ArrayList<>();
                    List<ReviewItem> tempReviewList = new ArrayList<>();

                    for (QueryDocumentSnapshot ulasanDoc : ulasanSnapshots) {
                        ReviewItem item = ulasanDoc.toObject(ReviewItem.class);
                        item.setUserId(ulasanDoc.getString("user_id"));
                        tempReviewList.add(item);
                        userTasks.add(db.collection("users").document(item.getUserId()).get());
                    }

                    Tasks.whenAllSuccess(userTasks).addOnSuccessListener(userSnapshots -> {
                        allReviewsList.clear();
                        for(int i = 0; i < tempReviewList.size(); i++) {
                            ReviewItem reviewItem = tempReviewList.get(i);
                            DocumentSnapshot userDoc = (DocumentSnapshot) userSnapshots.get(i);

                            if (userDoc.exists()) {
                                reviewItem.setUserName(userDoc.getString("user_name")); // Asumsi nama field-nya "nama"
                                reviewItem.setUserProfileUrl(userDoc.getString("profile_photo")); // Asumsi nama field-nya "fotoProfil"
                            }
                            allReviewsList.add(reviewItem);
                        }
                        showTopComments();
                    });

                }).addOnFailureListener(e -> Toast.makeText(this, "Failed to load reviews.", Toast.LENGTH_SHORT).show());
    }

    private void showTopComments() {
        allReviewsList.sort(Comparator.comparing(ReviewItem::getRating).reversed());
        adapter.updateData(allReviewsList);
    }

    private void showMyComments() {
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to see your comments.", Toast.LENGTH_SHORT).show();
            adapter.updateData(new ArrayList<>());
            return;
        }
        List<ReviewItem> myReviews = allReviewsList.stream()
                .filter(review -> currentUser.getUid().equals(review.getUserId()))
                .collect(Collectors.toList());
        adapter.updateData(myReviews);
    }
}