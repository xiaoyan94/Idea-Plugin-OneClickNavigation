# IntelliJ Platform Plugin OneClickNavigation for CloudMes

> **Note**
>
> 仅适用于 IntelliJ IDEA 中的 CloudMes 项目。

<!-- Plugin description -->

![i18n中文自动完成](https://idea-plugin.oss.vaetech.uk/i18n1.gif)
**[OneClickNavigation](https://github.com/xiaoyan94/Idea-Plugin-OneClickNavigation)** 是针对智引 Mes 项目开发的一个插件，用于提升日常开发效率。

此插件主要实现了以下功能：

- Dao接口方法声明和Service中的Dao方法调用，**一键跳转**到Mapper SQL
- **queryDaoDataT**方法参数和对应dao方法、xml**互相跳转**，**自动补全提示**（`Ctrl+空格`），方法参数错误提示。
- Moc 相关通用方法，提供Java一键跳转到 Moc xml文件。
- 大部分场景下的**I18n中文资源串错误提示，中文折叠显示**。已支持：
  - Java 中的 I18nUtils.getMessage 相关资源串方法
  - HTML 中 FreeMarker 模板的 message 指令
  - JSP 中的 message 标签
  - JavaScript 中的 i18n 方法
  - Layout 文件中 DataGrid 的 Title->value 和 Field->label
  - Imp*Mapper 文件中的 i18n
- 大部分场景下的I18n资源串**自动翻译**（**简繁英越**）和**自动替换key**（`Alt+Enter` 手动触发或💡**自动修复提示**）。支持场景同上。
- Imp*Mapper 导入模板文件中的 col 列字段**一键自动排序**。
- 一键打开当前 java 源文件编译后的 class 文件目录。
- 一键生成MOC文件。
- 一键生成Layout文件（测试中）。
- 一键百度搜索。
- 💡more useful features...


<!-- Plugin description end -->

> **Note**
>
> nothing.

### 目录

插件相关：

- [Getting started](#getting-started)
- [FAQ](#faq)
- [Useful links](#useful-links)


## Getting started

## FAQ

## Useful links

- [IntelliJ Platform SDK Plugin SDK][docs]
- [Gradle IntelliJ Plugin Documentation][gh:gradle-intellij-plugin-docs]
- [IntelliJ Platform Explorer][jb:ipe]
- [JetBrains Marketplace Quality Guidelines][jb:quality-guidelines]
- [IntelliJ Platform UI Guidelines][jb:ui-guidelines]
- [JetBrains Marketplace Paid Plugins][jb:paid-plugins]
- [Kotlin UI DSL][docs:kotlin-ui-dsl]
- [IntelliJ SDK Code Samples][gh:code-samples]
- [JetBrains Platform Slack][jb:slack]
- [JetBrains Platform Twitter][jb:twitter]
- [IntelliJ IDEA Open API and Plugin Development Forum][jb:forum]
- [Keep a Changelog][keep-a-changelog]
- [GitHub Actions][gh:actions]

[Github]: https://github.com/xiaoyan94/Idea-Plugin-OneClickNavigation

[docs]: https://plugins.jetbrains.com/docs/intellij?from=IJPluginTemplate
[docs:intellij-platform-kotlin-oom]: https://plugins.jetbrains.com/docs/intellij/using-kotlin.html#incremental-compilation
[docs:intro]: https://plugins.jetbrains.com/docs/intellij/intellij-platform.html?from=IJPluginTemplate
[docs:kotlin-ui-dsl]: https://plugins.jetbrains.com/docs/intellij/kotlin-ui-dsl-version-2.html?from=IJPluginTemplate
[docs:kotlin]: https://plugins.jetbrains.com/docs/intellij/using-kotlin.html?from=IJPluginTemplate
[docs:kotlin-stdlib]: https://plugins.jetbrains.com/docs/intellij/using-kotlin.html?from=IJPluginTemplate#kotlin-standard-library
[docs:plugin.xml]: https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html?from=IJPluginTemplate
[docs:publishing]: https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate
[docs:release-channel]: https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate#specifying-a-release-channel
[docs:using-gradle]: https://plugins.jetbrains.com/docs/intellij/developing-plugins.html?from=IJPluginTemplate
[docs:plugin-signing]: https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate
[docs:project-structure-settings]: https://www.jetbrains.com/help/idea/project-settings-and-structure.html
[docs:testing-plugins]: https://plugins.jetbrains.com/docs/intellij/testing-plugins.html?from=IJPluginTemplate

[file:gradle.properties]: ./gradle.properties
[file:plugin.xml]: ./src/main/resources/META-INF/plugin.xml

[gh:actions]: https://help.github.com/en/actions
[gh:build]: https://github.com/JetBrains/intellij-platform-plugin-template/actions?query=workflow%3ABuild
[gh:code-samples]: https://github.com/JetBrains/intellij-sdk-code-samples
[gh:dependabot]: https://docs.github.com/en/free-pro-team@latest/github/administering-a-repository/keeping-your-dependencies-updated-automatically
[gh:dependabot-pr]: https://github.com/JetBrains/intellij-platform-plugin-template/pull/73
[gh:gradle-changelog-plugin]: https://github.com/JetBrains/gradle-changelog-plugin
[gh:gradle-intellij-plugin]: https://github.com/JetBrains/gradle-intellij-plugin
[gh:gradle-intellij-plugin-docs]: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
[gh:gradle-intellij-plugin-runIde]: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#tasks-runide
[gh:gradle-intellij-plugin-runPluginVerifier]: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#tasks-runpluginverifier
[gh:gradle-qodana-plugin]: https://github.com/JetBrains/gradle-qodana-plugin
[gh:intellij-ui-test-robot]: https://github.com/JetBrains/intellij-ui-test-robot
[gh:kover]: https://github.com/Kotlin/kotlinx-kover
[gh:releases]: https://github.com/JetBrains/intellij-platform-plugin-template/releases
[gh:ui-test-example]: https://github.com/JetBrains/intellij-ui-test-robot/tree/master/ui-test-example

[gradle]: https://gradle.org
[gradle:build-cache]: https://docs.gradle.org/current/userguide/build_cache.html
[gradle:configuration-cache]: https://docs.gradle.org/current/userguide/configuration_cache.html
[gradle:kotlin-dsl]: https://docs.gradle.org/current/userguide/kotlin_dsl.html
[gradle:kotlin-dsl-assignment]: https://docs.gradle.org/current/userguide/kotlin_dsl.html#kotdsl:assignment
[gradle:lifecycle-tasks]: https://docs.gradle.org/current/userguide/java_plugin.html#lifecycle_tasks
[gradle:releases]: https://gradle.org/releases
[gradle:version-catalog]: https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog

[jb:github]: https://github.com/JetBrains/.github/blob/main/profile/README.md
[jb:download-ij]: https://www.jetbrains.com/idea/download
[jb:forum]: https://intellij-support.jetbrains.com/hc/en-us/community/topics/200366979-IntelliJ-IDEA-Open-API-and-Plugin-Development
[jb:ipe]: https://jb.gg/ipe
[jb:my-tokens]: https://plugins.jetbrains.com/author/me/tokens
[jb:paid-plugins]: https://plugins.jetbrains.com/docs/marketplace/paid-plugins-marketplace.html
[jb:qodana]: https://www.jetbrains.com/help/qodana
[jb:qodana-github-action]: https://www.jetbrains.com/help/qodana/qodana-intellij-github-action.html
[jb:quality-guidelines]: https://plugins.jetbrains.com/docs/marketplace/quality-guidelines.html
[jb:slack]: https://plugins.jetbrains.com/slack
[jb:twitter]: https://twitter.com/JBPlatform
[jb:ui-guidelines]: https://jetbrains.github.io/ui

[codecov]: https://codecov.io
[github-actions-skip-ci]: https://github.blog/changelog/2021-02-08-github-actions-skip-pull-request-and-push-workflows-with-skip-ci/
[keep-a-changelog]: https://keepachangelog.com
[keep-a-changelog-how]: https://keepachangelog.com/en/1.0.0/#how
[semver]: https://semver.org
[xpath]: https://www.w3.org/TR/xpath-21/