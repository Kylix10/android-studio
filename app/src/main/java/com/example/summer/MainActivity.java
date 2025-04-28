package com.example.summer;
import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.summer.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    // 将 AppBarConfiguration 声明为成员变量
    private AppBarConfiguration appBarConfiguration;
    private int currentFragmentId = R.id.nav_home; // 默认为首页

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // 绑定布局
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        // 获取 NavController（确保 nav_host_fragment_activity_main 的 ID 与布局一致）
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        // 设置 AppBarConfiguration，定义哪些目的地是顶级目的地
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_map, R.id.nav_profile) // 注意：确保导航图中的 ID 与菜单中的 ID 匹配
                .build();

        // 设置 ActionBar 和 NavController 关联（用于返回箭头处理等）
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

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
        // 显式传递 appBarConfiguration 消除二义性
        return NavigationUI.navigateUp(navController, appBarConfiguration);
    }
}



