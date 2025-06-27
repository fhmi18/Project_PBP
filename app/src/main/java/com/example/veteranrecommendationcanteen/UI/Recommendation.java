package com.example.veteranrecommendationcanteen.UI;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.veteranrecommendationcanteen.CanteenItem;
import com.example.veteranrecommendationcanteen.MenuItem;
import com.example.veteranrecommendationcanteen.R;
import com.example.veteranrecommendationcanteen.RecommendationAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Recommendation extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private RecommendationAdapter adapter;
    private List<Object> recommendationList;
    private TextView titleTextView, subtitleTextView;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation);

        db = FirebaseFirestore.getInstance();

        titleTextView = findViewById(R.id.titleTextView);
        subtitleTextView = findViewById(R.id.subtitleTextView);
        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerView);

        recommendationList = new ArrayList<>();
        adapter = new RecommendationAdapter(this, recommendationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        String recommendationType = getIntent().getStringExtra("RECOMMENDATION_TYPE");
        if (recommendationType == null) {
            Toast.makeText(this, "Tipe rekomendasi tidak ditemukan.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        updateUITitles(recommendationType);
        fetchData(recommendationType);

        btnBack.setOnClickListener(v -> finish());
    }

    private void updateUITitles(String type) {
        titleTextView.setText(type);
        switch (type) {
            case "Canteen":
                subtitleTextView.setText("Explore best canteens around you");
                break;
            case "Food":
                subtitleTextView.setText("Catch delicious main courses");
                break;
            case "Dessert & Snack":
                subtitleTextView.setText("Sweet treats and savory bites");
                break;
            case "Beverage":
                subtitleTextView.setText("Fresh drinks to quench your thirst");
                break;
        }
    }

    private void fetchData(String type) {
        recommendationList.clear();
        switch (type) {
            case "Canteen":
                fetchCanteens();
                break;
            case "Food":
                fetchMenuItems("food");
                break;
            case "Dessert & Snack":
                fetchMenuItems("dessert_snacks");
                break;
            case "Beverage":
                fetchMenuItems("beverage");
                break;
        }
    }

    private void fetchCanteens() {
        db.collectionGroup("kantin")
                // .orderBy("rating_keseluruhan", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            CanteenItem canteen = document.toObject(CanteenItem.class);
                            canteen.setCanteenId(document.getId());
                            canteen.setCampusId(document.getReference().getParent().getParent().getId());
                            recommendationList.add(canteen);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(Recommendation.this, "Gagal memuat data kantin.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchMenuItems(String collectionPath) {
        db.collectionGroup(collectionPath)
                // .orderBy("rating_menu", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            recommendationList.add(document.toObject(MenuItem.class));
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(Recommendation.this, "Gagal memuat data menu.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
