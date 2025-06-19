package com.suimove.intellij.testing

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Function
import com.suimove.intellij.MoveIcons
import com.suimove.intellij.psi.*
import com.suimove.intellij.testing.MoveTestRunConfiguration
import com.intellij.openapi.project.Project
import com.intellij.execution.ProgramRunnerUtil
import javax.swing.Icon

/**
 * Provides line markers for running Move tests.
 */
class MoveTestLineMarkerProvider : RunLineMarkerContributor() {
    
    override fun getInfo(element: PsiElement): Info? {
        // Check if element is a test function identifier
        if (!isIdentifier(element)) return null
        
        val function = element.parent as? MoveFunction ?: return null
        
        // Check if it's a test function
        if (!isTestFunction(function)) return null
        
        val icon = getTestIcon(function)
        val actions = ExecutorAction.getActions(0)
        
        return Info(
            icon,
            { "Run test '${function.name}'" },
            *actions
        )
    }
    
    private fun isIdentifier(element: PsiElement): Boolean {
        // Check if element is a function name identifier
        val parent = element.parent
        return parent is MoveFunction && element == parent.nameIdentifier
    }
    
    private fun isTestFunction(function: MoveFunction): Boolean {
        // Check for #[test] attribute
        if (hasTestAttribute(function)) return true
        
        // Check for #[expected_failure] attribute
        if (hasExpectedFailureAttribute(function)) return true
        
        // Check naming convention (functions starting with test_)
        val name = function.name ?: return false
        return name.startsWith("test_") || name.startsWith("testsuite_")
    }
    
    private fun hasTestAttribute(function: MoveFunction): Boolean {
        return function.attributeList?.attributes?.any { attr ->
            attr.name == "test"
        } ?: false
    }
    
    private fun hasExpectedFailureAttribute(function: MoveFunction): Boolean {
        return function.attributeList?.attributes?.any { attr ->
            attr.name == "expected_failure"
        } ?: false
    }
    
    private fun getTestIcon(function: MoveFunction): Icon {
        return if (hasExpectedFailureAttribute(function)) {
            MoveIcons.TEST_FUNCTION_ERROR
        } else {
            MoveIcons.TEST_FUNCTION
        }
    }
}

/**
 * Alternative implementation using LineMarkerProvider for more control.
 */
class MoveTestLineMarkerProviderAlt : LineMarkerProvider {
    
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        // Check if element is a test function
        if (element !is MoveFunction) return null
        if (!isTestFunction(element)) return null
        
        val identifier = element.nameIdentifier ?: return null
        
        return createTestFunctionMarker(element)
    }
    
    private fun isTestFunction(function: MoveFunction): Boolean {
        // Check for test attributes
        val attributes = function.attributeList?.attributes ?: emptyList()
        return attributes.any { it.name in listOf("test", "expected_failure") }
    }
    
    private fun createTestFunctionMarker(function: MoveFunction): LineMarkerInfo<PsiElement>? {
        val identifier = function.nameIdentifier ?: return null
        
        return LineMarkerInfo(
            identifier,
            identifier.textRange,
            MoveIcons.TEST_FUNCTION,
            Function { "Run test '${function.name}'" },
            { _, _ -> runTest(function) },
            GutterIconRenderer.Alignment.LEFT
        )
    }
    
    private fun runTest(function: MoveFunction) {
        val project = function.project
        createAndRunTestConfiguration(project, function)
    }
    
    private fun createAndRunTestConfiguration(project: Project, function: MoveFunction) {
        // Create run configuration for the test
        val runManager = com.intellij.execution.RunManager.getInstance(project)
        val configFactory = MoveTestConfigurationFactory(MoveTestRunConfigurationType())
        val settings = runManager.createConfiguration(
            "Test ${function.name}",
            configFactory
        )
        
        val config = settings.configuration as MoveTestRunConfiguration
        config.setTestKind(TestKind.FUNCTION)
        config.setFunctionName(function.name)
        config.setModulePath(function.containingFile.virtualFile.path)
        
        // Execute the configuration
        val executor = com.intellij.execution.executors.DefaultRunExecutor.getRunExecutorInstance()
        ProgramRunnerUtil.executeConfiguration(settings, executor)
    }
}

/**
 * Line marker provider for test modules.
 */
class MoveTestModuleLineMarkerProvider : LineMarkerProvider {
    
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        // Check if element is a module with tests
        if (element !is MoveModule) return null
        if (!hasTestFunctions(element)) return null
        
        val identifier = element.nameIdentifier ?: return null
        
        return createTestModuleMarker(element)
    }
    
    private fun hasTestFunctions(module: MoveModule): Boolean {
        return module.functions.any { function ->
            function.attributeList?.attributes?.any { 
                it.name in listOf("test", "expected_failure") 
            } ?: false
        }
    }
    
    private fun createTestModuleMarker(module: MoveModule): LineMarkerInfo<PsiElement>? {
        val identifier = module.nameIdentifier ?: return null
        
        return LineMarkerInfo(
            identifier,
            identifier.textRange,
            MoveIcons.MODULE,
            Function { "Run all tests in module '${module.name}'" },
            { _, _ -> runModuleTests(module) },
            GutterIconRenderer.Alignment.LEFT
        )
    }
    
    private fun runModuleTests(module: MoveModule) {
        val project = module.project
        createAndRunModuleTestConfiguration(project, module)
    }
    
    private fun createAndRunModuleTestConfiguration(project: Project, module: MoveModule) {
        val runManager = com.intellij.execution.RunManager.getInstance(project)
        val configFactory = MoveTestConfigurationFactory(MoveTestRunConfigurationType())
        val settings = runManager.createConfiguration(
            "Test module ${module.name}",
            configFactory
        )
        
        val config = settings.configuration as MoveTestRunConfiguration
        config.setTestKind(TestKind.MODULE)
        config.setModulePath(module.containingFile.virtualFile.path)
        
        val executor = com.intellij.execution.executors.DefaultRunExecutor.getRunExecutorInstance()
        ProgramRunnerUtil.executeConfiguration(settings, executor)
    }
}
