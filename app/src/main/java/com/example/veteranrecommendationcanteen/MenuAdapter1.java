package com.example.veteranrecommendationcanteen;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.veteranrecommendationcanteen.UI.MenuDetail;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

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

        String docId = item.getMenuId() + "_" + item.getCanteenId();
        DocumentReference favoriteRef = db.collection("users")
                .document(currentUser.getUid())
                .collection("favorites")
                .document(docId);

        holder.btnFavorite.setImageResource(R.drawable.ic_love);

        if (currentUser != null) {
            favoriteRef.get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    holder.btnFavorite.setImageResource(R.drawable.ic_loved);
                    holder.btnFavorite.setTag(true);
                } else {
                    holder.btnFavorite.setImageResource(R.drawable.ic_love);
                    holder.btnFavorite.setTag(false);
                }
            });
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MenuDetail.class);
            intent.putExtra("CAMPUS_ID", item.getCampusId());
            intent.putExtra("CANTEEN_ID", item.getCanteenId());
            intent.putExtra("CATEGORY_PATH", item.getCategoryPath());
            intent.putExtra("MENU_ID", item.getMenuId());
            context.startActivity(intent);
        });

        holder.btnFavorite.setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(context, "Please log in to manage favorites.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isFavorited = holder.btnFavorite.getTag() != null && (boolean) holder.btnFavorite.getTag();

            if (isFavorited) {
                favoriteRef.delete().addOnSuccessListener(unused -> {
                    Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    holder.btnFavorite.setImageResource(R.drawable.ic_love);
                    holder.btnFavorite.setTag(false);
                    if (favoriteRemovedListener != null) {
                        favoriteRemovedListener.onFavoriteRemoved(item);
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to remove favorite", Toast.LENGTH_SHORT).show();
                });
            } else {
                Map<String, Object> favoriteData = new HashMap<>();
                favoriteData.put("menuId", item.getMenuId());
                favoriteData.put("campusId", item.getCampusId());
                favoriteData.put("canteenId", item.getCanteenId());
                favoriteData.put("categoryPath", item.getCategoryPath());
                favoriteData.put("addedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

                favoriteRef.set(favoriteData).addOnSuccessListener(unused -> {
                    Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show();
                    holder.btnFavorite.setImageResource(R.drawable.ic_loved);
                    holder.btnFavorite.setTag(true);
                }).addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to add favorite", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        TextView name, canteenName, leastInfo;
        ImageView image, btnFavorite;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            canteenName = itemView.findViewById(R.id.canteenName);
            leastInfo = itemView.findViewById(R.id.leastInfo);
            image = itemView.findViewById(R.id.image);
            btnFavorite = itemView.findViewById(R.id.favoriteIcon);
        }
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private OnFavoriteRemovedListener favoriteRemovedListener;

    public interface OnFavoriteRemovedListener {
        void onFavoriteRemoved(MenuItem removedItem);
    }

    public void setOnFavoriteRemovedListener(OnFavoriteRemovedListener listener) {
        this.favoriteRemovedListener = listener;
    }
}