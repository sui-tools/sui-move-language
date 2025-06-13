package com.suimove.intellij.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil
import com.suimove.intellij.psi.MoveNamedElement
import com.suimove.intellij.psi.MoveTypes

abstract class MoveNamedElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), MoveNamedElement, PsiNameIdentifierOwner {
    
    override fun getName(): String? {
        return nameIdentifier?.text
    }
    
    override fun setName(name: String): PsiElement {
        nameIdentifier?.let { identifier ->
            val newIdentifier = MoveElementFactory.createIdentifier(project, name)
            identifier.replace(newIdentifier)
        }
        return this
    }
    
    override fun getNameIdentifier(): PsiElement? {
        return findChildByType(MoveTypes.IDENTIFIER)
    }
}
