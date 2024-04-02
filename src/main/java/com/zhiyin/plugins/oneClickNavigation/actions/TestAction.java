package com.zhiyin.plugins.oneClickNavigation.actions;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.compiler.ex.CompilerPathsEx;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootManagerEx;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.PsiNavigateUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomFileIndex;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.DomService;
import com.zhiyin.plugins.oneClickNavigation.xml.domElements.Mapper;
import com.zhiyin.plugins.oneClickNavigation.xml.domElements.Statement;
import com.zhiyin.plugins.service.MyProjectService;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.zhiyin.plugins.utils.MyPsiUtil.*;

public class TestAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: 获取当前项目内的所有Mapper xml文件
        Project project = e.getProject();
        if (null == project) return ;

//        String[] outputPaths = getOutputPathsDeperecated(project);
//
//        List<String> outputPaths1 = getOutputPaths(project);
//
//        System.out.println(outputPaths1);
//
//        VirtualFile virtualFile = e.getRequiredData(CommonDataKeys.VIRTUAL_FILE);
//        System.out.println(virtualFile.getPath());
//
//        boolean contains = isFileInOutputPaths(virtualFile, project);



//        Map<String, Mapper> xmlFileMap = project.getService(MyProjectService.class).getXmlFileMap();

        project.getService(MyProjectService.class).reInitXmlFileMap();

//        Statement statement = xmlFileMap.get("com.zhiyin.dao.order.IOrderDao").getStatements().get(0);
//        System.out.println(statement.getId());
//
//        PsiNavigateUtil.navigate(statement.getXmlElement());

//        Collection<VirtualFile> virtualFiles = DomFileIndex.findFiles("mapper", null, GlobalSearchScope.allScope(project));
//        Collection<VirtualFile> orderMapper = FilenameIndex.getVirtualFilesByName("OrderMapper.xml", false, GlobalSearchScope.allScope(project));
//        Collection<VirtualFile> virtualFiles1 = FileTypeIndex.getFiles(XmlFileType.INSTANCE, GlobalSearchScope.allScope(project));
//        for (VirtualFile virtualFile : orderMapper) {
//            PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
//            if (file instanceof XmlFile) {
//                XmlFile xmlFile = (XmlFile) file;
//                System.out.println(xmlFile.getRootTag().getNamespace());
//                System.out.println(xmlFile.getRootTag().getAttribute("namespace").getValue());
//                System.out.println(xmlFile.getDocument().getProlog());
//                DomFileElement<Mapper> element = DomManager.getDomManager(project).getFileElement(xmlFile, Mapper.class);
//                System.out.println(element);
//                System.out.println(element instanceof Mapper);
//                System.out.println(element.getRootElement());
//                System.out.println(element.getRootElement().getNamespace());
//            }
//
//        }
//
//        Map<String, Object> map = new HashMap<>();
//        PsiManager psiManager = PsiManager.getInstance(project);
//        for (VirtualFile virtualFile : virtualFiles1) {
//            PsiFile file = psiManager.findFile(virtualFile);
//            if (file instanceof XmlFile) {
//                XmlFile xmlFile = (XmlFile) file;
//                DomFileElement<Mapper> element = DomManager.getDomManager(project).getFileElement(xmlFile, Mapper.class);
//                if (element == null){
//
//                } else{
//                    System.out.println(element.getRootElement().getNamespace());
//                    map.put(element.getRootElement().getNamespace().getValue(), element.getRootElement().getStatements().get(0).getId().getValue());
//                }
////                System.out.println(element);
////                System.out.println(element instanceof Mapper);
////                System.out.println(element.getRootElement());
////                System.out.println(element.getRootElement().getNamespace());
//            }
//        }
//
//        List<DomFileElement<Mapper>> list = DomService.getInstance().getFileElements(Mapper.class, project, GlobalSearchScope.allScope(project));
//        for (DomFileElement<Mapper> mapperDomFileElement : list) {
//            System.out.println(mapperDomFileElement);
//        }
    }



}
