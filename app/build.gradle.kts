plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "app.xiguang"
    compileSdk = 37

    defaultConfig {
        applicationId = "app.xiguang"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file(
                providers.environmentVariable("XIGUANG_RELEASE_KEYSTORE_PATH")
                    .orElse("missing-release-keystore.jks")
                    .get(),
            )
            storePassword = providers.environmentVariable("XIGUANG_RELEASE_STORE_PASSWORD")
                .orElse("")
                .get()
            keyAlias = providers.environmentVariable("XIGUANG_RELEASE_KEY_ALIAS")
                .orElse("")
                .get()
            keyPassword = providers.environmentVariable("XIGUANG_RELEASE_KEY_PASSWORD")
                .orElse("")
                .get()
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

val releaseSigningEnvironment = listOf(
    "XIGUANG_RELEASE_KEYSTORE_PATH",
    "XIGUANG_RELEASE_STORE_PASSWORD",
    "XIGUANG_RELEASE_KEY_ALIAS",
    "XIGUANG_RELEASE_KEY_PASSWORD",
)

val verifyReleaseSigningEnvironment by tasks.registering {
    group = "verification"
    description = "Checks that the stable XiGuang release signing credentials are available."

    doLast {
        val missingVariables = releaseSigningEnvironment.filter { System.getenv(it).isNullOrBlank() }
        check(missingVariables.isEmpty()) {
            "Missing release signing environment variables: ${missingVariables.joinToString()}"
        }

        val keystorePath = System.getenv("XIGUANG_RELEASE_KEYSTORE_PATH")
        check(file(keystorePath).isFile) {
            "Release keystore does not exist: $keystorePath"
        }
    }
}

tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    dependsOn(verifyReleaseSigningEnvironment)
}
