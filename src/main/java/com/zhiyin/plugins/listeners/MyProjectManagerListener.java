package com.zhiyin.plugins.listeners;

import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.LineMarkerProviders;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageAnnotators;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.NoAccessDuringPsiEvents;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.zhiyin.plugins.actions.GoToLayoutXmlAction;
import com.zhiyin.plugins.annotator.MyHTMLAnnotator;
import com.zhiyin.plugins.annotator.MyJavaScriptBlockAnnotator;
import com.zhiyin.plugins.provider.lineMarkers.FeignClientRelatedItemLineMarkerProvider;
import com.zhiyin.plugins.provider.lineMarkers.JSUrlRelatedItemLineMarkerProvider;
import com.zhiyin.plugins.service.ControllerUrlService;
import com.zhiyin.plugins.service.MyProjectService;
import com.zhiyin.plugins.service.PluginDisposable;
import com.zhiyin.plugins.settings.TranslateSettingsComponent;
import com.zhiyin.plugins.settings.TranslateSettingsState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * execute something after loading the project and after the indexing finishes
 *
 * 
 */
public class MyProjectManagerListener implements ProjectManagerListener {

    private static final Logger LOG = Logger.getInstance(MyProjectManagerListener.class);

    private static final List<LineMarkerProvider> lineMarkerProviders = new ArrayList<>();

    /**
     * Invoked on project open. Executed in EDT.
     *
     * @param project opening project
     */
    @Override
    public void projectOpened(@NotNull Project project) {
        ProjectManagerListener.super.projectOpened(project);

        LOG.info(String.format("project opened:%s", project.getName()));

//        ApplicationManager.getApplication().invokeLater(initXmlFileMapInstantly(project));
        // runWhenSmart
        DumbService.getInstance(project).runWhenSmart(() -> {
            // 异步执行
//            ApplicationManager.getApplication().executeOnPooledThread (() -> {
//                ApplicationManager.getApplication().runReadAction(initXmlFileMapInstantly(project));
//            });
            if (project.isDisposed() || NoAccessDuringPsiEvents.isInsideEventProcessing()) {
                return;
            }

            MyProjectService myProjectService = project.getService(MyProjectService.class);
            System.out.println("reInitXmlFileMap start");
            myProjectService.reInitXmlFileMap();

            project.getService(ControllerUrlService.class);

            System.out.println("return runWhenSmart");
        });


        registerLineMarkerProvider(project);

    }

    @Override
    public void projectClosing(@NotNull Project project) {
        ProjectManagerListener.super.projectClosing(project);
        unregisterLineMarkerProvider();
        project.getService(ControllerUrlService.class).clearCache();
    }

    private static void unregisterLineMarkerProvider() {
        try {
            Language language = Language.findLanguageByID("JAVA"); // Or use the appropriate language
            if (language != null) {
                Iterator<LineMarkerProvider> iterator = lineMarkerProviders.iterator();
                while (iterator.hasNext()) {
                    LineMarkerProvider provider = iterator.next();
                    LineMarkerProviders.getInstance().removeExplicitExtension(language, provider);
                    iterator.remove();
                    System.out.println("LineMarkerProvider[" + provider.getClass().getSimpleName() + "] unregistered for language: " + language.getDisplayName());
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            LOG.error(e);
        }
    }

    private static void registerLineMarkerProvider(@NotNull Project project) {
        TranslateSettingsState state = TranslateSettingsComponent.Companion.getInstance().getState();
        if (state.getEnableFeignToRestController()) {
            LOG.info("enableFeignToRestController");
            try {
                Language language = Language.findLanguageByID("JAVA"); // Or use the appropriate language
                if (language != null && lineMarkerProviders.stream().noneMatch(p -> p instanceof FeignClientRelatedItemLineMarkerProvider)) {
                    FeignClientRelatedItemLineMarkerProvider feignClientRelatedItemLineMarkerProvider = new FeignClientRelatedItemLineMarkerProvider();
                    LineMarkerProviders.getInstance().addExplicitExtension(language, feignClientRelatedItemLineMarkerProvider, PluginDisposable.getInstance(project));
                    lineMarkerProviders.add(feignClientRelatedItemLineMarkerProvider);
                    System.out.println("FeignClientRelatedItemLineMarkerProvider registered for language: " + language.getDisplayName());
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                LOG.error(e);
            }
        }

        if(state.getEnableHtmlUrlToController()){
            LOG.info("enableHtmlUrlToController");
            Language javascript = Language.findLanguageByID("JavaScript");
            if (javascript != null) {
                if (lineMarkerProviders.stream().noneMatch(p -> p instanceof JSUrlRelatedItemLineMarkerProvider)) {
                    JSUrlRelatedItemLineMarkerProvider jsUrlRelatedItemLineMarkerProvider = new JSUrlRelatedItemLineMarkerProvider();
                    LineMarkerProviders.getInstance().addExplicitExtension(javascript, jsUrlRelatedItemLineMarkerProvider, PluginDisposable.getInstance(project));
                    lineMarkerProviders.add(jsUrlRelatedItemLineMarkerProvider);
                    System.out.println("JSUrlRelatedItemLineMarkerProvider registered for language: " + javascript.getDisplayName());
                }
            }
        }

        if (state.getEnableHtmlAnnotator()){
            LOG.info("enableHtmlAnnotator");
            // Registering the Annotator TODO 取消注册
            LanguageAnnotators.INSTANCE.addExplicitExtension(HTMLLanguage.INSTANCE, new MyHTMLAnnotator());
            Language javascript = Language.findLanguageByID("JavaScript");
            if (javascript != null) {
                LanguageAnnotators.INSTANCE.addExplicitExtension(javascript, new MyJavaScriptBlockAnnotator());
            }
        }
    }

    @NotNull
    private static Runnable initXmlFileMapInstantly(@NotNull Project project) {
        return () -> {
            if (project.isDisposed() || NoAccessDuringPsiEvents.isInsideEventProcessing()) {
                return;
            }

            MyProjectService myProjectService = project.getService(MyProjectService.class);
            int size = myProjectService.getXmlFileMap().size();
            LOG.info("myProjectService init xmlFileMap: " + size);

        };
    }

}
