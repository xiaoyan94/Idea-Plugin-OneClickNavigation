package com.zhiyin.plugins.provider.lineMarkers;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.zhiyin.plugins.resources.MyIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class JSLayoutRelatedItemLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    public void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<?
            super RelatedItemLineMarkerInfo<?>> result) {
        if (element instanceof JSVariable) {
            JSVariable variable = (JSVariable) element;
            if ("dataGridMap".equals(variable.getName())) {
                PsiElement firstChild = element.getFirstChild();
                System.out.println("firstChild: " + firstChild + ", isLeaf: " + (firstChild instanceof LeafPsiElement));
                registerControllerUrlLineMarker(element, result, "", firstChild != null ? firstChild : element);
            }
        }
    }

    /**
     * 添加跳转图标
     * @param element 变量
     * @param result 结果集
     * @param stringValue url
     * @param registerElement 要注册行标记器的叶子节点
     */
    @SuppressWarnings("unused")
    public static void registerControllerUrlLineMarker(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result, String stringValue, @NotNull PsiElement registerElement) {
        // 获取element所在文件完整路径
        // WEB-INF/view/MesRoot/Produce/DutyRecord.html
        String filePath = element.getContainingFile().getVirtualFile().getPath();
        // 替换为
        // WEB-INF/etc/business/layout/Produce/DutyRecord.xml
        filePath = filePath.replace("WEB-INF/view/MesRoot/", "WEB-INF/etc/business/layout/");
        filePath = filePath.replace(".html", ".xml");

        // 获取filePath表示的PsiFile
        VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl("file://" + filePath);
        if (virtualFile == null) {
            return;
        }
        PsiFile psiFile = PsiManager.getInstance(element.getProject()).findFile(virtualFile);
        if (psiFile == null) {
            return;
        }

        if (!result.isEmpty()){
            System.out.println("result is not empty: " + result);
            result.clear();
        }

        // 创建导航图标并添加到结果集中
        NavigationGutterIconBuilder<PsiElement> builder;
        builder = NavigationGutterIconBuilder
                .create(MyIcons.pandaIconSVG16_2)
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .setTarget(psiFile.getNavigationElement())
                .setTooltipText("跳转到Layout文件");
        RelatedItemLineMarkerInfo<PsiElement> relatedItemLineMarkerInfo = builder.createLineMarkerInfo(registerElement);
        result.add(relatedItemLineMarkerInfo);
    }
}
