package com.suimove.intellij.formatter

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.common.AbstractBlock
import com.suimove.intellij.psi.MoveTypes

class MoveBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    private val spacingBuilder: SpacingBuilder
) : AbstractBlock(node, wrap, alignment) {
    
    override fun buildChildren(): List<Block> {
        val blocks = mutableListOf<Block>()
        var child = myNode.firstChildNode
        
        while (child != null) {
            if (child.elementType != TokenType.WHITE_SPACE) {
                blocks.add(
                    MoveBlock(
                        child,
                        Wrap.createWrap(WrapType.NONE, false),
                        Alignment.createAlignment(),
                        spacingBuilder
                    )
                )
            }
            child = child.treeNext
        }
        
        return blocks
    }
    
    override fun getIndent(): Indent? {
        val elementType = myNode.elementType
        val parentType = myNode.treeParent?.elementType
        
        return when {
            parentType == MoveTypes.LBRACE && elementType != MoveTypes.RBRACE -> Indent.getNormalIndent()
            parentType == MoveTypes.LPAREN && elementType != MoveTypes.RPAREN -> Indent.getContinuationIndent()
            else -> Indent.getNoneIndent()
        }
    }
    
    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return spacingBuilder.getSpacing(this, child1, child2)
    }
    
    override fun isLeaf(): Boolean = myNode.firstChildNode == null
}
