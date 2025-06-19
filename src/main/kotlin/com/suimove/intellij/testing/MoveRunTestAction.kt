package com.suimove.intellij.testing

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.suimove.intellij.psi.*
import com.suimove.intellij.testing.TestKind

/**
 * Action to run Move tests from the editor.
 */
class MoveRunTestAction : AnAction("Run Move Test") {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        
        val offset = editor.caretModel.offset
        val element = psiFile.findElementAt(offset) ?: return
        
        // Find test function or module
        val testElement = findTestElement(element) ?: return
        
        // Create and run configuration
        val configuration = createRunConfiguration(testElement) ?: return
        ProgramRunnerUtil.executeConfiguration(configuration, DefaultRunExecutor.getRunExecutorInstance())
    }
    
    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        
        if (project == null || editor == null || psiFile == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }
        
        val offset = editor.caretModel.offset
        val element = psiFile.findElementAt(offset)
        
        val testElement = element?.let { findTestElement(it) }
        e.presentation.isEnabledAndVisible = testElement != null
        
        when (testElement) {
            is MoveFunction -> e.presentation.text = "Run Test '${testElement.name}'"
            is MoveModule -> e.presentation.text = "Run Tests in Module '${testElement.name}'"
            else -> e.presentation.text = "Run Move Test"
        }
    }
    
    private fun findTestElement(element: PsiElement): PsiElement? {
        // Check if we're in a test function
        val function = PsiTreeUtil.getParentOfType(element, MoveFunction::class.java)
        if (function != null && isTestFunction(function)) {
            return function
        }
        
        // Check if we're in a module with tests
        val module = PsiTreeUtil.getParentOfType(element, MoveModule::class.java)
        if (module != null && hasTestFunctions(module)) {
            return module
        }
        
        return null
    }
    
    private fun isTestFunction(function: MoveFunction): Boolean {
        val attributes = function.attributeList?.attributes ?: emptyList()
        return attributes.any { it.name in listOf("test", "expected_failure") }
    }
    
    private fun hasTestFunctions(module: MoveModule): Boolean {
        return module.functions.any { isTestFunction(it) }
    }
    
    private fun createRunConfiguration(testElement: PsiElement): RunnerAndConfigurationSettings? {
        val project = testElement.project
        val runManager = RunManager.getInstance(project)
        val configFactory = MoveTestConfigurationFactory(MoveTestRunConfigurationType())
        
        return when (testElement) {
            is MoveFunction -> {
                val settings = runManager.createConfiguration(
                    "Test ${testElement.name}",
                    configFactory
                )
                val config = settings.configuration as MoveTestRunConfiguration
                config.setTestKind(TestKind.FUNCTION)
                config.setFunctionName(testElement.name)
                config.setModulePath(testElement.containingFile.virtualFile.path)
                settings
            }
            is MoveModule -> {
                val settings = runManager.createConfiguration(
                    "Test module ${testElement.name}",
                    configFactory
                )
                val config = settings.configuration as MoveTestRunConfiguration
                config.setTestKind(TestKind.MODULE)
                config.setModulePath(testElement.containingFile.virtualFile.path)
                settings
            }
            else -> null
        }
    }
}

/**
 * Action to debug Move tests.
 */
class MoveDebugTestAction : AnAction("Debug Move Test") {
    
    override fun actionPerformed(e: AnActionEvent) {
        // TODO: Implement debug support
        // For now, just show a message
        val project = e.project ?: return
        com.intellij.openapi.ui.Messages.showInfoMessage(
            project,
            "Move test debugging is not yet implemented. This feature will be added in a future update.",
            "Debug Move Test"
        )
    }
    
    override fun update(e: AnActionEvent) {
        // Use same logic as run action for now
        val runAction = MoveRunTestAction()
        runAction.update(e)
        
        if (e.presentation.isEnabledAndVisible) {
            e.presentation.text = e.presentation.text.replace("Run", "Debug")
        }
    }
}
