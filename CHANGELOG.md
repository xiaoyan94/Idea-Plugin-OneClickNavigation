<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# OneClickNavigation Changelog

![i18n中文自动完成](https://idea-plugin.oss.vaetech.uk/i18n1.gif)

## [Unreleased]

## [1.0.34] - 2025-09-03

### Added

- [x] 拼写检查：自动加载数据库表及字段信息、MocName等生成词典，并自动完成自定义词典配置；
- [x] 代码提示：将数据库表及字段信息、MocName等上述信息加入字面量的代码提示中，`Ctrl`+`空格` 触发代码提示。

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

[Unreleased]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-Unreleased.zip
[1.0.34]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.34.zip
[1.0.33]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.33.zip
[1.0.32]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.32.zip
[1.0.31]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.31.zip
[1.0.30]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.30.zip
[1.0.29]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.29.zip
[1.0.28]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.28.zip
[1.0.27]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.27.zip
[1.0.26]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.26.zip
[1.0.25]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.25.zip
[1.0.24]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.24.zip
[1.0.23]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.23.zip
[1.0.22]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.22.zip
[1.0.21]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.21.zip
[1.0.20]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.20.zip
[1.0.19]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.19.zip
[1.0.18]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.18.zip
[1.0.17]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.17.zip
[1.0.16]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.16.zip
[1.0.15]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.15.zip
[1.0.14]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.14.zip
[1.0.13]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.13.zip
[1.0.12]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.12.zip
[1.0.11]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.11.zip
[1.0.10]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.10.zip
[1.0.9]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.9.zip
[1.0.8]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.8.zip
[1.0.7]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.7.zip
[1.0.6]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.6.zip
[1.0.5]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.5.zip
[1.0.4]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.4.zip
[1.0.3]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.3.zip
[1.0.2]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.2.zip
[1.0.1]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.1.zip
[1.0.0]: https://idea-plugin.oss.vaetech.uk/OneClickNavigation-1.0.0.zip
