import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.20"
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

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("com.google.code.gson:gson:2.10.1")
    // Not used yet - just have it here so that I will see when there's an update available.
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    api(files("/home/max/devel/datestamp/build/libs/datestamp-jvm-1.0-SNAPSHOT.jar"))
    api(files("/home/max/devel/kotlin-csv/lib/build/libs/kotlin-csvlib-1.0-SNAPSHOT.jar"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")

    // testing
    val junitVersion = "5.9.0"
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.WARN
}

// https://stackoverflow.com/a/60068986
tasks.register<Jar>(name = "sourceJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

tasks.register<Jar>(name = "binaryJar") {
    manifest {
        attributes["Manifest-Version"] = 1.0
        attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(" ") { it.name }
    }
    from(sourceSets.main.get().output)

    duplicatesStrategy = DuplicatesStrategy.WARN
}