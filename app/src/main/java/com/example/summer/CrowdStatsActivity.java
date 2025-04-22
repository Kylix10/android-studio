package com.example.summer;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.summer.datas.SpotData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CrowdStatsActivity extends AppCompatActivity {

    private SpotData spotData;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crowd_stats);

        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("景点人流量统计");
        
        // 初始化景点数据
        spotData = new SpotData();
        
        // 获取景点人流量数据并排序展示
        displayCrowdStats();
    }
    
    private void displayCrowdStats() {
        // 获取布局容器
        LinearLayout container = findViewById(R.id.crowd_stats_container);
        
        // 创建一个列表来存储景点及其人流量
        List<Map.Entry<String, Integer>> spotEntries = new ArrayList<>();
        
        // 获取所有景点的人流量
        List<String> categories = new ArrayList<>();
        categories.add("自然风光");
        categories.add("历史文化");
        categories.add("景区设施");
        
        // 创建一个临时HashMap存储所有景点名称及其人流量
        Map<String, Integer> spotCrowdMap = new HashMap<>();
        
        // 为每个类别获取景点列表并处理
        for (String category : categories) {
            List<String> spots = spotData.optimizedSearch(category);
            
            for (String spotInfo : spots) {
                // 解析人流量信息，格式为："景点名称（人流量：XXX）"
                String spotName = spotInfo.substring(0, spotInfo.indexOf("（"));
                int crowdSize = Integer.parseInt(spotInfo.substring(
                        spotInfo.indexOf("：") + 1, 
                        spotInfo.indexOf("）")
                ));
                
                spotCrowdMap.put(spotName, crowdSize);
            }
        }
        
        // 转换为列表并排序
        for (Map.Entry<String, Integer> entry : spotCrowdMap.entrySet()) {
            spotEntries.add(entry);
        }
        
        // 根据人流量从高到低排序
        spotEntries.sort(new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        
        // 找出最大人流量，用于计算比例
        int maxCrowd = 0;
        if (!spotEntries.isEmpty()) {
            maxCrowd = spotEntries.get(0).getValue();
        }
        
        // 为每个景点创建一个条形图条目
        for (Map.Entry<String, Integer> entry : spotEntries) {
            String spotName = entry.getKey();
            int crowdSize = entry.getValue();
            
            // 创建条目布局
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setPadding(16, 8, 16, 8);
            
            // 创建标题文本
            TextView titleText = new TextView(this);
            titleText.setText(spotName + " - " + crowdSize + "人");
            titleText.setTextColor(Color.BLACK);
            titleText.setTextSize(16);
            itemLayout.addView(titleText);
            
            // 创建条形图布局
            LinearLayout barLayout = new LinearLayout(this);
            barLayout.setOrientation(LinearLayout.HORIZONTAL);
            barLayout.setPadding(0, 8, 0, 8);
            
            // 创建条形图
            View barView = new View(this);
            int barColor = ContextCompat.getColor(this, R.color.teal_700);
            barView.setBackgroundColor(barColor);
            
            // 计算条形图宽度比例
            int maxWidth = getResources().getDisplayMetrics().widthPixels - 64;
            int barWidth = (int) (maxWidth * ((float) crowdSize / maxCrowd));
            
            // 设置条形图尺寸
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    barWidth, 
                    getResources().getDimensionPixelSize(R.dimen.bar_height)
            );
            barView.setLayoutParams(params);
            
            barLayout.addView(barView);
            itemLayout.addView(barLayout);
            
            // 添加分隔线
            View divider = new View(this);
            divider.setBackgroundColor(Color.LTGRAY);
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 
                    1
            );
            dividerParams.setMargins(0, 8, 0, 8);
            divider.setLayoutParams(dividerParams);
            
            // 将条目添加到容器
            container.addView(itemLayout);
            container.addView(divider);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 