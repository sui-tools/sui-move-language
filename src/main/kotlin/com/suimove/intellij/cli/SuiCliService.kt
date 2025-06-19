package com.suimove.intellij.cli

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessOutput
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.EnvironmentUtil
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Service for interacting with the Sui CLI.
 */
@Service(Service.Level.PROJECT)
class SuiCliService(private val project: Project) {
    
    companion object {
        private val LOG = Logger.getInstance(SuiCliService::class.java)
        
        fun getInstance(project: Project): SuiCliService = project.service()
        
        private const val DEFAULT_SUI_BINARY = "sui"
        private val COMMON_SUI_PATHS = listOf(
            "/usr/local/bin/sui",
            "/opt/homebrew/bin/sui",
            "\${HOME}/.cargo/bin/sui"
        )
        
        private val SUI_BINARY_NAME = if (SystemInfo.isWindows) "sui.exe" else DEFAULT_SUI_BINARY
        private const val CARGO_BIN_DIR = ".cargo/bin"
    }
    
    private var cachedSuiPath: String? = null
    private var cachedVersion: SuiVersion? = null
    
    /**
     * Get the path to the Sui CLI executable.
     */
    fun getSuiPath(): String? {
        if (cachedSuiPath != null) {
            return cachedSuiPath
        }
        
        // Try to find Sui in various locations
        val suiPath = findSuiExecutable()
        if (suiPath != null) {
            cachedSuiPath = suiPath
            LOG.info("Found Sui CLI at: $suiPath")
        } else {
            LOG.warn("Sui CLI not found in PATH or common locations")
        }
        
        return suiPath
    }
    
    /**
     * Get the Sui CLI version.
     */
    fun getVersion(): SuiVersion? {
        if (cachedVersion != null) {
            return cachedVersion
        }
        
        val suiPath = getSuiPath() ?: return null
        
        try {
            val output = executeCommand(listOf(suiPath, "--version"))
            if (output.exitCode == 0) {
                cachedVersion = parseSuiVersion(output.stdout)
                return cachedVersion
            }
        } catch (e: Exception) {
            LOG.error("Failed to get Sui version", e)
        }
        
        return null
    }
    
    /**
     * Execute a Sui command.
     */
    fun executeSuiCommand(
        args: List<String>,
        workingDirectory: File? = null,
        environmentVariables: Map<String, String> = emptyMap()
    ): ProcessOutput {
        val suiPath = getSuiPath() ?: throw SuiNotFoundException("Sui CLI not found")
        
        val command = mutableListOf(suiPath)
        command.addAll(args)
        
        return executeCommand(command, workingDirectory, environmentVariables)
    }
    
    /**
     * Build a Move package.
     */
    fun buildPackage(packagePath: String): ProcessOutput {
        return executeSuiCommand(
            listOf("move", "build"),
            workingDirectory = File(packagePath)
        )
    }
    
    /**
     * Test a Move package.
     */
    fun testPackage(
        packagePath: String,
        filter: String? = null,
        gasLimit: Long? = null,
        coverage: Boolean = false
    ): ProcessOutput {
        val args = mutableListOf("move", "test")
        
        filter?.let {
            args.add("--filter")
            args.add(it)
        }
        
        gasLimit?.let {
            args.add("--gas-limit")
            args.add(it.toString())
        }
        
        if (coverage) {
            args.add("--coverage")
        }
        
        return executeSuiCommand(args, workingDirectory = File(packagePath))
    }
    
    /**
     * Publish a Move package.
     */
    fun publishPackage(
        packagePath: String,
        gasObject: String? = null,
        gasBudget: Long? = null
    ): ProcessOutput {
        val args = mutableListOf("client", "publish")
        
        gasObject?.let {
            args.add("--gas")
            args.add(it)
        }
        
        gasBudget?.let {
            args.add("--gas-budget")
            args.add(it.toString())
        }
        
        return executeSuiCommand(args, workingDirectory = File(packagePath))
    }
    
    /**
     * Get active address.
     */
    fun getActiveAddress(): String? {
        try {
            val output = executeSuiCommand(listOf("client", "active-address"))
            if (output.exitCode == 0) {
                return output.stdout.trim()
            }
        } catch (e: Exception) {
            LOG.error("Failed to get active address", e)
        }
        return null
    }
    
    /**
     * Get list of addresses.
     */
    fun getAddresses(): List<String> {
        try {
            val output = executeSuiCommand(listOf("keytool", "list"))
            if (output.exitCode == 0) {
                return parseAddressList(output.stdout)
            }
        } catch (e: Exception) {
            LOG.error("Failed to get addresses", e)
        }
        return emptyList()
    }
    
    /**
     * Check if Sui CLI is available.
     */
    fun isAvailable(): Boolean {
        return getSuiPath() != null
    }
    
    /**
     * Invalidate cached data.
     */
    fun invalidateCache() {
        cachedSuiPath = null
        cachedVersion = null
    }
    
    private fun findSuiExecutable(): String? {
        // Check if sui is in PATH
        val pathSui = EnvironmentUtil.getValue("PATH")
            ?.split(File.pathSeparator)
            ?.map { File(it, SUI_BINARY_NAME) }
            ?.firstOrNull { it.exists() && it.canExecute() }
            ?.absolutePath
        
        if (pathSui != null) {
            return pathSui
        }
        
        // Check common installation locations
        val homeDir = System.getProperty("user.home")
        val commonPaths = COMMON_SUI_PATHS.map { it.replace("\${HOME}", homeDir) }.map { Paths.get(it).toFile() }
        
        return commonPaths
            .firstOrNull { it.exists() && it.canExecute() }
            ?.absolutePath
    }
    
    private fun executeCommand(
        command: List<String>,
        workingDirectory: File? = null,
        environmentVariables: Map<String, String> = emptyMap()
    ): ProcessOutput {
        val commandLine = GeneralCommandLine(command)
        
        workingDirectory?.let {
            commandLine.workDirectory = it
        }
        
        commandLine.environment.putAll(environmentVariables)
        
        return ExecUtil.execAndGetOutput(commandLine)
    }
    
    private fun parseSuiVersion(output: String): SuiVersion? {
        // Expected format: sui 1.14.0
        val regex = Regex("""sui\s+(\d+)\.(\d+)\.(\d+)""")
        val match = regex.find(output) ?: return null
        
        return try {
            SuiVersion(
                major = match.groupValues[1].toInt(),
                minor = match.groupValues[2].toInt(),
                patch = match.groupValues[3].toInt()
            )
        } catch (e: Exception) {
            LOG.warn("Failed to parse Sui version from: $output", e)
            null
        }
    }
    
    private fun parseAddressList(output: String): List<String> {
        // Parse address list from keytool output
        return output.lines()
            .filter { it.contains("0x") }
            .mapNotNull { line ->
                Regex("""(0x[a-fA-F0-9]+)""").find(line)?.value
            }
    }
}

/**
 * Represents a Sui CLI version.
 */
data class SuiVersion(
    val major: Int,
    val minor: Int,
    val patch: Int
) : Comparable<SuiVersion> {
    
    override fun compareTo(other: SuiVersion): Int {
        return compareValuesBy(this, other, { it.major }, { it.minor }, { it.patch })
    }
    
    override fun toString(): String = "$major.$minor.$patch"
    
    fun isAtLeast(major: Int, minor: Int = 0, patch: Int = 0): Boolean {
        return this >= SuiVersion(major, minor, patch)
    }
}

/**
 * Exception thrown when Sui CLI is not found.
 */
class SuiNotFoundException(message: String) : Exception(message)
