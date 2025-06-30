import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.17.0"
    id("org.jetbrains.changelog") version "2.2.0"
    id("org.jetbrains.grammarkit") version "2022.3.2"
    id("jacoco")
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

repositories {
    mavenCentral()
}

dependencies {
    // IntelliJ test framework dependencies are provided by the platform
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.3")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
    testImplementation(kotlin("test"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.9.3")
}

sourceSets {
    main {
        java {
            srcDirs("src/main/gen")
        }
    }
}

kotlin {
    jvmToolchain(17)
}

intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
    
    plugins.set(listOf("java"))
    
    // Download sources for IntelliJ SDK
    downloadSources.set(true)
    
    // Include test framework
    instrumentCode.set(true)
}

changelog {
    groups.empty()
    repositoryUrl.set(properties("pluginRepositoryUrl"))
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }
    
    generateLexer {
        sourceFile = file("src/main/flex/Move.flex")
        targetDir = "src/main/gen/com/suimove/intellij/lexer"
        targetClass = "_MoveLexer"
        purgeOldFiles = true
    }
    
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
        dependsOn(generateLexer)
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    test {
        // Use JUnit Platform for tests
        useJUnitPlatform()
        
        // Set up test JVM arguments
        jvmArgs = listOf(
            "-Djava.awt.headless=true",
            "-Didea.test.framework.detector=com.intellij.testFramework.LightPlatformTestCase",
            "-Didea.home.path=${layout.buildDirectory.get()}/idea-sandbox",
            "-Didea.config.path=${layout.buildDirectory.get()}/idea-sandbox/config",
            "-Didea.system.path=${layout.buildDirectory.get()}/idea-sandbox/system",
            "-Didea.plugins.path=${layout.buildDirectory.get()}/idea-sandbox/plugins",
            "-Didea.log.path=${layout.buildDirectory.get()}/idea-sandbox/log",
            "-Dfile.encoding=UTF-8",
            "-Duser.language=en",
            "-Duser.country=US"
        )
        
        // Increase memory for tests
        maxHeapSize = "2048m"
        
        // Show test output
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = false
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
        
        // Set system properties for IntelliJ
        systemProperty("idea.force.use.core.classloader", "true")
        systemProperty("idea.use.core.classloader.for.plugin.path", "true")
    }
    
    test {
        finalizedBy(jacocoTestReport)
    }
    
    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(false)
        }
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))

        pluginDescription.set(providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with (it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        })

        val changelog = project.changelog
        changeNotes.set(properties("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        })
    }

    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    signPlugin {
        certificateChain.set(environment("CERTIFICATE_CHAIN"))
        privateKey.set(environment("PRIVATE_KEY"))
        password.set(environment("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(environment("PUBLISH_TOKEN"))
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        // channels.set(listOf(properties("pluginVersion").map { it.split('-').getOrElse(1) { "default" }.split('.').first() }))
    }
}

jacoco {
    toolVersion = "0.8.11"
}
