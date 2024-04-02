package com.zhiyin.plugins.referenceContributor;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;

/**
 * 扩展额外引用
 *
 * @author yan on 2024/3/13 22:46
 */
public class MyJavaReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiLiteralExpression.class), new PsiReferenceProvider() {
            /**
             * queryDaoDataT(iOrderScheduleDao.getClass(), iOrderScheduleDao, "getOrderScheduleList", params, builder);
             */
            @Override
            public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                PsiLiteralExpression literalExpression = (PsiLiteralExpression) element;
                PsiMethodCallExpression psiMethodCallExpression = PsiTreeUtil.getParentOfType(literalExpression,
                        PsiMethodCallExpression.class);
                if (psiMethodCallExpression == null) {
                    return new PsiReference[0];
                }
                PsiReferenceExpression methodExpression = psiMethodCallExpression.getMethodExpression();
                if (!methodExpression.textMatches(Constants.QUERY_DAO_METHOD_EXPRESSION)) {
                    return new PsiReference[0];
                }

                String queryDaoMethodName = literalExpression.getValue() instanceof String ?
                        (String) literalExpression.getValue() : null;
                if (queryDaoMethodName == null) {
                    return new PsiReference[0];
                }

                PsiExpressionList argumentList = psiMethodCallExpression.getArgumentList();

                if (argumentList.getExpressionCount() < 4) {
                    return new PsiReference[0];
                }

                PsiType[] expressionTypes = argumentList.getExpressionTypes();
                PsiType secondExpressionType = expressionTypes[1];
                if (null == secondExpressionType) {
                    return new PsiReference[0];
                }

                Project project = element.getProject();
                Module module = MyPsiUtil.getModuleByPsiElement(element);
                PsiShortNamesCache psiShortNamesCache = PsiShortNamesCache.getInstance(project);
                GlobalSearchScope scope;
                if (module != null) {
                    scope = GlobalSearchScope.moduleScope(module);
                } else {
                    scope = GlobalSearchScope.allScope(project);
                }

//                PsiClass[] classesByName = psiShortNamesCache.getClassesByName(secondExpressionType.getPresentableText(), scope);

                PsiClass[] classesByName = JavaPsiFacade.getInstance(project).findClasses(secondExpressionType.getCanonicalText(), scope);

                for (PsiClass psiClass : classesByName) {
//                    PsiMethod[] methodsByName = psiClass.findMethodsByName(queryDaoMethodName, true);
//                    if (methodsByName.length > 0) {
                        return new PsiReference[]{new MyJavaMethodReference(literalExpression, psiClass, queryDaoMethodName)};
//                    }
                }


                return new PsiReference[0];
            }
        });
    }
}
