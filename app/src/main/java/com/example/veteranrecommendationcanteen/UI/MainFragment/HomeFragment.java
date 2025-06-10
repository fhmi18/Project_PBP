package com.example.veteranrecommendationcanteen.UI.MainFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.veteranrecommendationcanteen.R;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    private static class PromoData {
        String title;
        String subtitle;
        int gradientResId;

        PromoData(String title, String subtitle, int gradientResId) {
            this.title = title;
            this.subtitle = subtitle;
            this.gradientResId = gradientResId;
        }
    }

    private static class RecommendationData {
        String title;
        String subtitle;
        int colorId;

        RecommendationData(String title, String subtitle, int colorId) {
            this.title = title;
            this.subtitle = subtitle;
            this.colorId = colorId;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("userEmail", "User");

        LinearLayout promoContainer = view.findViewById(R.id.promoContainer);

        List<PromoData> promoList = new ArrayList<>();
        promoList.add(new PromoData("Today’s Best Geprek", "Off up to 75%", R.drawable.gradient_red_orange));
        promoList.add(new PromoData("Weekly Best Foods", "Only this week", R.drawable.gradient_blue_purple));
        promoList.add(new PromoData("Student Promo", "Up to 50%", R.drawable.gradient_green_blue));
        // Tambah promo lain jika perlu

        for (PromoData promo : promoList) {
            View card = getLayoutInflater().inflate(R.layout.item_promo_card, promoContainer, false);

            TextView title = card.findViewById(R.id.promoTitle);
            TextView subtitle = card.findViewById(R.id.promoSubtitle);
            View overlay = card.findViewById(R.id.promoOverlay);  // Tambahkan ID ini di layout (penjelasan di bawah)

            title.setText(promo.title);
            subtitle.setText(promo.subtitle);
            overlay.setBackgroundResource(promo.gradientResId);

            promoContainer.addView(card);
        }

        GridLayout recommendationContainer = view.findViewById(R.id.recommendationContainer);

        List<RecommendationData> recommendationList = new ArrayList<>();
        recommendationList.add(new RecommendationData("Canteen", "51 Canteens Already", R.drawable.gradient_red_orange));
        recommendationList.add(new RecommendationData("Food", "347 Foods Already", R.drawable.gradient_green_blue));
        recommendationList.add(new RecommendationData("Dessert & Snack", "476 Dessert and Snacks Already", R.drawable.gradient_blue_purple));
        recommendationList.add(new RecommendationData("Beverage", "339 Beverages Already", R.drawable.gradient_cyan_blue));

        for (RecommendationData rec : recommendationList) {
            View card = getLayoutInflater().inflate(R.layout.item_recommendation_card, recommendationContainer, false);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // 1 kolom, bobot 1
            params.setMargins(16, 16, 16, 16); // jarak antar item
            card.setLayoutParams(params);

            TextView title = card.findViewById(R.id.recommendationTitle);
            TextView subtitle = card.findViewById(R.id.recommendationSubtitle);
            View background = card.findViewById(R.id.recommendationOverlay);  // Tambahkan ID ini di layout

            title.setText(rec.title);
            subtitle.setText(rec.subtitle);
            background.setBackgroundResource(rec.colorId);

            recommendationContainer.addView(card);
        }

    }
}