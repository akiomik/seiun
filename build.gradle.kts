buildscript {
    val kotlinVersion by extra { "1.7.0" }
    val composeVersion by extra { "1.3.3" }
    val composeMaterialVersion by extra {  "1.3.1" }
    val material3Version by extra { "1.1.0-alpha08" }
    val retrofitVersion by extra { "2.9.0" }
    val moshiVersion by extra { "1.14.0" }
    val coilVersion by extra { "2.2.2" }

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
    }
} // Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    val kotlinVersion: String by extra

    id("com.android.application") version "7.4.2" apply false
    id("com.android.library") version "7.4.2" apply false
    id("org.jetbrains.kotlin.android") version kotlinVersion apply false
    id("com.google.devtools.ksp") version "$kotlinVersion-1.0.6" apply false
}

tasks {
    register("installGitHook", Copy::class) {
        from(File(rootProject.rootDir, "git-hooks/pre-commit"))
        from(File(rootProject.rootDir, "git-hooks/pre-push"))
        into { File(rootProject.rootDir, ".git/hooks") }
        fileMode = "0755".toInt(radix = 8)
    }

    getByPath(":app:preBuild").dependsOn(getByName("installGitHook"))
}
