package com.zhiyin.plugins.service;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericAttributeValue;
import com.zhiyin.plugins.listeners.MyXmlFileListener;
import com.zhiyin.plugins.notification.MyPluginMessages;
import com.zhiyin.plugins.oneClickNavigation.xml.domElements.Mapper;
import com.zhiyin.plugins.oneClickNavigation.xml.domElements.Moc;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.zhiyin.plugins.utils.MyPsiUtil.isFileInOutputPaths;

/**
 * 项目级别的Service
 */
@Service(Service.Level.PROJECT)
public final class MyProjectService {
    private final Project project;

    public @NotNull Map<String, List<Mapper>> getXmlFileMap() {
        return xmlFileMap;
    }

    private final Map<String /* namespace */, List<Mapper> /* Mapper xml file */> xmlFileMap;

    private final Map<String /* Moc */, List<Moc> /* Moc xml file */> mocFileMap;

    public @NotNull Map<String, List<Moc>> getMocFileMap() {
        return mocFileMap;
    }

    private static final Logger LOG = Logger.getInstance(MyProjectService.class);

    public MyProjectService(Project project) {
        this.project = project;
        xmlFileMap = new ConcurrentHashMap<>();
        mocFileMap = new ConcurrentHashMap<>();
        subscribeMessageTopicListeners(project);
    }

    private static void subscribeMessageTopicListeners(Project project) {
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new MyXmlFileListener(project));
        LOG.info("subscribeMessageTopicListeners, topic: " + VirtualFileManager.VFS_CHANGES + ", handler: " + MyXmlFileListener.class.getName());
    }

    private void initXmlFileMap() {

        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(XmlFileType.INSTANCE, GlobalSearchScope.allScope(project));

        for (VirtualFile virtualFile : virtualFiles) {
            addToXmlFileMap(virtualFile);
        }

    }

    public void reInitXmlFileMap() {
        /*ApplicationManager.getApplication().invokeLater (() -> {

        });*/

       /* ApplicationManager.getApplication().executeOnPooledThread (() -> {
            ApplicationManager.getApplication().runReadAction(() -> {
                xmlFileMap.clear();
                initXmlFileMap();

                Notification msg = new Notification("ZhiyinOneClickNavigation", "刷新数据完成", String.format("成功加载%d条数据", xmlFileMap.size()), NotificationType.INFORMATION);
                msg.setIcon(MyIcons.pandaIconSVG16_2);

                // notify() displays the notification
                // msg.notify();
                Notifications.Bus.notify(msg, project);
            });
        });*/

        Task.Backgroundable task = new Task.Backgroundable(project, "OneClickNavigation loading data", false) {
            /**
             * @param indicator {@link ProgressIndicator}
             */
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                System.out.println("Task.Backgroundable: Current thread:" + Thread.currentThread().getName());
                xmlFileMap.clear();

                initXmlFileMap(indicator);

            }
        };

        ProgressIndicator indicator = ProgressManager.getGlobalProgressIndicator();
        if (indicator == null) {
            indicator = new BackgroundableProcessIndicator(task);
            indicator.setIndeterminate(false);
            System.out.println("reInitXmlFileMap: Current thread:" + Thread.currentThread().getName());
        }

        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, indicator);

    }

    private void initXmlFileMap(ProgressIndicator indicator) {
        System.out.println("initXmlFileMap: runReadAction Current thread:" + Thread.currentThread().getName());
        final AtomicReference<Collection<VirtualFile>> virtualFiles = new AtomicReference<>();
        ApplicationManager.getApplication().runReadAction(() -> {
            virtualFiles.set(FileTypeIndex.getFiles(XmlFileType.INSTANCE, GlobalSearchScope.allScope(project)));
        });

        int totalFiles = virtualFiles.get().size();
        AtomicInteger processedFiles = new AtomicInteger();
//        ApplicationManager.getApplication().executeOnPooledThread(() -> {
        for (VirtualFile virtualFile : virtualFiles.get()) {
//            ApplicationManager.getApplication().invokeLater(() -> {
                indicator.checkCanceled();
//                indicator.setText(String.format("正在加载文件: %s", virtualFile.getName()));
                double fraction = processedFiles.incrementAndGet() * 1d / totalFiles;
                indicator.setFraction(fraction);

//            });

            ApplicationManager.getApplication().runReadAction(() -> {
//              System.out.println("addToXmlFileMap: runReadAction Current thread:" + Thread.currentThread().getName()); //
                addToXmlFileMap(virtualFile);
            });
//            });
        }
        if (processedFiles.get() == totalFiles) {
            String content = String.format("成功加载%d条Mapper, %d条Moc", xmlFileMap.size(), mocFileMap.size());
            MyPluginMessages.showInfo("OneClickNavigation", content, project);
        }
//        });

    }

    public void addToXmlFileMap(VirtualFile virtualFile) {
        if (virtualFile == null) {
            return;
        }
        if (isFileInOutputPaths(virtualFile, project)) {
//            LOG.debug(String.format("文件在输出目录中，不加载: %s", virtualFile.getPath()));
            return;
        }

        PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
        if (file instanceof XmlFile) {
            XmlFile xmlFile = (XmlFile) file;
            DomFileElement<Mapper> element = DomManager.getDomManager(project).getFileElement(xmlFile, Mapper.class);
            if (element != null) {
                cacheToMap(element);
                return;
            }

            DomFileElement<Moc> mocElement = DomManager.getDomManager(project).getFileElement(xmlFile, Moc.class);
            if (mocElement != null) {
                cacheMocToMap(mocElement);
            }
        }
    }

    private void cacheToMap(DomFileElement<Mapper> element) {
        GenericAttributeValue<String> namespace = element.getRootElement().getNamespace();
        if (namespace == null) {
            LOG.warn("namespace is null, element: " + element);
            return;
        }
        String key = namespace.getValue();
        if (key == null) {
//            LOG.warn("key is null, element: " + element);
            return;
        }
        xmlFileMap.compute(key, (k, v) -> {
            if (v == null) {
                v = new ArrayList<>();
            }
            v.add(element.getRootElement());
            return v;
        });
    }

    private void cacheMocToMap(DomFileElement<Moc> element) {
        String key = element.getRootElement().getName().getValue();
        if (key == null) {
//            LOG.warn("key is null, element: " + element);
            return;
        }
        mocFileMap.compute(key, (k, v) -> {
            if (v == null) {
                v = new ArrayList<>();
            }
            v.add(element.getRootElement());
            return v;
        });
    }

    private <T extends DomElement> void cacheToMap(XmlFile xmlFile, Class<T> clazz, Function<T, String> keyExtractor, Map<String, List<T>> targetMap) {
        XmlTag rootTag = xmlFile.getRootTag();
        if (rootTag == null) {
            return;
        }
        String rootTagName = rootTag.getName();
        if (rootTagName.equals(clazz.getSimpleName())) {
            DomFileElement<T> element = DomManager.getDomManager(project).getFileElement(xmlFile, clazz);
            if (element != null) {
                String key = keyExtractor.apply(element.getRootElement());
                targetMap.computeIfAbsent(key, k -> new ArrayList<>()).add(element.getRootElement());
            }
        }
    }
}
