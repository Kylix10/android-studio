package com.example.summer.utils;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionCreateParams;
import com.openai.models.ChatCompletionSystemMessageParam;
import com.openai.models.ChatCompletionUserMessageParam;
import com.openai.models.ChatCompletionAssistantMessageParam;
import com.openai.models.ChatCompletionMessageParam;

import java.util.ArrayList;
import java.util.List;

public class WenXin {

    // 请将此处替换为你在千帆控制台获取的真实 API Key
    private static final String API_KEY = "your_APIKey";
    private final OpenAIClient client;
    private final List<ChatCompletionMessageParam> dialogueContent; // 存储对话历史

    public WenXin() {
        this.client = OpenAIOkHttpClient.builder()
                .apiKey(API_KEY)
                .baseUrl("https://qianfan.baidubce.com/v2/")
                .build();
        this.dialogueContent = new ArrayList<>();

        // 动态抓取当前景区名称生成系统提示词，实现 AI 服务解耦
        String activeName = com.example.summer.utils.LocationStateManager.getInstance().getCurrentLocation().getName();

        // 添加系统提示词
        dialogueContent.add(ChatCompletionMessageParam.ofSystem(ChatCompletionSystemMessageParam.builder()
                .content("你是一位" + activeName + "的地图导览助手，你只回答对于输入的" + activeName + "内的地点名称的简介，回答字数限制在250字以内")
                .build()));
    }

    public String getLocationIntroduction(String location) {
        try {
            // 动态抓取当前景区名称拼装用户提问
            String activeName = com.example.summer.utils.LocationStateManager.getInstance().getCurrentLocation().getName();

            // 添加用户输入
            dialogueContent.add(ChatCompletionMessageParam.ofUser(ChatCompletionUserMessageParam.builder()
                    .content("请介绍一下" + activeName + "的 " + location)
                    .build()));

            // 构建请求参数
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .messages(dialogueContent)
                    .model("deepseek-r1-distill-qianfan-70b") // 根据需求选择模型
                    .build();

            // 发起请求
            ChatCompletion chatCompletion = client.chat().completions().create(params);
            String responseContent = chatCompletion.choices().get(0).message().content().orElse("");

            // 可选：将AI的回复也添加到历史记录中，以保持上下文
            dialogueContent.add(ChatCompletionMessageParam.ofAssistant(ChatCompletionAssistantMessageParam.builder()
                    .content(responseContent)
                    .build()));

            return responseContent;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}