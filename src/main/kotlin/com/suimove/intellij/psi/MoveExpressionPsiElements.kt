package com.suimove.intellij.psi

import com.intellij.psi.PsiElement

/**
 * Field access expression (e.g., expr.field).
 */
interface MoveFieldAccessExpression : MoveExpression {
    val expression: MoveExpression?
    val identifier: PsiElement?
}

/**
 * Index expression (e.g., expr[index]).
 */
interface MoveIndexExpression : MoveExpression {
    val expression: MoveExpression?
    val index: MoveExpression?
}

/**
 * Borrow expression (e.g., &expr or &mut expr).
 */
interface MoveBorrowExpression : MoveExpression {
    val expression: MoveExpression?
    val borrowMut: PsiElement?
}

/**
 * Dereference expression (e.g., *expr).
 */
interface MoveDereferenceExpression : MoveExpression {
    val expression: MoveExpression?
}

/**
 * Move expression (e.g., move expr).
 */
interface MoveMoveExpression : MoveExpression {
    val expression: MoveExpression?
}

/**
 * Copy expression (e.g., copy expr).
 */
interface MoveCopyExpression : MoveExpression {
    val expression: MoveExpression?
}
