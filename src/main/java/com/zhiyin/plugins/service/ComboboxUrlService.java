package com.zhiyin.plugins.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class ComboboxUrlService {

    private final Project project;
    private List<String> cachedResults = new ArrayList<>();

    public ComboboxUrlService(Project project) {
        this.project = project;
        searchAndCacheXmlTags("ComboxUrl", "value");
    }

    public List<String> getCachedResults() {
        return new ArrayList<>(cachedResults);  // Return a copy to ensure encapsulation
    }

    public void searchAndCacheXmlTags(String tagName, String attr) {
        List<String> results = searchXmlTags(tagName, attr);
        results = results.stream().distinct().collect(Collectors.toList());
        cachedResults = results;
    }

    private List<String> searchXmlTags(String tagName, String attr) {
        List<String> results = new ArrayList<>();
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        PsiManager psiManager = PsiManager.getInstance(project);

        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(XmlFileType.INSTANCE, scope);

        for (VirtualFile virtualFile : virtualFiles) {
            PsiFile psiFile = psiManager.findFile(virtualFile);
            if (psiFile instanceof XmlFile) {
                XmlFile xmlFile = (XmlFile) psiFile;
                if (xmlFile.getRootTag() == null) continue;
                if (!xmlFile.getRootTag().getName().equals("ViewDefine")) continue;
                Collection<XmlTag> tags = PsiTreeUtil.findChildrenOfType(xmlFile, XmlTag.class);
                for (XmlTag tag : tags) {
                    if (tag.getName().equals(tagName)) {
                        results.add(tag.getAttributeValue(attr));
                    }
                }
            }
        }

        return results;
    }
}
