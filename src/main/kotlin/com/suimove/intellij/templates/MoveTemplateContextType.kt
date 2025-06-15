package com.suimove.intellij.templates

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import com.suimove.intellij.psi.MoveFile

class MoveTemplateContextType : TemplateContextType("Move") {
    override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
        return templateActionContext.file is MoveFile
    }
}
