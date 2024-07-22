package com.zhiyin.plugins.service;

import com.intellij.openapi.components.Service;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

import java.util.TimeZone;

@Service(Service.Level.APP)
public final class FreeMarkerConfiguration {

    // 静态变量来保存 Configuration 实例
    private static volatile Configuration configuration;

    // 提供公共的静态方法来获取 Configuration 实例
    public static Configuration getConfiguration() {
        if (configuration == null) {
            synchronized (FreeMarkerConfiguration.class) {
                if (configuration == null) {
                    configuration = createConfiguration();
                }
            }
        }
        return configuration;
    }

    // 创建 Configuration 实例的方法
    private static Configuration createConfiguration() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_33);

        // Use the class loader to get the resource directory
        cfg.setClassForTemplateLoading(FreeMarkerConfiguration.class, "/templates");

        // Set the preferred charset template files are stored in
        cfg.setDefaultEncoding("UTF-8");

        // Set how errors will appear
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        // Don't log exceptions inside FreeMarker
        cfg.setLogTemplateExceptions(false);

        // Wrap unchecked exceptions thrown during template processing into TemplateException-s
        cfg.setWrapUncheckedExceptions(true);

        // Do not fall back to higher scopes when reading a null loop variable
        cfg.setFallbackOnNullLoopVariable(false);

        // To accommodate to how JDBC returns values
        cfg.setSQLDateAndTimeTimeZone(TimeZone.getDefault());

        return cfg;
    }
}
