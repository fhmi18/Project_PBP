package com.example.veteranrecommendationcanteen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DiscountAdapter extends RecyclerView.Adapter<DiscountAdapter.ViewHolder> {

    private final Context context;
    private final List<DiscountItem> discountList;

    public DiscountAdapter(Context context, List<DiscountItem> discountList) {
        this.context = context;
        this.discountList = discountList;
    }

    @NonNull
    @Override
    public DiscountAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_discount_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DiscountAdapter.ViewHolder holder, int position) {
        DiscountItem item = discountList.get(position);
        holder.title.setText(item.title);
        holder.description.setText(item.description);
        holder.icon.setImageResource(item.iconResId);
    }

    @Override
    public int getItemCount() {
        return discountList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, description;

        public ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.discountIcon);
            title = itemView.findViewById(R.id.discountTitle);
            description = itemView.findViewById(R.id.discountDescription);
        }
    }
}
