package com.example.veteranrecommendationcanteen;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.veteranrecommendationcanteen.UI.MenuDetail;

import java.util.List;

public class MenuAdapter2 extends RecyclerView.Adapter<MenuAdapter2.MenuViewHolder> {
    private final Context context;
    private final List<MenuItem> menuItems;
    private boolean isGridMode;

    // Variabel untuk menyimpan path yang diperlukan
    private String campusId;
    private String canteenId;
    private String categoryPath;

    private static final int VIEW_TYPE_GRID = 1;
    private static final int VIEW_TYPE_LIST = 2;

    public MenuAdapter2(Context context, List<MenuItem> menuItems, boolean isGridMode, String campusId, String canteenId) {
        this.context = context;
        this.menuItems = menuItems;
        this.isGridMode = isGridMode;
        this.campusId = campusId;
        this.canteenId = canteenId;
    }

    // Metode untuk mengatur path kategori saat tab berubah
    public void setCategoryPath(String categoryPath) {
        this.categoryPath = categoryPath;
    }

    public void setGridMode(boolean gridMode) {
        isGridMode = gridMode;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return isGridMode ? VIEW_TYPE_GRID : VIEW_TYPE_LIST;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = (viewType == VIEW_TYPE_GRID)
                ? LayoutInflater.from(context).inflate(R.layout.item_menu_grid, parent, false)
                : LayoutInflater.from(context).inflate(R.layout.item_menu_list, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem item = menuItems.get(position);
        if (item == null) return;

        holder.name.setText(item.getName());
        holder.price.setText(String.format("Rp%,d", item.getPrice()));

        Glide.with(context)
                .load(item.getFotoUrl())
                .placeholder(R.drawable.app_logo)
                .error(R.drawable.ic_launcher_background)
                .into(holder.image);

        // Set OnClickListener untuk seluruh item
        holder.button.setOnClickListener(v -> {
            if (item.getMenuId() == null || categoryPath == null) {
                Toast.makeText(context, "Menu data is incomplete.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(context, MenuDetail.class);
            intent.putExtra("CAMPUS_ID", campusId);
            intent.putExtra("CANTEEN_ID", canteenId);
            intent.putExtra("CATEGORY_PATH", categoryPath);
            intent.putExtra("MENU_ID", item.getMenuId());
            context.startActivity(intent);
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
