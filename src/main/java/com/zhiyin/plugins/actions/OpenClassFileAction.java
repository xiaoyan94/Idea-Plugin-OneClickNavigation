package com.zhiyin.plugins.actions;

import com.intellij.ide.actions.RevealFileAction;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public class OpenClassFileAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent event) {
        PsiFile file = event.getData(CommonDataKeys.PSI_FILE);
        event.getPresentation().setEnabledAndVisible(file != null && "Java".equalsIgnoreCase(file.getVirtualFile().getExtension()));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // Get the current project and virtual file from the action event
        Project project = e.getProject();
        VirtualFile sourceFile = e.getData(CommonDataKeys.VIRTUAL_FILE);

        if (project == null || sourceFile == null || ! (sourceFile.getFileType() instanceof JavaFileType) /*|| !sourceFile.getName().endsWith(".java")*/) {
            return; // Invalid context or not a Java file
        }

        // Determine the path to the compiled class file
        String classFilePath = deriveClassFilePath(sourceFile);

        if (StringUtils.isBlank(classFilePath)) {
            Messages.showErrorDialog(project, "Failed to locate the corresponding class file.", "Error");
            return;
        }

        // Open the class file in the file explorer
        VirtualFile classFile = LocalFileSystem.getInstance().findFileByPath(classFilePath);
        if (classFile != null) {
//            FileEditorManager.getInstance(project).openFile(classFile, true);
            RevealFileAction.openFile(classFile.toNioPath());
        } else {
            Messages.showErrorDialog(project, "The class file does not exist or is not accessible.", "Error");
        }
    }

    /**
     * TODO 待完善：利用 Idea 提供的 PSI 相关 API，以更通用地方法获取编译输出目录对应文件
     */
    private String deriveClassFilePath(VirtualFile sourceFile) {
        // 简单实现：直接替换src/main/java为target/classes
        String sourceDirPath = sourceFile.getParent().getPath();
        if (sourceDirPath.contains("src/main/java")) {
            sourceDirPath = sourceDirPath.replace("src/main/java", "target/classes");
        }
        return sourceDirPath + "/" + sourceFile.getNameWithoutExtension() + ".class";
    }
}