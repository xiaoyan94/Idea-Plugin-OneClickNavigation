package com.zhiyin.plugins.provider.lineMarkers;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.service.ControllerUrlService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class FeignClientRelatedItemLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    public void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<?
            super RelatedItemLineMarkerInfo<?>> result) {
        if (!(element instanceof PsiJavaToken)) {
            return;
        }

        PsiElement registeredLeafElement = element;
        element = element.getParent();

        if(!(element instanceof PsiLiteralExpression)) {
            return;
        }

        if (!(element.getParent() instanceof PsiArrayInitializerMemberValue || element.getParent() instanceof PsiNameValuePair)) {
            return;
        }

        PsiNameValuePair nameValuePair;
        if (element.getParent() instanceof PsiArrayInitializerMemberValue) {
            nameValuePair = (PsiNameValuePair) element.getParent().getParent();
        } else {
            nameValuePair = (PsiNameValuePair) element.getParent();
        }

        if (nameValuePair.getName() != null && !Objects.equals(nameValuePair.getName(), "value")) {
            return;
        }

        if (nameValuePair.getParent() instanceof PsiAnnotationParameterList && nameValuePair.getParent().getParent() instanceof PsiAnnotation){
            String qualifiedName = ((PsiAnnotation) nameValuePair.getParent().getParent()).getQualifiedName();
            if (!("org.springframework.web.bind.annotation.PostMapping".equals(qualifiedName)
                || "org.springframework.web.bind.annotation.GetMapping".equals(qualifiedName)
                || "org.springframework.web.bind.annotation.PutMapping".equals(qualifiedName)
                || "org.springframework.web.bind.annotation.DeleteMapping".equals(qualifiedName)
                || "org.springframework.web.bind.annotation.RequestMapping".equals(qualifiedName)
            )) {
                return;
            }
        }

        element = PsiTreeUtil.getParentOfType(element, PsiMethod.class);

        if (element == null) {
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
            if (methods == null || methods.isEmpty()) {
                return;
            }
            List<PsiMethod> toRemove = new ArrayList<>();
            for (PsiMethod psiMethod : methods) {
                if (psiMethod == null || psiMethod.getContainingClass() == null){
                    toRemove.add(psiMethod);
                }
            }
//            toRemove.forEach(urlService::removeUrlsNullMethod);

            // TODO 根据设置决定可以跳转哪些
            if (methods.isEmpty()) {
                return;
            }

            // this will cause BUG
//            methods.removeIf(m -> m == null || m.getContainingClass() == null || m.getContainingClass() == containingClass);

            methods.forEach(m -> {
                if (m == null || m.getContainingClass() == null || m.getContainingClass() == containingClass) {
                    return;
                }
                targets.add(m);
            });

//            targets.addAll(methods);
        });

        if (targets.isEmpty()) {
            return;
        }

        // 创建导航图标并添加到结果集中
        NavigationGutterIconBuilder<PsiElement> builder;
        builder = NavigationGutterIconBuilder
                .create(MyIcons.pandaIconSVG16_2)
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .setTargets(targets)
                .setTooltipText("Navigate to RestController")
                .setPopupTitle("Feign Client URLs -> RestController method");
        RelatedItemLineMarkerInfo<PsiElement> relatedItemLineMarkerInfo = builder.createLineMarkerInfo(registeredLeafElement);
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
