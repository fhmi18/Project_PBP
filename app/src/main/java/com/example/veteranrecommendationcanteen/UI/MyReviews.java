package com.example.veteranrecommendationcanteen.UI;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.veteranrecommendationcanteen.MenuItem;
import com.example.veteranrecommendationcanteen.MyReviewsItem;
import com.example.veteranrecommendationcanteen.MyReviewsAdapter;
import com.example.veteranrecommendationcanteen.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyReviews extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyReviewsAdapter adapter;
    private List<MyReviewsItem> myReviewList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reviews);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        setupToolbar();
        setupRecyclerView();

        if (currentUser == null) {
            Toast.makeText(this, "Please log in to see your reviews.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchMyReviews();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.myReviewsRecyclerView);
        myReviewList = new ArrayList<>();
        adapter = new MyReviewsAdapter(this, myReviewList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void fetchMyReviews() {
        db.collectionGroup("ulasan").whereEqualTo("user_id", currentUser.getUid()).get()
                .addOnSuccessListener(ulasanSnapshots -> {
                    if (ulasanSnapshots.isEmpty()) {
                        Toast.makeText(this, "You haven't made any reviews yet.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<Task<DocumentSnapshot>> dataFetchTasks = new ArrayList<>();
                    Map<String, QueryDocumentSnapshot> ulasanDocsMap = new HashMap<>();

                    for (QueryDocumentSnapshot ulasanDoc : ulasanSnapshots) {
                        DocumentReference menuRef = ulasanDoc.getReference().getParent().getParent();
                        if (menuRef != null) {
                            dataFetchTasks.add(menuRef.get());
                            DocumentReference canteenRef = menuRef.getParent().getParent();
                            dataFetchTasks.add(canteenRef.get());
                            ulasanDocsMap.put(menuRef.getPath(), ulasanDoc);
                        }
                    }

                    Tasks.whenAllSuccess(dataFetchTasks).addOnSuccessListener(results -> {
                        myReviewList.clear();
                        for (int i = 0; i < results.size(); i += 2) {
                            DocumentSnapshot menuDoc = (DocumentSnapshot) results.get(i);
                            DocumentSnapshot canteenDoc = (DocumentSnapshot) results.get(i+1);
                            QueryDocumentSnapshot ulasanDoc = ulasanDocsMap.get(menuDoc.getReference().getPath());

                            if (menuDoc.exists() && canteenDoc.exists() && ulasanDoc != null) {
                                MyReviewsItem reviewItem = new MyReviewsItem();

                                reviewItem.setKomentar(ulasanDoc.getString("komentar"));
                                reviewItem.setRating(ulasanDoc.getDouble("rating"));

                                MenuItem menuItem = menuDoc.toObject(MenuItem.class);
                                reviewItem.setMenuName(menuItem.getName());
                                if(menuItem.getGambarUrl() != null && !menuItem.getGambarUrl().isEmpty()) {
                                    reviewItem.setMenuImageUrl(menuItem.getGambarUrl().get(0));
                                }

                                reviewItem.setCanteenName(canteenDoc.getString("nama_kantin"));

                                reviewItem.setMenuId(menuDoc.getId());
                                reviewItem.setCategoryPath(menuDoc.getReference().getParent().getId());
                                reviewItem.setCanteenId(canteenDoc.getId());
                                reviewItem.setCampusId(canteenDoc.getReference().getParent().getParent().getId());

                                myReviewList.add(reviewItem);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    });

                }).addOnFailureListener(e -> Toast.makeText(this, "Failed to load reviews.", Toast.LENGTH_SHORT).show());
    }
}