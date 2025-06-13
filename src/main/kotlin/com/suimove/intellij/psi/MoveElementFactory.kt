package com.suimove.intellij.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.suimove.intellij.MoveFileType
import com.suimove.intellij.psi.MoveTypes

object MoveElementFactory {
    fun createFile(project: Project, text: String): MoveFile {
        val name = "dummy.move"
        return PsiFileFactory.getInstance(project)
            .createFileFromText(name, MoveFileType, text) as MoveFile
    }
    
    fun createIdentifier(project: Project, name: String): PsiElement {
        val file = createFile(project, "module 0x1::dummy { const $name: u64 = 0; }")
        return file.firstChild.lastChild.firstChild.firstChild
    }
    
    fun createModuleDeclaration(project: Project, address: String, name: String): PsiElement {
        val file = createFile(project, "module $address::$name {}")
        return file.firstChild
    }
    
    fun createFunctionDeclaration(project: Project, name: String, params: String = "", returnType: String? = null): PsiElement {
        val returns = if (returnType != null) ": $returnType" else ""
        val file = createFile(project, "module 0x1::dummy { public fun $name($params)$returns {} }")
        return file.firstChild.lastChild.prevSibling
    }
    
    fun createStructDeclaration(project: Project, name: String): PsiElement {
        val file = createFile(project, "module 0x1::dummy { struct $name {} }")
        return file.firstChild.lastChild.prevSibling
    }
    
    fun createTypeAnnotation(project: Project, type: String): PsiElement {
        val file = createFile(project, "module 0x1::dummy { fun f() { let x: $type = 0; } }")
        // Return the ": type" part
        val letBinding = file.firstChild.lastChild.lastChild.firstChild.firstChild
        return letBinding.node.findChildByType(MoveTypes.COLON)!!.psi
    }
    
    fun createPublicKeyword(project: Project): PsiElement {
        val file = createFile(project, "module 0x1::dummy { public fun f() {} }")
        val function = file.firstChild.lastChild.lastChild
        return function.node.findChildByType(MoveTypes.PUBLIC)!!.psi
    }
    
    fun createWhitespace(project: Project): PsiElement {
        val file = createFile(project, "module 0x1::dummy { }")
        return file.firstChild.firstChild.nextSibling // The space after "module"
    }
}
