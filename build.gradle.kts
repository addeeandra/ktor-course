group = "me.inibukanadit"
version = "1.0-SNAPSHOT"

plugins {
    application
    kotlin("jvm") version "1.3.61"
}

application.mainClassName = "MainKt"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    val ktorVersion = "1.2.6"

    implementation(kotlin("stdlib-jdk8"))
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}