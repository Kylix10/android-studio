package com.example.summer.ui.home;

import static com.example.summer.ui.home.CarouselAdapter.LOOP_MULTIPLIER;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.recyclerview.widget.RecyclerView;
import com.example.summer.R;
import com.example.summer.TicketPurchaseActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private Handler handler;
    private Runnable runnable;
    private int currentPosition;
    private boolean isUserScrolling = false;
    private CarouselAdapter carouselAdapter;
    private View rootView;
    private boolean isViewInitialized = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 使用视图缓存避免重复创建，提高首页跳转性能
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_home, container, false);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 只有在首次创建视图时才初始化UI组件
        if (!isViewInitialized) {
            initializeComponents();
            setupButtonListeners();
            isViewInitialized = true;
        } else {
            // 页面从缓存恢复时恢复自动滚动
            if (carouselAdapter != null) {
                carouselAdapter.enablePreloading();
                resumeAutoScroll();
            }
        }
    }
    
    private void setupButtonListeners() {
        // 设置猜猜人数按钮点击事件
        Button btnCrowdStats = rootView.findViewById(R.id.btn_crowd_stats);
        if (btnCrowdStats != null) {
            btnCrowdStats.setOnClickListener(v -> {
                try {
                    // 显示Toast提示
                    Toast.makeText(getActivity(), "正在加载人流量统计...", Toast.LENGTH_SHORT).show();
                    
                    // 创建Intent跳转到人流量统计页面
                    Intent intent = new Intent(getActivity(), com.example.summer.CrowdStatsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "打开人流量统计失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });
        }
        
        // 设置景点推荐按钮点击事件
        Button btnRecommend = rootView.findViewById(R.id.btn_recommend);
        if (btnRecommend != null) {
            btnRecommend.setOnClickListener(v -> {
                try {
                    // 显示Toast提示
                    Toast.makeText(getActivity(), "正在为您生成个性化推荐...", Toast.LENGTH_SHORT).show();
                    
                    // 使用NavController导航到景点推荐页面
                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                    navController.navigate(R.id.action_home_to_spotRecommendation);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "打开景点推荐失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });
        }
        
        // 设置游园须知按钮点击事件
        Button btnGardenGuide = rootView.findViewById(R.id.btn_youxuan);
        btnGardenGuide.setOnClickListener(v -> {
            try {
                // 使用NavController导航到游园须知页面
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                navController.navigate(R.id.action_home_to_gardenGuide);
            } catch (Exception e) {
                e.printStackTrace();
                // 可以添加错误处理逻辑，比如显示Toast提示用户
            }
        });
        
        // 设置景区概览按钮点击事件
        Button btnParkOverview = rootView.findViewById(R.id.btn_jingquguilan);
        btnParkOverview.setOnClickListener(v -> {
            try {
                // 使用NavController导航到景区概览页面
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                navController.navigate(R.id.action_home_to_parkOverview);
            } catch (Exception e) {
                e.printStackTrace();
                // 可以添加错误处理逻辑，比如显示Toast提示用户
            }
        });
        
        // 设置游客应知按钮点击事件
        Button btnTouristInfo = rootView.findViewById(R.id.btn_youkeyingzhi);
        btnTouristInfo.setOnClickListener(v -> {
            try {
                // 使用NavController导航到游客应知页面
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                navController.navigate(R.id.action_home_to_touristInfo);
            } catch (Exception e) {
                e.printStackTrace();
                // 可以添加错误处理逻辑，比如显示Toast提示用户
            }
        });
        
        // 设置购票入口按钮点击事件
        Button btnTicket = rootView.findViewById(R.id.btn_ticket);
        if (btnTicket != null) {
            btnTicket.setOnClickListener(v -> {
                try {
                    // 启动购票页面前先显示Toast提示
                    Toast.makeText(getActivity(), "正在打开购票页面...", Toast.LENGTH_SHORT).show();
                    
                    // 创建Intent
                    Intent intent = new Intent(getActivity(), TicketPurchaseActivity.class);
                    
                    // 添加标志，防止创建多个实例
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    
                    // 启动Activity
                    startActivity(intent);
                } catch (Exception e) {
                    // 显示错误信息
                    Toast.makeText(getActivity(), "打开购票页面失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });
        }
    }
    
    private void initializeComponents() {
        // 初始化 ViewPager2 和适配器
        viewPager = rootView.findViewById(R.id.viewPager);

        // 提高滑动性能配置
        optimizeViewPager();

        carouselAdapter = new CarouselAdapter(getImages());
        viewPager.setAdapter(carouselAdapter);

        // 绑定 TabLayout 作为分页指示器
        tabLayout = rootView.findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // 计算真实位置，显示正确的指示器
            int realPosition = position % carouselAdapter.getRealCount();
            tab.setText(String.valueOf(realPosition + 1));
        }).attach();

        // 计算初始位置，使 ViewPager2 可无限循环展示图片
        int initialPosition = (LOOP_MULTIPLIER / 2) * carouselAdapter.getRealCount();
        viewPager.setCurrentItem(initialPosition, false);
        currentPosition = initialPosition;

        // 设置页面切换监听
        setupPageChangeCallback();

        // 开始自动轮播
        setupAutoScroll();
    }

    private void optimizeViewPager() {
        // 预加载相邻页面，提高滑动流畅性
        viewPager.setOffscreenPageLimit(3);

        // 减少过度绘制
        try {
            RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
            recyclerView.setItemViewCacheSize(4);
            recyclerView.setHasFixedSize(true);

            // 禁用过度滚动效果以提高流畅度
            recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
            
            // 硬件加速
            recyclerView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 添加页面转换动画，使过渡更平滑
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer((page, position) -> {
            float absPos = Math.abs(position);
            page.setAlpha(1.0f - (absPos * 0.15f));
            page.setScaleX(0.85f + (1.0f - absPos) * 0.15f);
            page.setScaleY(0.85f + (1.0f - absPos) * 0.15f);
        });
        viewPager.setPageTransformer(transformer);
    }

    private void setupPageChangeCallback() {
        ViewPager2.OnPageChangeCallback callback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                // 检测用户是否正在滑动
                isUserScrolling = state == ViewPager2.SCROLL_STATE_DRAGGING;

                // 当滑动停止且不是由用户引起的，恢复自动滑动
                if (state == ViewPager2.SCROLL_STATE_IDLE && !isUserScrolling) {
                    resumeAutoScroll();
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPosition = position;
            }
        };
        
        viewPager.registerOnPageChangeCallback(callback);
        // 保存回调引用以便正确卸载
        viewPager.getChildAt(0).setTag(callback);
    }

    // 获取图片资源列表
    private List<Integer> getImages() {
        return Arrays.asList(
                R.drawable.banner1,
                R.drawable.banner2,
                R.drawable.banner3,
                R.drawable.banner4
        );
    }

    // 自动轮播逻辑，每隔3秒切换一次图片
    private void setupAutoScroll() {
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                if (getView() == null) return;

                // 用户正在滑动时不中断
                if (!isUserScrolling) {
                    // 使用动画滑动到下一页
                    viewPager.setCurrentItem(++currentPosition, true);
                }

                // 延迟执行下一次滑动
                handler.postDelayed(this, 3000);
            }
        };
        handler.postDelayed(runnable, 3000);
    }

    private void resumeAutoScroll() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, 3000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        if (carouselAdapter != null) {
            carouselAdapter.disablePreloading();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (handler != null && runnable != null) {
            handler.postDelayed(runnable, 3000);
        }
        if (carouselAdapter != null) {
            carouselAdapter.enablePreloading();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (viewPager != null) {
            viewPager.unregisterOnPageChangeCallback(viewPager.getChildAt(0).getTag() != null
                    ? (ViewPager2.OnPageChangeCallback) viewPager.getChildAt(0).getTag()
                    : null);
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        // 不清除adapter，保持引用以便重用
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // 只有在Fragment真正销毁时才清理资源
        if (viewPager != null) {
            viewPager.setAdapter(null);
        }
        rootView = null;
        isViewInitialized = false;
    }
}
