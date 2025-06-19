package com.suimove.intellij.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil
import com.suimove.intellij.psi.*

class MoveAttributeListImpl(node: ASTNode) : ASTWrapperPsiElement(node), MoveAttributeList {
    
    override val attributes: List<MoveAttribute>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, MoveAttribute::class.java)
}
