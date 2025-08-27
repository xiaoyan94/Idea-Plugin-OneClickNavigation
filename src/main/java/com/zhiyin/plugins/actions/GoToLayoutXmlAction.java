package com.zhiyin.plugins.actions;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.PsiNavigateUtil;
import com.zhiyin.plugins.notification.MyPluginMessages;
import org.jetbrains.annotations.NotNull;

public class GoToLayoutXmlAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent event) {
        PsiFile file = event.getData(CommonDataKeys.PSI_FILE);
        event.getPresentation().setEnabledAndVisible(file != null && ("HTML".equalsIgnoreCase(file.getVirtualFile().getExtension()) || "XML".equalsIgnoreCase(file.getVirtualFile().getExtension())));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        PsiElement element = event.getData(CommonDataKeys.PSI_ELEMENT);
        String gridName = null;
        if (element instanceof JSProperty){
            String value = element.toString();
            if (String.valueOf(value).endsWith("Grid") || String.valueOf(value).endsWith("grid")){
                if (value.contains(":")) {
                    gridName = value.split(":")[1];
                }
            }
        } else {
            Editor editor = event.getData(CommonDataKeys.EDITOR);
            if (editor != null) {
                if (editor.getSelectionModel().hasSelection()) {
                    String selectedText = editor.getSelectionModel().getSelectedText();
                    if (selectedText != null && selectedText.toLowerCase().endsWith("grid")) {
                        gridName = selectedText;
                    }
                }
            }
        }

        PsiFile psiFile = event.getRequiredData(CommonDataKeys.PSI_FILE);
        navigateToLayoutXml(psiFile,  gridName);
    }

    private void navigateToLayoutXml(@NotNull PsiElement element, String gridName) {
        // Get the file path of the current element
        String filePath = element.getContainingFile().getVirtualFile().getPath();

        if (filePath.contains("layout")) {
            navigateToLayoutHTML(element.getContainingFile(), null);
        }

        // Replace the path to find the corresponding layout XML file
        filePath = filePath.replace("WEB-INF/view/MesRoot/", "WEB-INF/etc/business/layout/");
        filePath = filePath.replace(".html", ".xml");

        // Find the layout XML file
        VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl("file://" + filePath);
        if (virtualFile == null) {
            // 提示用户: 未找到对应的layout xml文件
            MyPluginMessages.showInfo("[VirtualFile]未找到对应的layout xml文件", element.getProject());
            return;
        }
        PsiFile psiFile = PsiManager.getInstance(element.getProject()).findFile(virtualFile);
        if (psiFile == null) {
            MyPluginMessages.showInfo("[PsiFile]未找到对应的layout xml文件", element.getProject());
            return;
        }

        if (gridName != null) {
            if (psiFile instanceof XmlFile) {
                XmlFile xmlFile = (XmlFile) psiFile;
                // Find the <DataGrid> tag with the specified id attribute
                XmlTag rootTag = xmlFile.getRootTag();
                if (rootTag != null) {
                    XmlTag[] tags = rootTag.getSubTags();
                    for (XmlTag tag : tags) {
                        if ("DataGrid".equals(tag.getName()) && gridName.equals(tag.getAttributeValue("id"))) {
                            // Found the tag, now jump to it
                            PsiNavigateUtil.navigate(tag, true);
                            return;
                        }
                    }
                }
            }
        }

        // Navigate to the layout XML file
        psiFile.navigate(true);
    }

    private void navigateToLayoutHTML(@NotNull PsiFile psiFile, String gridName) {
        // Get the file path of the current element
        String filePath = psiFile.getVirtualFile().getPath();

        // Replace the path to find the corresponding layout XML file
        filePath = filePath.replace("WEB-INF/etc/business/layout/", "WEB-INF/view/MesRoot/");
        filePath = filePath.replace(".xml", ".html");

        // Find the layout XML file
        VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl("file://" + filePath);
        if (virtualFile == null) {
            // 提示用户: 未找到对应的layout xml文件
            MyPluginMessages.showInfo("[VirtualFile]未找到对应的layout html文件", psiFile.getProject());
            return;
        }
        PsiFile htmlPsiFile = PsiManager.getInstance(psiFile.getProject()).findFile(virtualFile);
        if (htmlPsiFile == null) {
            MyPluginMessages.showInfo("[PsiFile]未找到对应的layout html文件", psiFile.getProject());
            return;
        }

        /*if (gridName != null) {

        }*/

        // Navigate to the layout XML file
        htmlPsiFile.navigate(true);
    }

}