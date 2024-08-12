package com.zhiyin.plugins.oneClickNavigation.codeInsight;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.xml.GenericAttributeValue;
import com.zhiyin.plugins.oneClickNavigation.xml.domElements.Statement;
import com.zhiyin.plugins.oneClickNavigation.xml.utils.MyMapperUtils;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.service.MyProjectService;
import com.zhiyin.plugins.settings.AppSettingsState;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import p.K.M;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 从 Java 代码导航跳转到 MyBatis 的 Mapper XML 文件
 * TODO 拆分优化该类 分别收集 提升响应速度
 *
 */
public class JavaLineMarkerProvider extends RelatedItemLineMarkerProvider {

    private static final Logger LOG = Logger.getInstance(JavaLineMarkerProvider.class);

    @Override
    public void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<?
            super RelatedItemLineMarkerInfo<?>> result) {

        List<PsiElement> targets = new ArrayList<>();
        Project project = element.getProject();

        boolean anyCollected = collectDaoMethodCalledInServiceTarget(element, targets, project)
                || collectQueryDaoDataTMethodTarget(element, targets, project)
                || collectDaoInterfaceTarget(element, targets, project);

        if (anyCollected && !targets.isEmpty()) {
            targets.forEach(target -> {
                if (null == target) {
                    LOG.warn("target is null, ele:" + element.getText());
                }
            });
            targets.removeIf(Objects::isNull);
            if (Constants.SCOPE_NAME_MODULE.equals(AppSettingsState.getInstance().mapperToDaoModuleScope)) {
                targets.removeIf(targetEle -> !MyPsiUtil.isInSameModule(targetEle, element));
            }
            // 创建导航图标并添加到结果集中
            NavigationGutterIconBuilder<PsiElement> builder;
            builder = NavigationGutterIconBuilder
                    .create(MyIcons.pandaIconSVG16_2)
                    .setAlignment(GutterIconRenderer.Alignment.LEFT)
                    .setTargets(targets)
                    .setTooltipText(Constants.NAVIGATE_TO_MAPPER);
            RelatedItemLineMarkerInfo<PsiElement> relatedItemLineMarkerInfo = builder.createLineMarkerInfo(element);
            result.add(relatedItemLineMarkerInfo);
        }
    }


    /**
     * 处理Dao接口, 跳转到Mapper XML 文件
     *
     * @param element
     * @param targets
     */
    private boolean collectDaoInterfaceTarget(@NotNull PsiElement element, List<PsiElement> targets, Project project) {
        // 判断当前元素是否为接口方法
        if (!(element instanceof PsiIdentifier)) {
            return false;
        }

        PsiElement elementParent = element.getParent();

        // 从父元素中获取接口 PsiClass 对象
        PsiClass anInterface = this.getInterface(elementParent);
        if (null == anInterface) {
            return false;
        }

        // 获取接口的完全限定名
        final String qualifiedName = anInterface.getQualifiedName();
//        LOG.info("接口:" + qualifiedName);

        // 获取当前项目
//        final Project project = anInterface.getProject();
//        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);

        // 尝试根据模块缩小搜索范围
//        Module module = ModuleUtilCore.findModuleForPsiElement(elementParent);
//        if (null != module) {
//            scope = GlobalSearchScope.moduleScope(module);
//        }

        // 搜索项目或模块中所有 Mapper 类型的 XML 文件
//        Map<String, Mapper> xmlFileMap = project.getService(MyProjectService.class).getXmlFileMap();
//        LOG.info("xmlFileMap size:"+xmlFileMap.size());

//        Mapper daoMapper = xmlFileMap.get(qualifiedName);
//
//        if (null == daoMapper) {
//            return true;
//        }

        // 根据命名空间和 Java 元素类型，找到对应的 XML 标签
        if (elementParent instanceof PsiClass) {
            // mapper 标签
            targets.addAll(MyMapperUtils.getMapperElementsByNamespace(project, qualifiedName));
        } else if (elementParent instanceof PsiMethod) {
            // insert/select/delete/update 标签
            List<Statement> statements = MyMapperUtils.getStatementsByNamespace(project, qualifiedName);

            PsiMethod psiMethod = (PsiMethod) elementParent;
            final String method = psiMethod.getName();
            for (Statement statement : statements) {
                GenericAttributeValue<String> id = statement.getId();
                if (id == null || id.getValue() == null || id.getValue().isEmpty()) {
                    continue;
                }
                if (id.getValue().equals(method)) {
                    XmlAttributeValue xmlAttributeValue = id.getXmlAttributeValue();
                    if (xmlAttributeValue != null) {
                        targets.add(xmlAttributeValue);
                    }
                }
            }

        }
        return true;
    }

    /**
     * 处理queryDaoDataT方法<br/>
     * <p>
     * 处理queryDaoDataT(Class clazz, Object object, String method, Map params)
     */
    private boolean collectQueryDaoDataTMethodTarget(@NotNull PsiElement element, List<PsiElement> targets, Project project) {
        if (!(element instanceof PsiJavaToken)) {
            return false;
        }
        PsiMethodCallExpression psiMethodCallExpression = PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class);
        if (null == psiMethodCallExpression) {
            return false;
        }
        PsiReferenceExpression psiReferenceExpression = psiMethodCallExpression.getMethodExpression();
        // 适配bizCommonService.queryDaoDataT调用
        if (!psiReferenceExpression.getCanonicalText().endsWith(Constants.QUERY_DAO_METHOD_EXPRESSION)) {
            return false;
        }
        PsiLiteralExpression literalExpression;
        if (!(element.getParent() instanceof PsiLiteralExpression)) {
            return true;
        }
        literalExpression = (PsiLiteralExpression) element.getParent();
        String queryDaoMethodName = literalExpression.getValue() instanceof String ?
                (String) literalExpression.getValue() : null;
        if (null == queryDaoMethodName || queryDaoMethodName.isEmpty()) {
            return true;
        }
        PsiExpressionList argumentList = psiMethodCallExpression.getArgumentList();
        if (argumentList.getExpressionCount() < 4) {
            return true;
        }
        PsiType[] expressionTypes = argumentList.getExpressionTypes();
        PsiType secondExpressionType = expressionTypes[1];
        if (secondExpressionType == null) {
            return true;
        }
        Module module = MyPsiUtil.getModuleByPsiElement(element);
        PsiShortNamesCache psiShortNamesCache = PsiShortNamesCache.getInstance(project);
        GlobalSearchScope scope;
        if (module != null) {
            scope = GlobalSearchScope.moduleScope(module);
        } else {
            scope = GlobalSearchScope.allScope(project);
        }
        PsiClass[] classesByName = psiShortNamesCache.getClassesByName(secondExpressionType.getPresentableText(),
                scope);
        for (PsiClass psiClass : classesByName) {
            String classQualifiedName = psiClass.getQualifiedName();

            MyProjectService myProjectService = project.getService(MyProjectService.class);
//            Map<String, Mapper> xmlFileMap = myProjectService.getXmlFileMap();
//            Mapper mapper = xmlFileMap.get(classQualifiedName);
//            if (null != mapper) {
            List<Statement> statements = MyMapperUtils.getStatementsByNamespace(project, classQualifiedName);
            List<XmlAttributeValue> collected = statements.stream().filter(statement -> Objects.equals(queryDaoMethodName, statement.getId().getValue()))
                    .map(statement -> statement.getId().getXmlAttributeValue())
                    .collect(Collectors.toList());
//                for (XmlAttributeValue xmlAttributeValue : collected) {
//                    PsiElement[] children = xmlAttributeValue.getChildren();
//                    if (children.length > 0) {
//                        targets.add(children[0]);
//                    }
//                }
            targets.addAll(collected);
//            }

        }
        return true;
    }

    /**
     * 处理Service层调用的dao方法<br/>
     * <p>
     * 处理xxDao.queryXX(Map params)
     */
    private boolean collectDaoMethodCalledInServiceTarget(@NotNull PsiElement element, List<PsiElement> targets, Project project) {
        // queryXX
        if (!(element instanceof PsiIdentifier)) {
            return false;
        }
        PsiMethodCallExpression psiMethodCallExpression = PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class);
        if (null == psiMethodCallExpression || null == element.getParent() || element.getParent().getParent() != psiMethodCallExpression) {
            return false;
        }
        // xxDao.queryXX
        PsiReferenceExpression psiReferenceExpression = psiMethodCallExpression.getMethodExpression();
        if (psiReferenceExpression.getType() == null) {
            return false;
        }
        // xxDao
        PsiExpression qualifierExpression = psiReferenceExpression.getQualifierExpression();
        if (null == qualifierExpression) {
            return false;
        }
        PsiType qualifierExpressionType = qualifierExpression.getType();
        if (null == qualifierExpressionType) {
            return false;
        }
        // com.xx.xx.xxDao
        String refCanonicalText = qualifierExpressionType.getCanonicalText();

//        MyProjectService myProjectService = project.getService(MyProjectService.class);
//        Map<String, Mapper> xmlFileMap = myProjectService.getXmlFileMap();
//        if (!xmlFileMap.containsKey(refCanonicalText)) {
//            return true;
//        }

        MyMapperUtils.getStatementsByNamespace(project, refCanonicalText).stream()
                .filter(statement -> null != statement.getId() && null != statement.getId().getValue() && element.textMatches(statement.getId().getValue()))
                .map(statement -> statement.getId().getXmlAttributeValue())
                .forEach(targets::add);
        return true;
    }

    /**
     * @return DAO接口(PsiClass), 或者 null
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
