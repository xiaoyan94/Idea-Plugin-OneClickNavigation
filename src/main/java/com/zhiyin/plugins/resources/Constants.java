package com.zhiyin.plugins.resources;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 
 */
public class Constants {
    public static final String NOTIFICATION_GROUP_ID = "OneClickNavigation";

    private Constants() {}

    public static final Pattern HTML_MESSAGE_PATTERN = Pattern.compile("<@message\\s+key\\s*=\\s*['\"](.*?)['\"]\\s*/>");
    public static final Pattern JS_I18N_PATTERN = Pattern.compile("zhiyin\\.i18n\\.translate\\(\\s*['\"](.*?)['\"]\\s*\\)");
    public static final Pattern XML_TITLE_PATTERN = Pattern.compile("['\"](com\\.zhiyin\\..*?)['\"]");
    public static final Pattern XML_COLUMN_I18N_PATTERN = Pattern.compile("['\"](com\\.zhiyin\\..*?)['\"]");
    public static final Pattern XML_FIELD_LABEL_PATTERN = Pattern.compile("['\"](com\\.zhiyin\\..*?)['\"]");
    public static final Pattern PATTERN_I18N_JAVA = Pattern.compile("(?:I18nUtil[.\\s]+(?:getMessage|getMessageByFactory)|getSysI18nResource)\\(.*?\\s*,\\s*\"(com\\..*?)\"\\s*\\)");
    public static final Pattern PATTERN_I18N_JAVA_MESSAGE = Pattern.compile("message\\s*=\\s*\"(.*?)\"");
    public static final Pattern PATTERN_I18N_JSP_TAG = Pattern.compile("<mes:\\s*message\\s*key\\s*=\\s*\"(.*?)\"\\s*/>");

    // 根据文件扩展名返回对应的 Pattern 数组
    public static final Map<String, Pattern[]> FILETYPE_PATTERNS = Map.of(
            "html", new Pattern[]{HTML_MESSAGE_PATTERN, JS_I18N_PATTERN, PATTERN_I18N_JSP_TAG},
            "htm", new Pattern[]{HTML_MESSAGE_PATTERN, JS_I18N_PATTERN, PATTERN_I18N_JSP_TAG},
            "jsp", new Pattern[]{HTML_MESSAGE_PATTERN, JS_I18N_PATTERN, PATTERN_I18N_JSP_TAG},
            "ftl", new Pattern[]{HTML_MESSAGE_PATTERN, JS_I18N_PATTERN, PATTERN_I18N_JSP_TAG},
            "xml", new Pattern[]{XML_TITLE_PATTERN},
            "js", new Pattern[]{JS_I18N_PATTERN, HTML_MESSAGE_PATTERN},
            "java", new Pattern[]{PATTERN_I18N_JAVA, PATTERN_I18N_JAVA_MESSAGE}
    );

    // 获取文件类型的 Pattern
    public static Pattern[] getPatternsByExtension(String extension) {
        return FILETYPE_PATTERNS.getOrDefault(extension, new Pattern[0]);
    }

    public static final String SCOPE_NAME_PROJECT = "Project";
    public static final String SCOPE_NAME_MODULE = "Module";

    public static final String I18N_METHOD_EXPRESSION = "I18nUtil.getMessage";
    public static final String I18N_METHOD_EXPRESSION_GET_SYS_I18N_RESOURCE = "getSysI18nResource";
    public static final String I18N_METHOD_EXPRESSION_GET_MESSAGE_BY_FACTORY = "I18nUtil.getMessageByFactory";
    public static final String QUERY_DAO_METHOD_EXPRESSION = "queryDaoDataT";

    public static final List<String> BIZ_COMMON_SERVICE_METHODS = Arrays.asList(
            "bizCommonService.queryMocDaoData",
            "bizCommonService.queryMocDaoRawData",
            "bizCommonService.insertMocData",
            "bizCommonService.insertMocDataAndExtend",
            "bizCommonService.insertMocExtendData",
            "bizCommonService.updateMocData",
            "bizCommonService.updateMocDataAndExtend",
            "bizCommonService.findMocById",
            "bizCommonService.findMocExtendById",
            "bizCommonService.findMocDataById",
            "bizCommonService.findMocDataAndExtendById",
            "bizCommonService.deleteSoftMocData",
            "bizCommonService.deleteMocData",
            "bizCommonService.deleteMocDataAndExtend",
            "bizCommonService.queryMocDaoRawDataAndExtend",
            "bizCommonService.queryMocDaoDataAndExtend"
    );

    public static final String FOLDING_GROUP = "i18n_key";
    public static final String I18N_KEY_PREFIX = "com.zhiyin.";
    public static final String I18N_WEB_ZH_CN = "web_zh_CN.properties";
    public static final String I18N_WEB_ZH_TW = "web_zh_TW.properties";
    public static final String I18N_WEB_EN_US = "web_en_US.properties";
    public static final String I18N_WEB_VI_VN = "web_vi_VN.properties";
    public static final String I18N_ZH_CN_SUFFIX = "_zh_CN.properties";
    public static final String I18N_ZH_TW_SUFFIX = "_zh_TW.properties";
    public static final String I18N_EN_US_SUFFIX = "_en_US.properties";
    public static final String I18N_VI_VN_SUFFIX = "_vi_VN.properties";
    public static final String I18N_DATAGRID_ZH_CN_SUFFIX = ".datagrid_zh_CN.properties";
    public static final String I18N_DATAGRID_ZH_TW_SUFFIX = ".datagrid_zh_TW.properties";
    public static final String I18N_DATAGRID_EN_US_SUFFIX = ".datagrid_en_US.properties";
    public static final String I18N_DATAGRID_VI_VN_SUFFIX = ".datagrid_vi_VN.properties";

//    public static final String NO_SUCH_METHOD_IN_MAPPER = "Mapper XML中没有该方法";
    public static final String INVALID_I18N_KEY = "无效的i18n key";

//    public static final String MYBATIS_DTD_DEFAULT = "http://mybatis.org/dtd/mybatis-3-mapper.dtd";
    public static final String MYBATIS_DTD_CLASSPATH = "classpath://mybatis-3-mapper.dtd";
//    public static final String[] MYBATIS_POSSIBLE_NAMESPACES = new String[]{MYBATIS_DTD_CLASSPATH, MYBATIS_DTD_DEFAULT};

    public static final String NAVIGATE_TO_MAPPER = "跳转到 Mapper XML";
    public static final String NAVIGATE_TO_MOC = "跳转到 Moc XML";
    public static final String NAVIGATE_TO_DAO_INTERFACE = "跳转到 Dao 接口";
}
