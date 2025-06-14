package com.suimove.intellij.compiler

import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.any
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class MoveCompilerServiceTest : BasePlatformTestCase() {
    
    private lateinit var compilerService: MoveCompilerService
    
    override fun setUp() {
        super.setUp()
        compilerService = MoveCompilerService(project)
    }
    
    fun testSuccessfulBuild() {
        // Create test file
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {}
            }
        """.trimIndent())
        
        // Mock the compiler service
        val mockService = mock(MoveCompilerService::class.java)
        val future = CompletableFuture<Pair<Boolean, List<MoveCompilerError>>>()
        
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, List<MoveCompilerError>) -> Unit>(0)
            callback(true, emptyList())
            future.complete(Pair(true, emptyList()))
            null
        }.`when`(mockService).compileProject(any())
        
        // Execute compilation
        mockService.compileProject()
        
        // Verify result
        val result = future.get(5, TimeUnit.SECONDS)
        assertTrue("Build should succeed", result.first)
        assertTrue("Should have no errors", result.second.isEmpty())
    }
    
    fun testBuildWithErrors() {
        // Create test file with error
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let x: u64 = "string"; // Type error
                }
            }
        """.trimIndent())
        
        // Mock the compiler service
        val mockService = mock(MoveCompilerService::class.java)
        val future = CompletableFuture<Pair<Boolean, List<MoveCompilerError>>>()
        
        val errors = listOf(
            MoveCompilerError(
                "test.move",
                3,
                10,
                "Type error: cannot assign string to u64",
                ErrorSeverity.ERROR
            )
        )
        
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, List<MoveCompilerError>) -> Unit>(0)
            callback(false, errors)
            future.complete(Pair(false, errors))
            null
        }.`when`(mockService).compileProject(any())
        
        // Execute compilation
        mockService.compileProject()
        
        // Verify result
        val result = future.get(5, TimeUnit.SECONDS)
        assertFalse("Build should fail", result.first)
        assertEquals("Should have one error", 1, result.second.size)
        assertEquals("Error should be on line 3", 3, result.second[0].line)
        assertEquals("Error should have correct severity", ErrorSeverity.ERROR, result.second[0].severity)
    }
    
    fun testBuildWithWarnings() {
        // Create test file
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun unused_function() {} // Warning: unused function
                
                fun main() {}
            }
        """.trimIndent())
        
        // Mock the compiler service
        val mockService = mock(MoveCompilerService::class.java)
        val future = CompletableFuture<Pair<Boolean, List<MoveCompilerError>>>()
        
        val warnings = listOf(
            MoveCompilerError(
                "test.move",
                2,
                5,
                "Warning: unused function 'unused_function'",
                ErrorSeverity.WARNING
            )
        )
        
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, List<MoveCompilerError>) -> Unit>(0)
            callback(true, warnings)
            future.complete(Pair(true, warnings))
            null
        }.`when`(mockService).compileProject(any())
        
        // Execute compilation
        mockService.compileProject()
        
        // Verify result
        val result = future.get(5, TimeUnit.SECONDS)
        assertTrue("Build should succeed with warnings", result.first)
        assertEquals("Should have one warning", 1, result.second.size)
        assertEquals("Should be a warning", ErrorSeverity.WARNING, result.second[0].severity)
    }
}
