package com.zhiyin.plugins.service;

import com.google.common.collect.HashBasedTable;
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

import java.util.*;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class ComboboxUrlService {

    private final Project project;
    private HashBasedTable<String, String, Set<String>> cache = HashBasedTable.create();

    public ComboboxUrlService(Project project) {
        this.project = project;
        searchAndCacheXmlTags("ComboxUrl", "value");
        searchAndCacheXmlTags("Field", "easyuiClass");
    }

    public Set<String> getCachedResults(String tagName, String attr) {
        Set<String> list = cache.get(tagName, attr);
        if (!cache.contains(tagName, attr) || list == null || list.isEmpty()) {
            searchAndCacheXmlTags(tagName, attr);
        }
        return new HashSet<>(list == null ? Collections.emptySet() : list);
    }

    public void searchAndCacheXmlTags(String tagName, String attr) {
        Set<String> results = searchXmlTags(tagName, attr);
        cache.put(tagName, attr, results);
    }

    private Set<String> searchXmlTags(String tagName, String attr) {
        Set<String> results = new HashSet<>();
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        PsiManager psiManager = PsiManager.getInstance(project);

        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(XmlFileType.INSTANCE, scope);

        for (VirtualFile virtualFile : virtualFiles) {
            PsiFile psiFile = psiManager.findFile(virtualFile);
            if (psiFile instanceof XmlFile) {
                XmlFile xmlFile = (XmlFile) psiFile;
                if (xmlFile.getRootTag() == null) continue;
                if (!(xmlFile.getRootTag().getName().equals("ViewDefine") || xmlFile.getRootTag().getName().equals("DataGrid"))) continue;
                Collection<XmlTag> tags = PsiTreeUtil.findChildrenOfType(xmlFile, XmlTag.class);
                for (XmlTag tag : tags) {
                    if (tag.getName().equals(tagName) && tag.getAttributeValue(attr) != null) {
                        results.add(tag.getAttributeValue(attr));
                    }
                }
            }
        }

        return results;
    }
}
