package com.example.summer.utils;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import java.util.HashMap;
import java.util.Map;

public class LocationManagerHelper {

    public static final String ACTION_LOCATION_UPDATE = "com.example.summer.action.LOCATION_UPDATE";
    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";
    public static final String ACTION_SPOT_ARRIVAL = "com.example.summer.action.SPOT_ARRIVAL";
    public static final String EXTRA_SPOT_ID = "spot_id";
    public static final String EXTRA_SPOT_NAME = "spot_name";

    private static final double ARRIVAL_THRESHOLD = 50;

    // 景点位置映射表（承德避暑山庄主要景点的大致坐标）
    private static final Map<String, double[]> SPOT_LOCATIONS = new HashMap<>();

    static {
        // 自然风光类
        SPOT_LOCATIONS.put("如意湖", new double[]{40.9758, 117.9412});
        SPOT_LOCATIONS.put("上湖", new double[]{40.9765, 117.9430});
        SPOT_LOCATIONS.put("热河泉", new double[]{40.9780, 117.9450});
        SPOT_LOCATIONS.put("内湖", new double[]{40.9745, 117.9420});
        SPOT_LOCATIONS.put("南山积雪", new double[]{40.9790, 117.9380});
        SPOT_LOCATIONS.put("四面云山", new double[]{40.9800, 117.9350});
        SPOT_LOCATIONS.put("万树园", new double[]{40.9730, 117.9460});
        SPOT_LOCATIONS.put("甫田丛樾", new double[]{40.9720, 117.9480});

        // 历史文化类
        SPOT_LOCATIONS.put("溥仁寺", new double[]{40.9680, 117.9350});
        SPOT_LOCATIONS.put("普宁寺", new double[]{40.9650, 117.9280});
        SPOT_LOCATIONS.put("法林寺", new double[]{40.9700, 117.9300});
        SPOT_LOCATIONS.put("碧峰寺遗址", new double[]{40.9820, 117.9320});
        SPOT_LOCATIONS.put("灵泽龙王庙", new double[]{40.9770, 117.9440});
        SPOT_LOCATIONS.put("永佑寺", new double[]{40.9710, 117.9400});
        SPOT_LOCATIONS.put("曲水荷香", new double[]{40.9750, 117.9400});
        SPOT_LOCATIONS.put("濠濮间想", new double[]{40.9740, 117.9415});
        SPOT_LOCATIONS.put("避暑山庄碑", new double[]{40.9715, 117.9390});
        SPOT_LOCATIONS.put("双湖夹镜碑", new double[]{40.9755, 117.9425});
        SPOT_LOCATIONS.put("绿毯八韵碑", new double[]{40.9725, 117.9450});
        SPOT_LOCATIONS.put("澹泊敬诚殿", new double[]{40.9710, 117.9385});
        SPOT_LOCATIONS.put("四知书屋", new double[]{40.9712, 117.9395});
        SPOT_LOCATIONS.put("烟波致爽殿", new double[]{40.9720, 117.9380});

        // 景区设施类
        SPOT_LOCATIONS.put("公共厕所1", new double[]{40.9740, 117.9405});
        SPOT_LOCATIONS.put("公共厕所2", new double[]{40.9760, 117.9440});
        SPOT_LOCATIONS.put("公共厕所3", new double[]{40.9725, 117.9430});
        SPOT_LOCATIONS.put("公共厕所4", new double[]{40.9775, 117.9410});
        SPOT_LOCATIONS.put("地下停车场", new double[]{40.9690, 117.9360});
        SPOT_LOCATIONS.put("地上停车场", new double[]{40.9685, 117.9370});
        SPOT_LOCATIONS.put("丽正门", new double[]{40.9705, 117.9365});
        SPOT_LOCATIONS.put("德汇门", new double[]{40.9750, 117.9470});
        SPOT_LOCATIONS.put("游客中心", new double[]{40.9700, 117.9375});
    }

    public static void broadcastLocationUpdate(Context context, double latitude, double longitude) {
        Intent intent = new Intent(ACTION_LOCATION_UPDATE);
        intent.putExtra(EXTRA_LATITUDE, latitude);
        intent.putExtra(EXTRA_LONGITUDE, longitude);
        context.sendBroadcast(intent);
    }

    public static void checkArrival(Context context, Location location) {
        for (Map.Entry<String, double[]> entry : SPOT_LOCATIONS.entrySet()) {
            String spotName = entry.getKey();
            double[] spotLocation = entry.getValue();

            double distance = calculateDistance(
                    location.getLatitude(),
                    location.getLongitude(),
                    spotLocation[0],
                    spotLocation[1]
            );

            if (distance < ARRIVAL_THRESHOLD) {
                broadcastArrival(context, spotName.hashCode(), spotName);
            }
        }
    }

    private static void broadcastArrival(Context context, int spotId, String spotName) {
        Intent intent = new Intent(ACTION_SPOT_ARRIVAL);
        intent.putExtra(EXTRA_SPOT_ID, spotId);
        intent.putExtra(EXTRA_SPOT_NAME, spotName);
        context.sendBroadcast(intent);
    }

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000;
    }

    public static double[] getSpotLocation(String spotName) {
        return SPOT_LOCATIONS.get(spotName);
    }
}
