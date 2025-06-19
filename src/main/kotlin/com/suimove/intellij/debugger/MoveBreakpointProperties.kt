package com.suimove.intellij.debugger

import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.xdebugger.breakpoints.XBreakpointProperties

/**
 * Properties for Move breakpoints.
 */
class MoveBreakpointProperties : XBreakpointProperties<MoveBreakpointProperties>() {
    
    @Attribute("condition")
    var condition: String? = null
    
    @Attribute("logExpression")
    var logExpression: String? = null
    
    @Attribute("logMessage")
    var logMessage: Boolean = false
    
    @Attribute("suspendPolicy")
    var suspendPolicy: SuspendPolicy = SuspendPolicy.ALL
    
    override fun getState(): MoveBreakpointProperties? = this
    
    override fun loadState(state: MoveBreakpointProperties) {
        condition = state.condition
        logExpression = state.logExpression
        logMessage = state.logMessage
        suspendPolicy = state.suspendPolicy
    }
    
    fun hasCondition(): Boolean = !condition.isNullOrBlank()
    
    fun hasLogExpression(): Boolean = !logExpression.isNullOrBlank()
}

/**
 * Suspend policy for breakpoints.
 */
enum class SuspendPolicy {
    ALL,      // Suspend all threads
    THREAD,   // Suspend only the current thread
    NONE      // Don't suspend, just log
}
