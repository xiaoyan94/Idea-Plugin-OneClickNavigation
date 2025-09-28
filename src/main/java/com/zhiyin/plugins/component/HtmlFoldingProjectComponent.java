package com.zhiyin.plugins.component;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import com.zhiyin.plugins.manager.HtmlFoldingManager;
import org.jetbrains.annotations.NotNull;

/**
 * ProjectComponent 过时，使用 Service + Listener 实现
 */
@Deprecated
public class HtmlFoldingProjectComponent implements ProjectComponent {
    private final Project project;
    private MessageBusConnection connection;
    
    public HtmlFoldingProjectComponent(Project project) {
        this.project = project;
    }
    
    @Override
    public void projectOpened() {
        connection = project.getMessageBus().connect();
        
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, 
            new FileEditorManagerListener() {
                @Override
                public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                    // 只处理 HTML 或模板文件
                    if (isHtmlOrTemplateFile(file)) {
                        FileEditor[] editors = source.getEditors(file);
                        for (FileEditor editor : editors) {
                            if (editor instanceof TextEditor) {
                                HtmlFoldingManager.getInstance(((TextEditor) editor).getEditor());
                            }
                        }
                    }
                }
            });
        
        // 初始化已打开的文件
        initializeExistingFiles();
    }
    
    private boolean isHtmlOrTemplateFile(VirtualFile file) {
        String extension = file.getExtension();
        return "html".equalsIgnoreCase(extension) || 
               "htm".equalsIgnoreCase(extension) ||
               "ftl".equalsIgnoreCase(extension) ||
               "jsp".equalsIgnoreCase(extension);
    }
    
    private void initializeExistingFiles() {
        FileEditorManager manager = FileEditorManager.getInstance(project);
        for (VirtualFile file : manager.getOpenFiles()) {
            if (isHtmlOrTemplateFile(file)) {
                FileEditor[] editors = manager.getEditors(file);
                for (FileEditor editor : editors) {
                    if (editor instanceof TextEditor) {
                        HtmlFoldingManager.getInstance(((TextEditor) editor).getEditor());
                    }
                }
            }
        }
    }
    
    @Override
    public void projectClosed() {
        if (connection != null) {
            connection.disconnect();
        }
    }
}