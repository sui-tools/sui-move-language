package com.suimove.intellij.inspections

import com.intellij.codeInspection.*
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.UserDataHolder
import com.intellij.psi.*
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.suimove.intellij.MoveLanguage
import com.suimove.intellij.psi.*
import com.suimove.intellij.quickfix.AddTxContextParameterQuickFix
import com.suimove.intellij.services.type.*
import javax.swing.Icon

/**
 * Inspection for Sui entry function requirements.
 */
class SuiEntryFunctionInspection : LocalInspectionTool() {
    
    override fun getDisplayName(): String = "Sui entry function requirements"
    
    override fun getStaticDescription(): String = """
        Checks that Sui entry functions follow the required patterns:
        - Entry functions should have TxContext as the last parameter
        - Entry function parameters must have allowed types
        - Entry functions cannot return values
        - Entry functions must be public
    """.trimIndent()
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is MoveFunction && element.isEntry) {
                    checkEntryFunction(element, holder)
                }
                super.visitElement(element)
            }
        }
    }
    
    private fun checkEntryFunction(function: MoveFunction, holder: ProblemsHolder) {
        // Check visibility
        if (!function.isPublic) {
            holder.registerProblem(
                function.nameIdentifier ?: function,
                "Entry functions must be public",
                ProblemHighlightType.ERROR
            )
        }
        
        // Check return type
        checkReturnType(function, holder)
        
        // Check parameters
        val parameters = function.parameters
        parameters.forEach { param ->
            checkParameterType(param, holder)
            if (param == parameters.first()) {
                checkFirstParameter(param, holder)
            }
        }
        
        // Check TxContext parameter
        checkTxContextParameter(parameters, function, holder)
    }
    
    private fun checkParameterType(param: MoveFunctionParameter, holder: ProblemsHolder) {
        val type = param.type ?: return
        
        if (!isAllowedParameterType(type)) {
            holder.registerProblem(
                (param as PsiElement),
                "Entry function parameter type must be a primitive type or object type",
                ProblemHighlightType.ERROR
            )
        }
    }
    
    private fun isAllowedParameterType(type: MoveTypeElement?): Boolean {
        if (type == null) return false
        // TODO: Implement proper type checking
        // Should check if type is primitive (u8, u64, bool, address, vector<u8>) or object type
        return true
    }
    
    private fun checkReturnType(function: MoveFunction, holder: ProblemsHolder) {
        val returnType = function.returnType
        if (returnType != null && !isAllowedReturnType(null)) {
            holder.registerProblem(
                function.returnTypeElement ?: (function as PsiElement),
                "Entry function cannot have return values",
                ProblemHighlightType.ERROR
            )
        }
    }
    
    private fun isAllowedReturnType(type: MoveType?): Boolean {
        // Entry functions should not have return values
        return type == null
    }
    
    private fun checkFirstParameter(param: MoveFunctionParameter, holder: ProblemsHolder) {
        val paramType = param.type ?: return
        
        if (!isTxContextType(paramType)) {
            holder.registerProblem(
                param as PsiElement,
                "First parameter of entry function must be &mut TxContext",
                ProblemHighlightType.ERROR
            )
        }
    }
    
    private fun isTxContextType(type: MoveTypeElement): Boolean {
        if (type == null) return false
        val typeText = type.text
        return typeText == "TxContext" || 
               typeText == "&TxContext" || 
               typeText == "&mut TxContext" ||
               typeText.endsWith("::TxContext") ||
               typeText.endsWith("::TxContext&") ||
               typeText.endsWith("::TxContext&mut")
    }
    
    private fun checkTxContextParameter(
        parameters: List<MoveFunctionParameter>,
        function: MoveFunction,
        holder: ProblemsHolder
    ) {
        val params = function.parameters
        val txContextParam = params.find { param ->
            val type = param.type
            type != null && isTxContextType(type)
        }
        
        if (txContextParam != null) {
            // Check if it's the last parameter
            if (params.lastOrNull() != txContextParam) {
                holder.registerProblem(
                    (txContextParam as PsiElement),
                    "TxContext must be the last parameter",
                    ProblemHighlightType.ERROR
                )
            }
            
            // Check if it's passed by mutable reference
            val type = txContextParam.type
            if (type != null && !isMutableReference(type)) {
                holder.registerProblem(
                    (txContextParam as PsiElement),
                    "TxContext must be passed as &mut TxContext",
                    ProblemHighlightType.ERROR
                )
            }
        } else {
            // Entry function should have TxContext
            holder.registerProblem(
                function.nameIdentifier ?: (function as PsiElement),
                "Entry function missing required TxContext parameter",
                ProblemHighlightType.ERROR,
                AddTxContextParameterQuickFix(function)
            )
        }
    }
    
    private fun isMutableReference(type: MoveTypeElement): Boolean {
        val typeText = type.text
        return typeText.contains("&mut")
    }
    
    private fun hasStoreAbility(type: MoveType): Boolean {
        // TODO: Implement proper ability checking
        return false
    }
    
    private fun createAddTxContextQuickFix(function: MoveFunction): LocalQuickFix {
        return object : LocalQuickFix {
            override fun getName() = "Add &mut TxContext parameter"
            override fun getFamilyName() = "Add parameter"
            
            override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
                val functionElement = descriptor.psiElement as? MoveFunction ?: return
                val factory = MoveElementFactory.getInstance(project)
                
                // Add TxContext parameter to function
                val newParam = factory.createParameter("ctx: &mut TxContext")
                val paramList = functionElement.parameterList
                
                if (paramList != null) {
                    if (paramList.parameters.isEmpty()) {
                        paramList.add(newParam)
                    } else {
                        paramList.addAfter(newParam, paramList.parameters.last())
                    }
                }
            }
        }
    }
    
    private fun createRemoveReturnTypeQuickFix(function: MoveFunction): LocalQuickFix {
        return object : LocalQuickFix {
            override fun getName() = "Remove return type"
            override fun getFamilyName() = "Remove return type"
            
            override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
                // TODO: Implement return type removal
            }
        }
    }
    
    private fun createFixParameterTypeQuickFix(param: MoveFunctionParameter): LocalQuickFix {
        return object : LocalQuickFix {
            override fun getName() = "Fix parameter type"
            override fun getFamilyName() = "Fix parameter"
            
            override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
                // TODO: Implement parameter type fix
            }
        }
    }
    
    companion object {
        private val ALLOWED_PRIMITIVE_TYPES = setOf(
            "u8", "u16", "u32", "u64", "u128", "u256",
            "bool", "address", "vector<u8>"
        )
    }
}

/**
 * Implementation of MoveParameterList for inspection purposes.
 */
class MoveParameterListImpl(private val parameterList: List<MoveFunctionParameter>) : MoveParameterList {
    override val parameters: List<MoveFunctionParameter>
        get() = parameterList
    
    override fun getProject(): Project = throw UnsupportedOperationException()
    override fun getLanguage(): Language = MoveLanguage
    override fun getManager(): PsiManager = throw UnsupportedOperationException()
    override fun getChildren(): Array<PsiElement> = PsiElement.EMPTY_ARRAY
    override fun getParent(): PsiElement? = null
    override fun getFirstChild(): PsiElement? = null
    override fun getLastChild(): PsiElement? = null
    override fun getNextSibling(): PsiElement? = null
    override fun getPrevSibling(): PsiElement? = null
    override fun getContainingFile(): PsiFile? = null
    override fun getTextRange(): TextRange? = null
    override fun getStartOffsetInParent(): Int = 0
    override fun getTextLength(): Int = 0
    override fun findElementAt(offset: Int): PsiElement? = null
    override fun findReferenceAt(offset: Int): PsiReference? = null
    override fun getTextOffset(): Int = 0
    override fun getText(): String = ""
    override fun textToCharArray(): CharArray = CharArray(0)
    override fun getNavigationElement(): PsiElement = this
    override fun getOriginalElement(): PsiElement = this
    override fun textMatches(text: CharSequence): Boolean = false
    override fun textMatches(element: PsiElement): Boolean = false
    override fun textContains(c: Char): Boolean = false
    override fun accept(visitor: PsiElementVisitor) {}
    override fun acceptChildren(visitor: PsiElementVisitor) {}
    override fun copy(): PsiElement = this
    override fun add(element: PsiElement): PsiElement? = null
    override fun addBefore(element: PsiElement, anchor: PsiElement?): PsiElement? = null
    override fun addAfter(element: PsiElement, anchor: PsiElement?): PsiElement? = null
    override fun checkAdd(element: PsiElement) {}
    override fun addRange(first: PsiElement?, last: PsiElement?): PsiElement? = null
    override fun addRangeBefore(first: PsiElement, last: PsiElement, anchor: PsiElement?): PsiElement? = null
    override fun addRangeAfter(first: PsiElement, last: PsiElement, anchor: PsiElement?): PsiElement? = null
    override fun delete() {}
    override fun checkDelete() {}
    override fun deleteChildRange(first: PsiElement?, last: PsiElement?) {}
    override fun replace(newElement: PsiElement): PsiElement? = null
    override fun isValid(): Boolean = true
    override fun isWritable(): Boolean = false
    override fun getReference(): PsiReference? = null
    override fun getReferences(): Array<PsiReference> = PsiReference.EMPTY_ARRAY
    override fun <T> getUserData(key: Key<T>): T? = null
    override fun <T> putUserData(key: Key<T>, value: T?) {}
    override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean = true
    override fun getContext(): PsiElement? = null
    override fun isPhysical(): Boolean = false
    override fun getResolveScope(): GlobalSearchScope = GlobalSearchScope.EMPTY_SCOPE
    override fun getUseScope(): GlobalSearchScope = GlobalSearchScope.EMPTY_SCOPE
    override fun getNode(): ASTNode? = null
    override fun isEquivalentTo(another: PsiElement?): Boolean = this == another
    override fun getIcon(flags: Int): Icon? = null
    override fun <T> getCopyableUserData(key: Key<T>): T? = null
    override fun <T> putCopyableUserData(key: Key<T>, value: T?) {}
}

// Extension properties for PSI elements
val MoveFunction.isEntry: Boolean
    get() = attributeList?.attributes?.any { it.name == "entry" } == true

val MoveFunction.isPublic: Boolean
    get() = this.text.contains("public")

val MoveFunction.hasReturnType: Boolean
    get() = this.returnType != null

val MoveFunction.returnTypeElement: PsiElement?
    get() = null // TODO: Implement proper return type element access

val MoveFunction.parameterList: MoveParameterList?
    get() = object : MoveParameterList {
        override val parameters: List<MoveFunctionParameter> = this@parameterList.parameters
        
        override fun getProject() = this@parameterList.project
        override fun getLanguage() = MoveLanguage
        override fun getManager() = PsiManager.getInstance(this@parameterList.project)
        override fun getChildren() = PsiElement.EMPTY_ARRAY
        override fun getParent() = this@parameterList
        override fun getFirstChild(): PsiElement? = null
        override fun getLastChild(): PsiElement? = null
        override fun getNextSibling(): PsiElement? = null
        override fun getPrevSibling(): PsiElement? = null
        override fun getContainingFile() = this@parameterList.containingFile
        override fun getTextRange() = null
        override fun getStartOffsetInParent() = 0
        override fun getTextLength() = 0
        override fun findElementAt(offset: Int): PsiElement? = null
        override fun findReferenceAt(offset: Int): PsiReference? = null
        override fun getTextOffset() = 0
        override fun getText() = ""
        override fun textToCharArray() = CharArray(0)
        override fun getNavigationElement() = this
        override fun getOriginalElement() = this
        override fun textMatches(text: CharSequence): Boolean = false
        override fun textMatches(element: PsiElement): Boolean = false
        override fun textContains(c: Char): Boolean = false
        override fun accept(visitor: PsiElementVisitor) {}
        override fun acceptChildren(visitor: PsiElementVisitor) {}
        override fun copy() = this
        override fun add(element: PsiElement): PsiElement? = null
        override fun addBefore(element: PsiElement, anchor: PsiElement?): PsiElement? = null
        override fun addAfter(element: PsiElement, anchor: PsiElement?): PsiElement? = null
        override fun checkAdd(element: PsiElement) {}
        override fun addRange(first: PsiElement?, last: PsiElement?): PsiElement? = null
        override fun addRangeBefore(first: PsiElement, last: PsiElement, anchor: PsiElement?): PsiElement? = null
        override fun addRangeAfter(first: PsiElement, last: PsiElement, anchor: PsiElement?): PsiElement? = null
        override fun delete() {}
        override fun checkDelete() {}
        override fun deleteChildRange(first: PsiElement?, last: PsiElement?) {}
        override fun replace(newElement: PsiElement): PsiElement? = null
        override fun isValid() = true
        override fun isWritable() = false
        override fun getReference(): PsiReference? = null
        override fun getReferences() = PsiReference.EMPTY_ARRAY
        override fun <T> getUserData(key: Key<T>): T? = null
        override fun <T> putUserData(key: Key<T>, value: T?) {}
        override fun getIcon(flags: Int): Icon? = null
        override fun <T> getCopyableUserData(key: Key<T>): T? = null
        override fun <T> putCopyableUserData(key: Key<T>, value: T?) {}
        override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean = true
        override fun getContext(): PsiElement? = null
        override fun isPhysical(): Boolean = false
        override fun getResolveScope(): GlobalSearchScope = GlobalSearchScope.EMPTY_SCOPE
        override fun getUseScope(): GlobalSearchScope = GlobalSearchScope.EMPTY_SCOPE
        override fun getNode(): ASTNode? = null
        override fun isEquivalentTo(another: PsiElement?): Boolean = this == another
    }
