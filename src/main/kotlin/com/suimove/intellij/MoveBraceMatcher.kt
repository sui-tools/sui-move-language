package com.suimove.intellij

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.suimove.intellij.psi.MoveTypes

class MoveBraceMatcher : PairedBraceMatcher {
    private val pairs = arrayOf(
        BracePair(MoveTypes.LPAREN, MoveTypes.RPAREN, false),
        BracePair(MoveTypes.LBRACK, MoveTypes.RBRACK, false),
        BracePair(MoveTypes.LBRACE, MoveTypes.RBRACE, true),
        BracePair(MoveTypes.LT, MoveTypes.GT, false)
    )

    override fun getPairs(): Array<BracePair> = pairs

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset
}
