package com.suimove.intellij.psi

import com.intellij.psi.PsiElement

/**
 * Attribute list containing multiple attributes.
 */
interface MoveAttributeList : PsiElement {
    val attributes: List<MoveAttribute>
}

/**
 * Single attribute (e.g., #[test], #[expected_failure]).
 */
interface MoveAttribute : MoveNamedElement {
    val arguments: List<MoveAttributeArgument>
}

/**
 * Attribute argument.
 */
interface MoveAttributeArgument : PsiElement {
    val value: String?
}
