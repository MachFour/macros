import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
}

group = "com.machfour"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.machfour.macros.linux.LinuxMain")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":libmacros"))
    implementation(libs.kotlin.stdlib)

    // dunno why this is needed here now - it worked before just having it in libmacros
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    implementation(files("../libs/lanterna-3.1.0-alpha1.jar"))
    implementation(files("../libs/sqlite-jdbc-3.49.1.0.jar"))
    implementation(files("../libs/slf4j-api-1.7.36.jar"))
    implementation(files("../libs/slf4j-simple-1.7.36.jar"))

    // testing
    testImplementation(libs.kotlin.test)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

// https://stackoverflow.com/a/60068986
tasks.register<Jar>(name = "sourceJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

// apparently this is needed
tasks.named<KotlinCompile>("compileKotlin") {
    dependsOn(":libmacros:binaryJar")
}

tasks.register<Jar>(name = "binaryJar") {
    dependsOn(":libmacros:jar")

    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.map {
        config -> config.map { if (it.isDirectory) it else zipTree(it) }
    })
    manifest.attributes["Main-Class"] = "com.machfour.macros.linux.LinuxMain"
    duplicatesStrategy = DuplicatesStrategy.WARN
}
