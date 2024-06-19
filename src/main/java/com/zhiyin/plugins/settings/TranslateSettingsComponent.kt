package com.zhiyin.plugins.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

// 定义状态注解，指定组件名称和存储位置
@State(name = "TranslateSettings", storages = [Storage("OneClickNavigationTranslateSettings.xml")])
// 定义服务注解，默认为应用级别服务
@Service
// 定义配置类，必须实现PersistentStateComponent接口
class TranslateSettingsComponent : PersistentStateComponent<TranslateSettingsState> {

    // 存储当前设置项状态的变量
    private var state = TranslateSettingsState()

    companion object{
        /**
         * 定义一个单例方法，用于获取当前配置类的实例
         */
        fun getInstance(): TranslateSettingsComponent {
            return ApplicationManager.getApplication().getService(TranslateSettingsComponent::class.java)
        }
    }


    /**
     * @return a component state. All properties, public and annotated fields are serialized. Only values, which differ
     * from the default (i.e., the value of newly instantiated class) are serialized. `null` value indicates
     * that the returned state won't be stored, as a result previously stored state will be used.
     * @see com.intellij.util.xmlb.XmlSerializer
     */
    override fun getState(): TranslateSettingsState {
        return state
    }

    /**
     * This method is called when new component state is loaded. The method can and will be called several times, if
     * config files were externally changed while IDE was running.
     *
     *
     * State object should be used directly, defensive copying is not required.
     *
     * @param state loaded component state
     * @see com.intellij.util.xmlb.XmlSerializerUtil.copyBean
     */
    override fun loadState(state: TranslateSettingsState) {
        this.state = state
    }
}