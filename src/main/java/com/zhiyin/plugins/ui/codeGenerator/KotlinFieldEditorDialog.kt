package com.zhiyin.plugins.ui.codeGenerator

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.*
import javax.swing.JComponent

class KotlinFieldEditorDialog(private val field: Field) : DialogWrapper(true) {

    init {
        init()
        title = "编辑字段"
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row("字段名:") {
                textField()
                    .bindText(field::name)
                    .validationOnApply {
                        if (it.text.isEmpty()) {
                            error("字段名不能为空")
                        } else {
                            null
                        }
                    }
            }
            row("字段标题:") {
                textField()
                    .bindText(field::comment)
            }
            row("类型:") {
                textField()
                    .bindText(field::type)
                    .text(field.type.ifEmpty { "string" })
                    .validationOnApply {
                        if (it.text.isEmpty()) {
                            error("类型不能为空")
                        } else {
                            null
                        }
                    }
            }
            row("长度:") {
                textField()
                    .bindIntText(field::length)
            }
            row("是否必填:") {
                checkBox("")
                    .bindSelected(field::isRequired)
            }
            row("是否查询字段:") {
                checkBox("")
                    .bindSelected(field::isQueryField)
            }
            row("是否对话框字段:"){
                checkBox("")
                    .bindSelected(field::isDialogField)
            }
        }
    }

    companion object {
        fun showDialog(field: Field, callback: (Field) -> Unit) {
            val dialog = KotlinFieldEditorDialog(field)
            if (dialog.showAndGet()) {
                callback(field)
            }
        }
    }

}
