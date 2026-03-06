plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.sprotte.geofencer.demo"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = project.findProperty("APPLICATION_ID") as String? ?: "com.sprotte.geofencer"
        minSdk = 23
        targetSdk = 36
        versionCode = (project.findProperty("versionCode") as String?)?.toIntOrNull() ?: 1
        versionName = project.findProperty("VERSION_NAME") as String? ?: "3.0.0"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file(project.findProperty("DEBUG_KEYSYORE_PATH") as String? ?: "../debug.jks")
            storePassword = project.findProperty("DEBUG_STORE_PASSWORD") as String? ?: ""
            keyAlias = project.findProperty("DEBUG_KEYSTORE_ALLIAS") as String? ?: "debug"
            keyPassword = project.findProperty("DEBUG_KEY_PASSWORD") as String? ?: ""
        }
        create("release") {
            storeFile = file(project.findProperty("RELEASE_KEYSYORE_PATH") as String? ?: "../release.jks")
            storePassword = project.findProperty("RELEASE_STORE_PASSWORD") as String? ?: ""
            keyAlias = project.findProperty("RELEASE_KEYSTORE_ALIAS") as String? ?: "release"
            keyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as String? ?: ""
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    bundle {
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
        language {
            enableSplit = false
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        disable += "InvalidPackage"
        abortOnError = false
        checkAllWarnings = false
        checkReleaseBuilds = false
        ignoreWarnings = true
        quiet = true
        ignoreTestSources = true
        checkDependencies = true
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    compilerOptions {
        allWarningsAsErrors = false
        verbose = true
    }
}

configurations.configureEach {
    resolutionStrategy {
        capabilitiesResolution.all { selectHighestVersion() }

        force("org.jetbrains.kotlin:kotlin-stdlib:2.3.10")
        force("org.jetbrains.kotlin:kotlin-stdlib-common:2.3.10")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.3.10")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.3.10")
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(project(":geofencer"))

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.timber)

    implementation(libs.maps.utils.ktx)
    implementation(libs.maps.ktx)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    implementation(libs.navigation.ui.ktx)
    implementation(libs.navigation.fragment.ktx)

    implementation(libs.preference.ktx)
    implementation(libs.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.work.runtime.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.ktx)
}
