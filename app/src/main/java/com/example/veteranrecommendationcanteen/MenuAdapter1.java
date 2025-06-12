package com.example.veteranrecommendationcanteen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MenuAdapter1 extends RecyclerView.Adapter<MenuAdapter1.MenuViewHolder> {

    private final List<MenuItem> menuList;

    public MenuAdapter1(List<MenuItem> menuList) {
        this.menuList = menuList;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem item = menuList.get(position);
        holder.name.setText(item.name);
        holder.canteenName.setText(item.canteenName);
        holder.leastInfo.setText(item.leastInfo);
        holder.image.setImageResource(item.imageResId);
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        TextView name, canteenName, leastInfo;
        ImageView image;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            canteenName = itemView.findViewById(R.id.canteenName);
            leastInfo = itemView.findViewById(R.id.leastInfo);
            image = itemView.findViewById(R.id.image);
        }
    }
}