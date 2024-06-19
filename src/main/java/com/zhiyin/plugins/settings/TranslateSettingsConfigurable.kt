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
            group("资源串翻译设置") {
                row {
                    checkBox("无需确认直接复用Key")
                        .bindSelected(
                            { settings.state.doOKActionWhenI18nKeyExists},
                            { settings.state.doOKActionWhenI18nKeyExists = it})
                        .comment("当资源串已经存在时，是否不用点确认按钮，直接执行替换")
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
                            .validation { if(it.isSelected) error("百度翻译暂未支持") else null }

                        radioButton("微软必应翻译", "Microsoft")
                            .bindSelected(
                                { settings.state.apiProvider == "Microsoft" },
                                { settings.state.apiProvider = "Microsoft" })
                            .selected

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
                        .comment("自定义术语表ID，非必填")
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
                        .comment("不填则默认为eastasia")
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