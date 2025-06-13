package com.suimove.intellij.psi

import com.intellij.psi.PsiElement

interface MoveElement : PsiElement {
    val name: String?
}

interface MoveNamedElement : MoveElement {
    fun setName(name: String): PsiElement
}
