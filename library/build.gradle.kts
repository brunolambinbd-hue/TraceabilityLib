plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.parabd.lib.traceability"
    compileSdk = libs.versions.sdkCompile.get().toString().toInt()

    defaultConfig {
        minSdk = libs.versions.sdkMin.get().toString().toInt()
        consumerProguardFiles("consumer-rules.pro")
    }

    publishing {
        singleVariant("release")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get().toString())
        targetCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get().toString())
    }
    kotlinOptions {
        jvmTarget = libs.versions.javaVersion.get().toString()
    }
}
dependencies {
    testImplementation(libs.junit.junit)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.parabd.lib"
                artifactId = "traceability"
                version = "1.0.0"
            }
        }
    }
}


