plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val releaseVersion = providers.gradleProperty("releaseVersion").orElse("1.0.0-dev")
val releaseVersionCode = providers.gradleProperty("releaseVersionCode").orElse("1")
val releaseStoreFile = providers.environmentVariable("RELEASE_STORE_FILE")
val releaseStorePassword = providers.environmentVariable("RELEASE_STORE_PASSWORD")
val releaseKeyAlias = providers.environmentVariable("RELEASE_KEY_ALIAS")
val releaseKeyPassword = providers.environmentVariable("RELEASE_KEY_PASSWORD")

android {
    // Keep the source namespace stable; the application ID is the public Android identity.
    namespace = "com.example.refractiveindexapp"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "dev.xpr3ss0.refractiveindexapp"
        minSdk = 24
        targetSdk = 36
        versionCode = releaseVersionCode.get().toInt()
        versionName = releaseVersion.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(releaseStoreFile.orNull ?: "missing-release-keystore.jks")
            storePassword = releaseStorePassword.orNull
            keyAlias = releaseKeyAlias.orNull
            keyPassword = releaseKeyPassword.orNull
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.room.runtime) {
        exclude(
            group = "com.intellij",
            module = "annotations"
        )
    }
    implementation(libs.androidx.room.compiler) {
        exclude(
            group = "com.intellij",
            module = "annotations"
        )
    }

    implementation(libs.androidx.room.ktx)
    implementation(libs.snakeyaml)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.markwon.core)
    implementation(project(":scientificPlot"))
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
