package com.suimove.intellij.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.suimove.intellij.MoveFileType
import com.suimove.intellij.MoveLanguage

class MoveFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, MoveLanguage) {
    override fun getFileType(): FileType = MoveFileType

    override fun toString(): String = "Move File"
}
