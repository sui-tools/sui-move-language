package com.suimove.intellij.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.suimove.intellij.psi.MoveTypes;
import com.intellij.psi.TokenType;

%%

%class _MoveLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

CRLF=\R
WHITE_SPACE=[\ \n\t\f]
LINE_COMMENT="//"[^\r\n]*
BLOCK_COMMENT="/*" ~"*/"

IDENTIFIER=[a-zA-Z_][a-zA-Z0-9_]*
HEX_NUMBER=0x[0-9a-fA-F]+
DEC_NUMBER=[0-9]+
STRING_LITERAL=\"([^\\\"]|\\.)*\"
BYTE_STRING_LITERAL=b\"([^\\\"]|\\.)*\"
ADDRESS_LITERAL=@[a-zA-Z_][a-zA-Z0-9_]*|@0x[0-9a-fA-F]+

%%

<YYINITIAL> {
  {WHITE_SPACE}           { return TokenType.WHITE_SPACE; }
  {LINE_COMMENT}          { return MoveTypes.LINE_COMMENT; }
  {BLOCK_COMMENT}         { return MoveTypes.BLOCK_COMMENT; }

  // Keywords
  "module"                { return MoveTypes.MODULE; }
  "script"                { return MoveTypes.SCRIPT; }
  "fun"                   { return MoveTypes.FUN; }
  "public"                { return MoveTypes.PUBLIC; }
  "entry"                 { return MoveTypes.ENTRY; }
  "native"                { return MoveTypes.NATIVE; }
  "struct"                { return MoveTypes.STRUCT; }
  "has"                   { return MoveTypes.HAS; }
  "copy"                  { return MoveTypes.COPY; }
  "drop"                  { return MoveTypes.DROP; }
  "store"                 { return MoveTypes.STORE; }
  "key"                   { return MoveTypes.KEY; }
  "move"                  { return MoveTypes.MOVE_KEYWORD; }
  "const"                 { return MoveTypes.CONST; }
  "let"                   { return MoveTypes.LET; }
  "mut"                   { return MoveTypes.MUT; }
  "return"                { return MoveTypes.RETURN; }
  "abort"                 { return MoveTypes.ABORT; }
  "break"                 { return MoveTypes.BREAK; }
  "continue"              { return MoveTypes.CONTINUE; }
  "if"                    { return MoveTypes.IF; }
  "else"                  { return MoveTypes.ELSE; }
  "while"                 { return MoveTypes.WHILE; }
  "loop"                  { return MoveTypes.LOOP; }
  "spec"                  { return MoveTypes.SPEC; }
  "pragma"                { return MoveTypes.PRAGMA; }
  "invariant"             { return MoveTypes.INVARIANT; }
  "assume"                { return MoveTypes.ASSUME; }
  "assert"                { return MoveTypes.ASSERT; }
  "requires"              { return MoveTypes.REQUIRES; }
  "ensures"               { return MoveTypes.ENSURES; }
  "use"                   { return MoveTypes.USE; }
  "friend"                { return MoveTypes.FRIEND; }
  "acquires"              { return MoveTypes.ACQUIRES; }
  "as"                    { return MoveTypes.AS; }
  "true"                  { return MoveTypes.TRUE; }
  "false"                 { return MoveTypes.FALSE; }

  // Types
  "u8"                    { return MoveTypes.U8; }
  "u16"                   { return MoveTypes.U16; }
  "u32"                   { return MoveTypes.U32; }
  "u64"                   { return MoveTypes.U64; }
  "u128"                  { return MoveTypes.U128; }
  "u256"                  { return MoveTypes.U256; }
  "bool"                  { return MoveTypes.BOOL; }
  "address"               { return MoveTypes.ADDRESS; }
  "signer"                { return MoveTypes.SIGNER; }
  "vector"                { return MoveTypes.VECTOR; }

  // Operators and Punctuation
  "("                     { return MoveTypes.LPAREN; }
  ")"                     { return MoveTypes.RPAREN; }
  "["                     { return MoveTypes.LBRACK; }
  "]"                     { return MoveTypes.RBRACK; }
  "{"                     { return MoveTypes.LBRACE; }
  "}"                     { return MoveTypes.RBRACE; }
  "<"                     { return MoveTypes.LT; }
  ">"                     { return MoveTypes.GT; }
  "<="                    { return MoveTypes.LE; }
  ">="                    { return MoveTypes.GE; }
  "=="                    { return MoveTypes.EQ; }
  "!="                    { return MoveTypes.NE; }
  "="                     { return MoveTypes.ASSIGN; }
  "+"                     { return MoveTypes.PLUS; }
  "-"                     { return MoveTypes.MINUS; }
  "*"                     { return MoveTypes.MUL; }
  "/"                     { return MoveTypes.DIV; }
  "%"                     { return MoveTypes.MOD; }
  "&"                     { return MoveTypes.AND; }
  "|"                     { return MoveTypes.OR; }
  "^"                     { return MoveTypes.XOR; }
  "<<"                    { return MoveTypes.SHL; }
  ">>"                    { return MoveTypes.SHR; }
  "&&"                    { return MoveTypes.AND_AND; }
  "||"                    { return MoveTypes.OR_OR; }
  "!"                     { return MoveTypes.NOT; }
  ":"                     { return MoveTypes.COLON; }
  "::"                    { return MoveTypes.COLON_COLON; }
  ";"                     { return MoveTypes.SEMICOLON; }
  ","                     { return MoveTypes.COMMA; }
  "."                     { return MoveTypes.DOT; }
  "&mut"                  { return MoveTypes.AMP_MUT; }

  // Literals
  {ADDRESS_LITERAL}       { return MoveTypes.ADDRESS_LITERAL; }
  {HEX_NUMBER}            { return MoveTypes.HEX_NUMBER; }
  {DEC_NUMBER}            { return MoveTypes.DEC_NUMBER; }
  {STRING_LITERAL}        { return MoveTypes.STRING_LITERAL; }
  {BYTE_STRING_LITERAL}   { return MoveTypes.BYTE_STRING_LITERAL; }
  {IDENTIFIER}            { return MoveTypes.IDENTIFIER; }

  .                       { return TokenType.BAD_CHARACTER; }
}
