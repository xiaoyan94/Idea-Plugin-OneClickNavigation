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

**历史版本：**

## [1.0.33] - 2025-09-02

### Improved

- [x] 优化 一键生成基础代码功能
- [x] 添加 添加复制格式化后的SQL 功能，包括自动移除动态标签、替换占位符、去除多余空白等

## [1.0.32] - 2025-08-27

### Added

- [x] 添加 Java 代码中右键直接插入 I18n 资源串翻译方法的功能
- [x] 添加 JS 代码中右键直接插入 I18n 资源串翻译函数的功能
- [x] 添加 代码生成根据所勾选的文件类型进行指定生成 功能

### Improved

- [x] 增加 Spring Cloud MES 项目检查逻辑
- [x] 优化 SQL 格式化和表单字段类型识别
- [x] 添加 JavaScript 国际化函数插入功能
- [x] 优化 代码一键生成功能；
- [x] 改进 Layout 和 HTML 文件互跳
- [x] 修复 GenerateExcelHeaderAction 中的错误提示

## [1.0.31] - 2025-08-27

### Added

- [x] 添加 JavaScript 国际化函数插入功能

### Improved

- [x] 增加 Spring Cloud MES 项目检查逻辑
- [x] 优化 SQL 格式化和表单字段类型识别
- [x] 添加 JavaScript 国际化函数插入功能
- [x] 优化 代码一键生成功能；
- [x] 改进 Layout 和 HTML 文件互跳
- [x] 修复 GenerateExcelHeaderAction 中的错误提示

## [1.0.30] - 2025-07-28

### Added

- [x] 添加 Excel 多语言导入模板一键生成功能：根据 Imp**Mapper 文件自动生成多语言版本 Excel 导入模板

## [1.0.29] - 2025-07-09

### Improved

- [x] 资源串适配智塑云新增的wms2、technics2模块

## [1.0.28] - 2025-04-23

### Added

- [x] 添加 OneClickNavigation 工具窗口功能
- [x] 实现了基于 URL 模糊匹配的控制器方法查找和跳转功能
- [x] 新增工具窗口界面，支持输入接口 URL 并展示匹配的控制器方法列表
- [x] 添加了自定义列表渲染器，优化了方法列表的显示效果
- [x] 实现了自动导航和手动选择导航的功能

### Improved

- [x] OneClickNavigation 工具窗口：增加缓存初始化按钮
- [x] 限制查询结果最多50条

### Fixed

- [x] 修复不同模块类全限定名重复导致找不到接口方法的问题

## [1.0.27] - 2025-04-23

### Added

- [x] 添加 OneClickNavigation 工具窗口功能
- [x] 实现了基于 URL 模糊匹配的控制器方法查找和跳转功能
- [x] 新增工具窗口界面，支持输入接口 URL 并展示匹配的控制器方法列表
- [x] 添加了自定义列表渲染器，优化了方法列表的显示效果
- [x] 实现了自动导航和手动选择导航的功能

### Improved

- [x] OneClickNavigation 工具窗口：增加缓存初始化按钮
- [x] 限制查询结果最多50条

### Fixed

- [x] 修复已知问题

## [1.0.26] - 2025-04-23

### Added

- [x] 添加 OneClickNavigation 工具窗口功能
- [x] 实现了基于 URL 模糊匹配的控制器方法查找和跳转功能
- [x] 新增工具窗口界面，支持输入接口 URL 并展示匹配的控制器方法列表
- [x] 添加了自定义列表渲染器，优化了方法列表的显示效果
- [x] 实现了自动导航和手动选择导航的功能

### Improved

- [x] OneClickNavigation 工具窗口：增加缓存初始化
- [x] 限制查询结果最多50条

## [1.0.25] - 2025-04-23

### Added

- [x] 添加 OneClickNavigation 工具窗口功能
- [x] 实现了基于 URL 模糊匹配的控制器方法查找和跳转功能
- [x] 新增工具窗口界面，支持输入接口 URL 并展示匹配的控制器方法列表
- [x] 添加了自定义列表渲染器，优化了方法列表的显示效果
- [x] 实现了自动导航和手动选择导航的功能

## [1.0.24] - 2025-03-26

### Added

- [x] 增加从控制台Mybatis输出log中提取SQL语句功能

## [1.0.23] - 2025-01-20

### Added

- [x] 记录查询类页面代码自动生成功能（1.0版，支持 6 种文件一键生成，减小工作量）

## [1.0.22] - 2024-12-20

### Improved

- [x] 提升IntentionAction优先级

## [1.0.21] - 2024-12-20

### Improved

- [x] fix:优化资源串翻译插件在layout中的使用体验
  - 优化了 XML 文件中 Title标签下 value 属性的获取逻辑

## [1.0.20] - 2024-09-02

### Added

- [x] 实现对Moc名称引用的解析与代码补全;

### Improved

- [x] 使用体验优化，增加功能启用开关；

## [1.0.19] - 2024-08-14

### Improved

- 使用体验优化

## [1.0.18] - 2024-08-13

### Added

- [x] 支持Feign客户端URL到RestController的一键跳转
- [x] 支持RestController URL到调用它的Feign客户端的反向查找跳转
- [x] 支持Html中的URL到Controller的一键跳转
- [x] 支持Html中的requestUri到Layout文件的一键跳转
- [x] 支持Layout中的URL到Controller的一键跳转
- [x] 资源串默认带出的 key 去除项目名
- [x] xml中选中文本一键转换为CDATA块
- [x] 支持Layout中下拉框的代码完成提示，选取已存在的RL

## [1.0.17] - 2024-08-12

### Added

- [x] 支持Feign客户端URL到RestController的一键跳转
- [x] 支持RestController URL到调用它的Feign客户端的反向查找跳转
- [x] 支持Html中的URL到Controller的一键跳转
- [x] 支持Html中的requestUri到Layout文件的一键跳转
- [x] 支持Layout中的URL到Controller的一键跳转
- [x] 资源串默认带出的 key 去除项目名
- [x] xml中选中文本一键转换为CDATA块

## [1.0.16] - 2024-08-12

### Added

- [x] xml中选中文本一键转换为CDATA块
- [x] 资源串默认带出的 key 去除项目名
- [x] 支持Feign客户端URL到RestController的一键跳转

## [1.0.15] - 2024-07-25

### Added

- 增强已存在中文资源串的复用体验，添加了代码自动完成提示 (输中文再按`Ctrl`+`空格`) 和一键输入（Java、JS、Xml、Html）；
- 添加了一些代码实时模板，提升相关代码输入效率：
  - Java: `i18n`, `i18np`, `gi`, `gs`, `gd`, `dict`
  - MyBatis Xml: `se`, `up`, `wh`, `if`, `for`, `date_format`
- 添加 @SysLogger 注解被错误地使用在查询类方法上时的提示和一键修复
- 添加 StringUtils.objToString 方法的不建议使用提示和一键修复

### Improved

- 优化代码跳转使用体验，点击图标前记住行偏移量，使用返回按钮或者`Ctrl Alt ←`时可以回到上次跳转所在行位置；

### Alpha

- 代码生成器 (Alpha 测试)

## [1.0.14] - 2024-07-23

### Added

- 增强中文资源串的复用体验，添加了代码自动完成提示 (输中文再按`Ctrl`+`空格`) 和一键输入；
- 添加了一些代码实时模板，提升相关代码输入效率：
  - JAVA: `i18n`, `i18np`, `gi`, `gs`, `gd`, `dict`
  - MyBatis: `se`, `up`, `wh`, `if`, `for`, `date_format`
- 添加 @SysLogger 注解被错误地使用在查询类方法上时的提示和一键修复
- 添加 StringUtils.objToString 方法的不建议使用提示和一键修复
- 代码生成器 (Alpha)

## [1.0.13] - 2024-07-09

### Fixed

- 解决找不到framework system里的资源串的问题；
- 解决老的JavaWeb项目找不到web依赖的其他模块下的资源串的问题；

## [1.0.12] - 2024-07-06

### Added

- 增加百度翻译接口

### Fixed

- 优化微软必应翻译接口，在没有指定源语言时，会将某些中文识别为日文的问题
- 优化老项目资源串只有简中英，没有繁体、越南语导致的UI问题：翻译弹窗获取不到复用值

## [1.0.11] - 2024-07-05

### Added

- 添加了越南语支持.
- 翻译弹窗：增加翻译引擎选择按钮和清空缓存按钮.
- 支持JSP中折叠显示I18n中文资源串.
- 支持JSP中自动翻译和插入I18n标签.

### Fixed

- 优化翻译弹窗
- 优化翻译结果缓存逻辑

## [1.0.10] - 2024-07-04

### Added

- Java方法中已存在资源串时复用key.
- 支持JSP中折叠显示I18n中文资源串.
- 支持JSP中自动翻译和插入I18n标签.

## [1.0.9] - 2024-06-21

### Added

- 添加右键百度搜索.

## [1.0.8] - 2024-06-20

### Added

- 添加MyBatis Mapper Xml文件SQL标签到Dao接口方法的引用.

## [1.0.7] - 2024-06-19

### Fixed

- 修复了部分问题.

## [1.0.6]

### Added

- 添加了一些功能。

### Fixed

- 修复了一些问题。

## [1.0.5]

### Added

- 支持BizCommonService方法调用一键跳转到Moc文件.

## [1.0.4]

### Fixed

- Java中I18n方法的代码折叠.

## [1.0.3]

### Fixed

- Layout/ImpMapper中代码折叠偏移量问题.

## [1.0.2]

### Added

- 支持XML(Layout/ImpMapper)中折叠显示I18n中文资源串.

## [1.0.1]

### Changed

- 支持新版Idea (241).

## [1.0.0]

### Added

- 支持Dao接口, MyBatis mapper文件互相跳转;
- 支持Service调用Dao方法直接跳转到mapper文件;
- 支持queryDaoDataT, 直接调转到mapper文件; queryDaoDataT的方法参数与dao接口方法互相跳转, 自动补全;
- 支持Java代码中折叠显示I18n中文资源串, 以及资源串未配置检测;
- 支持JavaScript代码中折叠显示I18n中文资源串;

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