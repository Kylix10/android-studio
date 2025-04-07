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
}