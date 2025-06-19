package com.suimove.intellij.debugger

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.xdebugger.breakpoints.XBreakpointHandler
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.breakpoints.XLineBreakpoint

/**
 * Breakpoint handler for Move line breakpoints.
 */
class MoveLineBreakpointHandler(
    private val debugProcess: MoveDebugProcess
) : XBreakpointHandler<XLineBreakpoint<XBreakpointProperties<*>>>(MoveLineBreakpointType::class.java) {
    
    override fun registerBreakpoint(breakpoint: XLineBreakpoint<XBreakpointProperties<*>>) {
        val sourcePosition = breakpoint.sourcePosition ?: return
        debugProcess.addBreakpoint(sourcePosition.file, sourcePosition.line)
    }
    
    override fun unregisterBreakpoint(
        breakpoint: XLineBreakpoint<XBreakpointProperties<*>>,
        temporary: Boolean
    ) {
        val sourcePosition = breakpoint.sourcePosition ?: return
        debugProcess.removeBreakpoint(sourcePosition.file, sourcePosition.line)
    }
}
