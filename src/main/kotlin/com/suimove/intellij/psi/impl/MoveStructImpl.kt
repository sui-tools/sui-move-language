package com.suimove.intellij.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.suimove.intellij.psi.*
import com.suimove.intellij.services.type.MoveAbility
import com.suimove.intellij.services.type.MoveTypeParameter

class MoveStructImpl(node: ASTNode) : MoveNamedElementImpl(node), MoveStruct {
    
    override val fields: List<MoveStructField>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, MoveStructField::class.java)
    
    override val typeParameters: List<MoveTypeParameter>
        get() = emptyList() // TODO: Implement proper type parameter extraction
    
    override val abilities: Set<MoveAbility>
        get() = emptySet() // TODO: Implement abilities
        
    override val qualifiedName: String?
        get() = {
            val module = PsiTreeUtil.getParentOfType(this, MoveModule::class.java)
            if (module != null && name != null) {
                "${module.name}::$name"
            } else {
                name
            }
        }()
}
