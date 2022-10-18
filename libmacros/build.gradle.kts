import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("com.google.code.gson:gson:2.9.0")
    // Not used yet - just have it here so that I will see when there's an update available.
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")

    api(files("../libs/datestamp.jar"))
    api(files("/home/max/devel/kotlin-csv/lib/build/libs/kotlin-csvlib-1.0-SNAPSHOT.jar"))

    // testing
    val junitVersion = "5.9.0"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.WARN
}

tasks {
    val sourcesJar by creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    val binaryJar by creating(Jar::class) {
        // from(java.sourceSets.main.get().allSource)
        manifest {
            attributes["Manifest-Version"] = 1.0
            attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(" ") { it.name }
        }

        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    artifacts {
        add("archives", sourcesJar)
        add("archives", binaryJar)
    }

}

