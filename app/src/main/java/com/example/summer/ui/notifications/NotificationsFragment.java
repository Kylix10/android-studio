package com.example.summer.ui.notifications;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.summer.R;
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

    private void updateUsername() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_info", getContext().MODE_PRIVATE);
        String name = prefs.getString("name", "张三"); // 默认为“张三”
        binding.usernameText.setText(name);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 只有在首次创建视图时才初始化ViewModel和设置点击事件
        if (!isViewInitialized) {
            initViewModel();
            updateUsername();
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
        binding.consultLayout.setOnClickListener(v -> {
            // 加载自定义布局
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.activity_personal_phone, null);
            // 获取控件
            View confirmButton = dialogView.findViewById(R.id.confirm_service_phone_button);

            // 创建 Dialog
            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create();

            // 设置点击事件
            confirmButton.setOnClickListener(btn -> dialog.dismiss());
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 关键：透明背景

            dialog.show();
        });//在线咨询

        binding.personalLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PersonalInfoActivity.class);
            startActivity(intent);
        });

        binding.helpLayout.setOnClickListener(v -> {
            // 加载自定义布局
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.activity_personal_help, null);
            // 获取控件
            View confirmButton = dialogView.findViewById(R.id.confirm_service_help_button);

            // 创建 Dialog
            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create();

            // 设置点击事件
            confirmButton.setOnClickListener(btn -> dialog.dismiss());
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 关键：透明背景

            dialog.show();
        });//帮助中心

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

    @Override
    public void onResume() {
        super.onResume();
        updateUsername(); // 返回后实时刷新用户名
    }
}

