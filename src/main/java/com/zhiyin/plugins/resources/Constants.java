package com.zhiyin.plugins.resources;

import org.jetbrains.annotations.NonNls;

/**
 * @author yan on 2024/2/28 00:10
 */
public class Constants {
    private Constants() {

    }

    public static final String FOLDING_GROUP = "i18n_key";
    public static final String I18N_KEY_PREFIX = "com.zhiyin.";
    public static final String I18N_WEB_ZH_CN = "web_zh_CN.properties";
    public static final String I18N_WEB_ZH_TW = "web_zh_TW.properties";
    public static final String I18N_WEB_EN_US = "web_en_US.properties";
    public static final String I18N_ZH_CN_SUFFIX = "_zh_CN.properties";
    public static final String I18N_ZH_TW_SUFFIX = "_zh_TW.properties";
    public static final String I18N_EN_US_SUFFIX = "_en_US.properties";
    public static final String INVALID_I18N_KEY = "无效的i18n key";

    public static final String MYBATIS_DTD_DEFAULT = "http://mybatis.org/dtd/mybatis-3-mapper.dtd";
    public static final String MYBATIS_DTD_CLASSPATH = "classpath://mybatis-3-mapper.dtd";
    public static final String[] MYBATIS_POSSIBLE_NAMESPACES = new String[]{MYBATIS_DTD_CLASSPATH,
            MYBATIS_DTD_DEFAULT};

    @NonNls
    public static final String MYBATIS_NAMESPACE_KEY = "mybatis namespace";


}
