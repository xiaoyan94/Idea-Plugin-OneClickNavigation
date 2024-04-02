package com.zhiyin.plugins.oneClickNavigation.codeInsight;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.zhiyin.plugins.oneClickNavigation.xml.domElements.Mapper;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.settings.AppSettingsState;
import com.zhiyin.plugins.utils.MyPsiUtil   ;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 从 MyBatis 的 Mapper XML 文件跳转到 Java 文件
 *
 * @author yan on 2024/03/20
 */
public class MapperXmlLineMarkerProvider extends RelatedItemLineMarkerProvider {

    /**
     * @param element
     * @param result
     */
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {

        if (! (element instanceof XmlAttribute)) {
            return;
        }

        XmlAttribute xmlAttribute =  (XmlAttribute) element;
        if (!xmlAttribute.getName().equals("id")) {
            return;
        }

        PsiFile psiFile = xmlAttribute.getContainingFile();
        if (psiFile == null || psiFile.getContainingDirectory() == null || psiFile.getContainingDirectory().getName().contains("classes")) {
            return ;
        }

        if (!(psiFile instanceof XmlFile)) {
            return;
        }
        XmlFile xmlFile = (XmlFile) psiFile;

        Project project = element.getProject();
        DomManager domManager = DomManager.getDomManager(project);

        DomFileElement<Mapper> domFileElement = domManager.getFileElement(xmlFile, Mapper.class);
        if (domFileElement == null) {
            return;
        }

        Mapper mapper = domFileElement.getRootElement();
        String namespace = mapper.getNamespace().getValue();
        if (namespace == null || namespace.isEmpty()) {
            return;
        }

        String methodNameValue = xmlAttribute.getValue();
        if (methodNameValue == null || methodNameValue.isEmpty()) {
            return;
        }

        GlobalSearchScope searchScope = GlobalSearchScope.allScope(project);
        if(Constants.SCOPE_NAME_MODULE.equals(AppSettingsState.getInstance().mapperToDaoModuleScope)){
            Module module = MyPsiUtil.getModuleByPsiElement(element);
            if (module != null) {
                searchScope = GlobalSearchScope.moduleScope(module);
            }
        }
        PsiClass[] psiClasses = JavaPsiFacade.getInstance(project).findClasses(namespace, searchScope);

        List<PsiIdentifier> targets = new ArrayList<>();

        for (PsiClass psiClass : psiClasses) {
            PsiMethod[] psiMethods = psiClass.findMethodsByName(methodNameValue, false);
            if (psiMethods.length > 0) {
                targets.addAll(Arrays.stream(psiMethods).filter(Objects::nonNull).map(PsiMethod::getNameIdentifier).collect(Collectors.toList()));
            }
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
                .setTooltipText(Constants.NAVIGATE_TO_DAO_INTERFACE);

        PsiElement firstChild = element.getFirstChild();
        RelatedItemLineMarkerInfo<PsiElement> relatedItemLineMarkerInfo = builder.createLineMarkerInfo(firstChild == null ? element : firstChild);
        result.add(relatedItemLineMarkerInfo);

    }
}
