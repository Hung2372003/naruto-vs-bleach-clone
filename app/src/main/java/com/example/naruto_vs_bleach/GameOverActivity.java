package com.example.naruto_vs_bleach;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class GameOverActivity extends AppCompatActivity {

    public static final String EXTRA_RESULT = "result"; // "win" | "lose"

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        ImageView title = findViewById(R.id.imgTitle);
        ImageButton btnReplay = findViewById(R.id.btnReplay);
        ImageButton btnSettings = findViewById(R.id.btnSettings);

        String result = getIntent().getStringExtra(EXTRA_RESULT);
        @DrawableRes int resId = "win".equals(result) ? R.drawable.game_over : R.drawable.you_lose;
        title.setImageResource(resId);

        btnReplay.setOnClickListener(v -> {
            // Mở lại GameActivity mới tinh
            Intent i = new Intent(this, GameActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });

        btnSettings.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, SettingsActivity.class));
            } catch (Exception ignored) {
                // nếu chưa có SettingsActivity thì chỉ đóng màn
                finish();
            }
        });
    }
}
