package com.suimove.intellij.psi

import com.intellij.openapi.util.UserDataHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.ResolveState

/**
 * Interface for Move parameter list.
 */
interface MoveParameterList : PsiElement, UserDataHolder {
    val parameters: List<MoveFunctionParameter>
    override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean
}
