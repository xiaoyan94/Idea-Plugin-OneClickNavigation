package com.zhiyin.plugins.utils;

import com.intellij.openapi.compiler.ex.CompilerPathsEx;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * PSI 相关操作方法
 *
 * @author yan on 2024/3/7 23:33
 */
public class MyPsiUtil {

    /**
     * 获取所属模块
     *
     * @param psiElement 元素
     * @return 模块
     */
    public static @Nullable Module getModuleByPsiElement(PsiElement psiElement) {
        return ModuleUtilCore.findModuleForPsiElement(psiElement);
    }

    public static TextRange getTextRangeFromPsiLiteralExpression(PsiLiteralExpression psiLiteralExpression) {
        int startOffset = psiLiteralExpression.getTextOffset();
        return new TextRange(startOffset, startOffset + psiLiteralExpression.getTextLength());
    }

    @NotNull
    public static List<String> getOutputPaths(Project project) {
        List<String> outputPaths = new ArrayList<>();

        ModuleManager moduleManager = ModuleManager.getInstance(project);
        for (Module module : moduleManager.getModules()) {
            ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
            VirtualFile[] outputFolders = rootManager.getExcludeRoots();
            for (VirtualFile outputFolder : outputFolders) {
                outputPaths.add(outputFolder.getCanonicalPath());
            }
        }

        return outputPaths;
    }

//    @NotNull
//    @Deprecated
//    public static String[] getOutputPathsDeperecated(Project project) {
//        return CompilerPathsEx.getOutputPaths(ModuleManager.getInstance(project).getModules());
//    }

    /**
     * 判断给定文件是否在编译输出路径内
     *
     * @param virtualFile VFS文件
     * @param project     项目
     * @return 是否在编译输出路径内
     */
    public static boolean isFileInOutputPaths(@NotNull VirtualFile virtualFile, @NotNull Project project) {
        List<String> outputPaths = getOutputPaths(project);

        String filePath = virtualFile.getPath();

        // 遍历所有编译输出路径，检查给定文件的根路径是否包含在内
        for (String outputPath : outputPaths) {
            if (filePath.startsWith(outputPath)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isInSameModule(PsiElement targetEle, PsiElement element) {
        Module module1 = MyPsiUtil.getModuleByPsiElement(targetEle);
        Module module2 = MyPsiUtil.getModuleByPsiElement(element);
        if (module1 == null && module2 != null) {
            return false;
        } else if (module1 != null && module2 == null) {
            return false;
        } else if (module1 != null) {
            return module1.equals(module2);
        }
        return true;
    }
}
