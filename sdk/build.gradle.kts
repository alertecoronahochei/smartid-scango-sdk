import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import java.io.File

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    `maven-publish`
}

kotlin {
    // `expect class` + `actual typealias` is still flagged Beta; we rely on it deliberately.
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        // Publish the release variant so Android consumers can resolve `…:sdk:0.1.0`.
        publishLibraryVariants("release")
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                }
            }
        }
    }

    // One artifact per platform: SmartScanSDK.xcframework on iOS, one .aar on Android.
    val xcf = XCFramework("SmartScanSDK")
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SmartScanSDK"
            isStatic = true
            xcf.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.annotation)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.camerax.core)
            implementation(libs.camerax.camera2)
            implementation(libs.camerax.lifecycle)
            implementation(libs.camerax.view)
            implementation(libs.mlkit.barcode.scanning)
        }
    }
}

android {
    namespace = "ro.smartid.scango"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// --- Privacy manifest ---------------------------------------------------------
// Apple requires third-party SDKs to ship a PrivacyInfo.xcprivacy. Kotlin/Native does not
// bundle it automatically, so we copy it into every SmartScanSDK.framework slice right after
// the XCFramework is assembled. Runs both locally (on a Mac) and in CI, since it hooks the
// assemble*XCFramework tasks.
val privacyManifestFile = layout.projectDirectory.file("privacy/PrivacyInfo.xcprivacy")

val embedPrivacyManifest by tasks.registering {
    description = "Copies PrivacyInfo.xcprivacy into each SmartScanSDK.framework inside the built XCFrameworks."
    inputs.file(privacyManifestFile)
    val xcframeworksRoot = layout.buildDirectory.dir("XCFrameworks")
    doLast {
        val manifest = privacyManifestFile.asFile
        val root = xcframeworksRoot.get().asFile
        if (!root.exists()) {
            logger.lifecycle("No XCFrameworks directory yet — skipping privacy manifest embed.")
            return@doLast
        }
        val frameworks = root.walkTopDown()
            .filter { it.isDirectory && it.name == "SmartScanSDK.framework" }
            .toList()
        frameworks.forEach { fw ->
            manifest.copyTo(File(fw, "PrivacyInfo.xcprivacy"), overwrite = true)
            logger.lifecycle("Embedded PrivacyInfo.xcprivacy into ${fw.relativeTo(rootDir)}")
        }
        if (frameworks.isEmpty()) {
            logger.warn("embedPrivacyManifest found no SmartScanSDK.framework to write into.")
        }
    }
}

tasks.matching { it.name.startsWith("assembleSmartScanSDK") && it.name.endsWith("XCFramework") }
    .configureEach { finalizedBy(embedPrivacyManifest) }
