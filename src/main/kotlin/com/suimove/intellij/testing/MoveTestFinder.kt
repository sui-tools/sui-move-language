package com.suimove.intellij.testing

import com.intellij.testIntegration.TestFinder
import com.intellij.testIntegration.TestFramework
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.suimove.intellij.psi.*
import com.suimove.intellij.stubs.MoveModuleIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.openapi.fileTypes.FileType
import com.suimove.intellij.MoveLanguage
import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.lang.Language
import com.suimove.intellij.MoveIcons
import javax.swing.Icon

/**
 * Finds tests for Move code elements and vice versa.
 */
class MoveTestFinder : TestFinder {
    
    override fun findTestsForClass(element: PsiElement): Collection<PsiElement> {
        if (element !is MoveModule) return emptyList()
        
        // Find test functions in the module
        return element.functions.filter { isTestFunction(it) }
    }
    
    override fun findClassesForTest(element: PsiElement): Collection<PsiElement> {
        if (element !is MoveFunction || !isTestFunction(element)) return emptyList()
        
        // Return the containing module
        return listOfNotNull(element.parent as? MoveModule)
    }
    
    override fun isTest(element: PsiElement): Boolean {
        val function = findMoveFunction(element) ?: return false
        return isTestFunction(function)
    }
    
    override fun findSourceElement(from: PsiElement): PsiElement? {
        return findMoveElement(from)
    }
    
    private fun findMoveElement(element: PsiElement): PsiElement? {
        return when {
            element is MoveFunction -> element
            element is MoveStruct -> element
            element is MoveModule -> element
            else -> PsiTreeUtil.getParentOfType(element, 
                MoveFunction::class.java, 
                MoveStruct::class.java,
                MoveModule::class.java
            )
        }
    }
    
    private fun findMoveFunction(element: PsiElement): MoveFunction? {
        return when (element) {
            is MoveFunction -> element
            else -> PsiTreeUtil.getParentOfType(element, MoveFunction::class.java)
        }
    }
    
    private fun findTestsForFunction(function: MoveFunction): Collection<MoveFunction> {
        val functionName = function.name ?: return emptyList()
        val project = function.project
        val scope = GlobalSearchScope.projectScope(project)
        
        val testNames = generateTestNames(functionName)
        val results = mutableListOf<MoveFunction>()
        
        // Search for test functions
        for (testName in testNames) {
            FileBasedIndex.getInstance().processAllKeys(MoveModuleIndex.KEY, { moduleName ->
                val moduleList = findModulesByName(project, moduleName)
                moduleList.forEach { module ->
                    module.functions.forEach { func ->
                        if (func.name == testName && isTestFunction(func)) {
                            results.add(func)
                        }
                    }
                }
                true
            }, scope, null)
        }
        
        return results
    }
    
    private fun findTestsForStruct(struct: MoveStruct): Collection<MoveFunction> {
        val structName = struct.name ?: return emptyList()
        val project = struct.project
        val scope = GlobalSearchScope.projectScope(project)
        
        val testNames = generateTestNames(structName)
        val results = mutableListOf<MoveFunction>()
        
        for (testName in testNames) {
            FileBasedIndex.getInstance().processAllKeys(MoveModuleIndex.KEY, { moduleName ->
                val moduleList = findModulesByName(project, moduleName)
                moduleList.forEach { module ->
                    module.functions.find { it.name == testName }?.let { results.add(it) }
                }
                true
            }, scope, null)
        }
        
        return results
    }
    
    private fun findTestsForModule(module: MoveModule): Collection<MoveFunction> {
        // Find all test functions in the same package
        val moduleName = module.name ?: return emptyList()
        val project = module.project
        
        // Look for test modules
        val testModuleNames = listOf(
            "${moduleName}_tests",
            "${moduleName}_test",
            "test_$moduleName"
        )
        
        val results = mutableListOf<MoveFunction>()
        
        for (testModuleName in testModuleNames) {
            val testModules = findModulesByName(project, testModuleName)
            testModules.forEach { testModule ->
                testModule.functions.forEach { function ->
                    if (isTestFunction(function)) {
                        results.add(function)
                    }
                }
            }
        }
        
        // Also include tests in the same module
        module.functions.forEach { function ->
            if (isTestFunction(function)) {
                results.add(function)
            }
        }
        
        return results
    }
    
    private fun generateTestNames(elementName: String): List<String> {
        return listOf(
            "test_$elementName",
            "test_${elementName}_basic",
            "test_${elementName}_complex",
            "${elementName}_test",
            "should_${elementName}",
            "test_should_$elementName"
        )
    }
    
    private fun extractTestedName(testName: String): String {
        return when {
            testName.startsWith("test_") -> testName.removePrefix("test_").removeSuffix("_basic").removeSuffix("_complex")
            testName.endsWith("_test") -> testName.removeSuffix("_test")
            testName.startsWith("should_") -> testName.removePrefix("should_")
            testName.startsWith("test_should_") -> testName.removePrefix("test_should_")
            else -> testName
        }
    }
    
    private fun isTestFunction(function: MoveFunction): Boolean {
        // Check for test attributes
        val attributes = function.attributeList?.attributes ?: emptyList()
        if (attributes.any { it.name in listOf("test", "expected_failure") }) {
            return true
        }
        
        // Check naming convention
        val name = function.name ?: return false
        return name.startsWith("test_") || name.endsWith("_test")
    }
    
    private fun findFunctionsByName(project: Project, name: String, scope: GlobalSearchScope): Collection<MoveFunction> {
        val results = mutableListOf<MoveFunction>()
        
        // Search through all Move files
        FileBasedIndex.getInstance().processAllKeys(MoveModuleIndex.KEY, { moduleName ->
            val moduleList = findModulesByName(project, moduleName)
            moduleList.forEach { module ->
                module.functions.forEach { function ->
                    if (function.name == name) {
                        results.add(function)
                    }
                }
            }
            true
        }, scope, null)
        
        return results
    }
    
    private fun findFunctionByQualifiedName(project: Project, qualifiedName: String): MoveFunction? {
        if (qualifiedName.contains("::")) {
            val parts = qualifiedName.split("::")
            val moduleName = parts[0]
            val functionName = parts[1]
            
            // Find the module
            val modules = findModulesByName(project, moduleName)
            modules.forEach { module ->
                module.functions.find { it.name == functionName }?.let { return it }
            }
        }
        
        // Find all modules with test functions
        val allModules = getAllModules(project)
        allModules.forEach { module ->
            module.functions.find { it.name == qualifiedName }?.let { return it }
        }
        
        return null
    }
    
    private fun getAllModules(project: Project): Collection<MoveModule> {
        val scope = GlobalSearchScope.allScope(project)
        val modules = mutableListOf<MoveModule>()
        
        FileBasedIndex.getInstance().processAllKeys(
            MoveModuleIndex.KEY,
            { moduleName ->
                modules.addAll(findModulesByName(project, moduleName))
                true
            },
            scope,
            null
        )
        
        return modules
    }
    
    private fun findModulesByName(project: Project, name: String): Collection<MoveModule> {
        val scope = GlobalSearchScope.allScope(project)
        val modules = mutableListOf<MoveModule>()
        
        FileBasedIndex.getInstance().processValues(
            MoveModuleIndex.KEY,
            name,
            null,
            { file, value ->
                val psiFile = PsiManager.getInstance(project).findFile(file)
                if (psiFile != null) {
                    val module = PsiTreeUtil.findChildOfType(psiFile, MoveModule::class.java)
                    if (module != null && module.name == name) {
                        modules.add(module)
                    }
                }
                true
            },
            scope
        )
        
        return modules
    }
    
    private fun findElementsByName(project: Project, name: String): Collection<PsiElement> {
        val results = mutableListOf<PsiElement>()
        val scope = GlobalSearchScope.projectScope(project)
        
        // Find modules
        val modules = findModulesByName(project, name)
        results.addAll(modules)
        
        // Find functions
        FileBasedIndex.getInstance().processAllKeys(MoveModuleIndex.KEY, { moduleName ->
            val moduleList = findModulesByName(project, moduleName)
            moduleList.forEach { module ->
                module.functions.find { it.name == name }?.let { results.add(it) }
            }
            true
        }, scope, null)
        
        return results
    }
}

/**
 * Move test framework integration.
 */
class MoveTestFramework : TestFramework {
    override fun getName(): String = "Move Test"
    
    override fun getIcon(): Icon = MoveIcons.TEST_FUNCTION
    
    override fun getLibraryPath(): String? = null
    
    override fun getDefaultSuperClass(): String? = null
    
    override fun getLanguage(): Language = MoveLanguage
    
    override fun getTestMethodFileTemplateDescriptor(): FileTemplateDescriptor = 
        FileTemplateDescriptor("Move Test Method")
    
    override fun getSetUpMethodFileTemplateDescriptor(): FileTemplateDescriptor? = null
    
    override fun getTearDownMethodFileTemplateDescriptor(): FileTemplateDescriptor? = null
    
    override fun isPotentialTestClass(clazz: PsiElement): Boolean {
        return clazz is MoveModule
    }
    
    override fun isTestClass(clazz: PsiElement): Boolean {
        if (clazz !is MoveModule) return false
        return hasTestFunctions(clazz)
    }
    
    override fun isTestMethod(element: PsiElement): Boolean {
        return element is MoveFunction && isTestFunction(element)
    }
    
    override fun isLibraryAttached(module: com.intellij.openapi.module.Module): Boolean = true
    
    override fun isTestMethod(element: PsiElement, checkAbstract: Boolean): Boolean {
        if (!isTestMethod(element)) return false
        if (checkAbstract && element is MoveFunction) {
            // Move doesn't have abstract functions
            return true
        }
        return true
    }
    
    override fun findSetUpMethod(clazz: PsiElement): PsiElement? {
        if (clazz !is MoveModule) return null
        
        // Look for functions with setup attributes or naming
        return clazz.functions.find { function ->
            function.name in listOf("setup", "set_up", "before_each") ||
            function.attributeList?.attributes?.any { it.name == "before_each" } ?: false
        }
    }
    
    override fun findTearDownMethod(clazz: PsiElement): PsiElement? {
        if (clazz !is MoveModule) return null
        
        // Look for functions with teardown attributes or naming
        return clazz.functions.find { function ->
            function.name in listOf("teardown", "tear_down", "after_each") ||
            function.attributeList?.attributes?.any { it.name == "after_each" } ?: false
        }
    }
    
    override fun findOrCreateSetUpMethod(clazz: PsiElement): PsiElement? {
        return findSetUpMethod(clazz)
    }
    
    override fun isIgnoredMethod(element: PsiElement): Boolean {
        if (element !is MoveFunction) return false
        
        // Check for ignore attribute
        return element.attributeList?.attributes?.any { 
            it.name in listOf("ignore", "disabled") 
        } ?: false
    }
    
    private fun hasTestFunctions(module: MoveModule): Boolean {
        return module.functions.any { isTestFunction(it) }
    }
    
    private fun isTestFunction(function: MoveFunction): Boolean {
        val attributes = function.attributeList?.attributes ?: emptyList()
        return attributes.any { it.name in listOf("test", "expected_failure") } ||
            function.name?.startsWith("test_") ?: false ||
            function.name?.endsWith("_test") ?: false
    }
}

/**
 * Extension properties for test analysis.
 */
val MoveFunction.attributeList: MoveAttributeList?
    get() = PsiTreeUtil.findChildOfType(this, MoveAttributeList::class.java)
