package com.zhiyin.plugins.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.*;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import com.zhiyin.plugins.resources.MyIcons;
import com.zhiyin.plugins.service.MyProjectService;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class DictCompletionContributor extends CompletionContributor {

    public DictCompletionContributor() {
        super();
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().inside(PsiLiteralExpression.class),
               getCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().inside(XmlAttributeValue.class),
               getCompletionProvider()
        );
    }

    private static @NotNull CompletionProvider<CompletionParameters> getCompletionProvider() {
        return new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                Project project = parameters.getEditor().getProject();
                if (project == null) return;

                MyProjectService service = project.getService(MyProjectService.class);
                Set<String> words = service.getWords();

                String prefix = result.getPrefixMatcher().getPrefix();
                words.stream().filter(word -> word.toLowerCase().startsWith(prefix.toLowerCase()))
                     // .limit(10)
                     .forEach(word -> result.addElement(LookupElementBuilder.create(word)
                                                                            .withIcon(MyIcons.pandaIconSVG16_2)
                                                                            .withTypeText("词典建议", true)
                                                                            .withCaseSensitivity(false)
                                                                            .withAutoCompletionPolicy(
                                                                                    AutoCompletionPolicy.NEVER_AUTOCOMPLETE)))
                ;
            }
        };
    }
}
