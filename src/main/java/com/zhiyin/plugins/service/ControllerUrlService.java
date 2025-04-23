package com.zhiyin.plugins.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.*;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent;
import com.intellij.psi.*;
import com.intellij.psi.impl.JavaConstantExpressionEvaluator;
import com.intellij.psi.impl.PsiTreeChangeEventImpl;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtil;
import com.zhiyin.plugins.notification.MyPluginMessages;
import com.zhiyin.plugins.settings.TranslateSettingsComponent;
import com.zhiyin.plugins.settings.TranslateSettingsState;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static com.zhiyin.plugins.utils.MyPsiUtil.*;

@Service(Service.Level.PROJECT)
public final class ControllerUrlService {

    private long lastExecutionTime = 0; // Timestamp of the last execution
    private final long MIN_INTERVAL_MS = TimeUnit.SECONDS.toMillis(60); // Minimum interval between executions (e.g., 5 seconds)

    private static final String[] REQUEST_MAPPING_ANNOTATIONS = {
            "org.springframework.web.bind.annotation.RequestMapping",
            "org.springframework.web.bind.annotation.GetMapping",
            "org.springframework.web.bind.annotation.PostMapping",
            "org.springframework.web.bind.annotation.PutMapping",
            "org.springframework.web.bind.annotation.DeleteMapping"
    };

    private final Map<PsiClass, Set<String>> controllerUrlsCache = new ConcurrentHashMap<>();
    private final Map<String, List<PsiMethod>> urlMethodCache = new ConcurrentHashMap<>();
    /**
     * rest controller url (feign client url) -> rest controller method
     */
//    private final Map<String, PsiMethod> feignClientUrlRestMethodCache = new HashMap<>();
    private final Project project;
    private final AtomicBoolean isCollecting = new AtomicBoolean(false);

    public ControllerUrlService(Project project) {
        this.project = project;

        TranslateSettingsState state = TranslateSettingsComponent.Companion.getInstance().getState();
        if (state.getEnableFeignToRestController() || state.getEnableHtmlUrlToController()) {
            initControllerUrlsCache();
        }

//        registerPsiTreeChangeListener(project);

        // 添加文件系统监听器
//        registerVFSChangeListener(project);
    }

    private void registerVFSChangeListener(Project project) {
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                for (VFileEvent event : events) {
                    if(event instanceof VFileCreateEvent || event instanceof VFileDeleteEvent || event instanceof VFileMoveEvent) {
                        VirtualFile file = event.getFile();
                        if (file != null && file.isValid() && "java".equals(file.getExtension())) {
                            DumbService.getInstance(project).runWhenSmart(() -> {
                                ApplicationManager.getApplication().runReadAction(() -> {
                                    PsiFile psiFile = PsiUtil.getPsiFile(project, file);
                                    if (psiFile instanceof PsiJavaFile) {
                                        PsiJavaFile javaFile = (PsiJavaFile) psiFile;
                                        for (PsiClass psiClass : javaFile.getClasses()) {
                                            if (isAnnotatedWithFeignClient(psiClass) ||
                                                    isAnnotatedWithRestController(psiClass) ||
                                                    isAnnotatedWithController(psiClass)) {

                                                // Do something with the identified class
                                                collectControllerUrls(() -> MyPluginMessages.showInfo("Controller URLs collected", project));
                                                break;
                                            }
                                        }
                                    }
                                });
                            });

//                            MyPluginMessages.showInfo("BulkFileListener: ","listening to changes: event type: " + event.getClass().getSimpleName(), project);
//                            collectControllerUrls(() -> MyPluginMessages.showInfo("Controller URLs collected", project));
//                            break;
                        }
                    }
                }
            }
        });
    }

    private void registerPsiTreeChangeListener(Project project) {
        PsiManager.getInstance(project).addPsiTreeChangeListener(
                new PsiTreeChangeAdapter() {

                    @Override
                    public void childAdded(@NotNull PsiTreeChangeEvent event) {
                        handleChange(event);
                    }

                    @Override
                    public void childRemoved(@NotNull PsiTreeChangeEvent event) {
                        handleChange(event);
                    }

                    @Override
                    public void childReplaced(@NotNull PsiTreeChangeEvent event) {
                        handleChange(event);
                    }

                    @Override
                    public void childMoved(@NotNull PsiTreeChangeEvent event) {
                        handleChange(event);
                    }

                    @Override
                    public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
                        handleChange(event);
                    }

                    @Override
                    public void propertyChanged(@NotNull PsiTreeChangeEvent event) {
                        handleChange(event);
                    }

                    private void handleChange(PsiTreeChangeEvent event) {
                        if (event instanceof PsiTreeChangeEventImpl) {
                            PsiTreeChangeEventImpl impl = (PsiTreeChangeEventImpl) event;
                            PsiElement element = impl.getParent();

                            DumbService.getInstance(project).runWhenSmart(() -> {
                                        ApplicationManager.getApplication().runReadAction(() -> {
                            // Check if the element is a PsiClass or PsiMethod
                            if (element instanceof PsiClass || element instanceof PsiMethod) {
                                PsiClass psiClass = element instanceof PsiClass ? (PsiClass) element : ((PsiMethod) element).getContainingClass();

                                if (psiClass != null && hasRelevantAnnotation(psiClass)) {
//                                    MyPluginMessages.showInfo("PsiTreeChangeEventImpl: ", "listening to changes: event type: " + impl.getClass().getSimpleName(), project);
                                    if (event.getFile() != null) {
                                        recollectControllerUrls(event.getFile().getVirtualFile());
                                    } else {
                                        collectControllerUrls(null);
                                    }
                                }
                            } else {
                                // TODO 删除方法在撤销导致消失问题
                            }
                                        });
                            });
                        }
                    }

                    // Utility method to check if a PsiClass has relevant annotations
                    private boolean hasRelevantAnnotation(PsiClass psiClass) {
                        for (PsiAnnotation annotation : psiClass.getAnnotations()) {
                            String qualifiedName = annotation.getQualifiedName();
                            if ("org.springframework.web.bind.annotation.Controller".equals(qualifiedName) ||
                                    "org.springframework.web.bind.annotation.RestController".equals(qualifiedName) ||
                                    "org.springframework.cloud.openfeign.FeignClient".equals(qualifiedName)) {
                                return true;
                            }
                        }
                        return false;
                    }

                    // 其他必要的重写方法...
                },
                PluginDisposable.getInstance(project)
        );
    }

    public void initControllerUrlsCache() {
        collectControllerUrls(() -> MyPluginMessages.showInfo("OneClickNavigation", "成功加载: " + controllerUrlsCache.size() + " Controllers, " + urlMethodCache.size() + " API urls", project));
    }

    public void recollectControllerUrls(VirtualFile file){
        AtomicReference<GlobalSearchScope> scope0 = new AtomicReference<>();
        ApplicationManager.getApplication().runReadAction(() -> {
            scope0.set(GlobalSearchScope.fileScope(project, file));
        });

        GlobalSearchScope scope = scope0.get();
        collectControllerUrls(scope, () -> System.out.println("collectControllerUrls: " + file.getPath()));
//        collectControllerUrls(() -> {});
    }

    public void collectControllerUrls(Runnable onComplete) {
        collectControllerUrls(GlobalSearchScope.allScope(project),onComplete);
    }

    public void collectControllerUrls(GlobalSearchScope scope, Runnable onComplete) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastExecutionTime < MIN_INTERVAL_MS) {
            System.out.println("Skipped execution to respect the time interval.");
//            isCollecting.set(false);
            return;
        } else {
            lastExecutionTime = currentTime; // Update the last execution time
        }

        if (isCollecting.compareAndSet(false, true)) {
            DumbService.getInstance(project).runWhenSmart(() -> {
                if (project.isDisposed()){// || NoAccessDuringPsiEvents.isInsideEventProcessing()) {
                    isCollecting.set(false);
                    System.out.println("collectControllerUrls: project is disposed: " + project.isDisposed() + ", NoAccessDuringPsiEvents: " + NoAccessDuringPsiEvents.isInsideEventProcessing());
                    return;
                }

                Task.Backgroundable task = new Task.Backgroundable(project, "Collecting Controller URLs", true) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        try {
                            indicator.setIndeterminate(false);
                            System.out.println("collectControllerUrls: isDumb: " + DumbService.isDumb(project));
//                            while (true) {
//                                if (!DumbService.isDumb(project)) {
//                                    System.out.println("collectControllerUrls: isDumb: " + DumbService.isDumb(project));
                                    collectControllerUrls(scope, indicator);
//                                    break;
//                                }
//                                if (indicator.isCanceled()) {
//                                    MyPluginMessages.showInfo("OneClickNavigation", "已取消加载: " + controllerUrlsCache.size() + " Controllers, " + urlMethodCache.size() + " API urls", project);
//                                    break;
//                                }
//                            }
                        } finally {
                            isCollecting.set(false);
                            if (onComplete != null) {
                                ApplicationManager.getApplication().invokeLater(onComplete);
                            }
                        }
                    }
                };
                ProgressIndicator indicator = new BackgroundableProcessIndicator(task);
                indicator.setIndeterminate(false);
                System.out.println("collectControllerUrls: Current scope:" + scope);

                ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, indicator);
            });

        }
    }

    private void collectControllerUrls(GlobalSearchScope scope, ProgressIndicator indicator) {
        Map<PsiClass, Set<String>> tempControllerUrlsCache = new HashMap<>();
        Map<String, List<PsiMethod>> tempUrlMethodCache = new HashMap<>();

        List<PsiClass> controllers;
        CompletableFuture<List<PsiClass>> future = findClassesWithAnnotationUsingIndex(project, "Controller", scope);
        CompletableFuture<List<PsiClass>> future2 = findClassesWithAnnotationUsingIndex(project, "RestController", scope);
        CompletableFuture<List<PsiClass>> future3 = findClassesWithAnnotationUsingIndex(project, "FeignClient", scope);
        controllers = future.join();
        controllers.addAll(future2.join());
        controllers.addAll(future3.join());

        int totalControllers = controllers.size();

        for (int i = 0; i < totalControllers; i++) {
            if (indicator.isCanceled()) {
                return;
            }

            PsiClass controller = controllers.get(i);

            indicator.setFraction((double) i / totalControllers);

//            ApplicationManager.getApplication().runReadAction(() -> {
            DumbService.getInstance(project).runReadActionInSmartMode(() -> {
//                ApplicationManager.getApplication().assertReadAccessAllowed();

                indicator.setText("Processing controller: " + controller.getName());

                String classUrl = getMappingUrl(controller);
                Set<String> methodUrls = new HashSet<>();
                tempControllerUrlsCache.put(controller, methodUrls);

                PsiMethod[] methods = controller.getMethods();
                for (PsiMethod method : methods) {
                    String methodUrl;
//                    while(true){
//                        if(!DumbService.isDumb(project)){
                            methodUrl = getMappingUrl(method);
//                            break;
//                        }
//                        System.out.println("collectControllerUrls: isDumb: " + DumbService.isDumb(project));
//                        if(indicator.isCanceled()){
//                            return;
//                        }
//                    }
                    if (methodUrl != null && !methodUrl.isEmpty()) {
                        String fullUrl = classUrl + methodUrl;
                        fullUrl = fullUrl.startsWith("/") ? fullUrl : "/" + fullUrl;
                        methodUrls.add(fullUrl);
                        if (!(scope.equals(GlobalSearchScope.allScope(project)) || scope.equals(GlobalSearchScope.projectScope(project)))){
                            List<PsiMethod> methodForUrl = getMethodForUrl(fullUrl);
                            tempUrlMethodCache.put(fullUrl, methodForUrl);
//                            System.out.println("collectControllerUrls: methodForUrl: " + methodForUrl + ", fullUrl: " + fullUrl);
                        }
                        List<PsiMethod> psiMethods = tempUrlMethodCache.computeIfAbsent(fullUrl, k -> new ArrayList<>());
                        if (!psiMethods.contains(method)){
                            psiMethods.add(method);
                        }
                        psiMethods.removeIf(Objects::isNull);

                    }
                }
            });
        }

        synchronized (this) {
            if (scope.equals(GlobalSearchScope.allScope(project)) || scope.equals(GlobalSearchScope.projectScope(project))){
                // project scope: 收集所有
                System.out.println("collectControllerUrls: project scope: " + scope);
                controllerUrlsCache.clear();
                controllerUrlsCache.putAll(tempControllerUrlsCache);
                urlMethodCache.clear();
                urlMethodCache.putAll(tempUrlMethodCache);
            } else {
                // file scope
                System.out.println("collectControllerUrls: file scope: " + scope);
                controllerUrlsCache.putAll(tempControllerUrlsCache);
                urlMethodCache.putAll(tempUrlMethodCache);
            }

        }
    }

    public Set<String> getUrlsForController(PsiClass controller) {
        return new HashSet<>(controllerUrlsCache.getOrDefault(controller, Collections.emptySet()));
    }

    public void removeUrlsNullMethod(PsiMethod method) {
//        MyPluginMessages.showInfo("removeUrlsNullMethod", "remove method: " + method);
        urlMethodCache.forEach((key, psiMethods) -> {
            // Define your condition
            psiMethods.removeIf(psiMethod -> psiMethod == method);
        });

    }

    public @NotNull List<PsiMethod> getMethodForUrl(String url) {
//        synchronized (this) {
        if (urlMethodCache.containsKey(url)) {
//            System.out.println("getMethodForUrl: " + url + ", total urls: " + urlMethodCache.size() + ", current methods: " + urlMethodCache.get(url));
            return urlMethodCache.get(url);
            }
//        }
//        return Collections.synchronizedList(new ArrayList<>());
        return new ArrayList<>();
    }

    // Fuzzy match against the urlMethodCache keys
    public List<PsiMethod> getMethodsForUrlFuzzy(String url) {
        // List to hold the matching methods
        List<PsiMethod> matchingMethods = new ArrayList<>();

        // Compile a case-insensitive regex for fuzzy matching
        Pattern pattern = Pattern.compile(".*" + Pattern.quote(url) + ".*", Pattern.CASE_INSENSITIVE);

        // Search through the cache keys for fuzzy matches
        for (Map.Entry<String, List<PsiMethod>> entry : urlMethodCache.entrySet()) {
            String cacheUrl = entry.getKey();

            // If the URL matches the pattern (fuzzy match)
            if (pattern.matcher(cacheUrl).matches()) {
                matchingMethods.addAll(entry.getValue());  // Add all the methods for this URL
            }
        }

        return matchingMethods;
    }

    // 与getMethodsForUrlFuzzy相比，放到Map中
    public Map<String, List<PsiMethod>> getMethodsMapForUrlFuzzy(String url) {
        Map<String, List<PsiMethod>> matchingMethodsMap = new HashMap<>();

        // Compile a case-insensitive regex for fuzzy matching
        Pattern pattern = Pattern.compile(".*" + Pattern.quote(url) + ".*", Pattern.CASE_INSENSITIVE);

        // Search through the cache keys for fuzzy matches
        for (Map.Entry<String, List<PsiMethod>> entry : urlMethodCache.entrySet()) {
            String cacheUrl = entry.getKey();

            // List to hold the matching methods
            List<PsiMethod> matchingMethods = new ArrayList<>();

            // If the URL matches the pattern (fuzzy match)
            if (pattern.matcher(cacheUrl).matches()) {
                matchingMethods.addAll(entry.getValue());  // Add all the methods for this URL
            }

            matchingMethodsMap.put(cacheUrl, matchingMethods);
        }

        return matchingMethodsMap;
    }

    public Map<PsiClass, Set<String>> getAllControllerUrls() {
        return new HashMap<>(controllerUrlsCache);
    }

    public CompletableFuture<List<PsiClass>> findClassesWithAnnotationUsingIndex(Project project, String annotationName, GlobalSearchScope scope) {
        CompletableFuture<List<PsiClass>> future = new CompletableFuture<>();

//        ApplicationManager.getApplication().invokeLater(() -> {
//            ApplicationManager.getApplication().assertReadAccessAllowed();
            ApplicationManager.getApplication().runReadAction(() -> {
                try {
                    List<PsiClass> result = new ArrayList<>();
                    Collection<PsiAnnotation> annotations = JavaAnnotationIndex.getInstance().get(annotationName, project, scope);
                    for (PsiAnnotation annotation : annotations) {
                        PsiModifierList modifierList = (PsiModifierList) annotation.getParent();
                        PsiElement owner = modifierList.getParent();
                        if (owner instanceof PsiClass) {
                            result.add((PsiClass) owner);
                        }
                    }
                    future.complete(result);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });
//        });

        return future;
    }

    public String getMappingUrl(PsiElement element) {

        for (String annotationFQN : REQUEST_MAPPING_ANNOTATIONS) {
            PsiAnnotation annotation = getAnnotation(element, annotationFQN);
            if (annotation != null) {
                PsiAnnotationMemberValue value = annotation.findAttributeValue("value");
                if (value != null) {
                    return getAnnotationMemberValue(value);
                }
            }
        }

        return "";
    }

    public String getAnnotationMemberValue(PsiAnnotationMemberValue memberValue) {
        PsiReference reference = memberValue.getReference();
        if (memberValue instanceof PsiExpression) {
            Object constant = JavaConstantExpressionEvaluator.computeConstantExpression((PsiExpression) memberValue, false);
            return constant == null ? null : constant.toString();
        }
        if (memberValue instanceof PsiArrayInitializerMemberValue) {
            PsiArrayInitializerMemberValue arrayValue = (PsiArrayInitializerMemberValue) memberValue;
            for (PsiAnnotationMemberValue memberValue2 : arrayValue.getInitializers()) {
                // TODO: multiple values to be supported
                if (memberValue2 instanceof PsiLiteralExpression) {
                    Object constant = JavaConstantExpressionEvaluator.computeConstantExpression((PsiExpression) memberValue2, false);
                    return constant == null ? null : constant.toString();
                }
            }
        }
        try {
            if (reference != null) {
                PsiElement resolve = reference.resolve();
                if (resolve instanceof PsiEnumConstant) {
                    // 枚举常量
                    return ((PsiEnumConstant) resolve).getName();
                } else if (resolve instanceof PsiField) {
                    // 引用其他字段
                    return getFieldDefaultValue((PsiField) resolve);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "";
    }

    private String getFieldDefaultValue(PsiField psiField) {
        PsiExpression initializer = psiField.getInitializer();
        if (initializer instanceof PsiLiteralExpression) {
            return Objects.requireNonNull(((PsiLiteralExpression) initializer).getValue()).toString();
        }
        if (initializer instanceof PsiReferenceExpression) {
            PsiElement resolve = ((PsiReferenceExpression) initializer).resolve();
            if (resolve instanceof PsiField) {
                return getFieldDefaultValue((PsiField) resolve);
            }
        }
        return "";
    }

    private PsiAnnotation getAnnotation(PsiElement element, String annotationFQN) {
        if (element instanceof PsiClass) {
            return ((PsiClass) element).getAnnotation(annotationFQN);
        } else if (element instanceof PsiMethod) {
            return ((PsiMethod) element).getAnnotation(annotationFQN);
        }
        return null;
    }

    public void clearCache(){
        urlMethodCache.clear();
        controllerUrlsCache.clear();
        System.out.println("ControllerUrlService clear cache");
    }

}