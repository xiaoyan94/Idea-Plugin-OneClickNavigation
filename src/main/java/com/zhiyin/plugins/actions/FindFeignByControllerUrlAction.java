package com.zhiyin.plugins.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.service.ControllerUrlService;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;

import java.util.*;
import java.util.stream.Collectors;

public class FindFeignByControllerUrlAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        if (project == null || editor == null || psiFile == null) {
            return;
        }

        CaretModel caretModel = editor.getCaretModel();
        int offset = caretModel.getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);

        if (!(elementAt instanceof PsiJavaToken)) {
            Messages.showWarningDialog(project, "请将光标放在 Controller 的 Mapping 注解字符串上", "提示");
            return;
        }

        PsiLiteralExpression literalExpression = PsiTreeUtil.getParentOfType(elementAt, PsiLiteralExpression.class);
        if (literalExpression == null) {
            return;
        }

        PsiAnnotation methodAnnotation = PsiTreeUtil.getParentOfType(literalExpression, PsiAnnotation.class);
        if (methodAnnotation == null) {
            return;
        }

        String qualifiedName = methodAnnotation.getQualifiedName();
        if (qualifiedName == null || !qualifiedName.endsWith("Mapping")) {
            return;
        }

        // 拼接 Controller URL
        PsiMethod psiMethod = PsiTreeUtil.getParentOfType(methodAnnotation, PsiMethod.class);
        if (psiMethod == null) {
            return;
        }

        PsiClass psiClass = psiMethod.getContainingClass();
        if (psiClass == null) {
            return;
        }

        String classPath = getClassRequestMapping(psiClass);
        String methodPath = getAnnotationValue(methodAnnotation).orElse("");
        String targetUrl = normalizePath(classPath) + normalizePath(methodPath);

        if (targetUrl.isEmpty()) {
            Messages.showWarningDialog(project, "未解析到 Controller URL", "提示");
            return;
        }

        // 查找所有 Feign 接口
        ControllerUrlService controllerUrlService = project.getService(ControllerUrlService.class);
        List<PsiMethod> matchedFeignMethods = controllerUrlService.getMethodForUrl(targetUrl);

        if (matchedFeignMethods.isEmpty()) {
            Messages.showInfoMessage(project, "未找到对应的 Feign 接口方法", "提示");
            return;
        }

        matchedFeignMethods = matchedFeignMethods.stream()
                .filter(Objects::nonNull)
                .filter(m -> m.getContainingClass() != null)
                .filter(m -> m.getContainingClass().isInterface())
                .collect(Collectors.toList());

        if (matchedFeignMethods.size() == 1) {
            // 直接跳转
            navigateToMethod(matchedFeignMethods.get(0));
        } else {
            // 多个候选，弹出选择框
            String[] candidates = matchedFeignMethods.stream()
                                                     .filter(Objects::nonNull)
                                                     .filter(m -> m.getContainingClass() != null)
                                                     .filter(m -> m.getContainingClass().isInterface())
                                                     .map(m -> {
                                                         Module module = MyPsiUtil.getModuleByPsiElement(m);
                                                         String moduleName = "";
                                                         if (module != null) {
                                                             moduleName = MyPropertiesUtil.getSimpleModuleName(module);
                                                         }
                                                         String itemValue = moduleName + ": " + m.getContainingClass().getName() + "." + m.getName();
                                                         return itemValue;
                                                     })
                                                     .toArray(String[]::new);

            String chosen = Messages.showEditableChooseDialog(
                    "找到多个 Feign 方法匹配 " + targetUrl,
                    "选择 Feign 方法",
                    MyIcons.pandaIconSVG16_2,
                    candidates,
                    candidates[0],
                    null
                                                             );

            if (chosen != null) {
                // 根据字符串找到对应的 PsiMethod
                Optional<PsiMethod> method = matchedFeignMethods.stream()
                                                                .filter(Objects::nonNull)
                                                                .filter(m -> m.getContainingClass() != null)
                                                                .filter(m -> {
                                                                    Module module = MyPsiUtil.getModuleByPsiElement(m);
                                                                    String moduleName = "";
                                                                    if (module != null) {
                                                                        moduleName = MyPropertiesUtil.getSimpleModuleName(module);
                                                                    }
                                                                    String itemValue = moduleName + ": " + m.getContainingClass().getName() + "." + m.getName();
                                                                    return itemValue.equals(chosen);
                                                                })
                                                                .findFirst();

                method.ifPresent(this::navigateToMethod);
            }

        }
    }

    @Override
    public void update(AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        boolean visible = false;
        if (editor != null && psiFile instanceof PsiJavaFile) {
            int offset = editor.getCaretModel().getOffset();
            PsiElement elementAt = psiFile.findElementAt(offset);
            if (elementAt instanceof PsiJavaToken) {
                PsiLiteralExpression literalExpression =
                        PsiTreeUtil.getParentOfType(elementAt, PsiLiteralExpression.class);
                if (literalExpression != null) {
                    PsiAnnotation annotation =
                            PsiTreeUtil.getParentOfType(literalExpression, PsiAnnotation.class);
                    if (annotation != null && annotation.getQualifiedName() != null) {
                        String qName = annotation.getQualifiedName();
                        if (qName.startsWith("org.springframework.web.bind.annotation")
                                && qName.endsWith("Mapping")) {
                            PsiMethod method = PsiTreeUtil.getParentOfType(annotation, PsiMethod.class);
                            if (method != null) {
                                PsiClass psiClass = method.getContainingClass();
                                if (psiClass != null && !psiClass.isInterface()) {
                                    for (PsiAnnotation anno : psiClass.getAnnotations()) {
                                        if ("org.springframework.web.bind.annotation.RestController"
                                                .equals(anno.getQualifiedName())) {
                                            visible = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        e.getPresentation().setEnabledAndVisible(visible);
    }

    private Optional<String> getAnnotationValue(PsiAnnotation annotation) {
        PsiAnnotationMemberValue value = annotation.findDeclaredAttributeValue("value");
        if (value == null) {
            value = annotation.findDeclaredAttributeValue("path");
        }
        if (value instanceof PsiLiteralExpression) {
            Object val = ((PsiLiteralExpression) value).getValue();
            return Optional.ofNullable(val == null ? "" : val.toString());
        }
        if (value instanceof PsiArrayInitializerMemberValue) {
            PsiAnnotationMemberValue[] initializers =
                    ((PsiArrayInitializerMemberValue) value).getInitializers();
            List<String> values = new ArrayList<>();
            for (PsiAnnotationMemberValue initializer : initializers) {
                if (initializer instanceof PsiLiteralExpression) {
                    Object val = ((PsiLiteralExpression) initializer).getValue();
                    if (val != null) {
                        values.add(val.toString());
                    }
                }
            }
            if (!values.isEmpty()) {
                // 这里我用第一个，也可以用 String.join(",", values) 返回多个
                return Optional.of(values.get(0));
            }
        }
        return Optional.empty();
    }

    private String getClassRequestMapping(PsiClass psiClass) {
        for (PsiAnnotation anno : psiClass.getAnnotations()) {
            if ("org.springframework.web.bind.annotation.RequestMapping".equals(anno.getQualifiedName())) {
                return getAnnotationValue(anno).orElse("");
            }
        }
        return "";
    }

    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    private void navigateToMethod(PsiMethod method) {
        if (method != null) {
            PsiElement navElement = method.getNavigationElement();
            if (navElement instanceof Navigatable) {
                ((Navigatable) navElement).navigate(true);
            }
        }
    }
}
