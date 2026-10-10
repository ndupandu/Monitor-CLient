plugins {
    id("com.android.application")
}

// Alamat dashboard yang dibuka aplikasi. Dari variabel APP_URL (GitHub Actions) atau -PAPP_URL=... saat build lokal.
val appUrl: String = (System.getenv("APP_URL")?.takeIf { it.isNotBlank() }
    ?: (project.findProperty("APP_URL") as String?))?.trim().orEmpty()
require(appUrl.matches(Regex("https?://[^\"\\s]+"))) {
    "APP_URL belum diisi / tidak valid. Contoh: https://monitor.domainmu.com/  (lihat README.md)"
}

// Penandatanganan rilis: pakai keystore sendiri bila KEYSTORE_FILE diset, kalau tidak pakai kunci debug (tetap bisa dipasang).
val ksFile: String? = System.getenv("KEYSTORE_FILE")?.takeIf { it.isNotBlank() }

android {
    namespace = "com.pandu.monitoringclient"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.pandu.monitoringclient"
        minSdk = 24
        targetSdk = 34
        versionCode = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()
        versionName = "1.0.$versionCode"
        buildConfigField("String", "APP_URL", "\"$appUrl\"")
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        if (ksFile != null) {
            create("release") {
                storeFile = file(ksFile)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = if (ksFile != null) signingConfigs.getByName("release") else signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}
