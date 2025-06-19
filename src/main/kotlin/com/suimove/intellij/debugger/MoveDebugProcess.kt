package com.suimove.intellij.debugger

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.ExecutionConsole
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XBreakpointHandler
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.intellij.xdebugger.frame.XExecutionStack
import com.intellij.xdebugger.frame.XSuspendContext
import com.intellij.xdebugger.impl.XDebuggerManagerImpl
import com.suimove.intellij.testing.MoveTestRunConfiguration
import com.suimove.intellij.testing.MoveTestRunState
import com.suimove.intellij.testing.TestKind
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * Debug process for Move programs.
 */
class MoveDebugProcess(
    session: XDebugSession,
    private val runConfiguration: MoveTestRunConfiguration,
    private val runState: RunProfileState
) : XDebugProcess(session) {
    
    private var processHandler: ProcessHandler? = null
    private var debuggerConnection: MoveDebuggerConnection? = null
    private val breakpointHandler = MoveLineBreakpointHandler(this)
    
    val debugOptions: MoveDebugOptions
        get() = runConfiguration.debugOptions
    
    override fun getBreakpointHandlers(): Array<XBreakpointHandler<*>> {
        return arrayOf<XBreakpointHandler<*>>(breakpointHandler)
    }
    
    override fun getEditorsProvider(): XDebuggerEditorsProvider = MoveDebuggerEditorsProvider()
    
    override fun startStepOver(context: XSuspendContext?) {
        debuggerConnection?.sendCommand(DebugCommand.StepOver)
    }
    
    override fun startStepInto(context: XSuspendContext?) {
        debuggerConnection?.sendCommand(DebugCommand.StepInto)
    }
    
    override fun startStepOut(context: XSuspendContext?) {
        debuggerConnection?.sendCommand(DebugCommand.StepOut)
    }
    
    override fun stop() {
        debuggerConnection?.disconnect()
        processHandler?.destroyProcess()
    }
    
    override fun resume(context: XSuspendContext?) {
        debuggerConnection?.sendCommand(DebugCommand.Resume)
    }
    
    override fun runToPosition(position: XSourcePosition, context: XSuspendContext?) {
        val breakpoint = TemporaryBreakpoint(
            position.file.path,
            position.line + 1 // Convert to 1-based
        )
        debuggerConnection?.sendCommand(DebugCommand.RunToPosition(breakpoint))
    }
    
    override fun sessionInitialized() {
        super.sessionInitialized()
        
        try {
            // Start the debug process
            val commandLine = createDebugCommandLine()
            val process = ProcessBuilder(commandLine)
                .directory(runConfiguration.project.basePath?.let { java.io.File(it) })
                .start()
            
            // Create debug connection
            debuggerConnection = MoveDebuggerConnection(
                process,
                this,
                session.project
            )
            
            // Start listening for debug events
            debuggerConnection?.startListening()
            
            // Create process handler
            processHandler = MoveDebugProcessHandler(process, commandLine.joinToString(" "))
            
            // Initialize breakpoints
            initializeBreakpoints()
            
        } catch (e: Exception) {
            session.reportError("Failed to start debugger: ${e.message}")
            session.stop()
        }
    }
    
    override fun createConsole(): ExecutionConsole {
        // Create console from run state if available
        return ConsoleViewImpl(session.project, true)
    }
    
    private fun createDebugCommandLine(): List<String> {
        val commandLine = mutableListOf<String>()
        
        // Add Sui binary path
        commandLine.add("sui") // Default to "sui" if not specified
        
        // Add debug command
        commandLine.add("debug")
        
        val testConfig = runConfiguration as? MoveTestRunConfiguration
        if (testConfig != null) {
            // Add test-specific debug options
            val testOptions = testConfig.getOptions()
            when (testOptions.testKind) {
                TestKind.MODULE -> {
                    commandLine.add("--module")
                    testOptions.modulePath?.let { commandLine.add(it) }
                }
                TestKind.FUNCTION -> {
                    commandLine.add("--function")
                    testOptions.functionName?.let { commandLine.add(it) }
                }
                TestKind.PACKAGE -> {
                    // Package test - no specific flags needed
                }
            }
            
            testOptions.testFilter?.let {
                commandLine.add("--filter")
                commandLine.add(it)
            }
        }
        
        // Add package path
        runConfiguration.getOptions().packagePath?.let { path ->
            commandLine.add("--path")
            commandLine.add(path)
        }
        
        return commandLine
    }
    
    private fun initializeBreakpoints() {
        // Find all breakpoints in the session
        val breakpointManager = XDebuggerManagerImpl.getInstance(session.project).breakpointManager
        val allBreakpoints = breakpointManager.allBreakpoints
        allBreakpoints.forEach { breakpoint ->
            if (breakpoint is XLineBreakpoint<*>) {
                val file = breakpoint.sourcePosition?.file?.path
                val line = breakpoint.sourcePosition?.line
                if (file != null && line != null) {
                    debuggerConnection?.sendCommand(DebugCommand.AddBreakpoint(MoveBreakpoint(file, line + 1)))
                }
            }
        }
    }
    
    fun addBreakpoint(file: VirtualFile?, line: Int) {
        if (file == null) return
        
        val breakpoint = MoveBreakpoint(file.path, line + 1) // Convert to 1-based
        debuggerConnection?.sendCommand(DebugCommand.AddBreakpoint(breakpoint))
    }
    
    fun removeBreakpoint(file: VirtualFile?, line: Int) {
        if (file == null) return
        
        val breakpoint = MoveBreakpoint(file.path, line + 1) // Convert to 1-based
        debuggerConnection?.sendCommand(DebugCommand.RemoveBreakpoint(breakpoint))
    }
    
    fun handleBreakpointHit(breakpoint: BreakpointInfo) {
        val position = createSourcePosition(breakpoint.file, breakpoint.line) ?: return
        
        // Find the actual breakpoint that was hit
        val breakpointManager = XDebuggerManagerImpl.getInstance(session.project).breakpointManager
        val xBreakpoint = breakpointManager.allBreakpoints.find { bp ->
            bp is XLineBreakpoint<*> && 
            bp.sourcePosition?.file?.path == breakpoint.file &&
            bp.sourcePosition?.line == breakpoint.line - 1 // Convert from 1-based to 0-based
        }
        
        val frames = listOf(
            MoveStackFrame(
                id = 0,
                functionName = "main",
                file = breakpoint.file,
                line = breakpoint.line,
                variables = emptyMap()
            )
        )
        
        if (xBreakpoint != null) {
            session.breakpointReached(
                xBreakpoint,
                breakpoint.message,
                MoveSuspendContext(
                    this,
                    0, // Thread ID
                    frames
                )
            )
        } else {
            // Just pause at position if no breakpoint found
            session.positionReached(
                MoveSuspendContext(
                    this,
                    0, // Thread ID
                    frames
                )
            )
        }
    }
    
    fun handleStepCompleted(event: StepCompletedEvent) {
        val position = createSourcePosition(event.file, event.line - 1) // Convert to 0-based
        val suspendContext = MoveSuspendContext(this, event.threadId, event.frames)
        
        session.positionReached(suspendContext)
    }
    
    private fun createSourcePosition(filePath: String, line: Int): XSourcePosition? {
        val file = com.intellij.openapi.vfs.LocalFileSystem.getInstance()
            .findFileByPath(filePath) ?: return null
        
        return com.intellij.xdebugger.impl.XSourcePositionImpl.create(file, line)
    }
}

/**
 * Debug commands that can be sent to the Move debugger.
 */
sealed class DebugCommand {
    object StepOver : DebugCommand()
    object StepInto : DebugCommand()
    object StepOut : DebugCommand()
    object Resume : DebugCommand()
    data class RunToPosition(val breakpoint: TemporaryBreakpoint) : DebugCommand()
    data class AddBreakpoint(val breakpoint: MoveBreakpoint) : DebugCommand()
    data class RemoveBreakpoint(val breakpoint: MoveBreakpoint) : DebugCommand()
    data class Evaluate(val expression: String, val frameId: Int) : DebugCommand()
}

/**
 * Represents a breakpoint in Move code.
 */
data class MoveBreakpoint(
    val file: String,
    val line: Int
)

/**
 * Temporary breakpoint for run-to-position.
 */
data class TemporaryBreakpoint(
    val file: String,
    val line: Int
)

/**
 * Debug events received from the Move debugger.
 */
sealed class DebugEvent

data class BreakpointHitEvent(
    val file: String,
    val line: Int,
    val threadId: Int,
    val frames: List<MoveStackFrame>
) : DebugEvent()

data class StepCompletedEvent(
    val file: String,
    val line: Int,
    val threadId: Int,
    val frames: List<MoveStackFrame>
) : DebugEvent()

data class EvaluationResultEvent(
    val expression: String,
    val result: String,
    val type: String?
) : DebugEvent()

/**
 * Connection to the Move debugger process.
 */
class MoveDebuggerConnection(
    private val process: Process,
    private val debugProcess: MoveDebugProcess,
    private val project: Project
) {
    private val writer = OutputStreamWriter(process.outputStream)
    private val reader = BufferedReader(InputStreamReader(process.inputStream))
    private var listening = false
    
    fun startListening() {
        listening = true
        Thread {
            while (listening) {
                try {
                    val line = reader.readLine() ?: break
                    handleDebuggerOutput(line)
                } catch (e: Exception) {
                    if (listening) {
                        e.printStackTrace()
                    }
                }
            }
        }.start()
    }
    
    fun sendCommand(command: DebugCommand) {
        val commandStr = when (command) {
            is DebugCommand.StepOver -> "step-over"
            is DebugCommand.StepInto -> "step-into"
            is DebugCommand.StepOut -> "step-out"
            is DebugCommand.Resume -> "resume"
            is DebugCommand.RunToPosition -> "run-to ${command.breakpoint.file}:${command.breakpoint.line}"
            is DebugCommand.AddBreakpoint -> "break ${command.breakpoint.file}:${command.breakpoint.line}"
            is DebugCommand.RemoveBreakpoint -> "clear ${command.breakpoint.file}:${command.breakpoint.line}"
            is DebugCommand.Evaluate -> "eval ${command.frameId} ${command.expression}"
        }
        
        writer.write("$commandStr\n")
        writer.flush()
    }
    
    fun disconnect() {
        listening = false
        writer.close()
        reader.close()
    }
    
    private fun handleDebuggerOutput(line: String) {
        // Parse debugger output and convert to events
        when {
            line.startsWith("BREAK:") -> parseBreakpointHit(line)
            line.startsWith("STEP:") -> parseStepCompleted(line)
            line.startsWith("EVAL:") -> parseEvaluationResult(line)
        }
    }
    
    private fun parseBreakpointHit(line: String) {
        // Format: BREAK:file:line:thread:frame1|frame2|...
        val parts = line.substring(6).split(":")
        if (parts.size >= 4) {
            val file = parts[0]
            val lineNum = parts[1].toIntOrNull() ?: return
            val threadId = parts[2].toIntOrNull() ?: return
            val frames = parseFrames(parts[3])
            
            val event = BreakpointHitEvent(file, lineNum, threadId, frames)
            debugProcess.handleBreakpointHit(BreakpointInfo(file, lineNum, ""))
        }
    }
    
    private fun parseStepCompleted(line: String) {
        // Format: STEP:file:line:thread:frame1|frame2|...
        val parts = line.substring(5).split(":")
        if (parts.size >= 4) {
            val file = parts[0]
            val lineNum = parts[1].toIntOrNull() ?: return
            val threadId = parts[2].toIntOrNull() ?: return
            val frames = parseFrames(parts[3])
            
            val event = StepCompletedEvent(file, lineNum, threadId, frames)
            debugProcess.handleStepCompleted(event)
        }
    }
    
    private fun parseEvaluationResult(line: String) {
        // Format: EVAL:expression:result:type
        val parts = line.substring(5).split(":", limit = 3)
        if (parts.size >= 2) {
            val expression = parts[0]
            val result = parts[1]
            val type = if (parts.size > 2) parts[2] else null
            
            val event = EvaluationResultEvent(expression, result, type)
            // Handle evaluation result
        }
    }
    
    private fun parseFrames(framesStr: String): List<MoveStackFrame> {
        return framesStr.split("|").mapNotNull { frameStr ->
            val parts = frameStr.split(",")
            if (parts.size >= 4) {
                MoveStackFrame(
                    id = parts[0].toIntOrNull() ?: return@mapNotNull null,
                    functionName = parts[1],
                    file = parts[2],
                    line = parts[3].toIntOrNull() ?: return@mapNotNull null
                )
            } else {
                null
            }
        }
    }
}

/**
 * Process handler for the debug process.
 */
class MoveDebugProcessHandler(
    process: Process,
    commandLine: String
) : ProcessHandler() {
    
    init {
        startNotify()
    }
    
    override fun destroyProcessImpl() {
        // Terminate the debug process
    }
    
    override fun detachProcessImpl() {
        // Detach from the debug process
    }
    
    override fun detachIsDefault(): Boolean = false
    
    override fun getProcessInput(): java.io.OutputStream? = null
}

/**
 * Debug command types.
 */

data class BreakpointInfo(val file: String, val line: Int, val message: String)
