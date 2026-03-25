plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
    // Firebase - ENABLED
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.ludoblitz.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ludoblitz.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // AdMob Test IDs - Will be overridden by Firebase Remote Config
        buildConfigField("String", "ADMOB_BANNER_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
        buildConfigField("String", "ADMOB_REWARDED_ID", "\"ca-app-pub-3940256099942544/5224354917\"")
        
        // Web Client ID for Google Sign-In (from your google-services.json)
        buildConfigField("String", "WEB_CLIENT_ID", "\"942368836760-10rhtnrrdbn2egg4mma6a7mpbs51sob8.apps.googleusercontent.com\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    
    // Lifecycle Components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    
    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Dependency Injection - Hilt (using KSP)
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    
    // Room Database (for local game saves) - using KSP
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Lottie Animations
    implementation("com.airbnb.android:lottie:6.3.0")
    
    // Glide for Images - using KSP
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:compiler:4.16.0")
    
    // Circle ImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")
    
    // Shimmer Effect
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    
    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
    
    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    
    // AdMob
    implementation("com.google.android.gms:play-services-ads:22.6.0")
    
    // Billing (IAP)
    implementation("com.android.billingclient:billing-ktx:6.1.0")
    
    // Media
    implementation("androidx.media:media:1.7.0")
    
    // ========================================
    // FIREBASE DEPENDENCIES (BoM - Bill of Materials)
    // ========================================
    // Import the Firebase BoM - This automatically manages versions
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    
    // Firebase SDKs - No version needed when using BoM
    // Authentication
    implementation("com.google.firebase:firebase-auth-ktx")
    
    // Realtime Database
    implementation("com.google.firebase:firebase-database-ktx")
    
    // Firestore Database
    implementation("com.google.firebase:firebase-firestore-ktx")
    
    // Remote Config - For dynamic app control
    implementation("com.google.firebase:firebase-config-ktx")
    
    // Cloud Messaging (Push Notifications)
    implementation("com.google.firebase:firebase-messaging-ktx")
    
    // Analytics
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // Crashlytics
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    
    // ========================================
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
