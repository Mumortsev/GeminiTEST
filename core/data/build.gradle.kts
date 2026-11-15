plugins { id("com.android.library"); kotlin("android"); id("com.google.devtools.ksp") }
android { namespace = "ru.withbaby.data"; compileSdk = 35
 defaultConfig { minSdk = 26; ksp { arg("room.schemaLocation", "$projectDir/schemas") } }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
dependencies {
 implementation("androidx.exifinterface:exifinterface:1.4.0")
 api(project(":core:domain"))
 implementation("androidx.core:core-ktx:1.16.0")
 api("androidx.room:room-runtime:2.7.0"); api("androidx.room:room-ktx:2.7.0"); ksp("androidx.room:room-compiler:2.7.0")
 api("androidx.datastore:datastore-preferences:1.1.4")
 api("androidx.work:work-runtime-ktx:2.10.0")
 implementation("com.squareup.retrofit2:retrofit:2.11.0"); implementation("com.squareup.retrofit2:converter-gson:2.11.0")
 api("com.google.code.gson:gson:2.12.1")
}
