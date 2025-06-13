package com.suimove.intellij

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.suimove.intellij.psi.MoveTypes

class MoveQuoteHandler : SimpleTokenSetQuoteHandler(
    MoveTypes.STRING_LITERAL,
    MoveTypes.BYTE_STRING_LITERAL
)
