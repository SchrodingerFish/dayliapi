package com.cn.dayliapi.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.cn.dayliapi.model.Data;
import com.cn.dayliapi.model.Response;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Getter
public class DayliUtil {

    private final Gson gson = new Gson();

    private final String sendApiUrl;
    private final String sendApiKey;

    private final String newsApiUrl;
    private final String newsApiKey;

    public DayliUtil(@Value("${send.api.url}") String sendApiUrl,
                     @Value("${news.api.url}") String newsApiUrl,
                     @Value("${send.api.key}") String sendApiKey,
                     @Value("${news.api.key}") String newsApiKey) {
        this.sendApiUrl = sendApiUrl;
        this.newsApiUrl = newsApiUrl;
        this.sendApiKey = sendApiKey;
        this.newsApiKey = newsApiKey;
    }

    /**
     * 根据JSON字符串生成Markdown格式的新闻摘要。
     *
     * @param jsonString JSON字符串
     * @return Markdown格式的新闻摘要
     */
    public String convertNewsToMarkdown(String jsonString) {
        try {
            Response response = gson.fromJson(jsonString, Response.class);
            if (response == null || response.getData() == null) {
                return "无法解析JSON或JSON数据为空。";
            }
            Data data = response.getData();
            StringBuilder markdown = new StringBuilder();
            markdown.append("## ").append(data.getDate()).append("\n");
            markdown.append(data.getWeiyu()).append("\n\n");
            if (data.getNews() != null) {
                for (String newsItem : data.getNews()) {
                    markdown.append(newsItem).append("\n");
                }
                markdown.append("\n\n");
            }
            markdown.append("![新闻图片](").append(data.getImage()).append(")\n");
            markdown.append("## 音频简报").append("\n");
            markdown.append("[点击收听](").append(data.getAudio()).append(")").append("\n");
            return markdown.toString();
        } catch (JsonSyntaxException e) {
            log.error("Failed to parse JSON string: {}", jsonString, e);
            return "JSON 解析错误。"; // Or a more specific error message
        } catch (Exception e) {
            log.error("An unexpected error occurred while converting JSON to Markdown: {}", e.getMessage(), e);
            return "处理新闻时发生未知错误。";
        }
    }

    /**
     * 获取指定新闻的JSON字符串。
     *
     * @param url 新闻API的URL
     * @return JSON字符串，或null如果请求失败
     */
    public String getJsonStringFromUrl(String url) {
        try {
            HttpRequest request = HttpUtil.createGet(url)
                    .header("Content-Type", "application/json");
            HttpResponse response = request.execute();
            if (response.isOk()) {
                String body = response.body();
                if (StrUtil.isNotEmpty(body)) {
                    log.debug("Received JSON body from URL {}: {}", url, body); // Use debug level
                    return body;
                } else {
                    log.warn("Response body is empty for URL: {}", url);
                    return null;
                }
            } else {
                log.error("Request failed for URL: {}. HTTP status code: {}, Body: {}", url, response.getStatus(), response.body());
                return null;
            }
        } catch (Exception e) {
            log.error("Exception during network request for URL: {}: {}", url, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 发送消息到指定的API。
     *
     * @param apiKey    API密钥。
     * @param title     消息标题。
     * @param content   消息内容。
     * @param channel   消息渠道。
     * @param topicCode 消息主题代码。
     */
    public void sendMessage(String apiKey, String title, String content, String channel, String topicCode) {
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("apikey", apiKey);
            payload.addProperty("title", title);
            payload.addProperty("content", content);
            payload.addProperty("channel", channel);
            payload.addProperty("topicCode", topicCode);
            HttpRequest request = HttpRequest.post(sendApiUrl)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(payload));
            HttpResponse response = request.execute();
            if (response.isOk()) {
                String responseBody = response.body();
                log.info("消息发送成功. 响应主体: {}", responseBody);
            } else {
                log.error("消息发送失败。HTTP 状态码: {}, 响应主体: {}", response.getStatus(), response.body());
            }
        } catch (Exception e) {
            log.error("消息发送异常: {}", e.getMessage(), e);
        }

    }

    /**
     * 获取新闻内容并转换为Markdown格式。
     * @return Markdown格式的新闻内容，或null如果获取或转换失败
     */
    private String getNewsContent() {
        String jsonContent = getJsonStringFromUrl(newsApiUrl + "?token=" + newsApiKey + "&format=json");
        if (StrUtil.isEmpty(jsonContent)) {
            log.error("无法检索 JSON 新闻数据。");
            return null;
        }
        try {
            String markdownContent = convertNewsToMarkdown(jsonContent);
            log.info("成功检索并转换新闻数据。新闻内容总长度: {}", markdownContent.length());
            return markdownContent;
        } catch (JsonSyntaxException e) {
            log.error("无法解析 JSON: {}", jsonContent, e);
            return null;
        } catch (Exception e) {
            log.error("新闻内容处理过程中发生意外错误: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 发送每日新闻。
     */
    public void sendDayliNews() {
        String title = "每日新闻(彪哥推荐)";
        String content = getNewsContent();
        if (StrUtil.isEmpty(content)) {
            log.error("无法检索新闻数据。内容为空。");
            return;
        }
        String channel = "wechat";
        String topicCode = "hotnews";
        sendMessage(sendApiKey, title, content, channel, topicCode);
    }
}
