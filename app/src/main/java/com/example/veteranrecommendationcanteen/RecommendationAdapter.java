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
import com.example.veteranrecommendationcanteen.UI.CanteenDetail;

import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder> {
    private List<Object> itemList;
    private Context context;

    public RecommendationAdapter(Context context, List<Object> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public RecommendationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_menu, parent, false);
        return new RecommendationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendationViewHolder holder, int position) {
        Object item = itemList.get(position);

        if (item instanceof CanteenItem) {
            CanteenItem canteen = (CanteenItem) item;
            holder.title.setText(canteen.getNama_kantin());
            holder.subtitle.setText(canteen.getLokasi_spesifik());
            holder.description.setText("Rating: " + canteen.getRating_keseluruhan() + " | " + canteen.getJam_operasional());
            Glide.with(context)
                    .load("https://placehold.co/100x100/E94B64/FFFFFF?text=Kantin")
                    .into(holder.image);

        } else if (item instanceof MenuItem) {
            MenuItem menu = (MenuItem) item;
            holder.title.setText(menu.getName());
            holder.subtitle.setText(String.format("Rp%,d", menu.getPrice()));
            holder.description.setText(menu.getDescription());
            Glide.with(context)
                    .load(menu.getFotoUrl())
                    .placeholder(R.drawable.app_logo)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.image);
        }

        // Tambahkan OnClickListener ke item view
        holder.itemView.setOnClickListener(v -> {
            if (item instanceof CanteenItem) {
                CanteenItem canteen = (CanteenItem) item;
                if (canteen.getCampusId() != null && canteen.getCanteenId() != null) {
                    Intent intent = new Intent(context, CanteenDetail.class);
                    intent.putExtra("CAMPUS_ID", canteen.getCampusId());
                    intent.putExtra("CANTEEN_ID", canteen.getCanteenId());
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Canteen ID is missing.", Toast.LENGTH_SHORT).show();
                }
            }
            // Tambahkan logika klik untuk MenuItem jika perlu
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class RecommendationViewHolder extends RecyclerView.ViewHolder {
        TextView title, subtitle, description;
        ImageView image;

        public RecommendationViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.name);
            subtitle = itemView.findViewById(R.id.canteenName);
            description = itemView.findViewById(R.id.leastInfo);
            image = itemView.findViewById(R.id.image);
            itemView.findViewById(R.id.favoriteIcon).setVisibility(View.GONE);
        }
    }
}
