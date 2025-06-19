package com.suimove.intellij.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.suimove.intellij.psi.*

class MoveModuleImpl(node: ASTNode) : MoveNamedElementImpl(node), MoveModule {
    
    override val address: String?
        get() = (parent as? MoveAddressBlock)?.address
    
    override val functions: List<MoveFunction>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, MoveFunction::class.java)
    
    override val structs: List<MoveStruct>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, MoveStruct::class.java)
    
    override val constants: List<MoveConstant>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, MoveConstant::class.java)
    
    override val uses: List<MoveUseStatement>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, MoveUseStatement::class.java)
}
