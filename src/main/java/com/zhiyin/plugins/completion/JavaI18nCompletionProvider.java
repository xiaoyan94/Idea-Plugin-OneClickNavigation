package com.zhiyin.plugins.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.properties.IProperty;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.util.ProcessingContext;
import com.zhiyin.plugins.listeners.MyFileEditorManagerListener;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;
import com.zhiyin.plugins.utils.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class JavaI18nCompletionProvider extends BaseCompletionProvider {
    @Override
    protected void performCompletion(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result, @NotNull String prefix) {
        PsiFile originalFile = parameters.getOriginalFile();
        Module module = MyPsiUtil.getModuleByPsiElement(originalFile);
        List<IProperty> properties = MyPropertiesUtil.getModuleI18nPropertiesCN(originalFile.getProject(), module);
        addPropertiesToCompletionResult(result, prefix, properties);
    }

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

            if (value != null && !value.contains(prefix)) {
                return;
            }

            result.addElement(LookupElementBuilder.create(Objects.requireNonNull(value))
                    .withLookupString(value)
                    .withIcon(MyIcons.pandaIconSVG16_2)
                    .withBoldness(true)
                    .withCaseSensitivity(false)
                    .withTailText(Objects.requireNonNull(property.getKey()), true)
                    .withInsertHandler((insertionContext, item) -> {
                        insertionContext.getDocument().replaceString(insertionContext.getStartOffset(), insertionContext.getTailOffset(), Objects.requireNonNull(property.getKey()));
                        Editor editor = insertionContext.getEditor();

                        CaretModel caretModel = editor.getCaretModel();
                        LogicalPosition position = caretModel.getLogicalPosition();
                        int lineEndOffset = editor.getDocument().getLineEndOffset(position.line);
                        caretModel.moveToOffset(lineEndOffset);
                        MyFileEditorManagerListener.collapseFoldRegion(editor);
                    })
                    .withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE)
            );
        });
    }

    @Override
    protected boolean isApplicable(@NotNull PsiElement element, @NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        PsiMethodCallExpression psiMethodCallExpression = (PsiMethodCallExpression) MyPsiUtil.findPsiElementParentMatching(element, p -> p instanceof PsiMethodCallExpression);
        if (psiMethodCallExpression != null) {
            boolean i18nResourceMethod = MyPsiUtil.isI18nResourceMethod(psiMethodCallExpression);
            if (i18nResourceMethod) {
                PsiExpression[] expressions = psiMethodCallExpression.getArgumentList().getExpressions();
                if (expressions.length >= 2) {
                    return element.getParent() == expressions[1];
                }
            }
        }
        return false;
    }
}
