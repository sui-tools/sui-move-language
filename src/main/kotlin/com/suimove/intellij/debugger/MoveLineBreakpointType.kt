package com.suimove.intellij.debugger

import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.breakpoints.XLineBreakpointType
import com.intellij.xdebugger.breakpoints.XLineBreakpointTypeBase
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.suimove.intellij.MoveFileType
import javax.swing.Icon

/**
 * Line breakpoint type for Move files.
 */
class MoveLineBreakpointType : XLineBreakpointTypeBase(
    "move-line-breakpoint",
    "Move Line Breakpoint",
    MoveDebuggerEditorsProvider()
) {
    
    override fun createBreakpointProperties(file: VirtualFile, line: Int): XBreakpointProperties<*>? {
        return null
    }
    
    override fun canPutAt(file: VirtualFile, line: Int, project: Project): Boolean {
        // Check if the file is a Move file
        return file.fileType == MoveFileType
    }
    
    override fun getEnabledIcon(): Icon {
        return AllIcons.Debugger.Db_set_breakpoint
    }
    
    override fun getDisabledIcon(): Icon {
        return AllIcons.Debugger.Db_disabled_breakpoint
    }
    
    override fun getMutedEnabledIcon(): Icon {
        return AllIcons.Debugger.Db_muted_breakpoint
    }
    
    override fun getMutedDisabledIcon(): Icon {
        return AllIcons.Debugger.Db_muted_disabled_breakpoint
    }
    
    companion object {
        @JvmField
        val INSTANCE = MoveLineBreakpointType()
        
        @JvmStatic
        fun getInstance(): MoveLineBreakpointType = INSTANCE
    }
}
