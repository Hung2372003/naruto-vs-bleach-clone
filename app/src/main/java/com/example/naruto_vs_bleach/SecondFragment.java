package com.example.naruto_vs_bleach;  // giữ đúng package của dự án

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public class SecondFragment extends Fragment {

    private Toolbar toolbar;

    public SecondFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Nạp layout của màn Start
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy 2 nút từ layout (đảm bảo fragment_second.xml có đúng id)
        ImageButton btnPlay = view.findViewById(R.id.btnPlay);
        ImageButton btnSettings = view.findViewById(R.id.btnSettings);

        if (btnPlay != null) {
            btnPlay.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), GameActivity.class)));
        }
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), SettingsActivity.class)));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tự xoay ngang khi vào màn này
        requireActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        );

        // Ẩn thanh tiêu đề (nếu activity có Toolbar id = toolbar)
        toolbar = requireActivity().findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setVisibility(View.GONE);
        setHasOptionsMenu(false);
    }

}
