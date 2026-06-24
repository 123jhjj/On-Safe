import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val signingDir = System.getenv("ONSAFE_SIGNING_DIR")?.let { file(it) }
    ?: rootProject.file("../keystore/On-Safe")
val keyPropertiesFile = signingDir.resolve("key.properties")
check(keyPropertiesFile.exists()) {
    "key.properties not found at ${keyPropertiesFile.absolutePath}. Set ONSAFE_SIGNING_DIR if using a custom location."
}
val keyProperties = Properties().apply {
    load(keyPropertiesFile.inputStream())
}

android {
    namespace = "app.skons.onsafe"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.skons.onsafe"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    signingConfigs {
        create("release") {
            keyAlias = keyProperties.getProperty("keyAlias")
            keyPassword = keyProperties.getProperty("keyPassword")
            storeFile = keyProperties.getProperty("storeFile")?.let { signingDir.resolve(it) }
            storePassword = keyProperties.getProperty("storePassword")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.play.services.location)
    implementation(libs.accompanist.permissions)
    implementation(libs.coil.compose)
    implementation(libs.material)
    implementation(libs.reorderable)
    implementation(libs.androidx.security.crypto)
    debugImplementation(libs.androidx.ui.tooling)
}
