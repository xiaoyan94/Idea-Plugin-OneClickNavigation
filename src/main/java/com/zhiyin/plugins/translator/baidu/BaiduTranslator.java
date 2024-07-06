package com.zhiyin.plugins.translator.baidu;

import com.alibaba.fastjson2.JSONObject;
import com.intellij.openapi.components.Service;
import com.zhiyin.plugins.translator.TranslateException;
import com.zhiyin.plugins.translator.microsoft.MicrosoftTranslator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Service(Service.Level.APP)
public final class BaiduTranslator {

    private static String API_APP_ID;
    private static String API_SECURITY_KEY;

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
            API_APP_ID = properties.getProperty("translator.baidu.appId");
            API_SECURITY_KEY = properties.getProperty("translator.baidu.appSecret");
            System.out.println("Loaded config.properties successfully");
        } catch (IOException e) {
            System.err.println("Error loading config.properties: " + e.getMessage());
        }
    }

    public String translate(String text, String from, String to) throws TranslateException {
        return translate(text, from, to, API_APP_ID, API_SECURITY_KEY);
    }

    public String translate(String text, String from, String to, String apiAppId, String apiSecretKey) throws TranslateException {
        TransApi api = new TransApi(apiAppId, apiSecretKey);
        String transResult = api.getTransResult(text, from, to);
        try {
            return retrieveTranslation(transResult);
        } catch (Exception e) {
            if (e instanceof TranslateException) {
                throw (TranslateException) e;
            } else {
                throw new TranslateException("Error retrieving translation from Baidu API:" + e.getMessage());
            }
        }
    }

    public static String retrieveTranslation(String transResult) throws TranslateException {
        JSONObject object = JSONObject.parseObject(transResult);
        if (object.getJSONArray("trans_result") != null) {
            return object.getJSONArray("trans_result").getJSONObject(0).getString("dst");
        } else {
            throw new TranslateException("Response:" + object.toJSONString());
        }
    }

    public static void main(String[] args) {
        String query = "顺序";
        try {
            // en, cht, vie
            BaiduTranslator baiduTranslator = new BaiduTranslator();
            System.out.println(baiduTranslator.translate(query, "auto", "en"));
        } catch (TranslateException e) {
            System.err.println(e.getMessage());
        }
    }

}
