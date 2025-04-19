package com.example.summer.ui.home;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.summer.DemoApplication;
import com.example.summer.R;
import com.example.summer.datas.SpotData;
import com.example.summer.utils.SpotRecommender;

import java.util.List;
import java.util.Map;

public class SpotRecommendationFragment extends Fragment {

    private SpotData spotData;
    private View rootView;
    private LinearLayout layoutSimilarSpots;
    private DemoApplication demoApplication;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_spot_recommendation, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 从全局Application获取SpotData
        demoApplication = (DemoApplication) requireActivity().getApplication();
        spotData = demoApplication.getSpotData();
        
        layoutSimilarSpots = rootView.findViewById(R.id.layout_similar_spots);

        // 显示用户信息输入弹窗
        showUserInfoDialog();
    }

    /**
     * 显示用户信息输入弹窗
     */
    private void showUserInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_info, null);
        
        // 初始化控件
        RadioGroup rgGender = dialogView.findViewById(R.id.rg_gender);
        EditText etAge = dialogView.findViewById(R.id.et_age);
        
        builder.setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    // 获取性别选择
                    int selectedId = rgGender.getCheckedRadioButtonId();
                    RadioButton radioButton = dialogView.findViewById(selectedId);
                    String gender = radioButton.getText().toString();
                    
                    // 获取年龄输入
                    String ageString = etAge.getText().toString();
                    
                    // 验证年龄输入
                    if (TextUtils.isEmpty(ageString)) {
                        Toast.makeText(requireContext(), "请输入年龄", Toast.LENGTH_SHORT).show();
                        showUserInfoDialog(); // 重新显示对话框
                        return;
                    }
                    
                    try {
                        int age = Integer.parseInt(ageString);
                        if (age < 1 || age > 100) {
                            Toast.makeText(requireContext(), "请输入有效年龄（1-100）", Toast.LENGTH_SHORT).show();
                            showUserInfoDialog(); // 重新显示对话框
                            return;
                        }
                        
                        // 年龄验证通过，执行推荐
                        generateRecommendations(gender, age);
                        
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "请输入有效年龄", Toast.LENGTH_SHORT).show();
                        showUserInfoDialog(); // 重新显示对话框
                    }
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    // 用户取消，返回上一页
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                })
                .setCancelable(false) // 防止点击对话框外部关闭
                .create()
                .show();
    }

    /**
     * 生成景点推荐
     * @param gender 性别
     * @param age 年龄
     */
    private void generateRecommendations(String gender, int age) {
        // 创建SpotRecommender实例，使用全局SpotData确保搜索历史同步
        SpotRecommender recommender = new SpotRecommender(spotData, gender, age);
        
        // 显示正在加载提示
        Toast.makeText(requireContext(), "正在根据您的偏好和历史数据生成推荐...", Toast.LENGTH_SHORT).show();
        
        // 获取个性化推荐的景点
        List<Map<String, String>> recommendedSpots = recommender.recommendSpots();
        
        // 显示推荐结果
        displayRecommendations(recommendedSpots);
        
        // 获取用户最常搜索的景点类别
        String mostSearchedCategory = recommender.getMostSearchedCategory();
        
        // 获取相似类型的景点
        List<Map<String, String>> similarSpots = recommender.getSpotsByCategory(mostSearchedCategory);
        
        // 显示相似类型景点
        displaySimilarSpots(similarSpots, mostSearchedCategory);
        
        // 更新标题以反映推荐依据
        TextView recommendationTitle = rootView.findViewById(R.id.tv_recommendation_title);
        recommendationTitle.setText(String.format("根据您的偏好（%s, %d岁）和历史搜索推荐", gender, age));
    }

    /**
     * 显示个性化推荐景点
     * @param recommendedSpots 推荐景点列表
     */
    private void displayRecommendations(List<Map<String, String>> recommendedSpots) {
        if (recommendedSpots.isEmpty()) {
            Toast.makeText(requireContext(), "暂无推荐景点", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 根据推荐景点数量更新UI
        for (int i = 0; i < Math.min(recommendedSpots.size(), 3); i++) {
            Map<String, String> spot = recommendedSpots.get(i);
            
            // 获取对应控件
            TextView tvSpotName = rootView.findViewById(getResId("tv_spot_name_" + (i + 1)));
            TextView tvSpotCategory = rootView.findViewById(getResId("tv_spot_category_" + (i + 1)));
            TextView tvSpotDescription = rootView.findViewById(getResId("tv_spot_description_" + (i + 1)));
            
            // 设置内容
            tvSpotName.setText(spot.get("name"));
            tvSpotCategory.setText("类别：" + spot.get("category"));
            tvSpotDescription.setText(spot.get("description"));
            
            // 给推荐景点增加点击事件，点击时增加搜索次数
            LinearLayout layoutRecommendation = rootView.findViewById(getResId("layout_recommendation_" + (i + 1)));
            final String spotName = spot.get("name");
            layoutRecommendation.setOnClickListener(v -> {
                // 增加该景点的搜索次数
                demoApplication.increaseSearchTimes(spotName);
                Toast.makeText(requireContext(), "已将 " + spotName + " 加入您的兴趣点", Toast.LENGTH_SHORT).show();
            });
        }
    }

    /**
     * 显示相似类型的景点
     * @param similarSpots 相似景点列表
     * @param category 景点类别
     */
    private void displaySimilarSpots(List<Map<String, String>> similarSpots, String category) {
        // 更新标题
        TextView tvSimilarSpotsTitle = rootView.findViewById(R.id.tv_similar_spots_title);
        tvSimilarSpotsTitle.setText("您可能也会喜欢的" + category);
        
        // 清除旧内容
        layoutSimilarSpots.removeAllViews();
        
        if (similarSpots.isEmpty()) {
            TextView tvEmpty = new TextView(requireContext());
            tvEmpty.setText("暂无相关推荐");
            tvEmpty.setPadding(0, 16, 0, 16);
            layoutSimilarSpots.addView(tvEmpty);
            return;
        }
        
        // 最多显示5个相似景点
        int maxSimilarSpots = Math.min(similarSpots.size(), 5);
        
        for (int i = 0; i < maxSimilarSpots; i++) {
            Map<String, String> spot = similarSpots.get(i);
            
            // 防止重复显示已推荐的景点
            boolean isDuplicate = false;
            for (int j = 1; j <= 3; j++) {
                TextView tvRecommendedName = rootView.findViewById(getResId("tv_spot_name_" + j));
                if (tvRecommendedName != null && tvRecommendedName.getText() != null && 
                    tvRecommendedName.getText().toString().equals(spot.get("name"))) {
                    isDuplicate = true;
                    break;
                }
            }
            
            if (isDuplicate) continue;
            
            // 创建相似景点视图
            View similarSpotView = getLayoutInflater().inflate(R.layout.item_similar_spot, null);
            
            // 设置内容
            TextView tvName = similarSpotView.findViewById(R.id.tv_similar_spot_name);
            TextView tvDescription = similarSpotView.findViewById(R.id.tv_similar_spot_description);
            
            tvName.setText(spot.get("name"));
            tvDescription.setText(spot.get("description"));
            
            // 给相似景点增加点击事件，点击时增加搜索次数
            final String spotName = spot.get("name");
            similarSpotView.setOnClickListener(v -> {
                // 增加该景点的搜索次数
                demoApplication.increaseSearchTimes(spotName);
                Toast.makeText(requireContext(), "已将 " + spotName + " 加入您的兴趣点", Toast.LENGTH_SHORT).show();
            });
            
            // 添加到布局
            layoutSimilarSpots.addView(similarSpotView);
            
            // 添加间隔
            if (i < maxSimilarSpots - 1) {
                View divider = new View(requireContext());
                divider.setBackgroundColor(getResources().getColor(R.color.divider_color));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 1);
                params.setMargins(0, 8, 0, 8);
                divider.setLayoutParams(params);
                layoutSimilarSpots.addView(divider);
            }
        }
    }

    /**
     * 根据资源名称获取资源ID
     * @param resName 资源名称
     * @return 资源ID
     */
    private int getResId(String resName) {
        return getResources().getIdentifier(resName, "id", requireContext().getPackageName());
    }
} 