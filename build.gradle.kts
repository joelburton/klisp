plugins {
    kotlin("jvm") version "2.1.20"
    id("com.gradleup.shadow") version "9.0.0-beta11"
    application
}

repositories { mavenCentral() }
dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jline:jline:3.29.0")
}

group = "com.joelburton.klisp"
version = "1.0-SNAPSHOT"

application { mainClass = "com.joelburton.klisp.MainKt" }
tasks.test { useJUnitPlatform() }
kotlin { jvmToolchain(21) }

tasks.jar {
    manifest { attributes["Main-Class"] = "com.joelburton.klisp.MainKt" }
}
