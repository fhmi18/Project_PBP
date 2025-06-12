package com.example.veteranrecommendationcanteen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MenuAdapter2 extends RecyclerView.Adapter<MenuAdapter2.MenuViewHolder> {
    private final Context context;
    private final List<MenuItem> menuItems;
    private boolean isGridMode;

    public MenuAdapter2(Context context, List<MenuItem> menuItems, boolean isGridMode) {
        this.context = context;
        this.menuItems = menuItems;
        this.isGridMode = isGridMode;
    }

    public void setGridMode(boolean gridMode) {
        isGridMode = gridMode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
                isGridMode ? R.layout.item_menu_grid : R.layout.item_menu_list,
                parent, false
        );
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem item = menuItems.get(position);
        holder.name.setText(item.name);
        holder.price.setText(String.format("Rp%,d", item.price));
        holder.image.setImageResource(item.imageResId);

        holder.button.setOnClickListener(v -> {
            Toast.makeText(context, "View Review of " + item.name, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, price;
        Button button;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.menuImage);
            name = itemView.findViewById(R.id.menuName);
            price = itemView.findViewById(R.id.menuPrice);
            button = itemView.findViewById(R.id.menuButton);
        }
    }
}
