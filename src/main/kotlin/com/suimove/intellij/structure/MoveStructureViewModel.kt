package com.suimove.intellij.structure

import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.suimove.intellij.psi.MoveFile

class MoveStructureViewModel(
    psiFile: PsiFile,
    editor: Editor?
) : StructureViewModelBase(psiFile, editor, MoveStructureViewElement(psiFile)),
    StructureViewModel.ElementInfoProvider {
    
    override fun getSorters(): Array<Sorter> = arrayOf(Sorter.ALPHA_SORTER)
    
    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean = false
    
    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean = false
}
