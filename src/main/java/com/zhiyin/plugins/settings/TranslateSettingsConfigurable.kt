package com.zhiyin.plugins.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*

/**
 * 翻译设置UI面板
 */
class TranslateSettingsConfigurable :
    BoundConfigurable("翻译设置", "OneClickNavigation Translate Settings") {

    private val settings = TranslateSettingsComponent.getInstance()

    /**
     * 创建面板
     */
    override fun createPanel(): DialogPanel {
        return panel {
            group("功能设置") {
                row {
                    checkBox("Feign接口跳转RestController")
                        .bindSelected(
                            { settings.state.enableFeignToRestController},
                            { settings.state.enableFeignToRestController = it})

                    contextHelp("若担心影响性能，则可取消勾选。修改后需重启项目生效。", "开启Feign接口和RestController的互相跳转")
                }

                row {
                    checkBox("HTML/JS: URL跳转Controller")
                        .bindSelected(
                            { settings.state.enableHtmlUrlToController},
                            { settings.state.enableHtmlUrlToController = it})

                    contextHelp("若担心影响性能，则可取消勾选。修改后需重启项目生效。", "添加URL到Controller的跳转图标")
                }

                row {
                    checkBox("HTML/JS: i18n key值检查")
                        .bindSelected(
                            { settings.state.enableHtmlAnnotator},
                            { settings.state.enableHtmlAnnotator = it})

                    contextHelp("若担心影响性能，则可取消勾选。修改后需重启项目生效。", "Html 添加i18n Annotator")
                }
            }
            group("资源串翻译设置") {
                row {
                    checkBox("无需确认直接复用Key")
                        .bindSelected(
                            { settings.state.doOKActionWhenI18nKeyExists},
                            { settings.state.doOKActionWhenI18nKeyExists = it})

                    contextHelp("当资源串已经存在时，不用点确认按钮，直接执行复用。若需确认，请取消勾选。", "直接复用已经存在的资源串")
                }
            }
            group("翻译提供方") {
                buttonsGroup("翻译接口") {
                    row {
                        radioButton("有道翻译", "YouDao")
                            .bindSelected(
                                { settings.state.apiProvider == "YouDao" },
                                { settings.state.apiProvider = "YouDao" })

                        radioButton("百度翻译", "Baidu")
                            .bindSelected(
                                { settings.state.apiProvider == "Baidu" },
                                { settings.state.apiProvider = "Baidu" })
//                            .validation { if(it.isSelected) error("百度翻译暂未支持") else null }

                        radioButton("微软必应翻译", "Microsoft")
                            .bindSelected(
                                { settings.state.apiProvider == "Microsoft" },
                                { settings.state.apiProvider = "Microsoft" })
                            .selected

                        contextHelp("插件内置了翻译接口的密钥，请不要泄露。另外，也可以在下方填写自己的密钥。", "可选自定义密钥")
                    }

                }.bind(settings.state::apiProvider)
            }

            group("有道翻译"){
                row("API URL"){
                    textField()
//                        .bindText({ settings.state.apiUrlYouDao}, { settings.state.apiUrlYouDao = it })
                        .bindText(settings.state::apiUrlYouDao)
                        .columns(COLUMNS_LARGE)
                }

                row("API Key"){
                    textField()
                        .bindText({ settings.state.apiKeyYouDao }, { settings.state.apiKeyYouDao = it })
//                        .bindText(settings.state::apiKeyYouDao)
                        .columns(COLUMNS_LARGE)
                }

                row("API Secret"){
                    textField()
                        .bindText(settings.state::apiSecretYouDao)
                        .columns(COLUMNS_LARGE)

                }

                row("VocabID"){
                    textField()
                        .bindText(settings.state::apiVocabIdYouDao)
                        .columns(COLUMNS_LARGE)

                    contextHelp("自定义术语表ID，非必填。", "自定义术语表ID")
                }
            }

            group("百度翻译"){
                row("API URL"){
                    textField()
                        .bindText(settings.state::apiUrlBaidu)
                        .columns(COLUMNS_LARGE)
                }

                row("API AppID"){
                    textField()
                        .bindText(settings.state::apiAppIdBaidu)
                        .columns(COLUMNS_LARGE)
                }

                row("API AppSecret"){
                    textField()
                        .bindText(settings.state::apiAppSecretBaidu)
                        .columns(COLUMNS_LARGE)
                }
            }

            group("微软必应翻译"){
                row("API URL"){
                    textField()
                        .bindText(settings.state::apiUrlMicrosoft)
                        .columns(COLUMNS_LARGE)
                }

                row("API Secret"){
                    textField()
                        .bindText(settings.state::apiSecretMicrosoft)
                        .columns(COLUMNS_LARGE)
                }

                row("Endpoint Region"){
                    textField()
                        .bindText(settings.state::apiRegionMicrosoft)
                        .columns(COLUMNS_LARGE)

                    contextHelp("不填则默认为 eastasia")
                }
            }

        }
    }

    /**
     * 保存
     */
    override fun apply() {
        super.apply()
        settings.loadState(settings.state)
    }

    /**
     * 判断是否修改
     */
    override fun isModified(): Boolean {
        return super.isModified()
    }

    /**
     * 重置
     */
    override fun reset() {
        super.reset()
    }
}