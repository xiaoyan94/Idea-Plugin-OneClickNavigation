package com.zhiyin.plugins.listeners;

import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseMotionListener;
import com.intellij.ide.structureView.FileEditorPositionListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.CaretActionListener;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.ex.FileEditorWithProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.zhiyin.plugins.notification.MyPluginMessages;
import com.zhiyin.plugins.resources.Constants;
import com.zhiyin.plugins.service.ControllerUrlService;
import com.zhiyin.plugins.settings.AppSettingsState;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class MyFileEditorManagerListener implements FileEditorManagerListener {

    private static boolean isTargetFoldRegion(FoldRegion foldRegion) {
        return foldRegion.getGroup() != null && foldRegion.getGroup().toString().equals(Constants.FOLDING_GROUP);
    }

    /**
     * This method is called synchronously (in the same EDT event), as the creation of FileEditor(s).
     *
     * @param source
     * @param file
     * @param editorsWithProviders
     * @see #fileOpened(FileEditorManager, VirtualFile)
     */
    @Override
    public void fileOpenedSync(@NotNull FileEditorManager source, @NotNull VirtualFile file, @NotNull List<FileEditorWithProvider> editorsWithProviders) {
        FileEditorManagerListener.super.fileOpenedSync(source, file, editorsWithProviders);
    }

    /**
     * This method is called after the focus settles down (if requested) in a newly created FileEditor.
     * Be aware though, that this isn't always true in case of editors loaded asynchronously, which, in general,
     * may happen with any text editor. In that case, the focus request is postponed until after the editor is fully loaded,
     * which means that it may gain the focus way after this method is called.
     * When necessary, use {@link FileEditorManager#runWhenLoaded(Editor, Runnable)}) to ensure the desired ordering.
     * <p>
     * {@link #fileOpenedSync(FileEditorManager, VirtualFile, List<FileEditorWithProvider>)} is always invoked before this method,
     * either in the same or the previous EDT event.
     *
     * @param source
     * @param file
     * @see #fileOpenedSync(FileEditorManager, VirtualFile, List<FileEditorWithProvider>)}
     */
    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        FileEditorManagerListener.super.fileOpened(source, file);

        collapseFoldRegion(source);

//        MyPluginMessages.showInfo("fileOpened", file.getName(), source.getProject());

    }

    /**
     * @param source 获取Editor得到的文件是关闭后新编辑窗口的文件
     * @param file   被关闭的文件
     */
    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
//        collapseFoldRegion(source);
        FileEditorManagerListener.super.fileClosed(source, file);
//        MyPluginMessages.showInfo("fileClosed", file.getName(), source.getProject());
    }

    /**
     * @param event
     */
    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        FileEditorManagerListener.super.selectionChanged(event);
        collapseFoldRegion(event.getManager());

        Project project = event.getManager().getProject();
        ControllerUrlService controllerUrlService = project.getService(ControllerUrlService.class);
        VirtualFile oldFile = event.getOldFile();
        if (event.getOldFile() != null && event.getNewFile() != null) {
//            MyPluginMessages.showInfo("selectionChanged", oldFile.getName() + " -> " + event.getNewFile().getName(), project);
            controllerUrlService.recollectControllerUrls(oldFile);
        }

    }

    private static void collapseFoldRegion(@NotNull FileEditorManager source) {
        if(AppSettingsState.getInstance().defaultCollapseI18nStatus){
            Editor editor = source.getSelectedTextEditor();
            collapseFoldRegion(editor);
        }
    }

    public static void collapseFoldRegion(Editor editor) {
        if (editor != null) {
            editor.getFoldingModel().runBatchFoldingOperation(() -> {
                FoldRegion[] allFoldRegions = editor.getFoldingModel().getAllFoldRegions();
                Arrays.stream(allFoldRegions)
                        .filter(MyFileEditorManagerListener::isTargetFoldRegion)
                        .forEach(foldRegion -> {
                            foldRegion.setExpanded(false);
                        });
            });
        }
    }


    public static class Before implements FileEditorManagerListener.Before {

        /**
         * 在文件被关闭之前触发的事件处理方法。
         * <p>
         * 对于给定的文件编辑器管理器和虚拟文件，先尝试折叠所有折叠区域，然后调用超类的beforeFileClosed方法。
         * </p>
         *
         * @param source 文件编辑器管理器的实例，触发此事件的来源。
         * @param file   要被关闭的虚拟文件。
         */
        @Override
        public void beforeFileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            // 在文件关闭前尝试折叠所有折叠代码区域
            collapseFoldRegion(source);
            // 调用父类的
            FileEditorManagerListener.Before.super.beforeFileClosed(source, file);
        }
    }

    /*public static class MyCaretListener implements CaretListener {
     *//**
     * Called when the caret is moved.
     *
     * @param e the event containing information about the caret movement.
     *//*
        @Override
        public void caretPositionChanged(@NotNull CaretEvent e) {
            Editor editor = e.getEditor();
            if (editor instanceof FileEditor) {
                collapseFoldRegion(editor);
            }
        }

    }*/

    /*public static class MyEditorMouseMotionListener implements EditorMouseMotionListener {
     *//**
     * Called when the mouse is moved over the editor and no mouse buttons are pressed.
     *
     * @param e the event containing information about the mouse movement.
     *//*
        @Override
        public void mouseMoved(@NotNull EditorMouseEvent e) {
            EditorMouseMotionListener.super.mouseMoved(e);
            Editor editor = e.getEditor();
            if (editor instanceof FileEditor) {
                collapseFoldRegion(editor);
            }
        }

    }*/
}
