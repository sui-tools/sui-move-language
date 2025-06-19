package com.suimove.intellij.testing

import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * UI editor for Move test run configuration.
 */
class MoveTestRunConfigurationEditor : SettingsEditor<MoveTestRunConfiguration>() {
    
    private val testKindCombo = ComboBox(TestKind.values())
    private val modulePathField = TextFieldWithBrowseButton()
    private val functionNameField = JBTextField()
    private val packagePathField = TextFieldWithBrowseButton()
    private val testFilterField = JBTextField()
    private val additionalArgsField = JBTextField()
    private val envVarsField = EnvironmentVariablesTextFieldWithBrowseButton()
    private val gasLimitField = JBTextField()
    private val showOutputCheckbox = JBCheckBox("Show test output")
    private val coverageCheckbox = JBCheckBox("Generate coverage report")
    
    init {
        modulePathField.addBrowseFolderListener(
            "Select Module",
            "Select the Move module containing tests",
            null,
            FileChooserDescriptorFactory.createSingleFileDescriptor("move")
        )
        
        packagePathField.addBrowseFolderListener(
            "Select Package",
            "Select the Move package directory",
            null,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        )
        
        testKindCombo.addActionListener {
            updateFieldsVisibility()
        }
    }
    
    override fun createEditor(): JComponent {
        return panel {
            row("Test kind:") {
                cell(testKindCombo)
            }
            
            row("Module:") {
                cell(modulePathField)
                comment("Path to the Move module file")
            }.visible(testKindCombo.selectedItem == TestKind.MODULE)
            
            row("Function:") {
                cell(functionNameField)
                comment("Name of the test function to run")
            }.visible(testKindCombo.selectedItem == TestKind.FUNCTION)
            
            row("Package:") {
                cell(packagePathField)
                comment("Path to the Move package directory")
            }.visible(testKindCombo.selectedItem == TestKind.PACKAGE)
            
            row("Test filter:") {
                cell(testFilterField)
                comment("Filter test names (regex pattern)")
            }
            
            row("Additional arguments:") {
                cell(additionalArgsField)
                comment("Additional command line arguments for sui move test")
            }
            
            row("Environment variables:") {
                cell(envVarsField)
            }
            
            row("Gas limit:") {
                cell(gasLimitField)
                comment("Maximum gas units for test execution")
            }
            
            row {
                cell(showOutputCheckbox)
            }
            
            row {
                cell(coverageCheckbox)
            }
        }
    }
    
    override fun resetEditorFrom(configuration: MoveTestRunConfiguration) {
        val options = configuration.getOptions()
        
        testKindCombo.selectedItem = options.testKind
        modulePathField.text = options.modulePath ?: ""
        functionNameField.text = options.functionName ?: ""
        packagePathField.text = options.packagePath ?: ""
        testFilterField.text = options.testFilter ?: ""
        additionalArgsField.text = options.additionalArguments ?: ""
        envVarsField.envs = options.environmentVariables
        gasLimitField.text = options.gasLimit?.toString() ?: ""
        showOutputCheckbox.isSelected = options.showOutput
        coverageCheckbox.isSelected = options.coverage
        
        updateFieldsVisibility()
    }
    
    override fun applyEditorTo(configuration: MoveTestRunConfiguration) {
        configuration.setTestKind(testKindCombo.selectedItem as TestKind)
        configuration.setModulePath(modulePathField.text.takeIf { it.isNotBlank() })
        configuration.setFunctionName(functionNameField.text.takeIf { it.isNotBlank() })
        configuration.setPackagePath(packagePathField.text.takeIf { it.isNotBlank() })
        configuration.setTestFilter(testFilterField.text.takeIf { it.isNotBlank() })
        configuration.setAdditionalArguments(additionalArgsField.text.takeIf { it.isNotBlank() })
        configuration.setEnvironmentVariables(envVarsField.envs)
        configuration.setGasLimit(gasLimitField.text.toLongOrNull())
        configuration.setShowOutput(showOutputCheckbox.isSelected)
        configuration.setCoverage(coverageCheckbox.isSelected)
    }
    
    private fun updateFieldsVisibility() {
        // The panel builder handles visibility based on conditions
    }
}
