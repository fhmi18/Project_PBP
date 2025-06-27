package com.example.veteranrecommendationcanteen.UI.MainFragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.veteranrecommendationcanteen.R;
import com.example.veteranrecommendationcanteen.UI.CanteenDetail;
import com.example.veteranrecommendationcanteen.UI.Recommendation; // Impor kelas Recommendation

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Konstruktor super yang benar untuk Fragment
        super(R.layout.fragment_home);
    }

    // Kelas inner untuk data promo
    private static class PromoData {
        String title, subtitle;
        int gradientResId;
        PromoData(String title, String subtitle, int gradientResId) {
            this.title = title; this.subtitle = subtitle; this.gradientResId = gradientResId;
        }
    }

    // Kelas inner untuk data kartu rekomendasi
    private static class RecommendationData {
        String title, subtitle;
        int colorId;
        RecommendationData(String title, String subtitle, int colorId) {
            this.title = title; this.subtitle = subtitle; this.colorId = colorId;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout untuk fragment ini
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup untuk Promo Cards
        setupPromoCards(view);

        // Setup untuk Recommendation Cards
        setupRecommendationCards(view);
    }

    /**
     * Mengisi dan menampilkan kartu promo di HorizontalScrollView.
     */
    private void setupPromoCards(View view) {
        LinearLayout promoContainer = view.findViewById(R.id.promoContainer);
        // Hapus view lama untuk mencegah duplikasi
        promoContainer.removeAllViews();

        List<PromoData> promoList = new ArrayList<>();
        promoList.add(new PromoData("Today’s Best Geprek", "Off up to 75%", R.drawable.gradient_red_orange));
        promoList.add(new PromoData("Weekly Best Foods", "Only this week", R.drawable.gradient_blue_purple));
        promoList.add(new PromoData("Student Promo", "Up to 50%", R.drawable.gradient_green_blue));

        for (PromoData promo : promoList) {
            View card = getLayoutInflater().inflate(R.layout.item_promo_card, promoContainer, false);

            TextView title = card.findViewById(R.id.promoTitle);
            TextView subtitle = card.findViewById(R.id.promoSubtitle);
            View overlay = card.findViewById(R.id.promoOverlay);

            title.setText(promo.title);
            subtitle.setText(promo.subtitle);
            overlay.setBackgroundResource(promo.gradientResId);

            promoContainer.addView(card);
        }
    }

    /**
     * Mengisi dan menampilkan kartu rekomendasi di GridLayout.
     */
    private void setupRecommendationCards(View view) {
        GridLayout recommendationContainer = view.findViewById(R.id.recommendationContainer);
        // Hapus view lama untuk mencegah duplikasi
        recommendationContainer.removeAllViews();

        List<RecommendationData> recommendationList = new ArrayList<>();
        recommendationList.add(new RecommendationData("Canteen", "51 Canteens Already", R.drawable.gradient_red_orange));
        recommendationList.add(new RecommendationData("Food", "347 Foods Already", R.drawable.gradient_green_blue));
        recommendationList.add(new RecommendationData("Dessert & Snack", "476 Items Already", R.drawable.gradient_blue_purple));
        recommendationList.add(new RecommendationData("Beverage", "339 Items Already", R.drawable.gradient_cyan_blue));

        for (RecommendationData rec : recommendationList) {
            View card = getLayoutInflater().inflate(R.layout.item_recommendation_card, recommendationContainer, false);

            // Mengatur agar kartu mengisi kolom grid secara merata
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(16, 16, 16, 16);
            card.setLayoutParams(params);

            TextView title = card.findViewById(R.id.recommendationTitle);
            TextView subtitle = card.findViewById(R.id.recommendationSubtitle);
            View background = card.findViewById(R.id.recommendationOverlay);

            title.setText(rec.title);
            subtitle.setText(rec.subtitle);
            background.setBackgroundResource(rec.colorId);

            // Set OnClickListener untuk membuka RecommendationActivity
            card.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), Recommendation.class);
                // Kirim judul kartu sebagai tipe rekomendasi
                intent.putExtra("RECOMMENDATION_TYPE", rec.title);
                startActivity(intent);
            });

            recommendationContainer.addView(card);
        }
    }
}
