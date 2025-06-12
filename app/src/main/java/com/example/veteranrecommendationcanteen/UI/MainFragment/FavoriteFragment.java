package com.example.veteranrecommendationcanteen.UI.MainFragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.veteranrecommendationcanteen.MenuAdapter1;
import com.example.veteranrecommendationcanteen.R;
import com.example.veteranrecommendationcanteen.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment {

    public FavoriteFragment() {
        super(R.layout.fragment_favorite);
    }

    private RecyclerView recyclerView;
    private MenuAdapter1 adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<MenuItem> menuList = new ArrayList<>();
        menuList.add(new MenuItem("Chicken Geprek", "Kantin Ponlab 1", "Start from 15rb • Pondok Labu • available in 15 min", R.drawable.app_logo));
        menuList.add(new MenuItem("Chicken Skewers", "Kantin Ponlab 2", "Start from 20rb • Limo • available in 10 min", R.drawable.app_logo));
        menuList.add(new MenuItem("Omelette Fried Rice", "Kantin Ponlab 3", "Start from 14rb • Pondok Labu • available in 12 min", R.drawable.app_logo));
        menuList.add(new MenuItem("Indomie Noodles", "Kantin Ponlab 4", "Start from 6rb • Pondok Labu • available in 8 min", R.drawable.app_logo));

        adapter = new MenuAdapter1(menuList);
        recyclerView.setAdapter(adapter);

        return view;
    }
}