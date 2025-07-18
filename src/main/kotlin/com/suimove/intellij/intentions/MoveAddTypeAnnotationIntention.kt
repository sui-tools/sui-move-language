package com.suimove.intellij.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.suimove.intellij.psi.MoveElementFactory
import com.suimove.intellij.psi.MoveTypes

class MoveAddTypeAnnotationIntention : PsiElementBaseIntentionAction() {
    override fun getText(): String = "Add type annotation"
    
    override fun getFamilyName(): String = "Move type annotations"
    
    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        // Find parent let binding
        var current: PsiElement? = element
        while (current != null && current.node?.elementType != MoveTypes.LET_BINDING) {
            current = current.parent
        }
        val letBinding = current ?: return false
        
        // Check if already has type annotation
        return letBinding.node?.findChildByType(MoveTypes.TYPE_ANNOTATION) == null
    }
    
    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        // Find parent let binding
        var current: PsiElement? = element
        while (current != null && current.node?.elementType != MoveTypes.LET_BINDING) {
            current = current.parent
        }
        val letBinding = current ?: return
        
        val identifier = letBinding.node?.findChildByType(MoveTypes.IDENTIFIER) ?: return
        
        // Try to infer type from initialization expression
        val inferredType = inferType(letBinding) ?: "u64" // Default to u64
        
        // Create type annotation
        val typeAnnotation = MoveElementFactory.createTypeAnnotation(project, inferredType)
        
        // Insert after identifier
        letBinding.addAfter(typeAnnotation, identifier.psi)
    }
    
    private fun inferType(letBinding: PsiElement): String? {
        // Simple type inference based on literal values
        val expr = letBinding.node?.findChildByType(MoveTypes.EXPRESSION)
        if (expr != null) {
            val literal = expr.findChildByType(MoveTypes.INTEGER_LITERAL)
            if (literal != null) {
                val value = literal.text.toLongOrNull()
                return when {
                    value == null -> "u64"
                    value <= 255 -> "u8"
                    value <= 65535 -> "u16"
                    value <= 4294967295L -> "u32"
                    else -> "u64"
                }
            }
            
            if (expr.findChildByType(MoveTypes.TRUE) != null || 
                expr.findChildByType(MoveTypes.FALSE) != null) {
                return "bool"
            }
            
            if (expr.findChildByType(MoveTypes.ADDRESS_LITERAL) != null) {
                return "address"
            }
        }
        
        return null
    }
}
