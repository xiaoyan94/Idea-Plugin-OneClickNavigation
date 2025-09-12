package com.zhiyin.plugins.referenceContributor;

import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.highlighting.HighlightedReference;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericAttributeValue;
import com.zhiyin.plugins.oneClickNavigation.xml.domElements.Mapper;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyXMLReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(XmlAttributeValue.class), new PsiReferenceProvider() {
            @Override
            public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                if (MyPsiUtil.isXmlFile(element)) {
                    XmlFile xmlFile = (XmlFile) element.getContainingFile();
                    Project project = element.getProject();
                    DomManager domManager = DomManager.getDomManager(project);
                    DomFileElement<Mapper> domFileElement = domManager.getFileElement(xmlFile, Mapper.class);
                    if (domFileElement == null) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    XmlAttributeValue xmlAttributeValue = (XmlAttributeValue) element;
                    PsiElement parentXmlAttribute = MyPsiUtil.findPsiElementParentMatching(element, psiElement -> psiElement instanceof XmlAttribute);
                    if (parentXmlAttribute == null || !((XmlAttribute) parentXmlAttribute).getName().equalsIgnoreCase("id")){
                        return PsiReference.EMPTY_ARRAY;
                    }
                    PsiElement parentXmlTag = MyPsiUtil.findPsiElementParentMatching(element, psiElement -> psiElement instanceof XmlTag);
                    if (parentXmlTag == null || !Arrays.asList("select", "insert", "update", "delete").contains(((XmlTag) parentXmlTag).getName().toLowerCase())){
//                        System.out.println(parentXmlTag);
                        return PsiReference.EMPTY_ARRAY;
                    }
                    Mapper mapper = domFileElement.getRootElement();
                    GenericAttributeValue<String> namespace = mapper.getNamespace();
//                    System.out.println(namespace); // com.zhiyin.dao.assemble.basic.IRoutingBomDao
                    if (namespace == null || namespace.getValue() == null) {
                        return PsiReference.EMPTY_ARRAY;
                    }
                    String namespaceValue = namespace.getValue();
                    JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
                    PsiClass psiClass = javaPsiFacade.findClass(namespaceValue, element.getResolveScope());
                    if (psiClass == null) {
                        return PsiReference.EMPTY_ARRAY;
                    } else {
                        return new PsiReference[]{new MyXMLReference(xmlAttributeValue, psiClass, xmlAttributeValue.getValue())};
                    }
                }
                return PsiReference.EMPTY_ARRAY;
            }
        });
    }

    public static class MyXMLReference extends PsiReferenceBase<XmlAttributeValue> implements PsiPolyVariantReference, HighlightedReference {
        private final PsiClass psiClass;
        private final String methodName;

        /**
         * Reference range is obtained from {@link ElementManipulator#getRangeInElement(PsiElement)}.
         *
         * @param element Underlying element.
         */
        public MyXMLReference(@NotNull XmlAttributeValue element, PsiClass psiClass, String methodName) {
            super(element);
            this.psiClass = psiClass;
            this.methodName = methodName;
        }

        /**
         * Returns the results of resolving the reference.
         *
         * @param incompleteCode if true, the code in the context of which the reference is
         *                       being resolved is considered incomplete, and the method may return additional
         *                       invalid results.
         * @return the array of results for resolving the reference.
         */
        @Override
        public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
            PsiMethod[] psiMethods = psiClass.findMethodsByName(methodName, false);
            List<ResolveResult> results = new ArrayList<>();
            for (PsiMethod psiMethod : psiMethods) {
                results.add(new PsiElementResolveResult(psiMethod));
            }
            return results.toArray(new ResolveResult[0]);
        }

        /**
         * Returns the element which is the target of the reference.
         *
         * @return the target element, or {@code null} if it was not possible to resolve the reference to a valid target.
         * @see PsiPolyVariantReference#multiResolve(boolean)
         */
        @Override
        public @Nullable PsiElement resolve() {
            ResolveResult[] resolveResults = multiResolve(false);
            return resolveResults.length >= 1 ? resolveResults[0].getElement() : null;
        }

        /**
         * Returns the array of String, {@link PsiElement} and/or {@link LookupElement}
         * instances representing all identifiers that are visible at the location of the reference. The contents
         * of the returned array are used to build the lookup list for basic code completion. (The list
         * of visible identifiers may not be filtered by the completion prefix string - the
         * filtering is performed later by the IDE.)
         * <p>
         * This method is default since 2018.3.
         *
         * @return the array of available identifiers.
         */
        @Override
        public Object @NotNull [] getVariants() {
            PsiMethod[] methods = psiClass.getMethods();
            List<LookupElement> variants = new ArrayList<>();
            for (final PsiMethod psiMethod : methods) {
                if (psiMethod != null) {
                    LookupElement element = LookupElementBuilder.create(psiMethod)
                                                                .withIcon(MyIcons.pandaIconSVG16_2)
                                                                .withItemTextItalic(true)
                                                                .withTypeText(psiClass.getName(), AllIcons.FileTypes.Java, true)
                                                                .withBoldness(true)
                                                                .withCaseSensitivity(false)
                                                                .withTailText(psiClass.getQualifiedName(), true)
                                                                .withAutoCompletionPolicy(
                                                                        AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
                            ;
                    // 包装成最高优先级
                    LookupElement prioritized = PrioritizedLookupElement.withPriority(element, 1000.0);
                    variants.add(prioritized);
                }
            }
            return variants.toArray();
        }
    }
}
