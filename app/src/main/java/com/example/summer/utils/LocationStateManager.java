package com.example.summer.utils;

import com.example.summer.R;
import com.example.summer.datas.LocationConfig;
import java.util.ArrayList;
import java.util.List;

public class LocationStateManager {
    private static LocationStateManager instance;
    private LocationConfig currentConfig;
    private final List<OnLocationChangeListener> listeners = new ArrayList<>();
    private final List<LocationConfig> presetLocations = new ArrayList<>();

    public interface OnLocationChangeListener {
        void onLocationChanged(LocationConfig config);
    }

    private LocationStateManager() {
        // Initialize presets
        presetLocations.add(new LocationConfig(
                "承德避暑山庄",
                40.9978,
                117.9413,
                new int[]{R.drawable.banner1, R.drawable.banner2, R.drawable.banner3, R.drawable.banner4},
                "101090400",
                "400-0314-999",
                "\n医疗救助点：宫殿区东侧游客中心内\n\n警务服务站：丽正门入口右侧\n\n景区内设有免费饮水点（湖区、宫殿区）、母婴室及无障碍通道。"
        ));
        presetLocations.add(new LocationConfig(
                "北京颐和园",
                39.9998,
                116.2755,
                new int[]{R.drawable.banner2, R.drawable.banner3, R.drawable.banner4, R.drawable.banner1},
                "101010100",
                "010-62881144",
                "\n医疗救助点：东宫门游客服务中心内\n\n警务服务站：新建宫门警务岗亭\n\n景区内设有免费饮水点、母婴室及无障碍通道。"
        ));
        presetLocations.add(new LocationConfig(
                "杭州西湖",
                30.2526,
                120.1502,
                new int[]{R.drawable.banner3, R.drawable.banner4, R.drawable.banner1, R.drawable.banner2},
                "101210101",
                "0571-87179617",
                "\n医疗救助点：断桥游客咨询服务点内\n\n警务服务站：柳浪闻莺警务室\n\n景区内设有母婴室、残疾人无障碍通道及免费直饮水点。"
        ));
        presetLocations.add(new LocationConfig(
                "泰安泰山",
                36.2559,
                117.1082,
                new int[]{R.drawable.banner4, R.drawable.banner1, R.drawable.banner2, R.drawable.banner3},
                "101120801",
                "0538-5369666",
                "\n医疗救助点：中天门及南天门医疗救护站内\n\n警务服务站：红门派出所及南天门执勤点\n\n景区内设有防滑台阶拐杖租借处、母婴室及安全防护设施。"
        ));

        // Default to Chengde Summer Resort
        currentConfig = presetLocations.get(0);
    }

    public static synchronized LocationStateManager getInstance() {
        if (instance == null) {
            instance = new LocationStateManager();
        }
        return instance;
    }

    public LocationConfig getCurrentLocation() {
        return currentConfig;
    }

    public void setCurrentLocation(LocationConfig config) {
        this.currentConfig = config;
        notifyListeners();
    }

    public List<LocationConfig> getPresetLocations() {
        return presetLocations;
    }

    public void registerListener(OnLocationChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            // Instantly sync the initial status upon registration
            listener.onLocationChanged(currentConfig);
        }
    }

    public void unregisterListener(OnLocationChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (OnLocationChangeListener listener : listeners) {
            listener.onLocationChanged(currentConfig);
        }
    }
}
