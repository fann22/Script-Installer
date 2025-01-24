
plugins {
    id("com.android.application")
    
}

android {
    namespace = "dev.trindade.shizuku.example"
    compileSdk = 35
    
    defaultConfig {
        applicationId = "dev.trindade.shizuku.example"
        minSdk = 26
        //noinspection EditedTargetSdkVersion
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        
        vectorDrawables { 
            useSupportLibrary = true
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        
    }
    
}

dependencies {


    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.9.0")
    implementation("dev.rikka.shizuku:api:12.1.0")
    implementation("dev.rikka.shizuku:provider:12.1.0")

}
