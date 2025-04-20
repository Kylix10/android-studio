package com.example.summer.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.summer.R;

public class TouristInfoFragment extends Fragment {
    private View rootView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 加载游客应知布局
        rootView = inflater.inflate(R.layout.fragment_tourist_info, container, false);
        return rootView;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 初始化视图元素
        initializeViews();
    }
    
    private void initializeViews() {
        // 设置标题
        TextView titleTextView = rootView.findViewById(R.id.tvTitle);
        
        // 添加简单的进入动画效果，使页面更生动
        rootView.setAlpha(0f);
        rootView.animate()
                .alpha(1f)
                .setDuration(500)
                .start();
    }
} 