package com.example.veteranrecommendationcanteen;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.veteranrecommendationcanteen.UI.CanteenDetail;
import com.example.veteranrecommendationcanteen.UI.MenuDetail;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.PopularViewHolder> {
    private final List<Object> itemList;
    private final Context context;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    public PopularAdapter(Context context, List<Object> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public PopularViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recommendation, parent, false);
        return new PopularViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PopularViewHolder holder, int position) {
        Object item = itemList.get(position);

        if (item instanceof CanteenItem) {
            bindCanteenView(holder, (CanteenItem) item);
        } else if (item instanceof MenuItem) {
            bindMenuView(holder, (MenuItem) item);
        }
    }

    private void bindCanteenView(@NonNull PopularViewHolder holder, CanteenItem canteen) {
        holder.txtMenuName.setText(canteen.getNama_kantin());
        holder.txtCanteen.setText(canteen.getLokasi_spesifik() != null ? canteen.getLokasi_spesifik() : "Memuat lokasi...");
        holder.txtInfo.setText(String.format(Locale.US, "Rating: " + canteen.getRating_keseluruhan() + " | " + canteen.getJam_operasional()));
        holder.txtLikeCount.setText(formatLikeCount(canteen.getJumlah_like()));
        holder.ratingBadge.setVisibility(View.GONE);
        holder.btnLike.setImageResource(R.drawable.ic_like);
        holder.btnLike.setEnabled(false);

        Glide.with(context)
                .load("https://placehold.co/100x100/E94B64/FFFFFF?text=Kantin")
                .into(holder.imgMenu);

        DocumentReference itemRef = db.collection("kampus")
                .document(canteen.getCampusId())
                .collection("kantin")
                .document(canteen.getCanteenId());

        String likeDocId = canteen.getCanteenId();
        setupLikeListener(holder.btnLike, holder.txtLikeCount, itemRef, "liked_canteens", likeDocId);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CanteenDetail.class);
            intent.putExtra("CAMPUS_ID", canteen.getCampusId());
            intent.putExtra("CANTEEN_ID", canteen.getCanteenId());
            context.startActivity(intent);
        });
    }

    private void bindMenuView(@NonNull PopularViewHolder holder, MenuItem menu) {
        holder.txtMenuName.setText(menu.getName());
        holder.txtInfo.setText(String.format(Locale.US, "Start from Rp%,d", menu.getPrice()));
        holder.ratingBadge.setText(String.format(Locale.US, "★ %.1f", menu.getRating()));
        holder.ratingBadge.setVisibility(View.VISIBLE);

        holder.txtCanteen.setText("Memuat...");
        holder.btnLike.setImageResource(R.drawable.ic_like);
        holder.btnLike.setEnabled(false);
        holder.txtLikeCount.setText("0");

        String imageUrl = (menu.getGambarUrl() != null && !menu.getGambarUrl().isEmpty())
                ? menu.getGambarUrl().get(0) : null;
        Glide.with(context).load(imageUrl)
                .placeholder(R.drawable.app_logo)
                .error(R.drawable.ic_launcher_background)
                .into(holder.imgMenu);

        db.collection("kampus")
                .document(menu.getCampusId())
                .collection("kantin")
                .document(menu.getCanteenId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        holder.txtCanteen.setText(doc.getString("nama_kantin"));
                    }
                });

        DocumentReference itemRef = db.collection("kampus").document(menu.getCampusId())
                .collection("kantin").document(menu.getCanteenId())
                .collection(menu.getCategoryPath()).document(menu.getMenuId());

        String compositeLikeId = menu.getMenuId() + "_" + menu.getCanteenId();
        setupLikeListener(holder.btnLike, holder.txtLikeCount, itemRef, "liked_menus", compositeLikeId);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MenuDetail.class);
            intent.putExtra("CAMPUS_ID", menu.getCampusId());
            intent.putExtra("CANTEEN_ID", menu.getCanteenId());
            intent.putExtra("CATEGORY_PATH", menu.getCategoryPath());
            intent.putExtra("MENU_ID", menu.getMenuId());
            context.startActivity(intent);
        });
    }

    private void setupLikeListener(ImageButton btnLike, TextView tvLikeCount, DocumentReference itemRef, String likeCollection, String likeDocId) {
        if (currentUser == null) {
            btnLike.setVisibility(View.GONE);
            return;
        }

        final AtomicBoolean isLiked = new AtomicBoolean(false);
        DocumentReference userLikeRef = db.collection("users")
                .document(currentUser.getUid())
                .collection(likeCollection)
                .document(likeDocId);

        btnLike.setTag(likeDocId);

        userLikeRef.get().addOnSuccessListener(doc -> {
            if (!likeDocId.equals(btnLike.getTag())) return;

            isLiked.set(doc.exists());
            updateLikeButtonUI(btnLike, isLiked.get());
            btnLike.setEnabled(true);

            itemRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists() && snapshot.getLong("jumlah_like") != null && likeDocId.equals(btnLike.getTag())) {
                    tvLikeCount.setText(formatLikeCount(snapshot.getLong("jumlah_like").intValue()));
                }
            });
        });

        btnLike.setOnClickListener(v -> {
            btnLike.setEnabled(false);
            handleLikeTransaction(btnLike, tvLikeCount, itemRef, userLikeRef, isLiked);
        });
    }

    private void handleLikeTransaction(ImageButton btnLike, TextView tvLikeCount, DocumentReference itemRef, DocumentReference userLikeRef, AtomicBoolean isLiked) {
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
            updateLikeButtonUI(btnLike, isLiked.get());
            itemRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists() && snapshot.getLong("jumlah_like") != null) {
                    tvLikeCount.setText(formatLikeCount(snapshot.getLong("jumlah_like").intValue()));
                }
            });
            btnLike.setEnabled(true);
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show();
            btnLike.setEnabled(true);
        });
    }

    private void updateLikeButtonUI(ImageButton button, boolean liked) {
        button.setImageResource(liked ? R.drawable.ic_liked : R.drawable.ic_like);
    }

    private String formatLikeCount(int count) {
        if (count >= 1000) return String.format(Locale.US, "%.1fK", count / 1000.0);
        return String.valueOf(count);
    }

    @Override
    public int getItemCount() { return itemList.size(); }

    static class PopularViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMenu;
        TextView txtMenuName, txtCanteen, txtInfo, ratingBadge, txtLikeCount;
        ImageButton btnLike;

        public PopularViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMenu = itemView.findViewById(R.id.imgMenu);
            txtMenuName = itemView.findViewById(R.id.txtMenuName);
            txtCanteen = itemView.findViewById(R.id.txtCanteen);
            txtInfo = itemView.findViewById(R.id.txtInfo);
            ratingBadge = itemView.findViewById(R.id.ratingBadge);
            txtLikeCount = itemView.findViewById(R.id.txtLikeCount);
            btnLike = itemView.findViewById(R.id.btnLike);
        }
    }
}