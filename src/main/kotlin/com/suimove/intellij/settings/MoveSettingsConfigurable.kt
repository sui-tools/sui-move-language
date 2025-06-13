package com.suimove.intellij.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.util.NlsContexts
import javax.swing.JComponent

class MoveSettingsConfigurable : Configurable {
    private var settingsComponent: MoveSettingsComponent? = null
    
    @NlsContexts.ConfigurableName
    override fun getDisplayName(): String = "Sui Move"
    
    override fun createComponent(): JComponent? {
        settingsComponent = MoveSettingsComponent()
        return settingsComponent?.panel
    }
    
    override fun isModified(): Boolean {
        val settings = MoveSettings.instance
        return settingsComponent?.suiCliPath != settings.suiCliPath
    }
    
    @Throws(ConfigurationException::class)
    override fun apply() {
        val settings = MoveSettings.instance
        settings.suiCliPath = settingsComponent?.suiCliPath ?: ""
    }
    
    override fun reset() {
        val settings = MoveSettings.instance
        settingsComponent?.suiCliPath = settings.suiCliPath
    }
    
    override fun disposeUIResources() {
        settingsComponent = null
    }
}
