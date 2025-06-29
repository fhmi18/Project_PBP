package com.example.veteranrecommendationcanteen;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
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

public class MyReviewsAdapter extends RecyclerView.Adapter<MyReviewsAdapter.MyReviewViewHolder> {

    private final Context context;
    private final List<MyReviewsItem> reviewList;

    public MyReviewsAdapter(Context context, List<MyReviewsItem> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public MyReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new MyReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyReviewViewHolder holder, int position) {
        MyReviewsItem item = reviewList.get(position);

        String titleText = "<b>" + item.getMenuName() + "</b><br><small>" + item.getCanteenName() + "</small>";
        holder.tvUserName.setText(Html.fromHtml(titleText, Html.FROM_HTML_MODE_COMPACT));

        holder.tvRating.setText(String.format(Locale.US, "Rating Anda: %.1f", item.getRating()));
        holder.tvComment.setText("\"" + item.getKomentar() + "\"");

        Glide.with(context)
                .load(item.getMenuImageUrl())
                .placeholder(R.drawable.app_logo)
                .error(R.drawable.app_logo)
                .into(holder.ivUserProfile);

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
        return reviewList.size();
    }

    static class MyReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserProfile;
        TextView tvUserName, tvRating, tvComment;

        MyReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserProfile = itemView.findViewById(R.id.ivUserProfile);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvComment = itemView.findViewById(R.id.tvComment);
        }
    }
}