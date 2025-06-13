package com.suimove.intellij

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase

class MoveFileTypeTest : BasePlatformTestCase() {
    fun testFileType() {
        val file = myFixture.configureByText("test.move", "module 0x1::test {}")
        TestCase.assertEquals(MoveFileType, file.fileType)
    }
    
    fun testFileExtension() {
        TestCase.assertEquals("move", MoveFileType.defaultExtension)
    }
    
    fun testLanguage() {
        TestCase.assertEquals(MoveLanguage, MoveFileType.language)
    }
}
