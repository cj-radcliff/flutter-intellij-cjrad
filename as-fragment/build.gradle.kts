/*
 * Android Studio-specific source fragment.
 *
 * Compiles only AS-only classes and contributes them as an implementation
 * dependency of the root plugin project when platformType=AS.
 */

import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
  id("java")
  id("org.jetbrains.intellij.platform") version "2.12.0"
}

val ideaVersion = providers.gradleProperty("ideaVersion").get()
val platformType = providers.gradleProperty("platformType").getOrElse("AS")
val dartPluginVersion = providers.gradleProperty("dartPluginVersion").get()

repositories {
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

sourceSets {
  main {
    java.srcDirs(listOf("../src"))
    java.include(
      listOf(
        "io/flutter/FlutterStudioStartupActivity.java",
        "io/flutter/actions/OpenAndroidModule.java",
        "io/flutter/android/AndroidStudioGradleSyncProvider.java",
        "io/flutter/utils/AddToAppUtils.java",
        "io/flutter/utils/AndroidLocationProvider.java",
        "io/flutter/utils/FlutterExternalSystemTaskNotificationListener.java",
        "io/flutter/utils/GradleUtils.java",
        "org/jetbrains/android/facet/AndroidFrameworkDetector.java"
      )
    )
  }
}

dependencies {
  // AS fragment depends on shared plugin classes from root module.
  compileOnly(project(":"))

  intellijPlatform {
    if (platformType == "AS") {
      androidStudio(ideaVersion)
      bundledPlugins(listOf("org.jetbrains.android", "com.android.tools.idea.smali"))
      plugin("Dart:$dartPluginVersion")
    } else {
      // Avoid compiling any AS classes in IC builds.
      intellijIdeaCommunity(providers.gradleProperty("intellijIdeaVersion").get())
    }
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.encoding = "UTF-8"
}

tasks.named("compileJava") {
  onlyIf { platformType == "AS" }
}

tasks.named("processResources") {
  enabled = false
}

// This module only contributes compiled classes to the root plugin and should
// not run IntelliJ plugin packaging/sandbox tasks on its own.
tasks.matching {
  it.name in listOf(
    "buildPlugin",
    "runIde",
    "prepareSandbox",
    "prepareTestSandbox",
    "verifyPlugin",
    "verifyPluginProjectConfiguration",
    "verifyPluginStructure",
    "verifyPluginSignature",
    "jarSearchableOptions",
    "prepareJarSearchableOptions",
    "buildSearchableOptions"
  )
}.configureEach {
  enabled = false
}

