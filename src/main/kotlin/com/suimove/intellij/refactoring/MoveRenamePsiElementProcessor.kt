package com.suimove.intellij.refactoring

import com.intellij.psi.PsiElement
import com.intellij.psi.search.SearchScope
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.usageView.UsageInfo
import com.intellij.util.IncorrectOperationException
import com.suimove.intellij.psi.*

/**
 * Processor for renaming Move PSI elements.
 */
class MoveRenamePsiElementProcessor : RenamePsiElementProcessor() {
    
    override fun canProcessElement(element: PsiElement): Boolean {
        return element is MoveNamedElement
    }
    
    override fun prepareRenaming(
        element: PsiElement,
        newName: String,
        allRenames: MutableMap<PsiElement, String>,
        scope: SearchScope
    ) {
        // Handle special cases for renaming
        when (element) {
            is MoveStruct -> prepareStructRenaming(element, newName, allRenames)
            is MoveModule -> prepareModuleRenaming(element, newName, allRenames)
            is MoveFunction -> prepareFunctionRenaming(element, newName, allRenames)
        }
    }
    
    override fun renameElement(
        element: PsiElement,
        newName: String,
        usages: Array<UsageInfo>,
        listener: com.intellij.refactoring.listeners.RefactoringElementListener?
    ) {
        if (element !is MoveNamedElement) {
            throw IncorrectOperationException("Cannot rename non-named element")
        }
        
        // Perform the rename
        element.setName(newName)
        
        // Handle special post-rename operations
        when (element) {
            is MoveStruct -> handleStructPostRename(element, newName)
            is MoveModule -> handleModulePostRename(element, newName)
        }
        
        listener?.elementRenamed(element)
    }
    
    private fun prepareStructRenaming(
        struct: MoveStruct,
        newName: String,
        allRenames: MutableMap<PsiElement, String>
    ) {
        // If struct has a constructor function with the same name, rename it too
        val module = struct.parent as? MoveModule ?: return
        val constructorFunction = module.functions.find { it.name == struct.name }
        if (constructorFunction != null) {
            allRenames[constructorFunction] = newName.lowercase()
        }
    }
    
    private fun prepareModuleRenaming(
        module: MoveModule,
        newName: String,
        allRenames: MutableMap<PsiElement, String>
    ) {
        // Check if module has a corresponding test module
        val file = module.containingFile as? MoveFile ?: return
        val testModuleName = "${module.name}_tests"
        val testModule = file.modules.find { it.name == testModuleName }
        if (testModule != null) {
            allRenames[testModule] = "${newName}_tests"
        }
    }
    
    private fun prepareFunctionRenaming(
        function: MoveFunction,
        newName: String,
        allRenames: MutableMap<PsiElement, String>
    ) {
        // Check if function has a corresponding test function
        val module = function.parent as? MoveModule ?: return
        val testFunctionName = "test_${function.name}"
        val testFunction = module.functions.find { it.name == testFunctionName }
        if (testFunction != null) {
            allRenames[testFunction] = "test_$newName"
        }
    }
    
    private fun handleStructPostRename(struct: MoveStruct, newName: String) {
        // Update any phantom type parameters that might reference the old name
        updatePhantomTypes(struct)
        
        // Update any associated constants
        updateAssociatedConstants(struct, newName)
    }
    
    private fun handleModulePostRename(module: MoveModule, newName: String) {
        // Update module documentation if it references the old name
        updateModuleDocumentation(module, newName)
        
        // Update friend declarations in other modules
        updateFriendDeclarations(module, newName)
    }
    
    private fun updatePhantomTypes(struct: MoveStruct) {
        // Implementation for updating phantom type parameters
        val typeParams = struct.typeParameterList?.typeParameters ?: return
        for (param in typeParams) {
            if (param.isPhantom) {
                // Update phantom type references
            }
        }
    }
    
    private fun updateAssociatedConstants(struct: MoveStruct, newName: String) {
        val module = struct.parent as? MoveModule ?: return
        val oldPrefix = struct.name?.uppercase() ?: return
        val newPrefix = newName.uppercase()
        
        // Find and rename associated constants
        module.constants
            .filter { it.name?.startsWith(oldPrefix) == true }
            .forEach { constant ->
                val oldName = constant.name ?: return@forEach
                val newConstName = oldName.replaceFirst(oldPrefix, newPrefix)
                constant.setName(newConstName)
            }
    }
    
    private fun updateModuleDocumentation(module: MoveModule, newName: String) {
        // Update module-level documentation comments
        val docComment = module.docComment ?: return
        val oldName = module.name ?: return
        val newText = docComment.text.replace(oldName, newName)
        if (newText != docComment.text) {
            // Update documentation
        }
    }
    
    private fun updateFriendDeclarations(module: MoveModule, newName: String) {
        // Find and update friend declarations in other modules
        val file = module.containingFile as? MoveFile ?: return
        val oldName = module.name ?: return
        
        for (otherModule in file.modules) {
            if (otherModule == module) continue
            
            otherModule.friendDeclarations
                .filter { it.moduleName == oldName }
                .forEach { friendDecl ->
                    // Update friend declaration
                }
        }
    }
}

/**
 * Extension properties for rename processor.
 */
private val MoveModule.functions: List<MoveFunction>
    get() = com.intellij.psi.util.PsiTreeUtil.findChildrenOfType(this, MoveFunction::class.java).toList()

private val MoveModule.constants: List<MoveConstant>
    get() = com.intellij.psi.util.PsiTreeUtil.findChildrenOfType(this, MoveConstant::class.java).toList()

private val MoveFile.modules: List<MoveModule>
    get() = com.intellij.psi.util.PsiTreeUtil.findChildrenOfType(this, MoveModule::class.java).toList()

private val MoveStruct.typeParameterList: MoveTypeParameterList?
    get() = com.intellij.psi.util.PsiTreeUtil.findChildOfType(this, MoveTypeParameterList::class.java)

private val MoveTypeParameterDecl.isPhantom: Boolean
    get() = (this as? PsiElement)?.text?.contains("phantom") ?: false

private val MoveModule.docComment: PsiElement?
    get() = null // Simplified - would need proper doc comment handling

private val MoveModule.friendDeclarations: List<MoveFriendDeclaration>
    get() = com.intellij.psi.util.PsiTreeUtil.findChildrenOfType(this, MoveFriendDeclaration::class.java).toList()

interface MoveTypeParameterList : PsiElement {
    val typeParameters: List<MoveTypeParameterDecl>
}

interface MoveFriendDeclaration : PsiElement {
    val moduleName: String?
}
