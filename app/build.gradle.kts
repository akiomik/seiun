@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktlint)
    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    namespace = "io.github.akiomik.seiun"
    compileSdk = 33

    @Suppress("UnstableApiUsage")
    defaultConfig {
        applicationId = "io.github.akiomik.seiun"
        minSdk = 28
        targetSdk = 33
        versionCode = 10
        versionName = "0.1.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    @Suppress("UnstableApiUsage")
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                file("proguard-rules.pro")
            )

            // Use debug config for testing proguard
            signingConfig = signingConfigs.getByName("debug")
        }

        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    @Suppress("UnstableApiUsage")
    buildFeatures {
        compose = true
    }

    @Suppress("UnstableApiUsage")
    composeOptions {
        kotlinCompilerExtensionVersion = "1.2.0"
    }

    @Suppress("UnstableApiUsage")
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    @Suppress("UnstableApiUsage")
    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file("debug.keystore")
            storePassword = "android"
            keyPassword = "android"
            keyAlias = "androiddebugkey"
        }
    }
}

dependencies {
    implementation(libs.bundles.androidx)
    implementation(libs.appcompat)
    implementation(libs.bundles.api)
    implementation(libs.bundles.moshi)
    ksp(libs.moshi.kotlin.codegen)
    implementation(libs.bundles.coil)
    implementation(libs.play.services.oss.licenses)
    implementation(libs.toolbar.compose)

    testImplementation(libs.bundles.test)
    androidTestImplementation(libs.bundles.android.test)
    debugImplementation(libs.bundles.debug)
}
