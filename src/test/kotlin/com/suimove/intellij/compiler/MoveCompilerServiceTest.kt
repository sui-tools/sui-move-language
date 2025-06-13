package com.suimove.intellij.compiler

import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.*
import java.io.File

class MoveCompilerServiceTest : BasePlatformTestCase() {
    
    private lateinit var compilerService: MoveCompilerService
    private lateinit var mockProject: Project
    
    override fun setUp() {
        super.setUp()
        mockProject = mockk<Project>()
        compilerService = MoveCompilerService()
    }
    
    override fun tearDown() {
        unmockkAll()
        super.tearDown()
    }
    
    fun testSuccessfulBuild() {
        // Mock successful build output
        val mockOutput = """
            BUILDING MyModule
            Success
        """.trimIndent()
        
        mockkStatic("com.suimove.intellij.utils.MoveCommandRunner")
        every { 
            com.suimove.intellij.utils.MoveCommandRunner.runCommand(
                any(), 
                eq("sui"), 
                eq(listOf("move", "build")), 
                any()
            ) 
        } returns mockOutput
        
        val result = compilerService.build(project, File("/test/project"))
        
        assertTrue("Build should succeed", result.success)
        assertTrue("Should have no errors", result.errors.isEmpty())
    }
    
    fun testBuildWithErrors() {
        // Mock build output with errors
        val mockOutput = """
            error[E01001]: type mismatch
              ┌─ /test/project/sources/test.move:5:13
              │
            5 │     let x: u64 = true;
              │             ^^^   ---- expected 'u64', found 'bool'
              │
            
            error[E01002]: undefined function
              ┌─ /test/project/sources/test.move:10:9
              │
           10 │         undefined_function();
              │         ^^^^^^^^^^^^^^^^^^ function not found
        """.trimIndent()
        
        mockkStatic("com.suimove.intellij.utils.MoveCommandRunner")
        every { 
            com.suimove.intellij.utils.MoveCommandRunner.runCommand(
                any(), 
                eq("sui"), 
                eq(listOf("move", "build")), 
                any()
            ) 
        } returns mockOutput
        
        val result = compilerService.build(project, File("/test/project"))
        
        assertFalse("Build should fail", result.success)
        assertEquals("Should have 2 errors", 2, result.errors.size)
        
        val firstError = result.errors[0]
        assertEquals("Should parse error type", "type mismatch", firstError.message)
        assertEquals("Should parse file path", "/test/project/sources/test.move", firstError.file)
        assertEquals("Should parse line number", 5, firstError.line)
        assertEquals("Should parse column number", 13, firstError.column)
    }
    
    fun testBuildWithWarnings() {
        // Mock build output with warnings
        val mockOutput = """
            warning[W01001]: unused variable
              ┌─ /test/project/sources/test.move:3:9
              │
            3 │     let unused_var = 42;
              │         ^^^^^^^^^^ variable is never used
              │
              = help: consider prefixing with an underscore: '_unused_var'
            
            BUILDING MyModule
            Success
        """.trimIndent()
        
        mockkStatic("com.suimove.intellij.utils.MoveCommandRunner")
        every { 
            com.suimove.intellij.utils.MoveCommandRunner.runCommand(
                any(), 
                eq("sui"), 
                eq(listOf("move", "build")), 
                any()
            ) 
        } returns mockOutput
        
        val result = compilerService.build(project, File("/test/project"))
        
        assertTrue("Build should succeed with warnings", result.success)
        assertEquals("Should have 1 warning", 1, result.warnings.size)
        
        val warning = result.warnings[0]
        assertEquals("Should parse warning message", "unused variable", warning.message)
        assertTrue("Should include help text", warning.details.contains("consider prefixing"))
    }
    
    fun testCompilerNotFound() {
        mockkStatic("com.suimove.intellij.utils.MoveCommandRunner")
        every { 
            com.suimove.intellij.utils.MoveCommandRunner.runCommand(
                any(), 
                eq("sui"), 
                any(), 
                any()
            ) 
        } throws RuntimeException("Command not found: sui")
        
        val result = compilerService.build(project, File("/test/project"))
        
        assertFalse("Build should fail", result.success)
        assertTrue("Should have error about missing compiler", 
            result.errors.any { it.message.contains("Command not found") })
    }
    
    fun testInvalidProjectPath() {
        val result = compilerService.build(project, File("/non/existent/path"))
        
        assertFalse("Build should fail for invalid path", result.success)
        assertTrue("Should have error about invalid path",
            result.errors.any { it.message.contains("not found") || it.message.contains("does not exist") })
    }
}
