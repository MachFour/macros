plugins {
    application
    kotlin("jvm") version("1.6.0")
}

group = "com.machfour"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.machfour.macros.linux.LinuxMain")
}

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
    implementation(kotlin("stdlib:1.6.0"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2-native-mt")
    implementation("net.sf.supercsv:super-csv:2.4.0")
    implementation("com.google.code.gson:gson:2.8.9")

    // for lanterna, sqlite-jdbc, ExprK
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    // testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
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
