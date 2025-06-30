package com.example.veteranrecommendationcanteen.UI;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.veteranrecommendationcanteen.CanteenItem;
import com.example.veteranrecommendationcanteen.MenuItem;
import com.example.veteranrecommendationcanteen.PopularAdapter;
import com.example.veteranrecommendationcanteen.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Popular extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private PopularAdapter adapter;
    private List<Object> popularList;
    private String selectedCampusId;
    private TextView tvLocationInfo;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular);

        db = FirebaseFirestore.getInstance();
        selectedCampusId = getIntent().getStringExtra("SELECTED_CAMPUS_ID");

        setupToolbar();
        initializeViews();
        setupTabs();

        fetchCanteens();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void initializeViews() {
        tvLocationInfo = findViewById(R.id.tvLocationInfo);
        recyclerView = findViewById(R.id.recyclerView);
        tabLayout = findViewById(R.id.tabLayout);

        popularList = new ArrayList<>();
        adapter = new PopularAdapter(this, popularList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        if ("ALL".equals(selectedCampusId)) {
            tvLocationInfo.setText("Semua Kampus");
        } else if ("kampus_upn_pondok_labu".equals(selectedCampusId)) {
            tvLocationInfo.setText("Kampus Pondok Labu");
        } else {
            tvLocationInfo.setText("Kampus Limo");
        }
    }

    private void setupTabs() {
        String[] categories = {"Canteen", "Main Courses", "Appetizer", "Drinks", "Others"};
        for (String category : categories) {
            tabLayout.addTab(tabLayout.newTab().setText(category));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: fetchCanteens(); break;
                    case 1: fetchMenuItems("food"); break;
                    case 2: fetchMenuItems("dessert_snacks"); break;
                    case 3: fetchMenuItems("beverage"); break;
                    case 4: fetchMenuItems("others"); break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void fetchCanteens() {
        Query query;
        if ("ALL".equals(selectedCampusId)) {
            query = db.collectionGroup("kantin").orderBy("jumlah_like", Query.Direction.DESCENDING);
        } else {
            query = db.collection("kampus").document(selectedCampusId)
                    .collection("kantin").orderBy("jumlah_like", Query.Direction.DESCENDING);
        }

        query.limit(20).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                popularList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    CanteenItem canteen = document.toObject(CanteenItem.class);
                    canteen.setCanteenId(document.getId());
                    if ("ALL".equals(selectedCampusId)) {
                        CollectionReference canteenParentRef = document.getReference().getParent();
                        if (canteenParentRef != null && canteenParentRef.getParent() != null) {
                            canteen.setCampusId(canteenParentRef.getParent().getId());
                        }
                    } else {
                        canteen.setCampusId(selectedCampusId);
                    }
                    popularList.add(canteen);
                }
                adapter.notifyDataSetChanged();
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error.";
                Toast.makeText(this, "Failed to load canteens: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchMenuItems(String collectionPath) {
        Query query;
        if ("ALL".equals(selectedCampusId)) {
            query = db.collectionGroup(collectionPath).orderBy("jumlah_like", Query.Direction.DESCENDING);
        } else {
            query = db.collectionGroup(collectionPath).orderBy("jumlah_like", Query.Direction.DESCENDING);
        }

        query.limit(20).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                popularList.clear();
                List<MenuItem> tempMenuList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    MenuItem menuItem = document.toObject(MenuItem.class);

                    DocumentReference canteenRef = document.getReference().getParent().getParent();
                    if (canteenRef != null && canteenRef.getParent() != null && canteenRef.getParent().getParent() != null) {
                        String campusId = canteenRef.getParent().getParent().getId();

                        if ("ALL".equals(selectedCampusId) || selectedCampusId.equals(campusId)) {
                            menuItem.setMenuId(document.getId());
                            menuItem.setCategoryPath(collectionPath);
                            menuItem.setCanteenId(canteenRef.getId());
                            menuItem.setCampusId(campusId);
                            tempMenuList.add(menuItem);
                        }
                    }
                }
                popularList.addAll(tempMenuList);
                adapter.notifyDataSetChanged();
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error.";
                Toast.makeText(this, "Failed to load menus: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}