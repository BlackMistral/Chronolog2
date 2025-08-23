toml
[versions]
# SDK Versions
android-compileSdk = "34"
android-minSdk = "24" # Or your desired minimum SDK
android-targetSdk = "34"

# AndroidX Libraries
core-ktx = "1.12.0"
appcompat = "1.6.1"
material = "1.10.0"
constraintlayout = "2.1.4"
recyclerview = "1.3.2"
security-crypto = "1.0.0"

# Kotlin
kotlin = "1.9.0" # Use your Kotlin version

# Coroutines
coroutines = "1.7.3"

# Lifecycle
lifecycle = "2.6.2" # Use the latest stable version

# Hilt
hilt = "2.48" # Use the latest stable version

# Room
room = "2.6.0" # Use the latest stable version

# WorkManager
work = "2.8.1" # Use the latest stable version

# Google API Client (for Google Drive - specify versions)
# You need to find the correct versions for the Google Drive API client libraries
# These are examples, verify the latest recommended versions for Android
google-api-client = "1.32.1"
google-api-services-drive = "v3-rev20230822-2.0.0"
google-auth-library-oauth2-http = "1.19.0"
# google-oauth-client-jetty = "1.34.1" # Example, often not needed for Android

# Logging
timber = "5.0.1" # Use the latest stable version

# Testing
junit = "4.13.2"
androidx-test-ext-junit = "1.1.5"
androidx-test-espresso-core = "3.5.1"

[libraries]
# AndroidX Libraries
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "core-ktx" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
android-material = { group = "com.google.android.material", name = "material", version.ref = "material" }
androidx-constraintlayout = { group = "androidx.constraintlayout", name = "constraintlayout", version.ref = "constraintlayout" }
androidx-recyclerview = { group = "androidx.recyclerview", name = "recyclerview", version.ref = "recyclerview" }
androidx-security-crypto = { group = "androidx.security", name = "security-crypto", version.ref = "security-crypto" }

# Kotlin
kotlin-stdlib = { group = "org.jetbrains.kotlin", name = "kotlin-stdlib", version.ref = "kotlin" }

# Coroutines
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" } # For testing

# Lifecycle
androidx-lifecycle-livedata-ktx = { group = "androidx.lifecycle", name = "lifecycle-livedata-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-ktx = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-ktx", version.ref = "lifecycle" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-android-testing = { group = "com.google.dagger", name = "hilt-android-testing", version.ref = "hilt" }

# Room
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }

# WorkManager
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work" }
androidx-work-hilt = { group = "androidx.work", name = "work-hilt", version.ref = "work" }
androidx-work-testing = { group = "androidx.work", name = "work-testing", version.ref = "work" } # For testing

# Google API Client (for Google Drive)
google-api-client = { group = "com.google.api-client", name = "google-api-client", version.ref = "google-api-client" }
google-api-services-drive = { group = "com.google.apis", name = "google-api-services-drive", version.ref = "google-api-services-drive" }
google-auth-library-oauth2-http = { group = "com.google.auth", name = "google-auth-library-oauth2-http", version.ref = "google-auth-library-oauth2-http" }
# google-oauth-client-jetty = { group = "com.google.oauth-client", name = "google-oauth-client-jetty", version.ref = "google-oauth-client-jetty" } # Example

# Logging
timber = { group = "com.jakewharton.timber", name = "timber", version.ref = "timber" }

# Testing
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-test-ext-junit = { group = "androidx.test.ext", name = "junit", version.ref = "androidx-test-ext-junit" }
androidx-test-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "androidx-test-espresso-core" }

[bundles]
# Example bundle for common UI libraries
androidx-ui = [
    libs.androidx.core.ktx,
    libs.androidx.appcompat,
    libs.android.material,
    libs.androidx.constraintlayout,
