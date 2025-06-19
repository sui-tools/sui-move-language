package com.suimove.intellij.debugger

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.suimove.intellij.testing.MoveTestRunConfiguration
import javax.swing.JComponent

/**
 * Configuration editor for Move debugging.
 */
class MoveDebugConfigurationEditor(
    private val project: Project
) : SettingsEditor<MoveTestRunConfiguration>() {
    
    private val debugPortField = JBTextField("9229")
    private val enableLoggingCheckbox = JBCheckBox("Enable debug logging", false)
    private val breakOnEntryCheckbox = JBCheckBox("Break on entry", false)
    private val evaluateExpressionsCheckbox = JBCheckBox("Enable expression evaluation", true)
    private val showHiddenFramesCheckbox = JBCheckBox("Show hidden frames", false)
    
    override fun createEditor(): JComponent {
        return panel {
            row("Debug port:") {
                cell(debugPortField)
                comment("Port for debugger communication (default: 9229)")
            }
            
            row {
                cell(breakOnEntryCheckbox)
                comment("Pause execution at the first line of the test")
            }
            
            row {
                cell(evaluateExpressionsCheckbox)
                comment("Allow evaluating expressions during debugging")
            }
            
            row {
                cell(showHiddenFramesCheckbox)
                comment("Show internal framework frames in the call stack")
            }
            
            row {
                cell(enableLoggingCheckbox)
                comment("Enable verbose debug logging for troubleshooting")
            }
            
            row {
                comment("Note: Move debugging requires a debug-enabled version of the Sui CLI")
            }
        }
    }
    
    override fun resetEditorFrom(configuration: MoveTestRunConfiguration) {
        // Load debug settings from configuration
        val debugOptions = configuration.debugOptions
        debugPortField.text = debugOptions.debugPort.toString()
        enableLoggingCheckbox.isSelected = debugOptions.enableLogging
        breakOnEntryCheckbox.isSelected = debugOptions.breakOnEntry
        evaluateExpressionsCheckbox.isSelected = debugOptions.enableEvaluation
        showHiddenFramesCheckbox.isSelected = debugOptions.showHiddenFrames
    }
    
    override fun applyEditorTo(configuration: MoveTestRunConfiguration) {
        // Save debug settings to configuration
        val debugOptions = configuration.debugOptions
        debugOptions.debugPort = debugPortField.text.toIntOrNull() ?: 9229
        debugOptions.enableLogging = enableLoggingCheckbox.isSelected
        debugOptions.breakOnEntry = breakOnEntryCheckbox.isSelected
        debugOptions.enableEvaluation = evaluateExpressionsCheckbox.isSelected
        debugOptions.showHiddenFrames = showHiddenFramesCheckbox.isSelected
    }
}

/**
 * Debug options for Move test configuration.
 */
class MoveDebugOptions {
    var debugPort: Int = 9229
    var enableLogging: Boolean = false
    var breakOnEntry: Boolean = false
    var enableEvaluation: Boolean = true
    var showHiddenFrames: Boolean = false
    
    fun copy(): MoveDebugOptions {
        val copy = MoveDebugOptions()
        copy.debugPort = debugPort
        copy.enableLogging = enableLogging
        copy.breakOnEntry = breakOnEntry
        copy.enableEvaluation = enableEvaluation
        copy.showHiddenFrames = showHiddenFrames
        return copy
    }
}
