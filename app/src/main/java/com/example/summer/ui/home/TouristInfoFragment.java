package com.example.summer.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.summer.R;
import com.example.summer.datas.LocationConfig;
import com.example.summer.utils.LocationStateManager;
import com.example.summer.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TouristInfoFragment extends Fragment {
    private View rootView;
    private RecyclerView rvLocalLife;
    private final List<Merchant> allMerchants = new ArrayList<>();
    private final List<Merchant> filteredMerchants = new ArrayList<>();
    private LocalLifeAdapter adapter;

    // 分类 Tab 视图引用
    private TextView tabFood;
    private TextView tabHotel;
    private TextView tabShop;
    private TextView tabFun;
    
    private LocationStateManager.OnLocationChangeListener locationListener;
    private LocationConfig activeConfig;
    private String currentCategory = "food";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tourist_info, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 页面淡入动画效果
        rootView.setAlpha(0f);
        rootView.animate().alpha(1f).setDuration(400).start();

        initViews();
        
        // 注册位置状态变化监听器，自动响应景区定位切换，更新商户推荐列表！
        locationListener = config -> {
            activeConfig = config;
            loadRealNearbyPlaces(currentCategory);
        };
        LocationStateManager.getInstance().registerListener(locationListener);
    }

    private void initViews() {
        rvLocalLife = rootView.findViewById(R.id.rvLocalLife);
        rvLocalLife.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LocalLifeAdapter(filteredMerchants);
        rvLocalLife.setAdapter(adapter);

        tabFood = rootView.findViewById(R.id.tabFood);
        tabHotel = rootView.findViewById(R.id.tabHotel);
        tabShop = rootView.findViewById(R.id.tabShop);
        tabFun = rootView.findViewById(R.id.tabFun);

        tabFood.setOnClickListener(v -> selectCategory("food"));
        tabHotel.setOnClickListener(v -> selectCategory("hotel"));
        tabShop.setOnClickListener(v -> selectCategory("shop"));
        tabFun.setOnClickListener(v -> selectCategory("fun"));
    }

    private void selectCategory(String category) {
        currentCategory = category;
        resetTabs();
        
        // 高亮选中 tab 并过滤数据
        switch (category) {
            case "food":
                highlightTab(tabFood);
                break;
            case "hotel":
                highlightTab(tabHotel);
                break;
            case "shop":
                highlightTab(tabShop);
                break;
            case "fun":
                highlightTab(tabFun);
                break;
        }

        loadRealNearbyPlaces(category);
    }

    private void resetTabs() {
        if (getContext() == null) return;
        int unselectedBg = ContextCompat.getColor(requireContext(), R.color.light_gray);
        int blackColor = ContextCompat.getColor(requireContext(), R.color.black);

        TextView[] tabs = {tabFood, tabHotel, tabShop, tabFun};
        for (TextView tab : tabs) {
            if (tab != null) {
                tab.setBackgroundColor(unselectedBg);
                tab.setTextColor(blackColor);
                tab.setScaleX(1.0f);
                tab.setScaleY(1.0f);
            }
        }
    }

    private void highlightTab(TextView tab) {
        if (tab != null && getContext() != null) {
            int selectedBg = ContextCompat.getColor(requireContext(), R.color.purple_500);
            int whiteColor = ContextCompat.getColor(requireContext(), R.color.white);
            tab.setBackgroundColor(selectedBg);
            tab.setTextColor(whiteColor);
            
            // 添加轻微的缩放反馈效果
            tab.animate().scaleX(1.05f).scaleY(1.05f).setDuration(150).start();
        }
    }

    // 从百度 Place API 获取周边商家真实数据
    private void loadRealNearbyPlaces(String category) {
        if (activeConfig == null) return;
        
        String query;
        switch (category) {
            case "food":
                query = "美食";
                break;
            case "hotel":
                query = "酒店$民宿";
                break;
            case "shop":
                query = "商超$购物$便利店";
                break;
            case "fun":
                query = "景点$电影院$KTV$足疗";
                break;
            default:
                query = "商户";
                break;
        }

        double lat = activeConfig.getLatitude();
        double lon = activeConfig.getLongitude();

        NetworkUtils.getNearbyPlaces(query, lat, lon, 3000, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        loadFallbackData(category);
                        Toast.makeText(getContext(), "获取实时商户失败，已加载离线推荐", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        int status = jsonObject.optInt("status", -1);
                        if (status == 0) {
                            JSONArray results = jsonObject.optJSONArray("results");
                            List<Merchant> newList = new ArrayList<>();
                            if (results != null && results.length() > 0) {
                                for (int i = 0; i < results.length(); i++) {
                                    JSONObject item = results.getJSONObject(i);
                                    String name = item.optString("name", "优选商户");
                                    
                                    // 坐标解析与真实距离计算
                                    JSONObject loc = item.optJSONObject("location");
                                    String distanceStr = "300m";
                                    if (loc != null) {
                                        double placeLat = loc.optDouble("lat", 0);
                                        double placeLng = loc.optDouble("lng", 0);
                                        double distanceMeters = haversine(lat, lon, placeLat, placeLng) * 1000;
                                        if (distanceMeters < 1000) {
                                            distanceStr = (int) distanceMeters + "m";
                                        } else {
                                            distanceStr = String.format("%.1fkm", distanceMeters / 1000.0);
                                        }
                                    }

                                    // 基于名称 Hash 动态配对合理的星级与均价
                                    int hash = Math.abs(name.hashCode());
                                    double rating = 4.3 + (hash % 7) * 0.1; // 4.3 - 4.9 分
                                    int avgPrice = 0;
                                    if (category.equals("food")) {
                                        avgPrice = 25 + (hash % 12) * 10;
                                    } else if (category.equals("hotel")) {
                                        avgPrice = 120 + (hash % 40) * 20;
                                    } else if (category.equals("fun")) {
                                        avgPrice = 15 + (hash % 10) * 15;
                                    }

                                    String address = item.optString("address", "周边优质商户推荐");
                                    if (address.length() > 24) {
                                        address = address.substring(0, 22) + "...";
                                    }

                                    newList.add(new Merchant(name, rating, avgPrice, distanceStr, address, category));
                                }

                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        allMerchants.clear();
                                        allMerchants.addAll(newList);
                                        filteredMerchants.clear();
                                        filteredMerchants.addAll(newList);
                                        if (adapter != null) {
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                                return;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                // 若解析失败，走到降级逻辑
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        loadFallbackData(category);
                    });
                }
            }
        });
    }

    private void loadFallbackData(String category) {
        initMockData(activeConfig != null ? activeConfig.getName() : "承德避暑山庄");
        filteredMerchants.clear();
        for (Merchant m : allMerchants) {
            if (m.category.equalsIgnoreCase(category)) {
                filteredMerchants.add(m);
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // 计算球面两点间距离 (km)
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void initMockData(String locationName) {
        allMerchants.clear();
        
        // 包含 "当前" 的 GPS 定位也可以加载对应的 Mock 降级包
        if (locationName.contains("北京") || locationName.contains("颐和园")) {
            allMerchants.add(new Merchant("颐和园听鹂馆餐厅", 4.9, 150, "80m", "中华老字号宫廷风味", "food"));
            allMerchants.add(new Merchant("颐和园西门烤鸭店", 4.7, 110, "500m", "地道北京烤鸭", "food"));
            allMerchants.add(new Merchant("昆明湖畔茶水小吃", 4.5, 35, "300m", "特色宫廷点心", "food"));
            
            allMerchants.add(new Merchant("北京颐和安蔓酒店", 4.9, 3800, "150m", "顶级奢华四合院酒店", "hotel"));
            allMerchants.add(new Merchant("昆明湖快捷假日酒店", 4.6, 320, "1.2km", "舒适快捷住宿", "hotel"));
            allMerchants.add(new Merchant("万寿山青年旅馆", 4.4, 98, "900m", "平价便利青旅", "hotel"));
            
            allMerchants.add(new Merchant("颐和园文创特产旗舰店", 4.8, 85, "120m", "精美宫廷特色手礼", "shop"));
            allMerchants.add(new Merchant("宽广超市北京分店", 4.6, 0, "800m", "连锁生活物资超市", "shop"));
            allMerchants.add(new Merchant("昆明湖旅游特产总汇", 4.4, 45, "400m", "纪念品特产专卖", "shop"));
            
            allMerchants.add(new Merchant("十七孔桥皇家茶轩", 4.8, 65, "250m", "临湖赏景品茗茶艺", "fun"));
            allMerchants.add(new Merchant("颐和园游船码头", 4.9, 80, "180m", "湖光山色画中游船", "fun"));
            allMerchants.add(new Merchant("万寿山松涛琴吧", 4.5, 45, "600m", "国风古筝民乐演绎", "fun"));
            
        } else if (locationName.contains("杭州") || locationName.contains("西湖")) {
            allMerchants.add(new Merchant("楼外楼西湖醋鱼", 4.8, 160, "150m", "百年历史杭帮名菜", "food"));
            allMerchants.add(new Merchant("知味观味庄", 4.7, 110, "450m", "特色江南点心小吃", "food"));
            allMerchants.add(new Merchant("外婆家(西湖店)", 4.6, 75, "750m", "高性价比家常杭帮菜", "food"));
            
            allMerchants.add(new Merchant("杭州西子宾馆", 4.9, 1200, "300m", "国宾馆级园林湖景房", "hotel"));
            allMerchants.add(new Merchant("西湖国宾馆", 4.9, 1500, "600m", "江南水乡古典度假酒店", "hotel"));
            allMerchants.add(new Merchant("断桥边特色情调民宿", 4.7, 220, "350m", "网红赏荷观景民宿", "hotel"));
            
            allMerchants.add(new Merchant("杭州龙井茶叶总汇", 4.8, 260, "200m", "地道正宗西湖龙井", "shop"));
            allMerchants.add(new Merchant("西湖文创生活馆", 4.7, 0, "180m", "江南水乡特色文创", "shop"));
            allMerchants.add(new Merchant("采芝斋特产专卖店", 4.5, 45, "350m", "传统杭州糕点酥糖", "shop"));
            
            allMerchants.add(new Merchant("湖畔居茶楼", 4.9, 98, "220m", "俯瞰西湖最佳龙井茶馆", "fun"));
            allMerchants.add(new Merchant("印象西湖山水实景演出", 4.9, 280, "1.1km", "大型山水演艺秀", "fun"));
            allMerchants.add(new Merchant("西湖手划摇橹船", 4.8, 150, "100m", "悠闲泛舟体验江南风情", "fun"));
            
        } else if (locationName.contains("泰安") || locationName.contains("泰山")) {
            allMerchants.add(new Merchant("泰山封禅御膳房", 4.8, 90, "200m", "传统齐鲁宫廷菜肴", "food"));
            allMerchants.add(new Merchant("红门豆腐宴老店", 4.7, 65, "150m", "非遗手作豆腐宴", "food"));
            allMerchants.add(new Merchant("泰山山顶补给面馆", 4.2, 35, "950m", "登顶补充热汤热量", "food"));
            
            allMerchants.add(new Merchant("泰山神憩宾馆", 4.8, 680, "980m", "山顶观日出极佳观景房", "hotel"));
            allMerchants.add(new Merchant("仙居宾馆", 4.6, 520, "900m", "南天门下舒适山顶客栈", "hotel"));
            allMerchants.add(new Merchant("红门脚下泰山别院", 4.7, 180, "100m", "登山起点平价四合院", "hotel"));
            
            allMerchants.add(new Merchant("泰山女儿茶直营店", 4.8, 120, "250m", "高山女儿茶特产专卖", "shop"));
            allMerchants.add(new Merchant("泰山玉器文创工坊", 4.7, 0, "400m", "祈福辟邪泰山玉石雕", "shop"));
            allMerchants.add(new Merchant("登山杖平价补给超市", 4.5, 15, "50m", "雨具拐杖攀登物资补给", "shop"));
            
            allMerchants.add(new Merchant("泰安岱庙历史博物馆", 4.8, 50, "1.5km", "古代帝王祭祀泰山场所", "fun"));
            allMerchants.add(new Merchant("封禅大典实景演出", 4.9, 180, "3.5km", "大型帝王登基祈福大典", "fun"));
            allMerchants.add(new Merchant("泰山中天门客运索道", 4.9, 100, "1.8km", "飞越山峦直达南天门", "fun"));
            
        } else {
            // 承德及通用降级 mock data
            allMerchants.add(new Merchant("山庄秘制私房菜", 4.9, 85, "120m", "特色宫廷菜", "food"));
            allMerchants.add(new Merchant("塞外风情烤全羊", 4.7, 125, "450m", "地方烧烤大餐", "food"));
            allMerchants.add(new Merchant("承德百年老灶火锅", 4.6, 75, "800m", "地道火锅暖身", "food"));
            
            allMerchants.add(new Merchant("绮望楼皇家宾馆", 4.9, 580, "80m", "宫廷宿营/环境雅致", "hotel"));
            allMerchants.add(new Merchant("避暑山庄明珠大酒店", 4.8, 320, "260m", "高性价比观景客房", "hotel"));
            allMerchants.add(new Merchant("塞外客栈青年民宿", 4.5, 95, "950m", "平价温馨风民宿", "hotel"));
            
            allMerchants.add(new Merchant("金龙购物广场", 4.7, 0, "400m", "大型综合购物中心", "shop"));
            allMerchants.add(new Merchant("宽广时代超市", 4.6, 0, "300m", "连锁生活物资超市", "shop"));
            allMerchants.add(new Merchant("山庄特产总汇", 4.4, 45, "150m", "承德手信特产专卖", "shop"));
            
            allMerchants.add(new Merchant("御茶房中式茶馆", 4.9, 55, "180m", "清代宫廷茶艺表演", "fun"));
            allMerchants.add(new Merchant("塞外清风足道会所", 4.6, 98, "650m", "传统中式足疗放松", "fun"));
            allMerchants.add(new Merchant("避暑山庄精酿清吧", 4.5, 45, "550m", "民谣演艺/特调啤酒", "fun"));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (locationListener != null) {
            LocationStateManager.getInstance().unregisterListener(locationListener);
        }
        rvLocalLife = null;
        rootView = null;
    }

    // 商家实体类
    private static class Merchant {
        String name;
        double rating;
        int avgPrice;
        String distance;
        String tag;
        String category;

        Merchant(String name, double rating, int avgPrice, String distance, String tag, String category) {
            this.name = name;
            this.rating = rating;
            this.avgPrice = avgPrice;
            this.distance = distance;
            this.tag = tag;
            this.category = category;
        }
    }

    // RecyclerView 适配器
    private class LocalLifeAdapter extends RecyclerView.Adapter<LocalLifeAdapter.ViewHolder> {
        private final List<Merchant> list;

        LocalLifeAdapter(List<Merchant> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_local_life, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Merchant item = list.get(position);
            holder.tvMerchantName.setText(item.name);
            holder.tvRating.setText(String.format("★ %.1f", item.rating));
            
            if (item.avgPrice > 0) {
                holder.tvAvgPrice.setText(String.format("人均 ￥%d", item.avgPrice));
                holder.tvAvgPrice.setVisibility(View.VISIBLE);
            } else {
                holder.tvAvgPrice.setVisibility(View.GONE);
            }
            
            holder.tvDistance.setText(item.distance);
            holder.tvTag.setText(item.tag);

            if (item.category.equalsIgnoreCase("food")) {
                holder.ivMerchantIcon.setImageResource(android.R.drawable.ic_menu_today);
            } else if (item.category.equalsIgnoreCase("hotel")) {
                holder.ivMerchantIcon.setImageResource(android.R.drawable.ic_lock_power_off);
            } else if (item.category.equalsIgnoreCase("shop")) {
                holder.ivMerchantIcon.setImageResource(android.R.drawable.ic_input_add);
            } else {
                holder.ivMerchantIcon.setImageResource(android.R.drawable.ic_media_play);
            }
            
            if (getContext() != null) {
                holder.ivMerchantIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.text));
            }

            // 一键导航操作
            holder.btnNavigate.setOnClickListener(v -> {
                Toast.makeText(getContext(), "已将 " + item.name + " 设为导航目的地，正在启动路径规划...", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivMerchantIcon;
            TextView tvMerchantName;
            TextView tvRating;
            TextView tvAvgPrice;
            TextView tvDistance;
            TextView tvTag;
            TextView btnNavigate;

            ViewHolder(View itemView) {
                super(itemView);
                ivMerchantIcon = itemView.findViewById(R.id.ivMerchantIcon);
                tvMerchantName = itemView.findViewById(R.id.tvMerchantName);
                tvRating = itemView.findViewById(R.id.tvRating);
                tvAvgPrice = itemView.findViewById(R.id.tvAvgPrice);
                tvDistance = itemView.findViewById(R.id.tvDistance);
                tvTag = itemView.findViewById(R.id.tvTag);
                btnNavigate = itemView.findViewById(R.id.btnNavigate);
            }
        }
    }
}