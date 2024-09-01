package com.zhiyin.plugins.actions;

//import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlRecursiveElementWalkingVisitor;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.zhiyin.plugins.utils.MyPsiUtil;
import org.jetbrains.annotations.NotNull;

public class ImportMapperXMLSortColumnAction extends AnAction {
    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiFile psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE);
        if (!MyPsiUtil.isXmlFile(psiFile)) {
            return;
        }
        XmlFile xml = (XmlFile) psiFile;
        if (xml.getRootTag() == null) {
            return;
        }

        final Project project = e.getProject();
        xml.getRootTag().accept(new XmlRecursiveElementWalkingVisitor(){
            private int sort = 1;
            @Override
            public void visitElement(@NotNull PsiElement element) {
                super.visitElement(element);
            }

            @Override
            public void visitXmlElement(XmlElement element) {
                super.visitXmlElement(element);
            }

            @Override
            public void visitXmlTag(XmlTag tag) {
                super.visitXmlTag(tag);
                System.out.println(tag.getName());
                if ("column".equalsIgnoreCase(tag.getName())){
                    System.out.printf("%s %s%n", tag.getAttributeValue("name"), tag.getAttributeValue("col"));
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        tag.setAttribute("col", String.valueOf(sort++));
                    });
                }
            }
        });
    }

    /**
     *
     * @param e Carries information on the invocation place and data available
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        // 获取当前PsiElement
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        e.getPresentation().setEnabledAndVisible(MyPsiUtil.isImpMapperXML(psiFile));
    }

    // @AvailableSince(value = "222.3345.118")
//    @Override
//    public @NotNull ActionUpdateThread getActionUpdateThread() {
//        return ActionUpdateThread.EDT;
//    }
}
