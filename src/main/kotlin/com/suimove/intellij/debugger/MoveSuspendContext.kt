package com.suimove.intellij.debugger

import com.intellij.xdebugger.frame.XExecutionStack
import com.intellij.xdebugger.frame.XSuspendContext

/**
 * Suspend context for Move debugging.
 */
class MoveSuspendContext(
    private val debugProcess: MoveDebugProcess,
    private val activeThreadId: Int,
    private val frames: List<MoveStackFrame>
) : XSuspendContext() {
    
    private val executionStack = MoveExecutionStack(
        "Thread $activeThreadId",
        debugProcess,
        frames
    )
    
    override fun getActiveExecutionStack(): XExecutionStack = executionStack
    
    override fun getExecutionStacks(): Array<out XExecutionStack> = arrayOf(executionStack)
    
    override fun computeExecutionStacks(container: XExecutionStackContainer) {
        container.addExecutionStack(listOf(executionStack), true)
    }
}
