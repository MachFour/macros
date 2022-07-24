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
    implementation(kotlin("stdlib:1.7.10"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.10") // for use()
    implementation(project(":libmacros"))

    implementation(files("../libs/lanterna-3.1.0-alpha1.jar"))
    implementation(files("../libs/sqlite-jdbc-3.34.0.jar"))

    // testing

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    // This code recursively collects and copies all of a project's files and adds them to the JAR itself.
    // One can extend this task, to skip certain files or particular types at will
    from(configurations.runtimeClasspath.map {
            config -> config.map { if (it.isDirectory) it else zipTree(it) }
    })

    manifest.attributes["Main-Class"] = "com.machfour.macros.linux.LinuxMain"

    duplicatesStrategy = DuplicatesStrategy.WARN
}
