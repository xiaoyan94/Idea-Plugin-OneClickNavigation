package com.zhiyin.plugins.actions;

import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlText;
import com.zhiyin.plugins.notification.MyPluginMessages;
import com.zhiyin.plugins.service.MyToolWindowService;
import com.zhiyin.plugins.toolWindow.MyToolWindowUI;
import org.jetbrains.annotations.Nullable;

import java.awt.datatransfer.StringSelection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 一个 IDEA 插件 Action
 * 功能：当光标在 Feign 接口方法的 Mapping 注解字符串上时，
 *      获取完整 URL（类上的 RequestMapping + 方法上的 Mapping），
 *      并复制到剪贴板。
 */
public class FindControllerByFeignUrlAction extends AnAction {

    private static final Pattern URL_IN_DATA_OPTIONS_PATTERN =
            Pattern.compile("url\\s*:\\s*'([^']+)'|url\\s*:\\s*\"([^\"]+)\"");

    private static final Pattern URL_IN_JSON_PATTERN =
            Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"|'url'\\s*:\\s*'([^']+)'");

    /**
     * 点击 Action 时执行的逻辑
     */
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        if (project == null || editor == null || psiFile == null) {
            return;
        }

//        if (!(psiFile.getLanguage() instanceof JavaLanguage)) {
//            return;
//        }

        // 获取当前光标位置
        CaretModel caretModel = editor.getCaretModel();
        int offset = caretModel.getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);

        String url;

        if (psiFile.getLanguage().isKindOf(JavaLanguage.INSTANCE)) {
            url = getUrlByElementInJava(elementAt, project);
        } else if (psiFile.getLanguage().isKindOf(HTMLLanguage.INSTANCE)) {
            // 从 HTML 中获取 URL
            url = getUrlByElementInHtml(elementAt, project);
        } else {
            url = getUrlByElementInHtml(elementAt, project);
        }
        if (url == null || url.isEmpty()) {
            SelectionModel selectionModel = editor.getSelectionModel();
            if (selectionModel.hasSelection()) {
                url = selectionModel.getSelectedText();
            }
        }
        if (url == null) {
            return;
        }

        // 复制到剪贴板并弹窗
//        CopyPasteManager.getInstance().setContents(new StringSelection(url));
//        MyPluginMessages.showInfo("Feign URL", "已复制 URL:\n" + url);

        // 通过 Feign URL 跳转到对应的 RestContoller
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("OneClickNavigationToolWindow");
        if (toolWindow != null) {
            String finalUrl = url;
            toolWindow.show(() -> {
                MyToolWindowUI ui = project.getService(MyToolWindowService.class).getUI();
                if (ui != null) {
                    ui.setUrlText(finalUrl);
                    ui.clickJumpButton();
                }
            });
        }
    }

    private @Nullable String getUrlByElementInJava(PsiElement elementAt, Project project) {
        if (!(elementAt instanceof PsiJavaToken)) {
            Messages.showWarningDialog(project, "请将光标放在 Mapping 注解的字符串上", "提示");
            return null;
        }

        // 获取字符串字面量表达式
        PsiLiteralExpression literalExpression = PsiTreeUtil.getParentOfType(elementAt, PsiLiteralExpression.class);
        if (literalExpression == null) {
            Messages.showWarningDialog(project, "未找到字符串字面量", "提示");
            return null;
        }

        // 找到方法上的 Mapping 注解
        PsiAnnotation methodAnnotation = PsiTreeUtil.getParentOfType(literalExpression, PsiAnnotation.class);
        if (methodAnnotation == null) {
            Messages.showWarningDialog(project, "未找到方法注解", "提示");
            return null;
        }

        String qualifiedName = methodAnnotation.getQualifiedName();
        if (qualifiedName == null || !(qualifiedName.endsWith("Mapping"))) {
            Messages.showWarningDialog(project, "光标不在 Mapping 注解内部", "提示");
            return null;
        }

        // 取方法上的路径
        String methodPath = getAnnotationValue(methodAnnotation).orElse("");
        PsiMethod psiMethod = PsiTreeUtil.getParentOfType(methodAnnotation, PsiMethod.class);
        if (psiMethod == null) {
            Messages.showWarningDialog(project, "未找到方法", "提示");
            return null;
        }

        // 取类上的 RequestMapping
        PsiClass psiClass = psiMethod.getContainingClass();

        // 检查类是不是 Feign 接口
        if (psiClass == null || !psiClass.isInterface()) {
            Messages.showWarningDialog(project, "该操作仅支持 Feign 接口", "提示");
            return null;
        }

        String classPath = "";
        for (PsiAnnotation classAnnotation : psiClass.getAnnotations()) {
            if ("org.springframework.web.bind.annotation.RequestMapping".equals(classAnnotation.getQualifiedName())) {
                classPath = getAnnotationValue(classAnnotation).orElse("");
            }
        }

        // 拼接 URL
        String url = normalizePath(classPath) + normalizePath(methodPath);
        if (url.isEmpty()) {
            Messages.showWarningDialog(project, "未解析到有效 URL", "提示");
            return null;
        }
        return url;
    }

    /**
     * 控制 Action 是否显示/可用
     * 只有当光标在 Mapping 注解字符串上时才显示菜单
     */
    @Override
    public void update(AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        boolean visible = false;
        if (editor != null) {
            if (psiFile != null && psiFile.getVirtualFile().getExtension() != null) {
                if (psiFile.getVirtualFile().getExtension().matches("xml|html|js|htm|ftl")){
                    SelectionModel selectionModel = editor.getSelectionModel();
                    visible = selectionModel.hasSelection();
                    e.getPresentation().setEnabledAndVisible(true);
                    return;
                }
            }

        }
        if (editor != null && psiFile instanceof PsiJavaFile) {
            int offset = editor.getCaretModel().getOffset();
            PsiElement elementAt = psiFile.findElementAt(offset);
            if (elementAt instanceof PsiJavaToken) {
                PsiLiteralExpression literalExpression = PsiTreeUtil.getParentOfType(elementAt, PsiLiteralExpression.class);
                if (literalExpression != null) {
                    PsiAnnotation annotation = PsiTreeUtil.getParentOfType(literalExpression, PsiAnnotation.class);
                    if (annotation != null && annotation.getQualifiedName() != null) {
                        String qName = annotation.getQualifiedName();
                        if (qName.startsWith("org.springframework.web.bind.annotation")
                                && qName.endsWith("Mapping")) {
                            // ✅ 额外判断：必须在 Feign 接口类中
                            PsiMethod method = PsiTreeUtil.getParentOfType(annotation, PsiMethod.class);
                            if (method != null) {
                                PsiClass psiClass = method.getContainingClass();
                                if (psiClass != null && psiClass.isInterface()) {
                                    for (PsiAnnotation classAnno : psiClass.getAnnotations()) {
                                        if ("org.springframework.cloud.openfeign.FeignClient"
                                                .equals(classAnno.getQualifiedName())) {
                                            visible = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            // visible = true;
                        }
                    }
                }
            }
        }

        e.getPresentation().setEnabledAndVisible(visible);
    }

    /**
     * 获取注解的 value 或 path 属性
     */
    private Optional<String> getAnnotationValue(PsiAnnotation annotation) {
        PsiAnnotationMemberValue value = annotation.findDeclaredAttributeValue("value");
        if (value == null) {
            value = annotation.findDeclaredAttributeValue("path");
        }

        if (value instanceof PsiArrayInitializerMemberValue) {
            PsiAnnotationMemberValue[] initializers = ((PsiArrayInitializerMemberValue) value).getInitializers();
            if (initializers.length > 0 && initializers[0] instanceof PsiLiteralExpression) {
                Object val = ((PsiLiteralExpression) initializers[0]).getValue();
                return Optional.ofNullable(val == null ? "" : val.toString());
            }
        } else if (value instanceof PsiLiteralExpression) {
            Object val = ((PsiLiteralExpression) value).getValue();
            return Optional.ofNullable(val == null ? "" : val.toString());
        }
        return Optional.empty();
    }

    /**
     * 格式化路径，去掉多余的 / 并保证以 / 开头
     */
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

    private String getUrlByElementInHtml(PsiElement elementAt, Project project) {
        if (elementAt == null) return null;

        // 1) HTML attribute: <select data-url="../Produce/xxx"> 或 url="..."（XmlAttributeValue.getValue() 已去掉引号）
        XmlAttributeValue xmlAttr = PsiTreeUtil.getParentOfType(elementAt, XmlAttributeValue.class, false);
        if (xmlAttr != null) {
            String v = xmlAttr.getValue();

            // 先判断是不是标准 URL
            if (isUrlCandidate(v)) return normalizeUrlString(v);

            // 如果是 JSON 串，尝试正则提取 url
            String urlFromJson = extractUrlFromJson(v);
            if (urlFromJson != null) return normalizeUrlString(urlFromJson);
        }

        // 2) JS string literal inside <script> 或 .js inline (JSLiteralExpression.getStringValue() 去掉引号)
        JSLiteralExpression jsLiteral = PsiTreeUtil.getParentOfType(elementAt, JSLiteralExpression.class, false);
        if (jsLiteral == null && elementAt instanceof LeafPsiElement) {
            // 有时 elementAt 是叶子 token（JS:STRING_LITERAL），它的 parent 可能就是 JSLiteralExpression
            PsiElement p = elementAt.getParent();
            if (p instanceof JSLiteralExpression) {
                jsLiteral = (JSLiteralExpression) p;
            }
        }
        if (jsLiteral != null) {
            String v = jsLiteral.getStringValue();
            if (isUrlCandidate(v)) return normalizeUrlString(v);
        }

        // 3) 叶子节点文本本身（例如 elementAt.getText() = "'../Produce/xx'" 或 "\"../Produce/xx\""）
        String text = elementAt.getText();
        if (text != null) {
            String stripped = stripQuotes(text).trim();
            if (isUrlCandidate(stripped)) return normalizeUrlString(stripped);

            // data-options 里提取 url
            String urlFromOptions = extractUrlFromDataOptions(stripped);
            if (urlFromOptions != null) return normalizeUrlString(urlFromOptions);
        }

        // 4) html/xml 文本节点（少见，但兜底）
        XmlText xmlText = PsiTreeUtil.getParentOfType(elementAt, XmlText.class, false);
        if (xmlText != null) {
            String v = xmlText.getValue().trim();
            if (isUrlCandidate(v)) return normalizeUrlString(v);
        }

        return null;
    }

    private boolean isUrlCandidate(String s) {
        if (s == null || s.trim().isEmpty()) return false;
        s = s.trim();
        // 根据你项目的 URL 风格调整判断条件
        // 这里我们认为以 ../ 开始 或 以 / 开始（局部或绝对），都可能是目标 URL
        return s.startsWith("../") || s.startsWith("/") || s.startsWith("./");
    }

    private String extractUrlFromJson(String jsonLike) {
        if (jsonLike == null || jsonLike.isEmpty()) return null;

        Matcher m = URL_IN_JSON_PATTERN.matcher(jsonLike);
        if (m.find()) {
            // 捕获组1是双引号，组2是单引号
            String url = m.group(1) != null ? m.group(1) : m.group(2);
            return url;
        }
        return null;
    }

    private String extractUrlFromDataOptions(String optionsString) {
        if (optionsString == null || optionsString.isEmpty()) return null;
        Matcher m = URL_IN_DATA_OPTIONS_PATTERN.matcher(optionsString);
        if (m.find()) {
            // 捕获组1是单引号，组2是双引号
            String url = m.group(1) != null ? m.group(1) : m.group(2);
            return url;
        }
        return null;
    }

    private String stripQuotes(String s) {
        if (s == null) return null;
        s = s.trim();
        if ((s.length() >= 2) && ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private String normalizeUrlString(String s) {
        if (s == null) return null;
        s = s.trim();
        // 去掉查询参数
        int q = s.indexOf('?');
        if (q >= 0) s = s.substring(0, q);

        // 统一 ../ -> / 以及 ../../MesRoot/ -> /
        if (s.startsWith("../../MesRoot/")) {
            s = s.replaceFirst("\\.\\./\\.\\./MesRoot/", "/");
        } else if (s.startsWith("../")) {
            s = s.replaceFirst("\\.\\./", "/");
        }
        return s;
    }
}
