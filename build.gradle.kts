group = "com.joelburton.klisp"
version = "1.0"

plugins {
    kotlin("jvm") version "2.1.20"
    id("com.gradleup.shadow") version "9.0.0-beta11"
}
kotlin { jvmToolchain(21) }

repositories { mavenCentral() }
dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jline:jline-terminal:3.29.0")
    implementation("org.jline:jline-reader:3.29.0")
    implementation("org.jline:jline-console:3.29.0")
}

tasks.test { useJUnitPlatform() }
tasks.jar {
    manifest { attributes["Main-Class"] = "com.joelburton.klisp.MainKt" }
}
