package com.zhiyin.plugins.completion;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.properties.IProperty;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class CommonI18nCompletionLogic {
    public static void addPropertiesToCompletionResult(@NotNull CompletionResultSet result, @NotNull String prefix, List<IProperty> properties, Function<IProperty, InsertHandler<LookupElement>> getLookupElementInsertHandler) {
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


            result.addElement(LookupElementBuilder.create(Objects.requireNonNull(value))
                    .withLookupString(value)
                    .withIcon(MyIcons.pandaIconSVG16_2)
                    .withBoldness(true)
                    .withCaseSensitivity(false)
                    .withTailText(Objects.requireNonNull(property.getKey()), true)
                    .withInsertHandler(getLookupElementInsertHandler.apply(property))
                    .withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE)
            );
        });
    }

}
