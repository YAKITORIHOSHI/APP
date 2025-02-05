plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services") version "4.4.2"
}

android {
    namespace = "com.test.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.test.app"
        minSdk = 29
        targetSdk = 35
        versionCode = 8
        versionName = "5.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging {
        resources.excludes += listOf(
            "META-INF/LICENSE.md",
            "META-INF/NOTICE.md",
            "META-INF/INDEX.LIST",
            "META-INF/DEPENDENCIES",
            "META-INF/io.netty.versions.properties"
        )
    }

    signingConfigs {
        create("release") {
            storeFile = file("release/naphskrt.jks") // Path to your keystore file
            storePassword = "Naphtali_124"     // Keystore password
            keyAlias = "key042303"                  // Key alias
            keyPassword = "Naphtali_124"            // Key password
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

}

dependencies {
    // Material Design
    implementation(libs.material3)
    implementation(libs.androidx.material.icons.extended)

    // AndroidX Core Libraries
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.gridlayout)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.google.firebase.firestore)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Firebase (BOM ensures consistent versions)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.google.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database.ktx)

    // Gmail API and OAuth
    implementation(libs.google.api.client)
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.client.gson)
    implementation(libs.google.oauth.client.jetty)
    implementation(libs.google.api.services.gmail)
    implementation(libs.google.auth.library.oauth2.http)
    implementation(libs.google.http.client.jackson2.v1423)

    // Java Mail API (Remove javax.mail)
    implementation(libs.android.mail)
    implementation(libs.android.activation)

    // HTTP Client and JSON Processing
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind.v2182)
    implementation(libs.jackson.annotations)
    implementation(libs.sendgrid.java)

    // Unit Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
