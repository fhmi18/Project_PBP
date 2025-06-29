package com.example.veteranrecommendationcanteen.UI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.veteranrecommendationcanteen.DiscountAdapter;
import com.example.veteranrecommendationcanteen.DiscountItem;
import com.example.veteranrecommendationcanteen.MenuAdapter2;
import com.example.veteranrecommendationcanteen.MenuItem;
import com.example.veteranrecommendationcanteen.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CanteenDetail extends AppCompatActivity {

    private RecyclerView recyclerView, discountRecyclerView;
    private Spinner viewModeSpinner;
    private TabLayout tabLayout;
    private MenuAdapter2 adapter;
    private List<DiscountItem> discountList;
    private DiscountAdapter discountAdapter;
    private List<MenuItem> menuList;
    private ImageButton btnBack, btnLike;
    private LinearLayout topButtons;

    private Handler hideHandler = new Handler(Looper.getMainLooper());
    private Runnable hideRunnable;

    private FirebaseFirestore db;
    private String campusId;
    private String canteenId;
    private String currentCategoryPath = "food";
    private boolean isLiked = false;

    private TextView canteenNameTextView, campusLocationTextView, addressTextView, availabilityInfoTextView, ownerTextView;
    private ImageView headerImageView;
    private TextView totalLikeTextView, tvRatingValue, tvMenuCount, tvPriceRange, tvOpeningHours, tvRatingLabel, seeOnMapTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_canteen);

        campusId = getIntent().getStringExtra("CAMPUS_ID");
        canteenId = getIntent().getStringExtra("CANTEEN_ID");

        if (campusId == null || canteenId == null) {
            Toast.makeText(this, "Error: Canteen data not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupAutoHideButtons();
        setupDiscountRecyclerView();
        setupMenuRecyclerView();
        setupSpinner();
        setupTabs();
        setupLikeButton();

        fetchCanteenDetailsAndStats();
        fetchMenuFromFirestore(currentCategoryPath);
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnLike = findViewById(R.id.btnLike);
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

        totalLikeTextView = findViewById(R.id.totalLike);
        tvRatingValue = findViewById(R.id.tvRatingValue);
        tvMenuCount = findViewById(R.id.tvMenuCount);
        tvPriceRange = findViewById(R.id.tvPriceRange);
        tvOpeningHours = findViewById(R.id.tvOpeningHours);
        tvRatingLabel = findViewById(R.id.tvRatingLabel);
        seeOnMapTextView = findViewById(R.id.seeOnMap);

        btnBack.setOnClickListener(v -> finish());
    }

    private void fetchCanteenDetailsAndStats() {
        DocumentReference canteenDocRef = db.collection("kampus").document(campusId)
                .collection("kantin").document(canteenId);

        canteenDocRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) { return; }
            if (snapshot != null && snapshot.exists()) {
                String canteenName = snapshot.getString("nama_kantin");
                String specificLocation = snapshot.getString("lokasi_spesifik");
                String operationalHours = snapshot.getString("jam_operasional");
                String owner = snapshot.getString("pemilik");
                Double rating = snapshot.getDouble("rating_keseluruhan");
                Long likeCount = snapshot.getLong("jumlah_like");
                String mapUrl = snapshot.getString("lokasi_url");

                List<String> promoIds = new ArrayList<>();
                Object rawPromoIdObject = snapshot.get("promo_id");
                if (rawPromoIdObject instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> rawList = (List<Object>) rawPromoIdObject;
                    for (Object obj : rawList) {
                        if (obj instanceof String) {
                            promoIds.add((String) obj);
                        }
                    }
                }

                if (!promoIds.isEmpty()) {
                    fetchAndDisplayPromos(promoIds);
                }

                ownerTextView.setText("Owner: " + owner);
                totalLikeTextView.setText(String.format(Locale.US, "%,d orang menyukai kantin ini.", likeCount != null ? likeCount : 0));
                tvRatingValue.setText(rating != null ? String.format(Locale.US, "%.1f", rating) : "N/A");
                tvOpeningHours.setText(operationalHours);

                setupMapButton(mapUrl);
                checkInitialLikeStatus();

                Glide.with(this).load("https://placehold.co/600x400/CCCCCC/FFFFFF?text=" + canteenName).centerCrop().into(headerImageView);

                DocumentReference campusDocRef = db.collection("kampus").document(campusId);
                campusDocRef.get().addOnSuccessListener(campusDocument -> {
                    if (campusDocument != null && campusDocument.exists()) {
                        String campusAddress = campusDocument.getString("lokasi_kampus");
                        String campusName = campusDocument.getString("nama_kampus");
                        addressTextView.setText(campusAddress);
                        canteenNameTextView.setText(canteenName);
                        campusLocationTextView.setText(specificLocation + " • " + campusName);
                    }
                });

                fetchAndComputeMenuStats(canteenDocRef);
            } else {
                Toast.makeText(this, "Canteen details not found.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAndComputeMenuStats(DocumentReference canteenDocRef) {
        Task<QuerySnapshot> foodTask = canteenDocRef.collection("food").get();
        Task<QuerySnapshot> beverageTask = canteenDocRef.collection("beverage").get();
        Task<QuerySnapshot> snacksTask = canteenDocRef.collection("dessert_snacks").get();
        Task<QuerySnapshot> othersTask = canteenDocRef.collection("others").get();

        Tasks.whenAllSuccess(foodTask, beverageTask, snacksTask, othersTask).addOnSuccessListener(results -> {
            int totalMenuCount = 0;
            int minPrice = Integer.MAX_VALUE;
            int maxPrice = 0;
            int totalServingTime = 0;
            int servingTimeCount = 0;
            List<Task<QuerySnapshot>> reviewCountTasks = new ArrayList<>();

            for (Object result : results) {
                if (result instanceof QuerySnapshot) {
                    QuerySnapshot snapshot = (QuerySnapshot) result;
                    totalMenuCount += snapshot.size();
                    for (QueryDocumentSnapshot document : snapshot) {
                        Number priceNum = document.getLong("harga");
                        if (priceNum != null) {
                            int price = priceNum.intValue();
                            if (price > 0 && price < minPrice) minPrice = price;
                            if (price > maxPrice) maxPrice = price;
                        }
                        Number servingTimeNum = document.getLong("lama_penyajian");
                        if (servingTimeNum != null) {
                            totalServingTime += servingTimeNum.intValue();
                            servingTimeCount++;
                        }
                        reviewCountTasks.add(document.getReference().collection("ulasan").get());
                    }
                }
            }

            tvMenuCount.setText(String.valueOf(totalMenuCount));
            if (minPrice != Integer.MAX_VALUE) {
                String minPriceStr = String.format(Locale.US, "%d", minPrice);
                String maxPriceStr = String.format(Locale.US, "%d", maxPrice);
                tvPriceRange.setText(String.format("Rp%s-\n%s", minPriceStr, maxPriceStr));
            } else {
                tvPriceRange.setText("N/A");
            }
            int avgServingTime = (servingTimeCount > 0) ? (totalServingTime / servingTimeCount) : 0;
            if (minPrice != Integer.MAX_VALUE) {
                availabilityInfoTextView.setText(String.format(Locale.US, "Dimulai dari Rp%,d • Avg. %d min", minPrice, avgServingTime));
            } else {
                availabilityInfoTextView.setText(String.format(Locale.US, "Avg. %d min", avgServingTime));
            }
            Tasks.whenAllSuccess(reviewCountTasks).addOnSuccessListener(reviewSnapshots -> {
                long totalReviews = 0;
                for (Object snapshot : reviewSnapshots) {
                    if (snapshot instanceof QuerySnapshot) {
                        totalReviews += ((QuerySnapshot) snapshot).size();
                    }
                }
                tvRatingLabel.setText(String.format(Locale.US, "%d+ Reviews", totalReviews));
            });
        });
    }

    private void fetchAndDisplayPromos(List<String> promoIds) {
        discountList.clear();
        DocumentReference promosDocRef = db.collection("promo").document("promo");

        promosDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Object> allPromosMap = documentSnapshot.getData();
                if (allPromosMap != null) {
                    for (String promoId : promoIds) {
                        Object promoDataObject = allPromosMap.get(promoId);
                        if (promoDataObject instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> promoData = (Map<String, Object>) promoDataObject;

                            String promoName = (String) promoData.get("nama_promo");
                            String promoType = (String) promoData.get("tipe");
                            String promoMenu = (String) promoData.get("jenis_menu");
                            Number promoValue = (Number) promoData.get("nilai");

                            String description = "Special offer for " + promoMenu + " items!";
                            if ("diskon_persen".equals(promoType) && promoValue != null) {
                                description = "Get " + promoValue.longValue() + "% off for " + promoMenu + " items.";
                            } else if ("beli_bonus".equals(promoType)){
                                Number beli = (Number) promoData.get("beli");
                                Number gratis = (Number) promoData.get("gratis");
                                if(beli != null && gratis != null){
                                    description = "Buy " + beli.intValue() + " get " + gratis.intValue() + " free for " + promoMenu + " items.";
                                }
                            }
                            discountList.add(new DiscountItem(promoName, description, R.drawable.app_logo));
                        }
                    }
                    discountAdapter.notifyDataSetChanged();
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load promos.", Toast.LENGTH_SHORT).show();
        });
    }

    private void checkInitialLikeStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            isLiked = false;
            updateLikeButtonUI();
            return;
        }
        DocumentReference likeRef = db.collection("users").document(user.getUid())
                .collection("liked_canteens").document(canteenId);
        likeRef.get().addOnSuccessListener(documentSnapshot -> {
            isLiked = documentSnapshot.exists();
            updateLikeButtonUI();
        });
    }

    private void updateLikeButtonUI() {
        if (isLiked) {
            btnLike.setImageResource(R.drawable.ic_liked);
        } else {
            btnLike.setImageResource(R.drawable.ic_like);
        }
    }

    private void setupLikeButton() {
        btnLike.setOnClickListener(view -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Please log in to like a canteen.", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLike.setEnabled(false);
            DocumentReference canteenRef = db.collection("kampus").document(campusId).collection("kantin").document(canteenId);
            DocumentReference likeRef = db.collection("users").document(user.getUid()).collection("liked_canteens").document(canteenId);

            db.runTransaction((Transaction.Function<Void>) transaction -> {
                DocumentSnapshot snapshot = transaction.get(canteenRef);
                Long currentLikes = snapshot.getLong("jumlah_like");
                long likeCount = currentLikes != null ? currentLikes : 0;

                long newLikes = isLiked ? Math.max(0, likeCount - 1) : likeCount + 1;
                transaction.update(canteenRef, "jumlah_like", newLikes);
                return null;
            }).addOnSuccessListener(aVoid -> {
                if (isLiked) {
                    likeRef.delete();
                } else {
                    Map<String, Object> likeData = new HashMap<>();
                    likeData.put("campusId", campusId);
                    likeRef.set(likeData);
                }

                isLiked = !isLiked;
                updateLikeButtonUI();
                btnLike.setEnabled(true);
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to update like status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                btnLike.setEnabled(true);
            });
        });
    }

    private void setupMapButton(String url) {
        if (url == null || url.isEmpty() || !url.startsWith("http")) {
            seeOnMapTextView.setVisibility(View.GONE);
            return;
        }
        seeOnMapTextView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
    }

    private void fetchMenuFromFirestore(String collectionPath) {
        adapter.setCategoryPath(collectionPath);
        DocumentReference kantinDocRef = db.collection("kampus").document(campusId).collection("kantin").document(canteenId);
        kantinDocRef.collection(collectionPath).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                menuList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    MenuItem menuItem = document.toObject(MenuItem.class);
                    menuItem.setMenuId(document.getId());
                    menuList.add(menuItem);
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(CanteenDetail.this, "Failed to load menu data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMenuRecyclerView() {
        menuList = new ArrayList<>();
        adapter = new MenuAdapter2(this, menuList, true, campusId, canteenId);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private void setupTabs() {
        String[] categories = {"Main Courses", "Appetizer", "Drinks", "Others"};
        for (String category : categories) {
            tabLayout.addTab(tabLayout.newTab().setText(category));
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String selectedCategory = tab.getText().toString();
                switch (selectedCategory) {
                    case "Main Courses": currentCategoryPath = "food"; break;
                    case "Appetizer": currentCategoryPath = "dessert_snacks"; break;
                    case "Drinks": currentCategoryPath = "beverage"; break;
                    default: currentCategoryPath = "others"; break;
                }
                fetchMenuFromFirestore(currentCategoryPath);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.view_modes, android.R.layout.simple_spinner_item);
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
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupDiscountRecyclerView() {
        discountList = new ArrayList<>();
        discountAdapter = new DiscountAdapter(this, discountList);
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