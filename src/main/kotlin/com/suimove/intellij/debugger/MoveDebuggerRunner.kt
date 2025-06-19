package com.suimove.intellij.debugger

import com.intellij.debugger.impl.GenericDebuggerRunner
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugProcessStarter
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import com.suimove.intellij.testing.MoveTestRunConfiguration

/**
 * Debug runner for Move programs.
 */
class MoveDebuggerRunner : GenericDebuggerRunner() {
    
    override fun getRunnerId(): String = "MoveDebuggerRunner"
    
    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        return super.canRun(executorId, profile) && profile is MoveTestRunConfiguration
    }
    
    override fun doExecute(
        state: RunProfileState,
        environment: ExecutionEnvironment
    ): RunContentDescriptor? {
        val runProfile = environment.runProfile as? MoveTestRunConfiguration
            ?: throw ExecutionException("Invalid run configuration")
        
        return XDebuggerManager.getInstance(environment.project).startSession(
            environment,
            object : XDebugProcessStarter() {
                override fun start(session: XDebugSession): XDebugProcess {
                    return MoveDebugProcess(session, runProfile, state)
                }
            }
        ).runContentDescriptor
    }
}
