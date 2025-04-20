package com.example.summer.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.summer.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 设置点击事件
        binding.consultLayout.setOnClickListener(v -> showToast("在线咨询"));
        binding.personalLayout.setOnClickListener(v -> showToast("个人信息"));
        binding.helpLayout.setOnClickListener(v -> showToast("帮助中心"));
        // 添加我的日记点击事件
        binding.diaryLayout.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), DiaryActivity.class);
            startActivity(intent);
        });

        return root;
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}