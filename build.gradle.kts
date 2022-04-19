plugins {
    kotlin("jvm") version("1.6.20")
}

group = "com.machfour"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

dependencies {
    implementation(kotlin("stdlib:1.6.20"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1-native-mt")
    implementation("net.sf.supercsv:super-csv:2.4.0")
    implementation("com.google.code.gson:gson:2.9.0")
    // Not used yet - just have it here so that I will see when there's an update available.
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")

    implementation(files("libs/ExprK.jar"))
    api(files("libs/datestamp.jar"))

    // testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.WARN
}

