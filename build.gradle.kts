buildscript {
    dependencies {
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
    }
}

@Suppress( "DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.ksp).apply(false)
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
