package com.zhiyin.plugins.listeners;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent;
import com.zhiyin.plugins.service.MyProjectService;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MyXmlFileListener implements BulkFileListener {

    private final Project project;

    public MyXmlFileListener(Project project) {
        this.project = project;
    }

    /**
     * @param events
     */
    @Override
    public void before(@NotNull List<? extends @NotNull VFileEvent> events) {
        BulkFileListener.super.before(events);
    }

    /**
     * @param events
     */
    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
        BulkFileListener.super.after(events);

        for (VFileEvent event : events) {
            if (event instanceof VFileContentChangeEvent) {
//                VFileContentChangeEvent vfileContentChangeEvent = (VFileContentChangeEvent) event;
//                VirtualFile virtualFile = vfileContentChangeEvent.getFile();
                VirtualFile virtualFile = event.getFile();
                MyProjectService myProjectService = project.getService(MyProjectService.class);
                myProjectService.addToXmlFileMap(virtualFile);
            } else if (event instanceof VFileDeleteEvent) {
                // TODO
            } else if (event instanceof VFileMoveEvent) {
                // TODO
            }
        }

    }
}
