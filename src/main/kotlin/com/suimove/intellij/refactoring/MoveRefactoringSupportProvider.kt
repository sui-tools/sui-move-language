package com.suimove.intellij.refactoring

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import com.suimove.intellij.psi.MoveNamedElement

class MoveRefactoringSupportProvider : RefactoringSupportProvider() {
    override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
        return element is MoveNamedElement
    }
    
    override fun isSafeDeleteAvailable(element: PsiElement): Boolean {
        return element is MoveNamedElement
    }
}
