package com.zhiyin.plugins.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.openapi.vfs.VfsUtil;

public class JSPFileCounter {
    
    public static int countJspFiles(Project project) {
        VirtualFile baseDir = project.getBaseDir();
        JSPFileVisitor jspFileVisitor = new JSPFileVisitor();
        VfsUtil.visitChildrenRecursively(baseDir, jspFileVisitor);
        return jspFileVisitor.getJspFileCount();
    }

    private static class JSPFileVisitor extends VirtualFileVisitor<Void> {
        private int jspFileCount = 0;

        @Override
        public boolean visitFile(VirtualFile file) {
            if (file.getName().endsWith(".jsp")) {
                jspFileCount++;
            }
            return true; // Continue visiting other files
        }

        public int getJspFileCount() {
            return jspFileCount;
        }

    }
}
