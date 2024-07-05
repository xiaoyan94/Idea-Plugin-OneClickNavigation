package com.zhiyin.plugins.translator.youdao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.components.Service;
import com.zhiyin.plugins.notification.MyPluginMessages;
import com.zhiyin.plugins.translator.TranslateException;
import com.zhiyin.plugins.translator.youdao.utils.AuthV3Util;
import com.zhiyin.plugins.translator.youdao.utils.HttpUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 网易有道智云翻译服务api调用demo
 * api接口: <a href="https://openapi.youdao.com/api">URL</a>
 */
@Service(Service.Level.APP)
public final class YouDaoTranslate {

    /**
     * 提供默认的应用ID和应用密钥
     */
    public static String APP_KEY = "<your-app-key>";     // 您的应用ID
    public static String APP_SECRET = "<your-app-secret>";  // 您的应用密钥
    public static String URL = "https://openapi.youdao.com/api";

    public static final String LANG_ZH_CHS = "zh-CHS";
    public static final String LANG_ZH_CHT = "zh-CHT";
    public static final String LANG_EN = "en";
    public static final String LANG_VI = "vi"; // 越南语

    static {
        loadConfig();
    }

    private static void loadConfig() {
        Properties properties = new Properties();
        try (InputStream input = YouDaoTranslate.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Unable to find config.properties");
                return;
            }
            properties.load(input);
            APP_KEY = properties.getProperty("translator.youDao.appKey");
            APP_SECRET = properties.getProperty("translator.youDao.appSecret");
            URL = properties.getProperty("translator.youDao.url");
            System.out.println("Loaded config.properties successfully");
        } catch (IOException e) {
            System.err.println("Error loading config.properties: " + e.getMessage());
        }
    }

    public String translateToCHS(String text) throws TranslateException {
        return translate(text, LANG_EN, LANG_ZH_CHS);
    }

    public String translateToEN(String text) throws TranslateException {
        return translate(text, LANG_ZH_CHS, "en");
    }

    public String translateToCHT(String text) throws TranslateException {
        return translate(text, LANG_ZH_CHS, LANG_ZH_CHT);
    }

    /**
     * 使用默认的应用ID和应用密钥
     */
    public String translate(String text, String from, String to) throws TranslateException {
        return translate(text, from, to, URL, APP_KEY, APP_SECRET, null);
    }

    /**
     * 传入应用ID和应用密钥
     * @param text 待翻译文本
     * @param from 翻译源语言
     * @param to 翻译目标语言
     * @param apiUrlYouDao 接口地址
     * @param apiKeyYouDao 接口ID
     * @param apiSecretYouDao 接口密钥
     * @param apiVocabIdYouDao 翻译词汇表ID
     * @return 翻译结果
     */
    public String translate(String text, String from, String to, String apiUrlYouDao, String apiKeyYouDao, String apiSecretYouDao, String apiVocabIdYouDao) throws TranslateException {
        /*
         * note: 将下列变量替换为需要请求的参数
         * 取值参考文档: https://ai.youdao.com/DOCSIRMA/html/%E8%87%AA%E7%84%B6%E8%AF%AD%E8%A8%80%E7%BF%BB%E8%AF%91/API%E6%96%87%E6%A1%A3/%E6%96%87%E6%9C%AC%E7%BF%BB%E8%AF%91%E6%9C%8D%E5%8A%A1/%E6%96%87%E6%9C%AC%E7%BF%BB%E8%AF%91%E6%9C%8D%E5%8A%A1-API%E6%96%87%E6%A1%A3.html
         */
        Map<String, String[]> params = new HashMap<>();
        params.put("q", new String[]{text});
        params.put("from", new String[]{from});
        params.put("to", new String[]{to});
        if (apiVocabIdYouDao != null){
            params.put("vocabId", new String[]{apiVocabIdYouDao});
        }

        try {
            AuthV3Util.addAuthParams(apiKeyYouDao, apiSecretYouDao, params);
        } catch (NoSuchAlgorithmException e) {
            return e.getMessage();
        }
        byte[] result = HttpUtil.doPost(apiUrlYouDao, null, params, "application/json");
        if (result == null) {
            return "";
        }
        String responseJson = new String(result, StandardCharsets.UTF_8);
        return getTranslationFromYouDaoApi(responseJson);
    }

    /**
     * 有道API响应获取译文
     */
    private String getTranslationFromYouDaoApi(String responseJson) throws TranslateException {
        JSONArray translation = null;
        String errMsg = null;
        try {
            JSONObject jsonObject = JSON.parseObject(responseJson);
            if (jsonObject.getIntValue("errorCode") != 0) {
                errMsg = "接口返回错误码：" + jsonObject.getString("errorCode");
            }
            translation = jsonObject.getJSONArray("translation");
        } catch (Exception e) {
            errMsg = "接口响应异常：" + responseJson;
        }
        if (translation == null || translation.isEmpty()) {
            if (errMsg != null) {
                System.out.println(errMsg);
//                MyPluginMessages.showError("有道翻译API", errMsg, null);
            }
            throw new TranslateException(errMsg);
        }
        return translation.getString(0);
    }

    public static void main(String[] args) throws TranslateException {
        YouDaoTranslate translator = new YouDaoTranslate();
        System.out.println(translator.translateToCHT("你好，世界"));
        System.out.println(translator.translateToEN("你好，世界"));
        System.out.println(translator.translate("你好，世界", LANG_ZH_CHS, LANG_VI));
        System.out.println(translator.translateToCHS("hello, world"));
        System.exit(0);
    }

}
