package com.zhiyin.plugins.listeners;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseEventArea;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * 将光标移动到行首
 */
public class MyEditorMouseListener implements EditorMouseListener {

    @Override
    public void mouseClicked(@NotNull EditorMouseEvent e) {
        goToLineStartOffSet(e);
    }

    private static void goToLineStartOffSet(EditorMouseEvent e) {
        if (e.getArea() == EditorMouseEventArea.LINE_MARKERS_AREA) {
            MouseEvent mouseEvent = e.getMouseEvent();
            Point point = mouseEvent.getPoint();
            Editor editor = e.getEditor();
            int offset = editor.xyToLogicalPosition(point).line;
            int lineStartOffset = editor.getDocument().getLineStartOffset(offset);

            VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
            if (file != null) {
                Project project = e.getEditor().getProject();
                if (project == null) {
                    return;
                }
                PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                if (psiFile != null) {
                    PsiElement element = psiFile.findElementAt(lineStartOffset);
                    if (element != null) {
                        // 将光标移动到行首
                        editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(offset, 0));
                    }
                }
            }
        }
    }

    @Override
    public void mousePressed(@NotNull EditorMouseEvent event) {
        goToLineStartOffSet(event);
    }
}
