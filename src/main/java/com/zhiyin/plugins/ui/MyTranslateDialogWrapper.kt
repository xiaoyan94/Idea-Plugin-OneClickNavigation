package com.zhiyin.plugins.ui

import com.intellij.icons.AllIcons
import com.intellij.lang.properties.psi.Property
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.zhiyin.plugins.actions.ClearTranslationCacheAction
import com.zhiyin.plugins.notification.MyPluginMessages
import com.zhiyin.plugins.settings.TranslateSettingsComponent
import com.zhiyin.plugins.utils.MyPropertiesUtil
import com.zhiyin.plugins.utils.StringUtil.Companion.toCamelCase
import com.zhiyin.plugins.utils.StringUtil.Companion.toSnakeCase
import com.zhiyin.plugins.utils.StringUtil.Companion.toUnicode
import com.zhiyin.plugins.utils.StringUtil.Companion.unicodeToString
import com.zhiyin.plugins.utils.TranslateUtil
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import java.util.function.Predicate
import javax.swing.JButton
import javax.swing.SwingUtilities

class MyTranslateDialogWrapper(private val project: Project?, private val module: Module?) : DialogWrapper(project) {

    private var canAutoDoOKActionWhenI18nKeyExists: Boolean = true
    private val moduleName = module?.name
    private val simpleModuleName = module?.let { MyPropertiesUtil.getSimpleModuleName(module); }
    private val projectName = project?.name

    val inputModel = InputModel()

    init {
        title = "创建资源串 ${moduleName ?: projectName} - $simpleModuleName"
//        isResizable = true
//        isModal = false
        init()
        // 监听回车事件
        sourceCHSTextField.component.addActionListener {
//            println("Enter pressed")
            translateButton.component.doClick()
        }
        // 监听回车事件
//        sourceCHSTextField.component.addKeyListener(object : java.awt.event.KeyAdapter() {
//            override fun keyPressed(e: java.awt.event.KeyEvent) {
//                if (e.keyCode == java.awt.event.KeyEvent.VK_ENTER) {
//                    println("Enter Key pressed")
//                }
//            }
//        })
    }

    /**
     * 创建对话框
     */
    override fun createCenterPanel(): DialogPanel {
        return createPanel()
    }

    private lateinit var i18nKeyTextField: Cell<JBTextField>
    private lateinit var sourceCHSTextField: Cell<JBTextField>
    private lateinit var chineseUnicodeTextField: Cell<JBTextField>
    private lateinit var englishTextField: Cell<JBTextField>
    private lateinit var englishUnicodeTextField: Cell<JBTextField>
    private lateinit var chineseTWTextField: Cell<JBTextField>
    private lateinit var chineseTWUnicodeTextField: Cell<JBTextField>
    private lateinit var vietnameseTextField: Cell<JBTextField>
    private lateinit var vietnameseUnicodeTextField: Cell<JBTextField>
    private lateinit var translateButton: Cell<JButton>

    /**
     * 创建对话框
     */
    private fun createPanel(): DialogPanel {
        return panel {
            group("原文:") {
                row("简体:") {
                    sourceCHSTextField = textField().columns(COLUMNS_LARGE).focused().validationOnApply {
                        if (it.text.isEmpty()) {
                            error("Please input source text")
                        } else {
                            null
                        }
                    }.validationOnInput {
                        SwingUtilities.invokeLater {
                            translateButton.component.isEnabled = it.text.isNotEmpty()
                            chineseUnicodeTextField.component.text = it.text.toUnicode()
                            // 修改中文时清空翻译和状态
                            englishTextField.component.text = ""
                            englishUnicodeTextField.component.text = ""
                            chineseTWTextField.component.text = ""
                            vietnameseTextField.component.text = ""
                            sourceCHSTextField.comment?.text = "Enter to translate/按回车键翻译"
                            i18nKeyTextField.component.text = ""
                            inputModel.propertyKeyExists = false
                        }
                        null
                    }.bindText(inputModel::chinese).comment("")

                    translateButton = button(getTranslateButtonText(), doTranslate())

                    actionsButton(object : DumbAwareAction("有道翻译") {
                        override fun actionPerformed(e: AnActionEvent) {
                            updateApiProviderAndButtonText("YouDao")
                        }
                    }, object : DumbAwareAction("微软翻译") {
                        override fun actionPerformed(e: AnActionEvent) {
                            updateApiProviderAndButtonText("Microsoft")
                        }
                    }, object : DumbAwareAction("百度翻译") {
                        override fun actionPerformed(e: AnActionEvent) {
                            updateApiProviderAndButtonText("Baidu")
                        }
                    })

                    val action = object : DumbAwareAction("清空缓存", "清空翻译缓存", AllIcons.Actions.GC) {
                        override fun actionPerformed(e: AnActionEvent) {
                            ClearTranslationCacheAction.doAction()
                        }
                    }
                    actionButton(action)
                }
            }
            group("翻译结果:") {
//                row("简体中文:") {
//                    chineseTextField = textField().columns(COLUMNS_LARGE).validation {
//                        if (it.text.isEmpty()) {
//                            error("Chinese is required")
//                        } else {
//                            null
//                        }
//                    }.bindText(inputModel::chinese)
//                }
                row("简体Unicode:") {
                    chineseUnicodeTextField =
                        textField().columns(COLUMNS_LARGE).enabled(false)
                            .validation {
                                if (it.text.isEmpty() && it.isEnabled) {
                                    error("Chinese Unicode is required")
                                } else {
                                    null
                                }
                            }.bindText(inputModel::chineseUnicode)
                }

                row("英文:") {
                    englishTextField = textField().columns(COLUMNS_LARGE).enabled(true).validationOnInput {
                        if (it.text.isEmpty() && it.isEnabled) {
                            error("请输入英文")
                        } else {
                            englishUnicodeTextField.component.text = it.text.toUnicode()
                            // 复用已存在key时需保持不更改
                            if (!inputModel.propertyKeyExists) {
                                i18nKeyTextField.component.text = "com.zhiyin.mes.app.${simpleModuleName}.${it.text.toSnakeCase()}"
                            }
                            null
                        }
                    }.bindText(inputModel::english).comment(inputModel.englishUnicode)
                }
                row("英文Unicode") {
                    englishUnicodeTextField =
                        textField().columns(COLUMNS_LARGE).enabled(false)
                            .validation {
                                if (it.text.isEmpty() && !inputModel.propertyKeyExists) {
                                    error("English Unicode is required")
                                } else {
                                    null
                                }
                            }.bindText(inputModel::englishUnicode)

                }

                row("繁体:") {
                    chineseTWTextField = textField().columns(COLUMNS_LARGE).enabled(true).validation {
                        if (it.text.isEmpty() && it.isEnabled && !inputModel.propertyKeyExists) {
                            error("请输入繁体")
                        } else {
                            chineseTWUnicodeTextField.component.text = it.text.toUnicode()
                            null
                        }
                    }.bindText(inputModel::chineseTW)
                }
                row("繁体Unicode") {
                    chineseTWUnicodeTextField = textField().columns(COLUMNS_LARGE).enabled(false).validation {
                        if (it.text.isEmpty() && it.isEnabled && !inputModel.propertyKeyExists) {
                            error("ChineseTW Unicode is required")
                        } else {
                            null
                        }
                    }.bindText(inputModel::chineseTWUnicode)
                }

                row("越南语:") {
                    vietnameseTextField = textField().columns(COLUMNS_LARGE).enabled(true).validation {
                        if (it.text.isEmpty() && it.isEnabled && !inputModel.propertyKeyExists) {
                            error("请输入越南语")
                        } else {
                            vietnameseUnicodeTextField.component.text = it.text.toUnicode()
                            null
                        }
                    }.bindText(inputModel::vietnamese)
                }
                row("越南语Unicode") {
                    vietnameseUnicodeTextField = textField().columns(COLUMNS_LARGE).enabled(false).validation {
                        if (it.text.isEmpty() && it.isEnabled && !inputModel.propertyKeyExists) {
                            error("越南语 Unicode 必填")
                        } else {
                            null
                        }
                    }.bindText(inputModel::vietnameseUnicode)
                }

                row("I18n key:") {
                    i18nKeyTextField =
                        textField().text("$moduleName").columns(COLUMNS_LARGE)
                            .validationOnInput {
//                                i18nKeyTextField.component.isEditable = !inputModel.propertyKeyExists
                                i18nKeyTextField.comment?.text = "Length: ${it.text.length}"
                                if (it.text.isEmpty()) {
                                    error("Property key is required")
                                } else {
                                    null
                                }
                            }.validationOnApply {
                                if (it.text.isEmpty()) {
                                    error("Property key is required")
                                } else if (checkI18nKeyExists(it.text) && !inputModel.propertyKeyExists) {
                                    i18nKeyTextField.comment?.text = "已有key=${it.text}"
                                    error("该 property key 和已有key冲突，不能重复")
                                } else {
                                    null
                                }
                            }.bindText(inputModel::propertyKey).comment("Length: 0")
                }
            }

        }
    }

    private fun getTranslateButtonText(): String {
        return when (TranslateSettingsComponent.getInstance().state.apiProvider ){
            "Baidu" -> "百度翻译"
            "YouDao" -> "有道翻译"
            "Microsoft" -> "微软翻译"
            else -> "翻译"
        }
    }

    private fun updateApiProviderAndButtonText(apiProvider: String) {
//        if ("Baidu" == apiProvider) {
//            MyPluginMessages.showError("暂不支持百度翻译", "待开发", project)
//            return
//        }
        val state = TranslateSettingsComponent.getInstance().state
        state.apiProvider = apiProvider
        TranslateSettingsComponent.getInstance().loadState(state)
        translateButton.component.text = getTranslateButtonText()

        translateButton.component.doClick()
    }

    /**
     * 检查新填写的 property key 是否已重复存在
     */
    private fun checkI18nKeyExists(key: String): Boolean {
        // 根据弹窗来源上下文，获取是否来自 DataGrid等
//        return !inputModel.propertyKeyExists && (MyPropertiesUtil.findModuleI18nProperties(project, module, key)
//            .isNotEmpty() || MyPropertiesUtil.findModuleDataGridI18nProperties(project, module, key).isNotEmpty())
        return isI18nKeyExists(key)
    }

    /**
     * 默认不存在
     */
    private var checkI18nKeyExistsFun: Predicate<String> = Predicate { false }

    /**
     * 设置判断key是否已存在的函数。
     * 写入PSI前调用
     */
    fun setCheckI18nKeyExistsFun(checkFunction: Predicate<String>) {
        checkI18nKeyExistsFun = checkFunction
    }

    private fun isI18nKeyExists(key: String): Boolean {
        return checkI18nKeyExistsFun.test(key)
    }

    /**
     * 获取已存在的 Property 的函数。默认实现：返回空列表（即不复用已存在key）
     */
    private var getPropertiesByValueFun: java.util.function.Function<String, List<Property>> = java.util.function.Function { emptyList() }

    /**
     * 设置根据值获取已存在的 Property 的函数，目的是复用 Key。
     * 在点击翻译时调用。
     * @param getI18nPropertiesFun 获取已有键值对的函数。函数输入值是中文，输出值是 Property 列表
     */
    fun setGetI18nPropertiesFun(getI18nPropertiesFun: java.util.function.Function<String, List<Property>>) {
        this.getPropertiesByValueFun = getI18nPropertiesFun
    }

    /**
     * 翻译按钮事件
     */
    private fun doTranslate(): (event: ActionEvent) -> Unit = {
        runReadAction {
//            val foundProperties = getPropertiesByValueFun.apply(inputModel.chinese)
            val foundProperties = getPropertiesByValueFun.apply(sourceCHSTextField.component.text)
            // 如果 key 已存在（并且中繁英 3 个 key 都存在），则使用该 key 值
            if (foundProperties.isNotEmpty() && foundProperties.size >= 2) {
                val key = foundProperties.first().key ?: ""
                val isNative2Ascii = MyPropertiesUtil.isNative2AsciiForPropertiesFiles()
                SwingUtilities.invokeLater {
                    // 正好存在可用 key
                    inputModel.propertyKeyExists = true
                    i18nKeyTextField.text(key)

                    if (foundProperties.size == 2) {
                        // 老项目只有中英翻译
                        englishTextField.text(foundProperties[1].value?.let { if (!isNative2Ascii) it.unicodeToString() else it } ?: "")
                    }
                    if (foundProperties.size >= 3) {
                        // 中繁英
                        chineseTWTextField.text(foundProperties[1].value?.let { if (!isNative2Ascii) it.unicodeToString() else it } ?: "")
                        englishTextField.text(foundProperties[2].value?.let { if (!isNative2Ascii) it.unicodeToString() else it } ?: "")
                    }
                    if (foundProperties.size >= 4) {
                        // 越南语
                        vietnameseTextField.text(foundProperties[3].value?.let { if (!isNative2Ascii) it.unicodeToString() else it } ?: "")
                    }

                    translateButton.component.isEnabled = true
                    sourceCHSTextField.comment?.text = "值已存在，已复制key到剪切板，key=${key}"
                    // 根据设置，直接复用Key
                    if (TranslateSettingsComponent.getInstance().state.doOKActionWhenI18nKeyExists
                        && canAutoDoOKActionWhenI18nKeyExists){
                        doOKAction()
                    } else {
                        val okButton = getButton(okAction)
                        okButton?.requestFocus()
                        okButton?.isEnabled = true
                        canAutoDoOKActionWhenI18nKeyExists = TranslateSettingsComponent.getInstance().state.autoClickTranslateButton
                    }
                }
                CopyPasteManager.getInstance().setContents(StringSelection(key))
            } else {
                // 如果key不存在，则翻译
                SwingUtilities.invokeLater{
                    translateButton.component.text = "翻译中..."
                    translateButton.component.isEnabled = false
                    sourceCHSTextField.component.isEditable = false
                    sourceCHSTextField.comment?.text = ""
                }

                Thread {
                    translateAndUpdateUI()
                }.start()
            }
        }
    }


    /**
     * 翻译和更新UI逻辑
     */
    private fun translateAndUpdateUI() {
        val chinese = sourceCHSTextField.component.text
        val english = TranslateUtil.translateToEN(chinese).toCamelCase()
        val cht = TranslateUtil.translateToCHT(chinese)
        val vi = TranslateUtil.translateToVIET(chinese)

        SwingUtilities.invokeLater {
            if (chinese.isNotEmpty()) {
//                val chineseUnicode = stringToUnicode(chinese);
                chineseUnicodeTextField.text(chinese.toUnicode())
            }

            englishTextField.text(english)
            if (english.isNotEmpty()) {
                englishUnicodeTextField.text(english.toUnicode())
                i18nKeyTextField.text("com.zhiyin.mes.app.${simpleModuleName}.${english.toSnakeCase()}")
            }

            chineseTWTextField.text(cht)
            if (cht.isNotEmpty()) {
                chineseTWUnicodeTextField.text(cht.toUnicode())
            }

            vietnameseTextField.text(vi)
            if (vi.isNotEmpty()) {
                vietnameseUnicodeTextField.text(vi.toUnicode())
            }

            translateButton.component.text = getTranslateButtonText()
            translateButton.component.isEnabled = true
            sourceCHSTextField.component.isEditable = true
            sourceCHSTextField.comment?.text = "Enter to input the key"

            invokeLater {
                getButton(okAction)?.requestFocus()
                runWriteAction {
                    CopyPasteManager.getInstance().setContents(StringSelection(i18nKeyTextField.component.text))
                }
            }
        }

    }

    override fun doOKAction() {
        // 不需要重写
        super.doOKAction()

        if (inputModel.propertyKeyExists) {
            MyPluginMessages.showInfo("已复用key", "${i18nKeyTextField.component.text}=${sourceCHSTextField.component.text}", project)
        } else {
            MyPluginMessages.showInfo("已翻译", "value=${sourceCHSTextField.component.text}, key=${i18nKeyTextField.component.text}", project)
        }
    }

    /**
     * 输入模型
     */
    data class InputModel(
        var propertyKeyExists: Boolean = false,
        var propertyKey: String = "",
        var chinese: String = "",
        var chineseUnicode: String = "",
        var english: String = "",
        var englishUnicode: String = "",
        var chineseTW: String = "",
        var chineseTWUnicode: String = "",
        var vietnamese: String = "",
        var vietnameseUnicode: String = ""
    )

    fun setSourceCHSText(sourceCHSText: String) {
        sourceCHSTextField.text(sourceCHSText)
    }

    /**
     * Returns the object corresponding to the specified data identifier. Some of the supported
     * data identifiers are defined in the [com.intellij.openapi.actionSystem.PlatformDataKeys] class.
     *
     * @-param dataId the data identifier for which the value is requested.
     * @return the value, or null if no value is available in the current context for this identifier.
     */
//    override fun getData(dataId: String): Any? {
//        return if (PlatformDataKeys.COPY_PROVIDER.`is`(dataId)) {
//            inputModel
//        } else {
//            null
//        }
//    }

    fun clickTranslateButton() {
        this.canAutoDoOKActionWhenI18nKeyExists = false
        translateButton.component.doClick()
    }
}