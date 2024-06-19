package com.zhiyin.plugins.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.zhiyin.plugins.translator.microsoft.MicrosoftTranslator;
import com.zhiyin.plugins.translator.youdao.YouDaoTranslate;
import com.zhiyin.plugins.settings.TranslateSettingsComponent;
import com.zhiyin.plugins.settings.TranslateSettingsState;

import java.util.Map;

public class TranslateUtil {

    private static final LRUCache<String, String> TRANSLATION_CACHE_EN = new LRUCache<>(128);
    private static final LRUCache<String, String> TRANSLATION_CACHE_CHT = new LRUCache<>(128);

    /**
     * 清空缓存的翻译结果
     */
    public static void clearTranslationCache() {
        TRANSLATION_CACHE_EN.clear();
        TRANSLATION_CACHE_CHT.clear();
    }

    /**
     * 中文转英文
     */
    public static String translateToEN(String text) {
        if(TRANSLATION_CACHE_EN.containsKey(text)){
            return TRANSLATION_CACHE_EN.get(text);
        } else{
            String result = translate(text, YouDaoTranslate.LANG_ZH_CHS, YouDaoTranslate.LANG_EN);
            TRANSLATION_CACHE_EN.put(text, result);
            return result;
        }
    }

    /**
     * 中文转繁体
     */
    public static String translateToCHT(String text)
    {
        if(TRANSLATION_CACHE_CHT.containsKey(text)){
            return TRANSLATION_CACHE_CHT.get(text);
        } else{
            String result = translate(text, YouDaoTranslate.LANG_ZH_CHS, YouDaoTranslate.LANG_ZH_CHT);
            TRANSLATION_CACHE_CHT.put(text, result);
            return result;
        }
    }

    public static String translate(String text, String from, String to){
        TranslateSettingsState translateSettingsState = TranslateSettingsComponent.Companion.getInstance().getState();
        String apiProvider = translateSettingsState.getApiProvider();
        switch (apiProvider){
            case "YouDao":
                return doYouDaoTranslate(text, from, to, translateSettingsState);
            case "Microsoft":
                return doMicrosoftTranslate(text, to, translateSettingsState);
            default:
                return "";
        }

    }

    private static String doYouDaoTranslate(String text, String from, String to, TranslateSettingsState translateSettingsState) {
        YouDaoTranslate youDaoTranslate = ApplicationManager.getApplication().getService(YouDaoTranslate.class);
        String apiUrlYouDao = translateSettingsState.getApiUrlYouDao();
        String apiKeyYouDao = translateSettingsState.getApiKeyYouDao();
        String apiSecretYouDao = translateSettingsState.getApiSecretYouDao();
        String apiVocabIdYouDao = translateSettingsState.getApiVocabIdYouDao();
        // 设置为空时使用内置的密钥
        if(translateSettingsState.getApiSecretYouDao().isEmpty()){
            return youDaoTranslate.translate(text, from, to);
        }
        return youDaoTranslate.translate(text, from, to, apiUrlYouDao, apiKeyYouDao, apiSecretYouDao, apiVocabIdYouDao);
    }

    private static String doMicrosoftTranslate(String text, String to, TranslateSettingsState translateSettingsState) {
        MicrosoftTranslator microsoftTranslator = ApplicationManager.getApplication().getService(MicrosoftTranslator.class);
        Map<String, String> targetTranslations;
        if(translateSettingsState.getApiSecretMicrosoft().isEmpty()){
            targetTranslations = microsoftTranslator.translateToMultiLang(text);
        } else{
            targetTranslations = microsoftTranslator.translateToMultiLang(text, null, translateSettingsState.getApiUrlMicrosoft(), translateSettingsState.getApiSecretMicrosoft(), translateSettingsState.getApiRegionMicrosoft());
        }
        TRANSLATION_CACHE_EN.put(text, targetTranslations.get("en"));
        TRANSLATION_CACHE_CHT.put(text, targetTranslations.get("zh-Hant"));
        return targetTranslations.get(to);
    }
}
