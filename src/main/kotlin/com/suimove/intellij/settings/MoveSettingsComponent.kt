package com.suimove.intellij.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class MoveSettingsComponent {
    private val suiCliPathField = TextFieldWithBrowseButton()
    val panel: JPanel
    
    init {
        suiCliPathField.addBrowseFolderListener(
            "Select Sui CLI Path",
            "Select the path to the Sui CLI executable",
            null,
            FileChooserDescriptorFactory.createSingleFileDescriptor()
        )
        
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Sui CLI path:"), suiCliPathField, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }
    
    var suiCliPath: String
        get() = suiCliPathField.text
        set(value) {
            suiCliPathField.text = value
        }
}
