package com.zhiyin.plugins.service;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.zhiyin.plugins.listeners.MyXmlFileListener;
import com.zhiyin.plugins.oneClickNavigation.xml.domElements.Mapper;
import com.zhiyin.plugins.resources.MyIcons;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.zhiyin.plugins.utils.MyPsiUtil.isFileInOutputPaths;

/**
 * 项目级别的Service
 *
 * @author yan on 2024/3/10 00:26
 */
@Service(Service.Level.PROJECT)
public final class MyProjectService {
    private final Project project;

    public @NotNull Map<String, List<Mapper>> getXmlFileMap() {
        return xmlFileMap;
    }

    private final Map<String /* namespace */, List<Mapper> /* Mapper xml file */> xmlFileMap = new HashMap<>();

    private final Logger LOG = Logger.getInstance(MyProjectService.class);

    public MyProjectService(Project project) {
        this.project = project;

        initXmlFileMap();

        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES,
                new MyXmlFileListener(project));
    }

    private void initXmlFileMap() {

        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(XmlFileType.INSTANCE, GlobalSearchScope.allScope(project));

        for (VirtualFile virtualFile : virtualFiles) {
            addToXmlFileMap(virtualFile);
        }

    }

    public void reInitXmlFileMap() {
        ApplicationManager.getApplication().invokeLater (() -> {
            ApplicationManager.getApplication().runReadAction(() -> {
                xmlFileMap.clear();
                initXmlFileMap();

                Notification msg = new Notification("ZhiyinOneClickNavigation", "刷新数据完成", String.format("成功加载%d条数据", xmlFileMap.size()), NotificationType.INFORMATION);
                msg.setIcon(MyIcons.pandaIconSVG16_2);

                // notify() displays the notification
                // msg.notify();
                Notifications.Bus.notify(msg, project);
            });
        });

    }

    public void addToXmlFileMap(VirtualFile virtualFile) {
        if (virtualFile == null) {
            return;
        }
        if (isFileInOutputPaths(virtualFile, project)) {
            LOG.debug(String.format("文件在输出目录中，不加载: %s", virtualFile.getPath()));
            return;
        }

        PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
        if (file instanceof XmlFile) {
            XmlFile xmlFile = (XmlFile) file;
            DomFileElement<Mapper> element = DomManager.getDomManager(project).getFileElement(xmlFile, Mapper.class);
            if (element == null) {
                LOG.debug(String.format("Dom文件不是Mybatis Mapper类型，不加载: %s", virtualFile.getPath()));
            } else {
                cacheToMap(element);
            }
        }
    }

    private void cacheToMap(DomFileElement<Mapper> element) {
        String key = element.getRootElement().getNamespace().getValue();
        if (xmlFileMap.containsKey(key)) {
            xmlFileMap.get(key).add(element.getRootElement());
        } else{
            List<Mapper> list = new ArrayList<>();
            list.add(element.getRootElement());
            xmlFileMap.put(key, list);
        }
    }
}
