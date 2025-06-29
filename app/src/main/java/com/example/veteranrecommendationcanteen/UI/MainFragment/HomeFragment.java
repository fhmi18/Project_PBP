package com.example.veteranrecommendationcanteen.UI.MainFragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.veteranrecommendationcanteen.R;
import com.example.veteranrecommendationcanteen.UI.Recommendation;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvLocation;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LocationPrefs";
    private static final String KEY_CAMPUS_ID = "selectedCampusId";
    private static final String KEY_CAMPUS_NAME = "selectedCampusName";

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    private static class PromoData {
        String title, subtitle; int gradientResId;
        PromoData(String title, String subtitle, int gradientResId) { this.title = title; this.subtitle = subtitle; this.gradientResId = gradientResId; }
    }
    private static class RecommendationData {
        String title, subtitle; int colorId;
        RecommendationData(String title, String subtitle, int colorId) { this.title = title; this.subtitle = subtitle; this.colorId = colorId; }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        setupLocationPicker(view);

        setupPromoCards(view);
        setupRecommendationCards(view);
    }

    private void setupLocationPicker(View view) {
        tvLocation = view.findViewById(R.id.tvLocation);
        updateLocationDisplay();

        tvLocation.setOnClickListener(v -> {
            String[] locations = {"Semua", "Kampus Pondok Labu", "Kampus Limo"};

            new AlertDialog.Builder(requireContext())
                    .setTitle("Pilih Lokasi Kampus")
                    .setItems(locations, (dialog, which) -> {
                        String selectedCampusName = locations[which];
                        String selectedCampusId;

                        switch (which) {
                            case 1:
                                selectedCampusId = "kampus_upn_pondok_labu";
                                break;
                            case 2:
                                selectedCampusId = "kampus_upn_limo";
                                break;
                            default: // Case 0
                                selectedCampusId = "ALL"; // Penanda untuk "Semua"
                                break;
                        }

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(KEY_CAMPUS_ID, selectedCampusId);
                        editor.putString(KEY_CAMPUS_NAME, selectedCampusName);
                        editor.apply();

                        updateLocationDisplay();
                    })
                    .show();
        });
    }

    private void updateLocationDisplay() {
        String currentCampusName = sharedPreferences.getString(KEY_CAMPUS_NAME, "Semua");
        tvLocation.setText(currentCampusName);
    }

    private void setupRecommendationCards(View view) {
        GridLayout recommendationContainer = view.findViewById(R.id.recommendationContainer);
        recommendationContainer.removeAllViews();

        List<RecommendationData> recommendationList = new ArrayList<>();
        recommendationList.add(new RecommendationData("Canteen", "51 Canteens Already", R.drawable.gradient_red_orange));
        recommendationList.add(new RecommendationData("Food", "347 Foods Already", R.drawable.gradient_green_blue));
        recommendationList.add(new RecommendationData("Dessert & Snack", "476 Items Already", R.drawable.gradient_blue_purple));
        recommendationList.add(new RecommendationData("Beverage", "339 Items Already", R.drawable.gradient_cyan_blue));

        for (RecommendationData rec : recommendationList) {
            View card = getLayoutInflater().inflate(R.layout.item_recommendation_card, recommendationContainer, false);

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

            card.setOnClickListener(v -> {
                String selectedCampusId = sharedPreferences.getString(KEY_CAMPUS_ID, "ALL");

                Intent intent = new Intent(requireContext(), Recommendation.class);
                intent.putExtra("RECOMMENDATION_TYPE", rec.title);
                intent.putExtra("SELECTED_CAMPUS_ID", selectedCampusId);
                startActivity(intent);
            });

            recommendationContainer.addView(card);
        }
    }

    private void setupPromoCards(View view) {
        LinearLayout promoContainer = view.findViewById(R.id.promoContainer);
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
}