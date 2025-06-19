package com.suimove.intellij.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.suimove.intellij.psi.*

class MoveAttributeImpl(node: ASTNode) : MoveNamedElementImpl(node), MoveAttribute {
    
    override val arguments: List<MoveAttributeArgument>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, MoveAttributeArgument::class.java)
}
