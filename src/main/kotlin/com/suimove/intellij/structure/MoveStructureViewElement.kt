package com.suimove.intellij.structure

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.suimove.intellij.MoveIcons
import com.suimove.intellij.psi.MoveFile

class MoveStructureViewElement(
    private val element: NavigatablePsiElement
) : StructureViewTreeElement {
    
    override fun getValue(): Any = element
    
    override fun getPresentation(): ItemPresentation = element.presentation ?: PresentationData()
    
    override fun getChildren(): Array<TreeElement> {
        if (element !is MoveFile) {
            return emptyArray()
        }
        
        val children = mutableListOf<TreeElement>()
        
        // Find all top-level elements (modules, scripts, functions, structs)
        PsiTreeUtil.getChildrenOfType(element, PsiElement::class.java)?.forEach { child ->
            if (child is NavigatablePsiElement) {
                children.add(MoveStructureViewElement(child))
            }
        }
        
        return children.toTypedArray()
    }
    
    override fun navigate(requestFocus: Boolean) {
        element.navigate(requestFocus)
    }
    
    override fun canNavigate(): Boolean = element.canNavigate()
    
    override fun canNavigateToSource(): Boolean = element.canNavigateToSource()
}
