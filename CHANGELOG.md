<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# OneClickNavigation Changelog

![i18n中文自动完成](https://idea-plugin.oss.vaetech.uk/i18n1.gif)

## [Unreleased]

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
