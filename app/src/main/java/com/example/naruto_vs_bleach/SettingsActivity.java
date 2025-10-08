package com.example.naruto_vs_bleach;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup rgDifficulty, rgHealth, rgTime;
    private SeekBar seekSfx, seekMusic;
    private TextView tvSfxValue, tvMusicValue;
    private Button btnDone;

    private SettingsStore store;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        store = new SettingsStore(this);

        rgDifficulty = findViewById(R.id.rgDifficulty);
        rgHealth     = findViewById(R.id.rgHealth);
        rgTime       = findViewById(R.id.rgTime);

        seekSfx      = findViewById(R.id.seekSfx);
        seekMusic    = findViewById(R.id.seekMusic);
        tvSfxValue   = findViewById(R.id.tvSfxValue);
        tvMusicValue = findViewById(R.id.tvMusicValue);
        btnDone      = findViewById(R.id.btnDone);

        // --- Load giá trị đã lưu ---
        switch (store.getDifficulty()) {
            case SettingsStore.DIFF_EASY:   rgDifficulty.check(R.id.rbEasy); break;
            case SettingsStore.DIFF_HARD:   rgDifficulty.check(R.id.rbHard); break;
            default:                        rgDifficulty.check(R.id.rbNormal);
        }
        rgHealth.check(store.getHpMultiplier() == 2 ? R.id.rbHp200 : R.id.rbHp100);

        int time = store.getTimeLimitSeconds();
        if (time == 60) rgTime.check(R.id.rb60s);
        else if (time == 120) rgTime.check(R.id.rb120s);
        else rgTime.check(R.id.rbInfinite);

        seekSfx.setProgress(Math.round(store.getSfxVolume() * 100));
        seekMusic.setProgress(Math.round(store.getMusicVolume() * 100));
        tvSfxValue.setText(seekSfx.getProgress() + "%");
        tvMusicValue.setText(seekMusic.getProgress() + "%");

        // Hiển thị % khi kéo
        seekSfx.setOnSeekBarChangeListener(simpleSeek((p) -> {
            tvSfxValue.setText(p + "%");
        }));
        seekMusic.setOnSeekBarChangeListener(simpleSeek((p) -> {
            tvMusicValue.setText(p + "%");
        }));

        // Nút Xong: lưu tất cả & quay về
        btnDone.setOnClickListener(v -> {
            saveAll();
            Toast.makeText(this, "Đã lưu cài đặt", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish(); // quay lại SecondFragment (activity trước đó)
        });
    }

    private void saveAll() {
        // Difficulty
        int diffId = rgDifficulty.getCheckedRadioButtonId();
        if (diffId == R.id.rbEasy) store.setDifficulty(SettingsStore.DIFF_EASY);
        else if (diffId == R.id.rbHard) store.setDifficulty(SettingsStore.DIFF_HARD);
        else store.setDifficulty(SettingsStore.DIFF_NORMAL);

        // Health
        int hpId = rgHealth.getCheckedRadioButtonId();
        store.setHpMultiplier(hpId == R.id.rbHp200 ? 2 : 1);

        // Time
        int timeId = rgTime.getCheckedRadioButtonId();
        if (timeId == R.id.rb60s) store.setTimeLimitSeconds(60);
        else if (timeId == R.id.rb120s) store.setTimeLimitSeconds(120);
        else store.setTimeLimitSeconds(SettingsStore.TIME_INFINITY);

        // Volumes
        store.setSfxVolume(seekSfx.getProgress() / 100f);
        store.setMusicVolume(seekMusic.getProgress() / 100f);
    }

    // Helper nhỏ cho SeekBar
    private SeekBar.OnSeekBarChangeListener simpleSeek(ProgressListener l) {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                l.onChanged(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        };
    }
    private interface ProgressListener { void onChanged(int progress); }
}
