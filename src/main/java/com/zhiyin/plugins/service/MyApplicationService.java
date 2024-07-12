package com.zhiyin.plugins.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.zhiyin.plugins.utils.JSPFileCounter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Service(Service.Level.APP)
public final class MyApplicationService {

    private final Map<Project, Boolean> projectTypeMap = new HashMap<>();

    public boolean checkIsOldMesProject(Project project, Module module) {
        if (project == null) return false;
        Boolean isOldMesProject = projectTypeMap.get(project);
        if (isOldMesProject == null) {
            isOldMesProject = isOldMesProject(project, module);
            projectTypeMap.put(project, isOldMesProject);
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
