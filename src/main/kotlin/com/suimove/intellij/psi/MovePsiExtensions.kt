package com.suimove.intellij.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.suimove.intellij.services.type.MoveType

// Typealias for compatibility
typealias MoveTypeParameterDecl = MoveTypeParameter

/**
 * Extension properties for Move PSI elements to ensure name and nameIdentifier are accessible.
 */

val MoveFunction.name: String?
    get() = when (this) {
        is MoveNamedElement -> getName()
        else -> nameIdentifier?.text
    }

val MoveFunction.nameIdentifier: PsiElement?
    get() = when (this) {
        is MoveNamedElement -> getNameIdentifier()
        else -> PsiTreeUtil.findChildOfType(this, PsiElement::class.java)?.findChildByType(com.suimove.intellij.psi.MoveTypes.IDENTIFIER)
    }

val MoveFunction.body: MoveCodeBlock?
    get() = PsiTreeUtil.findChildOfType(this, MoveCodeBlock::class.java)

val MoveFunction.returnTypeElement: MoveTypeElement?
    get() = PsiTreeUtil.findChildOfType(this, MoveTypeElement::class.java)

val MoveFunction.visibility: MoveVisibility
    get() = when {
        text.contains("public(friend)") -> MoveVisibility.PUBLIC_FRIEND
        text.contains("public") -> MoveVisibility.PUBLIC
        else -> MoveVisibility.PRIVATE
    }

val MoveFunction.attributeList: MoveAttributeList?
    get() = PsiTreeUtil.getChildOfType(this, MoveAttributeList::class.java)

val MoveFunction.parameterList: MoveParameterList?
    get() = PsiTreeUtil.getChildOfType(this, MoveParameterList::class.java)

val MoveFunction.parameters: List<MoveFunctionParameter>
    get() = parameterList?.parameters ?: emptyList()

val MoveModule.name: String?
    get() = when (this) {
        is MoveNamedElement -> getName()
        else -> nameIdentifier?.text
    }

val MoveModule.nameIdentifier: PsiElement?
    get() = when (this) {
        is MoveNamedElement -> getNameIdentifier()
        else -> PsiTreeUtil.findChildOfType(this, PsiElement::class.java)?.findChildByType(com.suimove.intellij.psi.MoveTypes.IDENTIFIER)
    }

val MoveStruct.name: String?
    get() = when (this) {
        is MoveNamedElement -> getName()
        else -> nameIdentifier?.text
    }

val MoveStruct.nameIdentifier: PsiElement?
    get() = when (this) {
        is MoveNamedElement -> getNameIdentifier()
        else -> PsiTreeUtil.findChildOfType(this, PsiElement::class.java)?.findChildByType(com.suimove.intellij.psi.MoveTypes.IDENTIFIER)
    }

val MoveStruct.fields: List<MoveStructField>
    get() = PsiTreeUtil.getChildrenOfTypeAsList(this, MoveStructField::class.java)

val MoveStructField.name: String?
    get() = PsiTreeUtil.findChildOfType(this, PsiElement::class.java)?.findChildByType(com.suimove.intellij.psi.MoveTypes.IDENTIFIER)?.text

val MoveStructField.nameIdentifier: PsiElement?
    get() = when (this) {
        is MoveNamedElement -> getNameIdentifier()
        else -> PsiTreeUtil.findChildOfType(this, PsiElement::class.java)?.findChildByType(com.suimove.intellij.psi.MoveTypes.IDENTIFIER)
    }

val MoveAttribute.name: String?
    get() = when (this) {
        is MoveNamedElement -> getName()
        else -> nameIdentifier?.text
    }

val MoveAttribute.nameIdentifier: PsiElement?
    get() = when (this) {
        is MoveNamedElement -> getNameIdentifier()
        else -> PsiTreeUtil.findChildOfType(this, PsiElement::class.java)?.findChildByType(com.suimove.intellij.psi.MoveTypes.IDENTIFIER)
    }

/**
 * Extension property for MoveTypeParameterDecl name.
 */
val MoveTypeParameterDecl.name: String?
    get() = when (this) {
        is MoveTypeParameter -> (this as? MoveNamedElement)?.name
        else -> null
    }

/**
 * Extension property for MoveTypeParameterDecl nameIdentifier.
 */
val MoveTypeParameterDecl.nameIdentifier: PsiElement?
    get() = when (this) {
        is MoveTypeParameter -> (this as? MoveNamedElement)?.nameIdentifier
        else -> null
    }

/**
 * Extension property for MoveConstant name.
 */
val MoveConstant.name: String?
    get() = when (this) {
        is MoveNamedElement -> getName()
        else -> PsiTreeUtil.findChildOfType(this, PsiElement::class.java)?.findChildByType(com.suimove.intellij.psi.MoveTypes.IDENTIFIER)?.text
    }

/**
 * Extension property for MoveConstant nameIdentifier.
 */
val MoveConstant.nameIdentifier: PsiElement?
    get() = when (this) {
        is MoveNamedElement -> getNameIdentifier()
        else -> PsiTreeUtil.findChildOfType(this, PsiElement::class.java)?.findChildByType(com.suimove.intellij.psi.MoveTypes.IDENTIFIER)
    }

/**
 * Extension property for MoveVariable name.
 */
val MoveVariable.name: String?
    get() = when (this) {
        is MoveNamedElement -> getName()
        else -> PsiTreeUtil.findChildOfType(this, PsiElement::class.java)?.findChildByType(com.suimove.intellij.psi.MoveTypes.IDENTIFIER)?.text
    }

/**
 * Extension property for MoveVariable nameIdentifier.
 */
val MoveVariable.nameIdentifier: PsiElement?
    get() = when (this) {
        is MoveNamedElement -> getNameIdentifier()
        else -> PsiTreeUtil.findChildOfType(this, PsiElement::class.java)?.findChildByType(com.suimove.intellij.psi.MoveTypes.IDENTIFIER)
    }

/**
 * Extension property for MoveFile modules.
 */
val MoveFile.modules: List<MoveModule>
    get() = PsiTreeUtil.getChildrenOfTypeAsList(this, MoveModule::class.java)

/**
 * Extension property for MoveReferenceExpression referenceName.
 */
val MoveReferenceExpression.referenceName: String?
    get() = PsiTreeUtil.findChildOfType(this, PsiElement::class.java)?.findChildByType(com.suimove.intellij.psi.MoveTypes.IDENTIFIER)?.text

/**
 * Extension property for MoveFunctionParameter type.
 */
val MoveFunctionParameter.type: MoveTypeElement?
    get() = PsiTreeUtil.getChildOfType(this, MoveTypeElement::class.java)

/**
 * Extension property for MoveFunctionParameter name.
 */
val MoveFunctionParameter.name: String?
    get() = when (this) {
        is MoveNamedElement -> getName()
        else -> PsiTreeUtil.findChildOfType(this, PsiElement::class.java)?.findChildByType(com.suimove.intellij.psi.MoveTypes.IDENTIFIER)?.text
    }

/**
 * Extension property for MoveFunctionParameter type.
 */
val com.suimove.intellij.refactoring.MoveFunctionParameter.type: MoveTypeElement?
    get() = PsiTreeUtil.findChildOfType(this, PsiElement::class.java)?.findChildByType(com.suimove.intellij.psi.MoveTypes.TYPE_ANNOTATION)?.let { it as? MoveTypeElement }

/**
 * Extension property for MoveFunctionParameter name.
 */
val com.suimove.intellij.refactoring.MoveFunctionParameter.name: String?
    get() = when (this) {
        is MoveNamedElement -> getName()
        else -> PsiTreeUtil.findChildOfType(this, PsiElement::class.java)?.findChildByType(com.suimove.intellij.psi.MoveTypes.IDENTIFIER)?.text
    }

// Helper function to find child by type
private fun PsiElement.findChildByType(type: com.intellij.psi.tree.IElementType): PsiElement? {
    return node.findChildByType(type)?.psi
}
