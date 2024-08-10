package com.zhiyin.plugins.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
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

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        // Define the scope to search in (the whole project)
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
//        scope = GlobalSearchScope.fileScope(e.getRequiredData(CommonDataKeys.PSI_FILE));
        // Collect all controller URLs
        Set<String> urls = new HashSet<>();
        
        // Search for classes with @Controller annotation
        List<PsiClass> controllers = findClassesWithAnnotationUsingIndex(project, "Controller", scope);
        for (PsiClass controller : controllers) {
            String classUrl = getMappingUrl(controller);
            PsiMethod[] methods = controller.getMethods();
            for (PsiMethod method : methods) {
                String methodUrl = getMappingUrl(method);
                if (methodUrl != null) {
                    urls.add(classUrl + methodUrl);
                }
            }
        }
        
        // Show collected URLs
        StringBuilder message = new StringBuilder("Collected URLs:\n");
        for (String url : urls) {
            message.append(url).append("\n");
        }
        
        com.intellij.openapi.ui.Messages.showMessageDialog(project, message.toString(), "Controller URLs", com.intellij.openapi.ui.Messages.getInformationIcon());
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
