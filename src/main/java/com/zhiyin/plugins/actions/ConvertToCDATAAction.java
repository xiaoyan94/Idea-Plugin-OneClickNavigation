package com.zhiyin.plugins.actions;

import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;

public class ConvertToCDATAAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        Editor editor = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR);
        if (editor == null) return;

        SelectionModel selectionModel = editor.getSelectionModel();
        if (!selectionModel.hasSelection()) return;

        String selectedText = selectionModel.getSelectedText();
        if (selectedText == null) return;

        String cdataText = "<![CDATA[" + selectedText + "]]>";

        ApplicationManager.getApplication().runWriteAction(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                editor.getDocument().replaceString(selectionModel.getSelectionStart(), selectionModel.getSelectionEnd(), cdataText);
            });
        });

        selectionModel.removeSelection();
    }

    @Override
    public void update(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        Editor editor = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR);
        if (editor == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        // Check if the file is an XML file
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile == null || !psiFile.getLanguage().isKindOf(XMLLanguage.INSTANCE)) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        SelectionModel selectionModel = editor.getSelectionModel();
        if (!selectionModel.hasSelection()) {
            e.getPresentation().setEnabled(false);
            return;
        }

        String selectedText = selectionModel.getSelectedText();
        if (selectedText == null || selectedText.isEmpty()) {
            e.getPresentation().setEnabled(false);
        }

        e.getPresentation().setEnabled(true);
    }
}