package com.zhiyin.plugins.annotator;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.zhiyin.plugins.intention.RemoveSysLoggerFix;
import com.zhiyin.plugins.resources.MyIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * SysLogger 注解警告：不要在查询相关方法上使用 @SysLogger 注解
 */
public class SysLoggerAnnotator implements Annotator {

    private static final String[] QUERY_METHOD_PREFIXES = {"find", "get", "select", "list", "query", "load", "fetch", "search", "retrieve", "count", "total", "size", "length"};
    private static final String SYS_LOGGER_FQN = "com.zhiyin.aspect.SysLogger";

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        // 如果是在查询方法上的注解
        if (element instanceof PsiAnnotation) {
            PsiAnnotation annotation = (PsiAnnotation) element;
            if (Objects.equals(annotation.getQualifiedName(), SYS_LOGGER_FQN)) {
                PsiMethod method = PsiTreeUtil.getParentOfType(annotation, PsiMethod.class);
                if (method != null && isQueryRelatedMethod(method.getName())) {
                    holder.newAnnotation(HighlightSeverity.ERROR, "请不要在查询相关方法上使用 @SysLogger 注解")
                            .highlightType(ProblemHighlightType.ERROR)
                            .withFix(new RemoveSysLoggerFix(annotation))
//                            .range(annotation.getTextRange())
                            .gutterIconRenderer(new MyJavaAnnotator.MyRenderer(annotation, MyIcons.pandaIcon16, "请不要在查询相关方法上使用 @SysLogger 注解"))
                            .tooltip("请不要在查询相关方法上使用 @SysLogger 注解")
                            .create();
                }
            }
        }
    }

    private boolean isQueryRelatedMethod(String methodName) {
        for (String prefix : QUERY_METHOD_PREFIXES) {
            if (methodName.toLowerCase().startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
