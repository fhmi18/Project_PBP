package com.example.veteranrecommendationcanteen.UI;

import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.veteranrecommendationcanteen.CanteenItem;
import com.example.veteranrecommendationcanteen.DiscountAdapter;
import com.example.veteranrecommendationcanteen.DiscountItem;
import com.example.veteranrecommendationcanteen.MenuAdapter2;
import com.example.veteranrecommendationcanteen.MenuItem;
import com.example.veteranrecommendationcanteen.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CanteenDetail extends AppCompatActivity {

    private RecyclerView recyclerView, discountRecyclerView;
    private Spinner viewModeSpinner;
    private TabLayout tabLayout;
    private MenuAdapter2 adapter;
    private List<DiscountItem> discountList;
    private List<MenuItem> menuList;
    private ImageButton btnBack;
    private LinearLayout topButtons;
    private Handler hideHandler = new Handler();
    private Runnable hideRunnable;

    private FirebaseFirestore db;
    private String campusId;
    private String canteenId;
    private String currentCategoryPath = "food"; // Menyimpan path kategori saat ini

    // Views untuk Detail Kantin
    private TextView canteenNameTextView, campusLocationTextView, addressTextView, availabilityInfoTextView, ownerTextView;
    private ImageView headerImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_canteen);

        // Ambil ID dari Intent yang dikirim oleh RecommendationAdapter
        campusId = getIntent().getStringExtra("CAMPUS_ID");
        canteenId = getIntent().getStringExtra("CANTEEN_ID");

        // Validasi: pastikan ID tidak null sebelum melanjutkan
        if (campusId == null || canteenId == null) {
            Toast.makeText(this, "Error: Canteen data not found.", Toast.LENGTH_SHORT).show();
            finish(); // Keluar dari activity jika ID tidak ada
            return;
        }

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupAutoHideButtons();
        setupDiscountRecyclerView();
        setupMenuRecyclerView();
        setupSpinner();
        setupTabs();

        // Ambil data detail kantin dan menu pertama kali
        fetchCanteenDetails();
        fetchMenuFromFirestore(currentCategoryPath);
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        topButtons = findViewById(R.id.topButtons);
        recyclerView = findViewById(R.id.recyclerView);
        viewModeSpinner = findViewById(R.id.viewModeSpinner);
        tabLayout = findViewById(R.id.tabLayout);
        discountRecyclerView = findViewById(R.id.discountRecyclerView);

        canteenNameTextView = findViewById(R.id.canteenName);
        campusLocationTextView = findViewById(R.id.campusLocation);
        addressTextView = findViewById(R.id.address);
        availabilityInfoTextView = findViewById(R.id.availabilityInfo);
        ownerTextView = findViewById(R.id.ownerText);
        headerImageView = findViewById(R.id.headerImage);

        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Mengambil data detail dari satu dokumen kantin dan menampilkannya di UI.
     */
    private void fetchCanteenDetails() {
        DocumentReference canteenDocRef = db.collection("kampus").document(campusId)
                .collection("kantin").document(canteenId);

        canteenDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    CanteenItem canteen = document.toObject(CanteenItem.class);
                    if (canteen != null) {
                        canteenNameTextView.setText(canteen.getNama_kantin());
                        campusLocationTextView.setText(canteen.getLokasi_spesifik());
                        availabilityInfoTextView.setText("Rating: " + canteen.getRating_keseluruhan() + " | " + canteen.getJam_operasional());

                        // Anda bisa menambahkan field lain seperti alamat atau pemilik jika ada di database
                        // addressTextView.setText(...);
                        // ownerTextView.setText(...);

                        // Gunakan placeholder untuk gambar header kantin
                        Glide.with(this)
                                .load("https://placehold.co/600x400/CCCCCC/FFFFFF?text=" + canteen.getNama_kantin())
                                .centerCrop()
                                .into(headerImageView);
                    }
                } else {
                    Toast.makeText(this, "Canteen details not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to load canteen details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Mengambil daftar menu dari sub-koleksi berdasarkan path kategori.
     */
    private void fetchMenuFromFirestore(String collectionPath) {
        adapter.setCategoryPath(collectionPath);
        DocumentReference kantinDocRef = db.collection("kampus").document(campusId).collection("kantin").document(canteenId);

        kantinDocRef.collection(collectionPath).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                menuList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    MenuItem menuItem = document.toObject(MenuItem.class);
                    menuItem.setMenuId(document.getId()); // Simpan ID dokumen
                    menuList.add(menuItem);
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(CanteenDetail.this, "Gagal memuat data menu.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMenuRecyclerView() {
        menuList = new ArrayList<>();
        // Inisialisasi adapter dengan ID yang diperlukan untuk Intent
        adapter = new MenuAdapter2(this, menuList, true, campusId, canteenId);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private void setupTabs() {
        String[] categories = {"Popular", "Main Courses", "Appetizer", "Drinks", "Others"};
        for (String category : categories) {
            tabLayout.addTab(tabLayout.newTab().setText(category));
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String selectedCategory = tab.getText().toString();
                switch (selectedCategory) {
                    case "Popular":
                    case "Main Courses":
                        currentCategoryPath = "food";
                        break;
                    case "Appetizer":
                        currentCategoryPath = "dessert_snacks";
                        break;
                    case "Drinks":
                        currentCategoryPath = "beverage";
                        break;
                    default:
                        currentCategoryPath = "others";
                        break;
                }
                fetchMenuFromFirestore(currentCategoryPath);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.view_modes, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewModeSpinner.setAdapter(spinnerAdapter);
        viewModeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                boolean isGrid = position == 0;
                adapter.setGridMode(isGrid);
                if (isGrid) {
                    recyclerView.setLayoutManager(new GridLayoutManager(CanteenDetail.this, 2));
                } else {
                    recyclerView.setLayoutManager(new LinearLayoutManager(CanteenDetail.this));
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupDiscountRecyclerView() {
        discountList = new ArrayList<>();
        discountList.add(new DiscountItem("Food discount 10%", "Discounts for all foods", R.drawable.app_logo));
        discountList.add(new DiscountItem("Drink discount 15%", "Applicable for all drinks", R.drawable.app_logo));
        DiscountAdapter discountAdapter = new DiscountAdapter(this, discountList);
        discountRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        discountRecyclerView.setAdapter(discountAdapter);
    }

    private void setupAutoHideButtons() {
        hideRunnable = () -> {
            fadeView(btnBack, false);
            fadeView(topButtons, false);
        };
        NestedScrollView layoutMain = findViewById(R.id.layoutMain);
        layoutMain.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) showButtons();
            return false;
        });
        startHideTimer();
    }

    private void fadeView(View view, boolean show) {
        AlphaAnimation animation = new AlphaAnimation(show ? 0f : 1f, show ? 1f : 0f);
        animation.setDuration(300);
        animation.setFillAfter(true);
        view.startAnimation(animation);
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showButtons() {
        fadeView(btnBack, true);
        fadeView(topButtons, true);
        startHideTimer();
    }

    private void startHideTimer() {
        hideHandler.removeCallbacks(hideRunnable);
        hideHandler.postDelayed(hideRunnable, 4000);
    }
}
