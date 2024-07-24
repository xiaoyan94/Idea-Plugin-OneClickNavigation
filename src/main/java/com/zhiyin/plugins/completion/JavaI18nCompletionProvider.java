package com.zhiyin.plugins.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
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
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.zhiyin.plugins.completion.CommonI18nCompletionLogic.addPropertiesToCompletionResult;

public class JavaI18nCompletionProvider extends BaseCompletionProvider {
    @Override
    protected void performCompletion(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result, @NotNull String prefix) {
        PsiFile originalFile = parameters.getOriginalFile();
        Module module = MyPsiUtil.getModuleByPsiElement(originalFile);
        List<IProperty> properties = MyPropertiesUtil.getModuleI18nPropertiesCN(originalFile.getProject(), module);
        Function<IProperty, InsertHandler<LookupElement>> getLookupElementInsertHandler = getLookupElementInsertHandler(parameters);
        addPropertiesToCompletionResult(result, prefix, properties, getLookupElementInsertHandler);
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

    private @NotNull Function<IProperty, InsertHandler<LookupElement>> getLookupElementInsertHandler(@SuppressWarnings("unused") @NotNull CompletionParameters parameters) {
        return property -> (insertionContext, item) -> {
            insertionContext.getDocument().replaceString(insertionContext.getStartOffset(), insertionContext.getTailOffset(), Objects.requireNonNull(property.getKey()));
            Editor editor = insertionContext.getEditor();

            CaretModel caretModel = editor.getCaretModel();
            LogicalPosition position = caretModel.getLogicalPosition();
            int lineEndOffset = editor.getDocument().getLineEndOffset(position.line);
            caretModel.moveToOffset(lineEndOffset);
            MyFileEditorManagerListener.collapseFoldRegion(editor);
        };
    }
}
