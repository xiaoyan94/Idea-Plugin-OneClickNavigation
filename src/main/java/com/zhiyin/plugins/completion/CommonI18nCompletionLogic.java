package com.zhiyin.plugins.completion;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.lang.properties.IProperty;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("DuplicatedCode")
public class CommonI18nCompletionLogic {
    public static void addPropertiesToCompletionResult(@NotNull CompletionResultSet result, @NotNull String prefix, List<IProperty> properties) {
        // 添加代码补全建议项
        boolean needTransUnicode = !MyPropertiesUtil.isNative2AsciiForPropertiesFiles();
        properties.forEach(property -> {
            if (property == null || property.getKey() == null || property.getValue() == null){
                return;
            }

            String value = property.getValue();
            if(needTransUnicode){
                value = StringUtil.unicodeToString(value);
            }

            if (value == null || !value.contains(prefix)) {
                return;
            }

            if (value.contains("\\n\\r")) {
                System.out.println("value contains \\n");
            }

            value = value.replaceAll("\\n", "").replaceAll("\\r", "");

            if (value.contains("\\u")) {
                value = StringUtil.unicodeToString(value);
            }

            if (value != null) {
                result.addElement(new CommonI18nLookupElement(property.getKey(), value));
            }
        });
    }

    public static void addPropertiesToCompletionResultForXml(@NotNull CompletionResultSet result, @NotNull String prefix, List<IProperty> properties) {
        // 添加代码补全建议项
        boolean needTransUnicode = !MyPropertiesUtil.isNative2AsciiForPropertiesFiles();
        properties.forEach(property -> {
            if (property == null || property.getKey() == null || property.getValue() == null) {
                return;
            }

            String chsValue = property.getValue();
            if (needTransUnicode) {
                chsValue = StringUtil.unicodeToString(chsValue);
            }

            if (chsValue == null || !chsValue.contains(prefix)) {
                return;
            }

            if (chsValue.contains("\\n\\r")) {
                System.out.println("chsValue contains \\n");
            }

            chsValue = chsValue.replaceAll("\\n", "").replaceAll("\\r", "");

            if (chsValue.contains("\\u")) {
                chsValue = StringUtil.unicodeToString(chsValue);
            }

            String key = property.getKey();
            String eng = chsValue;
            if (key.contains(".")) {
                eng = key.substring(key.lastIndexOf(".") + 1);
                eng = StringUtil.capitalize(eng).replaceAll("_", " ");
            }
            if (chsValue != null) {
                result.addElement(new CommonI18nLookupElement(key, chsValue, eng));
            }
        });
    }

}
