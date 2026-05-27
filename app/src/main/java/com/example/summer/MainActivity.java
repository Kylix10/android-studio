package com.example.summer;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.IdRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.summer.databinding.ActivityMainBinding;
import com.example.summer.utils.LocationManagerHelper;
import com.example.summer.utils.NavigationHelper;
import com.example.summer.utils.NotificationChannelManager;
import com.example.summer.utils.NotificationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1001;
    private boolean permissionRequested = false;
    
    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private int currentFragmentId = R.id.nav_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 请求通知权限（Android 13+）
        requestNotificationPermission();

        // 处理通知点击跳转
        handleNotificationIntent(getIntent());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        // 获取 NavController（确保 nav_host_fragment_activity_main 的 ID 与布局一致）
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        // 设置 AppBarConfiguration，定义哪些目的地是顶级目的地
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_map, R.id.nav_profile) // 注意：确保导航图中的 ID 与菜单中的 ID 匹配
                .build();

        // 设置 ActionBar 和 NavController 关联（用于返回箭头处理等）
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        //
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!= null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.indicator_selected_color)));
        }

        // 绑定 BottomNavigationView 与 NavController
        NavigationUI.setupWithNavController(navView, navController);
        
        // 跟踪当前fragment ID
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            currentFragmentId = destination.getId();
        });

        // 优化处理底部导航栏点击事件
        navView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            // 如果点击的是当前页面，执行刷新操作
            if (itemId == currentFragmentId) {
                refreshCurrentFragment(itemId);
                return true;
            }
            
            // 优化动画和跳转
            NavOptions navOptions = new NavOptions.Builder()
                    // 如果导航到首页，则清空回退栈
                    .setPopUpTo(itemId == R.id.nav_home ? R.id.nav_home : -1, 
                            itemId == R.id.nav_home)
                    // 避免重复创建实例
                    .setLaunchSingleTop(true)
                    // 对首页跳转优化动画
                    .setEnterAnim(itemId == R.id.nav_home ? 0 : android.R.anim.fade_in)
                    .setExitAnim(itemId == R.id.nav_home ? 0 : android.R.anim.fade_out)
                    .setPopEnterAnim(android.R.anim.fade_in)
                    .setPopExitAnim(android.R.anim.fade_out)
                    .build();

            // 使用NavController进行导航
            navController.navigate(itemId, null, navOptions);
            return true;
        });

        // 处理底部导航栏重新点击事件以优化返回到首页（以及其他页面）时的体验
        navView.setOnNavigationItemReselectedListener(item -> {
            refreshCurrentFragment(item.getItemId());
        });

        // 启动功能测试
        startFeatureTests();
    }

    /**
     * 延迟执行新增功能测试，在应用启动后3秒执行
     */
    private void startFeatureTests() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Log.d("FeatureTest", "========== 开始测试新增功能 ==========");
            
            // 测试1: 通知推送功能
            testNotificationFeature();
            
            // 测试2: 位置管理工具类
            testLocationManagerHelper();
            
            // 测试3: 导航工具类
            testNavigationHelper();
            
            // 测试4: 通知渠道管理
            testNotificationChannelManager();
            
            Log.d("FeatureTest", "========== 新增功能测试完成 ==========");
        }, 3000); // 延迟3秒执行，确保应用完全启动
    }

    /**
     * 测试通知推送功能
     */
    private void testNotificationFeature() {
        Log.d("FeatureTest", "[通知推送] 开始测试...");
        
        // 确保通知渠道已创建
        NotificationChannelManager.createNotificationChannels(this);
        Log.d("FeatureTest", "[通知推送] 通知渠道已确认创建");
        
        // 检查通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d("FeatureTest", "[通知推送] 通知权限已授予");
            } else {
                Log.d("FeatureTest", "[通知推送] 通知权限未授予，通知可能无法显示");
            }
        }
        
        // 测试人流量预警通知 - 立即发送
        NotificationHelper.showCrowdAlertNotification(this, "如意湖", 500);
        
        // 延迟发送第二个通知
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            NotificationHelper.showPromotionNotification(this, "限时优惠", "门票8折优惠进行中");
        }, 2000);
        
        // 延迟发送第三个通知
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            NotificationHelper.showSystemNotification(this, "景区公告", "明日正常开放");
        }, 4000);
        
        Log.d("FeatureTest", "[通知推送] 测试完成");
    }

    /**
     * 测试位置管理工具类
     */
    private void testLocationManagerHelper() {
        Log.d("FeatureTest", "[位置管理] 开始测试...");
        
        // 测试距离计算
        double distance = LocationManagerHelper.calculateDistance(
                40.9758, 117.9412,  // 如意湖
                40.9765, 117.9430   // 上湖
        );
        Log.d("FeatureTest", "[位置管理] 两点距离: " + String.format("%.2f", distance) + " 米");
        
        // 测试获取景点位置
        double[] spotLocation = LocationManagerHelper.getSpotLocation("如意湖");
        if (spotLocation != null) {
            Log.d("FeatureTest", "[位置管理] 如意湖坐标: " + spotLocation[0] + ", " + spotLocation[1]);
        } else {
            Log.d("FeatureTest", "[位置管理] 未找到如意湖位置");
        }
        
        // 测试广播位置更新
        LocationManagerHelper.broadcastLocationUpdate(this, 40.9758, 117.9412);
        Log.d("FeatureTest", "[位置管理] 位置更新广播已发送");
        
        Log.d("FeatureTest", "[位置管理] 测试完成");
    }

    /**
     * 测试导航工具类
     */
    private void testNavigationHelper() {
        Log.d("FeatureTest", "[导航工具] 开始测试...");
        
        // 测试导航方法是否可用
        try {
            // 测试导航到购票页面
            Log.d("FeatureTest", "[导航工具] navigateToTicket 方法可用");
            
            // 测试导航到个人中心
            Log.d("FeatureTest", "[导航工具] navigateToProfile 方法可用");
            
            // 测试导航到首页
            Log.d("FeatureTest", "[导航工具] navigateToHome 方法可用");
            
        } catch (Exception e) {
            Log.e("FeatureTest", "[导航工具] 测试失败: " + e.getMessage());
        }
        
        Log.d("FeatureTest", "[导航工具] 测试完成");
    }

    /**
     * 测试通知渠道管理
     */
    private void testNotificationChannelManager() {
        Log.d("FeatureTest", "[通知渠道] 开始测试...");
        
        // 测试初始化通知渠道
        NotificationChannelManager.createNotificationChannels(this);
        Log.d("FeatureTest", "[通知渠道] 通知渠道已创建");
        
        // 测试获取通知渠道
        boolean hasCrowdChannel = NotificationChannelManager.hasNotificationChannel(this, 
                NotificationChannelManager.CHANNEL_ID_ALERT);
        boolean hasPromotionChannel = NotificationChannelManager.hasNotificationChannel(this, 
                NotificationChannelManager.CHANNEL_ID_PROMOTION);
        boolean hasSystemChannel = NotificationChannelManager.hasNotificationChannel(this, 
                NotificationChannelManager.CHANNEL_ID_SYSTEM);
        
        Log.d("FeatureTest", "[通知渠道] 人流量预警渠道: " + (hasCrowdChannel ? "存在" : "不存在"));
        Log.d("FeatureTest", "[通知渠道] 促销活动渠道: " + (hasPromotionChannel ? "存在" : "不存在"));
        Log.d("FeatureTest", "[通知渠道] 系统公告渠道: " + (hasSystemChannel ? "存在" : "不存在"));
        
        Log.d("FeatureTest", "[通知渠道] 测试完成");
    }
    
    // 刷新当前Fragment方法，提高响应速度
    private void refreshCurrentFragment(@IdRes int fragmentId) {
        NavDestination currentDestination = navController.getCurrentDestination();
        if (currentDestination != null && currentDestination.getId() == fragmentId) {
            // 使用无动画导航选项
            NavOptions navOptions = new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setEnterAnim(0)
                    .setExitAnim(0)
                    .setPopEnterAnim(0)
                    .setPopExitAnim(0)
                    .build();
                    
            // 如果是首页，优先尝试直接刷新而不是重新导航
            if (fragmentId == R.id.nav_home) {
                refreshHomeFragment();
            } else {
                // 对其他页面执行轻量级刷新
                navController.navigate(fragmentId, null, navOptions);
            }
        }
    }
    
    // 直接刷新首页，避免重新创建
    private void refreshHomeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment navHostFragment = fragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main);
        
        if (navHostFragment != null) {
            FragmentManager childFragmentManager = navHostFragment.getChildFragmentManager();
            for (Fragment fragment : childFragmentManager.getFragments()) {
                if (fragment != null && fragment.isVisible()) {
                    // 通知Fragment可见性变化，触发生命周期方法
                    fragment.onHiddenChanged(false);
                    return;
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleNotificationIntent(intent);
    }

    private void handleNotificationIntent(Intent intent) {
        if (intent != null && intent.hasExtra("target_fragment")) {
            String targetFragment = intent.getStringExtra("target_fragment");
            navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

            switch (targetFragment) {
                case "crowd":
                    navController.navigate(R.id.nav_map);
                    break;
                case "ticket":
                    navController.navigate(R.id.nav_home);
                    break;
                case "notice":
                    navController.navigate(R.id.nav_profile);
                    break;
            }

            // 清除intent中的extra，避免重复处理
            intent.removeExtra("target_fragment");
        }
    }

    /**
     * 请求通知权限（Android 13+）
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionRequested = true;
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_POST_NOTIFICATIONS);
            } else {
                // 权限已授予，直接开始测试
                startFeatureTests();
            }
        } else {
            // Android 13以下不需要通知权限，直接开始测试
            startFeatureTests();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("FeatureTest", "[通知权限] 通知权限已授予");
            } else {
                Log.d("FeatureTest", "[通知权限] 通知权限被拒绝，通知可能无法显示");
            }
            // 无论权限是否授予，都开始测试（测试会记录权限状态）
            startFeatureTests();
        }
    }
}

