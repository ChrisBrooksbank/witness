plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

val witnessNodeBaseUrl = providers
    .gradleProperty("witnessNodeBaseUrl")
    .orElse(providers.environmentVariable("WITNESS_NODE_BASE_URL"))

val witnessReleaseStoreFile = providers.environmentVariable("WITNESS_RELEASE_STORE_FILE").orNull
val witnessReleaseStorePassword = providers.environmentVariable("WITNESS_RELEASE_STORE_PASSWORD").orNull
val witnessReleaseKeyAlias = providers.environmentVariable("WITNESS_RELEASE_KEY_ALIAS").orNull
val witnessReleaseKeyPassword = providers.environmentVariable("WITNESS_RELEASE_KEY_PASSWORD").orNull
val witnessReleaseSigningConfigured = listOf(
    witnessReleaseStoreFile,
    witnessReleaseStorePassword,
    witnessReleaseKeyAlias,
    witnessReleaseKeyPassword,
).all { !it.isNullOrBlank() }

fun quotedBuildConfigValue(value: String): String {
    return "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""
}

android {
    namespace = "org.witness.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.witness.app"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0-pre-alpha.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    signingConfigs {
        if (witnessReleaseSigningConfigured) {
            create("witnessRelease") {
                storeFile = file(witnessReleaseStoreFile.orEmpty())
                storePassword = witnessReleaseStorePassword
                keyAlias = witnessReleaseKeyAlias
                keyPassword = witnessReleaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField(
                "String",
                "WITNESS_NODE_BASE_URL",
                quotedBuildConfigValue("http://10.0.2.2:8080/"),
            )
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField(
                "String",
                "WITNESS_NODE_BASE_URL",
                quotedBuildConfigValue(witnessNodeBaseUrl.orNull.orEmpty()),
            )
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (witnessReleaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("witnessRelease")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

detekt {
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.work.runtime.ktx)

    ksp(libs.room.compiler)
    testImplementation(libs.junit)
}

tasks.register("validateWitnessReleaseBackendUrl") {
    val configuredUrl = witnessNodeBaseUrl.orNull.orEmpty()
    inputs.property("witnessNodeBaseUrl", configuredUrl)

    doLast {
        val invalidLocalValues = listOf(
            "",
            "http://10.0.2.2:8080/",
            "http://localhost:8080/",
            "http://127.0.0.1:8080/",
        )
        require(configuredUrl !in invalidLocalValues && configuredUrl.startsWith("https://")) {
            "Release builds require -PwitnessNodeBaseUrl=https://your-domain/ " +
                "or WITNESS_NODE_BASE_URL=https://your-domain/."
        }
        require(configuredUrl.endsWith("/")) {
            "witnessNodeBaseUrl must end with a trailing slash, for example https://witness.example.org/."
        }
    }
}

tasks.register("validateWitnessReleaseSigning") {
    val signingConfigured = witnessReleaseSigningConfigured
    inputs.property("witnessReleaseSigningConfigured", signingConfigured)

    doLast {
        if (!signingConfigured) {
            throw GradleException(
                "Signed release APKs require WITNESS_RELEASE_STORE_FILE, " +
                    "WITNESS_RELEASE_STORE_PASSWORD, WITNESS_RELEASE_KEY_ALIAS, and WITNESS_RELEASE_KEY_PASSWORD.",
            )
        }
    }
}

tasks.matching { task ->
    task.name in listOf("assembleRelease", "bundleRelease")
}.configureEach {
    dependsOn("validateWitnessReleaseBackendUrl")
}
