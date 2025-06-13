package com.suimove.intellij.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.suimove.intellij.MoveIcons

class CreateMoveFileAction : CreateFileFromTemplateAction(
    "Move File",
    "Create new Move file",
    MoveIcons.FILE
) {
    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
            .setTitle("New Move File")
            .addKind("Module", MoveIcons.FILE, "Move Module")
            .addKind("Script", MoveIcons.FILE, "Move Script")
    }
    
    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String {
        return "Create Move File: $newName"
    }
}
