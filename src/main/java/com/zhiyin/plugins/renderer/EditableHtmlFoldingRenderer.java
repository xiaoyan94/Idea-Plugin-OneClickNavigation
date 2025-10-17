package com.zhiyin.plugins.renderer;

import com.intellij.icons.AllIcons;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import com.zhiyin.plugins.notification.MyPluginMessages;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.ui.MyTranslateDialogWrapper;
import com.zhiyin.plugins.utils.MyPropertiesUtil;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.openapi.util.text.StringUtil.isNotEmpty;

public class EditableHtmlFoldingRenderer implements EditorCustomElementRenderer {
    private static final Pattern HTML_PATTERN = Pattern.compile("<@message\\s+key=['\"](.*?)['\"]\\s*/>");
    // 支持多种写法
    private static final Pattern JS_PATTERN = Pattern.compile("zhiyin\\.i18n\\.translate\\(['\"](.*?)['\"]\\)");

    private final String originalText;
    private final String key;
    private final Editor editor;
    private final Font renderFont;
    private final int startOffset;
    private final int endOffset;
    private String displayText;
    private boolean isHovered = false;
    private final String extension;
    private final String xmlRootTagName;

    private Inlay<?> inlay;   // 保存 Inlay 引用

    public EditableHtmlFoldingRenderer(@NotNull String originalText, @NotNull Editor editor, int startOffset, int endOffset) {
        this.originalText = originalText;
        this.editor = editor;
        this.renderFont = resolveFont(editor);
        this.startOffset = startOffset;
        this.endOffset = endOffset;

        // 提取 key
        String extractedKey = "";
        String extension = "";
        String xmlRootTagName = "";
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (virtualFile != null) {
            extension = virtualFile.getExtension();
            Pattern[] patterns = Constants.getPatternsByExtension(extension);

            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(originalText);
                if (matcher.find()) {
                    extractedKey = matcher.group(1);
                    break;
                }
            }

            // 获取 xml 根标签名
            Project project = editor.getProject();
            if (project != null) {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                if (psiFile instanceof XmlFile) {
                    XmlTag rootTag = ((XmlFile) psiFile).getRootTag();
                    if (rootTag != null) {
                        xmlRootTagName = rootTag.getName();
                    }
                }
            }
        }
        this.extension = extension;
        this.xmlRootTagName = xmlRootTagName;

        this.key = extractedKey;

        // 获取显示文本
        this.displayText = getI18nValue();
    }

    private static @NotNull MyTranslateDialogWrapper createMyTranslateDialogWrapper(@NotNull Project project, Module module, String text, EditableHtmlFoldingRenderer renderer) {
        MyTranslateDialogWrapper myTranslateDialogWrapper = new MyTranslateDialogWrapper(project, module);
        if (text != null) {
            myTranslateDialogWrapper.setSourceCHSText(text);
        }

        if ("xml".equalsIgnoreCase(renderer.getExtension()) &&
            ("DataGrid".equalsIgnoreCase(renderer.getXmlRootTagName()) ||
             "ViewDefine".equalsIgnoreCase(renderer.getXmlRootTagName()))) {
            // 根据 value 复用 key 的方法
            // 查datagrid的i18n
            myTranslateDialogWrapper.setGetI18nPropertiesFun(value -> {
                List<Property> dataGridI18nPropertiesByValue = MyPropertiesUtil.findModuleDataGridI18nPropertiesByValue(
                        project, module, value);
                return dataGridI18nPropertiesByValue;
            });
            // 写入 .properties 文件前检查 key 的方法
            myTranslateDialogWrapper.setCheckI18nKeyExistsFun(
                    key -> !(MyPropertiesUtil.findModuleDataGridI18nProperties(project, module, key).isEmpty())
            );
        } else {
            // 根据 value 复用 key 的方法
            // 查module和web的i18n
            myTranslateDialogWrapper.setGetI18nPropertiesFun(value -> {
                List<Property> moduleWebI18nPropertiesByValue = new ArrayList<>(
                        MyPropertiesUtil.findModuleI18nPropertiesByValue(project, module, value));
                moduleWebI18nPropertiesByValue.addAll(
                        MyPropertiesUtil.findModuleWebI18nPropertiesByValue(project, module, value));
                return moduleWebI18nPropertiesByValue;
            });
            // 写入 .properties 文件前检查 key 的方法
            myTranslateDialogWrapper.setCheckI18nKeyExistsFun(
                    key -> !(MyPropertiesUtil.findModuleI18nProperties(project, module, key).isEmpty() &&
                             MyPropertiesUtil.findModuleWebI18nProperties(project, module, key).isEmpty()));
        }

        return myTranslateDialogWrapper;
    }

    // 在 Inlay 创建成功后调用
    public void bindInlay(Inlay<?> inlay) {
        this.inlay = inlay;
    }

    // 提取字体选择逻辑
    private Font resolveFont(Editor editor) {
        Font baseFont = editor.getColorsScheme().getFont(EditorFontType.PLAIN);

        // 示例：检测是否能显示中文字符“测”
        if (!baseFont.canDisplay('测')) {
            // fallback 到中文字体（你可以换成 PingFang SC / Noto Sans CJK 等）
            String[] fontNames = {"Microsoft YaHei", "PingFang SC", "Noto Sans CJK", "SimSun", "Arial"};
            for (String fontName : fontNames) {
                Font font = new Font(fontName, baseFont.getStyle(), baseFont.getSize());
                if (font.canDisplay('✎')) {
                    return font;
                }
            }
            for (String fontName : fontNames) {
                Font font = new Font(fontName, baseFont.getStyle(), baseFont.getSize());
                if (font.canDisplay('测')) {
                    return font;
                }
            }
        }
        return baseFont;
    }

    private String getI18nValue() {
        if (key.isEmpty()) return originalText;

        // 1. 获取项目实例
        Project project = editor.getProject();
        if (project == null) {
            return originalText;
        }

        // 2. 从编辑器文档获取虚拟文件
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (virtualFile == null) {
            return originalText;
        }

        // 3. 使用 PsiManager 获取 PsiFile
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile == null) {
            return originalText;
        }

        // 4. 现在可以使用 PsiFile 的 findElementAt 方法了
        PsiElement element = psiFile.findElementAt(startOffset);
        if (element == null) return "${" + key + "}";

        Module module = MyPsiUtil.getModuleByPsiElement(element);
        if (module == null) return "${" + key + "}";

        // 默认显示 key
        String defaultVal = "${" + key + "}";

        switch (extension.toLowerCase()) {
            case "html":
            case "ftl":
            case "htm":
            case "js":
            case "java":
            case "jsp":
                // 查module和web的i18n
                String val = MyPropertiesUtil.findModuleWebI18nPropertyValue(project, module, key);
                if (isNotEmpty(val)) return val;

                return defaultVal;

            case "xml":
                if (!(psiFile instanceof XmlFile)) return defaultVal;
                XmlFile xmlFile = (XmlFile) psiFile;
                XmlTag rootTag = xmlFile.getRootTag();
                if (rootTag == null) return defaultVal;

                String rootTagName = rootTag.getName();
                if ("mapper".equalsIgnoreCase(rootTagName)) {
                    // Imp***Mapper
                    String moduleWebI18nPropertyValue = MyPropertiesUtil.findModuleWebI18nPropertyValue(project, module, key);
                    if (isNotEmpty(moduleWebI18nPropertyValue)) return moduleWebI18nPropertyValue;
                } else if ("DataGrid".equalsIgnoreCase(rootTagName) || "ViewDefine".equalsIgnoreCase(rootTagName)) {
                    // Layout
                    String moduleDataGridI18nPropertyValue = MyPropertiesUtil.findModuleDataGridI18nPropertyValue(project, module, key);
                    if (isNotEmpty(moduleDataGridI18nPropertyValue)) return moduleDataGridI18nPropertyValue;
                }
                return defaultVal;

            default:
                return defaultVal;
        }

    }

    @Override
    public int calcWidthInPixels(@NotNull Inlay inlay) {
        FontMetrics metrics = editor.getComponent().getFontMetrics(renderFont);
        return metrics.stringWidth(displayText) + (isHovered ? 40 : 20);
    }

    @Override
    public void paint(@NotNull Inlay inlay, @NotNull Graphics g, @NotNull Rectangle r, @NotNull TextAttributes textAttributes) {
        Graphics2D g2d = (Graphics2D) g.create();

        try {
            // 抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 背景色 - 区分 i18n 值和 key
            Color backgroundColor = getColor();

            // 绘制背景
            g2d.setColor(backgroundColor);
            g2d.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);

            // 绘制边框
            g2d.setColor(isHovered ? JBColor.BLUE : JBColor.GRAY);
            g2d.setStroke(new BasicStroke(isHovered ? 2.0f : 1.0f));
            g2d.drawRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);

            // 绘制文本
            g2d.setColor(JBColor.DARK_GRAY);
            // g2d.setFont(editor.getColorsScheme().getFont(EditorFontType.PLAIN));

            g2d.setFont(renderFont);

            FontMetrics metrics = g2d.getFontMetrics();
            int textY = r.y + (r.height + metrics.getAscent()) / 2 - 2;

            // 限制文本长度
            String renderText = displayText.length() > 50 ? displayText.substring(0, 47) + "..." : displayText;
            g2d.drawString(renderText, r.x + 10, textY);

            // 悬停时显示编辑图标
            if (isHovered) {
                String hintText = "✎" + this.displayText;
                FontMetrics fm = g2d.getFontMetrics();

                int textWidth = fm.stringWidth(hintText);
                int lineHeight = editor.getLineHeight();

                int paddingX = 6;
                int paddingY = 2;

                int boxX = r.x;
                int boxY = r.y; // 行的顶部

                int boxWidth = textWidth + paddingX * 2;
                int boxHeight = lineHeight - 2; // 与行高一致，避免遮挡

                // 背景（不透明按钮）
                g2d.setColor(new JBColor(new Color(0x1D52D2), new Color(0xFF1B53CB, true)));
                g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 8, 8);

                // 边框
                g2d.setColor(new JBColor(new Color(0x4A7AFF), new Color(0x5C85FF)));
                g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 8, 8);

                // 文字
                g2d.setColor(new JBColor(Color.WHITE, Color.WHITE));
                g2d.drawString(hintText, boxX + paddingX, boxY + fm.getAscent() + paddingY);
            }

        } finally {
            g2d.dispose();
        }
    }

    private @NotNull Color getColor() {
        // 未找到 i18n 值 - Warning
        JBColor warningBg = new JBColor(
                new Color(255, 208, 160),   // Light
                new Color(198, 35, 32, 113)      // Dark
        );

        // 找到 i18n 值 - Success
        JBColor successBg = new JBColor(
                new Color(176, 255, 176),   // Light
                new Color(40, 59, 100, 56)      // Dark
        );

        // hover 状态
        JBColor warningHover = new JBColor(
                new Color(255, 180, 120),
                new Color(160, 90, 50)
        );

        JBColor successHover = new JBColor(
                new Color(120, 255, 120),
                new Color(20, 80, 20)
        );

        if (displayText.startsWith("${") && displayText.endsWith("}")) {
            return isHovered ? warningHover : warningBg;
        } else {
            return isHovered ? successHover : successBg;
        }
    }

    public void showEditDialog() {
        Project project = editor.getProject();
        if (project == null) return;

        // 创建自定义对话框，显示更多信息
        String message = String.format(
                "原始标签: %s\n\nKey: %s\n\n当前显示值: %s\n\n" + "请输入新的显示文本（或者直接修改 properties 文件）:",
                originalText, key, displayText
        );

        String newText = Messages.showInputDialog(project, message, "修改资源串内容",
                                                  Messages.getQuestionIcon(),
                                                  displayText, null
        );

        if (newText != null && !newText.trim().isEmpty()) {
            updateDisplayText(newText.trim());
        }
    }

    public void showTranslateDialog() {
        updateDisplayText(displayText.replaceAll("^\\$\\{|}$", ""));
    }

    private void updateDisplayText(String newText) {
        Project project = editor.getProject();
        if (project == null) return;

        WriteCommandAction.runWriteCommandAction(project, "更新折叠显示文本", null, () -> {
            // 这里可以选择：
            // 1. 仅更新显示文本（临时）
            // 2. 更新对应的 properties 文件（持久）

            // 方案1：仅更新显示（临时）
            // this.displayText = newText;

            // 方案2：如果需要更新 properties 文件，可以这样做：
            updatePropertiesFile(newText);

            // 关键：更新 inlay 宽度和重绘
            if (inlay != null && inlay.isValid()) {
                inlay.update();
            }

            // 刷新编辑器显示
            editor.getComponent().repaint();
        });
    }

    /**
     * 可选：更新对应的 properties 文件
     */
    private void updatePropertiesFile(String newValue) {
        if (key.isEmpty()) return;

        // 1. 获取项目实例
        Project project = editor.getProject();
        if (project == null) {
            return;
        }

        // 2. 从编辑器文档获取虚拟文件
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (virtualFile == null) {
            return;
        }

        // 3. 使用 PsiManager 获取 PsiFile
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile == null) {
            return;
        }

        PsiElement element = psiFile.findElementAt(startOffset);
        if (element == null) return;

        Module module = MyPsiUtil.getModuleByPsiElement(element);
        if (module == null) return;

        MyTranslateDialogWrapper myTranslateDialogWrapper = createMyTranslateDialogWrapper(project, module, newValue, this);
        if (!newValue.startsWith("com")) {
            myTranslateDialogWrapper.clickTranslateButton();
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            if (myTranslateDialogWrapper.showAndGet()) {
                MyTranslateDialogWrapper.InputModel inputModel = myTranslateDialogWrapper.getInputModel();
                // String key = inputModel.getPropertyKey();

                    /*XmlElementFactory xmlElementFactory = XmlElementFactory.getInstance(project);
                    XmlText xmlText = xmlElementFactory.createDisplayText("<@message key=\"" + key + "\"/>");
                    element.replace(xmlText);*/
                // TODO 修改源码替换key: 将原有被折叠的代码中旧的key替换成新的key
                String newKey = inputModel.getPropertyKey();
                String toReplaceText = originalText;
                /*if (HTML_PATTERN.matcher(originalText).find()) {
                    toReplaceText = "<@message key=\"" + newKey + "\"/>";
                } else if (Constants.HTML_MESSAGE_PATTERN.matcher(originalText).find()) {
                    toReplaceText = "<@message key=\"" + newKey + "\"/>";
                } else if (Constants.JS_I18N_PATTERN.matcher(originalText).find()){
                    toReplaceText = "zhiyin.i18n.translate(\"" + newKey + "\")";
                } else {
                    toReplaceText = originalText;
                }*/
                // 只替换匹配到的文本中的 key
                Pattern[] patternsByExtension = Constants.getPatternsByExtension(extension);
                for (Pattern pattern : patternsByExtension) {
                    Matcher matcher = pattern.matcher(originalText);
                    if (matcher.find()) {
                        if (matcher.groupCount() >= 1) {
                            String oldKey = matcher.group(1);
                            // 按捕获组替换
                            toReplaceText =  originalText.replaceFirst(
                                    Pattern.quote(oldKey),
                                    Matcher.quoteReplacement(newKey)
                            );
                            break;
                        }
                    }
                }

                String newFoldingText = toReplaceText;

                if (!inputModel.getPropertyKeyExists()) {
                    boolean isNative2AsciiForPropertiesFiles = MyPropertiesUtil.isNative2AsciiForPropertiesFiles();
                    String chsValue = inputModel.getChinese();
                    String chtValue = inputModel.getChineseTW();
                    String enValue = inputModel.getEnglish();
                    String viValue = inputModel.getVietnamese();
                    if (!isNative2AsciiForPropertiesFiles) {
                        chsValue = inputModel.getChineseUnicode();
                        chtValue = inputModel.getChineseTWUnicode();
                        viValue = inputModel.getVietnameseUnicode();
                    }

                    String finalChsValue = chsValue;
                    String finalChtValue = chtValue;
                    String finalViValue = viValue;
                    WriteCommandAction.runWriteCommandAction(project, () -> {

                        if ("xml".equalsIgnoreCase(extension) && ("DataGrid".equalsIgnoreCase(xmlRootTagName) ||
                                                                  "ViewDefine".equalsIgnoreCase(xmlRootTagName))) {
                            MyPropertiesUtil.addPropertyToI18nFile(project, module,
                                                                   Constants.I18N_DATAGRID_ZH_CN_SUFFIX, newKey,
                                                                   finalChsValue
                            );
                            MyPropertiesUtil.addPropertyToI18nFile(project, module,
                                                                   Constants.I18N_DATAGRID_ZH_TW_SUFFIX, newKey,
                                                                   finalChtValue
                            );
                            MyPropertiesUtil.addPropertyToI18nFile(project, module,
                                                                   Constants.I18N_DATAGRID_EN_US_SUFFIX, newKey, enValue
                            );
                            MyPropertiesUtil.addPropertyToI18nFile(project, module,
                                                                   Constants.I18N_DATAGRID_VI_VN_SUFFIX, newKey,
                                                                   finalViValue
                            );

                            // 如果当前是在Title标签中，替换整个标签包含中英文和key
                            // 获取当前位置的PSI元素
                            createLayoutColumnTitleTag(project, element, inputModel, newFoldingText);
                            /*XmlElementFactory xmlElementFactory = XmlElementFactory.getInstance(project);
                            XmlText xmlText = xmlElementFactory.createDisplayText(
                                    "<Title value=\"" + key + "\" chs=\"" + finalChsValue + "\" eng=\"" + enValue +
                                    "\" />");
                            element.replace(xmlText);*/
                        } else {
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_ZH_CN_SUFFIX, newKey,
                                                                   finalChsValue
                            );
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_ZH_TW_SUFFIX, newKey,
                                                                   finalChtValue
                            );
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_EN_US_SUFFIX, newKey,
                                                                   enValue
                            );
                            MyPropertiesUtil.addPropertyToI18nFile(project, module, Constants.I18N_VI_VN_SUFFIX, newKey,
                                                                   finalViValue
                            );

                            editor.getDocument().replaceString(startOffset, endOffset, newFoldingText);
                        }

                        // 替换后，更新 displayText 和 inlay 宽度
//                        this.displayText = finalChsValue;
//
//                        if (inlay != null && inlay.isValid()) {
//                            inlay.update();
//                        }
                    });
                } else {
                    WriteCommandAction.runWriteCommandAction(project, () -> {

                        if ("xml".equalsIgnoreCase(extension) && ("DataGrid".equalsIgnoreCase(xmlRootTagName) ||
                                                                  "ViewDefine".equalsIgnoreCase(xmlRootTagName))) {
                            createLayoutColumnTitleTag(project, element, inputModel, newFoldingText);
                        } else {
                            editor.getDocument().replaceString(startOffset, endOffset, newFoldingText);
                        }

                        // 替换后，更新 displayText 和 inlay 宽度
//                        this.displayText = inputModel.getChinese();
//
//                        if (inlay != null && inlay.isValid()) {
//                            inlay.update();
//                        }
                    });
                }

                // 替换后，更新 displayText 和 inlay 宽度
                this.displayText = inputModel.getChinese();

                if (inlay != null && inlay.isValid()) {
                    inlay.update();
                }
            } else {
                MyPluginMessages.showInfo("已取消", project);
            }

        });

    }

    private void createLayoutColumnTitleTag(Project project, PsiElement element, MyTranslateDialogWrapper.InputModel inputModel, String newFoldingText) {
        XmlTag xmlTag = PsiTreeUtil.getParentOfType(element, XmlTag.class);
        if (xmlTag != null && "Title".equalsIgnoreCase(xmlTag.getName())) {
            String newTagText = "<Title value=\"" + inputModel.getPropertyKey() + "\" chs=\"" +
                                inputModel.getChinese() + "\" eng=\"" + inputModel.getEnglish() +
                                "\"/>";

            XmlTag newTag = XmlElementFactory.getInstance(project).createTagFromText(newTagText);

            xmlTag.replace(newTag);
        } else {
            editor.getDocument().replaceString(startOffset, endOffset, newFoldingText);
        }
    }

    // 鼠标悬停状态管理
    public void setHovered(boolean hovered) {
        if (this.isHovered != hovered) {
            this.isHovered = hovered;
            editor.getComponent().repaint();
        }
    }

    public String getKey() {
        return key;
    }

    public String getOriginalText() {
        return originalText;
    }

    public String getExtension() {
        return extension;
    }

    public String getXmlRootTagName() {
        return xmlRootTagName;
    }
}