package com.suimove.intellij.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.suimove.intellij.psi.MoveStruct
import com.suimove.intellij.psi.MoveStructField

/**
 * Quick fix to add UID field to a Sui object struct.
 */
class AddUidFieldQuickFix(private val struct: MoveStruct) : LocalQuickFix {
    
    override fun getFamilyName(): String = "Add UID field"
    
    override fun getName(): String = "Add 'id: UID' field"
    
    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val document = PsiDocumentManager.getInstance(project).getDocument(struct.containingFile) ?: return
        
        // Find the position to insert the field
        val structBody = struct.structBody ?: return
        val firstField = struct.fields.firstOrNull()
        
        val insertOffset = if (firstField != null) {
            // Insert before the first field
            firstField.textRange.startOffset
        } else {
            // Insert after the opening brace
            val openBrace = structBody.text.indexOf('{')
            if (openBrace >= 0) {
                structBody.textRange.startOffset + openBrace + 1
            } else {
                return
            }
        }
        
        // Prepare the text to insert
        val indent = getIndentation(struct)
        val fieldText = if (struct.fields.isEmpty()) {
            "\n${indent}    id: UID\n$indent"
        } else {
            "${indent}    id: UID,\n"
        }
        
        // Insert the field
        document.insertString(insertOffset, fieldText)
        
        // Add import if needed
        addObjectImportIfNeeded(project, struct)
        
        // Commit the document
        PsiDocumentManager.getInstance(project).commitDocument(document)
    }
    
    private fun getIndentation(element: PsiElement): String {
        val text = element.text
        val firstNonWhitespace = text.indexOfFirst { !it.isWhitespace() }
        return if (firstNonWhitespace > 0) {
            text.substring(0, firstNonWhitespace)
        } else {
            "    " // Default to 4 spaces
        }
    }
    
    private fun addObjectImportIfNeeded(project: Project, struct: MoveStruct) {
        val file = struct.containingFile
        val hasObjectImport = file.text.contains("use 0x2::object") || 
                             file.text.contains("use std::object")
        
        if (!hasObjectImport) {
            val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return
            
            // Find module declaration
            val moduleMatch = Regex("module\\s+\\S+\\s*\\{").find(file.text)
            if (moduleMatch != null) {
                val insertOffset = moduleMatch.range.last + 1
                document.insertString(insertOffset, "\n    use 0x2::object::{Self, UID};\n")
            }
        }
    }
}

/**
 * Extension to get struct body.
 */
val MoveStruct.structBody: PsiElement?
    get() = this.children.find { it.text.contains("{") }
