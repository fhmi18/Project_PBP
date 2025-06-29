package com.example.veteranrecommendationcanteen.UI;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.veteranrecommendationcanteen.R;

public class FullscreenImageActivity extends AppCompatActivity {
    public static final String EXTRA_IMAGE_URL = "image_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        ImageView fullscreenImageView = findViewById(R.id.fullscreenImageView);
        ImageButton closeButton = findViewById(R.id.fullscreenCloseButton);

        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);

        Glide.with(this)
                .load(imageUrl)
                .into(fullscreenImageView);

        closeButton.setOnClickListener(v -> finish());
    }
}