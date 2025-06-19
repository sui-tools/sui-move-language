package com.suimove.intellij.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.suimove.intellij.psi.*

class MoveStructFieldImpl(node: ASTNode) : MoveNamedElementImpl(node), MoveStructField {
    
    override val type: MoveTypeElement?
        get() = PsiTreeUtil.findChildOfType(this, MoveTypeElement::class.java)
}
