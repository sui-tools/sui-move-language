package com.suimove.intellij.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.psi.PsiReferenceService

class MoveReferenceDebugTest : BasePlatformTestCase() {
    
    fun testDebugReference() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun helper(): u64 { 42 }
                
                fun test_ref() {
                    let x = <caret>helper();
                }
            }
        """.trimIndent())
        
        val elementAtCaret = myFixture.file.findElementAt(myFixture.caretOffset)
        println("Element at caret: ${elementAtCaret?.text} (${elementAtCaret?.node?.elementType})")
        println("Element class: ${elementAtCaret?.javaClass?.simpleName}")
        println("Parent: ${elementAtCaret?.parent?.text} (${elementAtCaret?.parent?.node?.elementType})")
        println("Reference on element: ${elementAtCaret?.reference}")
        println("Reference on parent: ${elementAtCaret?.parent?.reference}")
        
        // Try to get references
        val refs = elementAtCaret?.references ?: emptyArray()
        println("References on element: ${refs.size}")
        
        val parentRefs = elementAtCaret?.parent?.references ?: emptyArray()
        println("References on parent: ${parentRefs.size}")
        
        // Try using PsiReferenceService
        if (elementAtCaret != null) {
            val refService = PsiReferenceService.getService()
            val serviceRefs = refService.getReferences(elementAtCaret, PsiReferenceService.Hints.NO_HINTS)
            println("References from service: ${serviceRefs.size}")
            serviceRefs.forEach { ref ->
                println("  Reference: ${ref.javaClass.simpleName} -> ${ref.canonicalText}")
            }
        }
        
        // Check if the element type is correct
        println("Element type check: ${elementAtCaret?.node?.elementType == MoveTypes.IDENTIFIER}")
        
        // Check previous sibling
        var prev = elementAtCaret?.prevSibling
        while (prev != null && prev.textLength == 0) {
            prev = prev.prevSibling
        }
        println("Previous sibling: ${prev?.text} (${prev?.node?.elementType})")
        
        // Try using file.findReferenceAt
        val file = myFixture.file
        if (file is MoveFile) {
            val refAtOffset = file.findReferenceAt(myFixture.caretOffset)
            println("Reference from file.findReferenceAt: $refAtOffset")
            if (refAtOffset != null) {
                println("Reference element: ${refAtOffset.element.text}")
                println("Reference range: ${refAtOffset.rangeInElement}")
            }
        }
    }
}
