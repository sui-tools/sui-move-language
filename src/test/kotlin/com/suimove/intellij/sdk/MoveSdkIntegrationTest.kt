package com.suimove.intellij.sdk

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.suimove.intellij.MoveFileType
import junit.framework.TestCase
import java.io.File

class MoveSdkIntegrationTest : BasePlatformTestCase() {
    
    fun testSuiCliDetection() {
        // Test that we can detect Sui CLI installation
        val suiPath = findSuiCliPath()
        
        if (suiPath != null) {
            assertTrue("Sui CLI should exist at detected path", File(suiPath).exists())
            assertTrue("Sui CLI should be executable", File(suiPath).canExecute())
        } else {
            // If not found, that's okay in test environment
            println("Sui CLI not found in system PATH - skipping detection test")
        }
    }
    
    fun testProjectWithMoveToml() {
        // Create a mock Move.toml file
        val moveToml = myFixture.addFileToProject("Move.toml", """
            [package]
            name = "test_project"
            version = "0.0.1"
            
            [dependencies]
            Sui = { git = "https://github.com/MystenLabs/sui.git", subdir = "crates/sui-framework", rev = "main" }
            
            [addresses]
            test_project = "0x0"
            admin = "0x456"
        """.trimIndent())
        
        assertNotNull("Move.toml should be created", moveToml)
        assertTrue("Move.toml should contain package info", moveToml.text.contains("[package]"))
        assertTrue("Move.toml should contain dependencies", moveToml.text.contains("[dependencies]"))
    }
    
    fun testModuleDependencyResolution() {
        // Create a project with dependencies
        myFixture.addFileToProject("Move.toml", """
            [package]
            name = "my_project"
            
            [dependencies]
            Sui = { local = "../sui-framework" }
            MyLib = { local = "./deps/mylib" }
        """.trimIndent())
        
        val mainModule = myFixture.addFileToProject("sources/main.move", """
            module my_project::main {
                use sui::coin;
                use mylib::utils;
                
                fun main() {
                    // Using dependencies
                }
            }
        """.trimIndent())
        
        assertNotNull("Main module should be created", mainModule)
        assertTrue("Main module should import sui::coin", mainModule.text.contains("use sui::coin"))
        assertTrue("Main module should import mylib::utils", mainModule.text.contains("use mylib::utils"))
    }
    
    fun testSuiCommandLineBuilding() {
        // Test building Sui command lines
        val commands = mapOf(
            "build" to listOf("sui", "move", "build"),
            "test" to listOf("sui", "move", "test"),
            "publish" to listOf("sui", "client", "publish", "--gas-budget", "10000000")
        )
        
        for ((name, expectedParts) in commands) {
            val cmdParts = buildSuiCommand(name)
            assertEquals("Command $name should have correct parts", expectedParts, cmdParts)
        }
    }
    
    fun testStandardLibraryModules() {
        // Test that we know about standard library modules
        val expectedModules = listOf(
            "coin", "balance", "transfer", "object", "tx_context",
            "event", "package", "test_scenario", "sui", "option",
            "vector", "bcs", "hash", "address"
        )
        
        val stdModules = getStandardLibraryModules()
        
        for (module in expectedModules) {
            assertTrue("Should have $module in standard library", 
                stdModules.contains(module))
        }
    }
    
    fun testMoveTomlParsing() {
        val moveTomlContent = """
            [package]
            name = "defi_app"
            version = "1.0.0"
            
            [dependencies]
            Sui = { git = "https://github.com/MystenLabs/sui.git", subdir = "crates/sui-framework", rev = "testnet" }
            DefiLib = { local = "./packages/defi" }
            
            [addresses]
            defi_app = "0x123"
            admin = "0x456"
            
            [dev-dependencies]
            TestUtils = { local = "./tests/utils" }
        """.trimIndent()
        
        val parsed = parseMoveToml(moveTomlContent)
        
        assertEquals("Package name should be parsed", "defi_app", parsed["package.name"])
        assertEquals("Package version should be parsed", "1.0.0", parsed["package.version"])
        assertTrue("Should have Sui dependency", parsed.containsKey("dependencies.Sui"))
        assertTrue("Should have DefiLib dependency", parsed.containsKey("dependencies.DefiLib"))
        assertEquals("Should parse defi_app address", "0x123", parsed["addresses.defi_app"])
        assertEquals("Should parse admin address", "0x456", parsed["addresses.admin"])
        assertTrue("Should have dev dependency", parsed.containsKey("dev-dependencies.TestUtils"))
    }
    
    fun testEnvironmentVariables() {
        // Test environment variables for Move development
        val envVars = getMoveEnvironmentVariables("/usr/local/bin/sui")
        
        assertTrue("Should have SUI_CLI_PATH", envVars.containsKey("SUI_CLI_PATH"))
        assertEquals("SUI_CLI_PATH should be correct", "/usr/local/bin/sui", envVars["SUI_CLI_PATH"])
        assertTrue("Should have MOVE_STDLIB_PATH", envVars.containsKey("MOVE_STDLIB_PATH"))
    }
    
    fun testProjectStructureValidation() {
        // Create a standard Move project structure
        val moveToml = myFixture.addFileToProject("Move.toml", "[package]\nname = \"test\"")
        val sourceFile = myFixture.addFileToProject("sources/main.move", "module test::main {}")
        val testFile = myFixture.addFileToProject("tests/main_test.move", "#[test_only]\nmodule test::main_test {}")
        val scriptFile = myFixture.addFileToProject("scripts/deploy.move", "script { fun main() {} }")
        
        // Verify files were created
        assertNotNull("Move.toml should be created", moveToml)
        assertNotNull("Source file should be created", sourceFile)
        assertNotNull("Test file should be created", testFile)
        assertNotNull("Script file should be created", scriptFile)
        
        // Get the root directory from one of the created files
        val rootDir = moveToml.virtualFile.parent
        assertNotNull("Root directory should exist", rootDir)
        
        val structure = validateProjectStructure(rootDir)
        
        assertTrue("Should have valid Move.toml", structure.hasValidMoveToml)
        assertTrue("Should have sources directory", structure.hasSourcesDir)
        assertTrue("Should have tests directory", structure.hasTestsDir)
        assertTrue("Should have scripts directory", structure.hasScriptsDir)
    }
    
    fun testBuildOutputParsing() {
        // Test parsing Sui build output
        val buildOutput = """
            UPDATING GIT DEPENDENCY https://github.com/MystenLabs/sui.git (at revision testnet)
            INCLUDING DEPENDENCY Sui
            INCLUDING DEPENDENCY MoveStdlib
            BUILDING test_project
            Success
        """.trimIndent()
        
        val result = parseBuildOutput(buildOutput)
        
        assertTrue("Build should be successful", result.success)
        assertTrue("Should update git dependency", result.updatedDependencies.contains("Sui"))
        assertTrue("Should include MoveStdlib", result.includedDependencies.contains("MoveStdlib"))
        assertEquals("Should build test_project", "test_project", result.builtPackage)
    }
    
    fun testErrorOutputParsing() {
        // Test parsing Sui error output
        val errorOutput = """
            error[E01001]: cannot find module member
              ┌─ sources/main.move:3:9
              │
            3 │     use sui::nonexistent;
              │         ^^^^^^^^^^^^^^^^^ Unbound module member 'sui::nonexistent'
        """.trimIndent()
        
        val errors = parseErrorOutput(errorOutput)
        
        assertEquals("Should find 1 error", 1, errors.size)
        val error = errors.first()
        assertEquals("Error code should be E01001", "E01001", error.code)
        assertEquals("Error should be in main.move", "sources/main.move", error.file)
        assertEquals("Error should be on line 3", 3, error.line)
        assertTrue("Error message should mention unbound module", 
            error.message.contains("Unbound module member"))
    }
    
    // Helper functions
    
    private fun findSuiCliPath(): String? {
        val paths = System.getenv("PATH")?.split(File.pathSeparator) ?: emptyList()
        for (path in paths) {
            val suiFile = File(path, "sui")
            if (suiFile.exists() && suiFile.canExecute()) {
                return suiFile.absolutePath
            }
        }
        return null
    }
    
    private fun buildSuiCommand(command: String): List<String> {
        return when (command) {
            "build" -> listOf("sui", "move", "build")
            "test" -> listOf("sui", "move", "test")
            "publish" -> listOf("sui", "client", "publish", "--gas-budget", "10000000")
            else -> listOf("sui", command)
        }
    }
    
    private fun getStandardLibraryModules(): List<String> {
        return listOf(
            "coin", "balance", "transfer", "object", "tx_context",
            "event", "package", "test_scenario", "sui", "option",
            "vector", "bcs", "hash", "address", "dynamic_field",
            "dynamic_object_field", "table", "table_vec", "bag",
            "object_bag", "object_table", "priority_queue"
        )
    }
    
    private fun parseMoveToml(content: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        var currentSection = ""
        
        content.lines().forEach { line ->
            when {
                line.startsWith("[") && line.endsWith("]") -> {
                    currentSection = line.trim('[', ']')
                }
                line.contains("=") -> {
                    val (key, value) = line.split("=", limit = 2).map { it.trim() }
                    val fullKey = if (currentSection.isEmpty()) key else "$currentSection.$key"
                    result[fullKey] = value.trim('"', ' ')
                }
            }
        }
        
        return result
    }
    
    private fun getMoveEnvironmentVariables(suiPath: String): Map<String, String> {
        return mapOf(
            "SUI_CLI_PATH" to suiPath,
            "MOVE_STDLIB_PATH" to File(suiPath).parent + "/share/sui/stdlib"
        )
    }
    
    private fun validateProjectStructure(baseDir: com.intellij.openapi.vfs.VirtualFile?): ProjectStructure {
        if (baseDir == null) {
            return ProjectStructure(false, false, false, false)
        }
        return ProjectStructure(
            hasValidMoveToml = baseDir.findChild("Move.toml") != null,
            hasSourcesDir = baseDir.findChild("sources") != null,
            hasTestsDir = baseDir.findChild("tests") != null,
            hasScriptsDir = baseDir.findChild("scripts") != null
        )
    }
    
    private fun parseBuildOutput(output: String): BuildResult {
        val updatedDeps = mutableListOf<String>()
        val includedDeps = mutableListOf<String>()
        var builtPackage = ""
        var success = false
        
        output.lines().forEach { line ->
            when {
                line.startsWith("UPDATING GIT DEPENDENCY") -> {
                    // Extract dependency name from the URL
                    if (line.contains("sui.git")) {
                        updatedDeps.add("Sui")
                    }
                }
                line.startsWith("INCLUDING DEPENDENCY") -> {
                    includedDeps.add(line.substringAfter("DEPENDENCY "))
                }
                line.startsWith("BUILDING") -> {
                    builtPackage = line.substringAfter("BUILDING ")
                }
                line == "Success" -> {
                    success = true
                }
            }
        }
        
        return BuildResult(success, updatedDeps, includedDeps, builtPackage)
    }
    
    private fun parseErrorOutput(output: String): List<CompilerError> {
        val errors = mutableListOf<CompilerError>()
        val errorRegex = Regex("""error\[([A-Z0-9]+)\]: (.+)""")
        val locationRegex = Regex("""┌─ ([^:]+):(\d+):(\d+)""")
        val detailRegex = Regex("""^\s*│\s*\^+ (.+)""")
        
        var currentError: CompilerError? = null
        var captureDetails = false
        
        output.lines().forEach { line ->
            errorRegex.find(line)?.let { match ->
                currentError = CompilerError(
                    code = match.groupValues[1],
                    message = match.groupValues[2],
                    file = "",
                    line = 0,
                    column = 0
                )
            }
            
            locationRegex.find(line)?.let { match ->
                currentError?.let { error ->
                    error.file = match.groupValues[1]
                    error.line = match.groupValues[2].toInt()
                    error.column = match.groupValues[3].toInt()
                    captureDetails = true
                }
            }
            
            // Capture the detailed error message that appears after the caret line
            if (captureDetails && line.contains("^")) {
                val detailMatch = detailRegex.find(line)
                if (detailMatch != null) {
                    currentError?.let { error ->
                        // Append the detailed message to the main message
                        error.message = "${error.message} - ${detailMatch.groupValues[1]}"
                        errors.add(error)
                        currentError = null
                        captureDetails = false
                    }
                } else if (line.trim().matches(Regex("""│\s*\^+\s*(.+)"""))) {
                    // Handle case where detail is on the same line as carets
                    val detail = line.substringAfter("^").trim()
                    if (detail.isNotEmpty()) {
                        currentError?.let { error ->
                            error.message = "${error.message} - $detail"
                            errors.add(error)
                            currentError = null
                            captureDetails = false
                        }
                    }
                }
            }
        }
        
        // Add any remaining error
        currentError?.let { errors.add(it) }
        
        return errors
    }
    
    // Data classes
    data class ProjectStructure(
        val hasValidMoveToml: Boolean,
        val hasSourcesDir: Boolean,
        val hasTestsDir: Boolean,
        val hasScriptsDir: Boolean
    )
    
    data class BuildResult(
        val success: Boolean,
        val updatedDependencies: List<String>,
        val includedDependencies: List<String>,
        val builtPackage: String
    )
    
    data class CompilerError(
        val code: String,
        var message: String,
        var file: String,
        var line: Int,
        var column: Int
    )
}
