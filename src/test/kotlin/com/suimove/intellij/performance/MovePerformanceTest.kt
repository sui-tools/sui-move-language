package com.suimove.intellij.performance

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.PlatformTestUtil
import com.suimove.intellij.MoveFileType
import java.util.concurrent.TimeUnit

class MovePerformanceTest : BasePlatformTestCase() {
    
    // Set timeout for performance tests
    private val TIMEOUT_MS = 5000L
    
    fun testLargeFilePerformance() {
        // Generate a large Move file with many functions and structs
        val sb = StringBuilder()
        sb.append("module 0x1::large_module {\n")
        
        // Add 100 structs
        for (i in 1..100) {
            sb.append("    struct Struct$i {\n")
            // Each struct has 10 fields
            for (j in 1..10) {
                sb.append("        field${j}: u64,\n")
            }
            sb.append("    }\n\n")
        }
        
        // Add 100 functions
        for (i in 1..100) {
            sb.append("    fun function$i() {\n")
            // Each function has 10 statements
            for (j in 1..10) {
                sb.append("        let var$j = $j;\n")
            }
            sb.append("    }\n\n")
        }
        
        sb.append("}")
        
        // Create the large file
        val largeFile = myFixture.configureByText("large_file.move", sb.toString())
        
        // Measure time to parse and highlight the file
        PlatformTestUtil.startPerformanceTest("Parse large Move file", TIMEOUT_MS.toInt()) {
            myFixture.doHighlighting()
        }.assertTiming()
        
        // Measure time for code completion
        PlatformTestUtil.startPerformanceTest("Code completion in large file", TIMEOUT_MS.toInt()) {
            myFixture.completeBasic()
        }.assertTiming()
    }
    
    fun testComplexProjectPerformance() {
        // Create multiple files to simulate a complex project
        for (i in 1..10) {
            myFixture.configureByText("module$i.move", """
                module 0x1::module$i {
                    struct MyStruct$i {
                        field1: u64,
                        field2: bool
                    }
                    
                    fun function$i() {
                        let x = 42;
                        let y = true;
                        if (y) {
                            x + 1
                        } else {
                            x - 1
                        }
                    }
                }
            """.trimIndent())
        }
        
        // Create a file that imports all the other modules
        val mainFile = myFixture.configureByText("main.move", """
            module 0x1::main {
                ${(1..10).joinToString("\n                ") { "use 0x1::module$it;" }}
                
                fun main() {
                    ${(1..10).joinToString("\n                    ") { "module$it::function$it();" }}
                }
            }
        """.trimIndent())
        
        // Measure time to resolve references
        PlatformTestUtil.startPerformanceTest("Cross-module reference resolution", TIMEOUT_MS.toInt()) {
            myFixture.doHighlighting()
        }.assertTiming()
    }
    
    fun testMemoryUsage() {
        // Generate a very large file to test memory usage
        val sb = StringBuilder()
        sb.append("module 0x1::memory_test {\n")
        
        // Add 500 structs
        for (i in 1..500) {
            sb.append("    struct Struct$i {\n")
            // Each struct has 20 fields
            for (j in 1..20) {
                sb.append("        field${j}: u64,\n")
            }
            sb.append("    }\n\n")
        }
        
        sb.append("}")
        
        // Create the large file
        val largeFile = myFixture.configureByText("memory_test.move", sb.toString())
        
        // Measure memory usage before highlighting
        val beforeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        
        // Perform highlighting
        myFixture.doHighlighting()
        
        // Measure memory usage after highlighting
        val afterMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        
        // Calculate memory usage
        val memoryUsed = afterMemory - beforeMemory
        println("Memory used for highlighting: $memoryUsed bytes")
        
        // Assert that memory usage is within reasonable bounds
        // This is a rough estimate and may need adjustment
        assertTrue("Memory usage should be reasonable", memoryUsed < 100 * 1024 * 1024) // 100 MB limit
    }
    
    fun testParsingPerformance() {
        // Generate a file with complex nested expressions
        val sb = StringBuilder()
        sb.append("module 0x1::parsing_test {\n")
        sb.append("    fun complex_expression() {\n")
        
        // Create a deeply nested expression
        var expr = "1"
        for (i in 1..50) {
            expr = "($expr + $i)"
        }
        
        sb.append("        let result = $expr;\n")
        sb.append("    }\n")
        sb.append("}")
        
        // Create the file
        val complexFile = myFixture.configureByText("parsing_test.move", sb.toString())
        
        // Measure time to parse the file
        PlatformTestUtil.startPerformanceTest("Parse complex expressions", TIMEOUT_MS.toInt()) {
            myFixture.doHighlighting()
        }.assertTiming()
    }
    
    fun testTypeInferencePerformance() {
        // Generate a file with complex type inference
        val sb = StringBuilder()
        sb.append("module 0x1::type_inference_test {\n")
        
        // Create a function with many variables and complex types
        sb.append("    fun type_inference() {\n")
        for (i in 1..100) {
            sb.append("        let var$i = ")
            
            // Alternate between different expressions
            when (i % 4) {
                0 -> sb.append("$i;\n")
                1 -> sb.append("vector[$i, ${i+1}, ${i+2}];\n")
                2 -> sb.append("true && false || true;\n")
                3 -> sb.append("@0x${i};\n")
            }
        }
        sb.append("    }\n")
        sb.append("}")
        
        // Create the file
        val typeFile = myFixture.configureByText("type_inference.move", sb.toString())
        
        // Measure time for type inference
        PlatformTestUtil.startPerformanceTest("Type inference performance", TIMEOUT_MS.toInt()) {
            myFixture.doHighlighting()
        }.assertTiming()
    }
}
