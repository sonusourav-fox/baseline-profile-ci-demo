plugins {
    id("com.android.test")
    id("org.jetbrains.kotlin.android")
    id("androidx.baselineprofile")
}

android {
    namespace = "com.example.baselineprofiledemo.baselineprofile"
    compileSdk = 35
    targetProjectPath = ":app"

    defaultConfig {
        minSdk = 28
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // GMD used when baselineprofile.useConnectedDevices=false (default).
    // API 34 + aosp: no root required for BaselineProfileRule on API 33+.
    testOptions.managedDevices.devices {
        create<com.android.build.api.dsl.ManagedVirtualDevice>("pixel6Api34") {
            device = "Pixel 6"
            apiLevel = 34
            systemImageSource = "aosp"
        }
    }
}

val useConnectedDevicesProp = providers
    .gradleProperty("baselineprofile.useConnectedDevices")
    .map(String::toBoolean)
    .orElse(false)

baselineProfile {
    managedDevices += "pixel6Api34"
    useConnectedDevices = useConnectedDevicesProp.get()
}

dependencies {
    implementation("androidx.test:runner:1.6.2")
    implementation("androidx.test.ext:junit:1.2.1")
    implementation("androidx.test.uiautomator:uiautomator:2.3.0")
    implementation("androidx.benchmark:benchmark-macro-junit4:1.5.0")
}
