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
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":libmacros"))

    // dunno why this is needed here now - it worked before just having it in libmacros
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    implementation(files("../libs/lanterna-3.1.0-alpha1.jar"))
    implementation(files("../libs/sqlite-jdbc-3.44.0.0.jar"))
    implementation(files("../libs/slf4j-api-1.7.36.jar"))
    implementation(files("../libs/slf4j-simple-1.7.36.jar"))

    // testing
    val junitVersion = "5.9.0"

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
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
