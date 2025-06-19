package com.suimove.intellij.testing

import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.testframework.TestConsoleProperties
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.suimove.intellij.cli.SuiCliService
import java.io.File

/**
 * Execution state for running Move tests.
 */
class MoveTestRunState(
    environment: ExecutionEnvironment,
    private val configuration: MoveTestRunConfiguration
) : CommandLineState(environment) {
    
    override fun startProcess(): ProcessHandler {
        val commandLine = createCommandLine()
        val processHandler = ColoredProcessHandler(commandLine)
        
        // Add output parser for test results
        processHandler.addProcessListener(MoveTestOutputParser(environment.project))
        
        return processHandler
    }
    
    override fun execute(executor: Executor, runner: ProgramRunner<*>): ExecutionResult {
        val processHandler = startProcess()
        val console = createConsole(executor)
        console?.attachToProcess(processHandler)
        
        return DefaultExecutionResult(console, processHandler)
    }
    
    private fun createCommandLine(): GeneralCommandLine {
        val options = configuration.getOptions()
        val suiPath = SuiCliService.getInstance(environment.project).getSuiPath()
            ?: throw RuntimeException("Sui CLI not found")
        
        val commandLine = GeneralCommandLine(suiPath)
        commandLine.addParameter("move")
        commandLine.addParameter("test")
        
        // Add test-specific parameters based on test kind
        when (options.testKind) {
            TestKind.MODULE -> {
                options.modulePath?.let { path ->
                    commandLine.addParameter("--module")
                    commandLine.addParameter(File(path).nameWithoutExtension)
                }
            }
            TestKind.FUNCTION -> {
                options.functionName?.let { name ->
                    commandLine.addParameter("--filter")
                    commandLine.addParameter(name)
                }
            }
            TestKind.PACKAGE -> {
                // Package path is set as working directory
            }
        }
        
        // Add test filter if specified
        options.testFilter?.let { filter ->
            commandLine.addParameter("--filter")
            commandLine.addParameter(filter)
        }
        
        // Add gas limit
        options.gasLimit?.let { limit ->
            commandLine.addParameter("--gas-limit")
            commandLine.addParameter(limit.toString())
        }
        
        // Add coverage flag
        if (options.coverage) {
            commandLine.addParameter("--coverage")
        }
        
        // Add verbose output
        if (options.showOutput) {
            commandLine.addParameter("--verbose")
        }
        
        // Add additional arguments
        options.additionalArguments?.split(" ")?.forEach { arg ->
            if (arg.isNotBlank()) {
                commandLine.addParameter(arg)
            }
        }
        
        // Set working directory
        val workingDir = when (options.testKind) {
            TestKind.PACKAGE -> options.packagePath?.let { File(it) }
            TestKind.MODULE -> options.modulePath?.let { File(it).parentFile }
            TestKind.FUNCTION -> options.modulePath?.let { File(it).parentFile }
        } ?: File(environment.project.basePath ?: ".")
        
        commandLine.workDirectory = workingDir
        
        // Set environment variables
        commandLine.environment.putAll(options.environmentVariables)
        
        return commandLine
    }
    
    override fun createConsole(executor: Executor): ConsoleView? {
        val testConsoleProperties = MoveTestConsoleProperties(configuration, executor)
        return SMTestRunnerConnectionUtil.createConsole(
            testConsoleProperties.testFrameworkName,
            testConsoleProperties
        )
    }
}

/**
 * Console properties for Move test runner.
 */
class MoveTestConsoleProperties(
    config: MoveTestRunConfiguration,
    executor: Executor
) : SMTRunnerConsoleProperties(config, "Move Test", executor) {
    
    init {
        isUsePredefinedMessageFilter = false
        setIfUndefined(TestConsoleProperties.HIDE_PASSED_TESTS, false)
        setIfUndefined(TestConsoleProperties.HIDE_IGNORED_TEST, false)
        setIfUndefined(TestConsoleProperties.SCROLL_TO_SOURCE, true)
        setIfUndefined(TestConsoleProperties.SELECT_FIRST_DEFECT, true)
        isIdBasedTestTree = true
    }
}

/**
 * Parser for Move test output.
 */
class MoveTestOutputParser(private val project: Project) : ProcessAdapter() {
    
    companion object {
        private val TEST_STARTED = Regex("""Running (\d+) tests? for (.+)""")
        private val TEST_PASSED = Regex("""✅ Test (.+) passed\.""")
        private val TEST_FAILED = Regex("""❌ Test (.+) failed with (.+)""")
        private val TEST_RESULT = Regex("""Test result: (OK|FAILED)\. Total tests: (\d+); passed: (\d+); failed: (\d+)""")
        private val COVERAGE_SUMMARY = Regex("""Coverage summary: (\d+\.\d+)% coverage, (\d+) hits, (\d+) misses""")
        
        val TEST_OUTPUT_KEY = Key<StringBuilder>("MOVE_TEST_OUTPUT")
    }
    
    private val outputBuilder = StringBuilder()
    
    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        val text = event.text
        outputBuilder.append(text)
        
        // Parse test events
        when {
            TEST_STARTED.matches(text) -> {
                val match = TEST_STARTED.find(text)!!
                val testCount = match.groupValues[1].toInt()
                val moduleName = match.groupValues[2]
                // Notify test framework about test suite start
            }
            
            TEST_PASSED.matches(text) -> {
                val match = TEST_PASSED.find(text)!!
                val testName = match.groupValues[1]
                // Notify test framework about test pass
            }
            
            TEST_FAILED.matches(text) -> {
                val match = TEST_FAILED.find(text)!!
                val testName = match.groupValues[1]
                val error = match.groupValues[2]
                // Notify test framework about test failure
            }
            
            TEST_RESULT.matches(text) -> {
                val match = TEST_RESULT.find(text)!!
                val status = match.groupValues[1]
                val total = match.groupValues[2].toInt()
                val passed = match.groupValues[3].toInt()
                val failed = match.groupValues[4].toInt()
                // Notify test framework about overall results
            }
            
            COVERAGE_SUMMARY.matches(text) -> {
                val match = COVERAGE_SUMMARY.find(text)!!
                val percentage = match.groupValues[1].toDouble()
                val hits = match.groupValues[2].toInt()
                val misses = match.groupValues[3].toInt()
                // Store coverage data for display
            }
        }
    }
    
    override fun processTerminated(event: ProcessEvent) {
        // Store the complete output
        event.processHandler.putUserData(TEST_OUTPUT_KEY, outputBuilder)
    }
}
