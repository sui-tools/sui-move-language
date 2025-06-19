package com.suimove.intellij.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.util.ProcessingContext
import com.suimove.intellij.MoveIcons
import com.suimove.intellij.psi.*

/**
 * Provides import path completions for use statements.
 */
class MoveImportCompletionProvider : CompletionProvider<CompletionParameters>() {
    
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val position = parameters.position
        val project = position.project
        
        // Check if we're completing an address
        val useStatement = position.parent as? MoveUseStatement
        val currentPath = getCurrentImportPath(position)
        
        when {
            currentPath.isEmpty() -> {
                // Complete addresses
                addAddressCompletions(project, result)
            }
            currentPath.count { it == ':' } == 1 -> {
                // Complete module names for given address
                val address = currentPath.substringBefore("::")
                addModuleCompletions(project, address, result)
            }
            currentPath.count { it == ':' } >= 2 -> {
                // Complete items within module
                val parts = currentPath.split("::")
                val address = parts[0]
                val moduleName = parts[1]
                addModuleItemCompletions(project, address, moduleName, result)
            }
        }
    }
    
    private fun getCurrentImportPath(position: PsiElement): String {
        // Extract the current partial import path
        var current = position
        val pathBuilder = StringBuilder()
        
        while (current != null && current !is MoveUseStatement) {
            if (current.text.isNotBlank() && current.text != "use") {
                pathBuilder.insert(0, current.text)
            }
            current = current.prevSibling
        }
        
        return pathBuilder.toString().trim()
    }
    
    private fun addAddressCompletions(project: Project, result: CompletionResultSet) {
        // Add known addresses
        val knownAddresses = listOf(
            "0x1" to "Std library",
            "0x2" to "Sui framework",
            "0x3" to "Sui system"
        )
        
        knownAddresses.forEach { (address, description) ->
            result.addElement(
                LookupElementBuilder.create(address)
                    .withIcon(MoveIcons.ADDRESS)
                    .withTypeText("address")
                    .withTailText(" - $description", true)
                    .withInsertHandler { context, item ->
                        val editor = context.editor
                        val offset = editor.caretModel.offset
                        editor.document.insertString(offset, "::")
                        editor.caretModel.moveToOffset(offset + 2)
                    }
            )
        }
        
        // Add addresses from current project
        findProjectAddresses(project).forEach { address ->
            result.addElement(
                LookupElementBuilder.create(address)
                    .withIcon(MoveIcons.ADDRESS)
                    .withTypeText("address")
                    .withInsertHandler { context, item ->
                        val editor = context.editor
                        val offset = editor.caretModel.offset
                        editor.document.insertString(offset, "::")
                        editor.caretModel.moveToOffset(offset + 2)
                    }
            )
        }
    }
    
    private fun addModuleCompletions(project: Project, address: String, result: CompletionResultSet) {
        // Add standard library modules for common addresses
        if (address == "0x1" || address == "0x2" || address == "std") {
            val standardModules = listOf(
                "vector", "option", "string", "ascii", "bcs", "hash", 
                "signer", "error", "event", "account", "coin", "token"
            )
            
            standardModules.forEach { moduleName ->
                result.addElement(
                    LookupElementBuilder.create(moduleName)
                        .withIcon(MoveIcons.MODULE)
                        .withTypeText("standard module")
                        .withInsertHandler { context, item ->
                            val editor = context.editor
                            val offset = editor.caretModel.offset
                            
                            // Check if user wants to import specific items
                            editor.document.insertString(offset, "::{}")
                            editor.caretModel.moveToOffset(offset + 3)
                        }
                )
            }
        }
        
        // Find all modules at the given address
        val modules = findModulesAtAddress(project, address)
        
        modules.forEach { module ->
            result.addElement(
                LookupElementBuilder.create(module.name ?: "")
                    .withIcon(MoveIcons.MODULE)
                    .withTypeText("module")
                    .withInsertHandler { context, item ->
                        val editor = context.editor
                        val offset = editor.caretModel.offset
                        
                        // Check if user wants to import specific items
                        editor.document.insertString(offset, "::{}")
                        editor.caretModel.moveToOffset(offset + 3)
                    }
            )
        }
    }
    
    private fun addModuleItemCompletions(
        project: Project,
        address: String,
        moduleName: String,
        result: CompletionResultSet
    ) {
        val module = findModule(project, address, moduleName) ?: return
        
        // Add functions
        module.functions.filter { it.visibility == MoveVisibility.PUBLIC }.forEach { function ->
            result.addElement(
                LookupElementBuilder.create(function.name ?: "")
                    .withIcon(MoveIcons.FUNCTION)
                    .withTypeText("function")
                    .withTailText(getFunctionSignature(function), true)
            )
        }
        
        // Add structs
        module.structs.forEach { struct ->
            result.addElement(
                LookupElementBuilder.create(struct.name ?: "")
                    .withIcon(MoveIcons.STRUCT)
                    .withTypeText("struct")
                    .withTailText(
                        if (struct.typeParameters.isNotEmpty()) 
                            "<${struct.typeParameters.joinToString(", ") { it.name }}>"
                        else "",
                        true
                    )
            )
        }
        
        // Add constants
        module.constants.filter { constant ->
            constant.text.contains("public")
        }.forEach { constant ->
            result.addElement(
                LookupElementBuilder.create(constant.name ?: "")
                    .withIcon(MoveIcons.CONSTANT)
                    .withTypeText("const")
                    .withTailText(": ${constant.type?.displayName() ?: "?"}", true)
            )
        }
        
        // Add "Self" for importing the module itself
        result.addElement(
            LookupElementBuilder.create("Self")
                .withIcon(MoveIcons.MODULE)
                .withTypeText("module alias")
                .withBoldness(true)
        )
    }
    
    private fun findProjectAddresses(project: Project): List<String> {
        // TODO: Implement address discovery from project
        return emptyList()
    }
    
    private fun findModulesAtAddress(project: Project, address: String): List<MoveModule> {
        // TODO: Use stub index to find modules
        return emptyList()
    }
    
    private fun findModule(project: Project, address: String, moduleName: String): MoveModule? {
        // TODO: Use stub index to find specific module
        return null
    }
    
    private fun getFunctionSignature(function: MoveFunction): String {
        val params = function.parameters.joinToString(", ") { param ->
            "${param.name}: ${param.type?.text ?: "?"}"
        }
        val returnType = function.children.find { it.text.contains(":") && it != function.parameterList }?.text?.substringAfter(":")?.trim() ?: ""
        return "($params)${if (returnType.isNotEmpty()) ": $returnType" else ""}"
    }
}
