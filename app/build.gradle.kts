plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services") //use firebase
}
android {
    namespace = "com.example.iot_kotlin"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.iot_kotlin"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-database:20.3.1")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("androidx.preference:preference:1.2.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("pl.droidsonroids.gif:android-gif-drawable:1.2.22")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")


    implementation ("com.github.faizkhan12:mjpeg-view-android-kotlin:v1.0.0")
    //implementation ("com.github.TutorialsAndroid:GButton:v1.0.19")
    implementation ("com.google.android.gms:play-services-auth:19.0.0")
    //MultiDex
    implementation ("androidx.multidex:multidex:2.0.1")
    //Rounded ImageView 頭像物件
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    //implementation ("com.makeramen:roundedimageview:2.3.0")
    // 將ImageURL設定給ImageView顯示
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    //Firebase
    implementation("com.google.firebase:firebase-firestore:24.10.3")
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.3"))

    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")

    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries
}