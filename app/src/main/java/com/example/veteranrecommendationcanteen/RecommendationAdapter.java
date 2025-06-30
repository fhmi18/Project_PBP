package com.example.veteranrecommendationcanteen;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.veteranrecommendationcanteen.UI.CanteenDetail;
import com.example.veteranrecommendationcanteen.UI.MenuDetail;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder> {
    private List<Object> itemList;
    private Context context;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


    public RecommendationAdapter(Context context, List<Object> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public RecommendationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recommendation, parent, false);
        return new RecommendationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendationViewHolder holder, int position) {
        Object item = itemList.get(position);

        holder.btnLike.setImageResource(R.drawable.ic_like);
        holder.btnLike.setEnabled(false);
        holder.ratingBadge.setVisibility(View.GONE);
        holder.txtLikeCount.setText("0");
        holder.subtitle.setText("Memuat...");

        if (item instanceof CanteenItem) {
            CanteenItem canteen = (CanteenItem) item;
            holder.title.setText(canteen.getNama_kantin());
            holder.subtitle.setText(canteen.getLokasi_spesifik());
            holder.description.setText("Rating: " + canteen.getRating_keseluruhan() + " | " + canteen.getJam_operasional());
            holder.txtLikeCount.setText(formatLikeCount(canteen.getJumlah_like()));
            Glide.with(context).load("https://placehold.co/100x100/E94B64/FFFFFF?text=Kantin").into(holder.image);

            DocumentReference itemRef = db.collection("kampus").document(canteen.getCampusId())
                    .collection("kantin").document(canteen.getCanteenId());

            setupLike(holder, itemRef, "liked_canteens", canteen.getCanteenId());

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, CanteenDetail.class);
                intent.putExtra("CAMPUS_ID", canteen.getCampusId());
                intent.putExtra("CANTEEN_ID", canteen.getCanteenId());
                context.startActivity(intent);
            });

        } else if (item instanceof MenuItem) {
            MenuItem menu = (MenuItem) item;
            holder.title.setText(menu.getName());
            holder.subtitle.setText(String.format(Locale.US, "Rp%,d", menu.getPrice()));
            holder.description.setText(menu.getDescription());
            holder.ratingBadge.setVisibility(View.VISIBLE);
            holder.ratingBadge.setText(String.format(Locale.US, "★ %.1f", menu.getRating()));
            holder.txtLikeCount.setText(formatLikeCount(menu.getTotalLike()));

            String imageUrl = (menu.getGambarUrl() != null && !menu.getGambarUrl().isEmpty())
                    ? menu.getGambarUrl().get(0)
                    : null;
            Glide.with(context).load(imageUrl).placeholder(R.drawable.app_logo).into(holder.image);

            DocumentReference itemRef = db.collection("kampus").document(menu.getCampusId())
                    .collection("kantin").document(menu.getCanteenId())
                    .collection(menu.getCategoryPath()).document(menu.getMenuId());

            String compositeId = menu.getMenuId() + "_" + menu.getCanteenId();
            setupLike(holder, itemRef, "liked_menus", compositeId);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, MenuDetail.class);
                intent.putExtra("CAMPUS_ID", menu.getCampusId());
                intent.putExtra("CANTEEN_ID", menu.getCanteenId());
                intent.putExtra("CATEGORY_PATH", menu.getCategoryPath());
                intent.putExtra("MENU_ID", menu.getMenuId());
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    private void setupLike(RecommendationViewHolder holder, DocumentReference itemRef, String collection, String likeDocId) {
        if (currentUser == null) {
            holder.btnLike.setVisibility(View.GONE);
            return;
        }

        AtomicBoolean isLiked = new AtomicBoolean(false);
        DocumentReference userLikeRef = db.collection("users").document(currentUser.getUid())
                .collection(collection).document(likeDocId);

        holder.btnLike.setTag(likeDocId);
        userLikeRef.get().addOnSuccessListener(doc -> {
            if (!likeDocId.equals(holder.btnLike.getTag())) return;

            isLiked.set(doc.exists());
            holder.btnLike.setImageResource(isLiked.get() ? R.drawable.ic_liked : R.drawable.ic_like);
            holder.btnLike.setEnabled(true);

            itemRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    Long likeCount = snapshot.getLong("jumlah_like");
                    if (likeCount != null && likeDocId.equals(holder.btnLike.getTag())) {
                        holder.txtLikeCount.setText(formatLikeCount(likeCount.intValue()));
                    }
                }
            });
        });

        holder.btnLike.setOnClickListener(v -> {
            holder.btnLike.setEnabled(false);
            db.runTransaction((Transaction.Function<Void>) transaction -> {
                DocumentSnapshot itemSnapshot = transaction.get(itemRef);
                long currentLikes = itemSnapshot.getLong("jumlah_like") != null ? itemSnapshot.getLong("jumlah_like") : 0;

                if (isLiked.get()) {
                    transaction.update(itemRef, "jumlah_like", Math.max(0, currentLikes - 1));
                    transaction.delete(userLikeRef);
                } else {
                    transaction.update(itemRef, "jumlah_like", currentLikes + 1);
                    transaction.set(userLikeRef, new HashMap<>());
                }
                return null;
            }).addOnSuccessListener(aVoid -> {
                isLiked.set(!isLiked.get());
                holder.btnLike.setImageResource(isLiked.get() ? R.drawable.ic_liked : R.drawable.ic_like);
                holder.btnLike.setEnabled(true);
                itemRef.get().addOnSuccessListener(snapshot -> {
                    Long updatedLikes = snapshot.getLong("jumlah_like");
                    if (updatedLikes != null) {
                        holder.txtLikeCount.setText(formatLikeCount(updatedLikes.intValue()));
                    }
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Gagal update like", Toast.LENGTH_SHORT).show();
                holder.btnLike.setEnabled(true);
            });
        });
    }

    private String formatLikeCount(int count) {
        if (count >= 1000) return String.format(Locale.US, "%.1fK", count / 1000.0);
        return String.valueOf(count);
    }

    public static class RecommendationViewHolder extends RecyclerView.ViewHolder {
        TextView title, subtitle, description, txtLikeCount, ratingBadge;
        ImageView image;
        ImageButton btnLike;

        public RecommendationViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txtMenuName);
            subtitle = itemView.findViewById(R.id.txtCanteen);
            description = itemView.findViewById(R.id.txtInfo);
            image = itemView.findViewById(R.id.imgMenu);
            txtLikeCount = itemView.findViewById(R.id.txtLikeCount);
            ratingBadge = itemView.findViewById(R.id.ratingBadge);
            btnLike = itemView.findViewById(R.id.btnLike);
        }
    }
}