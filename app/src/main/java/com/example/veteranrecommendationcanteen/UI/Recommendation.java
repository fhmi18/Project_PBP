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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Recommendation extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private RecommendationAdapter adapter;
    private List<Object> recommendationList;
    private TextView titleTextView, subtitleTextView;
    private ImageButton btnBack;
    private String selectedCampusId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation);

        db = FirebaseFirestore.getInstance();
        initializeViews();

        String recommendationType = getIntent().getStringExtra("RECOMMENDATION_TYPE");
        selectedCampusId = getIntent().getStringExtra("SELECTED_CAMPUS_ID");

        if (recommendationType == null || selectedCampusId == null) {
            Toast.makeText(this, "Required data is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        updateUITitles(recommendationType);
        fetchData(recommendationType);
        btnBack.setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        titleTextView = findViewById(R.id.titleTextView);
        subtitleTextView = findViewById(R.id.subtitleTextView);
        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerView);
        recommendationList = new ArrayList<>();
        adapter = new RecommendationAdapter(this, recommendationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void updateUITitles(String type) {
        titleTextView.setText(type);
        switch (type) {
            case "Canteen": subtitleTextView.setText("Explore best canteens around you"); break;
            case "Food": subtitleTextView.setText("Catch delicious main courses"); break;
            case "Dessert & Snack": subtitleTextView.setText("Sweet treats and savory bites"); break;
            case "Beverage": subtitleTextView.setText("Fresh drinks to quench your thirst"); break;
        }
    }

    private void fetchData(String type) {
        recommendationList.clear();
        adapter.notifyDataSetChanged();

        switch (type) {
            case "Canteen": fetchCanteens(); break;
            case "Food": fetchMenuItems("food"); break;
            case "Dessert & Snack": fetchMenuItems("dessert_snacks"); break;
            case "Beverage": fetchMenuItems("beverage"); break;
        }
    }

    private void fetchCanteens() {
        Query query;
        if ("ALL".equals(selectedCampusId)) {
            query = db.collectionGroup("kantin").orderBy("rating_keseluruhan", Query.Direction.DESCENDING);
        } else {
            query = db.collection("kampus").document(selectedCampusId)
                    .collection("kantin").orderBy("rating_keseluruhan", Query.Direction.DESCENDING);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                recommendationList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    CanteenItem canteen = document.toObject(CanteenItem.class);
                    canteen.setCanteenId(document.getId());
                    if ("ALL".equals(selectedCampusId)) {
                        // Dari dokumen di koleksi 'kantin', induknya adalah dokumen 'kampus'
                        canteen.setCampusId(document.getReference().getParent().getParent().getId());
                    } else {
                        canteen.setCampusId(selectedCampusId);
                    }
                    recommendationList.add(canteen);
                }
                adapter.notifyDataSetChanged();
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(Recommendation.this, "Failed to load canteen data: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchMenuItems(String collectionPath) {
        if ("ALL".equals(selectedCampusId)) {
            fetchMenuItemsFromAllCampuses(collectionPath);
        } else {
            fetchMenuItemsFromSpecificCampus(collectionPath, selectedCampusId);
        }
    }

    private void fetchMenuItemsFromAllCampuses(String collectionPath) {
        db.collectionGroup(collectionPath).orderBy("rating_menu", Query.Direction.DESCENDING).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        recommendationList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MenuItem menuItem = document.toObject(MenuItem.class);
                            menuItem.setMenuId(document.getId());

                            CollectionReference categoryRef = document.getReference().getParent();
                            menuItem.setCategoryPath(categoryRef.getId());

                            DocumentReference canteenRef = categoryRef.getParent();
                            menuItem.setCanteenId(canteenRef.getId());

                            // PERBAIKAN: Dari Dokumen Kantin, perlu .getParent().getParent() untuk mencapai Dokumen Kampus
                            DocumentReference campusRef = canteenRef.getParent().getParent();
                            menuItem.setCampusId(campusRef.getId());

                            recommendationList.add(menuItem);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(Recommendation.this, "Failed to load menu data: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void fetchMenuItemsFromSpecificCampus(String collectionPath, String campusId) {
        db.collection("kampus").document(campusId).collection("kantin").get()
                .addOnSuccessListener(canteensSnapshot -> {
                    if (canteensSnapshot.isEmpty()) {
                        adapter.notifyDataSetChanged(); // Tampilkan daftar kosong jika tidak ada kantin
                        return;
                    }
                    List<Task<QuerySnapshot>> menuTasks = new ArrayList<>();
                    for (QueryDocumentSnapshot canteenDoc : canteensSnapshot) {
                        Task<QuerySnapshot> menuTask = canteenDoc.getReference().collection(collectionPath).get();
                        menuTasks.add(menuTask);
                    }

                    Tasks.whenAllComplete(menuTasks).addOnCompleteListener(allTasks -> {
                        recommendationList.clear();
                        // Loop pada list task awal
                        for (Task<QuerySnapshot> task : menuTasks) {
                            // PERBAIKAN: Menghilangkan `instanceof` yang berlebihan
                            if (task.isSuccessful() && task.getResult() != null) {
                                QuerySnapshot menuSnapshots = task.getResult();
                                for (QueryDocumentSnapshot menuDoc : menuSnapshots) {
                                    MenuItem menuItem = menuDoc.toObject(MenuItem.class);
                                    menuItem.setMenuId(menuDoc.getId());

                                    CollectionReference categoryRef = menuDoc.getReference().getParent();
                                    menuItem.setCategoryPath(categoryRef.getId());

                                    DocumentReference canteenRef = categoryRef.getParent();
                                    menuItem.setCanteenId(canteenRef.getId());
                                    menuItem.setCampusId(campusId);

                                    recommendationList.add(menuItem);
                                }
                            }
                        }

                        recommendationList.sort((o1, o2) -> {
                            if(o1 instanceof MenuItem && o2 instanceof MenuItem){
                                return Double.compare(((MenuItem) o2).getRating(), ((MenuItem) o1).getRating());
                            }
                            return 0;
                        });
                        adapter.notifyDataSetChanged();
                    });
                });
    }
}