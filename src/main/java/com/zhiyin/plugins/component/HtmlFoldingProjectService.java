package com.zhiyin.plugins.component;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.messages.MessageBusConnection;
import com.zhiyin.plugins.manager.HtmlFoldingManager;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.PROJECT)
public final class HtmlFoldingProjectService implements Disposable {

    private final Project project;
    private final MessageBusConnection connection;

    public HtmlFoldingProjectService(Project project) {
        this.project = project;
        this.connection = project.getMessageBus().connect(this);

        // 订阅文件打开事件
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                handleFile(source, file);
            }
        });

        // 初始化已经打开的文件
        initializeExistingFiles();
    }

    private void handleFile(FileEditorManager source, VirtualFile file) {
        if (isHtmlOrTemplateFile(file)) {
            for (FileEditor editor : source.getEditors(file)) {
                if (editor instanceof TextEditor) {
                    HtmlFoldingManager.getInstance(((TextEditor) editor).getEditor());
                }
            }
        }
    }

    private boolean isHtmlOrTemplateFile(VirtualFile file) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        String extension = file.getExtension();
        return "html".equalsIgnoreCase(extension) || "htm".equalsIgnoreCase(extension) ||
               "ftl".equalsIgnoreCase(extension) || "xml".equalsIgnoreCase(extension) &&
                                                    (MyPsiUtil.isLayoutFile(psiFile) ||
                                                     MyPsiUtil.isImpMapperXML(psiFile)) ||
               "js".equalsIgnoreCase(extension) || "java".equalsIgnoreCase(extension) ||
//                "properties".equalsIgnoreCase(extension) ||
               "jsp".equalsIgnoreCase(extension);
    }

    private void initializeExistingFiles() {
        FileEditorManager manager = FileEditorManager.getInstance(project);
        for (VirtualFile file : manager.getOpenFiles()) {
            handleFile(manager, file);
        }
    }

    @Override
    public void dispose() {
        // connection 会随 this 自动释放，不需要手动 disconnect
    }

    public static HtmlFoldingProjectService getInstance(@NotNull Project project) {
        return project.getService(HtmlFoldingProjectService.class);
    }
}
