package com.zhiyin.plugins.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.impl.JavaConstantExpressionEvaluator;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.*;

public class CollectUrlsAction extends AnAction {

    private static final String[] REQUEST_MAPPING_ANNOTATIONS = {
        "org.springframework.web.bind.annotation.RequestMapping",
        "org.springframework.web.bind.annotation.GetMapping",
        "org.springframework.web.bind.annotation.PostMapping",
        "org.springframework.web.bind.annotation.PutMapping",
        "org.springframework.web.bind.annotation.DeleteMapping"
    };

    // 缓存结构
    private final Map<PsiClass, Set<String>> controllerUrlsCache = new HashMap<>();
    private final Map<String, PsiMethod> urlMethodCache = new HashMap<>();

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        collectControllerUrls(project, scope);

        // 显示收集到的 URL
        showCollectedUrls(project);

        // 测试 URL
        String[] testUrls = {
                "/Order/OrderSort/endFastStopDevice",
                "/MesRoot/sysadm/Role/updateRoleById",
                "/Mold/downloadTemplate",
                "/Report/ProcessCapacityReport/exportProcessCapacityReport"
        };

        for (String url : testUrls) {
            navigateToUrl(url);
            Messages.showInfoMessage("Test URL: " + url, "Test Result");
        }
    }

    private void navigateToUrl(String url) {
        PsiMethod method = getMethodForUrl(url);
        if (method != null) {
            // 找到了对应的方法，进行导航
            method.navigate(true);
            System.out.println("Successfully navigated to: " + url);
            System.out.println("Method: " + method.getName());
            System.out.println("Containing class: " + method.getContainingClass().getQualifiedName());
            System.out.println("File: " + method.getContainingFile().getVirtualFile().getPath());
            System.out.println();
        } else {
            System.out.println("Could not find method for URL: " + url);
            System.out.println();
        }
    }

    private void collectControllerUrls(Project project, GlobalSearchScope scope) {
        List<PsiClass> controllers = findClassesWithAnnotationUsingIndex(project, "Controller", scope);
        for (PsiClass controller : controllers) {
            String classUrl = getMappingUrl(controller);
            Set<String> methodUrls = new HashSet<>();
            controllerUrlsCache.put(controller, methodUrls);

            PsiMethod[] methods = controller.getMethods();
            for (PsiMethod method : methods) {
                String methodUrl = getMappingUrl(method);
                if (methodUrl != null && !methodUrl.isEmpty()) {
                    String fullUrl = classUrl + methodUrl;
                    methodUrls.add(fullUrl);
                    urlMethodCache.put(fullUrl, method);
                }
            }
        }
    }

    private void showCollectedUrls(Project project) {
        StringBuilder message = new StringBuilder("Collected URLs:\n");
        for (Map.Entry<PsiClass, Set<String>> entry : controllerUrlsCache.entrySet()) {
            message.append("Controller: ").append(entry.getKey().getQualifiedName()).append("\n");
            for (String url : entry.getValue()) {
                message.append("  ").append(url).append("\n");
            }
            message.append("\n");
        }

        com.intellij.openapi.ui.Messages.showMessageDialog(project, message.toString(), "Controller URLs", com.intellij.openapi.ui.Messages.getInformationIcon());
    }

    // 新增方法：获取指定 Controller 的所有 URL
    public Set<String> getUrlsForController(PsiClass controller) {
        return controllerUrlsCache.getOrDefault(controller, Collections.emptySet());
    }

    // 新增方法：根据 URL 获取对应的 PsiMethod
    public PsiMethod getMethodForUrl(String url) {
        return urlMethodCache.get(url);
    }

    public List<PsiClass> findClassesWithAnnotationUsingIndex(Project project, String annotationName, GlobalSearchScope scope) {
        List<PsiClass> result = new ArrayList<>();

        Collection<PsiAnnotation> annotations = JavaAnnotationIndex.getInstance().get(annotationName, project, scope);
        for (PsiAnnotation annotation : annotations) {
            PsiModifierList modifierList = (PsiModifierList) annotation.getParent();
            PsiElement owner = modifierList.getParent();
            if (owner instanceof PsiClass) {
                result.add((PsiClass) owner);
            }
        }

        return result;
    }

    private String getMappingUrl(PsiElement element) {
        for (String annotationFQN : REQUEST_MAPPING_ANNOTATIONS) {
            PsiAnnotation annotation = getAnnotation(element, annotationFQN);
            if (annotation != null) {
                PsiAnnotationMemberValue value = annotation.findAttributeValue("value");
                if (value != null) {
                    return getAnnotationMemberValue(value);
                }
            }
        }
        return "";
    }

    public String getAnnotationMemberValue(PsiAnnotationMemberValue memberValue) {
        PsiReference reference = memberValue.getReference();
        if (memberValue instanceof PsiExpression) {
            Object constant = JavaConstantExpressionEvaluator.computeConstantExpression((PsiExpression) memberValue, false);
            return constant == null ? null : constant.toString();
        }
        if (memberValue instanceof PsiArrayInitializerMemberValue) {
            PsiArrayInitializerMemberValue arrayValue = (PsiArrayInitializerMemberValue) memberValue;
            for (PsiAnnotationMemberValue memberValue2 : arrayValue.getInitializers()) {
                if (memberValue2 instanceof PsiLiteralExpression) {
                    Object constant = JavaConstantExpressionEvaluator.computeConstantExpression((PsiExpression) memberValue2, false);
                    return constant == null ? null : constant.toString();
                }
            }
        }
        try {
            if (reference != null) {
                PsiElement resolve = reference.resolve();
                if (resolve instanceof PsiEnumConstant) {
                    // 枚举常量
                    return ((PsiEnumConstant) resolve).getName();
                } else if (resolve instanceof PsiField) {
                    // 引用其他字段
                    return getFieldDefaultValue((PsiField) resolve);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "";
    }

    private String getFieldDefaultValue(PsiField psiField) {
        PsiExpression initializer = psiField.getInitializer();
        if (initializer instanceof PsiLiteralExpression) {
            return Objects.requireNonNull(((PsiLiteralExpression) initializer).getValue()).toString();
        }
        if (initializer instanceof PsiReferenceExpression) {
            PsiElement resolve = ((PsiReferenceExpression) initializer).resolve();
            if (resolve instanceof PsiField) {
                return getFieldDefaultValue((PsiField) resolve);
            }
        }
        return "";
    }

    private PsiAnnotation getAnnotation(PsiElement element, String annotationFQN) {
        if (element instanceof PsiClass) {
            return ((PsiClass) element).getAnnotation(annotationFQN);
        } else if (element instanceof PsiMethod) {
            return ((PsiMethod) element).getAnnotation(annotationFQN);
        }
        return null;
    }
}
