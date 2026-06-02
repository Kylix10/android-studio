package com.example.summer.utils;

import android.util.Log;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class WenXin {

    private static final String TAG = "WenXin";

    // 请将此处替换为你在千帆控制台获取的真实 API Key
    private static final String API_KEY = "bce-v3/ALTAK-bPMHgqooLH6SMnZhAxDaL/3ce96d049a8390ab9afdf739c82bfd52d3fbd6f6";
    private static final String APP_ID = "app-ZdgFs9Hq";

    private final OkHttpClient client;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public WenXin() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    public String getLocationIntroduction(String location) {
        try {
            // 动态抓取当前景区名称拼装用户提问
            String activeName = com.example.summer.utils.LocationStateManager.getInstance().getCurrentLocation().getName();

            // 构建请求的 JSON Body
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("model", "ernie-3.5-8k");

            JSONArray messages = new JSONArray();

            // 1. 系统提示词
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一位" + activeName + "的地图导览助手，你只回答对于输入的" + activeName + "内的地点名称的简介，回答字数限制在250字以内");
            messages.put(systemMessage);

            // 2. 用户输入
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", "请介绍一下" + activeName + "的 " + location);
            messages.put(userMessage);

            bodyJson.put("messages", messages);

            RequestBody body = RequestBody.create(bodyJson.toString(), JSON);

            // 构建带 appid 头的 HTTP 请求 (符合千帆官方接口说明)
            Request request = new Request.Builder()
                    .url("https://qianfan.baidubce.com/v2/chat/completions")
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("appid", APP_ID)
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseStr = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful() && !responseStr.isEmpty()) {
                    JSONObject responseJson = new JSONObject(responseStr);
                    JSONArray choices = responseJson.optJSONArray("choices");
                    if (choices != null && choices.length() > 0) {
                        JSONObject firstChoice = choices.getJSONObject(0);
                        JSONObject message = firstChoice.optJSONObject("message");
                        if (message != null) {
                            return message.optString("content", "");
                        }
                    }
                    Log.e(TAG, "解析响应数据失败，空内容。响应: " + responseStr);
                } else {
                    Log.e(TAG, "请求失败，状态码: " + response.code() + ", 消息: " + response.message() + ", 详情: " + responseStr);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取景点简介异常", e);
        }
        return null;
    }
}