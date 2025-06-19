package com.suimove.intellij.refactoring

import com.intellij.codeInsight.CodeInsightUtilCore
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pass
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.RefactoringBundle
import com.intellij.refactoring.extractMethod.ExtractMethodHandler
import com.intellij.refactoring.util.CommonRefactoringUtil
import com.intellij.util.containers.MultiMap
import com.suimove.intellij.psi.*
import com.suimove.intellij.services.type.MoveType
import com.suimove.intellij.MoveFileType
import com.suimove.intellij.services.type.MoveTypeInferenceEngine

/**
 * Handler for extracting Move code into a function.
 */
class MoveExtractFunctionHandler : RefactoringActionHandler {
    
    override fun invoke(project: Project, editor: Editor?, file: PsiFile?, dataContext: DataContext?) {
        if (editor == null || file !is MoveFile) return
        
        val selectionModel = editor.selectionModel
        if (!selectionModel.hasSelection()) {
            CommonRefactoringUtil.showErrorHint(
                project,
                editor,
                "Please select code to extract",
                RefactoringBundle.message("extract.method.title"),
                null
            )
            return
        }
        
        val startOffset = selectionModel.selectionStart
        val endOffset = selectionModel.selectionEnd
        
        // Find the elements to extract
        val elements = findElementsToExtract(file, startOffset, endOffset)
        if (elements.isEmpty()) {
            CommonRefactoringUtil.showErrorHint(
                project,
                editor,
                "Cannot extract selected code",
                RefactoringBundle.message("extract.method.title"),
                null
            )
            return
        }
        
        // Analyze the selection
        val analysis = analyzeSelection(elements)
        if (analysis.errors.isNotEmpty()) {
            CommonRefactoringUtil.showErrorHint(
                project,
                editor,
                analysis.errors.first(),
                RefactoringBundle.message("extract.method.title"),
                null
            )
            return
        }
        
        // Show dialog
        val dialog = MoveExtractFunctionDialog(project, analysis)
        if (!dialog.showAndGet()) {
            return
        }
        
        // Perform extraction
        performExtraction(project, editor, file, analysis, dialog.getFunctionName(), dialog.getVisibility())
    }
    
    override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) {
        // Not used for this refactoring
    }
    
    private fun findElementsToExtract(file: MoveFile, startOffset: Int, endOffset: Int): List<PsiElement> {
        val startElement = file.findElementAt(startOffset) ?: return emptyList()
        val endElement = file.findElementAt(endOffset - 1) ?: return emptyList()
        
        // Find common parent
        val commonParent = PsiTreeUtil.findCommonParent(startElement, endElement) ?: return emptyList()
        
        // Collect all statements in the selection
        val statements = mutableListOf<PsiElement>()
        var current: PsiElement? = startElement
        
        while (current != null && current.textRange.startOffset < endOffset) {
            if (current is MoveStatement || current is MoveExpression) {
                if (current.textRange.startOffset >= startOffset && current.textRange.endOffset <= endOffset) {
                    statements.add(current)
                }
            }
            current = PsiTreeUtil.nextLeaf(current)
        }
        
        return statements
    }
    
    private fun analyzeSelection(elements: List<PsiElement>): ExtractionAnalysis {
        val errors = mutableListOf<String>()
        val inputVariables = mutableSetOf<MoveVariable>()
        val outputVariables = mutableSetOf<MoveVariable>()
        val modifiedVariables = mutableSetOf<MoveVariable>()
        val usedTypes = mutableSetOf<MoveType>()
        
        // Check if we can extract these elements
        val containsReturn = elements.any { element ->
            PsiTreeUtil.findChildOfType(element, MoveReturnStatement::class.java) != null
        }
        
        if (containsReturn) {
            errors.add("Cannot extract code containing return statements")
        }
        
        // Analyze variable usage
        val declaredInSelection = mutableSetOf<MoveVariable>()
        val usedInSelection = mutableSetOf<MoveVariable>()
        
        for (element in elements) {
            // Find declared variables
            PsiTreeUtil.findChildrenOfType(element, MoveLetStatement::class.java).forEach { letStmt ->
                letStmt.variables.forEach { variable -> declaredInSelection.add(variable) }
            }
            
            // Find used variables
            PsiTreeUtil.findChildrenOfType(element, MoveReferenceExpression::class.java).forEach { ref ->
                val resolved = ref.reference?.resolve()
                if (resolved is MoveVariable) {
                    usedInSelection.add(resolved)
                }
            }
            
            // Find assignments
            PsiTreeUtil.findChildrenOfType(element, MoveAssignment::class.java).forEach { assignment ->
                val lvalue = assignment.lvalue
                if (lvalue is MoveReferenceExpression) {
                    val resolved = lvalue.reference?.resolve()
                    if (resolved is MoveVariable && resolved !in declaredInSelection) {
                        modifiedVariables.add(resolved)
                    }
                }
            }
        }
        
        // Determine input variables (used but not declared in selection)
        inputVariables.addAll(usedInSelection - declaredInSelection)
        
        // Check for variables used after selection
        val parentBlock = PsiTreeUtil.getParentOfType(elements.first(), MoveCodeBlock::class.java)
        if (parentBlock != null) {
            val selectionEnd = elements.last().textRange.endOffset
            
            parentBlock.statements.forEach { stmt ->
                if (stmt.textRange.startOffset > selectionEnd) {
                    PsiTreeUtil.findChildrenOfType(stmt, MoveReferenceExpression::class.java).forEach { ref ->
                        val resolved = ref.reference?.resolve()
                        if (resolved is MoveVariable && resolved in declaredInSelection) {
                            outputVariables.add(resolved)
                        }
                    }
                }
            }
        }
        
        // Check for multiple outputs
        if (outputVariables.size + modifiedVariables.size > 1) {
            errors.add("Cannot extract code with multiple output values")
        }
        
        // Infer types
        val typeEngine = MoveTypeInferenceEngine.getInstance(elements.first().project)
        inputVariables.forEach { variable ->
            typeEngine.inferType(variable)?.let { usedTypes.add(it) }
        }
        outputVariables.forEach { variable ->
            typeEngine.inferType(variable)?.let { usedTypes.add(it) }
        }
        
        return ExtractionAnalysis(
            elements = elements,
            inputVariables = inputVariables,
            outputVariables = outputVariables,
            modifiedVariables = modifiedVariables,
            usedTypes = usedTypes,
            errors = errors
        )
    }
    
    private fun performExtraction(
        project: Project,
        editor: Editor,
        file: MoveFile,
        analysis: ExtractionAnalysis,
        functionName: String,
        visibility: String
    ) {
        val factory = MoveElementFactory.getInstance(project)
        
        // Build function signature
        val parameters = analysis.inputVariables.map { variable ->
            val type = MoveTypeInferenceEngine.getInstance(project).inferType(variable)
            val ref = if (variable in analysis.modifiedVariables) "&mut " else ""
            "$ref${variable.name}: ${type ?: "unknown"}"
        }.joinToString(", ")
        
        val returnType = when {
            analysis.outputVariables.isNotEmpty() -> {
                val variable = analysis.outputVariables.first()
                MoveTypeInferenceEngine.getInstance(project).inferType(variable)?.toString() ?: "unknown"
            }
            analysis.modifiedVariables.isNotEmpty() -> null // No return type for mut ref
            else -> null
        }
        
        val returnClause = if (returnType != null) ": $returnType" else ""
        
        // Build function body
        val bodyStatements = analysis.elements.map { it.text }.joinToString("\n        ")
        val returnStatement = if (analysis.outputVariables.isNotEmpty()) {
            "\n        ${analysis.outputVariables.first().name}"
        } else ""
        
        val functionText = """
            |$visibility fun $functionName($parameters)$returnClause {
            |        $bodyStatements$returnStatement
            |    }
        """.trimMargin()
        
        // Create the function
        val newFunction = factory.createFunction(functionText)
        
        // Find insertion point (after current function)
        val currentFunction = PsiTreeUtil.getParentOfType(analysis.elements.first(), MoveFunction::class.java)
        val module = PsiTreeUtil.getParentOfType(currentFunction, MoveModule::class.java)
        
        if (currentFunction != null && module != null) {
            // Insert the new function
            val insertedFunction = module.addAfter(newFunction, currentFunction) as MoveFunction
            module.addAfter(factory.createNewline(), currentFunction)
            
            // Replace selected code with function call
            val callArguments = analysis.inputVariables.map { variable ->
                if (variable in analysis.modifiedVariables) "&mut ${variable.name}" else variable.name
            }.joinToString(", ")
            
            val functionCall = if (analysis.outputVariables.isNotEmpty()) {
                "let ${analysis.outputVariables.first().name} = $functionName($callArguments)"
            } else {
                "$functionName($callArguments)"
            }
            
            val callStatement = factory.createStatement(functionCall)
            
            // Replace the selection
            val firstElement = analysis.elements.first()
            val lastElement = analysis.elements.last()
            val parent = firstElement.parent
            
            parent.addBefore(callStatement, firstElement)
            parent.deleteChildRange(firstElement, lastElement)
            
            // Format the code
            CodeInsightUtilCore.forcePsiPostprocessAndRestoreElement(insertedFunction)
            
            // Move caret to the new function
            editor.caretModel.moveToOffset(insertedFunction.textOffset)
        }
    }
}

/**
 * Analysis result for extract function refactoring.
 */
data class ExtractionAnalysis(
    val elements: List<PsiElement>,
    val inputVariables: Set<MoveVariable>,
    val outputVariables: Set<MoveVariable>,
    val modifiedVariables: Set<MoveVariable>,
    val usedTypes: Set<MoveType>,
    val errors: List<String>
)

/**
 * Factory for creating Move PSI elements.
 */
class MoveElementFactory private constructor(private val project: Project) {
    
    companion object {
        fun getInstance(project: Project): MoveElementFactory {
            return MoveElementFactory(project)
        }
    }
    
    fun createFunction(text: String): MoveFunction {
        val file = createDummyFile(project, "module dummy { $text }")
        return PsiTreeUtil.findChildOfType(file, MoveFunction::class.java)
            ?: throw IllegalArgumentException("Failed to create function from text: $text")
    }
    
    fun createStatement(text: String): MoveStatement {
        val file = createDummyFile(project, "module dummy { fun dummy() { $text; } }")
        return PsiTreeUtil.findChildOfType(file, MoveStatement::class.java)
            ?: throw IllegalArgumentException("Failed to create statement from text: $text")
    }
    
    fun createExpression(text: String): MoveExpression {
        val file = createDummyFile(project, "module dummy { fun dummy() { $text; } }")
        return PsiTreeUtil.findChildOfType(file, MoveExpression::class.java)
            ?: throw IllegalArgumentException("Failed to create expression from text: $text")
    }
    
    fun createBlock(text: String): MoveCodeBlock {
        val file = createDummyFile(project, "module dummy { fun dummy() $text }")
        return PsiTreeUtil.findChildOfType(file, MoveCodeBlock::class.java)
            ?: throw IllegalArgumentException("Failed to create block from text: $text")
    }
    
    fun createNewline(): PsiElement {
        return createDummyFile(project, "\n").firstChild
    }
    
    private fun createDummyFile(project: Project, text: String): MoveFile {
        return PsiFileFactory.getInstance(project)
            .createFileFromText("dummy.move", MoveFileType, text) as MoveFile
    }
}

// Extension properties
private val MoveLetStatement.variables: List<MoveVariable>
    get() = PsiTreeUtil.getChildrenOfTypeAsList(this, MoveVariable::class.java)

private val MoveAssignment.lvalue: MoveExpression?
    get() = children.firstOrNull { it is MoveExpression } as? MoveExpression

// Temporary interface definitions - these should be moved to proper PSI files
interface MoveLetStatement : PsiElement
