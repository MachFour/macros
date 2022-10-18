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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8") // for use()
    implementation(project(":libmacros"))

    // dunno why this is needed here now - it worked before just having it in libmacros
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    implementation(files("../libs/lanterna-3.1.0-alpha1.jar"))
    implementation(files("../libs/sqlite-jdbc-3.34.0.jar"))

    // testing
    
    val junitVersion = "5.9.0"

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks {
    val sourcesJar by creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    val binaryJar by creating(Jar::class) {
        dependsOn(":libmacros:binaryJar")
        // from(java.sourceSets.main.get().allSource)
        from(configurations.runtimeClasspath.map {
                config -> config.map { if (it.isDirectory) it else zipTree(it) }
        })
        manifest.attributes["Main-Class"] = "com.machfour.macros.linux.LinuxMain"
        duplicatesStrategy = DuplicatesStrategy.WARN

    }
    artifacts {
        add("archives", binaryJar)
        add("archives", sourcesJar)
    }

}


//tasks.withType<Jar> { }
