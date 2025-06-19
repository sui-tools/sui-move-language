package com.suimove.intellij.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReference
import com.suimove.intellij.services.type.MoveAbility
import com.suimove.intellij.services.type.MoveType
import com.suimove.intellij.services.type.MoveTypeParameter

/**
 * Base interface for all Move expressions.
 */
interface MoveExpression : PsiElement

/**
 * Literal expression (numbers, strings, booleans, addresses).
 */
interface MoveLiteralExpression : MoveExpression

/**
 * Reference expression (variable, function, or type reference).
 */
interface MoveReferenceExpression : MoveExpression {
    override fun getReference(): PsiReference?
}

/**
 * Function call expression.
 */
interface MoveFunctionCall : MoveExpression {
    val functionName: String?
    val arguments: List<MoveExpression>
}

/**
 * Call expression (alias for backwards compatibility).
 */
interface MoveCallExpression : MoveFunctionCall

/**
 * Binary expression (e.g., a + b, x && y).
 */
interface MoveBinaryExpression : MoveExpression {
    val left: MoveExpression
    val right: MoveExpression
    val operator: String
    val operatorToken: PsiElement?
}

/**
 * Unary expression (e.g., !x, -y, &z).
 */
interface MoveUnaryExpression : MoveExpression {
    val operand: MoveExpression
    val operator: String
}

/**
 * Struct literal expression.
 */
interface MoveStructLiteral : MoveExpression {
    val structType: MoveType?
    val fields: List<MoveStructLiteralField>
}

/**
 * Struct literal expression (alias for backwards compatibility).
 */
interface MoveStructLiteralExpression : MoveStructLiteral

/**
 * Struct literal field.
 */
interface MoveStructLiteralField : PsiElement {
    val name: String?
    val value: MoveExpression?
}

/**
 * Vector expression.
 */
interface MoveVectorExpression : MoveExpression {
    val elements: List<MoveExpression>
    val typeAnnotation: MoveTypeElement?
}

/**
 * If expression.
 */
interface MoveIfExpression : MoveExpression {
    val condition: MoveExpression
    val thenBranch: MoveExpression
    val elseBranch: MoveExpression?
}

/**
 * Block expression.
 */
interface MoveBlockExpression : MoveExpression {
    val statements: List<MoveStatement>
}

/**
 * Path expression (e.g., module::function).
 */
interface MovePathExpression : MoveExpression {
    override fun getReference(): PsiReference?
}

/**
 * Base interface for all Move statements.
 */
interface MoveStatement : PsiElement

/**
 * Expression statement.
 */
interface MoveExpressionStatement : MoveStatement {
    val expression: MoveExpression
    val hasSemicolon: Boolean
}

/**
 * Let statement.
 */
interface MoveLetStatement : MoveStatement {
    val pattern: MovePattern
    val typeAnnotation: MoveTypeElement?
    val initializationExpression: MoveExpression?
}

/**
 * Assignment statement.
 */
interface MoveAssignmentStatement : MoveStatement {
    val lhs: MoveExpression
    val rhs: MoveExpression
}

/**
 * Use statement (import).
 */
interface MoveUseStatement : PsiElement {
    val modulePath: String?
    val alias: String?
}

/**
 * Base interface for patterns.
 */
interface MovePattern : PsiElement

/**
 * Binding pattern (variable binding).
 */
interface MoveBindingPattern : MovePattern {
    val nameIdentifier: PsiElement?
}

/**
 * Base interface for type elements.
 */
interface MoveTypeElement : PsiElement {
    val type: MoveType?
}

/**
 * Built-in type element (u8, bool, address, etc.).
 */
interface MoveBuiltinTypeElement : MoveTypeElement

/**
 * Named type element (struct or type alias reference).
 */
interface MoveNamedTypeElement : MoveTypeElement {
    override fun getReference(): PsiReference?
}

/**
 * Generic type element (e.g., vector<T>).
 */
interface MoveGenericTypeElement : MoveTypeElement {
    val baseName: String?
    val typeArguments: List<MoveTypeElement>
}

/**
 * Reference type element (&T or &mut T).
 */
interface MoveReferenceTypeElement : MoveTypeElement {
    val innerType: MoveTypeElement
    val isMutable: Boolean
}

/**
 * Tuple type element.
 */
interface MoveTupleTypeElement : MoveTypeElement {
    val types: List<MoveTypeElement>
}

/**
 * Function declaration.
 */
interface MoveFunction : MoveNamedElement {
    val parameters: List<MoveFunctionParameter>
    val returnType: MoveType?
    val typeParameters: List<MoveTypeParameter>
    val body: MoveCodeBlock?
    val visibility: MoveVisibility
    val isEntry: Boolean
    val isPublic: Boolean
    val isNative: Boolean
    val isInline: Boolean
}

/**
 * Function parameter.
 */
interface MoveFunctionParameter : MoveNamedElement {
    val type: MoveTypeElement?
}

/**
 * Function signature.
 */
interface MoveFunctionSignature : PsiElement {
    val parameters: List<MoveFunctionParameter>
}

/**
 * Struct declaration.
 */
interface MoveStruct : MoveNamedElement {
    val fields: List<MoveStructField>
    val abilities: Set<MoveAbility>
    val typeParameters: List<MoveTypeParameter>
    val qualifiedName: String?
}

/**
 * Struct field.
 */
interface MoveStructField : MoveNamedElement {
    val type: MoveTypeElement?
}

/**
 * Constant declaration.
 */
interface MoveConstant : MoveNamedElement {
    val type: MoveType?
    val value: MoveExpression?
}

/**
 * Type parameter.
 */
interface MoveTypeParameter : MoveNamedElement {
    val abilities: Set<MoveAbility>
}

/**
 * Variable (local binding).
 */
interface MoveVariable : MoveNamedElement {
    val type: MoveType?
    val initializationExpression: MoveExpression?
}

/**
 * Module declaration.
 */
interface MoveModule : MoveNamedElement {
    val address: String?
    val functions: List<MoveFunction>
    val structs: List<MoveStruct>
    val constants: List<MoveConstant>
    val uses: List<MoveUseStatement>
}

/**
 * Address block.
 */
interface MoveAddressBlock : PsiElement {
    val address: String?
    val modules: List<MoveModule>
}

/**
 * Code block.
 */
interface MoveCodeBlock : PsiElement {
    val statements: List<MoveStatement>
}

/**
 * Visibility modifier.
 */
enum class MoveVisibility {
    PRIVATE,
    PUBLIC,
    PUBLIC_FRIEND
}
