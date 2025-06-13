package com.suimove.intellij.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.suimove.intellij.settings.MoveSettings",
    storages = [Storage("SuiMoveSettings.xml")]
)
class MoveSettings : PersistentStateComponent<MoveSettings> {
    var suiCliPath: String = ""
    
    override fun getState(): MoveSettings = this
    
    override fun loadState(state: MoveSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
    
    companion object {
        val instance: MoveSettings
            get() = ApplicationManager.getApplication().getService(MoveSettings::class.java)
    }
}
