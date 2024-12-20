package com.zhiyin.plugins.intention;

import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * 替换 StringUtils.objToString 为 StringUtils.getStringFromMap
 */
public class ReplaceWithGetStringFromMapIntention implements IntentionAction, HighPriorityAction {

    @SafeFieldForPreview
    private final SmartPsiElementPointer<PsiMethodCallExpression> methodCallPointer;

    public ReplaceWithGetStringFromMapIntention(PsiMethodCallExpression methodCall) {
        this.methodCallPointer = SmartPointerManager.createPointer(methodCall);
    }

    @NotNull
    @Override
    public String getText() {
        return "使用 StringUtils.getStringFromMap 方法代替";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiFile file) {
        PsiMethodCallExpression methodCall = methodCallPointer.getElement();
        if (methodCall == null) return false;

        PsiReferenceExpression methodExpression = methodCall.getMethodExpression();
        String methodName = methodExpression.getReferenceName();
        PsiExpression qualifierExpression = methodExpression.getQualifierExpression();

        if ("objToString".equals(methodName) && qualifierExpression != null) {
            if (qualifierExpression instanceof PsiReferenceExpression) {
                PsiReferenceExpression ref = (PsiReferenceExpression) qualifierExpression;
                if ("StringUtils".equals(ref.getReferenceName())) {
                    PsiExpression[] arguments = methodCall.getArgumentList().getExpressions();
                    if (arguments.length == 1 && arguments[0] instanceof PsiMethodCallExpression) {
                        PsiMethodCallExpression innerCall = (PsiMethodCallExpression) arguments[0];
                        PsiReferenceExpression innerMethodExpression = innerCall.getMethodExpression();
                        if ("get".equals(innerMethodExpression.getReferenceName())) {
                            PsiExpression innerQualifier = innerMethodExpression.getQualifierExpression();
                            return innerQualifier != null;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiFile file) {
        PsiMethodCallExpression methodCall = methodCallPointer.getElement();
        if (methodCall == null) return;

        PsiExpression[] arguments = methodCall.getArgumentList().getExpressions();
        if (arguments.length == 1 && arguments[0] instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression innerCall = (PsiMethodCallExpression) arguments[0];
            PsiExpression innerQualifier = innerCall.getMethodExpression().getQualifierExpression();
            PsiExpression[] innerArguments = innerCall.getArgumentList().getExpressions();
            if (innerQualifier != null && innerArguments.length == 1) {
                PsiExpression keyExpression = innerArguments[0];
                String newExpressionText = "StringUtils.getStringFromMap(" + innerQualifier.getText() + ", " + keyExpression.getText() + ")";
                PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
                PsiExpression newExpression = factory.createExpressionFromText(newExpressionText, methodCall);
                methodCall.replace(newExpression);
            }
        }
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
