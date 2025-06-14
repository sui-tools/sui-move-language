package com.suimove.intellij.templates

import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory
import com.suimove.intellij.MoveIcons

class MoveFileTemplateProvider : FileTemplateGroupDescriptorFactory {
    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor {
        val group = FileTemplateGroupDescriptor("Move", MoveIcons.FILE)
        group.addTemplate(FileTemplateDescriptor("Move Module.move", MoveIcons.FILE))
        group.addTemplate(FileTemplateDescriptor("Move Script.move", MoveIcons.FILE))
        group.addTemplate(FileTemplateDescriptor("Move Test Module.move", MoveIcons.FILE))
        return group
    }
}
