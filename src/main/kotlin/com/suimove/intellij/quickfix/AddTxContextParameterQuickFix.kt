package com.suimove.intellij.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.suimove.intellij.psi.MoveFunction
import com.suimove.intellij.psi.MoveFunctionParameter

/**
 * Quick fix to add TxContext parameter to entry function.
 */
class AddTxContextParameterQuickFix(private val function: MoveFunction) : LocalQuickFix {
    
    override fun getFamilyName(): String = "Add TxContext parameter"
    
    override fun getName(): String = "Add 'ctx: &mut TxContext' parameter"
    
    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val document = PsiDocumentManager.getInstance(project).getDocument(function.containingFile) ?: return
        
        // Find the parameter list
        val paramListText = function.text.substringAfter("(").substringBefore(")")
        val paramListStart = function.textRange.startOffset + function.text.indexOf("(") + 1
        val paramListEnd = function.textRange.startOffset + function.text.indexOf(")")
        
        // Prepare the new parameter text
        val newParamText = if (function.parameters.isEmpty()) {
            "ctx: &mut TxContext"
        } else {
            ", ctx: &mut TxContext"
        }
        
        // Insert the parameter
        document.insertString(paramListEnd, newParamText)
        
        // Add import if needed
        addTxContextImportIfNeeded(project, function)
        
        // Commit the document
        PsiDocumentManager.getInstance(project).commitDocument(document)
    }
    
    private fun addTxContextImportIfNeeded(project: Project, function: MoveFunction) {
        val file = function.containingFile
        val hasTxContextImport = file.text.contains("use 0x2::tx_context") || 
                                file.text.contains("use sui::tx_context")
        
        if (!hasTxContextImport) {
            val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return
            
            // Find module declaration
            val moduleMatch = Regex("module\\s+\\S+\\s*\\{").find(file.text)
            if (moduleMatch != null) {
                val insertOffset = moduleMatch.range.last + 1
                
                // Check if there are already imports
                val hasImports = file.text.substring(moduleMatch.range.last).contains("use ")
                
                if (hasImports) {
                    // Find the last import
                    val imports = Regex("use .+;").findAll(file.text.substring(moduleMatch.range.last))
                    val lastImport = imports.lastOrNull()
                    if (lastImport != null) {
                        val lastImportEnd = moduleMatch.range.last + lastImport.range.last + 1
                        document.insertString(lastImportEnd, "\n    use 0x2::tx_context::TxContext;")
                    }
                } else {
                    // Add as first import
                    document.insertString(insertOffset, "\n    use 0x2::tx_context::TxContext;\n")
                }
            }
        }
    }
}
