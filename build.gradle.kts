plugins {
    id("java")
    kotlin("jvm") version("1.5.21")
}

group = "com.machfour"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin JVM standard library
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation("net.sf.supercsv:super-csv:2.4.0")
    implementation("com.google.code.gson:gson:2.8.6")

    // for lanterna, sqlite-jdbc, ExprK
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    // testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.3")
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.WARN
}

tasks.withType<Jar> {
    // This code recursively collects and copies all of a project's files and adds them to the JAR itself.
    // One can extend this task, to skip certain files or particular types at will
    from(configurations.runtimeClasspath.map {
            config -> config.map { if (it.isDirectory) it else zipTree(it) }
    })
    manifest {
        attributes["Main-Class"] = "com.machfour.macros.linux.LinuxMain"
    }
    duplicatesStrategy = DuplicatesStrategy.WARN
}