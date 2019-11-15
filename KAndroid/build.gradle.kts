import com.android.build.gradle.BaseExtension

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
}

android {
    val config = rootProject.rootProjectConfig
    configure<BaseExtension> {
        compileSdkVersion(config.compileSdk)

        defaultConfig {
            minSdkVersion(config.minSdk)
            targetSdkVersion(config.compileSdk)

            versionName = config.version
            versionCode = config.versionCode
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        lintOptions {
            isCheckAllWarnings = true
            isAbortOnError = true
        }

        buildTypes {
            getByName("debug") {
                lintOptions {
                    isWarningsAsErrors = true
                }
            }
            getByName("release") {
                lintOptions {
                    isWarningsAsErrors = true
                }
            }
        }

        dataBinding {
            isEnabled = true
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk7"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.1")

    implementation("androidx.appcompat:appcompat:1.1.0")

    implementation("androidx.core:core-ktx:1.1.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.1.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("com.google.android.material:material:1.0.0")

    implementation("androidx.navigation:navigation-fragment-ktx:2.2.0-beta01")
    implementation("androidx.navigation:navigation-ui-ktx:2.2.0-beta01")

    implementation("com.squareup.retrofit2:retrofit:2.5.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.2.1")
    implementation("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")
    implementation("com.github.salomonbrys.kotson:kotson:2.5.0")
    implementation("com.squareup.retrofit2:converter-gson:2.5.0")


    implementation("org.kodein.di:kodein-di-framework-android-x:6.3.3")
    implementation("org.kodein.di:kodein-di-generic-jvm:6.3.3")

    implementation("com.github.bumptech.glide:glide:4.9.0")
    kapt("com.github.bumptech.glide:compiler:4.9.0")

    implementation("androidx.room:room-runtime:2.2.0")
    implementation("androidx.room:room-ktx:2.2.0")
    kapt("androidx.room:room-compiler:2.2.0")

    implementation("com.github.yandextaxitech:binaryprefs:1.0.1")
    implementation("com.google.crypto.tink:tink-android:1.2.2")
}
