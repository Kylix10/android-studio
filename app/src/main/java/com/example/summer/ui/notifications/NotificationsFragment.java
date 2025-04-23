package com.example.summer.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.summer.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private View rootView;
    private boolean isViewInitialized = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // 使用视图缓存，避免重复创建视图
        if (rootView == null) {
            binding = FragmentNotificationsBinding.inflate(inflater, container, false);
            rootView = binding.getRoot();
        }
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 只有在首次创建视图时才初始化ViewModel和设置点击事件
        if (!isViewInitialized) {
            initViewModel();
            setupClickListeners();
            isViewInitialized = true;
        }
    }

    private void initViewModel() {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);
        // 这里可以添加ViewModel的数据观察
    }

    private void setupClickListeners() {
        // 设置点击事件
        binding.consultLayout.setOnClickListener(v -> showToast("在线咨询"));
        binding.personalLayout.setOnClickListener(v -> showToast("个人信息"));
        binding.helpLayout.setOnClickListener(v -> showToast("帮助中心"));

        // 添加我的日记点击事件
        binding.diaryLayout.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), DiaryActivity.class);
            startActivity(intent);
        });

//        return root;

    }

    private void showToast(String message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 不重置binding，保持视图缓存
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 彻底销毁时释放资源
        binding = null;
        rootView = null;
        isViewInitialized = false;
    }
}