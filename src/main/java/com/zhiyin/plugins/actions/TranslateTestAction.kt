package com.zhiyin.plugins.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.ui.Messages
import com.zhiyin.plugins.ui.MyTranslateDialogWrapper

class TranslateTestAction : AnAction() {
    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    override fun actionPerformed(e: AnActionEvent) {
        val myTranslateDialogWrapper = MyTranslateDialogWrapper(e.project, e.getRequiredData(PlatformDataKeys.MODULE))
//        myTranslateDialogWrapper.show()

        if(myTranslateDialogWrapper.showAndGet()){ // modal
            Messages.showInfoMessage(myTranslateDialogWrapper.inputModel.propertyKey, "获取i18n key")
        }
    }

    /**
     * 仅打开项目模块时可用
     */
    override fun update(e: AnActionEvent) {
        val module = e.getData(PlatformDataKeys.MODULE)
        e.presentation.isEnabled = module != null
    }

}