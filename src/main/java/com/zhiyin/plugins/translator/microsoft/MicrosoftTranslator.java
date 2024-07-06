package com.zhiyin.plugins.translator.microsoft;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.intellij.openapi.components.Service;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service(Service.Level.APP)
public final class MicrosoftTranslator {

    private static String DEFAULT_RESOURCE_KEY = "<your-resource-key>";
    private static String DEFAULT_REGION = "eastasia";
    private static String DEFAULT_ENDPOINT = "https://api.cognitive.microsofttranslator.com";
    private static final String DEFAULT_URL = DEFAULT_ENDPOINT + "/translate?api-version=3.0&to=en";

    private final OkHttpClient client = new OkHttpClient();

    static {
        loadConfig();
    }

    private static void loadConfig() {
        Properties properties = new Properties();
        try (InputStream input = MicrosoftTranslator.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Unable to find config.properties");
                return;
            }
            properties.load(input);
            DEFAULT_RESOURCE_KEY = properties.getProperty("translator.microsoft.resourceKey");
            DEFAULT_REGION = properties.getProperty("translator.microsoft.region");
            DEFAULT_ENDPOINT = properties.getProperty("translator.microsoft.endpoint");
//            DEFAULT_URL = properties.getProperty("translator.microsoft.url");
            System.out.println("Loaded config.properties successfully");
        } catch (IOException e) {
            System.err.println("Error loading config.properties: " + e.getMessage());
        }
    }

    public MicrosoftTranslator() {}

    /**
     * 使用内置接口密钥
     */
    public Map<String, String> translateToMultiLang(String text) {
        return translateToMultiLang(text, null, DEFAULT_ENDPOINT, DEFAULT_RESOURCE_KEY, DEFAULT_REGION);
    }

    /**
     * 使用自定义接口密钥
     * @param text 原文
     * @return 翻译结果 [en, zh-Hant, zh-CHT]
     */
    public Map<String, String> translateToMultiLang(String text, String to, String endpoint, String resourceKey, String region) {
        Map<String, String> translationsMap = new HashMap<>();
        // 解析JSON字符串为JSONObject
        JSONArray jsonArray = null;
        String responseJsonStr = "";
        try {
            responseJsonStr = translate(text,  to == null || to.isEmpty() ? "en,zh-Hant,vi" : to, endpoint, resourceKey, region);
            jsonArray = JSONArray.parseArray(responseJsonStr);
        } catch (IOException | JSONException e) {
            System.out.println(responseJsonStr);
            try {
                JSONObject jsonObject = JSON.parseObject(responseJsonStr);
                String error = jsonObject.getString("error");
                translationsMap.put("error", error);
            } catch (Exception ex) {
                translationsMap.put("error", ex.getMessage());
            }
        }
        if (jsonArray == null || jsonArray.isEmpty()) {
            return translationsMap;
        }

        JSONArray translationsArray;
        translationsArray = jsonArray.getJSONObject(0).getJSONArray("translations");
        if (translationsArray == null || translationsArray.isEmpty()) {
            translationsMap.put("error", "翻译失败");
        }
        // 遍历translations数组，填充Map
        if (translationsArray != null) {
            for (int i = 0; i < translationsArray.size(); i++) {
                JSONObject translationObject = translationsArray.getJSONObject(i);
                to = translationObject.getString("to");
                String value = translationObject.getString("text");
                translationsMap.put(to, value);
            }
            translationsMap.put("zh-CHT", translationsMap.get("zh-Hant"));
        }
        return translationsMap;
    }

    /**
     * 翻译
     * @param text 原文内容，语言自动检测
     * @param to 翻译目标语言缩写，多种语言逗号隔开 <a href="https://api.cognitive.microsofttranslator.com/languages?api-version=3.0">语言缩写列表接口</a>
     * @return 翻译结果
     * @throws IOException e
     */
    @SuppressWarnings({"UastIncorrectHttpHeaderInspection", "SpellCheckingInspection"})
    public String translate(String text, String to, String apiEndpoint, String apiSecretMicrosoft, String apiRegionMicrosoft) throws IOException {
        if (to == null) {
            to = "en";
        }
        Map<String, String> content = new HashMap<>();
        content.put("Text", text);
        List<Map<String, String>> contentList = Collections.singletonList(content);
        String contentJsonString = JSON.toJSONString(contentList);

        String region;
        String resourceKey;
        String url = DEFAULT_URL.replaceFirst("to=en", "to=" + to);
        if (apiSecretMicrosoft != null && !apiSecretMicrosoft.isEmpty()){
            if(apiEndpoint != null && !apiEndpoint.isEmpty()){
                apiEndpoint = apiEndpoint.endsWith("/") ? apiEndpoint : apiEndpoint + "/";
                url  = apiEndpoint + "translate?api-version=3.0&to=en";
                url = url.replaceFirst("to=en", "to=" + to);
            }
            resourceKey = apiSecretMicrosoft;
            region = apiRegionMicrosoft == null || apiRegionMicrosoft.isEmpty() ? DEFAULT_REGION : apiRegionMicrosoft;
        } else {
            url = DEFAULT_URL.replaceFirst("to=en", "to=" + to);
            resourceKey = DEFAULT_RESOURCE_KEY;
            region = DEFAULT_REGION;
        }
        url = url + "&from=zh-Hans"; // 默认源语言中文简体，否则微软翻译会出现将待翻译内容“顺序”的源语言错误识别成日语的情况
        RequestBody body = RequestBody.create(contentJsonString, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Ocp-Apim-Subscription-Key", resourceKey)
                .addHeader("Ocp-Apim-Subscription-Region", region)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                return response.body().string();
            } else {
                throw new IOException("Response body is null.");
            }
        }
    }

    public static void main(String[] args) {
        MicrosoftTranslator translator = new MicrosoftTranslator();
        Map<String, String> response = translator.translateToMultiLang("顺序");
        System.out.println(response);
    }

}