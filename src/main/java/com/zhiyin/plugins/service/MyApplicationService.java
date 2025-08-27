package com.zhiyin.plugins.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.zhiyin.plugins.utils.JSPFileCounter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Service(Service.Level.APP)
public final class MyApplicationService {

    private final Map<String, Boolean> projectTypeMap = new HashMap<>();

    public boolean checkIsOldMesProject(Project project, Module module) {
        if (project == null) return false;
        String projectFilePath = project.getProjectFilePath();
        Boolean isOldMesProject = projectTypeMap.get(projectFilePath);
        if (isOldMesProject == null) {
//            isOldMesProject = isOldMesProject(project, module);
            isOldMesProject = !isSpringCloudMesProject(project);
            projectTypeMap.put(projectFilePath, isOldMesProject);
        }
        return isOldMesProject;
    }

    private boolean isOldMesProject(Project project, Module module) {
        VirtualFile baseDir = getProjectVirtualFile(project, module);

        // Check for web.xml
        VirtualFile webXml = baseDir.findFileByRelativePath("src/main/webapp/WEB-INF/web.xml");
        if (webXml != null && webXml.exists()) {
            System.out.println("web.xml exists");
            return true;
        }

        // Check for JSP files
        VirtualFile webAppDir = baseDir.findFileByRelativePath("src/main/webapp");
        if (webAppDir != null && webAppDir.isDirectory()) {
            for (VirtualFile file : webAppDir.getChildren()) {
                if (file.getName().endsWith(".jsp")) {
                    System.out.println("JSP file found: " + file.getPath());
                    return true;
                }
            }
        }

        if (JSPFileCounter.countJspFiles(project) > 30) {
            System.out.println("JSP file count > 30");
            return true;
        }

        return false;
    }

    public boolean isSpringCloudMesProject(Project project) {
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir == null) {
            return false;
        }

        VirtualFile springbootDir = baseDir.findChild("springboot");
        VirtualFile springcloudDir = baseDir.findChild("springcloud");
        VirtualFile pomFile = baseDir.findChild("pom.xml");

        // 检查目录和文件是否存在且类型正确
        if (springbootDir == null || !springbootDir.isDirectory() ||
                springcloudDir == null || !springcloudDir.isDirectory() ||
                pomFile == null || pomFile.isDirectory()) {
            return false;
        }

        // 如果目录和文件都存在，则继续检查 pom.xml 的内容
        return hasCorrectModulesInPom(project, pomFile);
    }

    private boolean hasCorrectModulesInPom(Project project, VirtualFile pomFile) {
        // Get the PSI file for the pom.xml
        PsiFile psiFile = PsiManager.getInstance(project).findFile(pomFile);

        // Check if the file is a valid XML file
        if (!(psiFile instanceof XmlFile)) {
            return false;
        }

        XmlFile xmlFile = (XmlFile) psiFile;
        XmlTag rootTag = xmlFile.getRootTag();

        // Check if the root tag is <project>
        if (rootTag == null || !rootTag.getName().equalsIgnoreCase("project")) {
            return false;
        }

        // Now, proceed to find the <modules> tag
        return findModulesTag(rootTag);
    }

    private boolean findModulesTag(XmlTag projectTag) {
        XmlTag modulesTag = projectTag.findFirstSubTag("modules");
        if (modulesTag == null) {
            return false;
        }

        boolean hasSpringbootModule = false;
        boolean hasSpringcloudModule = false;

        // 遍历 modulesTag 的所有子元素
        for (PsiElement child : modulesTag.getChildren()) {
            // 检查当前元素是否是 XmlTag 类型
            if (child instanceof XmlTag) {
                XmlTag moduleTag = (XmlTag) child;

                // 检查标签名称是否为 "module"
                if (moduleTag.getName().equalsIgnoreCase("module")) {
                    String moduleName = moduleTag.getValue().getText();
                    if ("springboot".equalsIgnoreCase(moduleName)) {
                        hasSpringbootModule = true;
                    }
                    if ("springcloud".equalsIgnoreCase(moduleName)) {
                        hasSpringcloudModule = true;
                    }
                }
            }
        }

        return hasSpringbootModule && hasSpringcloudModule;
    }

    private boolean isSpringBootProject(Project project, Module module) {
        VirtualFile baseDir = getProjectVirtualFile(project, module);

        // Check for application.properties or application.yml
        VirtualFile applicationProperties = baseDir.findFileByRelativePath("src/main/resources/application.properties");
        if (applicationProperties != null && applicationProperties.exists()) {
            System.out.println("application.properties exists");
            return true;
        }

        VirtualFile applicationYaml = baseDir.findFileByRelativePath("src/main/resources/application.yml");
        if (applicationYaml != null && applicationYaml.exists()) {
            System.out.println("application.yml exists");
            return true;
        }

        // Check for Spring Boot dependencies in pom.xml or build.gradle
        VirtualFile pomXml = baseDir.findFileByRelativePath("pom.xml");
        if (pomXml != null && pomXml.exists()) {
            // You can further analyze the pom.xml content to look for Spring Boot dependencies
            // e.g., using a XML parser to look for "spring-boot-starter" dependencies

            // TODO

            return false;
        }

        VirtualFile buildGradle = baseDir.findFileByRelativePath("build.gradle");
        if (buildGradle != null && buildGradle.exists()) {
            // Similar check for build.gradle
            return false;
        }

        // Check for @SpringBootApplication annotation in source files
        // This requires parsing source files, which can be more complex.

        return false;
    }

    private @NotNull VirtualFile getProjectVirtualFile(Project project, Module module) {
        VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
        VirtualFile baseDir;
        if (contentRoots.length == 0) {
            baseDir = project.getBaseDir();
            System.out.println("baseDir: " + baseDir);
        } else{
            baseDir = contentRoots[0];
            System.out.println("contentRoots[0]: " + baseDir.getPath());
        }
        return baseDir;
    }

}
