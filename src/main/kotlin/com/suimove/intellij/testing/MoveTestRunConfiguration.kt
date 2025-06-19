package com.suimove.intellij.testing

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMExternalizerUtil
import com.intellij.psi.PsiElement
import com.suimove.intellij.debugger.MoveDebugOptions
import org.jdom.Element
import javax.swing.Icon
import com.suimove.intellij.MoveIcons

/**
 * Run configuration for Move tests.
 */
class MoveTestRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<MoveTestRunConfigurationOptions>(project, factory, name) {
    
    val debugOptions = MoveDebugOptions()
    
    public override fun getOptions(): MoveTestRunConfigurationOptions {
        return super.getOptions() as MoveTestRunConfigurationOptions
    }
    
    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return MoveTestRunConfigurationEditor()
    }
    
    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return MoveTestRunState(environment, this)
    }
    
    override fun checkConfiguration() {
        val options = getOptions()
        
        when (options.testKind) {
            TestKind.MODULE -> {
                if (options.modulePath.isNullOrBlank()) {
                    throw RuntimeConfigurationException("Module path is not specified")
                }
            }
            TestKind.FUNCTION -> {
                if (options.functionName.isNullOrBlank()) {
                    throw RuntimeConfigurationException("Test function name is not specified")
                }
            }
            TestKind.PACKAGE -> {
                if (options.packagePath.isNullOrBlank()) {
                    throw RuntimeConfigurationException("Package path is not specified")
                }
            }
        }
    }
    
    override fun getIcon(): Icon = MoveIcons.TEST_FUNCTION
    
    fun setTestKind(kind: TestKind) {
        getOptions().testKind = kind
    }
    
    fun setModulePath(path: String?) {
        getOptions().modulePath = path
    }
    
    fun setFunctionName(name: String?) {
        getOptions().functionName = name
    }
    
    fun setPackagePath(path: String?) {
        getOptions().packagePath = path
    }
    
    fun setTestFilter(filter: String?) {
        getOptions().testFilter = filter
    }
    
    fun setAdditionalArguments(args: String?) {
        getOptions().additionalArguments = args
    }
    
    fun setEnvironmentVariables(vars: Map<String, String>) {
        getOptions().environmentVariables = vars
    }
    
    fun setGasLimit(limit: Long?) {
        getOptions().gasLimit = limit
    }
    
    fun setShowOutput(show: Boolean) {
        getOptions().showOutput = show
    }
    
    fun setCoverage(enabled: Boolean) {
        getOptions().coverage = enabled
    }
}

/**
 * Configuration options for Move test runs.
 */
class MoveTestRunConfigurationOptions : RunConfigurationOptions() {
    private val testKindProperty = string("testKind").provideDelegate(this, "testKind")
    private val modulePathProperty = string("modulePath").provideDelegate(this, "modulePath")
    private val functionNameProperty = string("functionName").provideDelegate(this, "functionName")
    private val packagePathProperty = string("packagePath").provideDelegate(this, "packagePath")
    private val testFilterProperty = string("testFilter").provideDelegate(this, "testFilter")
    private val additionalArgsProperty = string("additionalArguments").provideDelegate(this, "additionalArguments")
    private val envVarsProperty = map<String, String>().provideDelegate(this, "environmentVariables")
    private val gasLimitProperty = string("gasLimit").provideDelegate(this, "gasLimit")
    private val showOutputProperty = property(false).provideDelegate(this, "showOutput")
    private val coverageProperty = property(false).provideDelegate(this, "coverage")
    
    var testKind: TestKind
        get() = TestKind.valueOf(testKindProperty.getValue(this) ?: TestKind.MODULE.name)
        set(value) = testKindProperty.setValue(this, value.name)
    
    var modulePath: String?
        get() = modulePathProperty.getValue(this)
        set(value) = modulePathProperty.setValue(this, value)
    
    var functionName: String?
        get() = functionNameProperty.getValue(this)
        set(value) = functionNameProperty.setValue(this, value)
    
    var packagePath: String?
        get() = packagePathProperty.getValue(this)
        set(value) = packagePathProperty.setValue(this, value)
    
    var testFilter: String?
        get() = testFilterProperty.getValue(this)
        set(value) = testFilterProperty.setValue(this, value)
    
    var additionalArguments: String?
        get() = additionalArgsProperty.getValue(this)
        set(value) = additionalArgsProperty.setValue(this, value)
    
    var environmentVariables: Map<String, String>
        get() = envVarsProperty.getValue(this) ?: emptyMap()
        set(value) = envVarsProperty.setValue(this, value.toMutableMap())
    
    var gasLimit: Long?
        get() = gasLimitProperty.getValue(this)?.toLongOrNull()
        set(value) = gasLimitProperty.setValue(this, value?.toString())
    
    var showOutput: Boolean
        get() = showOutputProperty.getValue(this)
        set(value) = showOutputProperty.setValue(this, value)
    
    var coverage: Boolean
        get() = coverageProperty.getValue(this)
        set(value) = coverageProperty.setValue(this, value)
}

/**
 * Test kind enumeration.
 */
enum class TestKind {
    MODULE,    // Run all tests in a module
    FUNCTION,  // Run a specific test function
    PACKAGE    // Run all tests in a package
}

/**
 * Configuration factory for Move tests.
 */
class MoveTestConfigurationFactory(type: MoveTestRunConfigurationType) : ConfigurationFactory(type) {
    
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return MoveTestRunConfiguration(project, this, "Move Test")
    }
    
    override fun getId(): String = "MoveTestConfiguration"
    
    override fun getName(): String = "Move Test"
    
    override fun getOptionsClass(): Class<out RunConfigurationOptions> {
        return MoveTestRunConfigurationOptions::class.java
    }
}

/**
 * Run configuration type for Move tests.
 */
class MoveTestRunConfigurationType : ConfigurationType {
    
    override fun getDisplayName(): String = "Move Test"
    
    override fun getConfigurationTypeDescription(): String = "Run Move tests"
    
    override fun getIcon(): Icon = MoveIcons.TEST_FUNCTION
    
    override fun getId(): String = "MoveTestRunConfiguration"
    
    override fun getConfigurationFactories(): Array<ConfigurationFactory> {
        return arrayOf(MoveTestConfigurationFactory(this))
    }
}
