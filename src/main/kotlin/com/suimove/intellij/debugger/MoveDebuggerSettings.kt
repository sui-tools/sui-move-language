package com.suimove.intellij.debugger

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.xdebugger.settings.DebuggerSettingsCategory
import com.intellij.xdebugger.settings.XDebuggerSettings
import javax.swing.JComponent

/**
 * Debugger settings for Move.
 */
class MoveDebuggerSettings : XDebuggerSettings<MoveDebuggerSettings>("move") {
    
    private var myDataViewSettings = MoveDataViewSettings()
    private var myGeneralSettings = MoveGeneralDebuggerSettings()
    
    override fun createConfigurables(category: DebuggerSettingsCategory): Collection<Configurable> {
        return when (category) {
            DebuggerSettingsCategory.GENERAL -> listOf(MoveGeneralSettingsConfigurable())
            DebuggerSettingsCategory.DATA_VIEWS -> listOf(MoveDataViewSettingsConfigurable())
            else -> emptyList()
        }
    }
    
    override fun getState(): MoveDebuggerSettings = this
    
    override fun loadState(state: MoveDebuggerSettings) {
        myDataViewSettings = state.myDataViewSettings.copy()
        myGeneralSettings = state.myGeneralSettings.copy()
    }
    
    fun getDataViewSettings(): MoveDataViewSettings = myDataViewSettings
    fun getGeneralSettings(): MoveGeneralDebuggerSettings = myGeneralSettings
}

/**
 * Data view settings for Move debugger.
 */
data class MoveDataViewSettings(
    var showHexAddresses: Boolean = true,
    var showTypeAnnotations: Boolean = true,
    var expandStructsByDefault: Boolean = false,
    var vectorDisplayLimit: Int = 100,
    var showPhantomTypes: Boolean = false
) {
    fun copy(): MoveDataViewSettings = copy(
        showHexAddresses = showHexAddresses,
        showTypeAnnotations = showTypeAnnotations,
        expandStructsByDefault = expandStructsByDefault,
        vectorDisplayLimit = vectorDisplayLimit,
        showPhantomTypes = showPhantomTypes
    )
}

/**
 * General debugger settings for Move.
 */
data class MoveGeneralDebuggerSettings(
    var enableExpressionEvaluation: Boolean = true,
    var breakOnAssertions: Boolean = true,
    var breakOnAborts: Boolean = true,
    var showFrameworkFrames: Boolean = false,
    var debugPort: Int = 9229
) {
    fun copy(): MoveGeneralDebuggerSettings = copy(
        enableExpressionEvaluation = enableExpressionEvaluation,
        breakOnAssertions = breakOnAssertions,
        breakOnAborts = breakOnAborts,
        showFrameworkFrames = showFrameworkFrames,
        debugPort = debugPort
    )
}

/**
 * Configurable for general debugger settings.
 */
class MoveGeneralSettingsConfigurable : SearchableConfigurable {
    private var myPanel: JComponent? = null
    private val settings = MoveDebuggerSettings().getGeneralSettings()
    
    private val enableEvaluationCheckbox = JBCheckBox("Enable expression evaluation", settings.enableExpressionEvaluation)
    private val breakOnAssertionsCheckbox = JBCheckBox("Break on assertions", settings.breakOnAssertions)
    private val breakOnAbortsCheckbox = JBCheckBox("Break on aborts", settings.breakOnAborts)
    private val showFrameworkFramesCheckbox = JBCheckBox("Show framework frames", settings.showFrameworkFrames)
    private val debugPortField = JBTextField(settings.debugPort.toString())
    
    override fun getId(): String = "move.debugger.general"
    
    override fun getDisplayName(): String = "Move Debugger"
    
    override fun createComponent(): JComponent? {
        myPanel = panel {
            row {
                cell(enableEvaluationCheckbox)
                comment("Allow evaluating expressions during debugging")
            }
            row {
                cell(breakOnAssertionsCheckbox)
                comment("Pause execution when an assertion fails")
            }
            row {
                cell(breakOnAbortsCheckbox)
                comment("Pause execution when code aborts")
            }
            row {
                cell(showFrameworkFramesCheckbox)
                comment("Show Sui framework internal frames in stack traces")
            }
            row("Debug port:") {
                cell(debugPortField)
                comment("Default port for debugger communication")
            }
        }
        return myPanel
    }
    
    override fun isModified(): Boolean {
        return settings.enableExpressionEvaluation != enableEvaluationCheckbox.isSelected ||
               settings.breakOnAssertions != breakOnAssertionsCheckbox.isSelected ||
               settings.breakOnAborts != breakOnAbortsCheckbox.isSelected ||
               settings.showFrameworkFrames != showFrameworkFramesCheckbox.isSelected ||
               settings.debugPort != (debugPortField.text.toIntOrNull() ?: 9229)
    }
    
    override fun apply() {
        settings.enableExpressionEvaluation = enableEvaluationCheckbox.isSelected
        settings.breakOnAssertions = breakOnAssertionsCheckbox.isSelected
        settings.breakOnAborts = breakOnAbortsCheckbox.isSelected
        settings.showFrameworkFrames = showFrameworkFramesCheckbox.isSelected
        settings.debugPort = debugPortField.text.toIntOrNull() ?: 9229
    }
    
    override fun reset() {
        enableEvaluationCheckbox.isSelected = settings.enableExpressionEvaluation
        breakOnAssertionsCheckbox.isSelected = settings.breakOnAssertions
        breakOnAbortsCheckbox.isSelected = settings.breakOnAborts
        showFrameworkFramesCheckbox.isSelected = settings.showFrameworkFrames
        debugPortField.text = settings.debugPort.toString()
    }
}

/**
 * Configurable for data view settings.
 */
class MoveDataViewSettingsConfigurable : SearchableConfigurable {
    private var myPanel: JComponent? = null
    private val settings = MoveDebuggerSettings().getDataViewSettings()
    
    private val showHexAddressesCheckbox = JBCheckBox("Show addresses in hex", settings.showHexAddresses)
    private val showTypeAnnotationsCheckbox = JBCheckBox("Show type annotations", settings.showTypeAnnotations)
    private val expandStructsCheckbox = JBCheckBox("Expand structs by default", settings.expandStructsByDefault)
    private val showPhantomTypesCheckbox = JBCheckBox("Show phantom type parameters", settings.showPhantomTypes)
    private val vectorLimitField = JBTextField(settings.vectorDisplayLimit.toString())
    
    override fun getId(): String = "move.debugger.dataviews"
    
    override fun getDisplayName(): String = "Data Views"
    
    override fun createComponent(): JComponent? {
        myPanel = panel {
            row {
                cell(showHexAddressesCheckbox)
                comment("Display addresses in hexadecimal format")
            }
            row {
                cell(showTypeAnnotationsCheckbox)
                comment("Show type information for variables")
            }
            row {
                cell(expandStructsCheckbox)
                comment("Automatically expand struct fields in variables view")
            }
            row {
                cell(showPhantomTypesCheckbox)
                comment("Display phantom type parameters in generic types")
            }
            row("Vector display limit:") {
                cell(vectorLimitField)
                comment("Maximum number of vector elements to display")
            }
        }
        return myPanel
    }
    
    override fun isModified(): Boolean {
        return settings.showHexAddresses != showHexAddressesCheckbox.isSelected ||
               settings.showTypeAnnotations != showTypeAnnotationsCheckbox.isSelected ||
               settings.expandStructsByDefault != expandStructsCheckbox.isSelected ||
               settings.showPhantomTypes != showPhantomTypesCheckbox.isSelected ||
               settings.vectorDisplayLimit != (vectorLimitField.text.toIntOrNull() ?: 100)
    }
    
    override fun apply() {
        settings.showHexAddresses = showHexAddressesCheckbox.isSelected
        settings.showTypeAnnotations = showTypeAnnotationsCheckbox.isSelected
        settings.expandStructsByDefault = expandStructsCheckbox.isSelected
        settings.showPhantomTypes = showPhantomTypesCheckbox.isSelected
        settings.vectorDisplayLimit = vectorLimitField.text.toIntOrNull() ?: 100
    }
    
    override fun reset() {
        showHexAddressesCheckbox.isSelected = settings.showHexAddresses
        showTypeAnnotationsCheckbox.isSelected = settings.showTypeAnnotations
        expandStructsCheckbox.isSelected = settings.expandStructsByDefault
        showPhantomTypesCheckbox.isSelected = settings.showPhantomTypes
        vectorLimitField.text = settings.vectorDisplayLimit.toString()
    }
}
