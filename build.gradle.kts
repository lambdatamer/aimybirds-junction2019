buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.50")
        classpath("com.android.tools.build:gradle:3.5.1")
        classpath("com.getkeepsafe.dexcount:dexcount-gradle-plugin:0.8.6")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven("https://kotlin.bintray.com/kotlinx")
        maven("https://jitpack.io")
    }
}

configureRootProject {
    kotlinVersion = "1.3.50"
    version = "0.0.1"
    versionCode = 1
    compileSdk = 29
    minSdk = 21
    groupId = "com.justai.junction"
}