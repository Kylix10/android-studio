package com.example.summer.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.summer.R;
import com.example.summer.datas.LocationConfig;
import com.example.summer.utils.LocationStateManager;
import com.example.summer.utils.NetworkUtils;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class GardenGuideFragment extends Fragment {
    private View rootView;

    // 天气面板引用
    private TextView tvTemp;
    private TextView tvWeatherStatus;
    private TextView tvAQI;
    private TextView tvHumidity;
    private TextView tvWind;
    private TextView tvUV;

    // 出行建议引用
    private TextView tvTravelAdvice;
    private TextView tvClothingAdvice;
    private TextView tvSunRainAdvice;
    private TextView tvClimbingAdvice;

    // 气象模拟按钮引用
    private Button btnSimulateSunny;
    private Button btnSimulateRainy;
    private Button btnSimulateWindy;
    
    private LocationStateManager.OnLocationChangeListener locationListener;
    private LocationConfig activeConfig;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_garden_guide, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 页面淡入过渡
        rootView.setAlpha(0f);
        rootView.animate().alpha(1f).setDuration(400).start();

        initViews();
        setupSimulators();
        
        // 注册位置状态变化监听器，根据切换到的景区自动配备符合当地气候特质的默认天气！
        locationListener = config -> {
            activeConfig = config;
            loadRealTimeWeather();
        };
        LocationStateManager.getInstance().registerListener(locationListener);
    }

    private void initViews() {
        tvTemp = rootView.findViewById(R.id.tvTemp);
        tvWeatherStatus = rootView.findViewById(R.id.tvWeatherStatus);
        tvAQI = rootView.findViewById(R.id.tvAQI);
        tvHumidity = rootView.findViewById(R.id.tvHumidity);
        tvWind = rootView.findViewById(R.id.tvWind);
        tvUV = rootView.findViewById(R.id.tvUV);

        tvTravelAdvice = rootView.findViewById(R.id.tvTravelAdvice);
        tvClothingAdvice = rootView.findViewById(R.id.tvClothingAdvice);
        tvSunRainAdvice = rootView.findViewById(R.id.tvSunRainAdvice);
        tvClimbingAdvice = rootView.findViewById(R.id.tvClimbingAdvice);

        btnSimulateSunny = rootView.findViewById(R.id.btnSimulateSunny);
        btnSimulateRainy = rootView.findViewById(R.id.btnSimulateRainy);
        btnSimulateWindy = rootView.findViewById(R.id.btnSimulateWindy);
    }

    private void setupSimulators() {
        btnSimulateSunny.setOnClickListener(v -> {
            simulateSunny();
            Toast.makeText(getContext(), "已切换模拟气象：炎热晴天 ☀️", Toast.LENGTH_SHORT).show();
        });

        btnSimulateRainy.setOnClickListener(v -> {
            simulateRainy();
            Toast.makeText(getContext(), "已切换模拟气象：雷雨湿滑 ⛈️", Toast.LENGTH_SHORT).show();
        });

        btnSimulateWindy.setOnClickListener(v -> {
            simulateWindy();
            Toast.makeText(getContext(), "已切换模拟气象：寒冷大风 💨", Toast.LENGTH_SHORT).show();
        });
    }

    // 从 Open-Meteo 天气接口异步拉取真实天气
    private void loadRealTimeWeather() {
        if (activeConfig == null) return;

        double lat = activeConfig.getLatitude();
        double lon = activeConfig.getLongitude();

        NetworkUtils.getRealTimeWeather(lat, lon, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        loadFallbackWeather();
                        Toast.makeText(getContext(), "获取实时天气失败，已加载离线天气", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        JSONObject current = jsonObject.optJSONObject("current");
                        if (current != null) {
                            double temp = current.optDouble("temperature_2m", 25);
                            int humidity = current.optInt("relative_humidity_2m", 50);
                            int code = current.optInt("weather_code", 0);
                            double windSpeed = current.optDouble("wind_speed_10m", 5.0);

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    updateRealWeatherUI(temp, humidity, code, windSpeed);
                                });
                            }
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                // 走降级逻辑
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        loadFallbackWeather();
                    });
                }
            }
        });
    }

    private void updateRealWeatherUI(double temp, int humidity, int code, double windSpeed) {
        tvTemp.setText(String.format("%.0f°C", temp));
        tvHumidity.setText(humidity + "%");
        
        // AQI 模拟，因为天气 API 通常不直接带 AQI 核心指标
        int simulatedAqi = 25 + (int)(temp * 0.8) % 60;
        tvAQI.setText("AQI: " + simulatedAqi + " 优");

        // WMO 天气映射
        String weatherStr = "多云 / Cloudy";
        String uvStr = "中等";
        
        // 出行策略文本
        String travel;
        String clothing;
        String protection;
        String climbing;

        if (code == 0) {
            weatherStr = "晴朗 / Sunny";
            uvStr = "极强";
            travel = "今日天气晴朗，能见度好，非常适合户外摄影和景区长途打卡。中午紫外线极强，不建议长时间在暴晒区剧烈运动，请注意防暑。";
            clothing = "建议穿着轻薄短袖、吸汗棉衫，配戴大沿防晒帽或遮阳镜。";
            protection = "防晒系数建议 SPF50+ 以上，需随时涂抹防晒霜并携带遮阳伞。";
            climbing = "天气较热，登山排汗量极大，请随身带足矿泉水，谨防中暑脱水。";
        } else if (code >= 51 && code <= 82) { // 雨季
            weatherStr = "阵雨 / Rainy";
            uvStr = "弱";
            travel = "目前景区正伴有降水，地面湿滑，空气湿度极高。室外高山段可能采取临时封闭，建议主要以室内博物馆或宫殿长廊避雨游览为主。";
            clothing = "气温有所下降，建议加穿轻防水风衣，并选用防滑耐磨运动鞋。";
            protection = "雨水天气，出行请务必随身携带雨伞或优质抗风雨衣。";
            climbing = "高空山路台阶陡峭湿滑，大雨天容易滑倒甚至有雷电风险，不建议登山。";
        } else if (code >= 71 && code <= 86) { // 雪季
            weatherStr = "大雪 / Snowy";
            uvStr = "弱";
            travel = "景区正迎来降雪，冰雪景观极其壮丽。由于路面结冰滑溜，请注意台阶和坡道安全，避开陡峭台阶。";
            clothing = "强烈建议加穿保暖羽绒服、毛线手套、围巾，注意保暖防滑。";
            protection = "雪地反光严重，建议佩戴护目镜或墨镜，防紫外线反射。";
            climbing = "雪季台阶结冰危险，严禁盲目攀登陡峭石阶，可选用索道通行。";
        } else if (code >= 95 && code <= 99) { // 雷雨
            weatherStr = "雷暴 / Storm";
            uvStr = "弱";
            travel = "今日有大风雷暴天气，雨势强劲。出于安全起见，所有室外攀登路线和水上游船全部暂停服务，请在室内安全掩体内暂避。";
            clothing = "气温寒凉，建议穿着防风防雨外套，注意保护头部。";
            protection = "雷电天气切勿打带有金属尖的雨伞，远离大树与高空金属设施。";
            climbing = "雷暴天登高极度危险，属于雷击易发区，严禁攀登任何高山山顶。";
        } else { // 阴天或多云
            weatherStr = "多云 / Cloudy";
            uvStr = "中等";
            travel = "今日天空云量较多，紫外线较弱，气温温和宜人，是全天步行漫游、骑行赏景的黄金天气。";
            clothing = "建议穿着轻便长袖卫衣、纯棉运动裤与软底跑步鞋。";
            protection = "紫外线温和，正常涂抹日常防晒即可，无需携带重型防晒伞。";
            climbing = "气温十分舒适，负氧离子充沛，极利于登高强身，合理分配体力即可。";
        }

        tvWeatherStatus.setText(weatherStr);
        tvUV.setText(uvStr);

        // 风力描述
        String windStr = "微风 1级";
        if (windSpeed < 10) {
            windStr = "微风 2级";
        } else if (windSpeed < 20) {
            windStr = "和风 3级";
        } else if (windSpeed < 30) {
            windStr = "清风 4级";
            travel = "今日风力强劲，虽然视野开阔，但高空悬崖和缆车区可能停运，游览请注意头部防风和安全保暖。";
            clothing = "强烈建议加穿防风冲锋衣或带帽风衣，注意防风固发。";
        } else {
            windStr = "大风 5级以上";
            travel = "今日有西北大风，风力强劲。高空观景台风力刺骨，极易发生吹落衣物，不建议老人或儿童在此天气下登顶。";
            clothing = "必须穿着厚实防风外套、拉紧拉链，避免宽大衣物兜风。";
        }
        tvWind.setText(windStr);

        tvTravelAdvice.setText(travel);
        tvClothingAdvice.setText(clothing);
        tvSunRainAdvice.setText(protection);
        tvClimbingAdvice.setText(climbing);
    }

    private void loadFallbackWeather() {
        if (activeConfig == null) return;
        
        if (activeConfig.getName().contains("西湖")) {
            simulateRainy();
        } else if (activeConfig.getName().contains("泰山")) {
            simulateWindy();
        } else {
            simulateSunny();
        }
    }

    private void simulateSunny() {
        tvTemp.setText("34°C");
        tvWeatherStatus.setText("晴朗 / Sunny");
        tvAQI.setText("AQI: 56 良");
        tvHumidity.setText("30%");
        tvWind.setText("西南风 2级");
        tvUV.setText("极强");

        tvTravelAdvice.setText("今日天气炎热，紫外线照度极强，非常适宜水上游船或树荫避暑。不建议在中午高温段进行无遮挡的烈日攀爬，请合理安排体力。");
        tvClothingAdvice.setText("建议穿着透气轻薄棉质衣物、凉鞋，配戴防晒帽。");
        tvSunRainAdvice.setText("防晒系数SPF50+以上，建议随身携带防晒伞及墨镜。");
        tvClimbingAdvice.setText("天气较热，登山排汗量极大，请随身带足温水以防中暑。");
    }

    private void simulateRainy() {
        tvTemp.setText("21°C");
        tvWeatherStatus.setText("雷阵雨 / Storm");
        tvAQI.setText("AQI: 22 优");
        tvHumidity.setText("95%");
        tvWind.setText("东南风 4级");
        tvUV.setText("弱");

        tvTravelAdvice.setText("今日景区受雷阵雨天气影响，空气温润。室外路面湿滑，出于安全起见，山峦区可能采取临时管制，建议多在宫殿区室内展馆如烟波致爽等避雨游览。");
        tvClothingAdvice.setText("气温下降，建议携带轻便防水风衣或带连帽长袖。");
        tvSunRainAdvice.setText("雷雨大风天气，请务必随身携带雨伞或耐磨雨衣，防雷电。");
        tvClimbingAdvice.setText("山路台阶陡峭湿滑，大风雷雨天登山较危险，建议暂停登高。");
    }

    private void simulateWindy() {
        tvTemp.setText("12°C");
        tvWeatherStatus.setText("多云大风 / Windy");
        tvAQI.setText("AQI: 35 优");
        tvHumidity.setText("40%");
        tvWind.setText("西北风 5级");
        tvUV.setText("中等");

        tvTravelAdvice.setText("今日西北大风降温，天气晴冷，但能见度极高，非常适合登高极目远眺避暑山庄水榭全景及四周外八庙宏伟建筑，但高空山顶风力强劲，注意御寒。");
        tvClothingAdvice.setText("强烈建议穿着冲锋衣、卫衣或轻薄羽绒服，注意防寒。");
        tvSunRainAdvice.setText("气候干燥多风，建议携带保温水杯补充水分，防风沙。");
        tvClimbingAdvice.setText("山顶温度低且风力大，登山时请扎紧袖口，注意抓稳扶手。");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (locationListener != null) {
            LocationStateManager.getInstance().unregisterListener(locationListener);
        }
        rootView = null;
    }
}
