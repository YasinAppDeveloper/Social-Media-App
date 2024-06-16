plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.instagram"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.instagram"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures{
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.lottie.v641)
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)
    implementation("com.google.android.gms:play-services-auth:21.2.0")
 //   implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.github.yalantis:ucrop:2.2.6")
 //   implementation("com.github.shts:StoriesProgressView:3.0.0")
   // implementation("com.android.support:multidex:2.0.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.github.OMARIHAMZA:StoryView:1.0.2-alpha")
    // Status View
    implementation ("com.github.3llomi:CircularStatusView:V1.0.2")
    //implementation("com.google.android.exoplayer:exoplayer:2.16.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("androidx.media3:media3-exoplayer:1.0.0")
    implementation("androidx.media3:media3-ui:1.0.0")
    implementation("androidx.media3:media3-common:1.0.0")
    // Add any other dependencies your project requires
    implementation("com.jakewharton.threetenabp:threetenabp:1.3.1")
    implementation("com.github.thunder413:DateTimeUtils:3.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
}
