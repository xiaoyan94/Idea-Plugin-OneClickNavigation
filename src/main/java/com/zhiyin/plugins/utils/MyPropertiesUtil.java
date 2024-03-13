package com.zhiyin.plugins.utils;

import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import com.zhiyin.plugins.resources.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Search PSI elements for defined properties over the project.
 *
 * @author yan on 2024/3/6 22:56
 */
public class MyPropertiesUtil {

    private static final Logger LOG = Logger.getInstance(MyPropertiesUtil.class);

    /**
     * 在 psiElement 所在模块中查找 Properties 资源文件
     *
     * @param project 项目
     * @param module 模块
     * @param key        Properties 文件中的键
     * @return 单个 Property
     */
    public static Property findModuleI18nProperty(Project project, Module module, String key) {
        List<Property> properties = findModuleI18nProperties(project, module, key).stream().limit(1).collect(Collectors.toList());
        return ContainerUtil.getOnlyItem(properties);
    }

    /**
     * 查找模块范围内的 i18n 资源文件
     * @param module 用于定位所属模块
     * @param key 键
     * @return 多个 Property
     */
    public static List<Property> findModuleI18nProperties(Project project, Module module, String key) {
        List<Property> result = new ArrayList<>();
        List<VirtualFile> virtualFiles = new ArrayList<>();
        String moduleName = getSimpleModuleName(module);
        virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(moduleName + Constants.I18N_ZH_CN_SUFFIX,
                GlobalSearchScope.moduleScope(module)));
        virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(moduleName + Constants.I18N_ZH_TW_SUFFIX,
                GlobalSearchScope.moduleScope(module)));
        virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(moduleName + Constants.I18N_EN_US_SUFFIX,
                GlobalSearchScope.moduleScope(module)));
        return getProperties(project, virtualFiles, key, result);
    }

    /**
     * 查找模块范围内的 web i18n 资源文件
     * @param project 用于定位所属项目
     * @param module 用于定位所属模块
     * @param key 键
     * @return Property 键值对
     */
    public static List<Property> findModuleWebI18nProperties(Project project, Module module, String key) {
        List<Property> result = new ArrayList<>();
        List<VirtualFile> virtualFiles = new ArrayList<>();

        virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(Constants.I18N_WEB_ZH_CN,
                GlobalSearchScope.moduleScope(module)));
        virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(Constants.I18N_WEB_ZH_TW,
                GlobalSearchScope.moduleScope(module)));
        virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(Constants.I18N_WEB_EN_US,
                GlobalSearchScope.moduleScope(module)));
        return getProperties(project, virtualFiles, key, result);
    }

    private static List<Property> getProperties(Project project, Collection<VirtualFile> virtualFiles, String key,
                                                List<Property> result) {
        for (VirtualFile virtualFile : virtualFiles) {
            PropertiesFile propertiesFile = (PropertiesFile) PsiManager.getInstance(project)
                                                                       .findFile(virtualFile);
            if (propertiesFile != null) {
                List<IProperty> propertiesByKey = propertiesFile.findPropertiesByKey(key);
                for (IProperty iProperty : propertiesByKey) {
                    if (iProperty instanceof Property) {
                        result.add((Property) iProperty);
                    }
                }
            }
        }

        return result;
    }

    /**
     * 获取模块名称 com.zhiyin.mes.app.order 返回 order
     *
     * @param module 模块
     * @return 名称
     */
    public static String getSimpleModuleName(Module module) {
        String moduleName = module.getName();
        return moduleName.substring(moduleName.lastIndexOf('.') + 1);
    }

    public static String getTop3PropertiesValueString(List<Property> properties) {
        return properties.stream()
                         .limit(3)
                         .map(IProperty::getValue)
                         .collect(Collectors.joining("<br/>"));
    }
}

