package com.suimove.intellij.commenter

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.suimove.intellij.MoveCommenter

/**
 * Tests for Move commenter
 */
class MoveCommenterTest : BasePlatformTestCase() {
    
    fun testCommenterCanBeCreated() {
        val commenter = MoveCommenter()
        assertNotNull(commenter)
    }
    
    fun testLineCommentPrefix() {
        val commenter = MoveCommenter()
        assertEquals("//", commenter.lineCommentPrefix)
    }
    
    fun testBlockCommentStart() {
        val commenter = MoveCommenter()
        assertEquals("/*", commenter.blockCommentPrefix)
    }
    
    fun testBlockCommentEnd() {
        val commenter = MoveCommenter()
        assertEquals("*/", commenter.blockCommentSuffix)
    }
    
    fun testCommentedBlockCommentPrefix() {
        val commenter = MoveCommenter()
        assertNull(commenter.commentedBlockCommentPrefix)
    }
    
    fun testCommentedBlockCommentSuffix() {
        val commenter = MoveCommenter()
        assertNull(commenter.commentedBlockCommentSuffix)
    }
}
