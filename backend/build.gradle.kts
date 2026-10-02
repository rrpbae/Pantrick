plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

group = "com.pantrick"
version = "1.0.0"


application {
    mainClass.set("com.pantrick.backend.ApplicationKt")
}

dependencies {

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.logback.classic)
    implementation(libs.jbcrypt)
    implementation(libs.commons.csv)

    testImplementation(libs.ktor.server.test.host)
    testImplementation("io.ktor:ktor-client-content-negotiation-jvm:3.0.3")
    testImplementation(libs.junit)
}

