package com.zhiyin.plugins.utils;

import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.zhiyin.plugins.resources.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PSI 相关操作方法
 *
 * 
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

    private static List<String> outputPaths = null;

    @NotNull
    public static synchronized List<String> getOutputPaths(Project project) {

        if (outputPaths == null){
            outputPaths = new ArrayList<>();
            ModuleManager moduleManager = ModuleManager.getInstance(project);
            for (Module module : moduleManager.getModules()) {
                ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
                VirtualFile[] outputFolders = rootManager.getExcludeRoots();
                for (VirtualFile outputFolder : outputFolders) {
                    outputPaths.add(outputFolder.getCanonicalPath());
                }
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

    public static boolean isI18nResourceMethod(@NotNull PsiMethodCallExpression psiMethodCallExpression) {
        PsiReferenceExpression psiReferenceExpression = psiMethodCallExpression.getMethodExpression();
        return psiReferenceExpression.textMatches(Constants.I18N_METHOD_EXPRESSION)
                || psiReferenceExpression.textMatches(Constants.I18N_METHOD_EXPRESSION_GET_SYS_I18N_RESOURCE)
                || psiReferenceExpression.textMatches(Constants.I18N_METHOD_EXPRESSION_GET_MESSAGE_BY_FACTORY);
    }

    /**
     * 判断是否是XML文件
     * @param element PSI元素
     * @return 是否是XML文件
     */
    public static boolean isXmlFile(PsiElement element) {
        PsiFile psiFile = element.getContainingFile();
        return isXmlFile(psiFile);
    }

    /**
     * 判断是否是XML文件
     * @param psiFile PSI文件
     * @return 是否是XML文件
     */
    public static boolean isXmlFile(PsiFile psiFile) {
        if(psiFile == null) {
            return false;
        }
        return psiFile instanceof XmlFile;
    }


    /**
     * 判断是否是布局文件
     * @param element PSI元素
     * @return 是否是布局文件
     */
    public static boolean isLayoutFile(PsiElement element) {
        if (!isXmlFile(element)) {
            return false;
        }
        XmlFile psiFile = (XmlFile) element.getContainingFile();
        XmlTag rootTag = psiFile.getRootTag();
        return rootTag != null && rootTag.getName().equals("ViewDefine");
    }

    /**
     * 判断是否是导入Mapper文件
     * @param psiFile XML文件Psi
     */
    public static boolean isImpMapperXML(PsiFile psiFile) {
        if (!isXmlFile(psiFile)) {
            return false;
        }
        XmlFile xmlFile = (XmlFile) psiFile;
        XmlTag rootTag = xmlFile.getRootTag();
        if (rootTag != null && rootTag.getName().equals("mapper")) {
            XmlTag tableTag = rootTag.findFirstSubTag("table");
            return tableTag != null && tableTag.getAttribute("name") != null;
        }
        return false;
    }

    /**
     * 获取最近的父辈 XmlTag
     * @param element PSI元素
     */
    public static XmlTag getParentXmlTag(PsiElement element) {
        PsiElement parent = element.getParent();
        while (parent != null) {
            if (parent instanceof XmlTag) {
                return (XmlTag) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /**
     * 以非递归方式进行遍历，找到最近的父辈元素，匹配给定的条件。
     */
    public static PsiElement findPsiElementParentMatching(PsiElement element, Predicate<PsiElement> predicate) {
        // 验证输入的有效性
        if (element == null || predicate == null) {
            return null;
        }

        // 从当前元素开始向上搜索满足条件的父元素
        while (element != null) {
            try {
                // 测试当前元素是否满足条件
                if (predicate.test(element)) {
                    return element; // 如果满足条件，返回当前元素
                }
            } catch (Exception e) {
                // 处理predicate.test抛出的异常
                System.out.println("Error testing predicate: " + e);
            }

            // 继续检查当前元素的父元素
            element = element.getParent();
        }

        // 如果没有找到匹配的元素，返回null
        return null;
    }

    /**
     * 从freemarker指令中获取key
     */
    public static String retrieveI18nKeyFromFreemarkerDirective(PsiElement element){
        if (element == null) {
            return null;
        }

        if ("PsiElement(FTL_FRAGMENT)".equals(element.toString()) && element.getText() != null && element.getText().startsWith("<@message")) {
            String inputString = element.getText();
//          String inputString = "<@message key=\"com.zhiyin.mes.app.web.workline_title\"/>";
            String patternString = "key=(['\"])(.*?)(['\"])";

            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(inputString);

            if (matcher.find()) {
                String keyValue = matcher.group(2);
                System.out.println("Key value: " + keyValue);
                return keyValue;
            } else {
                System.out.println("No key value found");
            }
        }

        return null;
    }
}
