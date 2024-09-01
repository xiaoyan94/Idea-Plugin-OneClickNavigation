package com.zhiyin.plugins.settings

/**
 * 存储翻译设置项的状态
 */
data class TranslateSettingsState (

    var apiProvider: String = "YouDao",
    var apiUrlYouDao: String = "https://openapi.youdao.com/api",
    var apiKeyYouDao: String = "",
    var apiSecretYouDao: String = "",
    /**
     * 自定义术语表ID
     */
    var apiVocabIdYouDao: String = "",


    var apiUrlBaidu: String = "https://fanyi-api.baidu.com/api/trans/vip/translate",
    var apiAppIdBaidu: String = "",
    var apiAppSecretBaidu: String = "",

    var apiUrlMicrosoft: String = "https://api.cognitive.microsofttranslator.com/translate",
    var apiSecretMicrosoft: String = "",
    var apiRegionMicrosoft: String = "eastasia",


    /**
     * 翻译资源串弹窗设置：当资源串中包含翻译原文时，是否直接执行替换
     */
    var doOKActionWhenI18nKeyExists: Boolean = false,

    var enableFeignToRestController: Boolean = false,
    var enableHtmlUrlToController: Boolean = false,
)