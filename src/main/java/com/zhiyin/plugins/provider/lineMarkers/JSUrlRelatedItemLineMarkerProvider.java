package com.zhiyin.plugins.provider.lineMarkers;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.service.ControllerUrlService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JSUrlRelatedItemLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    public void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<?
            super RelatedItemLineMarkerInfo<?>> result) {
        PsiElement targetElement;

        if (element instanceof LeafPsiElement) {
            targetElement = element;
            String elementType = ((LeafPsiElement) element).getElementType().toString();
            if (elementType.equals("JS:STRING_LITERAL")) {
                element = element.getParent();
            }
        } else {
            return;
        }

        if (!(element instanceof JSLiteralExpression)) {
            return;
        }

        String stringValue = ((JSLiteralExpression) element).getStringValue();
        if (stringValue == null || !stringValue.startsWith("../")) {
            return;
        }

        registerControllerUrlLineMarker(element, result, stringValue, targetElement);
    }

    /**
     * 添加跳转图标
     * @param element 变量
     * @param result 结果集
     * @param stringValue url
     * @param targetElement 要注册行标记器的叶子节点
     */
    public static void registerControllerUrlLineMarker(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result, String stringValue, PsiElement targetElement) {
        if (stringValue.startsWith("../../MesRoot/")) {
            stringValue = stringValue.replace("../../MesRoot/", "/");
        }

        if (stringValue.startsWith("../")) {
            stringValue = stringValue.replace("../", "/");
        }

        if (stringValue.contains("?")){
            stringValue = stringValue.substring(0, stringValue.indexOf("?"));
        }

        ControllerUrlService urlService = element.getProject().getService(ControllerUrlService.class);
        List<PsiMethod> psiMethods = urlService.getMethodForUrl(stringValue);

        List<PsiElement> targets = new ArrayList<>();
        psiMethods.forEach(m -> {
            if (m == null || m.getContainingClass() == null) {
                return;
            }
            targets.add(m);
        });

        // "/Produce/layout/getDataGridColumns" -> "/{module}/layout/getDataGridColumns"
        // "/Basic/layout/getDataGridColumns" -> "/{module}/layout/getDataGridColumns"
        String replacedFirst = stringValue.replaceFirst("/[a-zA-Z]+/", "/{module}/");
        List<PsiMethod> psiMethods2 = urlService.getMethodForUrl(replacedFirst);
        if (!psiMethods2.isEmpty() && psiMethods != psiMethods2) {
            psiMethods2.forEach(m -> {
                if (m == null || m.getContainingClass() == null) {
                    return;
                }
                targets.add(m);
            });
        }

        if (targets.isEmpty()) {
            return;
        }

        // 创建导航图标并添加到结果集中
        NavigationGutterIconBuilder<PsiElement> builder;
        builder = NavigationGutterIconBuilder
                .create(MyIcons.pandaIconSVG16_2)
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .setTargets(targets)
                .setTooltipText("跳转到Controller方法")
                .setPopupTitle("Controller URLs -> Controller method");
        RelatedItemLineMarkerInfo<PsiElement> relatedItemLineMarkerInfo = builder.createLineMarkerInfo(targetElement);
        result.add(relatedItemLineMarkerInfo);
    }
}
