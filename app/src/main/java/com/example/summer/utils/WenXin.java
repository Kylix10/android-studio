package com.example.summer.utils;

import okhttp3.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WenXin {

    public static final String API_KEY = "qrwZfhrrNEwJsgOb1YKLPA9I";
    public static final String SECRET_KEY = "vmTGbkzOWxNjP60BrmMVUDeSD5Pv8K0D";

    public List<Map<String, String>> Dialogue_Content;

    private static final int MAX_RETRIES = 3;
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public WenXin() {
        Dialogue_Content = new ArrayList<>();
    }

    public String getLocationIntroduction(String location) throws IOException {
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", "请介绍一下承德避暑山庄的 " + location);

        Dialogue_Content.add(userMessage);

        String messagesJson = buildMessagesJson();
        String requestBody = "{\"messages\":" +
                messagesJson +
                ",\"system\":\"你是一位承德避暑山庄的地图导览助手，你只回答对于输入的承德避暑山庄内的地点名称的简介，回答字数限制在250字以内\",\"disable_search\":false,\"enable_citation\":false}";

        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions?access_token=" +
                        getAccessToken())
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody))
                .build();

        return executeRequestWithRetry(request, 0);
    }

    public String getAccessToken() throws IOException {
        String requestBody = "grant_type=client_credentials&client_id=" + API_KEY + "&client_secret=" + SECRET_KEY;

        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), requestBody))
                .build();

        return executeRequestWithRetry(request, 0);
    }

    private String buildMessagesJson() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < Dialogue_Content.size(); i++) {
            Map<String, String> message = Dialogue_Content.get(i);
            sb.append("{");
            int count = 0;
            for (Map.Entry<String, String> entry : message.entrySet()) {
                if (count > 0) {
                    sb.append(",");
                }
                sb.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
                count++;
            }
            sb.append("}");
            if (i < Dialogue_Content.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String executeRequestWithRetry(Request request, int retryCount) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (retryCount < MAX_RETRIES) {
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return executeRequestWithRetry(request, retryCount + 1);
                }
                throw new IOException("Unexpected code " + response);
            }
            String responseData = response.body().string();
            if (request.url().toString().contains("oauth/2.0/token")) {
                return parseAccessToken(responseData);
            } else {
                return parseResponse(responseData);
            }
        }
    }

    private String parseResponse(String response) {
        int startIndex = response.indexOf("\"result\":\"");
        if (startIndex != -1) {
            startIndex += "\"result\":\"".length();
            int endIndex = response.indexOf("\"", startIndex);
            if (endIndex != -1) {
                return response.substring(startIndex, endIndex);
            }
        }
        return "";
    }

    private String parseAccessToken(String response) {
        int startIndex = response.indexOf("\"access_token\":\"");
        if (startIndex != -1) {
            startIndex += "\"access_token\":\"".length();
            int endIndex = response.indexOf("\"", startIndex);
            if (endIndex != -1) {
                return response.substring(startIndex, endIndex);
            }
        }
        return "";
    }
}