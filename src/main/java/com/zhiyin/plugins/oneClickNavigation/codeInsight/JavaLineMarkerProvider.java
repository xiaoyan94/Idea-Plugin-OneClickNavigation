package com.zhiyin.plugins.oneClickNavigation.codeInsight;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
// 使用 PsiClass 需要在 gradle 中添加 plugins.set(listOf("com.intellij.java"))
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.*;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.oneClickNavigation.xml.domElements.Mapper;
import com.zhiyin.plugins.oneClickNavigation.xml.domElements.Statement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.diagnostic.Logger;

import java.util.*;

/**
 * 从 Java 代码导航跳转到 MyBatis 的 Mapper XML 文件
 *
 * @author yan on 2024/2/25 01:30
 */
public class JavaLineMarkerProvider extends RelatedItemLineMarkerProvider {

    private static final Logger LOG = Logger.getInstance(JavaLineMarkerProvider.class);
    @Override
    public void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<?
            super RelatedItemLineMarkerInfo<?>> result) {

        /*if (element instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression psiMethodCallExpression = (PsiMethodCallExpression) element;
            PsiReferenceExpression psiReferenceExpression = psiMethodCallExpression.getMethodExpression();

        }

        if (element instanceof PsiLiteralExpression) {
            PsiLiteralExpression psiLiteralExpression = (PsiLiteralExpression) element;
            collectNavigationMarkersWithPsiLiteralExpressionElement(psiLiteralExpression, result);
            return;
        }*/

        // 判断当前元素是否为接口方法
        if (!(element instanceof PsiIdentifier)) {
            return;
        }

        PsiElement elementParent = element.getParent();

        // 从父元素中获取接口 PsiClass 对象
        PsiClass anInterface = this.getInterface(elementParent);
        if (null == anInterface) {
            return;
        }

        // 获取接口的完全限定名
        final String qualifiedName = anInterface.getQualifiedName();
        LOG.info("接口:" + qualifiedName);

        // 获取当前项目
        final Project project = anInterface.getProject();
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);

        // 尝试根据模块缩小搜索范围
        Module module = ModuleUtilCore.findModuleForPsiElement(elementParent);
        if (null != module) {
//            scope = GlobalSearchScope.moduleScope(module);
        }

        // 搜索项目或模块中所有 Mapper 类型的 XML 文件
        DomService domService = DomService.getInstance();

        List<DomFileElement<Mapper>> xmlFiles = domService.getFileElements(Mapper.class, project,
                scope);

        LOG.info("xmlFiles list size:"+xmlFiles.size());

        if (xmlFiles.isEmpty()) {
            Collection<VirtualFile> files1 = DomFileIndex.findFiles("mapper", "", scope);
            Collection<VirtualFile> files2 = DomFileIndex.findFiles("mapper", null, scope);
            Collection<VirtualFile> files3 = DomFileIndex.findFiles("mapper", "-NULL-", scope);
            Collection<VirtualFile> files4 = DomFileIndex.findFiles("mapper", "://mybatis-3-mapper.dtd", scope);
            Collection<VirtualFile> files5 = DomFileIndex.findFiles("mapper",
                    Constants.MYBATIS_POSSIBLE_NAMESPACES[0], scope);
            Collection<VirtualFile> files6 = DomFileIndex.findFiles("mapper",
                    "-//mybatis.org//DTD Mapper 3.0//EN", scope);
            Collection<VirtualFile> files7 = DomFileIndex.findFiles("mapper",
                    "mybatis-3-mapper.dtd", scope);
            System.out.println("files1:"+files1.size());
            System.out.println("files2:"+files2.size());
            System.out.println("files3:"+files3.size());
            System.out.println("files4:"+files4.size());
            System.out.println("files5:"+files5.size());
            System.out.println("files6:"+files6.size());
            System.out.println("files7:"+files7.size());
            return;
        }

        Collection<VirtualFile> files1 = DomFileIndex.findFiles("mapper", "", scope);
        Collection<VirtualFile> files2 = DomFileIndex.findFiles("mapper", null, scope);
        Collection<VirtualFile> files3 = DomFileIndex.findFiles("mapper", "-NULL-", scope);
        Collection<VirtualFile> files4 = DomFileIndex.findFiles("mapper", "://mybatis-3-mapper.dtd", scope);
        Collection<VirtualFile> files5 = DomFileIndex.findFiles("mapper",
                Constants.MYBATIS_POSSIBLE_NAMESPACES[0], scope);
        Collection<VirtualFile> files6 = DomFileIndex.findFiles("mapper",
                "-//mybatis.org//DTD Mapper 3.0//EN", scope);
        Collection<VirtualFile> files7 = DomFileIndex.findFiles("mapper",
                "mybatis-3-mapper.dtd", scope);
        System.out.println("files1:"+files1.size());
        System.out.println("files2:"+files2.size());
        System.out.println("files3:"+files3.size());
        System.out.println("files4:"+files4.size());
        System.out.println("files5:"+files5.size());
        System.out.println("files6:"+files6.size());
        System.out.println("files7:"+files7.size());

        // 过滤不符合命名空间的 XML 文件
        xmlFiles.removeIf(e -> {
            Mapper mapper = e.getRootElement();
            GenericAttributeValue<String> namespace = mapper.getNamespace();
            return namespace == null || namespace.getRawText() == null || namespace.getRawText().isEmpty() || !namespace.getRawText().equals(qualifiedName);
        });

        // 如果没有找到符合条件的 XML 文件，则返回
        if (xmlFiles.isEmpty()) {
            return;
        }

        // 准备一个列表来存储找到的 XML 文件中的标签
        List<XmlTag> xmlTags = new ArrayList<>(xmlFiles.size() * 16);

        // 遍历每个找到的 XML 文件
        for (DomFileElement<Mapper> xml : xmlFiles) {
            Mapper mapper = xml.getRootElement();
            GenericAttributeValue<String> namespace = mapper.getNamespace();
            if (namespace == null || namespace.getRawText() == null || namespace.getRawText().isEmpty() || !namespace.getRawText().equals(qualifiedName)) {
                continue;
            }

            // 根据命名空间和 Java 元素类型，找到对应的 XML 标签
            if (elementParent instanceof PsiClass) {
                // mapper 标签
                xmlTags.add(namespace.getXmlTag());
            } else if (elementParent instanceof PsiMethod) {
                // insert/select/delete/update 标签
                List<Statement> statements = mapper.getStatements();
                if (statements == null || statements.isEmpty()) {
                    continue;
                }
                PsiMethod psiMethod = (PsiMethod) elementParent;
                final String method = psiMethod.getName();
                for (Statement statement : statements) {
                    GenericAttributeValue<String> id = statement.getId();
                    if (id == null || id.getRawText() == null || id.getRawText().isEmpty()) {
                        continue;
                    }
                    if (id.getRawText().equals(method)) {
                        xmlTags.add(statement.getXmlTag());
                    }
                }
            }
        }

        // 如果没有找到任何匹配的标签，则返回
        if (xmlTags.isEmpty()) {
            return;
        }

        // 创建导航图标并添加到结果集中
        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                .create(MyIcons.rightArrow)
                .setAlignment(GutterIconRenderer.Alignment.RIGHT)
                .setTargets(xmlTags)
                .setTooltipText("跳转到XML");
        result.add(builder.createLineMarkerInfo(element));
    }

    /**
     * 处理queryDaoDataT方法<br/>
     *
     * 处理queryDaoDataT(Class clazz, Object object, String method, Map params)
     * @param element
     * @param result
     */
    private void collectNavigationMarkersWithPsiLiteralExpressionElement(PsiLiteralExpression element, Collection<?
            super RelatedItemLineMarkerInfo<?>> result) {

        PsiMethodCallExpression psiMethodCallExpression = PsiTreeUtil.getParentOfType(element.getOriginalElement(),
                PsiMethodCallExpression.class);

        if (null == psiMethodCallExpression){
            return;
        }

        // 字符串字面量所在的方法引用名
        PsiReferenceExpression psiReferenceExpression = psiMethodCallExpression.getMethodExpression();
        String referenceName = psiReferenceExpression.getReferenceName();

        if (! "queryDaoDataT".equals(referenceName)) {
            return;
        }

        if (!(element.getValue() instanceof String)) {
            return;
        }

        // 获取标识符的父元素
        PsiElement parent = element.getParent();
        if (null == parent) {
            return;
        }

        // 字符串字面量
        String daoMethodName = (String) element.getValue();

        // 拼接dao接口的完全限定名
        // queryDaoDataT(Class clazz, Object object, String method, Map params)
        PsiExpressionList psiExpressionList = psiMethodCallExpression.getArgumentList();
        if (psiExpressionList.getExpressionCount() < 4){
            // 参数小于 4 个
            return;
        }

        // 第二个参数类型
        PsiType psiType = psiExpressionList.getExpressionTypes()[1];
        // 类名"IUserDao"
        String canonicalText = psiType.getCanonicalText();

        // 获取当前项目
        final Project project = element.getProject();
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);

        // 尝试根据模块缩小搜索范围
        Module module = ModuleUtilCore.findModuleForPsiElement(element);
        if (null != module) {
            scope = GlobalSearchScope.moduleScope(module);
        }

        // 从父元素中获取接口 PsiClass 对象
//        PsiClass jif = this.getInterface(parent);
//        @NotNull PsiClass[] classesByName = PsiShortNamesCache.getInstance(project)
//                                                              .getClassesByName(canonicalText, scope);
//

//        if (null == jif) {
//            return;
//        }

        // 搜索项目或模块中所有 Mapper 类型的 XML 文件
        DomService domService = DomService.getInstance();
        List<DomFileElement<Mapper>> xmlFiles = domService.getFileElements(Mapper.class, project,
                scope);

        if (xmlFiles.isEmpty()) {
            return;
        }

        // 过滤不符合命名空间的 XML 文件
        xmlFiles.removeIf(e -> {
            Mapper mapper = e.getRootElement();
            GenericAttributeValue<String> namespace = mapper.getNamespace();
            return namespace == null || namespace.getRawText() == null || namespace.getRawText().isEmpty() || !namespace.getRawText().contains(canonicalText);
        });

        // 如果没有找到符合条件的 XML 文件，则返回
        if (xmlFiles.isEmpty()) {
            return;
        }

        // 准备一个列表来存储找到的 XML 文件中的标签
        List<XmlTag> xmlTags = new ArrayList<>(xmlFiles.size() * 16);

        // 遍历每个找到的 XML 文件
        for (DomFileElement<Mapper> xml : xmlFiles) {
            Mapper mapper = xml.getRootElement();
            GenericAttributeValue<String> namespace = mapper.getNamespace();
            if (namespace == null || namespace.getRawText() == null || namespace.getRawText().isEmpty() || !namespace.getRawText().contains(canonicalText)) {
                continue;
            }

            // 根据命名空间和 Java 元素类型，找到对应的 XML 标签
            if (parent instanceof PsiClass) {
                // mapper 标签
                xmlTags.add(namespace.getXmlTag());
            } else {
                // insert/select/delete/update 标签
                List<Statement> statements = mapper.getStatements();
                if (statements == null || statements.isEmpty()) {
                    continue;
                }

                for (Statement statement : statements) {
                    GenericAttributeValue<String> id = statement.getId();
                    if (id == null || id.getRawText() == null || id.getRawText().isEmpty()) {
                        continue;
                    }
                    if (id.getRawText().equals(daoMethodName)) {
                        xmlTags.add(statement.getXmlTag());
                    }
                }
            }
        }

        // 如果没有找到任何匹配的标签，则返回
        if (xmlTags.isEmpty()) {
            return;
        }

        // 创建导航图标并添加到结果集中
        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                .create(MyIcons.rightArrow)
                .setAlignment(GutterIconRenderer.Alignment.RIGHT)
                .setTargets(xmlTags)
                .setTooltipText("跳转到XML");
        result.add(builder.createLineMarkerInfo(element));
    }

    /**
     * @return DAO接口(PsiClass),或者 null
     */
    @Nullable
    private PsiClass getInterface(@NotNull PsiElement element) {
        // java PsiIdentifier
        if (element instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) element;
            if (psiClass.isInterface()) {
                return psiClass;
            }
        }
        if (element instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) element;
            PsiClass psiClass = psiMethod.getContainingClass();
            if (null != psiClass && psiClass.isInterface()) {
                return psiClass;
            }
        }

        return null;
    }

}
