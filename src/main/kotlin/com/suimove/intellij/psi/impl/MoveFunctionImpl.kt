package com.suimove.intellij.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.suimove.intellij.psi.*
import com.suimove.intellij.services.type.MoveType
import com.suimove.intellij.services.type.MoveTypeParameter

class MoveFunctionImpl(node: ASTNode) : MoveNamedElementImpl(node), MoveFunction {
    
    override val parameters: List<MoveFunctionParameter>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, MoveFunctionParameter::class.java)
    
    override val typeParameters: List<MoveTypeParameter>
        get() = emptyList() // TODO: Implement proper type parameter extraction
    
    override val returnType: MoveType?
        get() = null // TODO: Implement type resolution
    
    override val body: MoveCodeBlock?
        get() = PsiTreeUtil.findChildOfType(this, MoveCodeBlock::class.java)
        
    override val visibility: MoveVisibility
        get() = when {
            text.contains("public") -> MoveVisibility.PUBLIC
            text.contains("public(friend)") -> MoveVisibility.PUBLIC_FRIEND
            else -> MoveVisibility.PRIVATE
        }
        
    override val isEntry: Boolean
        get() = text.contains("entry")
        
    override val isPublic: Boolean
        get() = visibility == MoveVisibility.PUBLIC
        
    override val isNative: Boolean
        get() = text.contains("native")
        
    override val isInline: Boolean
        get() = text.contains("inline")
}
