package com.suimove.intellij.formatter

import com.intellij.application.options.CodeStyleAbstractConfigurable
import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.application.options.TabbedLanguageCodeStylePanel
import com.intellij.psi.codeStyle.CodeStyleConfigurable
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider
import com.suimove.intellij.MoveLanguage

class MoveCodeStyleSettingsProvider : CodeStyleSettingsProvider() {
    override fun createConfigurable(
        settings: CodeStyleSettings,
        modelSettings: CodeStyleSettings
    ): CodeStyleConfigurable {
        return object : CodeStyleAbstractConfigurable(settings, modelSettings, "Move") {
            override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
                return MoveCodeStyleMainPanel(currentSettings, settings)
            }
        }
    }
    
    override fun getConfigurableDisplayName(): String = "Move"
    
    private class MoveCodeStyleMainPanel(
        currentSettings: CodeStyleSettings,
        settings: CodeStyleSettings
    ) : TabbedLanguageCodeStylePanel(MoveLanguage, currentSettings, settings)
}
