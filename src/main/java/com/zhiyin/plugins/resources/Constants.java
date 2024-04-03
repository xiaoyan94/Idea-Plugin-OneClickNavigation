package com.zhiyin.plugins.resources;

/**
 * @author yan on 2024/2/28 00:10
 */
public class Constants {
    private Constants() {}

    public static final String SCOPE_NAME_PROJECT = "Project";
    public static final String SCOPE_NAME_MODULE = "Module";

    public static final String I18N_METHOD_EXPRESSION = "I18nUtil.getMessage";
    public static final String QUERY_DAO_METHOD_EXPRESSION = "queryDaoDataT";

    public static final String FOLDING_GROUP = "i18n_key";
    public static final String I18N_KEY_PREFIX = "com.zhiyin.";
    public static final String I18N_WEB_ZH_CN = "web_zh_CN.properties";
    public static final String I18N_WEB_ZH_TW = "web_zh_TW.properties";
    public static final String I18N_WEB_EN_US = "web_en_US.properties";
    public static final String I18N_ZH_CN_SUFFIX = "_zh_CN.properties";
    public static final String I18N_ZH_TW_SUFFIX = "_zh_TW.properties";
    public static final String I18N_EN_US_SUFFIX = "_en_US.properties";
    public static final String I18N_DATAGRID_ZH_CN_SUFFIX = ".datagrid_zh_CN.properties";
    public static final String I18N_DATAGRID_ZH_TW_SUFFIX = ".datagrid_zh_TW.properties";
    public static final String I18N_DATAGRID_EN_US_SUFFIX = ".datagrid_en_US.properties";

//    public static final String NO_SUCH_METHOD_IN_MAPPER = "Mapper XML中没有该方法";
    public static final String INVALID_I18N_KEY = "无效的i18n key";

//    public static final String MYBATIS_DTD_DEFAULT = "http://mybatis.org/dtd/mybatis-3-mapper.dtd";
    public static final String MYBATIS_DTD_CLASSPATH = "classpath://mybatis-3-mapper.dtd";
//    public static final String[] MYBATIS_POSSIBLE_NAMESPACES = new String[]{MYBATIS_DTD_CLASSPATH, MYBATIS_DTD_DEFAULT};

    public static final String NAVIGATE_TO_MAPPER = "跳转到 Mapper XML";
    public static final String NAVIGATE_TO_DAO_INTERFACE = "跳转到 Dao 接口";
}
