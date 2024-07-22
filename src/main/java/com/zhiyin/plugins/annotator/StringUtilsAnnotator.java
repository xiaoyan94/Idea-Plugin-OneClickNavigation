package com.zhiyin.plugins.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiExpression;
import com.zhiyin.plugins.intention.ReplaceWithGetStringFromMapIntention;
import org.jetbrains.annotations.NotNull;

/**
 * StringUtils.objToString 方法警告
 */
public class StringUtilsAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression methodCall = (PsiMethodCallExpression) element;
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
                                if (innerQualifier != null) {
                                    holder.newAnnotation(HighlightSeverity.WARNING, "请使用 StringUtils.getStringFromMap(map, \"someKey\") 代替")
                                          .range(element)
                                          .withFix(new ReplaceWithGetStringFromMapIntention(methodCall))
                                          .create();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
