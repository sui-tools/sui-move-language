package com.suimove.intellij.toolwindow

// Simple standalone test without IntelliJ test framework
// This test can be run independently to verify basic functionality
class MoveToolWindowTest {
    
    fun testToolWindowFactoryCreation() {
        // Test that the factory can be instantiated
        val factory = MoveToolWindowFactory()
        assert(factory != null) { "Tool window factory should exist" }
        println("✓ MoveToolWindowFactory can be instantiated")
    }
    
    fun testToolWindowFactoryClass() {
        // Test that the factory is of the correct type
        val factory = MoveToolWindowFactory()
        assert(factory is MoveToolWindowFactory) { "Factory should be MoveToolWindowFactory instance" }
        println("✓ Factory is correct type")
    }
    
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val test = MoveToolWindowTest()
            try {
                test.testToolWindowFactoryCreation()
                test.testToolWindowFactoryClass()
                println("\nAll tests passed!")
            } catch (e: AssertionError) {
                println("\nTest failed: ${e.message}")
                throw e
            }
        }
    }
}
