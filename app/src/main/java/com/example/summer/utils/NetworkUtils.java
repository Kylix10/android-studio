package com.example.summer.utils;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.net.URLEncoder;

public class NetworkUtils {
    private static final String API_URL = "https://api.map.baidu.com/geocoding/v3/";
    private static final String API_KEY = "IvsyvCZEAOcleZHRRSQaw574IgdtFoPt"; // 替换为你的AK
    private static final OkHttpClient client = new OkHttpClient();

    public static void getLocationFromAddress(String address, Callback callback) {
        try {
            String encodedAddress = URLEncoder.encode(address, "UTF-8");
            String url = API_URL + "?address=" + encodedAddress
                    + "&output=json"
                    + "&ak=" + API_KEY
                    + "&callback=showLocation";
            //System.out.println("请求URL：" + url);
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(callback);
        } catch (IOException e) {
            callback.onFailure(null, e);
        }
    }

    public static void getNearbyPlaces(String query, double lat, double lon, int radius, Callback callback) {
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String url = "https://api.map.baidu.com/place/v2/search"
                    + "?query=" + encodedQuery
                    + "&location=" + lat + "," + lon
                    + "&radius=" + radius
                    + "&output=json"
                    + "&ak=" + API_KEY
                    + "&page_size=15";
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            client.newCall(request).enqueue(callback);
        } catch (IOException e) {
            callback.onFailure(null, e);
        }
    }

    public static void getRealTimeWeather(double lat, double lon, Callback callback) {
        String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + lat
                + "&longitude=" + lon
                + "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m"
                + "&timezone=auto";
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(callback);
    }
}