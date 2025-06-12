package com.example.veteranrecommendationcanteen.UI;

import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import com.example.veteranrecommendationcanteen.R;
import com.example.veteranrecommendationcanteen.MenuAdapter2;
import com.example.veteranrecommendationcanteen.MenuItem;
import com.example.veteranrecommendationcanteen.DiscountAdapter;
import com.example.veteranrecommendationcanteen.DiscountItem;

public class CanteenDetail extends AppCompatActivity {

    private RecyclerView recyclerView, discountRecyclerView;

    private Chip chipCardView, chipListView;
    private MenuAdapter2 adapter;
    private List<DiscountItem> discountList;
    private List<MenuItem> menuList;
    private ImageButton btnBack;
    private LinearLayout topButtons;
    private Handler hideHandler = new Handler();
    private Runnable hideRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_canteen);

        btnBack = findViewById(R.id.btnBack);
        topButtons = findViewById(R.id.topButtons);

        hideRunnable = () -> {
            fadeView(btnBack, false);
            fadeView(topButtons, false);
        };

        // Set touch listener untuk seluruh layout utama agar bisa mendeteksi sentuhan
        NestedScrollView layoutMain = findViewById(R.id.layoutMain);
        layoutMain.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                showButtons();  // Saat disentuh, munculkan tombol
            }
            return false;
        });

        // Jalankan timer pertama kali
        startHideTimer();

        recyclerView = findViewById(R.id.recyclerView);
        chipCardView = findViewById(R.id.chipCardView);
        chipListView = findViewById(R.id.chipListView);
        discountRecyclerView = findViewById(R.id.discountRecyclerView);

        generateDiscount();

        DiscountAdapter discountAdapter = new DiscountAdapter(this, discountList);
        discountRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        discountRecyclerView.setAdapter(discountAdapter);

        generateMenu();

        adapter = new MenuAdapter2(this, menuList, true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        chipCardView.setOnClickListener(v -> {
            adapter.setGridMode(true);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            recyclerView.setAdapter(adapter);
        });

        chipListView.setOnClickListener(v -> {
            adapter.setGridMode(false);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        });
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
        hideHandler.postDelayed(hideRunnable, 4000); // sembunyikan setelah 4 detik
    }

    private void generateDiscount() {
        discountList = new ArrayList<>();
        discountList.add(new DiscountItem("Food discount 10%", "Discounts for all foods", R.drawable.app_logo));
        discountList.add(new DiscountItem("Drink discount 15%", "Applicable for all drinks", R.drawable.app_logo));
        discountList.add(new DiscountItem("Food discount 10%", "Discounts for all foods", R.drawable.app_logo));
        discountList.add(new DiscountItem("Drink discount 15%", "Applicable for all drinks", R.drawable.app_logo));
        discountList.add(new DiscountItem("Food discount 10%", "Discounts for all foods", R.drawable.app_logo));
        discountList.add(new DiscountItem("Drink discount 15%", "Applicable for all drinks", R.drawable.app_logo));
        // Tambahkan diskon lainnya jika perlu
    }

    private void generateMenu() {
        menuList = new ArrayList<>();
        menuList.add(new MenuItem("Fried Rice Sausages", 15000, R.drawable.app_logo));
        menuList.add(new MenuItem("Grilled Chicken", 27000, R.drawable.app_logo));
        menuList.add(new MenuItem("Chicken Noodles", 13500, R.drawable.app_logo));
        menuList.add(new MenuItem("Meatball and Noodles", 21000, R.drawable.app_logo));
        menuList.add(new MenuItem("Fried Rice Sausages", 15000, R.drawable.app_logo));
        menuList.add(new MenuItem("Grilled Chicken", 27000, R.drawable.app_logo));
        menuList.add(new MenuItem("Chicken Noodles", 13500, R.drawable.app_logo));
        menuList.add(new MenuItem("Meatball and Noodles", 21000, R.drawable.app_logo));
        // Tambahkan menu lainnya sesuai kebutuhan
    }
}
