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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_spot_recommendation, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 从 Application 获取单例 SpotData
        demoApplication = (DemoApplication) requireActivity().getApplication();
        spotData = demoApplication.getSpotData();
        layoutSimilarSpots = rootView.findViewById(R.id.layout_similar_spots);
        showUserInfoDialog();
    }

    private void showUserInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_info, null);

        RadioGroup rgGender = dialogView.findViewById(R.id.rg_gender);
        EditText etAge    = dialogView.findViewById(R.id.et_age);

        builder.setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    int sid = rgGender.getCheckedRadioButtonId();
                    String gender = ((RadioButton) dialogView.findViewById(sid)).getText().toString();
                    String ageStr = etAge.getText().toString();
                    if (TextUtils.isEmpty(ageStr)) {
                        Toast.makeText(requireContext(), "请输入年龄", Toast.LENGTH_SHORT).show();
                        showUserInfoDialog();
                        return;
                    }
                    int age;
                    try {
                        age = Integer.parseInt(ageStr);
                        if (age < 1 || age > 100) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "请输入 1–100 的数字年龄", Toast.LENGTH_SHORT).show();
                        showUserInfoDialog();
                        return;
                    }
                    generateRecommendations(gender, age);
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    if (getActivity() != null) getActivity().onBackPressed();
                })
                .setCancelable(false)
                .show();
    }

    private void generateRecommendations(String gender, int age) {
        SpotRecommender recommender = new SpotRecommender(spotData, gender, age);
        Toast.makeText(requireContext(), "正在生成个性化推荐...", Toast.LENGTH_SHORT).show();

        // 个性化推荐（加权随机，最多3条）
        List<Map<String, String>> recs = recommender.recommendSpots();
        displayRecommendations(recs);

        // 最常搜索类别 → 相似景点（最多5条，不与上面重复）
        String topCategory = recommender.getMostSearchedCategory();
        List<Map<String, String>> simSpots = recommender.getSpotsByCategory(topCategory);
        displaySimilarSpots(simSpots, topCategory);

        // 更新标题
        TextView tvTitle = rootView.findViewById(R.id.tv_recommendation_title);
        tvTitle.setText(String.format("根据您的偏好（%s, %d岁）和历史搜索推荐", gender, age));
    }

    private void displayRecommendations(List<Map<String, String>> list) {
        if (list.isEmpty()) {
            Toast.makeText(requireContext(), "暂无推荐景点", Toast.LENGTH_SHORT).show();
            return;
        }
        int count = Math.min(3, list.size());
        for (int i = 0; i < count; i++) {
            Map<String,String> spot = list.get(i);
            int idx = i + 1;
            TextView tvName = rootView.findViewById(getResId("tv_spot_name_" + idx));
            TextView tvCat  = rootView.findViewById(getResId("tv_spot_category_" + idx));
            TextView tvDesc = rootView.findViewById(getResId("tv_spot_description_" + idx));
            tvName.setText(spot.get("name"));
            tvCat.setText("类别：" + spot.get("category"));
            tvDesc.setText(spot.get("description"));
            rootView.findViewById(getResId("layout_recommendation_" + idx))
                    .setOnClickListener(v -> {
                        demoApplication.increaseSearchTimes(spot.get("name"));
                        Toast.makeText(requireContext(),
                                "已将 " + spot.get("name") + " 加入兴趣点",
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void displaySimilarSpots(List<Map<String, String>> spots, String category) {
        TextView tvTitle = rootView.findViewById(R.id.tv_similar_spots_title);
        tvTitle.setText("您可能也会喜欢的" + category);
        layoutSimilarSpots.removeAllViews();

        if (spots.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("暂无相关推荐");
            empty.setPadding(0,16,0,16);
            layoutSimilarSpots.addView(empty);
            return;
        }

        int shown = 0;
        for (Map<String,String> spot: spots) {
            if (shown >= 5) break;
            // 跳过已在推荐列表中的
            boolean dup = false;
            for (int j = 1; j <= 3; j++) {
                TextView already = rootView.findViewById(getResId("tv_spot_name_" + j));
                if (already != null && spot.get("name").equals(already.getText().toString())) {
                    dup = true; break;
                }
            }
            if (dup) continue;

            View item = getLayoutInflater().inflate(R.layout.item_similar_spot, null);
            TextView n = item.findViewById(R.id.tv_similar_spot_name);
            TextView c = item.findViewById(R.id.tv_similar_spot_category);
            TextView d = item.findViewById(R.id.tv_similar_spot_description);
            n.setText(spot.get("name"));
            c.setText("类别：" + spot.get("category"));
            d.setText(spot.get("description"));
            item.setOnClickListener(v -> {
                demoApplication.increaseSearchTimes(spot.get("name"));
                Toast.makeText(requireContext(),
                        "已将 " + spot.get("name") + " 加入兴趣点",
                        Toast.LENGTH_SHORT).show();
            });
            layoutSimilarSpots.addView(item);

            if (++shown < 5 && shown < spots.size()) {
                View div = new View(requireContext());
                div.setBackgroundColor(getResources().getColor(R.color.divider_color));
                LinearLayout.LayoutParams p =
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, 1);
                p.setMargins(0,8,0,8);
                layoutSimilarSpots.addView(div, p);
            }
        }
    }

    private int getResId(String name) {
        return getResources()
                .getIdentifier(name, "id", requireContext().getPackageName());
    }
}
