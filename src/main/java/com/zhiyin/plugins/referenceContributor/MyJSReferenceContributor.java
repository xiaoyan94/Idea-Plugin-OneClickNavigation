package com.zhiyin.plugins.referenceContributor;

import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;

public class MyJSReferenceContributor extends PsiReferenceContributor {
    /**
     * TODO
     * @param registrar
     */
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
//        registrar.registerReferenceProvider(PlatformPatterns.psiElement(JSProperty.class), );
    }
}
