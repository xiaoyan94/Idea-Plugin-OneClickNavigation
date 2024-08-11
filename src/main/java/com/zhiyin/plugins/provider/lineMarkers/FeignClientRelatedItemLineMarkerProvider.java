package com.zhiyin.plugins.provider.lineMarkers;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.service.ControllerUrlService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FeignClientRelatedItemLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    public void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<?
            super RelatedItemLineMarkerInfo<?>> result) {
        if (!(element instanceof PsiMethod)) {
            return;
        }

        Project project = element.getProject();

        PsiMethod method = (PsiMethod) element;

        PsiAnnotation[] annotations = method.getAnnotations();
        if(annotations.length == 0){
            return;
        }

        PsiClass containingClass = method.getContainingClass();

        if (containingClass == null || !isFeignClient(containingClass)) {
            return;
        }

        List<String> urls = extractUrlsFromMethod(project, containingClass, method);
        if (urls.isEmpty()) {
            return;
        }

        List<PsiElement> targets = new ArrayList<>();
        ControllerUrlService urlService = project.getService(ControllerUrlService.class);
        urls.forEach(url -> {
            if (url != null && !url.startsWith("/")) {
                url = "/" + url;
            }

            List<PsiMethod> methods = urlService.getMethodForUrl(url);
            List<PsiMethod> toRemove = new ArrayList<>();
            for (PsiMethod psiMethod : methods) {
                if (psiMethod == null || psiMethod.getContainingClass() == null){
                    toRemove.add(psiMethod);
                }
            }
            toRemove.forEach(urlService::removeUrlsNullMethod);

            // TODO 根据设置决定可以跳转哪些
            targets.addAll(methods);
        });

        // 创建导航图标并添加到结果集中
        NavigationGutterIconBuilder<PsiElement> builder;
        builder = NavigationGutterIconBuilder
                .create(MyIcons.pandaIconSVG16_2)
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .setTargets(targets)
                .setTooltipText("Navigate to RestController")
                .setPopupTitle("Feign Client URLs -> RestController method");
        RelatedItemLineMarkerInfo<PsiElement> relatedItemLineMarkerInfo = builder.createLineMarkerInfo(annotations[0]);
        result.add(relatedItemLineMarkerInfo);
    }

    private boolean isFeignClient(PsiClass psiClass) {
        for (PsiAnnotation annotation : psiClass.getAnnotations()) {
            String qualifiedName = annotation.getQualifiedName();
            // RestController
            if ("org.springframework.cloud.openfeign.FeignClient".equals(qualifiedName)
                    || "org.springframework.web.bind.annotation.RestController".equals(qualifiedName)
            ) {
                return true;
            }
        }
        return false;
    }

    private List<String> extractUrlsFromMethod(Project project, PsiClass containingClass, PsiMethod method) {
        // Extract URLs from @RequestMapping and its variants
        ControllerUrlService urlService = project.getService(ControllerUrlService.class);
        String classUrl = urlService.getMappingUrl(containingClass);
        String methodUrl = urlService.getMappingUrl(method);
        if (classUrl != null && methodUrl != null) {
            return List.of(classUrl.startsWith("/") ? classUrl + methodUrl : "/" + classUrl + methodUrl);
        }
        return List.of(); // Replace with actual URL extraction logic
    }

    private String constructUrl(PsiMethod method, List<String> urls) {
        // Construct the URL based on method and URL patterns
        return urls.stream().findFirst().orElse(""); // Simplified for example
    }

    private void callYourService(String url) {
        // Implement URL handling, e.g., call your service or navigate
    }
}
