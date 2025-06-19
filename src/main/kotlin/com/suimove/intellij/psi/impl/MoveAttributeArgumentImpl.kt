package com.suimove.intellij.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.suimove.intellij.psi.MoveAttributeArgument

class MoveAttributeArgumentImpl(node: ASTNode) : ASTWrapperPsiElement(node), MoveAttributeArgument {
    
    override val value: String?
        get() = text
}
