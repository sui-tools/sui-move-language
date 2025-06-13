package com.suimove.intellij.highlighting

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.suimove.intellij.MoveIcons
import javax.swing.Icon

class MoveColorSettingsPage : ColorSettingsPage {
    override fun getIcon(): Icon = MoveIcons.FILE

    override fun getHighlighter(): SyntaxHighlighter = MoveSyntaxHighlighter()

    override fun getDemoText(): String = """
        // This is a line comment
        /* This is a block comment */
        
        module 0x1::example {
            use std::vector;
            use sui::coin::{Self, Coin};
            
            const MAX_SUPPLY: u64 = 1000000;
            
            struct MyToken has key, store {
                id: UID,
                balance: u64,
                name: vector<u8>,
            }
            
            public entry fun mint(
                amount: u64,
                recipient: address,
                ctx: &mut TxContext
            ) {
                assert!(amount <= MAX_SUPPLY, 0);
                let token = MyToken {
                    id: object::new(ctx),
                    balance: amount,
                    name: b"My Token",
                };
                transfer::transfer(token, recipient);
            }
            
            public fun get_balance(token: &MyToken): u64 {
                token.balance
            }
            
            #[test]
            fun test_mint() {
                let ctx = test_scenario::ctx(@0x1);
                mint(100, @0x2, &mut ctx);
            }
        }
        
        script {
            use 0x1::example;
            
            fun main(account: signer) {
                let addr = signer::address_of(&account);
                example::mint(50, addr);
            }
        }
    """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = arrayOf(
        AttributesDescriptor("Keyword", MoveSyntaxHighlighter.KEYWORD),
        AttributesDescriptor("Type", MoveSyntaxHighlighter.TYPE),
        AttributesDescriptor("Number", MoveSyntaxHighlighter.NUMBER),
        AttributesDescriptor("String", MoveSyntaxHighlighter.STRING),
        AttributesDescriptor("Comment", MoveSyntaxHighlighter.COMMENT),
        AttributesDescriptor("Identifier", MoveSyntaxHighlighter.IDENTIFIER),
        AttributesDescriptor("Bad character", MoveSyntaxHighlighter.BAD_CHARACTER)
    )

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "Move"
}
