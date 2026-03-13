plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.p2pnodereciever"
    compileSdk {
        version = release(36)
    }

    packaging {
        resources {
            pickFirsts +=
                listOf(
                    "META-INF/io.netty.versions.properties",
                    "META-INF/native-image/io.netty.incubator/netty-incubator-codec-native-quic/*",
                    "META-INF/native-image/io.netty/netty-codec-native-quic/*",
                    "META-INF/*.SF",
                    "META-INF/*.DSA",
                    "META-INF/*.RSA",
                    "META-INF/INDEX.LIST",
                    "META-INF/license/*",
                )
        }
    }

    configurations.all {
        exclude(
            group = "org.bouncycastle",
            module = "bcpkix-jdk18on"
        )
    }

    defaultConfig {
        applicationId = "com.example.p2pnodereciever"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.nabu) {
        exclude(
            group = "org.bouncycastle",
            module = "bcutil-jdk15on"
        )
        exclude(
            group = "org.bouncycastle",
            module = "bcprov-jdk15on"
        )
        exclude(
            group = "tech.pegasys",
            module = "noise-java"
        )
    }

    implementation(libs.noise.java)
}