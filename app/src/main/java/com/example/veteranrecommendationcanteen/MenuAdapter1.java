package com.example.veteranrecommendationcanteen;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.veteranrecommendationcanteen.UI.MenuDetail;

import java.util.List;
import java.util.Locale;

public class MenuAdapter1 extends RecyclerView.Adapter<MenuAdapter1.MenuViewHolder> {

    private List<MenuItem> menuList;
    private Context context;

    public MenuAdapter1(Context context, List<MenuItem> menuList) {
        this.context = context;
        this.menuList = menuList;
    }

    public void updateData(List<MenuItem> newMenuList) {
        this.menuList.clear();
        this.menuList.addAll(newMenuList);
        notifyDataSetChanged();
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

        holder.name.setText(item.getName());
        holder.canteenName.setText(item.canteenName);
        holder.leastInfo.setText(String.format(Locale.US, "Rp%,d", item.getPrice()));

        if (item.getGambarUrl() != null && !item.getGambarUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getGambarUrl().get(0))
                    .placeholder(R.drawable.app_logo)
                    .error(R.drawable.app_logo)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.app_logo);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MenuDetail.class);
            intent.putExtra("CAMPUS_ID", item.getCampusId());
            intent.putExtra("CANTEEN_ID", item.getCanteenId());
            intent.putExtra("CATEGORY_PATH", item.getCategoryPath());
            intent.putExtra("MENU_ID", item.getMenuId());
            context.startActivity(intent);
        });
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