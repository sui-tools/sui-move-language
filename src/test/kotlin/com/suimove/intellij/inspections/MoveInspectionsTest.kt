package com.suimove.intellij.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MoveInspectionsTest : BasePlatformTestCase() {
    
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(
            MoveUnusedVariableInspection(),
            MoveNamingConventionInspection()
        )
    }
    
    // Unused Variable Inspection Tests
    
    fun testUnusedVariableDetection() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let <warning descr="Unused variable 'unused_var'">unused_var</warning> = 42;
                    let used_var = 10;
                    let result = used_var + 5;
                }
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(true, false, true)
    }
    
    fun testUnusedVariableWithUnderscore() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let _unused_var = 42; // Should not warn
                    let _ = 10; // Should not warn
                }
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(true, false, true)
    }
    
    fun testUsedVariableNoWarning() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let x = 42;
                    let y = x + 1;
                    let z = y * 2;
                    z
                }
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(true, false, true)
    }
    
    fun testUnusedParameterDetection() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun calculate(x: u64, <warning descr="Unused parameter 'y'">y</warning>: u64): u64 {
                    x * 2
                }
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(true, false, true)
    }
    
    // Naming Convention Inspection Tests
    
    fun testModuleNamingConvention() {
        myFixture.configureByText("test.move", """
            module 0x1::<warning descr="Module name should be in snake_case">TestModule</warning> {
                fun main() {}
            }
            
            module 0x1::valid_module {
                fun main() {}
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(true, false, true)
    }
    
    fun testFunctionNamingConvention() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun <warning descr="Function name should be in snake_case">calculateValue</warning>(): u64 { 42 }
                fun valid_function_name(): u64 { 42 }
                fun <warning descr="Function name should be in snake_case">GetData</warning>(): u64 { 42 }
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(true, false, true)
    }
    
    fun testStructNamingConvention() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                struct ValidStruct {
                    value: u64
                }
                
                struct <warning descr="Struct name should be in PascalCase">invalid_struct</warning> {
                    value: u64
                }
                
                struct <warning descr="Struct name should be in PascalCase">myStruct</warning> {
                    value: u64
                }
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(true, false, true)
    }
    
    fun testConstantNamingConvention() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                const VALID_CONSTANT: u64 = 42;
                const <warning descr="Constant name should be in UPPER_SNAKE_CASE">InvalidConstant</warning>: u64 = 42;
                const <warning descr="Constant name should be in UPPER_SNAKE_CASE">invalid_constant</warning>: u64 = 42;
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(true, false, true)
    }
    
    fun testVariableNamingConvention() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let valid_var = 42;
                    let <warning descr="Variable name should be in snake_case">InvalidVar</warning> = 42;
                    let <warning descr="Variable name should be in snake_case">myVariable</warning> = 42;
                }
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(true, false, true)
    }
    
    fun testSpecialNamingCases() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                // Single letter variables are ok
                fun main() {
                    let x = 42;
                    let y = 10;
                    let i = 0;
                }
                
                // Type parameters follow PascalCase
                fun generic<T, U>() {}
                
                // Numbers in names are ok
                fun calculate_v2() {}
                struct Data123 {}
                const MAX_SIZE_256: u64 = 256;
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(true, false, true)
    }
    
    fun testQuickFixForUnusedVariable() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let <caret>unused = 42;
                }
            }
        """.trimIndent())
        
        val intention = myFixture.findSingleIntention("Rename to _unused")
        assertNotNull("Should have quick fix to rename unused variable", intention)
        
        myFixture.launchAction(intention)
        myFixture.checkResult("""
            module 0x1::test {
                fun main() {
                    let _unused = 42;
                }
            }
        """.trimIndent())
    }
}
