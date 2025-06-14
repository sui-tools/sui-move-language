package com.suimove.intellij.bracematcher

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import com.suimove.intellij.psi.MoveTypes
import com.intellij.lang.BracePair
import com.suimove.intellij.MoveBraceMatcher

/**
 * Tests for Move brace matcher
 */
class MoveBraceMatcherTest {
    
    @Test
    fun `test brace matcher can be created`() {
        val matcher = MoveBraceMatcher()
        assertNotNull(matcher, "Brace matcher should be instantiated")
    }
    
    @Test
    fun `test brace pairs are defined`() {
        val matcher = MoveBraceMatcher()
        val pairs = matcher.getPairs()
        assertNotNull(pairs, "Brace pairs should be defined")
        assertTrue(pairs.isNotEmpty(), "Should have at least one brace pair")
    }
    
    @Test
    fun `test parentheses pair`() {
        val matcher = MoveBraceMatcher()
        val pairs = matcher.getPairs()
        
        val parenPair = pairs.find { it.leftBraceType == MoveTypes.LPAREN }
        assertNotNull(parenPair, "Should have parentheses pair")
        assertEquals(MoveTypes.RPAREN, parenPair?.rightBraceType, "Right paren should match left paren")
    }
    
    @Test
    fun `test braces pair`() {
        val matcher = MoveBraceMatcher()
        val pairs = matcher.getPairs()
        
        val bracePair = pairs.find { it.leftBraceType == MoveTypes.LBRACE }
        assertNotNull(bracePair, "Should have braces pair")
        assertEquals(MoveTypes.RBRACE, bracePair?.rightBraceType, "Right brace should match left brace")
    }
    
    @Test
    fun `test brackets pair`() {
        val matcher = MoveBraceMatcher()
        val pairs = matcher.getPairs()
        
        val bracketPair = pairs.find { it.leftBraceType == MoveTypes.LBRACK }
        assertNotNull(bracketPair, "Should have brackets pair")
        assertEquals(MoveTypes.RBRACK, bracketPair?.rightBraceType, "Right bracket should match left bracket")
    }
    
    @Test
    fun `test angle brackets pair`() {
        val matcher = MoveBraceMatcher()
        val pairs = matcher.getPairs()
        
        val anglePair = pairs.find { it.leftBraceType == MoveTypes.LT }
        assertNotNull(anglePair, "Should have angle brackets pair")
        assertEquals(MoveTypes.GT, anglePair?.rightBraceType, "Right angle should match left angle")
    }
    
    @Test
    fun `test paired brace tokens are not structural`() {
        val matcher = MoveBraceMatcher()
        assertTrue(matcher.isPairedBracesAllowedBeforeType(MoveTypes.IDENTIFIER, null),
                   "Paired braces should be allowed before identifiers")
    }
    
    @Test
    fun `test code construct start`() {
        val matcher = MoveBraceMatcher()
        val codeConstructStart = matcher.getCodeConstructStart(null, 100)
        assertEquals(100, codeConstructStart, "Should return the opening brace offset")
    }
}
