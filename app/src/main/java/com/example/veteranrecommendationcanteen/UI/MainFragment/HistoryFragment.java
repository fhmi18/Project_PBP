package com.example.veteranrecommendationcanteen.UI.MainFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.veteranrecommendationcanteen.MenuAdapter1;
import com.example.veteranrecommendationcanteen.MenuItem;
import com.example.veteranrecommendationcanteen.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private MenuAdapter1 adapter;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // --- State untuk filter & sort ---
    private List<MenuItem> masterHistoryList = new ArrayList<>();
    private String selectedCampusId = "Semua";
    private Query.Direction sortDirection = Query.Direction.DESCENDING;
    private Set<String> activeCategoryFilters = new HashSet<>();
    private Map<Integer, Button> categoryButtons = new HashMap<>();

    public HistoryFragment() {
        super(R.layout.fragment_history);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MenuAdapter1(getContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

        setupCategoryFilterButtons(view);
        setupAdvancedFilterButton(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentUser == null) {
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
        }

        if (currentUser != null) {
            fetchHistory();
        } else {
            Toast.makeText(getContext(), "Please log in to see your history.", Toast.LENGTH_SHORT).show();
            masterHistoryList.clear();
            adapter.updateData(new ArrayList<>());
        }
    }

    private void fetchHistory() {
        Query historyQuery = db.collection("users").document(currentUser.getUid())
                .collection("history")
                .orderBy("visitedAt", sortDirection);

        historyQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                masterHistoryList.clear();
                applyFilters();
                return;
            }

            List<Task<DocumentSnapshot>> menuTasks = new ArrayList<>();
            Map<String, DocumentSnapshot> historyDocs = new HashMap<>();

            for (QueryDocumentSnapshot historyDoc : queryDocumentSnapshots) {
                String menuId = historyDoc.getString("menuId");
                String campusId = historyDoc.getString("campusId");
                String canteenId = historyDoc.getString("canteenId");
                String categoryPath = historyDoc.getString("categoryPath");

                if (!"Semua".equals(selectedCampusId) && !selectedCampusId.equals(campusId)) {
                    continue;
                }

                if (menuId == null) continue;

                DocumentReference menuRef = db.collection("kampus").document(campusId)
                        .collection("kantin").document(canteenId)
                        .collection(categoryPath).document(menuId);

                menuTasks.add(menuRef.get());
                historyDocs.put(menuId, historyDoc);
            }

            Tasks.whenAllSuccess(menuTasks).addOnSuccessListener(results -> {
                masterHistoryList.clear();
                for (Object result : results) {
                    DocumentSnapshot menuDoc = (DocumentSnapshot) result;
                    if (menuDoc.exists()) {
                        MenuItem menuItem = menuDoc.toObject(MenuItem.class);
                        if (menuItem != null) {
                            DocumentSnapshot historyDoc = historyDocs.get(menuDoc.getId());
                            menuItem.setMenuId(menuDoc.getId());
                            menuItem.setCampusId(historyDoc.getString("campusId"));
                            menuItem.setCanteenId(historyDoc.getString("canteenId"));
                            menuItem.setCategoryPath(historyDoc.getString("categoryPath"));

                            db.collection("kampus").document(menuItem.getCampusId()).collection("kantin").document(menuItem.getCanteenId()).get()
                                    .addOnSuccessListener(canteenDoc -> {
                                        if(canteenDoc.exists()){
                                            menuItem.canteenName = canteenDoc.getString("nama_kantin");
                                            adapter.notifyDataSetChanged();
                                        }
                                    });

                            masterHistoryList.add(menuItem);
                        }
                    }
                }
                applyFilters();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to load history.", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupCategoryFilterButtons(View view) {
        categoryButtons.put(R.id.filterFood, view.findViewById(R.id.filterFood));
        categoryButtons.put(R.id.filterBeverage, view.findViewById(R.id.filterBeverage));
        categoryButtons.put(R.id.filterDesertSnacks, view.findViewById(R.id.filterDesertSnacks));
        categoryButtons.put(R.id.filterOthers, view.findViewById(R.id.filterOthers));

        View.OnClickListener listener = v -> {
            com.google.android.material.button.MaterialButton btn = (com.google.android.material.button.MaterialButton) v;
            String category = getCategoryFromButtonId(btn.getId());

            btn.setSelected(!btn.isSelected());
            if (btn.isSelected()) {
                activeCategoryFilters.add(category);
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.Primary));
                btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            } else {
                activeCategoryFilters.remove(category);
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent));
                btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.Primary));
                btn.setStrokeColorResource(R.color.Primary);
            }
            applyFilters();
        };

        for (Button btn : categoryButtons.values()) {
            btn.setOnClickListener(listener);
        }
    }

    private void setupAdvancedFilterButton(View view) {
        Button btnFilter = view.findViewById(R.id.filter);
        btnFilter.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.item_dialog_filter, null);
            builder.setView(dialogView);

            RadioGroup rgCampus = dialogView.findViewById(R.id.rg_campus);
            RadioGroup rgSort = dialogView.findViewById(R.id.rg_sort_order);

            if ("Semua".equals(selectedCampusId)) rgCampus.check(R.id.rb_campus_all);
            else if ("kampus_upn_pondok_labu".equals(selectedCampusId)) rgCampus.check(R.id.rb_campus_pl);
            else rgCampus.check(R.id.rb_campus_limo);

            if (sortDirection == Query.Direction.DESCENDING) rgSort.check(R.id.rb_sort_desc);
            else rgSort.check(R.id.rb_sort_asc);

            builder.setPositiveButton("Apply", (dialog, id) -> {
                int selectedCampusRbId = rgCampus.getCheckedRadioButtonId();
                if (selectedCampusRbId == R.id.rb_campus_pl) selectedCampusId = "kampus_upn_pondok_labu";
                else if (selectedCampusRbId == R.id.rb_campus_limo) selectedCampusId = "kampus_upn_limo";
                else selectedCampusId = "Semua";

                sortDirection = (rgSort.getCheckedRadioButtonId() == R.id.rb_sort_asc) ? Query.Direction.ASCENDING : Query.Direction.DESCENDING;

                fetchHistory();
            });
            builder.setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

            builder.create().show();
        });
    }

    private void applyFilters() {
        if (activeCategoryFilters.isEmpty()) {
            adapter.updateData(masterHistoryList);
            return;
        }

        List<MenuItem> filteredList = masterHistoryList.stream()
                .filter(menuItem -> activeCategoryFilters.contains(menuItem.getCategoryPath()))
                .collect(Collectors.toList());

        adapter.updateData(filteredList);
    }

    private String getCategoryFromButtonId(int id) {
        if (id == R.id.filterFood) return "food";
        if (id == R.id.filterBeverage) return "beverage";
        if (id == R.id.filterDesertSnacks) return "dessert_snacks";
        if (id == R.id.filterOthers) return "others";
        return "";
    }
}