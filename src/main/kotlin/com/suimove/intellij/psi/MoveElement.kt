package com.suimove.intellij.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiNameIdentifierOwner

interface MoveElement : PsiElement {
}

interface MoveNamedElement : MoveElement, PsiNamedElement, PsiNameIdentifierOwner {
    override fun setName(name: String): PsiElement
}
