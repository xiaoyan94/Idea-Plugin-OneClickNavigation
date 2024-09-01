package com.zhiyin.plugins.utils;

import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.encoding.EncodingManager;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.SlowOperations;
import com.intellij.util.containers.ContainerUtil;
import com.zhiyin.plugins.resources.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Search PSI elements for defined properties over the project.
 *
 * 
 */
public class MyPropertiesUtil {

//    private static final Logger LOG = Logger.getInstance(MyPropertiesUtil.class);

    /**
     * 在 psiElement 所在模块中查找 Properties 资源文件
     *
     * @param project 项目
     * @param module  模块
     * @param key     Properties 文件中的键
     * @return 单个 Property
     */
    public static Property findModuleI18nProperty(Project project, Module module, String key) {
        List<Property> properties = findModuleI18nProperties(project, module, key).stream().limit(1).collect(Collectors.toList());
        return ContainerUtil.getOnlyItem(properties);
    }

    /**
     * 在 psiElement 所在模块中查找 Properties 资源文件
     */
    public static String findModuleI18nPropertyValue(Project project, Module module, String key) {
        Property property = findModuleI18nProperty(project, module, key);
        if (property == null || property.getValue() == null) return null;
        boolean native2AsciiForPropertiesFiles = isNative2AsciiForPropertiesFiles();
        if (native2AsciiForPropertiesFiles) {
            return property.getValue();
        } else {
            return StringUtil.unicodeToString(property.getValue());
        }
    }

    /**
     * 在所在模块中查找 web Properties 资源文件
     */
    public static String findModuleWebI18nPropertyValue(Project project, Module module, String key) {
        List<Property> properties = findModuleI18nProperties(project, module, key);
        properties.addAll(findModuleWebI18nProperties(project, module, key));
        if (properties.isEmpty()) return null;
        Property property = properties.get(0);
        if (property == null || property.getValue() == null) return null;
        boolean native2AsciiForPropertiesFiles = isNative2AsciiForPropertiesFiles();
        if (native2AsciiForPropertiesFiles) {
            return property.getValue();
        } else {
            return StringUtil.unicodeToString(property.getValue());
        }
    }

    /**
     * 是否开启 native2ascii
     * @return true: 开启 native2ascii
     */
    public static boolean isNative2AsciiForPropertiesFiles() {
        return EncodingManager.getInstance().isNative2AsciiForPropertiesFiles();
    }

    /**
     * 查找模块范围内的 i18n 资源文件
     *
     * @param module 用于定位所属模块
     * @param key    键
     * @return 多个 Property
     */
    public static List<Property> findModuleI18nProperties(Project project, Module module, String key) {
        if (key == null) {
            return new ArrayList<>();
        }
        List<Property> result = new ArrayList<>();
        List<VirtualFile> virtualFiles = new ArrayList<>();
        String moduleName = getSimpleModuleName(module);
        GlobalSearchScope scope = GlobalSearchScope.moduleScope(module);
        if (ProjectTypeChecker.isTraditionalJavaWebProject(project, module)){
            scope = GlobalSearchScope.projectScope(project);
            virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(Constants.I18N_ZH_CN_SUFFIX,
                    scope));
            // 查找项目下所有.properties文件
            Collection<VirtualFile> properties = FileTypeIndex.getFiles(FileTypeManager.getInstance().getFileTypeByExtension("properties"), scope);
            // 根据文件名进行过滤
            properties = properties.stream().filter(virtualFile -> virtualFile.getName().contains(Constants.I18N_ZH_CN_SUFFIX)).collect(Collectors.toList());
            virtualFiles.addAll(properties);
        }
        virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(moduleName + Constants.I18N_ZH_CN_SUFFIX,
                scope));
        virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(moduleName + Constants.I18N_ZH_TW_SUFFIX,
                scope));
        virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(moduleName + Constants.I18N_EN_US_SUFFIX,
                scope));
        virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(moduleName + Constants.I18N_VI_VN_SUFFIX,
                scope));
        return getProperties(project, virtualFiles, key, result);
    }

    /**
     * 查找模块范围内DataGrid的 i18n 资源文件
     *
     * @param module 用于定位所属模块
     * @param key    键
     * @return Property[chs, cht, en]
     */
    public static @NotNull List<Property> findModuleDataGridI18nProperties(Project project, Module module, String key) {
        List<Property> result = new ArrayList<>();
        if (key == null) {
            return result;
        }
        List<VirtualFile> virtualFiles = new ArrayList<>();
        String moduleName = getSimpleModuleName(module);
        GlobalSearchScope scope;
        if (ProjectTypeChecker.isTraditionalJavaWebProject(project, module)){
            scope = GlobalSearchScope.projectScope(project);
            virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(Constants.I18N_DATAGRID_ZH_CN_SUFFIX,
                    scope));

            // FilenameIndex.getVirtualFilesByName 方法是根据文件全名来查找，不支持模糊查找。所以需要使用下面的方法根据文件名进行过滤
            // 查找项目下所有.properties文件
            Collection<VirtualFile> properties = FileTypeIndex.getFiles(FileTypeManager.getInstance().getFileTypeByExtension("properties"), scope);
            // 根据文件名进行过滤
            properties = properties.stream().filter(virtualFile -> virtualFile.getName().contains(Constants.I18N_DATAGRID_ZH_CN_SUFFIX)).collect(Collectors.toList());
            virtualFiles.addAll(properties);
        }
        scope = GlobalSearchScope.moduleScope(module);
        virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(moduleName + Constants.I18N_DATAGRID_ZH_CN_SUFFIX,
                scope));
        virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(moduleName + Constants.I18N_DATAGRID_ZH_TW_SUFFIX,
                scope));
        virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(moduleName + Constants.I18N_DATAGRID_EN_US_SUFFIX,
                scope));
        virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(moduleName + Constants.I18N_DATAGRID_VI_VN_SUFFIX,
                scope));
        return getProperties(project, virtualFiles, key, result);
    }

    /**
     * 查找模块范围内的 web i18n 资源文件
     *
     * @param project 用于定位所属项目
     * @param module  用于定位所属模块
     * @param key     键
     * @return Property 键值对
     */
    public static List<Property> findModuleWebI18nProperties(Project project, Module module, String key) {
        List<Property> result = new ArrayList<>();
        if (key == null) {
            return result;
        }
        List<VirtualFile> virtualFiles = new ArrayList<>();

        virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(Constants.I18N_WEB_ZH_CN,
                GlobalSearchScope.moduleScope(module)));
        virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(Constants.I18N_WEB_ZH_TW,
                GlobalSearchScope.moduleScope(module)));
        virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(Constants.I18N_WEB_EN_US,
                GlobalSearchScope.moduleScope(module)));
        virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(Constants.I18N_WEB_VI_VN,
                GlobalSearchScope.moduleScope(module)));
        return getProperties(project, virtualFiles, key, result);
    }

    public static List<Property> getProperties(Project project, Collection<VirtualFile> virtualFiles, String key,
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
     * 向多个 Properties文件中添加键值对
     */
    public static void addPropertyByFileNames(Project project, Module module, String[] propertiesFileName, String key, String value, boolean updateIfExist) {
        if (propertiesFileName == null || propertiesFileName.length == 0) {
            return;
        }
        List<VirtualFile> virtualFiles = new ArrayList<>();
        for (String nFileSuffix : propertiesFileName) {
            virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(nFileSuffix,
                    GlobalSearchScope.moduleScope(module)));
        }

        ApplicationManager.getApplication().invokeLater(() -> WriteCommandAction.runWriteCommandAction(project, () -> {
            for (VirtualFile virtualFile : virtualFiles) {
                PropertiesFile propertiesFile = (PropertiesFile) PsiManager.getInstance(project)
                        .findFile(virtualFile);

                if (propertiesFile != null) {
                    // 添加键值对
                    if (propertiesFile.findPropertyByKey(key) == null) {
                        propertiesFile.addProperty(key, value);
                    } else if (updateIfExist) {
                        // 更新键值对
                        IProperty iProperty = propertiesFile.findPropertyByKey(key);
                        if (iProperty instanceof Property) {
                            iProperty.setValue(value);
                        }
                    }
                }
            }
        }));
    }

    /**
     * 向 Properties 文件中添加键值对
     */
    public static void addPropertyToI18nFile(Project project, Module module, String i18nFileSuffix, String key, String value) {
        String moduleName = getSimpleModuleName(module);
//        PropertiesComponent.getInstance();
        addPropertyByFileNames(project, module, new String[]{moduleName + i18nFileSuffix}, key, value, false);
    }

    public static void addPropertyToI18nFile(Project project, Module module, String i18nFileSuffix, String key, String value, boolean updateIfExist) {
        String moduleName = getSimpleModuleName(module);
//        PropertiesComponent.getInstance();
        addPropertyByFileNames(project, module, new String[]{moduleName + i18nFileSuffix}, key, value, updateIfExist);
    }

    /**
     * 查找模块范围内的 i18n 资源文件
     *
     * @param module 用于定位所属模块
     * @param value  值
     * @return 键
     */
    public static List<Property> findModuleI18nPropertiesByValue(Project project, Module module, String value) {
        List<Property> result = new ArrayList<>();
        if (value == null) {
            return result;
        }

        String moduleName = getSimpleModuleName(module);

        SlowOperations.allowSlowOperations(() -> {
            DumbService.getInstance(project).runReadActionInSmartMode(() -> {
                List<VirtualFile> virtualFiles = new ArrayList<>(FilenameIndex.getVirtualFilesByName(moduleName + Constants.I18N_ZH_CN_SUFFIX,
                        GlobalSearchScope.moduleScope(module)));
                List<IProperty> allProperties = getProperties(project, virtualFiles);
                Optional<Property> foundProperty = allProperties.stream()
                        .filter(iProperty -> iProperty instanceof Property && comparePropertyValue(value, iProperty))
                        .map(iProperty -> (Property) iProperty)
                        .findFirst();
                foundProperty.ifPresent((property) -> {
                    List<Property> properties = MyPropertiesUtil.findModuleI18nProperties(project, module, property.getKey());
                    if (properties != null && !properties.isEmpty()) {
                        result.addAll(properties);
                    }
                });
            });
        });

        return result;
    }

    /**
     * 查找模块Web范围内的 i18n 资源文件
     *
     * @param module 用于定位所属模块
     * @param value  值
     * @return 键
     */
    public static List<Property> findModuleWebI18nPropertiesByValue(Project project, Module module, String value) {
        List<Property> result = new ArrayList<>();
        if (value == null) {
            return result;
        }

        String moduleName = getSimpleModuleName(module);

        SlowOperations.allowSlowOperations(() -> {
            DumbService.getInstance(project).runReadActionInSmartMode(() -> {
                List<VirtualFile> virtualFiles = new ArrayList<>(FilenameIndex.getVirtualFilesByName(moduleName + Constants.I18N_WEB_ZH_CN,
                        GlobalSearchScope.moduleScope(module)));
                List<IProperty> allProperties = getProperties(project, virtualFiles);
                Optional<Property> foundProperty = allProperties.stream()
                        .filter(iProperty -> iProperty instanceof Property && comparePropertyValue(value, iProperty))
                        .map(iProperty -> (Property) iProperty)
                        .findFirst();
                foundProperty.ifPresent((property) -> {
                    List<Property> properties = MyPropertiesUtil.findModuleWebI18nProperties(project, module, property.getKey());
                    if (properties != null && !properties.isEmpty()) {
                        result.addAll(properties);
                    }
                });
            });
        });

        return result;
    }

    /**
     * 查找模块DataGrid范围内的 i18n 资源文件
     *
     * @param module 用于定位所属模块
     * @param value  值
     * @return Property[chs,cht,en]
     */
    public static List<Property> findModuleDataGridI18nPropertiesByValue(Project project, Module module, String value) {
        List<Property> result = new ArrayList<>();
        if (value == null) {
            return result;
        }

        String moduleName = getSimpleModuleName(module);

        SlowOperations.allowSlowOperations(() -> {
            DumbService.getInstance(project).runReadActionInSmartMode(() -> {
                List<VirtualFile> virtualFiles = new ArrayList<>(FilenameIndex.getVirtualFilesByName(moduleName + Constants.I18N_DATAGRID_ZH_CN_SUFFIX,
                        GlobalSearchScope.moduleScope(module)));
                List<IProperty> allProperties = getProperties(project, virtualFiles);
                Optional<Property> foundProperty = allProperties.stream()
                        .filter(iProperty -> iProperty instanceof Property && comparePropertyValue(value, iProperty))
                        .map(iProperty -> (Property) iProperty)
                        .findFirst();
                foundProperty.ifPresent((property) -> {
                    List<Property> properties = MyPropertiesUtil.findModuleDataGridI18nProperties(project, module, property.getKey());
                    if (!properties.isEmpty()) {
                        result.addAll(properties);
                    }
                });
            });
        });

        return result;
    }

    /**
     * 查找模块DataGrid范围内的 i18n 资源文件
     *
     * @param module 用于定位所属模块
     * @param value  值
     * @return 键
     */
    public static String[] findModuleDataGridI18nKeysByValue(Project project, Module module, String value) {
        List<String> result = new ArrayList<>();
        if (value == null) {
            return result.toArray(new String[0]);
        }

        String moduleName = getSimpleModuleName(module);
        return SlowOperations.allowSlowOperations(() -> {
            List<VirtualFile> virtualFiles = new ArrayList<>(FilenameIndex.getVirtualFilesByName(moduleName + Constants.I18N_DATAGRID_ZH_CN_SUFFIX,
                    GlobalSearchScope.moduleScope(module)));
            List<IProperty> allProperties = getProperties(project, virtualFiles);
            return getKeysFromProperties(value, allProperties, result);
        });
//        return getKeysFromProperties(value, allProperties, result);
    }

    private static String @NotNull [] getKeysFromProperties(String value, List<IProperty> allProperties, List<String> result) {
        allProperties.forEach(iProperty -> {
            if (iProperty instanceof Property && comparePropertyValue(value, iProperty)) {
                result.add(iProperty.getKey());
            }
        });
        return result.toArray(new String[0]);
    }

    /**
     * 比较属性值, 兼容 native2ascii
     * @param value 值
     * @param iProperty 属性
     * @return 是否相等
     */
    public static boolean comparePropertyValue(String value, IProperty iProperty) {
        if (value == null || iProperty == null) {
            return false;
        }
        boolean native2AsciiForPropertiesFiles = isNative2AsciiForPropertiesFiles();
        if (native2AsciiForPropertiesFiles) {
            return value.equals(iProperty.getValue());
        }
        return value.equals(StringUtil.unicodeToString(iProperty.getValue()));
    }

    /**
     * 获取所有 Properties 键值对
     *
     * @param project      项目
     * @param virtualFiles properties 文件
     * @return 该文件所有键值对集合
     */
    public static List<IProperty> getProperties(Project project, Collection<VirtualFile> virtualFiles) {
        List<IProperty> result = new ArrayList<>();
        for (VirtualFile virtualFile : virtualFiles) {
            PropertiesFile propertiesFile = (PropertiesFile) PsiManager.getInstance(project)
                    .findFile(virtualFile);
            if (propertiesFile != null) {
                result.addAll(propertiesFile.getProperties());
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
        if (module == null) {
            return "";
        }
        String moduleName = module.getName();
        String simpleModuleName = moduleName.substring(moduleName.lastIndexOf('.') + 1);
        if ("system".equalsIgnoreCase(simpleModuleName)){
            return "sysadm";
        }
        return simpleModuleName;
    }

    public static String getTop3PropertiesValueString(List<Property> properties) {
        boolean native2AsciiForPropertiesFiles = isNative2AsciiForPropertiesFiles();
        return properties.stream()
                .limit(4)
                .map(IProperty::getValue)
                .map(value -> {
                    if (native2AsciiForPropertiesFiles) {
                        return value;
                    } else {
                        return StringUtil.unicodeToString(value);
                    }
                })
                .collect(Collectors.joining("<br/>"));
    }

    public static List<IProperty> getModuleI18nPropertiesCN(Project project, Module module) {
        String moduleName = getSimpleModuleName(module);
        List<VirtualFile> virtualFiles = new ArrayList<>();

        GlobalSearchScope scope = GlobalSearchScope.moduleScope(module);
        if (ProjectTypeChecker.isTraditionalJavaWebProject(project, module)){
            scope = GlobalSearchScope.projectScope(project);
            virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(Constants.I18N_ZH_CN_SUFFIX,
                    scope));
            // 查找项目下所有.properties文件
            Collection<VirtualFile> properties = FileTypeIndex.getFiles(FileTypeManager.getInstance().getFileTypeByExtension("properties"), scope);
            // 根据文件名进行过滤
            properties = properties.stream().filter(virtualFile -> virtualFile.getName().contains(Constants.I18N_ZH_CN_SUFFIX)).collect(Collectors.toList());
            virtualFiles.addAll(properties);
        }

        virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(moduleName + Constants.I18N_ZH_CN_SUFFIX,
                scope));

        return getProperties(project, virtualFiles);
    }

    public static List<IProperty> getModuleDataGridI18nPropertiesCN(Project project, Module module) {
        String moduleName = getSimpleModuleName(module);
        List<VirtualFile> virtualFiles = new ArrayList<>();

        GlobalSearchScope scope = GlobalSearchScope.moduleScope(module);
        if (ProjectTypeChecker.isTraditionalJavaWebProject(project, module)){
            scope = GlobalSearchScope.projectScope(project);
            virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(Constants.I18N_DATAGRID_ZH_CN_SUFFIX,
                    scope));
            // 查找项目下所有.properties文件
            Collection<VirtualFile> properties = FileTypeIndex.getFiles(FileTypeManager.getInstance().getFileTypeByExtension("properties"), scope);
            // 根据文件名进行过滤
            properties = properties.stream().filter(virtualFile -> virtualFile.getName().contains(Constants.I18N_DATAGRID_ZH_CN_SUFFIX)).collect(Collectors.toList());
            virtualFiles.addAll(properties);
        }

        virtualFiles.addAll(FilenameIndex.getVirtualFilesByName(moduleName + Constants.I18N_DATAGRID_ZH_CN_SUFFIX,
                scope));
        return getProperties(project, virtualFiles);
    }
}

