package com.zhiyin.plugins.oneClickNavigation.codeInsight;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.zhiyin.plugins.oneClickNavigation.xml.utils.MyMapperUtils;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.settings.AppSettingsState;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class JavaToMocLineMarkerProvider extends RelatedItemLineMarkerProvider {
    private static final Logger LOG = Logger.getInstance(JavaLineMarkerProvider.class);

    @Override
    public void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<?
            super RelatedItemLineMarkerInfo<?>> result) {

        List<PsiElement> targets = new ArrayList<>();
        Project project = element.getProject();

        boolean anyCollected = collectBizCommonServiceMethodTarget(element, targets, project);

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
                    .setTooltipText(Constants.NAVIGATE_TO_MOC);
            RelatedItemLineMarkerInfo<PsiElement> relatedItemLineMarkerInfo = builder.createLineMarkerInfo(element);
            result.add(relatedItemLineMarkerInfo);
        }
    }

    private boolean collectBizCommonServiceMethodTarget(@NotNull PsiElement element, List<PsiElement> targets, Project project) {
        if (!(element instanceof PsiJavaToken)) {
            return false;
        }
        PsiMethodCallExpression psiMethodCallExpression = PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class);
        if (null == psiMethodCallExpression) {
            return false;
        }
        PsiReferenceExpression psiReferenceExpression = psiMethodCallExpression.getMethodExpression();
        List<String> expectedMethods = Arrays.asList(
                "bizCommonService.queryMocDaoData",
                "bizCommonService.queryMocDaoRawData",
                "bizCommonService.insertMocData",
                "bizCommonService.insertMocDataAndExtend",
                "bizCommonService.insertMocExtendData",
                "bizCommonService.updateMocData",
                "bizCommonService.updateMocDataAndExtend",
                "bizCommonService.findMocById",
                "bizCommonService.findMocExtendById",
                "bizCommonService.findMocDataById",
                "bizCommonService.findMocDataAndExtendById",
                "bizCommonService.deleteSoftMocData",
                "bizCommonService.deleteMocData",
                "bizCommonService.deleteMocDataAndExtend",
                "bizCommonService.queryMocDaoRawDataAndExtend",
                "bizCommonService.queryMocDaoDataAndExtend"
        );

        if (expectedMethods.stream().noneMatch(psiReferenceExpression::textMatches)) {
            return false;
        }
        PsiLiteralExpression literalExpression;
        if (!(element.getParent() instanceof PsiLiteralExpression)) {
            return true;
        }
        literalExpression = (PsiLiteralExpression) element.getParent();
        String bizCommonServiceMethodName = literalExpression.getValue() instanceof String ?
                (String) literalExpression.getValue() : null;
        if (null == bizCommonServiceMethodName || bizCommonServiceMethodName.isEmpty()) {
            return true;
        }
        PsiExpressionList argumentList = psiMethodCallExpression.getArgumentList();
        if (argumentList.getExpressionCount() < 2) {
            return true;
        }

        /*Module module = MyPsiUtil.getModuleByPsiElement(element);
        PsiShortNamesCache psiShortNamesCache = PsiShortNamesCache.getInstance(project);
        GlobalSearchScope scope;
        if (module != null) {
            scope = GlobalSearchScope.moduleScope(module);
        } else {
            scope = GlobalSearchScope.allScope(project);
        }*/

//        MyProjectService myProjectService = project.getService(MyProjectService.class);

        List<XmlAttributeValue> xmlAttributeValueList = MyMapperUtils.getMocListByName(project, bizCommonServiceMethodName);

        targets.addAll(xmlAttributeValueList);


        return true;
    }

}
