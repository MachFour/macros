plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
}

group = "com.machfour"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.gson)
    // Not used yet - just have it here so that I will see when there's an update available.
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    implementation(libs.okio)
    // https://mvnrepository.com/artifact/org.apache.commons/commons-compress
    implementation(libs.apache.commons.compress)

    api(files("/home/max/devel/datestamp/build/libs/datestamp-jvm-1.0-SNAPSHOT.jar"))
    api(files("/home/max/devel/kotlin-csv/lib/build/libs/kotlin-csvlib-1.0-SNAPSHOT.jar"))

    // testing
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
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