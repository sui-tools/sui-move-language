package com.suimove.intellij.intentions

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MoveIntentionsTest : BasePlatformTestCase() {
    
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections()
    }
    
    // Add Type Annotation Intention Tests
    
    fun testAddTypeAnnotationToVariable() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let <caret>x = 42;
                }
            }
        """.trimIndent())
        
        val intention = myFixture.findSingleIntention("Add type annotation")
        assertNotNull("Should have intention to add type annotation", intention)
        
        myFixture.launchAction(intention)
        myFixture.checkResult("""
            module 0x1::test {
                fun main() {
                    let x: u64 = 42;
                }
            }
        """.trimIndent())
    }
    
    fun testAddTypeAnnotationToBoolVariable() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let <caret>flag = true;
                }
            }
        """.trimIndent())
        
        val intention = myFixture.findSingleIntention("Add type annotation")
        myFixture.launchAction(intention)
        
        myFixture.checkResult("""
            module 0x1::test {
                fun main() {
                    let flag: bool = true;
                }
            }
        """.trimIndent())
    }
    
    fun testAddTypeAnnotationToAddressVariable() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let <caret>addr = @0x1;
                }
            }
        """.trimIndent())
        
        val intention = myFixture.findSingleIntention("Add type annotation")
        myFixture.launchAction(intention)
        
        myFixture.checkResult("""
            module 0x1::test {
                fun main() {
                    let addr: address = @0x1;
                }
            }
        """.trimIndent())
    }
    
    fun testAddTypeAnnotationToVectorVariable() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let <caret>v = vector[1, 2, 3];
                }
            }
        """.trimIndent())
        
        val intention = myFixture.findSingleIntention("Add type annotation")
        myFixture.launchAction(intention)
        
        myFixture.checkResult("""
            module 0x1::test {
                fun main() {
                    let v: vector<u64> = vector[1, 2, 3];
                }
            }
        """.trimIndent())
    }
    
    fun testNoIntentionForAlreadyAnnotated() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let <caret>x: u64 = 42;
                }
            }
        """.trimIndent())
        
        val intentions = myFixture.filterAvailableIntentions("Add type annotation")
        assertTrue("Should not show intention for already annotated variable", intentions.isEmpty())
    }
    
    // Convert to Public Intention Tests
    
    fun testConvertFunctionToPublic() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                <caret>fun helper(): u64 {
                    42
                }
            }
        """.trimIndent())
        
        val intention = myFixture.findSingleIntention("Make function public")
        assertNotNull("Should have intention to make function public", intention)
        
        myFixture.launchAction(intention)
        myFixture.checkResult("""
            module 0x1::test {
                public fun helper(): u64 {
                    42
                }
            }
        """.trimIndent())
    }
    
    fun testConvertStructToPublic() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                <caret>struct MyStruct {
                    value: u64
                }
            }
        """.trimIndent())
        
        val intention = myFixture.findSingleIntention("Make struct public")
        myFixture.launchAction(intention)
        
        myFixture.checkResult("""
            module 0x1::test {
                public struct MyStruct {
                    value: u64
                }
            }
        """.trimIndent())
    }
    
    fun testNoIntentionForAlreadyPublic() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                <caret>public fun helper(): u64 {
                    42
                }
            }
        """.trimIndent())
        
        val intentions = myFixture.filterAvailableIntentions("Make function public")
        assertTrue("Should not show intention for already public function", intentions.isEmpty())
    }
    
    fun testConvertToPublicWithFriend() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                <caret>friend fun helper(): u64 {
                    42
                }
            }
        """.trimIndent())
        
        val intention = myFixture.findSingleIntention("Make function public")
        myFixture.launchAction(intention)
        
        myFixture.checkResult("""
            module 0x1::test {
                public fun helper(): u64 {
                    42
                }
            }
        """.trimIndent())
    }
    
    fun testIntentionPreservesFormatting() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                // This is a helper function
                <caret>fun helper(
                    x: u64,
                    y: u64
                ): u64 {
                    x + y
                }
            }
        """.trimIndent())
        
        val intention = myFixture.findSingleIntention("Make function public")
        myFixture.launchAction(intention)
        
        myFixture.checkResult("""
            module 0x1::test {
                // This is a helper function
                public fun helper(
                    x: u64,
                    y: u64
                ): u64 {
                    x + y
                }
            }
        """.trimIndent())
    }
    
    fun testMultipleIntentionsAvailable() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let <caret>x = 42;
                }
            }
        """.trimIndent())
        
        val intentions = myFixture.availableIntentions
        assertTrue("Should have multiple intentions available", intentions.size > 1)
        assertTrue("Should have add type annotation intention", 
            intentions.any { it.text == "Add type annotation" })
    }
}
