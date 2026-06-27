import java.util.Base64

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

// ── Stable signing key ──────────────────────────────────────────
// All builds (debug & release fallback) use the same debug.keystore.
// The base64 encoding is committed to git so every machine/clone
// produces APKs with the identical signature → updates install
// without "uninstall first" errors.
// ────────────────────────────────────────────────────────────────

val keystoreFile = file("${rootDir}/debug.keystore")
val base64File = file("${rootDir}/debug.keystore.base64")

// 1) If base64 artifact exists, restore the keystore from it (reproducible builds)
if (base64File.exists()) {
  try {
    val raw = base64File.readText().trim()
      .replace("\r", "")
      .replace("\n", "")
      .replace(" ", "")
    keystoreFile.writeBytes(Base64.getDecoder().decode(raw))
    logger.lifecycle("Restored debug.keystore from debug.keystore.base64 (stable signature)")
  } catch (e: Exception) {
    logger.error("Failed to decode debug.keystore.base64: ${e.message}", e)
  }
}

// 2) Auto‑generate keystore on first build (fresh checkout / no base64 yet)
if (!keystoreFile.exists()) {
  logger.warn("debug.keystore not found. Generating a new one (happens once)...")
  try {
    val proc = ProcessBuilder(
      "keytool", "-genkey", "-v",
      "-keystore", keystoreFile.absolutePath,
      "-alias", "androiddebugkey",
      "-storepass", "android",
      "-keypass", "android",
      "-keyalg", "RSA",
      "-keysize", "2048",
      "-validity", "10000",
      "-dname", "CN=Android Debug, O=Android, C=US"
    ).inheritIO().start()
    check(proc.waitFor() == 0) { "keytool failed" }
    // Immediately write the base64 counterpart for future builds
    base64File.writeText(Base64.getEncoder().encodeToString(keystoreFile.readBytes()).chunked(64).joinToString("\n"))
    logger.lifecycle("Generated debug.keystore + debug.keystore.base64 — commit this file to share the same key across machines")
  } catch (e: Exception) {
    logger.error("Could not auto‑generate debug.keystore: ${e.message}")
    logger.error("Run manually: keytool -genkey -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname \"CN=Android Debug, O=Android, C=US\"")
  }
}

// ── Version from version.json (single source of truth) ───────────
// versionCode is computed from the semantic version so it always
// increases when versionName is bumped – no more manual increments.
// ─────────────────────────────────────────────────────────────────

val appVersion: String = try {
  val json = file("${rootDir}/version.json")
  if (json.exists()) {
    "\"version\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(json.readText())?.groupValues?.get(1) ?: "1.0.0"
  } else "1.0.0"
} catch (_: Exception) { "1.0.0" }

val verParts = appVersion.split(".").map { it.toIntOrNull() ?: 0 }
val appVersionCode = when {
  verParts.size >= 3 -> verParts[0] * 1_000_000 + verParts[1] * 1_000 + verParts[2]
  verParts.size == 2 -> verParts[0] * 1_000 + verParts[1]
  else               -> verParts[0]
}

// ── Android configuration ─────────────────────────────────────

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.gestordeproducao.xqwzpt"
    minSdk = 24
    targetSdk = 36
    versionCode = appVersionCode
    versionName = appVersion

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val ks = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(ks)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    getByName("debug") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

      val ksPath = System.getenv("KEYSTORE_PATH")
      val hasKs = ksPath != null && file(ksPath).exists()
      val hasPwd = !System.getenv("STORE_PASSWORD").isNullOrEmpty()
      signingConfig = if (hasKs && hasPwd) signingConfigs.getByName("release")
                      else signingConfigs.getByName("debug")
    }
    debug {
      signingConfig = signingConfigs.getByName("debug")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.camera.camera2)
  implementation(libs.androidx.camera.core)
  implementation(libs.androidx.camera.lifecycle)
  implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.code.scanner)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.play.services.auth)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
