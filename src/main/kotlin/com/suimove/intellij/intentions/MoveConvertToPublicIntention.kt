package com.suimove.intellij.intentions

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.suimove.intellij.psi.MoveElementFactory
import com.suimove.intellij.psi.MoveTypes

class MoveConvertToPublicIntention : PsiElementBaseIntentionAction() {
    override fun getText(): String = "Make function public"
    
    override fun getFamilyName(): String = "Move visibility modifiers"
    
    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        var current: PsiElement? = element
        while (current != null && current.node?.elementType != MoveTypes.FUNCTION_DEFINITION) {
            current = current.parent
        }
        val function = current ?: return false
        
        // Check if function is not already public
        val hasPublic = function.node?.findChildByType(MoveTypes.PUBLIC) != null
        val hasEntry = function.node?.findChildByType(MoveTypes.ENTRY) != null
        
        return !hasPublic && !hasEntry
    }
    
    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        var current: PsiElement? = element
        while (current != null && current.node?.elementType != MoveTypes.FUNCTION_DEFINITION) {
            current = current.parent
        }
        val function = current ?: return
        
        val funKeyword = function.node?.findChildByType(MoveTypes.FUN) ?: return
        
        // Create public keyword
        val publicKeyword = MoveElementFactory.createPublicKeyword(project)
        
        // Insert before 'fun'
        function.addBefore(publicKeyword, funKeyword.psi)
        function.addBefore(MoveElementFactory.createWhitespace(project), funKeyword.psi)
    }
}
